import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { AlertCircle, MapPin, Calendar, Plus, Trash2, DollarSign, ArrowLeft } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import ProviderReservas from './ProviderReservas';

const ProviderServices = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [proveedorUuid, setProveedorUuid] = useState(null);
  const [activeTab, setActiveTab] = useState('servicios');
  
  const [formData, setFormData] = useState({
    nombre: '',
    deporte: '',
    descripcion: '',
    precio: '',
    ubicacion: {
      direccion: '',
      ciudad: 'Santa Marta',
      departamento: 'Magdalena',
      pais: 'Colombia',
      latitud: '',
      longitud: ''
    },
    disponibilidad: []
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const deportes = [
    'Fútbol', 'Baloncesto', 'Tenis', 'Voleibol', 'Natación',
    'Boxeo', 'Yoga', 'CrossFit', 'Ciclismo', 'Running'
  ];

  // Verificar si hay un parámetro tab en la URL al cargar
  useEffect(() => {
    const tabParam = searchParams.get('tab');
    if (tabParam === 'reservas') {
      setActiveTab('reservas');
    }
  }, [searchParams]);

  useEffect(() => {
    const obtenerUsuarioDesdeStorage = () => {
      try {
        const storedUser = localStorage.getItem('user');
        console.log("DEBUG: Usuario en localStorage:", storedUser);
        
        if (!storedUser) {
          setErrors({ submit: 'No se encontraron datos de usuario. Inicia sesión nuevamente.' });
          return;
        }

        const userData = JSON.parse(storedUser);
        console.log("DEBUG: Usuario parseado:", userData);
        
        const uuid = userData.idUsuario;
        
        if (uuid) {
          setProveedorUuid(uuid);
          console.log("DEBUG: UUID del proveedor:", uuid);
        } else {
          setErrors({ submit: 'No se pudo obtener el ID del usuario. Inicia sesión nuevamente.' });
        }
      } catch (err) {
        console.error('Error al leer usuario desde localStorage:', err);
        setErrors({ submit: 'Error al procesar la autenticación. Inicia sesión nuevamente.' });
      }
    };

    obtenerUsuarioDesdeStorage();
  }, []);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.nombre || formData.nombre.length < 5) {
      newErrors.nombre = 'El nombre debe tener al menos 5 caracteres';
    }

    if (!formData.deporte) {
      newErrors.deporte = 'Debe seleccionar un deporte';
    }

    if (!formData.precio || parseFloat(formData.precio) <= 0) {
      newErrors.precio = 'El precio debe ser mayor a 0';
    }

    if (!formData.ubicacion.direccion) {
      newErrors.direccion = 'La dirección es obligatoria';
    }

    if (!formData.ubicacion.latitud || !formData.ubicacion.longitud) {
      newErrors.coordenadas = 'Las coordenadas son obligatorias';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrors({});
    setSuccessMessage('');
  
    let userRole = null;
    try {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        const parsedUser = JSON.parse(storedUser);
        userRole = parsedUser.rol;
      }
    } catch (parseError) {
      console.error('Error al leer el usuario desde localStorage:', parseError);
    }
  
    if (userRole !== 'PROVEEDOR') {
      setErrors({ submit: 'Solo los usuarios con rol PROVEEDOR pueden publicar servicios.' });
      return;
    }
  
    if (!proveedorUuid) {
      setErrors({ submit: 'No se pudo identificar al proveedor. Recarga la página.' });
      return;
    }
  
    if (!validateForm()) {
      return;
    }
  
    setLoading(true);
  
    try {
      console.log('Publicando servicio para proveedor:', proveedorUuid);
  
      const lat = parseFloat(formData.ubicacion.latitud);
      const lng = parseFloat(formData.ubicacion.longitud);
      
      if (isNaN(lat) || isNaN(lng)) {
        setErrors({ submit: 'Las coordenadas deben ser números válidos' });
        setLoading(false);
        return;
      }

      if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
        setErrors({ submit: 'Las coordenadas están fuera de rango válido' });
        setLoading(false);
        return;
      }
  
      const ubicacionPayload = {
        direccion: formData.ubicacion.direccion.trim(),
        ciudad: formData.ubicacion.ciudad.trim(),
        departamento: formData.ubicacion.departamento.trim(),
        pais: formData.ubicacion.pais.trim(),
        coordenadasLat: lat,
        coordenadasLng: lng
      };

      if (!ubicacionPayload.direccion || !ubicacionPayload.ciudad || 
          !ubicacionPayload.departamento || !ubicacionPayload.pais) {
        setErrors({ submit: 'Todos los campos de ubicación son obligatorios' });
        setLoading(false);
        return;
      }

      const payload = {
        idUsuario: proveedorUuid,
        nombre: formData.nombre.trim(),
        deporte: formData.deporte.trim(),
        descripcion: formData.descripcion?.trim() || null,
        precio: parseFloat(formData.precio),
        ubicacion: ubicacionPayload,
        disponibilidad: []
      };
  
      const response = await axios.post('/api/v1/servicios', payload, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      const servicioCreado = response.data;
  
      if (formData.disponibilidad && formData.disponibilidad.length > 0) {
        const disponibilidadPayload = formData.disponibilidad
          .filter((d) => d.fecha && d.horaInicio && d.horaFin)
          .map((d) => ({
            fecha: d.fecha,
            horaInicio: d.horaInicio,
            horaFin: d.horaFin,
            cuposDisponibles: Number(d.cuposDisponibles) || 1,
          }));
  
        if (disponibilidadPayload.length > 0) {
          try {
            await axios.post(
              `/api/v1/servicios/${servicioCreado.idServicio}/disponibilidad`,
              disponibilidadPayload
            );
            console.log('Disponibilidad agregada exitosamente');
          } catch (dispError) {
            console.error('Error al agregar disponibilidad:', dispError);
          }
        }
      }
  
      setSuccessMessage(`¡Servicio publicado exitosamente con ID: ${servicioCreado.idServicio}!`);
      
      setFormData({
        nombre: '',
        deporte: '',
        descripcion: '',
        precio: '',
        ubicacion: {
          direccion: '',
          ciudad: 'Santa Marta',
          departamento: 'Magdalena',
          pais: 'Colombia',
          latitud: '',
          longitud: ''
        },
        disponibilidad: []
      });
  
      window.scrollTo({ top: 0, behavior: 'smooth' });
  
    } catch (err) {
      console.error('Error al crear servicio:', err);
      
      let errorMessage = 'Error al crear el servicio. Revisa los datos.';
      
      if (err.response?.data) {
        const errorData = err.response.data;
        
        if (errorData.validationErrors) {
          const validationMessages = Object.entries(errorData.validationErrors)
            .map(([field, msg]) => `${field}: ${msg}`)
            .join('\n');
          errorMessage = `Errores de validación:\n${validationMessages}`;
        } else if (errorData.details && errorData.details.length > 0) {
          errorMessage = `Errores: ${errorData.details.join(', ')}`;
        } else if (errorData.message) {
          errorMessage = errorData.message;
        } else if (errorData.error) {
          errorMessage = `${errorData.error}: ${errorData.message || 'Error desconocido'}`;
        }
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      setErrors({ submit: errorMessage });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    
    if (name.startsWith('ubicacion.')) {
      const ubicacionField = name.split('.')[1];
      setFormData({
        ...formData,
        ubicacion: {
          ...formData.ubicacion,
          [ubicacionField]: value
        }
      });
    } else {
      setFormData({
        ...formData,
        [name]: value
      });
    }
  };

  const agregarDisponibilidad = () => {
    setFormData({
      ...formData,
      disponibilidad: [
        ...formData.disponibilidad,
        {
          fecha: '',
          horaInicio: '',
          horaFin: '',
          cuposDisponibles: 1
        }
      ]
    });
  };

  const eliminarDisponibilidad = (index) => {
    const newDisponibilidad = formData.disponibilidad.filter((_, i) => i !== index);
    setFormData({
      ...formData,
      disponibilidad: newDisponibilidad
    });
  };

  const handleDisponibilidadChange = (index, field, value) => {
    const newDisponibilidad = [...formData.disponibilidad];
    newDisponibilidad[index][field] = value;
    setFormData({
      ...formData,
      disponibilidad: newDisponibilidad
    });
  };

  const obtenerUbicacionActual = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setFormData({
            ...formData,
            ubicacion: {
              ...formData.ubicacion,
              latitud: position.coords.latitude.toFixed(6),
              longitud: position.coords.longitude.toFixed(6)
            }
          });
        },
        (error) => {
          setErrors({ ...errors, geolocalizacion: 'No se pudo obtener la ubicación' });
        }
      );
    }
  };

  if (activeTab === 'reservas') {
    return (
      <div style={{ minHeight: '100vh', padding: '2rem' }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <div style={{
            background: 'rgba(255, 255, 255, 0.1)',
            backdropFilter: 'blur(10px)',
            WebkitBackdropFilter: 'blur(10px)',
            borderRadius: '16px',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            padding: '2rem',
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)'
          }}>
            <div style={{ marginBottom: '2rem' }}>
              <button
                onClick={() => navigate('/dashboard')}
                style={{
                  marginBottom: '1rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  color: 'white',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  padding: '8px 16px',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  transition: 'all 0.2s'
                }}
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.2)';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                }}
              >
                <ArrowLeft className="w-5 h-5" />
                Volver al Dashboard
              </button>
              
              <h1 style={{ 
                fontSize: '2rem', 
                fontWeight: 'bold', 
                marginBottom: '0.5rem',
                color: 'white'
              }}>Gestión de Reservas</h1>
              <p style={{ color: 'rgba(255, 255, 255, 0.8)' }}>Gestiona las reservas realizadas a tus servicios</p>

              <div style={{ 
                marginTop: '1.5rem', 
                borderBottom: '1px solid rgba(255, 255, 255, 0.2)',
                paddingBottom: '0'
              }}>
                <nav style={{ display: 'flex', gap: '2rem' }}>
                  <button
                    onClick={() => setActiveTab('servicios')}
                    style={{
                      padding: '0.75rem 1rem',
                      borderBottom: activeTab === 'servicios' ? '2px solid #FF6B00' : '2px solid transparent',
                      background: 'transparent',
                      borderTop: 'none',
                      borderLeft: 'none',
                      borderRight: 'none',
                      color: activeTab === 'servicios' ? 'white' : 'rgba(255, 255, 255, 0.6)',
                      fontWeight: '500',
                      fontSize: '14px',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    }}
                    onMouseOver={(e) => {
                      if (activeTab !== 'servicios') {
                        e.target.style.color = 'rgba(255, 255, 255, 0.9)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (activeTab !== 'servicios') {
                        e.target.style.color = 'rgba(255, 255, 255, 0.6)';
                      }
                    }}
                  >
                    Mis Servicios
                  </button>
                  <button
                    onClick={() => setActiveTab('reservas')}
                    style={{
                      padding: '0.75rem 1rem',
                      borderBottom: activeTab === 'reservas' ? '2px solid #FF6B00' : '2px solid transparent',
                      background: 'transparent',
                      borderTop: 'none',
                      borderLeft: 'none',
                      borderRight: 'none',
                      color: activeTab === 'reservas' ? 'white' : 'rgba(255, 255, 255, 0.6)',
                      fontWeight: '500',
                      fontSize: '14px',
                      cursor: 'pointer',
                      transition: 'all 0.2s'
                    }}
                    onMouseOver={(e) => {
                      if (activeTab !== 'reservas') {
                        e.target.style.color = 'rgba(255, 255, 255, 0.9)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (activeTab !== 'reservas') {
                        e.target.style.color = 'rgba(255, 255, 255, 0.6)';
                      }
                    }}
                  >
                    Gestión de Reservas
                  </button>
                </nav>
              </div>
            </div>

            <ProviderReservas />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', padding: '2rem' }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <div style={{
          background: 'rgba(255, 255, 255, 0.1)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          borderRadius: '16px',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          padding: '2rem',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.1)'
        }}>
          <div className="mb-8">
            <button
              onClick={() => navigate('/dashboard')}
              style={{
                marginBottom: '1rem',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                color: 'white',
                background: 'rgba(255, 255, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                padding: '8px 16px',
                borderRadius: '8px',
                cursor: 'pointer',
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => {
                e.target.style.background = 'rgba(255, 255, 255, 0.2)';
              }}
              onMouseOut={(e) => {
                e.target.style.background = 'rgba(255, 255, 255, 0.1)';
              }}
            >
              <ArrowLeft className="w-5 h-5" />
              Volver al Dashboard
            </button>
            
            <h1 style={{ 
              fontSize: '2rem', 
              fontWeight: 'bold', 
              marginBottom: '0.5rem',
              color: 'white'
            }}>Publicar Nuevo Servicio</h1>
            <p style={{ color: 'rgba(255, 255, 255, 0.8)' }}>Completa el formulario para publicar tu servicio deportivo</p>

            <div style={{ 
              marginTop: '1.5rem', 
              borderBottom: '1px solid rgba(255, 255, 255, 0.2)',
              paddingBottom: '0'
            }}>
              <nav style={{ display: 'flex', gap: '2rem' }}>
                <button
                  onClick={() => setActiveTab('servicios')}
                  style={{
                    padding: '0.75rem 1rem',
                    borderBottom: activeTab === 'servicios' ? '2px solid #FF6B00' : '2px solid transparent',
                    background: 'transparent',
                    borderTop: 'none',
                    borderLeft: 'none',
                    borderRight: 'none',
                    color: activeTab === 'servicios' ? 'white' : 'rgba(255, 255, 255, 0.6)',
                    fontWeight: '500',
                    fontSize: '14px',
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                  }}
                  onMouseOver={(e) => {
                    if (activeTab !== 'servicios') {
                      e.target.style.color = 'rgba(255, 255, 255, 0.9)';
                    }
                  }}
                  onMouseOut={(e) => {
                    if (activeTab !== 'servicios') {
                      e.target.style.color = 'rgba(255, 255, 255, 0.6)';
                    }
                  }}
                >
                  Mis Servicios
                </button>
                <button
                  onClick={() => setActiveTab('reservas')}
                  style={{
                    padding: '0.75rem 1rem',
                    borderBottom: activeTab === 'reservas' ? '2px solid #FF6B00' : '2px solid transparent',
                    background: 'transparent',
                    borderTop: 'none',
                    borderLeft: 'none',
                    borderRight: 'none',
                    color: activeTab === 'reservas' ? 'white' : 'rgba(255, 255, 255, 0.6)',
                    fontWeight: '500',
                    fontSize: '14px',
                    cursor: 'pointer',
                    transition: 'all 0.2s'
                  }}
                  onMouseOver={(e) => {
                    if (activeTab !== 'reservas') {
                      e.target.style.color = 'rgba(255, 255, 255, 0.9)';
                    }
                  }}
                  onMouseOut={(e) => {
                    if (activeTab !== 'reservas') {
                      e.target.style.color = 'rgba(255, 255, 255, 0.6)';
                    }
                  }}
                >
                  Gestión de Reservas
                </button>
              </nav>
            </div>
          </div>

          {successMessage && (
            <div style={{
              marginBottom: '1.5rem',
              background: 'rgba(34, 197, 94, 0.2)',
              border: '1px solid rgba(34, 197, 94, 0.5)',
              borderRadius: '8px',
              padding: '1rem',
              display: 'flex',
              alignItems: 'center',
              gap: '12px'
            }}>
              <div style={{
                width: '32px',
                height: '32px',
                background: 'rgba(34, 197, 94, 0.3)',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0
              }}>
                <span style={{ color: '#4ade80', fontSize: '20px' }}>✓</span>
              </div>
              <p style={{ color: '#4ade80', fontWeight: '500' }}>{successMessage}</p>
            </div>
          )}

          {errors.submit && (
            <div style={{
              marginBottom: '1.5rem',
              background: 'rgba(220, 53, 69, 0.2)',
              border: '1px solid rgba(220, 53, 69, 0.5)',
              borderRadius: '8px',
              padding: '1rem',
              display: 'flex',
              alignItems: 'center',
              gap: '12px'
            }}>
              <AlertCircle className="text-red-400" size={20} style={{ flexShrink: 0 }} />
              <p style={{ color: '#ff6b6b' }}>{errors.submit}</p>
            </div>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
            <section>
              <h2 style={{
                fontSize: '1.25rem',
                fontWeight: '600',
                color: 'white',
                marginBottom: '1rem',
                paddingBottom: '0.5rem',
                borderBottom: '1px solid rgba(255, 255, 255, 0.2)'
              }}>
                Información Básica
              </h2>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '14px',
                    fontWeight: '500',
                    color: 'rgba(255, 255, 255, 0.9)',
                    marginBottom: '0.5rem'
                  }}>
                    Nombre del Servicio *
                  </label>
                  <input
                    type="text"
                    name="nombre"
                    value={formData.nombre}
                    onChange={handleChange}
                    placeholder="Ej: Clases de Fútbol para Principiantes"
                    maxLength={150}
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
                    Deporte *
                  </label>
                  <select
                    name="deporte"
                    value={formData.deporte}
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
                    {deportes.map(deporte => (
                      <option key={deporte} value={deporte} style={{ background: '#1a2f5a', color: 'white' }}>{deporte}</option>
                    ))}
                  </select>
                  {errors.deporte && (
                    <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.deporte}</p>
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
                    Descripción
                  </label>
                  <textarea
                    name="descripcion"
                    value={formData.descripcion}
                    onChange={handleChange}
                    placeholder="Describe tu servicio, qué incluye, qué nivel, etc."
                    rows={4}
                    maxLength={1000}
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
                  <p style={{ marginTop: '0.25rem', fontSize: '14px', color: 'rgba(255, 255, 255, 0.6)' }}>
                    {formData.descripcion.length}/1000 caracteres
                  </p>
                </div>

                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '14px',
                    fontWeight: '500',
                    color: 'rgba(255, 255, 255, 0.9)',
                    marginBottom: '0.5rem'
                  }}>
                    Precio (COP) *
                  </label>
                  <div style={{ position: 'relative' }}>
                    <DollarSign style={{
                      position: 'absolute',
                      left: '12px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      color: 'rgba(255, 255, 255, 0.5)'
                    }} size={20} />
                    <input
                      type="number"
                      name="precio"
                      value={formData.precio}
                      onChange={handleChange}
                      placeholder="0.00"
                      step="0.01"
                      min="0.01"
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
                  {errors.precio && (
                    <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.precio}</p>
                  )}
                </div>
              </div>
            </section>

            <section>
              <h2 style={{
                fontSize: '1.25rem',
                fontWeight: '600',
                color: 'white',
                marginBottom: '1rem',
                paddingBottom: '0.5rem',
                borderBottom: '1px solid rgba(255, 255, 255, 0.2)',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}>
                <MapPin size={20} />
                Ubicación
              </h2>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '14px',
                    fontWeight: '500',
                    color: 'rgba(255, 255, 255, 0.9)',
                    marginBottom: '0.5rem'
                  }}>
                    Dirección *
                  </label>
                  <input
                    type="text"
                    name="ubicacion.direccion"
                    value={formData.ubicacion.direccion}
                    onChange={handleChange}
                    placeholder="Ej: Calle 22 #5-20, Rodadero"
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
                  {errors.direccion && (
                    <p style={{ marginTop: '0.25rem', fontSize: '14px', color: '#ff6b6b' }}>{errors.direccion}</p>
                  )}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '14px',
                      fontWeight: '500',
                      color: 'rgba(255, 255, 255, 0.9)',
                      marginBottom: '0.5rem'
                    }}>
                      Ciudad
                    </label>
                    <input
                      type="text"
                      name="ubicacion.ciudad"
                      value={formData.ubicacion.ciudad}
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
                      Departamento
                    </label>
                    <input
                      type="text"
                      name="ubicacion.departamento"
                      value={formData.ubicacion.departamento}
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
                      País
                    </label>
                    <input
                      type="text"
                      name="ubicacion.pais"
                      value={formData.ubicacion.pais}
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
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{
                      display: 'block',
                      fontSize: '14px',
                      fontWeight: '500',
                      color: 'rgba(255, 255, 255, 0.9)',
                      marginBottom: '0.5rem'
                    }}>
                      Latitud *
                    </label>
                    <input
                      type="number"
                      name="ubicacion.latitud"
                      value={formData.ubicacion.latitud}
                      onChange={handleChange}
                      placeholder="11.0041"
                      step="0.000001"
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
                      Longitud *
                    </label>
                    <input
                      type="number"
                      name="ubicacion.longitud"
                      value={formData.ubicacion.longitud}
                      onChange={handleChange}
                      placeholder="-74.8070"
                      step="0.000001"
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
                </div>

                {errors.coordenadas && (
                  <p style={{ fontSize: '14px', color: '#ff6b6b' }}>{errors.coordenadas}</p>
                )}

                <button
                  type="button"
                  onClick={obtenerUbicacionActual}
                  style={{
                    color: '#FF6B00',
                    fontSize: '14px',
                    fontWeight: '500',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    background: 'transparent',
                    border: 'none',
                    cursor: 'pointer',
                    padding: '0.5rem 0',
                    transition: 'color 0.2s'
                  }}
                  onMouseOver={(e) => {
                    e.target.style.color = '#ff8533';
                  }}
                  onMouseOut={(e) => {
                    e.target.style.color = '#FF6B00';
                  }}
                >
                  <MapPin size={16} />
                  Usar mi ubicación actual
                </button>
              </div>
            </section>

            <section>
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: '1rem',
                paddingBottom: '0.5rem',
                borderBottom: '1px solid rgba(255, 255, 255, 0.2)'
              }}>
                <h2 style={{
                  fontSize: '1.25rem',
                  fontWeight: '600',
                  color: 'white',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px'
                }}>
                  <Calendar size={20} />
                  Disponibilidad (Opcional)
                </h2>
                <button
                  type="button"
                  onClick={agregarDisponibilidad}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '0.5rem 1rem',
                    background: 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                    color: 'white',
                    borderRadius: '8px',
                    border: 'none',
                    fontWeight: '500',
                    fontSize: '14px',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    boxShadow: '0 2px 4px rgba(255, 107, 0, 0.3)'
                  }}
                  onMouseOver={(e) => {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 4px 8px rgba(255, 107, 0, 0.4)';
                  }}
                  onMouseOut={(e) => {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = '0 2px 4px rgba(255, 107, 0, 0.3)';
                  }}
                >
                  <Plus size={16} />
                  Agregar Disponibilidad
                </button>
              </div>

              {formData.disponibilidad.length === 0 ? (
                <div style={{
                  textAlign: 'center',
                  padding: '2rem',
                  border: '2px dashed rgba(255, 255, 255, 0.3)',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.05)'
                }}>
                  <Calendar style={{ margin: '0 auto', color: 'rgba(255, 255, 255, 0.5)', marginBottom: '0.5rem' }} size={40} />
                  <p style={{ color: 'rgba(255, 255, 255, 0.7)', marginBottom: '0.25rem' }}>Sin disponibilidad configurada</p>
                  <p style={{ fontSize: '14px', color: 'rgba(255, 255, 255, 0.5)', marginTop: '0.25rem' }}>
                    Puedes agregar disponibilidad más tarde desde la edición del servicio
                  </p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {formData.disponibilidad.map((disp, index) => (
                    <div key={index} style={{
                      border: '1px solid rgba(255, 255, 255, 0.2)',
                      borderRadius: '8px',
                      padding: '1rem',
                      background: 'rgba(255, 255, 255, 0.05)'
                    }}>
                      <div style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'flex-start',
                        marginBottom: '0.75rem'
                      }}>
                        <h3 style={{ fontWeight: '500', color: 'white' }}>Disponibilidad #{index + 1}</h3>
                        <button
                          type="button"
                          onClick={() => eliminarDisponibilidad(index)}
                          style={{
                            color: '#ff6b6b',
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            padding: '4px',
                            transition: 'color 0.2s'
                          }}
                          onMouseOver={(e) => {
                            e.target.style.color = '#ff5252';
                          }}
                          onMouseOut={(e) => {
                            e.target.style.color = '#ff6b6b';
                          }}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>

                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem' }}>
                        <div>
                          <label style={{
                            display: 'block',
                            fontSize: '14px',
                            fontWeight: '500',
                            color: 'rgba(255, 255, 255, 0.9)',
                            marginBottom: '0.25rem'
                          }}>
                            Fecha
                          </label>
                          <input
                            type="date"
                            value={disp.fecha}
                            onChange={(e) => handleDisponibilidadChange(index, 'fecha', e.target.value)}
                            style={{
                              width: '100%',
                              padding: '0.5rem',
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
                            marginBottom: '0.25rem'
                          }}>
                            Hora Inicio
                          </label>
                          <input
                            type="time"
                            value={disp.horaInicio}
                            onChange={(e) => handleDisponibilidadChange(index, 'horaInicio', e.target.value)}
                            style={{
                              width: '100%',
                              padding: '0.5rem',
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
                            marginBottom: '0.25rem'
                          }}>
                            Hora Fin
                          </label>
                          <input
                            type="time"
                            value={disp.horaFin}
                            onChange={(e) => handleDisponibilidadChange(index, 'horaFin', e.target.value)}
                            style={{
                              width: '100%',
                              padding: '0.5rem',
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
                            marginBottom: '0.25rem'
                          }}>
                            Cupos
                          </label>
                          <input
                            type="number"
                            value={disp.cuposDisponibles}
                            onChange={(e) => handleDisponibilidadChange(index, 'cuposDisponibles', parseInt(e.target.value))}
                            min="1"
                            style={{
                              width: '100%',
                              padding: '0.5rem',
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
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <div style={{
              display: 'flex',
              gap: '1rem',
              paddingTop: '1.5rem',
              borderTop: '1px solid rgba(255, 255, 255, 0.2)'
            }}>
              <button
                type="button"
                onClick={handleSubmit}
                disabled={loading}
                style={{
                  flex: 1,
                  background: loading ? 'rgba(255, 255, 255, 0.2)' : 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
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
                {loading ? 'Publicando...' : 'Publicar Servicio'}
              </button>
              
              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                style={{
                  padding: '0.75rem 1.5rem',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  color: 'white',
                  fontWeight: '500',
                  fontSize: '16px',
                  cursor: 'pointer',
                  transition: 'all 0.2s'
                }}
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.2)';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                }}
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProviderServices;
