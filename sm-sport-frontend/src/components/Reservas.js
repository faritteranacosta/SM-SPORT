import React, { useState, useEffect } from 'react';
import axios from 'axios';

const Reservas = () => {
  const [reservas, setReservas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [accionLoading, setAccionLoading] = useState(null);

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

  const cancelarReserva = async (idReserva) => {
    if (!window.confirm('¿Estás seguro de que deseas cancelar esta reserva?')) {
      return;
    }

    try {
      setAccionLoading(idReserva);
      
      const motivo = prompt('Por favor, indica el motivo de la cancelación:');
      if (!motivo) {
        setAccionLoading(null);
        return;
      }

      await axios.delete(`/api/v1/reservas/${idReserva}`, {
        motivoCancelacion: motivo
      });

      // Actualizar la lista de reservas
      setReservas(prev => 
        prev.map(reserva => 
          reserva.idReserva === idReserva 
            ? { ...reserva, estado: 'CANCELADA' }
            : reserva
        )
      );

      alert('Reserva cancelada exitosamente');
    } catch (err) {
      console.error('Error al cancelar reserva:', err);
      alert(err.response?.data?.message || 'Error al cancelar la reserva');
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

  if (loading) {
    return <div className="auth-container">Cargando reservas...</div>;
  }

  return (
    <div className="auth-container">
      <h2>Mis Reservas</h2>
      {error && <div className="error-message">{error}</div>}
      
      {reservas.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">📅</div>
          <h3>No tienes reservas aún</h3>
          <p>Explora nuestros servicios y reserva tu primera actividad deportiva</p>
        </div>
      ) : (
        <div className="reservas-container">
          <div className="reservas-stats">
            <div className="stat-card">
              <span className="stat-number">{reservas.length}</span>
              <span className="stat-label">Total Reservas</span>
            </div>
            <div className="stat-card">
              <span className="stat-number">
                {reservas.filter(r => r.estado === 'PENDIENTE').length}
              </span>
              <span className="stat-label">Pendientes</span>
            </div>
            <div className="stat-card">
              <span className="stat-number">
                {reservas.filter(r => r.estado === 'CONFIRMADA').length}
              </span>
              <span className="stat-label">Confirmadas</span>
            </div>
          </div>

          <div className="reservas-list">
            {reservas.map((reserva) => (
              <div key={reserva.idReserva} className="reserva-item">
                <div className="reserva-header">
                  <div className="reserva-servicio">
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
                      <strong>Costo</strong>
                      <p>${reserva.costoTotal?.toLocaleString('es-CO')}</p>
                    </div>
                  </div>

                  {reserva.notasCliente && (
                    <div className="detalle-item">
                      <span className="detalle-icon">📝</span>
                      <div>
                        <strong>Notas</strong>
                        <p>{reserva.notasCliente}</p>
                      </div>
                    </div>
                  )}
                </div>

                <div className="reserva-ubicacion">
                  <span className="ubicacion-icon">📍</span>
                  <p>
                    {reserva.servicio?.ubicacion?.direccion || 'N/A'}, 
                    {reserva.servicio?.ubicacion?.ciudad || 'N/A'}
                  </p>
                </div>

                <div className="reserva-acciones">
                  {reserva.estado === 'PENDIENTE' && (
                    <button 
                      className="btn-cancelar"
                      onClick={() => cancelarReserva(reserva.idReserva)}
                      disabled={accionLoading === reserva.idReserva}
                    >
                      {accionLoading === reserva.idReserva ? 'Cancelando...' : 'Cancelar Reserva'}
                    </button>
                  )}
                  
                  {reserva.estado === 'CONFIRMADA' && (
                    <button className="btn-contactar" disabled>
                      Contactar Proveedor
                    </button>
                  )}
                  
                  {reserva.estado === 'FINALIZADA' && (
                    <button className="btn-calificar" disabled>
                      Calificar Servicio
                    </button>
                  )}
                </div>

                <div className="reserva-fecha-creacion">
                  <small>
                    Creada el {new Date(reserva.fechaCreacion).toLocaleDateString('es-CO')}
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
          grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
          gap: 15px;
          margin-bottom: 30px;
        }

        .stat-card {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          padding: 20px;
          border-radius: 10px;
          text-align: center;
          box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .stat-number {
          display: block;
          font-size: 2rem;
          font-weight: bold;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 0.9rem;
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

        .reserva-servicio h3 {
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
          grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
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

        .reserva-ubicacion {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 15px;
          padding: 10px;
          background: #f8f9fa;
          border-radius: 6px;
        }

        .ubicacion-icon {
          font-size: 1rem;
        }

        .reserva-ubicacion p {
          margin: 0;
          color: #666;
          font-size: 0.9rem;
        }

        .reserva-acciones {
          display: flex;
          gap: 10px;
          margin-bottom: 10px;
        }

        .btn-cancelar {
          background: #dc3545;
          color: white;
          border: none;
          padding: 8px 16px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 0.9rem;
          transition: background-color 0.2s;
        }

        .btn-cancelar:hover:not(:disabled) {
          background: #c82333;
        }

        .btn-contactar, .btn-calificar {
          background: #28a745;
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
        }
      `}</style>
    </div>
  );
};

export default Reservas;

