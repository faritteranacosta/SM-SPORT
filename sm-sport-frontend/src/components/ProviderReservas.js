import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ProviderReservas = () => {
  const [reservas, setReservas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [accionLoading, setAccionLoading] = useState(null);
  const [filtroEstado, setFiltroEstado] = useState('TODAS');

  useEffect(() => {
    const cargarReservas = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/v1/reservas');
        setReservas(response.data.content || response.data || []);
      } catch (err) {
        setError('Error al cargar las reservas. Por favor, intenta nuevamente.');
        console.error('Error al cargar reservas:', err);
      } finally {
        setLoading(false);
      }
    };

    cargarReservas();
  }, []);

  const confirmarReserva = async (idReserva) => {
    if (!window.confirm('¿Estás seguro de que deseas confirmar esta reserva?')) {
      return;
    }

    try {
      setAccionLoading(idReserva);
      
      await axios.post(`/api/v1/reservas/${idReserva}/confirmar`);

      // Actualizar la lista de reservas
      setReservas(prev => 
        prev.map(reserva => 
          reserva.idReserva === idReserva 
            ? { ...reserva, estado: 'CONFIRMADA' }
            : reserva
        )
      );

      alert('Reserva confirmada exitosamente');
    } catch (err) {
      console.error('Error al confirmar reserva:', err);
      alert(err.response?.data?.message || 'Error al confirmar la reserva');
    } finally {
      setAccionLoading(null);
    }
  };

  const rechazarReserva = async (idReserva) => {
    const motivo = prompt('Por favor, indica el motivo del rechazo:');
    if (!motivo) return;

    try {
      setAccionLoading(idReserva);
      
      await axios.post(`/api/v1/reservas/${idReserva}/rechazar`, {
        motivoRechazo: motivo
      });

      // Actualizar la lista de reservas
      setReservas(prev => 
        prev.map(reserva => 
          reserva.idReserva === idReserva 
            ? { ...reserva, estado: 'RECHAZADA' }
            : reserva
        )
      );

      alert('Reserva rechazada exitosamente');
    } catch (err) {
      console.error('Error al rechazar reserva:', err);
      alert(err.response?.data?.message || 'Error al rechazar la reserva');
    } finally {
      setAccionLoading(null);
    }
  };

  const getEstadoColor = (estado) => {
    switch (estado) {
      case 'PENDIENTE': return '#ffc107';
      case 'CONFIRMADA': return '#28a745';
      case 'RECHAZADA': return '#dc3545';
      case 'CANCELADA': return '#6c757d';
      case 'FINALIZADA': return '#17a2b8';
      default: return '#6c757d';
    }
  };

  const getEstadoIcon = (estado) => {
    switch (estado) {
      case 'PENDIENTE': return '⏳';
      case 'CONFIRMADA': return '✅';
      case 'RECHAZADA': return '❌';
      case 'CANCELADA': return '🚫';
      case 'FINALIZADA': return '✨';
      default: return '📅';
    }
  };

  const formatearFecha = (fecha) => {
    return new Date(fecha).toLocaleDateString('es-CO', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatearHora = (hora) => {
    const [h, m] = hora.split(':');
    const hour = parseInt(h);
    const ampm = hour >= 12 ? 'p. m.' : 'a. m.';
    const displayHour = hour > 12 ? hour - 12 : hour === 0 ? 12 : hour;
    return `${displayHour}:${m} ${ampm}`;
  };

  const reservasFiltradas = filtroEstado === 'TODAS' 
    ? reservas 
    : reservas.filter(r => r.estado === filtroEstado);

  const contarPorEstado = (estado) => {
    return reservas.filter(r => r.estado === estado).length;
  };

  if (loading) {
    return <div className="auth-container">Cargando reservas...</div>;
  }

  return (
    <div className="auth-container">
      <h2>Gestión de Reservas</h2>
      {error && <div className="error-message">{error}</div>}
      
      {reservas.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📅</div>
          <h3>No tienes reservas aún</h3>
          <p>Los clientes podrán reservar tus servicios pronto</p>
        </div>
      ) : (
        <div className="reservas-container">
          {/* Estadísticas */}
          <div className="reservas-stats">
            <div className="stat-card">
              <span className="stat-number">{reservas.length}</span>
              <span className="stat-label">Total</span>
            </div>
            <div className="stat-card" style={{ backgroundColor: '#ffc107' }}>
              <span className="stat-number">{contarPorEstado('PENDIENTE')}</span>
              <span className="stat-label">Pendientes</span>
            </div>
            <div className="stat-card" style={{ backgroundColor: '#28a745' }}>
              <span className="stat-number">{contarPorEstado('CONFIRMADA')}</span>
              <span className="stat-label">Confirmadas</span>
            </div>
            <div className="stat-card" style={{ backgroundColor: '#dc3545' }}>
              <span className="stat-number">{contarPorEstado('RECHAZADA')}</span>
              <span className="stat-label">Rechazadas</span>
            </div>
          </div>

          {/* Filtros */}
          <div className="filtros-container">
            <label htmlFor="filtroEstado">Filtrar por estado:</label>
            <select 
              id="filtroEstado"
              value={filtroEstado} 
              onChange={(e) => setFiltroEstado(e.target.value)}
              className="filtro-select"
            >
              <option value="TODAS">Todas</option>
              <option value="PENDIENTE">Pendientes</option>
              <option value="CONFIRMADA">Confirmadas</option>
              <option value="RECHAZADA">Rechazadas</option>
              <option value="CANCELADA">Canceladas</option>
              <option value="FINALIZADA">Finalizadas</option>
            </select>
          </div>

          {/* Lista de reservas */}
          <div className="reservas-list">
            {reservasFiltradas.map((reserva) => (
              <div key={reserva.idReserva} className="reserva-item">
                <div className="reserva-header">
                  <div className="reserva-info">
                    <h3>{reserva.servicio?.nombre || 'Servicio'}</h3>
                    <span className="servicio-deporte">{reserva.servicio?.deporte || 'N/A'}</span>
                  </div>
                  <div className="reserva-estado" style={{ backgroundColor: getEstadoColor(reserva.estado) }}>
                    <span className="estado-icon">{getEstadoIcon(reserva.estado)}</span>
                    <span className="estado-text">{reserva.estado}</span>
                  </div>
                </div>

                <div className="reserva-detalles">
                  <div className="detalle-item">
                    <span className="detalle-icon">👤</span>
                    <div>
                      <strong>Cliente</strong>
                      <p>{reserva.cliente?.nombre || 'N/A'}</p>
                      <small>{reserva.cliente?.correo || 'N/A'}</small>
                    </div>
                  </div>
                  
                  <div className="detalle-item">
                    <span className="detalle-icon">📅</span>
                    <div>
                      <strong>Fecha</strong>
                      <p>{formatearFecha(reserva.fechaReserva)}</p>
                    </div>
                  </div>
                  
                  <div className="detalle-item">
                    <span className="detalle-icon">🕐</span>
                    <div>
                      <strong>Hora</strong>
                      <p>{formatearHora(reserva.horaReserva)}</p>
                    </div>
                  </div>

                  <div className="detalle-item">
                    <span className="detalle-icon">💰</span>
                    <div>
                      <strong>Ingresos</strong>
                      <p>${reserva.costoTotal?.toLocaleString('es-CO')}</p>
                    </div>
                  </div>
                </div>

                {reserva.notasCliente && (
                  <div className="reserva-notas">
                    <strong>Notas del cliente:</strong>
                    <p>{reserva.notasCliente}</p>
                  </div>
                )}

                <div className="reserva-acciones">
                  {reserva.estado === 'PENDIENTE' && (
                    <>
                      <button 
                        className="btn-confirmar"
                        onClick={() => confirmarReserva(reserva.idReserva)}
                        disabled={accionLoading === reserva.idReserva}
                      >
                        {accionLoading === reserva.idReserva ? 'Confirmando...' : '✅ Confirmar'}
                      </button>
                      <button 
                        className="btn-rechazar"
                        onClick={() => rechazarReserva(reserva.idReserva)}
                        disabled={accionLoading === reserva.idReserva}
                      >
                        {accionLoading === reserva.idReserva ? 'Rechazando...' : '❌ Rechazar'}
                      </button>
                    </>
                  )}
                  
                  {reserva.estado === 'CONFIRMADA' && (
                    <button className="btn-contactar" disabled>
                      📞 Contactar Cliente
                    </button>
                  )}
                  
                  {reserva.estado === 'FINALIZADA' && (
                    <button className="btn-calificar" disabled>
                      ⭐ Ver Calificación
                    </button>
                  )}
                </div>

                <div className="reserva-fecha-creacion">
                  <small>
                    Reserva creada el {new Date(reserva.fechaCreacion).toLocaleDateString('es-CO')}
                  </small>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <style jsx>{`
        .reservas-container {
          margin-top: 20px;
        }

        .reservas-stats {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
          gap: 15px;
          margin-bottom: 30px;
        }

        .stat-card {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          padding: 15px;
          border-radius: 10px;
          text-align: center;
          box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .stat-number {
          display: block;
          font-size: 1.5rem;
          font-weight: bold;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 0.8rem;
          opacity: 0.9;
        }

        .empty-state {
          text-align: center;
          padding: 60px 20px;
          background: #f8f9fa;
          border-radius: 10px;
          border: 2px dashed #dee2e6;
        }

        .empty-icon {
          font-size: 4rem;
          margin-bottom: 20px;
        }

        .empty-state h3 {
          margin: 0 0 10px 0;
          color: #495057;
        }

        .empty-state p {
          color: #6c757d;
          margin: 0;
        }

        .filtros-container {
          margin-bottom: 20px;
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .filtro-select {
          padding: 8px 12px;
          border: 1px solid #ddd;
          border-radius: 6px;
          font-size: 0.9rem;
        }

        .reservas-list {
          display: grid;
          gap: 20px;
        }

        .reserva-item {
          background: white;
          border-radius: 12px;
          padding: 20px;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
          border-left: 4px solid #007bff;
          transition: transform 0.2s, box-shadow 0.2s;
        }

        .reserva-item:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        }

        .reserva-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 15px;
        }

        .reserva-info h3 {
          margin: 0 0 5px 0;
          color: #333;
          font-size: 1.2rem;
        }

        .servicio-deporte {
          background: #e9ecef;
          color: #495057;
          padding: 4px 8px;
          border-radius: 12px;
          font-size: 0.8rem;
          font-weight: 500;
        }

        .reserva-estado {
          display: flex;
          align-items: center;
          gap: 5px;
          padding: 6px 12px;
          border-radius: 20px;
          color: white;
          font-size: 0.8rem;
          font-weight: 500;
        }

        .estado-icon {
          font-size: 0.9rem;
        }

        .reserva-detalles {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
          gap: 15px;
          margin-bottom: 15px;
        }

        .detalle-item {
          display: flex;
          align-items: flex-start;
          gap: 10px;
        }

        .detalle-icon {
          font-size: 1.2rem;
          margin-top: 2px;
        }

        .detalle-item strong {
          display: block;
          color: #495057;
          font-size: 0.9rem;
          margin-bottom: 2px;
        }

        .detalle-item p {
          margin: 0;
          color: #333;
          font-size: 0.95rem;
        }

        .detalle-item small {
          color: #666;
          font-size: 0.8rem;
        }

        .reserva-notas {
          background: #f8f9fa;
          padding: 10px;
          border-radius: 6px;
          margin-bottom: 15px;
        }

        .reserva-notas strong {
          color: #495057;
          font-size: 0.9rem;
        }

        .reserva-notas p {
          margin: 5px 0 0 0;
          color: #333;
          font-size: 0.9rem;
        }

        .reserva-acciones {
          display: flex;
          gap: 10px;
          margin-bottom: 10px;
        }

        .btn-confirmar {
          background: #28a745;
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 0.9rem;
          transition: background-color 0.2s;
        }

        .btn-confirmar:hover:not(:disabled) {
          background: #218838;
        }

        .btn-rechazar {
          background: #dc3545;
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 0.9rem;
          transition: background-color 0.2s;
        }

        .btn-rechazar:hover:not(:disabled) {
          background: #c82333;
        }

        .btn-contactar, .btn-calificar {
          background: #17a2b8;
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 0.9rem;
          opacity: 0.6;
        }

        button:disabled {
          cursor: not-allowed;
        }

        .reserva-fecha-creacion {
          text-align: right;
          color: #6c757d;
          font-size: 0.8rem;
          border-top: 1px solid #eee;
          padding-top: 10px;
        }

        @media (max-width: 768px) {
          .reserva-header {
            flex-direction: column;
            gap: 10px;
          }

          .reserva-detalles {
            grid-template-columns: 1fr;
          }

          .reserva-acciones {
            flex-direction: column;
          }

          .reservas-stats {
            grid-template-columns: repeat(2, 1fr);
          }
        }
      `}</style>
    </div>
  );
};

export default ProviderReservas;
