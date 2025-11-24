import React, { useState } from 'react';
import axios from 'axios';
import { CheckCircle, X } from 'lucide-react';

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
  const [mostrarExito, setMostrarExito] = useState(false);
  const [reservaCreada, setReservaCreada] = useState(null);

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

    // Verificar disponibilidad primero si no se ha verificado
    if (disponible === null) {
      setVerificando(true);
      try {
        const response = await axios.post('/api/v1/reservas/verificar-disponibilidad', {
          idServicio: servicio.idServicio,
          fechaReserva: formData.fechaReserva,
          horaReserva: formData.horaReserva
        });
        
        const esDisponible = response.data.success;
        setDisponible(esDisponible);
        
        if (!esDisponible) {
          setError(response.data.message || 'No hay disponibilidad para esta fecha y hora');
          setVerificando(false);
          return;
        }
      } catch (err) {
        setError('Error al verificar disponibilidad. Intenta nuevamente.');
        setVerificando(false);
        return;
      } finally {
        setVerificando(false);
      }
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
      
      // Guardar datos de la reserva creada
      setReservaCreada(response.data);
      
      // Mostrar modal de éxito
      setMostrarExito(true);
      
      // Notificar al componente padre
      onReservaCreada(response.data);
      
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
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      background: 'rgba(0, 0, 0, 0.7)',
      backdropFilter: 'blur(4px)',
      WebkitBackdropFilter: 'blur(4px)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 1000,
      padding: '1rem'
    }}
    onClick={onClose}
    >
      <div style={{
        background: 'linear-gradient(135deg, rgba(14, 30, 64, 0.95) 0%, rgba(26, 47, 90, 0.95) 100%)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderRadius: '20px',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        padding: '2rem',
        maxWidth: '550px',
        width: '100%',
        maxHeight: '90vh',
        overflowY: 'auto',
        boxShadow: '0 20px 60px rgba(0, 0, 0, 0.5)',
        position: 'relative',
        animation: 'slideIn 0.3s ease-out'
      }}
      onClick={(e) => e.stopPropagation()}
      >
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '1.5rem'
        }}>
          <h2 style={{
            margin: 0,
            color: 'white',
            fontSize: '1.75rem',
            fontWeight: 'bold'
          }}>
            Reservar Servicio
          </h2>
          <button
            onClick={onClose}
            style={{
              background: 'rgba(255, 255, 255, 0.1)',
              border: '1px solid rgba(255, 255, 255, 0.2)',
              borderRadius: '8px',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: 'white',
              fontSize: '20px',
              transition: 'all 0.2s'
            }}
            onMouseOver={(e) => {
              e.target.style.background = 'rgba(255, 107, 0, 0.3)';
              e.target.style.borderColor = '#FF6B00';
            }}
            onMouseOut={(e) => {
              e.target.style.background = 'rgba(255, 255, 255, 0.1)';
              e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
            }}
          >
            ×
          </button>
        </div>

        <div style={{
          background: 'rgba(255, 255, 255, 0.05)',
          padding: '1.25rem',
          borderRadius: '12px',
          marginBottom: '1.5rem',
          border: '1px solid rgba(255, 255, 255, 0.1)'
        }}>
          <h3 style={{
            margin: '0 0 0.75rem 0',
            color: 'white',
            fontSize: '1.25rem',
            fontWeight: '600'
          }}>
            {servicio.nombre}
          </h3>
          <div style={{
            display: 'grid',
            gap: '0.5rem',
            fontSize: '14px'
          }}>
            <p style={{ margin: 0, color: 'rgba(255, 255, 255, 0.8)' }}>
              <strong style={{ color: 'rgba(255, 255, 255, 0.9)' }}>Deporte:</strong> {servicio.deporte}
            </p>
            <p style={{ margin: 0, color: 'rgba(255, 255, 255, 0.8)' }}>
              <strong style={{ color: 'rgba(255, 255, 255, 0.9)' }}>Precio:</strong> ${servicio.precio?.toLocaleString('es-CO')}
            </p>
            <p style={{ margin: 0, color: 'rgba(255, 255, 255, 0.8)' }}>
              <strong style={{ color: 'rgba(255, 255, 255, 0.9)' }}>Ubicación:</strong> {servicio.direccion}, {servicio.ciudad}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit} style={{
          display: 'flex',
          flexDirection: 'column',
          gap: '1.25rem'
        }}>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <label htmlFor="fechaReserva" style={{
              marginBottom: '0.5rem',
              fontWeight: '500',
              color: 'white',
              fontSize: '14px'
            }}>
              Fecha de Reserva *
            </label>
            <input
              type="date"
              id="fechaReserva"
              name="fechaReserva"
              value={formData.fechaReserva}
              onChange={handleChange}
              min={minDate}
              required
              style={{
                padding: '0.75rem 1rem',
                background: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                outline: 'none',
                transition: 'all 0.2s'
              }}
              onFocus={(e) => {
                e.target.style.border = '1px solid #FF6B00';
                e.target.style.boxShadow = '0 0 0 3px rgba(255, 107, 0, 0.1)';
              }}
              onBlur={(e) => {
                e.target.style.border = '1px solid rgba(255, 255, 255, 0.2)';
                e.target.style.boxShadow = 'none';
              }}
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <label htmlFor="horaReserva" style={{
              marginBottom: '0.5rem',
              fontWeight: '500',
              color: 'white',
              fontSize: '14px'
            }}>
              Hora de Reserva *
            </label>
            <input
              type="time"
              id="horaReserva"
              name="horaReserva"
              value={formData.horaReserva}
              onChange={handleChange}
              min="08:00"
              max="18:00"
              required
              style={{
                padding: '0.75rem 1rem',
                background: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                outline: 'none',
                transition: 'all 0.2s'
              }}
              onFocus={(e) => {
                e.target.style.border = '1px solid #FF6B00';
                e.target.style.boxShadow = '0 0 0 3px rgba(255, 107, 0, 0.1)';
              }}
              onBlur={(e) => {
                e.target.style.border = '1px solid rgba(255, 255, 255, 0.2)';
                e.target.style.boxShadow = 'none';
              }}
            />
            <small style={{
              marginTop: '0.5rem',
              color: 'rgba(255, 255, 255, 0.6)',
              fontSize: '12px'
            }}>
              Horario disponible: 8:00 AM - 6:00 PM
            </small>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <label htmlFor="notasCliente" style={{
              marginBottom: '0.5rem',
              fontWeight: '500',
              color: 'white',
              fontSize: '14px'
            }}>
              Notas (Opcional)
            </label>
            <textarea
              id="notasCliente"
              name="notasCliente"
              value={formData.notasCliente}
              onChange={handleChange}
              placeholder="Algún comentario o requisito especial..."
              rows="3"
              maxLength="500"
              style={{
                padding: '0.75rem 1rem',
                background: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                outline: 'none',
                resize: 'vertical',
                fontFamily: 'inherit',
                transition: 'all 0.2s'
              }}
              onFocus={(e) => {
                e.target.style.border = '1px solid #FF6B00';
                e.target.style.boxShadow = '0 0 0 3px rgba(255, 107, 0, 0.1)';
              }}
              onBlur={(e) => {
                e.target.style.border = '1px solid rgba(255, 255, 255, 0.2)';
                e.target.style.boxShadow = 'none';
              }}
            />
          </div>

          {error && (
            <div style={{
              background: 'rgba(220, 53, 69, 0.2)',
              color: '#ff6b6b',
              padding: '0.75rem 1rem',
              borderRadius: '8px',
              fontSize: '14px',
              border: '1px solid rgba(220, 53, 69, 0.3)'
            }}>
              {error}
            </div>
          )}

          {disponible === true && (
            <div style={{
              background: 'rgba(40, 167, 69, 0.2)',
              color: '#51cf66',
              padding: '0.75rem 1rem',
              borderRadius: '8px',
              fontSize: '14px',
              border: '1px solid rgba(40, 167, 69, 0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}>
              <CheckCircle size={18} />
              ¡Fecha y hora disponibles!
            </div>
          )}

          {disponible === false && (
            <div style={{
              background: 'rgba(255, 193, 7, 0.2)',
              color: '#ffd43b',
              padding: '0.75rem 1rem',
              borderRadius: '8px',
              fontSize: '14px',
              border: '1px solid rgba(255, 193, 7, 0.3)',
              display: 'flex',
              alignItems: 'center',
              gap: '8px'
            }}>
              <X size={18} />
              No hay disponibilidad para esta fecha y hora
            </div>
          )}

          <div style={{
            display: 'flex',
            gap: '0.75rem',
            marginTop: '0.5rem'
          }}>
            <button
              type="button"
              onClick={verificarDisponibilidad}
              disabled={verificando || !formData.fechaReserva || !formData.horaReserva}
              style={{
                flex: 1,
                padding: '0.75rem 1.5rem',
                background: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                fontWeight: '500',
                cursor: verificando || !formData.fechaReserva || !formData.horaReserva ? 'not-allowed' : 'pointer',
                opacity: verificando || !formData.fechaReserva || !formData.horaReserva ? 0.6 : 1,
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => {
                if (!verificando && formData.fechaReserva && formData.horaReserva) {
                  e.target.style.background = 'rgba(255, 255, 255, 0.2)';
                  e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                }
              }}
              onMouseOut={(e) => {
                e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
              }}
            >
              {verificando ? 'Verificando...' : 'Verificar Disponibilidad'}
            </button>

            <button
              type="submit"
              disabled={loading || verificando || disponible === false || !formData.fechaReserva || !formData.horaReserva}
              style={{
                flex: 1,
                padding: '0.75rem 1.5rem',
                background: loading || verificando || disponible === false || !formData.fechaReserva || !formData.horaReserva
                  ? 'rgba(255, 107, 0, 0.3)'
                  : 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                border: 'none',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                fontWeight: '500',
                cursor: loading || verificando || disponible === false || !formData.fechaReserva || !formData.horaReserva ? 'not-allowed' : 'pointer',
                opacity: loading || verificando || disponible === false || !formData.fechaReserva || !formData.horaReserva ? 0.6 : 1,
                transition: 'all 0.2s',
                boxShadow: loading || verificando || disponible === false || !formData.fechaReserva || !formData.horaReserva
                  ? 'none'
                  : '0 4px 12px rgba(255, 107, 0, 0.3)'
              }}
              onMouseOver={(e) => {
                if (!loading && !verificando && disponible !== false && formData.fechaReserva && formData.horaReserva) {
                  e.target.style.transform = 'translateY(-2px)';
                  e.target.style.boxShadow = '0 6px 16px rgba(255, 107, 0, 0.4)';
                }
              }}
              onMouseOut={(e) => {
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = loading || verificando || disponible === false || !formData.fechaReserva || !formData.horaReserva
                  ? 'none'
                  : '0 4px 12px rgba(255, 107, 0, 0.3)';
              }}
            >
              {loading ? 'Creando Reserva...' : 'Confirmar Reserva'}
            </button>
          </div>
        </form>

        <div style={{
          marginTop: '1.5rem',
          paddingTop: '1.5rem',
          borderTop: '1px solid rgba(255, 255, 255, 0.1)',
          textAlign: 'center'
        }}>
          <small style={{
            color: 'rgba(255, 255, 255, 0.6)',
            fontSize: '12px'
          }}>
            La reserva se creará en estado PENDIENTE. El proveedor deberá confirmarla.
          </small>
        </div>
      </div>
      
      {/* Modal de Éxito */}
      {mostrarExito && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.7)',
          backdropFilter: 'blur(4px)',
          WebkitBackdropFilter: 'blur(4px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 2000,
          padding: '1rem'
        }}
        onClick={() => {
          setMostrarExito(false);
          onClose();
        }}
        >
          <div style={{
            background: 'linear-gradient(135deg, rgba(14, 30, 64, 0.95) 0%, rgba(26, 47, 90, 0.95) 100%)',
            backdropFilter: 'blur(20px)',
            WebkitBackdropFilter: 'blur(20px)',
            borderRadius: '20px',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            padding: '2.5rem',
            maxWidth: '450px',
            width: '100%',
            boxShadow: '0 20px 60px rgba(0, 0, 0, 0.5)',
            position: 'relative',
            animation: 'slideIn 0.3s ease-out',
            textAlign: 'center'
          }}
          onClick={(e) => e.stopPropagation()}
          >
            {/* Icono de éxito */}
            <div style={{
              display: 'flex',
              justifyContent: 'center',
              marginBottom: '1.5rem'
            }}>
              <div style={{
                width: '100px',
                height: '100px',
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #28a745 0%, #20c997 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 8px 20px rgba(40, 167, 69, 0.4)',
                animation: 'scaleIn 0.5s ease-out'
              }}>
                <CheckCircle size={50} color="white" />
              </div>
            </div>
            
            {/* Título */}
            <h2 style={{
              color: 'white',
              fontSize: '1.75rem',
              fontWeight: 'bold',
              marginBottom: '0.5rem'
            }}>
              ¡Reserva Creada Exitosamente!
            </h2>
            
            {/* Mensaje */}
            <p style={{
              color: 'rgba(255, 255, 255, 0.8)',
              fontSize: '15px',
              marginBottom: '1.5rem',
              lineHeight: '1.6'
            }}>
              Tu reserva ha sido creada y está pendiente de confirmación por parte del proveedor.
            </p>
            
            {/* Información de la reserva */}
            {reservaCreada && (
              <div style={{
                background: 'rgba(255, 255, 255, 0.05)',
                borderRadius: '12px',
                padding: '1rem',
                marginBottom: '1.5rem',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                textAlign: 'left'
              }}>
                <div style={{
                  display: 'grid',
                  gap: '0.75rem',
                  fontSize: '14px'
                }}>
                  <div>
                    <span style={{ color: 'rgba(255, 255, 255, 0.6)' }}>Estado:</span>
                    <span style={{ 
                      color: '#ffc107', 
                      marginLeft: '8px',
                      fontWeight: '600',
                      background: 'rgba(255, 193, 7, 0.1)',
                      padding: '4px 8px',
                      borderRadius: '4px'
                    }}>
                      PENDIENTE
                    </span>
                  </div>
                  {reservaCreada.fechaReserva && (
                    <div>
                      <span style={{ color: 'rgba(255, 255, 255, 0.6)' }}>Fecha:</span>
                      <p style={{ color: 'white', margin: '4px 0 0 0', fontWeight: '500' }}>
                        {new Date(reservaCreada.fechaReserva).toLocaleDateString('es-CO', {
                          weekday: 'long',
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric'
                        })}
                      </p>
                    </div>
                  )}
                  {reservaCreada.horaReserva && (
                    <div>
                      <span style={{ color: 'rgba(255, 255, 255, 0.6)' }}>Hora:</span>
                      <p style={{ color: 'white', margin: '4px 0 0 0', fontWeight: '500' }}>
                        {typeof reservaCreada.horaReserva === 'object' 
                          ? `${String(reservaCreada.horaReserva.hour || 0).padStart(2, '0')}:${String(reservaCreada.horaReserva.minute || 0).padStart(2, '0')}`
                          : reservaCreada.horaReserva}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            )}
            
            {/* Botón de cerrar */}
            <button
              onClick={() => {
                setMostrarExito(false);
                onClose();
              }}
              style={{
                width: '100%',
                padding: '0.75rem 1.5rem',
                background: 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                border: 'none',
                borderRadius: '8px',
                color: 'white',
                fontSize: '16px',
                fontWeight: '500',
                cursor: 'pointer',
                transition: 'all 0.2s',
                boxShadow: '0 4px 12px rgba(255, 107, 0, 0.3)'
              }}
              onMouseOver={(e) => {
                e.target.style.transform = 'translateY(-2px)';
                e.target.style.boxShadow = '0 6px 16px rgba(255, 107, 0, 0.4)';
              }}
              onMouseOut={(e) => {
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = '0 4px 12px rgba(255, 107, 0, 0.3)';
              }}
            >
              Entendido
            </button>
          </div>
        </div>
      )}
      
      <style>{`
        @keyframes slideIn {
          from {
            opacity: 0;
            transform: scale(0.9) translateY(-20px);
          }
          to {
            opacity: 1;
            transform: scale(1) translateY(0);
          }
        }
        
        @keyframes scaleIn {
          from {
            transform: scale(0);
          }
          to {
            transform: scale(1);
          }
        }
      `}</style>
    </div>
  );
};

export default ReservaModal;
