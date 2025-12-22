import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { 
  User, 
  Mail, 
  Lock, 
  Phone, 
  MapPin, 
  Calendar,
  Briefcase,
  Heart,
  TrendingUp,
  AlertCircle,
  Eye,
  EyeOff
} from 'lucide-react';

const Register = () => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  
  const [formData, setFormData] = useState({
    nombre: '',
    correo: '',
    contrasena: '',
    confirmarContrasena: '',
    telefono: '',
    direccion: '',
    rol: '',
    // Campos Cliente
    preferenciaDeportes: '',
    nivelExperiencia: '',
    fechaNacimiento: '',
    // Campos Proveedor
    descripcionNegocio: ''
  });

  const nivelesExperiencia = ['PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO', 'PROFESIONAL'];
  
  const deportesPopulares = [
    'Fútbol', 'Baloncesto', 'Tenis', 'Voleibol', 'Natación',
    'Boxeo', 'Yoga', 'CrossFit', 'Ciclismo', 'Running'
  ];

  const validatePassword = (password) => {
    const regex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$/;
    return regex.test(password);
  };

  const getPasswordStrength = (password) => {
    if (!password) return { level: 0, label: '', color: '', width: '0%' };
    
    let strength = 0;
    let checks = {
      length: password.length >= 8 && password.length <= 50,
      lowercase: /[a-z]/.test(password),
      uppercase: /[A-Z]/.test(password),
      number: /[0-9]/.test(password),
      special: /[@#$%^&+=!]/.test(password)
    };

    if (checks.length) strength += 1;
    if (checks.lowercase) strength += 1;
    if (checks.uppercase) strength += 1;
    if (checks.number) strength += 1;
    if (checks.special) strength += 1;

    if (strength <= 2) {
      return { level: 1, label: 'Débil', color: '#dc3545', width: '33%' };
    } else if (strength <= 4) {
      return { level: 2, label: 'Media', color: '#ffc107', width: '66%' };
    } else {
      return { level: 3, label: 'Fuerte', color: '#28a745', width: '100%' };
    }
  };

  const validateForm = () => {
    const newErrors = {};

    // Validación de Nombre
    if (!formData.nombre || formData.nombre.trim() === '') {
      newErrors.nombre = 'El nombre es obligatorio';
    } else if (formData.nombre.length < 3) {
      newErrors.nombre = 'El nombre debe tener al menos 3 caracteres';
    } else if (formData.nombre.length > 100) {
      newErrors.nombre = 'El nombre no puede exceder 100 caracteres';
    }

    // Validación de Correo
    if (!formData.correo || formData.correo.trim() === '') {
      newErrors.correo = 'El correo es obligatorio';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.correo)) {
      newErrors.correo = 'Ingresa un correo válido';
    } else if (formData.correo.length > 100) {
      newErrors.correo = 'El correo no puede exceder 100 caracteres';
    }

    // Validación de Contraseña
    if (!formData.contrasena || formData.contrasena.trim() === '') {
      newErrors.contrasena = 'La contraseña es obligatoria';
    } else if (formData.contrasena.length < 8) {
      newErrors.contrasena = 'La contraseña debe tener al menos 8 caracteres';
    } else if (formData.contrasena.length > 50) {
      newErrors.contrasena = 'La contraseña no puede exceder 50 caracteres';
    } else if (!validatePassword(formData.contrasena)) {
      newErrors.contrasena = 'Debe contener mayúscula, minúscula, número y carácter especial (@#$%^&+=!)';
    }

    // Validación de Confirmar Contraseña
    if (!formData.confirmarContrasena || formData.confirmarContrasena.trim() === '') {
      newErrors.confirmarContrasena = 'Debes confirmar la contraseña';
    } else if (formData.contrasena !== formData.confirmarContrasena) {
      newErrors.confirmarContrasena = 'Las contraseñas no coinciden';
    }

    // Validación de Teléfono (opcional pero con formato)
    if (formData.telefono && formData.telefono.trim() !== '') {
      if (!/^[0-9]{10}$/.test(formData.telefono)) {
        newErrors.telefono = 'El teléfono debe tener exactamente 10 dígitos';
      }
    }

    // Validación de Dirección (opcional pero con límite)
    if (formData.direccion && formData.direccion.length > 200) {
      newErrors.direccion = 'La dirección no puede exceder 200 caracteres';
    }

    // Validación de Rol
    if (!formData.rol || formData.rol.trim() === '') {
      newErrors.rol = 'Debes seleccionar un rol';
    } else if (!['CLIENTE', 'PROVEEDOR'].includes(formData.rol)) {
      newErrors.rol = 'Rol inválido';
    }

    // Validación de Fecha de Nacimiento (opcional pero si se envía)
    if (formData.fechaNacimiento && formData.fechaNacimiento.trim() !== '') {
      const fechaNac = new Date(formData.fechaNacimiento);
      const hoy = new Date();
      hoy.setHours(0, 0, 0, 0);
      if (fechaNac >= hoy) {
        newErrors.fechaNacimiento = 'La fecha de nacimiento debe ser en el pasado';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validar antes de enviar
    if (!validateForm()) {
      // Scroll al primer error
      const firstErrorField = Object.keys(errors)[0];
      if (firstErrorField) {
        const errorElement = document.querySelector(`[name="${firstErrorField}"]`);
        if (errorElement) {
          errorElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
          errorElement.focus();
        }
      }
      return;
    }

    setLoading(true);

    try {
      // Preparar payload según el rol
      const payload = {
        nombre: formData.nombre.trim(),
        correo: formData.correo.trim().toLowerCase(),
        contrasena: formData.contrasena,
        telefono: formData.telefono || null,
        direccion: formData.direccion.trim() || null,
        rol: formData.rol
      };

      // Agregar campos específicos según el rol
      if (formData.rol === 'CLIENTE') {
        payload.preferenciaDeportes = formData.preferenciaDeportes || null;
        payload.nivelExperiencia = formData.nivelExperiencia || null;
        payload.fechaNacimiento = formData.fechaNacimiento || null;
      } else if (formData.rol === 'PROVEEDOR') {
        payload.descripcionNegocio = formData.descripcionNegocio.trim() || null;
      }

      console.log('Payload a enviar:', payload);

      const response = await axios.post('/api/v1/auth/registro', payload);

      console.log('Registro exitoso:', response.data);

      // Guardar token y usuario
      if (response.data.token) {
        localStorage.setItem('token', response.data.token);
      }

      const userData = {
        idUsuario: response.data.idUsuario,
        nombre: response.data.nombre,
        correo: response.data.correo,
        rol: response.data.rol
      };

      localStorage.setItem('user', JSON.stringify(userData));

      // Redirigir al dashboard
      navigate('/dashboard');

    } catch (err) {
      console.error('Error en registro:', err);
      console.error('Error completo:', err.response?.data);

      let errorMessage = 'Error al registrar usuario. Intenta nuevamente.';

      if (err.response?.data) {
        const errorData = err.response.data;

        if (errorData.validationErrors) {
          // Mostrar errores de validación del backend
          setErrors(errorData.validationErrors);
          return;
        } else if (errorData.message) {
          errorMessage = errorData.message;
        } else if (errorData.error) {
          errorMessage = errorData.error;
        }
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
      const newErrors = { ...errors };
      delete newErrors[name];
      setErrors(newErrors);
    }
  };

  const passwordStrength = getPasswordStrength(formData.contrasena);

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
        maxWidth: '800px'
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
              Crear Cuenta
            </h2>
            <p style={{
              color: 'rgba(255, 255, 255, 0.8)',
              fontSize: '14px'
            }}>
              Únete a la comunidad deportiva de Santa Marta
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
              {/* Información Básica */}
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
                <div style={{ position: 'relative' }}>
                  <User style={{
                    position: 'absolute',
                    left: '12px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    color: 'rgba(255, 255, 255, 0.5)'
                  }} size={20} />
                  <input
                    type="text"
                    name="nombre"
                    value={formData.nombre}
                    onChange={handleChange}
                    placeholder="Ej: Juan Pérez"
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
                {errors.nombre && (
                  <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.nombre}</p>
                )}
              </div>

              <div>
                <label style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '500',
                  color: 'rgba(255, 255, 255, 0.9)',
                  marginBottom: '0.5rem'
                }}>
                  Correo Electrónico *
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
                    style={{
                      width: '100%',
                      paddingLeft: '2.5rem',
                      paddingRight: '1rem',
                      paddingTop: '0.75rem',
                      paddingBottom: '0.75rem',
                      background: 'rgba(255, 255, 255, 0.1)',
                      border: errors.correo 
                        ? '1px solid #dc3545' 
                        : '1px solid rgba(255, 255, 255, 0.2)',
                      borderRadius: '8px',
                      color: 'white',
                      fontSize: '14px',
                      outline: 'none',
                      transition: 'all 0.2s'
                    }}
                    onFocus={(e) => {
                      e.target.style.border = errors.correo ? '1px solid #dc3545' : '1px solid #FF6B00';
                      e.target.style.boxShadow = errors.correo 
                        ? '0 0 0 3px rgba(220, 53, 69, 0.1)' 
                        : '0 0 0 3px rgba(255, 107, 0, 0.1)';
                    }}
                    onBlur={(e) => {
                      e.target.style.border = errors.correo 
                        ? '1px solid #dc3545' 
                        : '1px solid rgba(255, 255, 255, 0.2)';
                      e.target.style.boxShadow = 'none';
                    }}
                  />
                </div>
                {errors.correo && (
                  <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.correo}</p>
                )}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '14px',
                    fontWeight: '500',
                    color: 'rgba(255, 255, 255, 0.9)',
                    marginBottom: '0.5rem'
                  }}>
                    Contraseña *
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
                      placeholder="Mínimo 8 caracteres"
                      style={{
                        width: '100%',
                        paddingLeft: '2.5rem',
                        paddingRight: '2.5rem',
                        paddingTop: '0.75rem',
                        paddingBottom: '0.75rem',
                        background: 'rgba(255, 255, 255, 0.1)',
                        border: errors.contrasena 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)',
                        borderRadius: '8px',
                        color: 'white',
                        fontSize: '14px',
                        outline: 'none',
                        transition: 'all 0.2s'
                      }}
                      onFocus={(e) => {
                        e.target.style.border = errors.contrasena ? '1px solid #dc3545' : '1px solid #FF6B00';
                        e.target.style.boxShadow = errors.contrasena 
                          ? '0 0 0 3px rgba(220, 53, 69, 0.1)' 
                          : '0 0 0 3px rgba(255, 107, 0, 0.1)';
                      }}
                      onBlur={(e) => {
                        e.target.style.border = errors.contrasena 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)';
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
                  {formData.contrasena && (
                    <div style={{ marginTop: '0.5rem' }}>
                      <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        marginBottom: '4px'
                      }}>
                        <div style={{
                          flex: 1,
                          height: '6px',
                          background: 'rgba(255, 255, 255, 0.1)',
                          borderRadius: '3px',
                          overflow: 'hidden'
                        }}>
                          <div style={{
                            height: '100%',
                            width: passwordStrength.width,
                            background: passwordStrength.color,
                            borderRadius: '3px',
                            transition: 'all 0.3s'
                          }}></div>
                        </div>
                        <span style={{
                          fontSize: '12px',
                          color: passwordStrength.color,
                          fontWeight: '500',
                          minWidth: '50px',
                          textAlign: 'right'
                        }}>
                          {passwordStrength.label}
                        </span>
                      </div>
                      <div style={{
                        fontSize: '11px',
                        color: 'rgba(255, 255, 255, 0.6)',
                        marginTop: '4px'
                      }}>
                        {formData.contrasena.length}/50 caracteres
                      </div>
                    </div>
                  )}
                  {errors.contrasena && (
                    <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.contrasena}</p>
                  )}
                </div>

                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '14px',
                    fontWeight: '500',
                    color: 'rgba(255, 255, 255, 0.9)',
                    marginBottom: '0.5rem'
                  }}>
                    Confirmar Contraseña *
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
                      name="confirmarContrasena"
                      value={formData.confirmarContrasena}
                      onChange={handleChange}
                      placeholder="Repite la contraseña"
                      style={{
                        width: '100%',
                        paddingLeft: '2.5rem',
                        paddingRight: '1rem',
                        paddingTop: '0.75rem',
                        paddingBottom: '0.75rem',
                        background: 'rgba(255, 255, 255, 0.1)',
                        border: errors.confirmarContrasena 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)',
                        borderRadius: '8px',
                        color: 'white',
                        fontSize: '14px',
                        outline: 'none',
                        transition: 'all 0.2s'
                      }}
                      onFocus={(e) => {
                        e.target.style.border = errors.confirmarContrasena ? '1px solid #dc3545' : '1px solid #FF6B00';
                        e.target.style.boxShadow = errors.confirmarContrasena 
                          ? '0 0 0 3px rgba(220, 53, 69, 0.1)' 
                          : '0 0 0 3px rgba(255, 107, 0, 0.1)';
                      }}
                      onBlur={(e) => {
                        e.target.style.border = errors.confirmarContrasena 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)';
                        e.target.style.boxShadow = 'none';
                      }}
                    />
                  </div>
                  {errors.confirmarContrasena && (
                    <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.confirmarContrasena}</p>
                  )}
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
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
                  <div style={{ position: 'relative' }}>
                    <Phone style={{
                      position: 'absolute',
                      left: '12px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      color: 'rgba(255, 255, 255, 0.5)'
                    }} size={20} />
                    <input
                      type="tel"
                      name="telefono"
                      value={formData.telefono}
                      onChange={handleChange}
                      placeholder="3001234567"
                      maxLength={10}
                      style={{
                        width: '100%',
                        paddingLeft: '2.5rem',
                        paddingRight: '1rem',
                        paddingTop: '0.75rem',
                        paddingBottom: '0.75rem',
                        background: 'rgba(255, 255, 255, 0.1)',
                        border: errors.telefono 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)',
                        borderRadius: '8px',
                        color: 'white',
                        fontSize: '14px',
                        outline: 'none',
                        transition: 'all 0.2s'
                      }}
                      onFocus={(e) => {
                        e.target.style.border = errors.telefono ? '1px solid #dc3545' : '1px solid #FF6B00';
                        e.target.style.boxShadow = errors.telefono 
                          ? '0 0 0 3px rgba(220, 53, 69, 0.1)' 
                          : '0 0 0 3px rgba(255, 107, 0, 0.1)';
                      }}
                      onBlur={(e) => {
                        e.target.style.border = errors.telefono 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)';
                        e.target.style.boxShadow = 'none';
                      }}
                    />
                  </div>
                  {errors.telefono && (
                    <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.telefono}</p>
                  )}
                </div>

                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '14px',
                    fontWeight: '500',
                    color: 'rgba(255, 255, 255, 0.9)',
                    marginBottom: '0.5rem'
                  }}>
                    Dirección
                  </label>
                  <div style={{ position: 'relative' }}>
                    <MapPin style={{
                      position: 'absolute',
                      left: '12px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      color: 'rgba(255, 255, 255, 0.5)'
                    }} size={20} />
                    <input
                      type="text"
                      name="direccion"
                      value={formData.direccion}
                      onChange={handleChange}
                      placeholder="Calle 22 #5-20"
                      style={{
                        width: '100%',
                        paddingLeft: '2.5rem',
                        paddingRight: '1rem',
                        paddingTop: '0.75rem',
                        paddingBottom: '0.75rem',
                        background: 'rgba(255, 255, 255, 0.1)',
                        border: errors.direccion 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)',
                        borderRadius: '8px',
                        color: 'white',
                        fontSize: '14px',
                        outline: 'none',
                        transition: 'all 0.2s'
                      }}
                      onFocus={(e) => {
                        e.target.style.border = errors.direccion ? '1px solid #dc3545' : '1px solid #FF6B00';
                        e.target.style.boxShadow = errors.direccion 
                          ? '0 0 0 3px rgba(220, 53, 69, 0.1)' 
                          : '0 0 0 3px rgba(255, 107, 0, 0.1)';
                      }}
                      onBlur={(e) => {
                        e.target.style.border = errors.direccion 
                          ? '1px solid #dc3545' 
                          : '1px solid rgba(255, 255, 255, 0.2)';
                        e.target.style.boxShadow = 'none';
                      }}
                    />
                  </div>
                  {errors.direccion && (
                    <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.direccion}</p>
                  )}
                </div>
              </div>

              {/* Selección de Rol */}
              <div>
                <label style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '500',
                  color: 'rgba(255, 255, 255, 0.9)',
                  marginBottom: '0.75rem'
                }}>
                  Tipo de Cuenta *
                </label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1rem' }}>
                  <button
                    type="button"
                    onClick={() => setFormData({ ...formData, rol: 'CLIENTE' })}
                    style={{
                      padding: '1.5rem',
                      border: errors.rol 
                        ? '2px solid #dc3545' 
                        : formData.rol === 'CLIENTE' 
                          ? '2px solid #FF6B00' 
                          : '2px solid rgba(255, 255, 255, 0.2)',
                      borderRadius: '8px',
                      background: formData.rol === 'CLIENTE' 
                        ? 'rgba(255, 107, 0, 0.1)' 
                        : 'rgba(255, 255, 255, 0.05)',
                      transition: 'all 0.2s',
                      cursor: 'pointer',
                      textAlign: 'center'
                    }}
                    onMouseOver={(e) => {
                      if (formData.rol !== 'CLIENTE') {
                        e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                        e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (formData.rol !== 'CLIENTE') {
                        e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                        e.target.style.background = 'rgba(255, 255, 255, 0.05)';
                      }
                    }}
                  >
                    <Heart style={{
                      margin: '0 auto 0.5rem auto',
                      color: formData.rol === 'CLIENTE' ? '#FF6B00' : 'rgba(255, 255, 255, 0.5)'
                    }} size={32} />
                    <h3 style={{
                      fontWeight: '600',
                      color: 'white',
                      marginBottom: '0.25rem'
                    }}>Cliente</h3>
                    <p style={{
                      fontSize: '14px',
                      color: 'rgba(255, 255, 255, 0.7)',
                      marginTop: '0.25rem'
                    }}>
                      Busca y reserva servicios deportivos
                    </p>
                  </button>

                  <button
                    type="button"
                    onClick={() => setFormData({ ...formData, rol: 'PROVEEDOR' })}
                    style={{
                      padding: '1.5rem',
                      border: errors.rol 
                        ? '2px solid #dc3545' 
                        : formData.rol === 'PROVEEDOR' 
                          ? '2px solid #FF6B00' 
                          : '2px solid rgba(255, 255, 255, 0.2)',
                      borderRadius: '8px',
                      background: formData.rol === 'PROVEEDOR' 
                        ? 'rgba(255, 107, 0, 0.1)' 
                        : 'rgba(255, 255, 255, 0.05)',
                      transition: 'all 0.2s',
                      cursor: 'pointer',
                      textAlign: 'center'
                    }}
                    onMouseOver={(e) => {
                      if (formData.rol !== 'PROVEEDOR') {
                        e.target.style.borderColor = 'rgba(255, 255, 255, 0.3)';
                        e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (formData.rol !== 'PROVEEDOR') {
                        e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                        e.target.style.background = 'rgba(255, 255, 255, 0.05)';
                      }
                    }}
                  >
                    <Briefcase style={{
                      margin: '0 auto 0.5rem auto',
                      color: formData.rol === 'PROVEEDOR' ? '#FF6B00' : 'rgba(255, 255, 255, 0.5)'
                    }} size={32} />
                    <h3 style={{
                      fontWeight: '600',
                      color: 'white',
                      marginBottom: '0.25rem'
                    }}>Proveedor</h3>
                    <p style={{
                      fontSize: '14px',
                      color: 'rgba(255, 255, 255, 0.7)',
                      marginTop: '0.25rem'
                    }}>
                      Ofrece servicios deportivos
                    </p>
                  </button>
                </div>
                {errors.rol && (
                  <p style={{ marginTop: '0.5rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.rol}</p>
                )}
              </div>

              {/* Campos específicos para CLIENTE */}
              {formData.rol === 'CLIENTE' && (
                <div style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '1rem',
                  background: 'rgba(255, 107, 0, 0.1)',
                  padding: '1.5rem',
                  borderRadius: '8px',
                  border: '1px solid rgba(255, 107, 0, 0.3)'
                }}>
                  <h3 style={{
                    fontWeight: '600',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    marginBottom: '0.5rem'
                  }}>
                    <Heart size={20} style={{ color: '#FF6B00' }} />
                    Información de Cliente
                  </h3>

                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '14px',
                      fontWeight: '500',
                      color: 'rgba(255, 255, 255, 0.9)',
                      marginBottom: '0.5rem'
                    }}>
                      Deportes de Interés
                    </label>
                    <select
                      name="preferenciaDeportes"
                      value={formData.preferenciaDeportes}
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
                        transition: 'all 0.2s',
                        cursor: 'pointer'
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
                      <option value="" style={{ background: '#1a2f5a', color: 'white' }}>Selecciona un deporte</option>
                      {deportesPopulares.map(deporte => (
                        <option key={deporte} value={deporte} style={{ background: '#1a2f5a', color: 'white' }}>{deporte}</option>
                      ))}
                    </select>
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
                    <div style={{ position: 'relative' }}>
                      <TrendingUp style={{
                        position: 'absolute',
                        left: '12px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        color: 'rgba(255, 255, 255, 0.5)'
                      }} size={20} />
                      <select
                        name="nivelExperiencia"
                        value={formData.nivelExperiencia}
                        onChange={handleChange}
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
                          transition: 'all 0.2s',
                          cursor: 'pointer'
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
                        {nivelesExperiencia.map(nivel => (
                          <option key={nivel} value={nivel} style={{ background: '#1a2f5a', color: 'white' }}>{nivel}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '14px',
                      fontWeight: '500',
                      color: 'rgba(255, 255, 255, 0.9)',
                      marginBottom: '0.5rem'
                    }}>
                      Fecha de Nacimiento
                    </label>
                    <div style={{ position: 'relative' }}>
                      <Calendar style={{
                        position: 'absolute',
                        left: '12px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        color: 'rgba(255, 255, 255, 0.5)'
                      }} size={20} />
                      <input
                        type="date"
                        name="fechaNacimiento"
                        value={formData.fechaNacimiento}
                        onChange={handleChange}
                        style={{
                          width: '100%',
                          paddingLeft: '2.5rem',
                          paddingRight: '1rem',
                          paddingTop: '0.75rem',
                          paddingBottom: '0.75rem',
                          background: 'rgba(255, 255, 255, 0.1)',
                          border: errors.fechaNacimiento 
                            ? '1px solid #dc3545' 
                            : '1px solid rgba(255, 255, 255, 0.2)',
                          borderRadius: '8px',
                          color: 'white',
                          fontSize: '14px',
                          outline: 'none',
                          transition: 'all 0.2s'
                        }}
                        onFocus={(e) => {
                          e.target.style.border = errors.fechaNacimiento ? '1px solid #dc3545' : '1px solid #FF6B00';
                          e.target.style.boxShadow = errors.fechaNacimiento 
                            ? '0 0 0 3px rgba(220, 53, 69, 0.1)' 
                            : '0 0 0 3px rgba(255, 107, 0, 0.1)';
                        }}
                        onBlur={(e) => {
                          e.target.style.border = errors.fechaNacimiento 
                            ? '1px solid #dc3545' 
                            : '1px solid rgba(255, 255, 255, 0.2)';
                          e.target.style.boxShadow = 'none';
                        }}
                      />
                    </div>
                    {errors.fechaNacimiento && (
                      <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.fechaNacimiento}</p>
                    )}
                  </div>
                </div>
              )}

              {/* Campos específicos para PROVEEDOR */}
              {formData.rol === 'PROVEEDOR' && (
                <div style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '1rem',
                  background: 'rgba(255, 107, 0, 0.1)',
                  padding: '1.5rem',
                  borderRadius: '8px',
                  border: '1px solid rgba(255, 107, 0, 0.3)'
                }}>
                  <h3 style={{
                    fontWeight: '600',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    marginBottom: '0.5rem'
                  }}>
                    <Briefcase size={20} style={{ color: '#FF6B00' }} />
                    Información de Negocio
                  </h3>

                  <div>
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
                      placeholder="Describe tu negocio, servicios que ofreces, experiencia..."
                      rows={4}
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
                        resize: 'none',
                        transition: 'all 0.2s',
                        fontFamily: 'inherit'
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
                    <p style={{
                      marginTop: '0.25rem',
                      fontSize: '14px',
                      color: 'rgba(255, 255, 255, 0.6)'
                    }}>
                      {formData.descripcionNegocio.length}/200 caracteres
                    </p>
                  </div>
                </div>
              )}

              {/* Botón de Registro */}
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
                  boxShadow: loading ? 'none' : '0 4px 6px rgba(255, 107, 0, 0.3)'
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
                {loading ? 'Registrando...' : 'Crear Cuenta'}
              </button>

              {/* Link a Login */}
              <div style={{ textAlign: 'center' }}>
                <p style={{
                  color: 'rgba(255, 255, 255, 0.7)',
                  fontSize: '14px'
                }}>
                  ¿Ya tienes cuenta?{' '}
                  <Link 
                    to="/login" 
                    style={{
                      color: '#FF6B00',
                      fontWeight: '500',
                      textDecoration: 'none',
                      transition: 'color 0.2s'
                    }}
                    onMouseOver={(e) => {
                      e.target.style.color = '#ff8533';
                    }}
                    onMouseOut={(e) => {
                      e.target.style.color = '#FF6B00';
                    }}
                  >
                    Inicia sesión aquí
                  </Link>
                </p>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;