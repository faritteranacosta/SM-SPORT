import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import ReservaModal from './ReservaModal';
import { Search, Filter, Grid3x3, List, Calendar, MapPin, DollarSign, Star, X, ArrowLeft } from 'lucide-react';

const Servicios = () => {
  const navigate = useNavigate();
  const [servicios, setServicios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [servicioSeleccionado, setServicioSeleccionado] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [userRol, setUserRol] = useState(null);
  const [servicioDetalle, setServicioDetalle] = useState(null);
  
  // Estados para búsqueda y filtros
  const [busquedaTexto, setBusquedaTexto] = useState('');
  const [filtroDeporte, setFiltroDeporte] = useState('TODOS');
  const [filtroPrecioMin, setFiltroPrecioMin] = useState('');
  const [filtroPrecioMax, setFiltroPrecioMax] = useState('');
  const [filtroCiudad, setFiltroCiudad] = useState('');
  const [vistaModo, setVistaModo] = useState('grid');
  const [mostrarFiltros, setMostrarFiltros] = useState(false);

  useEffect(() => {
    // Obtener el rol del usuario desde localStorage
    try {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        const user = JSON.parse(storedUser);
        setUserRol(user.rol);
      }
    } catch (err) {
      console.error('Error al obtener rol del usuario:', err);
    }
  }, []);

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

  const verDetalleServicio = (servicio) => {
    setServicioDetalle(servicio);
  };

  const cerrarDetalle = () => {
    setServicioDetalle(null);
  };

  const deportes = [
    'Fútbol', 'Baloncesto', 'Tenis', 'Voleibol', 'Natación',
    'Boxeo', 'Yoga', 'CrossFit', 'Ciclismo', 'Running'
  ];

  // Lógica de filtrado
  const serviciosFiltrados = servicios.filter(servicio => {
    // Búsqueda por texto
    if (busquedaTexto.trim() !== '') {
      const textoBusqueda = busquedaTexto.toLowerCase();
      const nombre = (servicio.nombre || '').toLowerCase();
      const descripcion = (servicio.descripcion || '').toLowerCase();
      const deporte = (servicio.deporte || '').toLowerCase();
      const ciudad = (servicio.ciudad || '').toLowerCase();
      
      if (!nombre.includes(textoBusqueda) && 
          !descripcion.includes(textoBusqueda) && 
          !deporte.includes(textoBusqueda) &&
          !ciudad.includes(textoBusqueda)) {
        return false;
      }
    }
    
    // Filtro por deporte
    if (filtroDeporte !== 'TODOS' && servicio.deporte !== filtroDeporte) {
      return false;
    }
    
    // Filtro por precio mínimo
    if (filtroPrecioMin && servicio.precio < parseFloat(filtroPrecioMin)) {
      return false;
    }
    
    // Filtro por precio máximo
    if (filtroPrecioMax && servicio.precio > parseFloat(filtroPrecioMax)) {
      return false;
    }
    
    // Filtro por ciudad
    if (filtroCiudad.trim() !== '' && !servicio.ciudad?.toLowerCase().includes(filtroCiudad.toLowerCase())) {
      return false;
    }
    
    return true;
  });

  const limpiarFiltros = () => {
    setBusquedaTexto('');
    setFiltroDeporte('TODOS');
    setFiltroPrecioMin('');
    setFiltroPrecioMax('');
    setFiltroCiudad('');
  };

  if (loading) {
    return <div className="auth-container">Cargando servicios...</div>;
  }

  const getTitulo = () => {
    if (userRol === 'ADMINISTRADOR') return 'Servicios Creados';
    if (userRol === 'PROVEEDOR') return 'Explorar Otros Servicios';
    return 'Explorar Servicios';
  };

  // Si hay un servicio en detalle, mostrar la vista de detalle
  if (servicioDetalle) {
    return (
      <div className="auth-container">
        <div style={{
          background: 'rgba(255, 255, 255, 0.1)',
          backdropFilter: 'blur(10px)',
          WebkitBackdropFilter: 'blur(10px)',
          borderRadius: '20px',
          border: '1px solid rgba(255, 255, 255, 0.2)',
          padding: '2rem',
          maxWidth: '900px',
          margin: '0 auto'
        }}>
          <button
            onClick={cerrarDetalle}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              background: 'rgba(255, 255, 255, 0.1)',
              border: '1px solid rgba(255, 255, 255, 0.2)',
              borderRadius: '8px',
              padding: '0.5rem 1rem',
              color: 'white',
              cursor: 'pointer',
              marginBottom: '1.5rem',
              transition: 'all 0.2s'
            }}
            onMouseOver={(e) => {
              e.target.style.background = 'rgba(255, 107, 0, 0.2)';
              e.target.style.borderColor = '#FF6B00';
            }}
            onMouseOut={(e) => {
              e.target.style.background = 'rgba(255, 255, 255, 0.1)';
              e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
            }}
          >
            <ArrowLeft size={20} />
            Volver a la lista
          </button>
          
          <h1 style={{ color: 'white', marginBottom: '1.5rem', fontSize: '2rem' }}>
            {servicioDetalle.nombre}
          </h1>
          
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
            gap: '1.5rem',
            marginBottom: '2rem'
          }}>
            <div style={{
              background: 'rgba(255, 255, 255, 0.05)',
              padding: '1rem',
              borderRadius: '12px',
              border: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem' }}>
                <DollarSign size={20} color="#FF6B00" />
                <span style={{ color: 'rgba(255, 255, 255, 0.6)', fontSize: '14px' }}>Precio</span>
              </div>
              <p style={{ color: 'white', fontSize: '1.5rem', fontWeight: 'bold', margin: 0 }}>
                ${servicioDetalle.precio?.toLocaleString('es-CO')}
              </p>
            </div>
            
            <div style={{
              background: 'rgba(255, 255, 255, 0.05)',
              padding: '1rem',
              borderRadius: '12px',
              border: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem' }}>
                <Star size={20} color="#FF6B00" />
                <span style={{ color: 'rgba(255, 255, 255, 0.6)', fontSize: '14px' }}>Calificación</span>
              </div>
              <p style={{ color: 'white', fontSize: '1.5rem', fontWeight: 'bold', margin: 0 }}>
                {servicioDetalle.calificacionPromedio || 'N/A'}
              </p>
            </div>
            
            <div style={{
              background: 'rgba(255, 255, 255, 0.05)',
              padding: '1rem',
              borderRadius: '12px',
              border: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem' }}>
                <span style={{
                  background: 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                  color: 'white',
                  padding: '4px 12px',
                  borderRadius: '20px',
                  fontSize: '12px',
                  fontWeight: '500'
                }}>
                  {servicioDetalle.deporte}
                </span>
              </div>
            </div>
          </div>
          
          {servicioDetalle.descripcion && (
            <div style={{ marginBottom: '2rem' }}>
              <h3 style={{ color: 'white', marginBottom: '1rem' }}>Descripción</h3>
              <p style={{ color: 'rgba(255, 255, 255, 0.8)', lineHeight: '1.6' }}>
                {servicioDetalle.descripcion}
              </p>
            </div>
          )}
          
          <div style={{ marginBottom: '2rem' }}>
            <h3 style={{ color: 'white', marginBottom: '1rem' }}>Ubicación</h3>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'rgba(255, 255, 255, 0.8)' }}>
              <MapPin size={20} color="#FF6B00" />
              <span>
                {servicioDetalle.direccion}, {servicioDetalle.ciudad}
              </span>
            </div>
          </div>
          
          <div style={{ marginBottom: '2rem' }}>
            <h3 style={{ color: 'white', marginBottom: '1rem' }}>Disponibilidad</h3>
            <div style={{
              background: 'rgba(255, 255, 255, 0.05)',
              padding: '1rem',
              borderRadius: '12px',
              border: '1px solid rgba(255, 255, 255, 0.1)'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '0.5rem' }}>
                <Calendar size={20} color="#FF6B00" />
                <span style={{ color: 'rgba(255, 255, 255, 0.8)', fontSize: '14px' }}>
                  Horario disponible: 8:00 AM - 6:00 PM
                </span>
              </div>
              <p style={{ color: 'rgba(255, 255, 255, 0.6)', fontSize: '13px', margin: '0.5rem 0 0 0' }}>
                Para verificar disponibilidad específica de fechas y horas, haz clic en "Reservar Ahora" y selecciona tu fecha y hora preferida.
              </p>
            </div>
          </div>
          
          {userRol === 'CLIENTE' && (
            <button
              onClick={() => {
                setServicioSeleccionado(servicioDetalle);
                setModalOpen(true);
                setServicioDetalle(null);
              }}
              style={{
                width: '100%',
                padding: '1rem 2rem',
                background: 'linear-gradient(135deg, #28a745 0%, #34ce57 100%)',
                border: 'none',
                borderRadius: '12px',
                color: 'white',
                fontSize: '16px',
                fontWeight: '600',
                cursor: 'pointer',
                transition: 'all 0.3s',
                boxShadow: '0 4px 12px rgba(40, 167, 69, 0.3)'
              }}
              onMouseOver={(e) => {
                e.target.style.transform = 'translateY(-2px)';
                e.target.style.boxShadow = '0 6px 16px rgba(40, 167, 69, 0.4)';
              }}
              onMouseOut={(e) => {
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = '0 4px 12px rgba(40, 167, 69, 0.3)';
              }}
            >
              Reservar Ahora
            </button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="auth-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ margin: 0 }}>{getTitulo()}</h2>
        <button
          onClick={() => navigate('/dashboard')}
          style={{
            padding: '8px 16px',
            background: 'rgba(255, 255, 255, 0.1)',
            color: 'white',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '14px',
            fontWeight: '500',
            transition: 'all 0.2s'
          }}
          onMouseOver={(e) => {
            e.target.style.background = 'rgba(255, 255, 255, 0.2)';
          }}
          onMouseOut={(e) => {
            e.target.style.background = 'rgba(255, 255, 255, 0.1)';
          }}
        >
          ← Volver al Dashboard
        </button>
      </div>
      {error && <div className="error-message">{error}</div>}
      
      {/* Buscador y Filtros */}
      <div style={{
        background: 'rgba(255, 255, 255, 0.1)',
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)',
        borderRadius: '16px',
        border: '1px solid rgba(255, 255, 255, 0.2)',
        padding: '1.5rem',
        marginBottom: '2rem'
      }}>
        <div style={{
          display: 'flex',
          gap: '1rem',
          marginBottom: mostrarFiltros ? '1rem' : '0'
        }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <Search style={{
              position: 'absolute',
              left: '12px',
              top: '50%',
              transform: 'translateY(-50%)',
              color: 'rgba(255, 255, 255, 0.5)'
            }} size={20} />
            <input
              type="text"
              placeholder="Buscar por nombre, descripción, deporte o ciudad..."
              value={busquedaTexto}
              onChange={(e) => setBusquedaTexto(e.target.value)}
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
          <button
            onClick={() => setMostrarFiltros(!mostrarFiltros)}
            style={{
              padding: '0.75rem 1.5rem',
              background: mostrarFiltros ? 'rgba(255, 107, 0, 0.2)' : 'rgba(255, 255, 255, 0.1)',
              border: '1px solid rgba(255, 255, 255, 0.2)',
              borderRadius: '8px',
              color: 'white',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              transition: 'all 0.2s'
            }}
          >
            <Filter size={20} />
            Filtros
          </button>
          <button
            onClick={() => setVistaModo(vistaModo === 'grid' ? 'lista' : 'grid')}
            style={{
              padding: '0.75rem 1.5rem',
              background: 'rgba(255, 255, 255, 0.1)',
              border: '1px solid rgba(255, 255, 255, 0.2)',
              borderRadius: '8px',
              color: 'white',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              transition: 'all 0.2s'
            }}
          >
            {vistaModo === 'grid' ? <List size={20} /> : <Grid3x3 size={20} />}
            {vistaModo === 'grid' ? 'Lista' : 'Grid'}
          </button>
        </div>
        
        {mostrarFiltros && (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
            gap: '1rem',
            paddingTop: '1rem',
            borderTop: '1px solid rgba(255, 255, 255, 0.1)'
          }}>
            <div>
              <label style={{
                display: 'block',
                fontSize: '12px',
                fontWeight: '500',
                color: 'rgba(255, 255, 255, 0.8)',
                marginBottom: '0.5rem'
              }}>
                Deporte
              </label>
              <select
                value={filtroDeporte}
                onChange={(e) => setFiltroDeporte(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  color: 'white',
                  fontSize: '14px',
                  outline: 'none',
                  cursor: 'pointer'
                }}
              >
                <option value="TODOS" style={{ background: '#1a2f5a', color: 'white' }}>Todos</option>
                {deportes.map(deporte => (
                  <option key={deporte} value={deporte} style={{ background: '#1a2f5a', color: 'white' }}>{deporte}</option>
                ))}
              </select>
            </div>
            
            <div>
              <label style={{
                display: 'block',
                fontSize: '12px',
                fontWeight: '500',
                color: 'rgba(255, 255, 255, 0.8)',
                marginBottom: '0.5rem'
              }}>
                Precio Mínimo
              </label>
              <input
                type="number"
                placeholder="0"
                value={filtroPrecioMin}
                onChange={(e) => setFiltroPrecioMin(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  color: 'white',
                  fontSize: '14px',
                  outline: 'none'
                }}
              />
            </div>
            
            <div>
              <label style={{
                display: 'block',
                fontSize: '12px',
                fontWeight: '500',
                color: 'rgba(255, 255, 255, 0.8)',
                marginBottom: '0.5rem'
              }}>
                Precio Máximo
              </label>
              <input
                type="number"
                placeholder="Sin límite"
                value={filtroPrecioMax}
                onChange={(e) => setFiltroPrecioMax(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  color: 'white',
                  fontSize: '14px',
                  outline: 'none'
                }}
              />
            </div>
            
            <div>
              <label style={{
                display: 'block',
                fontSize: '12px',
                fontWeight: '500',
                color: 'rgba(255, 255, 255, 0.8)',
                marginBottom: '0.5rem'
              }}>
                Ciudad
              </label>
              <input
                type="text"
                placeholder="Buscar ciudad..."
                value={filtroCiudad}
                onChange={(e) => setFiltroCiudad(e.target.value)}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  color: 'white',
                  fontSize: '14px',
                  outline: 'none'
                }}
              />
            </div>
            
            <div style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button
                onClick={limpiarFiltros}
                style={{
                  width: '100%',
                  padding: '0.75rem 1rem',
                  background: 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  color: 'white',
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                Limpiar Filtros
              </button>
            </div>
          </div>
        )}
        
        <div style={{
          marginTop: '1rem',
          paddingTop: '1rem',
          borderTop: '1px solid rgba(255, 255, 255, 0.1)',
          color: 'rgba(255, 255, 255, 0.8)',
          fontSize: '14px'
        }}>
          {serviciosFiltrados.length} {serviciosFiltrados.length === 1 ? 'servicio encontrado' : 'servicios encontrados'}
        </div>
      </div>
      
      {servicios.length === 0 ? (
        <div style={{ 
          textAlign: 'center', 
          padding: '60px 20px',
          color: 'rgba(255, 255, 255, 0.8)'
        }}>
          <p style={{ fontSize: '18px' }}>No hay servicios disponibles en este momento.</p>
        </div>
      ) : serviciosFiltrados.length === 0 ? (
        <div style={{ 
          textAlign: 'center', 
          padding: '60px 20px',
          color: 'rgba(255, 255, 255, 0.8)'
        }}>
          <p style={{ fontSize: '18px' }}>No se encontraron servicios con los filtros aplicados.</p>
        </div>
      ) : (
        <div className="servicios-list" style={{
          display: vistaModo === 'grid' ? 'grid' : 'flex',
          gridTemplateColumns: vistaModo === 'grid' ? 'repeat(auto-fill, minmax(320px, 1fr))' : 'none',
          flexDirection: vistaModo === 'lista' ? 'column' : 'auto',
          gap: '1.5rem'
        }}>
          {serviciosFiltrados.map((servicio) => (
            <div 
              key={servicio.idServicio} 
              className="servicio-card"
              onClick={() => verDetalleServicio(servicio)}
              style={{
                background: 'rgba(255, 255, 255, 0.1)',
                backdropFilter: 'blur(10px)',
                WebkitBackdropFilter: 'blur(10px)',
                borderRadius: '16px',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                padding: '1.5rem',
                cursor: 'pointer',
                transition: 'all 0.3s',
                boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                display: vistaModo === 'lista' ? 'flex' : 'block',
                gap: vistaModo === 'lista' ? '1.5rem' : '0'
              }}
              onMouseOver={(e) => {
                e.currentTarget.style.transform = 'translateY(-4px)';
                e.currentTarget.style.background = 'rgba(255, 255, 255, 0.15)';
                e.currentTarget.style.borderColor = '#FF6B00';
                e.currentTarget.style.boxShadow = '0 8px 16px rgba(0, 0, 0, 0.2)';
              }}
              onMouseOut={(e) => {
                e.currentTarget.style.transform = 'translateY(0)';
                e.currentTarget.style.background = 'rgba(255, 255, 255, 0.1)';
                e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                e.currentTarget.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
              }}
            >
              <div style={{ flex: vistaModo === 'lista' ? '1' : 'auto' }}>
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  marginBottom: '1rem'
                }}>
                  <h3 style={{
                    margin: 0,
                    color: 'white',
                    fontSize: '1.25rem',
                    fontWeight: '600',
                    flex: 1
                  }}>
                    {servicio.nombre}
                  </h3>
                  <span style={{
                    background: 'linear-gradient(135deg, #FF6B00 0%, #ff8533 100%)',
                    color: 'white',
                    padding: '4px 12px',
                    borderRadius: '20px',
                    fontSize: '12px',
                    fontWeight: '500',
                    whiteSpace: 'nowrap',
                    marginLeft: '0.5rem'
                  }}>
                    {servicio.deporte}
                  </span>
                </div>
                
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: vistaModo === 'lista' ? 'repeat(3, 1fr)' : '1fr',
                  gap: '1rem',
                  marginBottom: '1rem'
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <DollarSign size={18} color="#FF6B00" />
                    <div>
                      <div style={{ fontSize: '12px', color: 'rgba(255, 255, 255, 0.6)' }}>Precio</div>
                      <div style={{ color: 'white', fontWeight: '600' }}>
                        ${servicio.precio?.toLocaleString('es-CO')}
                      </div>
                    </div>
                  </div>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Star size={18} color="#FF6B00" />
                    <div>
                      <div style={{ fontSize: '12px', color: 'rgba(255, 255, 255, 0.6)' }}>Calificación</div>
                      <div style={{ color: 'white', fontWeight: '600' }}>
                        {servicio.calificacionPromedio || 'N/A'}
                      </div>
                    </div>
                  </div>
                  
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <MapPin size={18} color="#FF6B00" />
                    <div>
                      <div style={{ fontSize: '12px', color: 'rgba(255, 255, 255, 0.6)' }}>Ubicación</div>
                      <div style={{ color: 'white', fontWeight: '500', fontSize: '13px' }}>
                        {servicio.ciudad}
                      </div>
                    </div>
                  </div>
                </div>
                
                {servicio.descripcion && (
                  <p style={{
                    color: 'rgba(255, 255, 255, 0.7)',
                    fontSize: '14px',
                    lineHeight: '1.5',
                    marginBottom: '1rem',
                    display: '-webkit-box',
                    WebkitLineClamp: vistaModo === 'lista' ? 2 : 3,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden'
                  }}>
                    {servicio.descripcion}
                  </p>
                )}
                
                {userRol === 'CLIENTE' && (
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleReservar(servicio);
                    }}
                    style={{
                      width: '100%',
                      padding: '0.75rem 1rem',
                      background: 'linear-gradient(135deg, #28a745 0%, #34ce57 100%)',
                      border: 'none',
                      borderRadius: '8px',
                      color: 'white',
                      fontSize: '14px',
                      fontWeight: '500',
                      cursor: 'pointer',
                      transition: 'all 0.2s',
                      boxShadow: '0 2px 4px rgba(40, 167, 69, 0.3)'
                    }}
                    onMouseOver={(e) => {
                      e.target.style.transform = 'translateY(-2px)';
                      e.target.style.boxShadow = '0 4px 8px rgba(40, 167, 69, 0.4)';
                    }}
                    onMouseOut={(e) => {
                      e.target.style.transform = 'translateY(0)';
                      e.target.style.boxShadow = '0 2px 4px rgba(40, 167, 69, 0.3)';
                    }}
                  >
                    Reservar Ahora
                  </button>
                )}
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

      <style>{`
        .servicio-card {
          position: relative;
        }
        
        @media (max-width: 768px) {
          .servicios-list {
            grid-template-columns: 1fr !important;
          }
        }
      `}</style>
    </div>
  );
};

export default Servicios;

