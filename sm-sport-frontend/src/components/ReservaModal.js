import React, { useState } from 'react';
import axios from 'axios';

const ReservaModal = ({ servicio, isOpen, onClose, onReservaCreada }) => {
  const [formData, setFormData] = useState({
    fechaReserva: '',
    horaReserva: '',
    notasCliente: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [verificando, setVerificando] = useState(false);
  const [disponible, setDisponible] = useState(null);

  if (!isOpen) return null;

  const handleChange = (e) => {
    const { name, value } = e.target;
    console.log(`Cambio en ${name}:`, value, typeof value);
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    setError('');
    setDisponible(null);
  };

  const verificarDisponibilidad = async () => {
    if (!formData.fechaReserva || !formData.horaReserva) {
      setError('Por favor selecciona fecha y hora');
      return;
    }

    try {
      setVerificando(true);
      setError('');
      
      // Debug: Mostrar valores que se envían
      console.log('Enviando:', {
        idServicio: servicio.idServicio,
        fechaReserva: formData.fechaReserva,
        horaReserva: formData.horaReserva,
        tipoHora: typeof formData.horaReserva
      });
      
      const response = await axios.post('/api/v1/reservas/verificar-disponibilidad', {
        idServicio: servicio.idServicio,
        fechaReserva: formData.fechaReserva,
        horaReserva: formData.horaReserva
      });

      console.log('Respuesta:', response.data);
      setDisponible(response.data.success);
      if (!response.data.success) {
        setError(response.data.message || 'No hay disponibilidad para esta fecha y hora');
      }
    } catch (err) {
      console.error('Error completo:', err);
      console.error('Response data:', err.response?.data);
      setError('Error al verificar disponibilidad. Intenta nuevamente.');
    } finally {
      setVerificando(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.fechaReserva || !formData.horaReserva) {
      setError('Por favor completa todos los campos obligatorios');
      return;
    }

    // Verificar disponibilidad primero
    if (disponible === null) {
      await verificarDisponibilidad();
      if (disponible === false) return;
    }

    if (!disponible) {
      setError('La fecha y hora seleccionadas no están disponibles');
      return;
    }

    try {
      setLoading(true);
      setError('');

      const reservaData = {
        idServicio: servicio.idServicio,
        fechaReserva: formData.fechaReserva,
        horaReserva: formData.horaReserva,
        notasCliente: formData.notasCliente
      };

      const response = await axios.post('/api/v1/reservas', reservaData);
      
      // Limpiar formulario
      setFormData({
        fechaReserva: '',
        horaReserva: '',
        notasCliente: ''
      });
      setDisponible(null);
      
      // Notificar al componente padre
      onReservaCreada(response.data);
      
      // Cerrar modal
      onClose();
      
      // Mostrar mensaje de éxito
      alert('¡Reserva creada exitosamente! Estado: PENDIENTE');
      
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear la reserva. Intenta nuevamente.');
      console.error('Error creando reserva:', err);
    } finally {
      setLoading(false);
    }
  };

  // Establecer fecha mínima como mañana
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  const minDate = tomorrow.toISOString().split('T')[0];

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>Reservar Servicio</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        <div className="servicio-info">
          <h3>{servicio.nombre}</h3>
          <p><strong>Deporte:</strong> {servicio.deporte}</p>
          <p><strong>Precio:</strong> ${servicio.precio?.toLocaleString()}</p>
          <p><strong>Ubicación:</strong> {servicio.direccion}, {servicio.ciudad}</p>
        </div>

        <form onSubmit={handleSubmit} className="reserva-form">
          <div className="form-group">
            <label htmlFor="fechaReserva">Fecha de Reserva *</label>
            <input
              type="date"
              id="fechaReserva"
              name="fechaReserva"
              value={formData.fechaReserva}
              onChange={handleChange}
              min={minDate}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="horaReserva">Hora de Reserva *</label>
            <input
              type="time"
              id="horaReserva"
              name="horaReserva"
              value={formData.horaReserva}
              onChange={handleChange}
              min="08:00"
              max="18:00"
              required
            />
            <small>Horario disponible: 8:00 AM - 6:00 PM</small>
          </div>

          <div className="form-group">
            <label htmlFor="notasCliente">Notas (Opcional)</label>
            <textarea
              id="notasCliente"
              name="notasCliente"
              value={formData.notasCliente}
              onChange={handleChange}
              placeholder="Algún comentario o requisito especial..."
              rows="3"
              maxLength="500"
            />
          </div>

          {error && (
            <div className="error-message">
              {error}
            </div>
          )}

          {disponible === true && (
            <div className="success-message">
              ✓ ¡Fecha y hora disponibles!
            </div>
          )}

          {disponible === false && (
            <div className="warning-message">
              ✗ No hay disponibilidad para esta fecha y hora
            </div>
          )}

          <div className="form-actions">
            <button
              type="button"
              className="secondary-btn"
              onClick={verificarDisponibilidad}
              disabled={verificando || !formData.fechaReserva || !formData.horaReserva}
            >
              {verificando ? 'Verificando...' : 'Verificar Disponibilidad'}
            </button>

            <button
              type="submit"
              className="primary-btn"
              disabled={loading || disponible === false || !formData.fechaReserva || !formData.horaReserva}
            >
              {loading ? 'Creando Reserva...' : 'Confirmar Reserva'}
            </button>
          </div>
        </form>

        <div className="modal-footer">
          <small>
            La reserva se creará en estado PENDIENTE. El proveedor deberá confirmarla.
          </small>
        </div>
      </div>

      <style jsx>{`
        .modal-overlay {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background-color: rgba(0, 0, 0, 0.5);
          display: flex;
          justify-content: center;
          align-items: center;
          z-index: 1000;
        }

        .modal-content {
          background: white;
          border-radius: 8px;
          padding: 24px;
          max-width: 500px;
          width: 90%;
          max-height: 90vh;
          overflow-y: auto;
        }

        .modal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
        }

        .modal-header h2 {
          margin: 0;
          color: #333;
        }

        .close-btn {
          background: none;
          border: none;
          font-size: 24px;
          cursor: pointer;
          color: #666;
        }

        .servicio-info {
          background: #f8f9fa;
          padding: 16px;
          border-radius: 6px;
          margin-bottom: 20px;
        }

        .servicio-info h3 {
          margin: 0 0 8px 0;
          color: #333;
        }

        .servicio-info p {
          margin: 4px 0;
          font-size: 14px;
          color: #666;
        }

        .reserva-form {
          display: flex;
          flex-direction: column;
          gap: 16px;
        }

        .form-group {
          display: flex;
          flex-direction: column;
        }

        .form-group label {
          margin-bottom: 4px;
          font-weight: 500;
          color: #333;
        }

        .form-group input,
        .form-group textarea {
          padding: 8px 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 14px;
        }

        .form-group textarea {
          resize: vertical;
        }

        .form-actions {
          display: flex;
          gap: 12px;
          margin-top: 8px;
        }

        .primary-btn,
        .secondary-btn {
          padding: 10px 16px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-size: 14px;
          font-weight: 500;
        }

        .primary-btn {
          background: #007bff;
          color: white;
        }

        .primary-btn:hover:not(:disabled) {
          background: #0056b3;
        }

        .secondary-btn {
          background: #6c757d;
          color: white;
        }

        .secondary-btn:hover:not(:disabled) {
          background: #545b62;
        }

        button:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .error-message {
          background: #f8d7da;
          color: #721c24;
          padding: 8px 12px;
          border-radius: 4px;
          font-size: 14px;
        }

        .success-message {
          background: #d4edda;
          color: #155724;
          padding: 8px 12px;
          border-radius: 4px;
          font-size: 14px;
        }

        .warning-message {
          background: #fff3cd;
          color: #856404;
          padding: 8px 12px;
          border-radius: 4px;
          font-size: 14px;
        }

        .modal-footer {
          margin-top: 20px;
          padding-top: 16px;
          border-top: 1px solid #eee;
          text-align: center;
        }

        .modal-footer small {
          color: #666;
          font-size: 12px;
        }
      `}</style>
    </div>
  );
};

export default ReservaModal;
