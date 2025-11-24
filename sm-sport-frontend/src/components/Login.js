import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { 
  Mail, 
  Lock, 
  Eye, 
  EyeOff, 
  AlertCircle,
  LogIn,
  User
} from 'lucide-react';

const Login = ({ onLogin }) => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  
  const [formData, setFormData] = useState({
    correo: '',
    contrasena: ''
  });

  const validateForm = () => {
    const newErrors = {};

    if (!formData.correo) {
      newErrors.correo = 'El correo es obligatorio';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.correo)) {
      newErrors.correo = 'Ingresa un correo válido';
    }

    if (!formData.contrasena) {
      newErrors.contrasena = 'La contraseña es obligatoria';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const payload = {
        correo: formData.correo.trim().toLowerCase(),
        contrasena: formData.contrasena
      };

      console.log('Intentando login con:', { correo: payload.correo });

      const response = await axios.post('/api/v1/auth/login', payload);

      console.log('Login exitoso:', response.data);

      // Llamar a la función onLogin del App.js para manejar la autenticación
      if (onLogin) {
        onLogin(response.data);
      }

      // Redirigir al dashboard
      navigate('/dashboard');

    } catch (err) {
      console.error('Error en login:', err);
      console.error('Error completo:', err.response?.data);

      let errorMessage = 'Error al iniciar sesión. Verifica tus credenciales.';

      if (err.response?.data) {
        const errorData = err.response.data;

        if (errorData.message) {
          errorMessage = errorData.message;
        } else if (errorData.error) {
          errorMessage = errorData.error;
        } else if (errorData.validationErrors) {
          setErrors(errorData.validationErrors);
          return;
        }
      } else if (err.message === 'Network Error') {
        errorMessage = 'Error de conexión. Verifica que el servidor esté activo.';
      }

      setErrors({ submit: errorMessage });
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

    // Limpiar error del campo al escribir
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: undefined
      });
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #0E1E40 0%, #1a2f5a 50%, #0E1E40 100%)',
      backgroundAttachment: 'fixed',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      padding: '2rem 1rem'
    }}>
      <div style={{
        width: '100%',
        maxWidth: '450px'
      }}>
        <div style={{
          background: 'rgba(255, 255, 255, 0.1)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          borderRadius: '16px',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)',
          overflow: 'hidden'
        }}>
          {/* Header */}
          <div style={{
            padding: '2rem',
            textAlign: 'center'
          }}>
            <div style={{
              display: 'flex',
              justifyContent: 'center',
              marginBottom: '1rem'
            }}>
              <div style={{
                background: 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                borderRadius: '16px',
                padding: '1rem',
                display: 'inline-flex'
              }}>
                <User style={{ color: 'white' }} size={40} />
              </div>
            </div>
            <h2 style={{
              fontSize: '2rem',
              fontWeight: 'bold',
              color: 'white',
              marginBottom: '0.5rem'
            }}>
              Bienvenido
            </h2>
            <p style={{
              color: 'rgba(255, 255, 255, 0.8)',
              fontSize: '14px'
            }}>
              Inicia sesión en tu cuenta
            </p>
          </div>

          {/* Error General */}
          {errors.submit && (
            <div style={{
              margin: '0 2rem 1rem 2rem',
              background: 'rgba(220, 53, 69, 0.2)',
              border: '1px solid rgba(220, 53, 69, 0.5)',
              borderRadius: '8px',
              padding: '1rem',
              display: 'flex',
              alignItems: 'center',
              gap: '12px'
            }}>
              <AlertCircle style={{ color: '#ff6b6b', flexShrink: 0 }} size={20} />
              <p style={{ color: '#ff6b6b', fontSize: '14px', margin: 0 }}>{errors.submit}</p>
            </div>
          )}

          <div style={{ padding: '0 2rem 2rem 2rem' }}>
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
              {/* Correo */}
              <div>
                <label style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '500',
                  color: 'rgba(255, 255, 255, 0.9)',
                  marginBottom: '0.5rem'
                }}>
                  Correo Electrónico
                </label>
                <div style={{ position: 'relative' }}>
                  <Mail style={{
                    position: 'absolute',
                    left: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: 'rgba(255, 255, 255, 0.5)'
                  }} size={20} />
                  <input
                    type="email"
                    name="correo"
                    value={formData.correo}
                    onChange={handleChange}
                    placeholder="correo@ejemplo.com"
                    autoComplete="email"
                    style={{
                      width: '100%',
                      paddingLeft: '2.5rem',
                      paddingRight: '1rem',
                      paddingTop: '0.75rem',
                      paddingBottom: '0.75rem',
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
                {errors.correo && (
                  <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.correo}</p>
                )}
              </div>

              {/* Contraseña */}
              <div>
                <label style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '500',
                  color: 'rgba(255, 255, 255, 0.9)',
                  marginBottom: '0.5rem'
                }}>
                  Contraseña
                </label>
                <div style={{ position: 'relative' }}>
                  <Lock style={{
                    position: 'absolute',
                    left: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: 'rgba(255, 255, 255, 0.5)'
                  }} size={20} />
                  <input
                    type={showPassword ? "text" : "password"}
                    name="contrasena"
                    value={formData.contrasena}
                    onChange={handleChange}
                    placeholder="Ingresa tu contraseña"
                    autoComplete="current-password"
                    style={{
                      width: '100%',
                      paddingLeft: '2.5rem',
                      paddingRight: '2.5rem',
                      paddingTop: '0.75rem',
                      paddingBottom: '0.75rem',
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
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={{
                      position: 'absolute',
                      right: '12px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'transparent',
                      border: 'none',
                      color: 'rgba(255, 255, 255, 0.5)',
                      cursor: 'pointer',
                      padding: '4px',
                      transition: 'color 0.2s'
                    }}
                    onMouseOver={(e) => {
                      e.target.style.color = 'rgba(255, 255, 255, 0.8)';
                    }}
                    onMouseOut={(e) => {
                      e.target.style.color = 'rgba(255, 255, 255, 0.5)';
                    }}
                  >
                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                  </button>
                </div>
                {errors.contrasena && (
                  <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.contrasena}</p>
                )}
              </div>

              {/* Recordar sesión y Olvidé contraseña */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}>
                <div style={{
                  display: 'flex',
                  alignItems: 'center'
                }}>
                  <input
                    id="recordar"
                    type="checkbox"
                    style={{
                      width: '16px',
                      height: '16px',
                      accentColor: '#FF6B00',
                      cursor: 'pointer'
                    }}
                  />
                  <label htmlFor="recordar" style={{
                    marginLeft: '8px',
                    fontSize: '14px',
                    color: 'rgba(255, 255, 255, 0.8)',
                    cursor: 'pointer'
                  }}>
                    Recordar sesión
                  </label>
                </div>

                <button
                  type="button"
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: '#FF6B00',
                    fontSize: '14px',
                    fontWeight: '500',
                    cursor: 'pointer',
                    transition: 'color 0.2s'
                  }}
                  onMouseOver={(e) => {
                    e.target.style.color = '#ff8533';
                  }}
                  onMouseOut={(e) => {
                    e.target.style.color = '#FF6B00';
                  }}
                >
                  ¿Olvidaste tu contraseña?
                </button>
              </div>

              {/* Botón de Login */}
              <button
                type="submit"
                disabled={loading}
                style={{
                  width: '100%',
                  background: loading 
                    ? 'rgba(255, 255, 255, 0.2)' 
                    : 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                  color: 'white',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '8px',
                  border: 'none',
                  fontWeight: '500',
                  fontSize: '16px',
                  cursor: loading ? 'not-allowed' : 'pointer',
                  transition: 'all 0.3s',
                  boxShadow: loading ? 'none' : '0 4px 6px rgba(255, 107, 0, 0.3)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '8px'
                }}
                onMouseOver={(e) => {
                  if (!loading) {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 6px 12px rgba(255, 107, 0, 0.4)';
                  }
                }}
                onMouseOut={(e) => {
                  if (!loading) {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = '0 4px 6px rgba(255, 107, 0, 0.3)';
                  }
                }}
              >
                {loading ? (
                  <>
                    <div style={{
                      width: '20px',
                      height: '20px',
                      border: '2px solid rgba(255, 255, 255, 0.3)',
                      borderTop: '2px solid white',
                      borderRadius: '50%',
                      animation: 'spin 1s linear infinite'
                    }}></div>
                    <span>Iniciando sesión...</span>
                  </>
                ) : (
                  <>
                    <LogIn size={20} />
                    <span>Iniciar Sesión</span>
                  </>
                )}
              </button>

              {/* Divider */}
              <div style={{ position: 'relative', margin: '1rem 0' }}>
                <div style={{
                  position: 'absolute',
                  inset: 0,
                  display: 'flex',
                  alignItems: 'center'
                }}>
                  <div style={{
                    width: '100%',
                    borderTop: '1px solid rgba(255, 255, 255, 0.2)'
                  }}></div>
                </div>
                <div style={{
                  position: 'relative',
                  display: 'flex',
                  justifyContent: 'center',
                  fontSize: '14px'
                }}>
                  <span style={{
                    padding: '0 8px',
                    background: 'rgba(255, 255, 255, 0.1)',
                    color: 'rgba(255, 255, 255, 0.6)'
                  }}>
                    ¿No tienes cuenta?
                  </span>
                </div>
              </div>

              {/* Link a Registro */}
              <Link
                to="/register"
                style={{
                  width: '100%',
                  border: '2px solid rgba(255, 255, 255, 0.2)',
                  color: 'white',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.1)',
                  fontWeight: '500',
                  fontSize: '16px',
                  transition: 'all 0.2s',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '8px',
                  textDecoration: 'none'
                }}
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.2)';
                  e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                  e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                }}
              >
                <User size={20} />
                <span>Crear Cuenta Nueva</span>
              </Link>
            </form>
          </div>
        </div>

        {/* Footer */}
        <div style={{
          textAlign: 'center',
          marginTop: '2rem'
        }}>
          <p style={{
            fontSize: '14px',
            color: 'rgba(255, 255, 255, 0.6)'
          }}>
            © 2024 SM Sport - Plataforma Deportiva de Santa Marta
          </p>
        </div>

        <style>{`
          @keyframes spin {
            from { transform: rotate(0deg); }
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    </div>
  );
};

export default Login;