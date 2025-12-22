import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { User, Mail, Phone, MapPin, Edit2, Save, X, ArrowLeft, CheckCircle, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const Perfil = () => {
  const navigate = useNavigate();
  const [userData, setUserData] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [formData, setFormData] = useState({
    nombre: '',
    telefono: '',
    direccion: '',
    preferenciaDeportes: '',
    nivelExperiencia: '',
    descripcionNegocio: ''
  });

  useEffect(() => {
    cargarPerfil();
  }, []);

  const cargarPerfil = async () => {
    try {
      setLoading(true);
      setError('');
      
      console.log('DEBUG: Iniciando carga de perfil...');
      
      // Verificar token
      const token = localStorage.getItem('token');
      if (!token) {
        setError('No estás autenticado. Por favor, inicia sesión nuevamente.');
        setLoading(false);
        return;
      }
      
      console.log('DEBUG: Token encontrado, haciendo petición...');
      
      const response = await axios.get('/api/v1/usuarios/perfil');
      
      console.log('DEBUG: Respuesta del servidor:', response.data);
      
      if (!response.data) {
        setError('No se recibieron datos del servidor.');
        setLoading(false);
        return;
      }
      
      setUserData(response.data);
      
      // Inicializar formulario con datos actuales
      setFormData({
        nombre: response.data.nombre || '',
        telefono: response.data.telefono || '',
        direccion: response.data.direccion || '',
        preferenciaDeportes: response.data.rol === 'CLIENTE' ? (response.data.preferenciaDeportes || '') : '',
        nivelExperiencia: response.data.rol === 'CLIENTE' ? (response.data.nivelExperiencia || '') : '',
        descripcionNegocio: response.data.rol === 'PROVEEDOR' ? (response.data.descripcionNegocio || '') : ''
      });
      
      console.log('DEBUG: Perfil cargado exitosamente');
    } catch (err) {
      console.error('ERROR: Detalles del error al cargar perfil:', err);
      console.error('ERROR: Response status:', err.response?.status);
      console.error('ERROR: Response data:', err.response?.data);
      console.error('ERROR: Response headers:', err.response?.headers);
      
      if (err.response?.status === 401) {
        setError('No estás autenticado. Por favor, cierra sesión e inicia nuevamente.');
      } else if (err.response?.status === 404) {
        setError('Usuario no encontrado. Por favor, cierra sesión e inicia nuevamente.');
      } else if (err.response?.status === 403) {
        setError('No tienes permisos para acceder a este perfil.');
      } else if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.message) {
        setError(`Error al cargar el perfil: ${err.message}`);
      } else {
        setError('Error al cargar el perfil. Por favor, verifica tu conexión e intenta nuevamente.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccessMessage('');

    try {
      const response = await axios.put('/api/v1/usuarios/perfil', formData);
      
      setUserData(response.data);
      setEditMode(false);
      setSuccessMessage('¡Perfil actualizado exitosamente!');
      
      // Actualizar localStorage
      const updatedUser = {
        ...JSON.parse(localStorage.getItem('user') || '{}'),
        nombre: response.data.nombre,
        telefono: response.data.telefono,
        direccion: response.data.direccion,
        preferenciaDeportes: response.data.preferenciaDeportes,
        nivelExperiencia: response.data.nivelExperiencia,
        descripcionNegocio: response.data.descripcionNegocio
      };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      
      // Ocultar mensaje de éxito después de 3 segundos
      setTimeout(() => setSuccessMessage(''), 3000);
    } catch (err) {
      console.error('Error al actualizar perfil:', err);
      if (err.response?.status === 404) {
        setError('Usuario no encontrado. Por favor, cierra sesión e inicia nuevamente.');
      } else if (err.response?.status === 401) {
        setError('No autorizado. Por favor, inicia sesión nuevamente.');
      } else if (err.response?.status === 400) {
        setError(err.response?.data?.message || 'Datos inválidos. Por favor, verifica los campos.');
      } else {
        setError('Error al actualizar el perfil. Por favor, verifica los datos.');
      }
    } finally {
      setSaving(false);
    }
  };

  const cancelEdit = () => {
    setEditMode(false);
    // Restaurar datos originales
    if (userData) {
      setFormData({
        nombre: userData.nombre || '',
        telefono: userData.telefono || '',
        direccion: userData.direccion || '',
        preferenciaDeportes: userData.rol === 'CLIENTE' ? (userData.preferenciaDeportes || '') : '',
        nivelExperiencia: userData.rol === 'CLIENTE' ? (userData.nivelExperiencia || '') : '',
        descripcionNegocio: userData.rol === 'PROVEEDOR' ? (userData.descripcionNegocio || '') : ''
      });
    }
  };

  if (loading) {
    return (
      <div style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #0E1E40 0%, #1a2f5a 50%, #0E1E40 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <div style={{ textAlign: 'center', color: 'white' }}>
          <div style={{
            width: '48px',
            height: '48px',
            border: '4px solid rgba(255, 255, 255, 0.2)',
            borderTop: '4px solid #FF6B00',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
            margin: '0 auto 1rem'
          }}></div>
          <p>Cargando perfil...</p>
        </div>
      </div>
    );
  }

  // Si hay error y no hay datos, mostrar solo el error
  if (error && !userData) {
    return (
      <div style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #0E1E40 0%, #1a2f5a 50%, #0E1E40 100%)',
        backgroundAttachment: 'fixed',
        padding: '2rem 1rem',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <div style={{
          background: 'rgba(255, 255, 255, 0.1)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          borderRadius: '20px',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          padding: '2rem',
          maxWidth: '500px',
          width: '100%',
          textAlign: 'center'
        }}>
          <AlertCircle size={48} color="#dc3545" style={{ marginBottom: '1rem' }} />
          <h2 style={{ color: 'white', marginBottom: '1rem' }}>Error al Cargar Perfil</h2>
          <p style={{ color: 'rgba(255, 255, 255, 0.8)', marginBottom: '1.5rem' }}>{error}</p>
          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <button
              onClick={cargarPerfil}
              style={{
                padding: '0.75rem 1.5rem',
                background: 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                border: 'none',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                fontWeight: '500',
                cursor: 'pointer',
                transition: 'all 0.2s'
              }}
            >
              Reintentar
            </button>
            <button
              onClick={() => navigate('/dashboard')}
              style={{
                padding: '0.75rem 1.5rem',
                background: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '8px',
                color: 'white',
                fontSize: '14px',
                fontWeight: '500',
                cursor: 'pointer',
                transition: 'all 0.2s'
              }}
            >
              Volver al Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #0E1E40 0%, #1a2f5a 50%, #0E1E40 100%)',
      backgroundAttachment: 'fixed',
      padding: '2rem 1rem'
    }}>
      <div style={{ maxWidth: '900px', margin: '0 auto' }}>
        <div style={{
          background: 'rgba(255, 255, 255, 0.1)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          borderRadius: '20px',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)',
          overflow: 'hidden'
        }}>
          {/* Header */}
          <div style={{
            padding: '2rem',
            background: 'rgba(255, 107, 0, 0.1)',
            borderBottom: '1px solid rgba(255, 255, 255, 0.2)'
          }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <button
                  onClick={() => navigate('/dashboard')}
                  style={{
                    padding: '0.5rem',
                    background: 'rgba(255, 255, 255, 0.1)',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    borderRadius: '8px',
                    color: 'white',
                    cursor: 'pointer',
                    transition: 'all 0.2s',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}
                  onMouseOver={(e) => {
                    e.target.style.background = 'rgba(255, 107, 0, 0.2)';
                  }}
                  onMouseOut={(e) => {
                    e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                  }}
                >
                  <ArrowLeft size={20} />
                </button>
                <div>
                  <h1 style={{
                    color: 'white',
                    fontSize: '2rem',
                    fontWeight: 'bold',
                    margin: 0
                  }}>
                    Mi Perfil
                  </h1>
                  <p style={{
                    color: 'rgba(255, 255, 255, 0.8)',
                    margin: '0.25rem 0 0 0'
                  }}>
                    Gestiona tu información personal
                  </p>
                </div>
              </div>
              {!editMode && (
                <button
                  onClick={() => setEditMode(true)}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    background: 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                    color: 'white',
                    padding: '0.75rem 1.5rem',
                    borderRadius: '8px',
                    border: 'none',
                    cursor: 'pointer',
                    fontSize: '14px',
                    fontWeight: '500',
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
                  <Edit2 size={18} />
                  Editar Perfil
                </button>
              )}
            </div>
          </div>

          {/* Success Message */}
          {successMessage && (
            <div style={{
              margin: '1.5rem',
              background: 'rgba(40, 167, 69, 0.2)',
              border: '1px solid rgba(40, 167, 69, 0.5)',
              borderRadius: '12px',
              padding: '1rem',
              display: 'flex',
              alignItems: 'center',
              gap: '12px'
            }}>
              <CheckCircle style={{ color: '#28a745', flexShrink: 0 }} size={20} />
              <p style={{ color: '#28a745', margin: 0, fontWeight: '500' }}>{successMessage}</p>
            </div>
          )}

          {/* Error Message */}
          {error && (
            <div style={{
              margin: '1.5rem',
              background: 'rgba(220, 53, 69, 0.2)',
              border: '1px solid rgba(220, 53, 69, 0.5)',
              borderRadius: '12px',
              padding: '1rem',
              display: 'flex',
              alignItems: 'center',
              gap: '12px'
            }}>
              <AlertCircle style={{ color: '#dc3545', flexShrink: 0 }} size={20} />
              <p style={{ color: '#dc3545', margin: 0 }}>{error}</p>
            </div>
          )}

          {/* Profile Content */}
          <div style={{ padding: '2rem' }}>
            {editMode ? (
              // Edit Mode
              <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                  gap: '1.5rem'
                }}>
                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '14px',
                      fontWeight: '500',
                      color: 'rgba(255, 255, 255, 0.9)',
                      marginBottom: '0.5rem'
                    }}>
                      Nombre Completo *
                    </label>
                    <input
                      type="text"
                      name="nombre"
                      value={formData.nombre}
                      onChange={handleChange}
                      required
                      style={{
                        width: '100%',
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
                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '14px',
                      fontWeight: '500',
                      color: 'rgba(255, 255, 255, 0.9)',
                      marginBottom: '0.5rem'
                    }}>
                      Teléfono
                    </label>
                    <input
                      type="tel"
                      name="telefono"
                      value={formData.telefono}
                      onChange={handleChange}
                      placeholder="Ej: 3001234567"
                      maxLength={10}
                      style={{
                        width: '100%',
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
                  <div style={{ gridColumn: '1 / -1' }}>
                    <label style={{
                      display: 'block',
                      fontSize: '14px',
                      fontWeight: '500',
                      color: 'rgba(255, 255, 255, 0.9)',
                      marginBottom: '0.5rem'
                    }}>
                      Dirección
                    </label>
                    <input
                      type="text"
                      name="direccion"
                      value={formData.direccion}
                      onChange={handleChange}
                      placeholder="Ej: Calle 123 #45-67, Santa Marta"
                      maxLength={200}
                      style={{
                        width: '100%',
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

                  {/* Campos específicos para CLIENTE */}
                  {userData?.rol === 'CLIENTE' && (
                    <>
                      <div>
                        <label style={{
                          display: 'block',
                          fontSize: '14px',
                          fontWeight: '500',
                          color: 'rgba(255, 255, 255, 0.9)',
                          marginBottom: '0.5rem'
                        }}>
                          Preferencia de Deportes
                        </label>
                        <input
                          type="text"
                          name="preferenciaDeportes"
                          value={formData.preferenciaDeportes}
                          onChange={handleChange}
                          placeholder="Ej: Fútbol, Tenis, Natación"
                          style={{
                            width: '100%',
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
                      <div>
                        <label style={{
                          display: 'block',
                          fontSize: '14px',
                          fontWeight: '500',
                          color: 'rgba(255, 255, 255, 0.9)',
                          marginBottom: '0.5rem'
                        }}>
                          Nivel de Experiencia
                        </label>
                        <select
                          name="nivelExperiencia"
                          value={formData.nivelExperiencia}
                          onChange={handleChange}
                          style={{
                            width: '100%',
                            padding: '0.75rem 1rem',
                            background: 'rgba(255, 255, 255, 0.1)',
                            border: '1px solid rgba(255, 255, 255, 0.2)',
                            borderRadius: '8px',
                            color: 'white',
                            fontSize: '14px',
                            outline: 'none',
                            cursor: 'pointer',
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
                        >
                          <option value="" style={{ background: '#1a2f5a', color: 'white' }}>Selecciona tu nivel</option>
                          <option value="PRINCIPIANTE" style={{ background: '#1a2f5a', color: 'white' }}>Principiante</option>
                          <option value="INTERMEDIO" style={{ background: '#1a2f5a', color: 'white' }}>Intermedio</option>
                          <option value="AVANZADO" style={{ background: '#1a2f5a', color: 'white' }}>Avanzado</option>
                          <option value="PROFESIONAL" style={{ background: '#1a2f5a', color: 'white' }}>Profesional</option>
                        </select>
                      </div>
                    </>
                  )}

                  {/* Campos específicos para PROVEEDOR */}
                  {userData?.rol === 'PROVEEDOR' && (
                    <div style={{ gridColumn: '1 / -1' }}>
                      <label style={{
                        display: 'block',
                        fontSize: '14px',
                        fontWeight: '500',
                        color: 'rgba(255, 255, 255, 0.9)',
                        marginBottom: '0.5rem'
                      }}>
                        Descripción del Negocio
                      </label>
                      <textarea
                        name="descripcionNegocio"
                        value={formData.descripcionNegocio}
                        onChange={handleChange}
                        rows={4}
                        placeholder="Describe tu negocio, experiencia, especialidades..."
                        style={{
                          width: '100%',
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
                  )}
                </div>

                {/* Form Actions */}
                <div style={{
                  display: 'flex',
                  gap: '1rem',
                  paddingTop: '1.5rem',
                  borderTop: '1px solid rgba(255, 255, 255, 0.1)'
                }}>
                  <button
                    type="submit"
                    disabled={saving}
                    style={{
                      flex: 1,
                      padding: '0.75rem 1.5rem',
                      background: saving 
                        ? 'rgba(255, 255, 255, 0.2)' 
                        : 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                      border: 'none',
                      borderRadius: '8px',
                      color: 'white',
                      fontSize: '16px',
                      fontWeight: '500',
                      cursor: saving ? 'not-allowed' : 'pointer',
                      transition: 'all 0.2s',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px',
                      boxShadow: saving ? 'none' : '0 4px 12px rgba(255, 107, 0, 0.3)'
                    }}
                    onMouseOver={(e) => {
                      if (!saving) {
                        e.target.style.transform = 'translateY(-2px)';
                        e.target.style.boxShadow = '0 6px 16px rgba(255, 107, 0, 0.4)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (!saving) {
                        e.target.style.transform = 'translateY(0)';
                        e.target.style.boxShadow = '0 4px 12px rgba(255, 107, 0, 0.3)';
                      }
                    }}
                  >
                    {saving ? (
                      <>
                        <div style={{
                          width: '16px',
                          height: '16px',
                          border: '2px solid rgba(255, 255, 255, 0.3)',
                          borderTop: '2px solid white',
                          borderRadius: '50%',
                          animation: 'spin 1s linear infinite'
                        }}></div>
                        Guardando...
                      </>
                    ) : (
                      <>
                        <Save size={18} />
                        Guardar Cambios
                      </>
                    )}
                  </button>
                  
                  <button
                    type="button"
                    onClick={cancelEdit}
                    style={{
                      padding: '0.75rem 1.5rem',
                      background: 'rgba(255, 255, 255, 0.1)',
                      border: '1px solid rgba(255, 255, 255, 0.2)',
                      borderRadius: '8px',
                      color: 'white',
                      cursor: 'pointer',
                      fontSize: '16px',
                      fontWeight: '500',
                      transition: 'all 0.2s',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}
                    onMouseOver={(e) => {
                      e.target.style.background = 'rgba(255, 255, 255, 0.2)';
                    }}
                    onMouseOut={(e) => {
                      e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                    }}
                  >
                    <X size={18} />
                    Cancelar
                  </button>
                </div>
              </form>
            ) : (
              // View Mode
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                {/* Basic Info */}
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
                  gap: '1.5rem'
                }}>
                  <div style={{
                    background: 'rgba(255, 255, 255, 0.05)',
                    padding: '1.5rem',
                    borderRadius: '12px',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    display: 'flex',
                    alignItems: 'start',
                    gap: '1rem'
                  }}>
                    <div style={{
                      padding: '0.75rem',
                      background: 'rgba(255, 107, 0, 0.2)',
                      borderRadius: '12px'
                    }}>
                      <User size={24} color="#FF6B00" />
                    </div>
                    <div>
                      <h3 style={{
                        color: 'rgba(255, 255, 255, 0.6)',
                        fontSize: '14px',
                        fontWeight: '500',
                        margin: '0 0 0.5rem 0'
                      }}>
                        Nombre Completo
                      </h3>
                      <p style={{
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: '600',
                        margin: 0
                      }}>
                        {userData?.nombre || 'No especificado'}
                      </p>
                    </div>
                  </div>

                  <div style={{
                    background: 'rgba(255, 255, 255, 0.05)',
                    padding: '1.5rem',
                    borderRadius: '12px',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    display: 'flex',
                    alignItems: 'start',
                    gap: '1rem'
                  }}>
                    <div style={{
                      padding: '0.75rem',
                      background: 'rgba(255, 107, 0, 0.2)',
                      borderRadius: '12px'
                    }}>
                      <Mail size={24} color="#FF6B00" />
                    </div>
                    <div>
                      <h3 style={{
                        color: 'rgba(255, 255, 255, 0.6)',
                        fontSize: '14px',
                        fontWeight: '500',
                        margin: '0 0 0.5rem 0'
                      }}>
                        Correo Electrónico
                      </h3>
                      <p style={{
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: '600',
                        margin: 0
                      }}>
                        {userData?.correo || 'No especificado'}
                      </p>
                      <p style={{
                        color: 'rgba(255, 255, 255, 0.5)',
                        fontSize: '12px',
                        margin: '0.25rem 0 0 0'
                      }}>
                        No editable
                      </p>
                    </div>
                  </div>

                  <div style={{
                    background: 'rgba(255, 255, 255, 0.05)',
                    padding: '1.5rem',
                    borderRadius: '12px',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    display: 'flex',
                    alignItems: 'start',
                    gap: '1rem'
                  }}>
                    <div style={{
                      padding: '0.75rem',
                      background: 'rgba(255, 107, 0, 0.2)',
                      borderRadius: '12px'
                    }}>
                      <Phone size={24} color="#FF6B00" />
                    </div>
                    <div>
                      <h3 style={{
                        color: 'rgba(255, 255, 255, 0.6)',
                        fontSize: '14px',
                        fontWeight: '500',
                        margin: '0 0 0.5rem 0'
                      }}>
                        Teléfono
                      </h3>
                      <p style={{
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: '600',
                        margin: 0
                      }}>
                        {userData?.telefono || 'No especificado'}
                      </p>
                    </div>
                  </div>

                  <div style={{
                    background: 'rgba(255, 255, 255, 0.05)',
                    padding: '1.5rem',
                    borderRadius: '12px',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    display: 'flex',
                    alignItems: 'start',
                    gap: '1rem'
                  }}>
                    <div style={{
                      padding: '0.75rem',
                      background: 'rgba(255, 107, 0, 0.2)',
                      borderRadius: '12px'
                    }}>
                      <MapPin size={24} color="#FF6B00" />
                    </div>
                    <div>
                      <h3 style={{
                        color: 'rgba(255, 255, 255, 0.6)',
                        fontSize: '14px',
                        fontWeight: '500',
                        margin: '0 0 0.5rem 0'
                      }}>
                        Dirección
                      </h3>
                      <p style={{
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: '600',
                        margin: 0
                      }}>
                        {userData?.direccion || 'No especificado'}
                      </p>
                    </div>
                  </div>
                </div>

                {/* Bio */}
                {(userData?.descripcionNegocio || userData?.preferenciaDeportes) && (
                  <div style={{
                    background: 'rgba(255, 107, 0, 0.1)',
                    borderRadius: '12px',
                    padding: '1.5rem',
                    border: '1px solid rgba(255, 107, 0, 0.3)'
                  }}>
                    <h3 style={{
                      color: 'white',
                      fontSize: '18px',
                      fontWeight: '600',
                      margin: '0 0 1rem 0'
                    }}>
                      {userData?.rol === 'PROVEEDOR' ? 'Descripción del Negocio' : 'Sobre Mí'}
                    </h3>
                    <p style={{
                      color: 'rgba(255, 255, 255, 0.8)',
                      lineHeight: '1.6',
                      margin: 0
                    }}>
                      {userData?.rol === 'PROVEEDOR' 
                        ? (userData.descripcionNegocio || 'No especificado')
                        : userData?.preferenciaDeportes 
                          ? `Deportes preferidos: ${userData.preferenciaDeportes}${userData.nivelExperiencia ? ` • Nivel: ${userData.nivelExperiencia}` : ''}`
                          : 'No especificado'}
                    </p>
                  </div>
                )}

                {/* Role-specific info */}
                <div style={{
                  paddingTop: '1.5rem',
                  borderTop: '1px solid rgba(255, 255, 255, 0.1)'
                }}>
                  <h3 style={{
                    color: 'white',
                    fontSize: '18px',
                    fontWeight: '600',
                    margin: '0 0 1rem 0'
                  }}>
                    Información de Cuenta
                  </h3>
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                    gap: '1rem'
                  }}>
                    <div>
                      <span style={{
                        color: 'rgba(255, 255, 255, 0.6)',
                        fontSize: '14px'
                      }}>
                        Rol
                      </span>
                      <p style={{
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: '600',
                        margin: '0.25rem 0 0 0',
                        textTransform: 'capitalize'
                      }}>
                        {userData?.rol?.toLowerCase()}
                      </p>
                    </div>
                    <div>
                      <span style={{
                        color: 'rgba(255, 255, 255, 0.6)',
                        fontSize: '14px'
                      }}>
                        Estado
                      </span>
                      <p style={{
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: '600',
                        margin: '0.25rem 0 0 0',
                        textTransform: 'capitalize'
                      }}>
                        {userData?.estado?.toLowerCase() || 'Activo'}
                      </p>
                    </div>
                    <div>
                      <span style={{
                        color: 'rgba(255, 255, 255, 0.6)',
                        fontSize: '14px'
                      }}>
                        Miembro desde
                      </span>
                      <p style={{
                        color: 'white',
                        fontSize: '16px',
                        fontWeight: '600',
                        margin: '0.25rem 0 0 0'
                      }}>
                        {userData?.fechaCreacion ? 
                          new Date(userData.fechaCreacion).toLocaleDateString('es-CO') : 
                          'No disponible'}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
      
      <style>{`
        @keyframes spin {
          from {
            transform: rotate(0deg);
          }
          to {
            transform: rotate(360deg);
          }
        }
      `}</style>
    </div>
  );
};

export default Perfil;

