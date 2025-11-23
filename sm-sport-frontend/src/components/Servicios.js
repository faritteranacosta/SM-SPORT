import React, { useState, useEffect } from 'react';
import axios from 'axios';
import ReservaModal from './ReservaModal';

const Servicios = () => {
  const [servicios, setServicios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [servicioSeleccionado, setServicioSeleccionado] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);

  useEffect(() => {
    const cargarServicios = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/v1/servicios');
        setServicios(response.data.content || response.data || []);
      } catch (err) {
        setError('Error al cargar los servicios. Por favor, intenta nuevamente.');
        console.error('Error al cargar servicios:', err);
      } finally {
        setLoading(false);
      }
    };

    cargarServicios();
  }, []);

  const handleReservar = (servicio) => {
    setServicioSeleccionado(servicio);
    setModalOpen(true);
  };

  const handleReservaCreada = (reserva) => {
    console.log('Reserva creada:', reserva);
    // Opcional: mostrar notificación o actualizar la lista
  };

  const cerrarModal = () => {
    setModalOpen(false);
    setServicioSeleccionado(null);
  };

  if (loading) {
    return <div className="auth-container">Cargando servicios...</div>;
  }

  return (
    <div className="auth-container">
      <h2>Explorar Servicios</h2>
      {error && <div className="error-message">{error}</div>}
      
      {servicios.length === 0 ? (
        <p>No hay servicios disponibles en este momento.</p>
      ) : (
        <div className="servicios-list">
          {servicios.map((servicio) => (
            <div key={servicio.idServicio} className="servicio-item">
              <div className="servicio-header">
                <h3>{servicio.nombre}</h3>
                <span className="servicio-deporte">{servicio.deporte}</span>
              </div>
              
              <div className="servicio-details">
                <p><strong>Precio:</strong> ${servicio.precio?.toLocaleString()}</p>
                <p><strong>Calificación:</strong> {servicio.calificacionPromedio || 'N/A'} ⭐</p>
                <p><strong>Ubicación:</strong> {servicio.direccion}, {servicio.ciudad}</p>
                {servicio.descripcion && <p className="servicio-descripcion">{servicio.descripcion}</p>}
              </div>

              <div className="servicio-actions">
                <button 
                  className="reserva-btn"
                  onClick={() => handleReservar(servicio)}
                >
                  Reservar Ahora
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <ReservaModal
        servicio={servicioSeleccionado}
        isOpen={modalOpen}
        onClose={cerrarModal}
        onReservaCreada={handleReservaCreada}
      />

      <style jsx>{`
        .servicios-list {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
          gap: 20px;
          margin-top: 20px;
        }

        .servicio-item {
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 20px;
          background: white;
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
          transition: transform 0.2s, box-shadow 0.2s;
        }

        .servicio-item:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        }

        .servicio-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;
        }

        .servicio-header h3 {
          margin: 0;
          color: #333;
          font-size: 18px;
        }

        .servicio-deporte {
          background: #007bff;
          color: white;
          padding: 4px 8px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: 500;
        }

        .servicio-details {
          margin-bottom: 16px;
        }

        .servicio-details p {
          margin: 6px 0;
          font-size: 14px;
          color: #666;
        }

        .servicio-details strong {
          color: #333;
        }

        .servicio-descripcion {
          color: #666 !important;
          font-style: italic;
          margin-top: 8px !important;
        }

        .servicio-actions {
          display: flex;
          justify-content: flex-end;
        }

        .reserva-btn {
          background: #28a745;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
          transition: background-color 0.2s;
        }

        .reserva-btn:hover {
          background: #218838;
        }

        @media (max-width: 768px) {
          .servicios-list {
            grid-template-columns: 1fr;
          }
          
          .servicio-header {
            flex-direction: column;
            gap: 8px;
          }
        }
      `}</style>
    </div>
  );
};

export default Servicios;

