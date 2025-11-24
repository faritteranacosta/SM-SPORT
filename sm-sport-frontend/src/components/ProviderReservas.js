import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Search, Filter, Grid3x3, List, Calendar, X, CheckCircle, XCircle, AlertCircle } from 'lucide-react';

// Componente para gestión de reservas del proveedor
// Permite confirmar o rechazar reservas pendientes
const ProviderReservas = () => {
  const [reservas, setReservas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [accionLoading, setAccionLoading] = useState(null);
  const [filtroEstado, setFiltroEstado] = useState('TODAS');
  
  // Estados para el modal de confirmación
  const [modalConfirmacion, setModalConfirmacion] = useState({
    mostrar: false,
    tipo: '', // 'confirmar' o 'rechazar'
    idReserva: null,
    datosReserva: null,
    motivoRechazo: ''
  });
  
  // Estados para modales de éxito/error
  const [modalMensaje, setModalMensaje] = useState({
    mostrar: false,
    tipo: '', // 'exito' o 'error'
    titulo: '',
    mensaje: ''
  });
  
  // Estados para búsqueda y filtros
  const [busquedaTexto, setBusquedaTexto] = useState('');
  const [filtroFechaDesde, setFiltroFechaDesde] = useState('');
  const [filtroFechaHasta, setFiltroFechaHasta] = useState('');
  const [filtroCliente, setFiltroCliente] = useState('');
  const [vistaModo, setVistaModo] = useState('lista'); // 'lista' o 'grid'
  const [mostrarFiltros, setMostrarFiltros] = useState(false);

  useEffect(() => {
    const cargarReservas = async () => {
      try {
        setLoading(true);
        setError('');
        
        // Obtener todas las reservas con paginación si es necesario
        // El backend devuelve por defecto página 0 con tamaño 20
        // Para obtener todas, podemos aumentar el tamaño o hacer múltiples peticiones
        const response = await axios.get('/api/v1/reservas', {
          params: {
            pagina: 0,
            tamano: 100 // Aumentar el tamaño para obtener más reservas
          }
        });
        
        // Manejar respuesta paginada o directa
        const reservasObtenidas = response.data.content || response.data || [];
        
        console.log('=== ProviderReservas - Cargando reservas ===');
        console.log('Reservas obtenidas:', reservasObtenidas.length);
        console.log('Estados de reservas:', reservasObtenidas.map(r => r.estado));
        console.log('Datos completos:', reservasObtenidas);
        
        setReservas(reservasObtenidas);
      } catch (err) {
        setError('Error al cargar las reservas. Por favor, intenta nuevamente.');
        console.error('Error al cargar reservas:', err);
        console.error('Detalles del error:', err.response?.data);
      } finally {
        setLoading(false);
      }
    };

    cargarReservas();
  }, []);

  const abrirModalConfirmacion = (tipo, idReserva, datosReserva) => {
    setModalConfirmacion({
      mostrar: true,
      tipo,
      idReserva,
      datosReserva,
      motivoRechazo: ''
    });
  };

  const cerrarModalConfirmacion = () => {
    setModalConfirmacion({
      mostrar: false,
      tipo: '',
      idReserva: null,
      datosReserva: null,
      motivoRechazo: ''
    });
  };

  const confirmarReserva = async () => {
    const { idReserva } = modalConfirmacion;
    
    try {
      setAccionLoading(idReserva);
      
      await axios.post(`/api/v1/reservas/${idReserva}/confirmar`);

      // Recargar las reservas para obtener los datos actualizados del servidor
      const response = await axios.get('/api/v1/reservas', {
        params: {
          pagina: 0,
          tamano: 100
        }
      });
      const reservasActualizadas = response.data.content || response.data || [];
      setReservas(reservasActualizadas);

      cerrarModalConfirmacion();
      setModalMensaje({
        mostrar: true,
        tipo: 'exito',
        titulo: 'Reserva Confirmada',
        mensaje: 'La reserva ha sido confirmada exitosamente. El cliente ha sido notificado.'
      });
    } catch (err) {
      console.error('Error al confirmar reserva:', err);
      const mensajeError = err.response?.data?.message || err.response?.data?.error || 'Error al confirmar la reserva';
      cerrarModalConfirmacion();
      setModalMensaje({
        mostrar: true,
        tipo: 'error',
        titulo: 'Error al Confirmar',
        mensaje: mensajeError
      });
    } finally {
      setAccionLoading(null);
    }
  };

  const rechazarReserva = async () => {
    const { idReserva, motivoRechazo } = modalConfirmacion;
    
    if (!motivoRechazo || motivoRechazo.trim() === '') {
      setModalMensaje({
        mostrar: true,
        tipo: 'error',
        titulo: 'Motivo Requerido',
        mensaje: 'Debes proporcionar un motivo para rechazar la reserva'
      });
      return;
    }

    try {
      setAccionLoading(idReserva);
      
      // El backend espera el motivo como query parameter, no en el body
      await axios.post(`/api/v1/reservas/${idReserva}/rechazar`, null, {
        params: {
          motivo: motivoRechazo.trim()
        }
      });

      // Recargar las reservas para obtener los datos actualizados del servidor
      const response = await axios.get('/api/v1/reservas', {
        params: {
          pagina: 0,
          tamano: 100
        }
      });
      const reservasActualizadas = response.data.content || response.data || [];
      setReservas(reservasActualizadas);

      cerrarModalConfirmacion();
      setModalMensaje({
        mostrar: true,
        tipo: 'exito',
        titulo: 'Reserva Rechazada',
        mensaje: 'La reserva ha sido rechazada exitosamente.'
      });
    } catch (err) {
      console.error('Error al rechazar reserva:', err);
      const mensajeError = err.response?.data?.message || err.response?.data?.error || 'Error al rechazar la reserva';
      cerrarModalConfirmacion();
      setModalMensaje({
        mostrar: true,
        tipo: 'error',
        titulo: 'Error al Rechazar',
        mensaje: mensajeError
      });
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
      case 'PENDIENTE': return '';
      case 'CONFIRMADA': return '';
      case 'RECHAZADA': return '';
      case 'CANCELADA': return '';
      case 'FINALIZADA': return '';
      default: return '';
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
    // Manejar diferentes formatos de hora
    let horaStr = hora;
    if (typeof hora === 'object' && hora.hour !== undefined) {
      // Si viene como objeto {hour, minute, second, nano}
      horaStr = `${String(hora.hour).padStart(2, '0')}:${String(hora.minute || 0).padStart(2, '0')}`;
    } else if (typeof hora === 'string') {
      horaStr = hora;
    } else {
      return 'N/A';
    }
    
    const [h, m] = horaStr.split(':');
    const hour = parseInt(h);
    if (isNaN(hour)) return horaStr;
    
    const ampm = hour >= 12 ? 'p. m.' : 'a. m.';
    const displayHour = hour > 12 ? hour - 12 : hour === 0 ? 12 : hour;
    const minute = m || '00';
    return `${displayHour}:${minute} ${ampm}`;
  };

  // Lógica de filtrado avanzado
  const reservasFiltradas = reservas.filter(reserva => {
    // Filtro por estado
    if (filtroEstado !== 'TODAS' && reserva.estado !== filtroEstado) {
      return false;
    }
    
    // Búsqueda por texto (nombre del servicio, cliente, deporte)
    if (busquedaTexto.trim() !== '') {
      const textoBusqueda = busquedaTexto.toLowerCase();
      const nombreServicio = (reserva.nombreServicio || reserva.servicio?.nombre || '').toLowerCase();
      const nombreCliente = (reserva.nombreCliente || reserva.cliente?.nombre || '').toLowerCase();
      const deporte = (reserva.deporteServicio || reserva.servicio?.deporte || '').toLowerCase();
      
      if (!nombreServicio.includes(textoBusqueda) && 
          !nombreCliente.includes(textoBusqueda) && 
          !deporte.includes(textoBusqueda)) {
        return false;
      }
    }
    
    // Filtro por cliente
    if (filtroCliente.trim() !== '') {
      const nombreCliente = (reserva.nombreCliente || reserva.cliente?.nombre || '').toLowerCase();
      if (!nombreCliente.includes(filtroCliente.toLowerCase())) {
        return false;
      }
    }
    
    // Filtro por fecha desde
    if (filtroFechaDesde) {
      const fechaReserva = new Date(reserva.fechaReserva);
      const fechaDesde = new Date(filtroFechaDesde);
      fechaDesde.setHours(0, 0, 0, 0);
      if (fechaReserva < fechaDesde) {
        return false;
      }
    }
    
    // Filtro por fecha hasta
    if (filtroFechaHasta) {
      const fechaReserva = new Date(reserva.fechaReserva);
      const fechaHasta = new Date(filtroFechaHasta);
      fechaHasta.setHours(23, 59, 59, 999);
      if (fechaReserva > fechaHasta) {
        return false;
      }
    }
    
    return true;
  });
  
  const limpiarFiltros = () => {
    setBusquedaTexto('');
    setFiltroFechaDesde('');
    setFiltroFechaHasta('');
    setFiltroCliente('');
    setFiltroEstado('TODAS');
  };

  const contarPorEstado = (estado) => {
    return reservas.filter(r => r.estado === estado).length;
  };

  if (loading) {
    return <div className="w-full">Cargando reservas...</div>;
  }

  return (
    <div className="w-full">
      {error && <div className="error-message mb-4 p-3 bg-red-100 border border-red-400 text-red-700 rounded">{error}</div>}
      
      {reservas.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon"></div>
          <h3>No tienes reservas aún</h3>
          <p>Los clientes podrán reservar tus servicios pronto</p>
        </div>
      ) : (
        <div className="reservas-container">
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
            {/* Barra de búsqueda principal */}
            <div style={{
              display: 'flex',
              gap: '1rem',
              marginBottom: mostrarFiltros ? '1rem' : '0'
            }}>
              <div style={{
                flex: 1,
                position: 'relative'
              }}>
                <Search style={{
                  position: 'absolute',
                  left: '12px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  color: 'rgba(255, 255, 255, 0.5)'
                }} size={20} />
                <input
                  type="text"
                  placeholder="Buscar por servicio, cliente o deporte..."
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
                  background: mostrarFiltros 
                    ? 'rgba(255, 107, 0, 0.2)' 
                    : 'rgba(255, 255, 255, 0.1)',
                  border: '1px solid rgba(255, 255, 255, 0.2)',
                  borderRadius: '8px',
                  color: 'white',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  transition: 'all 0.2s'
                }}
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(255, 107, 0, 0.2)';
                  e.target.style.borderColor = '#FF6B00';
                }}
                onMouseOut={(e) => {
                  if (!mostrarFiltros) {
                    e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                    e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                  }
                }}
              >
                <Filter size={20} />
                Filtros
              </button>
              <button
                onClick={() => setVistaModo(vistaModo === 'lista' ? 'grid' : 'lista')}
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
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(255, 107, 0, 0.2)';
                  e.target.style.borderColor = '#FF6B00';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                  e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                }}
              >
                {vistaModo === 'lista' ? <Grid3x3 size={20} /> : <List size={20} />}
                {vistaModo === 'lista' ? 'Grid' : 'Lista'}
              </button>
            </div>
            
            {/* Panel de filtros expandible */}
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
                    Estado
                  </label>
                  <select
                    value={filtroEstado}
                    onChange={(e) => setFiltroEstado(e.target.value)}
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
                    onFocus={(e) => {
                      e.target.style.border = '1px solid #FF6B00';
                    }}
                    onBlur={(e) => {
                      e.target.style.border = '1px solid rgba(255, 255, 255, 0.2)';
                    }}
                  >
                    <option value="TODAS" style={{ background: '#1a2f5a', color: 'white' }}>Todas</option>
                    <option value="PENDIENTE" style={{ background: '#1a2f5a', color: 'white' }}>Pendientes</option>
                    <option value="CONFIRMADA" style={{ background: '#1a2f5a', color: 'white' }}>Confirmadas</option>
                    <option value="RECHAZADA" style={{ background: '#1a2f5a', color: 'white' }}>Rechazadas</option>
                    <option value="CANCELADA" style={{ background: '#1a2f5a', color: 'white' }}>Canceladas</option>
                    <option value="FINALIZADA" style={{ background: '#1a2f5a', color: 'white' }}>Finalizadas</option>
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
                    Cliente
                  </label>
                  <input
                    type="text"
                    placeholder="Nombre del cliente..."
                    value={filtroCliente}
                    onChange={(e) => setFiltroCliente(e.target.value)}
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
                    onFocus={(e) => {
                      e.target.style.border = '1px solid #FF6B00';
                    }}
                    onBlur={(e) => {
                      e.target.style.border = '1px solid rgba(255, 255, 255, 0.2)';
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
                    Fecha Desde
                  </label>
                  <div style={{ position: 'relative' }}>
                    <Calendar style={{
                      position: 'absolute',
                      left: '12px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      color: 'rgba(255, 255, 255, 0.5)'
                    }} size={18} />
                    <input
                      type="date"
                      value={filtroFechaDesde}
                      onChange={(e) => setFiltroFechaDesde(e.target.value)}
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
                        outline: 'none'
                      }}
                      onFocus={(e) => {
                        e.target.style.border = '1px solid #FF6B00';
                      }}
                      onBlur={(e) => {
                        e.target.style.border = '1px solid rgba(255, 255, 255, 0.2)';
                      }}
                    />
                  </div>
                </div>
                
                <div>
                  <label style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: '500',
                    color: 'rgba(255, 255, 255, 0.8)',
                    marginBottom: '0.5rem'
                  }}>
                    Fecha Hasta
                  </label>
                  <div style={{ position: 'relative' }}>
                    <Calendar style={{
                      position: 'absolute',
                      left: '12px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      color: 'rgba(255, 255, 255, 0.5)'
                    }} size={18} />
                    <input
                      type="date"
                      value={filtroFechaHasta}
                      onChange={(e) => setFiltroFechaHasta(e.target.value)}
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
                        outline: 'none'
                      }}
                      onFocus={(e) => {
                        e.target.style.border = '1px solid #FF6B00';
                      }}
                      onBlur={(e) => {
                        e.target.style.border = '1px solid rgba(255, 255, 255, 0.2)';
                      }}
                    />
                  </div>
                </div>
                
                <div style={{
                  display: 'flex',
                  alignItems: 'flex-end'
                }}>
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
                      fontSize: '14px',
                      transition: 'all 0.2s'
                    }}
                    onMouseOver={(e) => {
                      e.target.style.background = 'rgba(220, 53, 69, 0.2)';
                      e.target.style.borderColor = '#dc3545';
                    }}
                    onMouseOut={(e) => {
                      e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                      e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)';
                    }}
                  >
                    Limpiar Filtros
                  </button>
                </div>
              </div>
            )}
            
            {/* Resultados */}
            <div style={{
              marginTop: '1rem',
              paddingTop: '1rem',
              borderTop: '1px solid rgba(255, 255, 255, 0.1)',
              color: 'rgba(255, 255, 255, 0.8)',
              fontSize: '14px'
            }}>
              {reservasFiltradas.length} {reservasFiltradas.length === 1 ? 'reserva encontrada' : 'reservas encontradas'}
            </div>
          </div>
          
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

          {/* Lista de reservas */}
          <div className="reservas-list">
            {reservasFiltradas.map((reserva) => (
              <div 
                key={reserva.idReserva} 
                className={`reserva-item ${reserva.estado === 'PENDIENTE' ? 'reserva-pendiente' : ''}`}
              >
                <div className="reserva-header">
                  <div className="reserva-info">
                    <h3>{reserva.nombreServicio || reserva.servicio?.nombre || 'Servicio'}</h3>
                    <span className="servicio-deporte">{reserva.deporteServicio || reserva.servicio?.deporte || 'N/A'}</span>
                  </div>
                  <div className="reserva-estado" style={{ backgroundColor: getEstadoColor(reserva.estado) }}>
                    <span className="estado-icon">{getEstadoIcon(reserva.estado)}</span>
                    <span className="estado-text">{reserva.estado}</span>
                  </div>
                </div>

                <div className="reserva-detalles">
                  <div className="detalle-item">
                    <span className="detalle-icon"></span>
                    <div>
                      <strong>Cliente</strong>
                      <p>{reserva.nombreCliente || reserva.cliente?.nombre || 'N/A'}</p>
                      <small>{reserva.cliente?.correo || 'N/A'}</small>
                    </div>
                  </div>
                  
                  <div className="detalle-item">
                    <span className="detalle-icon"></span>
                    <div>
                      <strong>Fecha</strong>
                      <p>{formatearFecha(reserva.fechaReserva)}</p>
                    </div>
                  </div>
                  
                  <div className="detalle-item">
                    <span className="detalle-icon"></span>
                    <div>
                      <strong>Hora</strong>
                      <p>{formatearHora(reserva.horaReserva)}</p>
                    </div>
                  </div>

                  <div className="detalle-item">
                    <span className="detalle-icon"></span>
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
                      <div className="acciones-pendiente">
                        <p className="texto-accion-pendiente">
                          Esta reserva está pendiente de tu aprobación
                        </p>
                        <div className="botones-accion">
                          <button 
                            className="btn-confirmar"
                            onClick={() => abrirModalConfirmacion('confirmar', reserva.idReserva, reserva)}
                            disabled={accionLoading === reserva.idReserva}
                          >
                            {accionLoading === reserva.idReserva ? 'Confirmando...' : 'Confirmar Reserva'}
                          </button>
                          <button 
                            className="btn-rechazar"
                            onClick={() => abrirModalConfirmacion('rechazar', reserva.idReserva, reserva)}
                            disabled={accionLoading === reserva.idReserva}
                          >
                            {accionLoading === reserva.idReserva ? 'Rechazando...' : 'Rechazar Reserva'}
                          </button>
                        </div>
                      </div>
                    </>
                  )}
                  
                  {reserva.estado === 'CONFIRMADA' && (
                    <div className="acciones-confirmada">
                      <p className="texto-accion-confirmada">
                        Reserva confirmada - El cliente ha sido notificado
                      </p>
                      <button className="btn-contactar" disabled>
                        Contactar Cliente
                      </button>
                    </div>
                  )}
                  
                  {reserva.estado === 'RECHAZADA' && (
                    <div className="acciones-rechazada">
                      <p className="texto-accion-rechazada">
                        Reserva rechazada
                      </p>
                    </div>
                  )}
                  
                  {reserva.estado === 'FINALIZADA' && (
                    <div className="acciones-finalizada">
                      <p className="texto-accion-finalizada">
                        Reserva finalizada
                      </p>
                      <button className="btn-calificar" disabled>
                        Ver Calificación
                      </button>
                    </div>
                  )}
                  
                  {reserva.estado === 'CANCELADA' && (
                    <div className="acciones-cancelada">
                      <p className="texto-accion-cancelada">
                        Reserva cancelada por el cliente
                      </p>
                    </div>
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

      <style>{`
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

        /* Destacar reservas pendientes */
        .reserva-item.reserva-pendiente {
          border-left: 4px solid #ffc107;
          box-shadow: 0 2px 10px rgba(255, 193, 7, 0.2);
          background: linear-gradient(to right, #fffbf0 0%, white 5%);
        }

        .reserva-item.reserva-pendiente:hover {
          box-shadow: 0 4px 20px rgba(255, 193, 7, 0.3);
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
          margin-bottom: 10px;
        }

        .acciones-pendiente {
          background: #fff3cd;
          border: 2px solid #ffc107;
          border-radius: 8px;
          padding: 15px;
          margin-bottom: 10px;
        }

        .texto-accion-pendiente {
          margin: 0 0 12px 0;
          color: #856404;
          font-weight: 500;
          font-size: 0.9rem;
        }

        .botones-accion {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .acciones-confirmada, .acciones-rechazada, .acciones-finalizada, .acciones-cancelada {
          background: #f8f9fa;
          border-radius: 6px;
          padding: 10px;
          margin-bottom: 10px;
        }

        .texto-accion-confirmada {
          margin: 0 0 8px 0;
          color: #155724;
          font-size: 0.85rem;
        }

        .texto-accion-rechazada {
          margin: 0;
          color: #721c24;
          font-size: 0.85rem;
        }

        .texto-accion-finalizada {
          margin: 0 0 8px 0;
          color: #0c5460;
          font-size: 0.85rem;
        }

        .texto-accion-cancelada {
          margin: 0;
          color: #6c757d;
          font-size: 0.85rem;
        }

        .btn-confirmar {
          background: #28a745;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 0.95rem;
          font-weight: 500;
          transition: all 0.2s;
          box-shadow: 0 2px 4px rgba(40, 167, 69, 0.3);
        }

        .btn-confirmar:hover:not(:disabled) {
          background: #218838;
          transform: translateY(-1px);
          box-shadow: 0 4px 6px rgba(40, 167, 69, 0.4);
        }

        .btn-confirmar:active:not(:disabled) {
          transform: translateY(0);
        }

        .btn-rechazar {
          background: #dc3545;
          color: white;
          border: none;
          padding: 10px 20px;
          border-radius: 6px;
          cursor: pointer;
          font-size: 0.95rem;
          font-weight: 500;
          transition: all 0.2s;
          box-shadow: 0 2px 4px rgba(220, 53, 69, 0.3);
        }

        .btn-rechazar:hover:not(:disabled) {
          background: #c82333;
          transform: translateY(-1px);
          box-shadow: 0 4px 6px rgba(220, 53, 69, 0.4);
        }

        .btn-rechazar:active:not(:disabled) {
          transform: translateY(0);
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
          opacity: 0.6;
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
      
      {/* Modal de Confirmación */}
      {modalConfirmacion.mostrar && (
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
          zIndex: 1000,
          padding: '1rem'
        }}
        onClick={cerrarModalConfirmacion}
        >
          <div style={{
            background: 'linear-gradient(135deg, rgba(14, 30, 64, 0.95) 0%, rgba(26, 47, 90, 0.95) 100%)',
            backdropFilter: 'blur(20px)',
            WebkitBackdropFilter: 'blur(20px)',
            borderRadius: '20px',
            border: '1px solid rgba(255, 255, 255, 0.2)',
            padding: '2rem',
            maxWidth: '500px',
            width: '100%',
            boxShadow: '0 20px 60px rgba(0, 0, 0, 0.5)',
            position: 'relative',
            animation: 'slideIn 0.3s ease-out'
          }}
          onClick={(e) => e.stopPropagation()}
          >
            {/* Botón cerrar */}
            <button
              onClick={cerrarModalConfirmacion}
              style={{
                position: 'absolute',
                top: '1rem',
                right: '1rem',
                background: 'rgba(255, 255, 255, 0.1)',
                border: 'none',
                borderRadius: '50%',
                width: '32px',
                height: '32px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                color: 'white',
                transition: 'all 0.2s'
              }}
              onMouseOver={(e) => {
                e.target.style.background = 'rgba(255, 107, 0, 0.3)';
                e.target.style.transform = 'rotate(90deg)';
              }}
              onMouseOut={(e) => {
                e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                e.target.style.transform = 'rotate(0deg)';
              }}
            >
              <X size={20} />
            </button>
            
            {/* Icono */}
            <div style={{
              display: 'flex',
              justifyContent: 'center',
              marginBottom: '1.5rem'
            }}>
              <div style={{
                width: '80px',
                height: '80px',
                borderRadius: '50%',
                background: modalConfirmacion.tipo === 'confirmar' 
                  ? 'linear-gradient(135deg, #28a745 0%, #20c997 100%)'
                  : 'linear-gradient(135deg, #dc3545 0%, #c82333 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: `0 8px 20px ${modalConfirmacion.tipo === 'confirmar' ? 'rgba(40, 167, 69, 0.4)' : 'rgba(220, 53, 69, 0.4)'}`
              }}>
                {modalConfirmacion.tipo === 'confirmar' ? (
                  <CheckCircle size={40} color="white" />
                ) : (
                  <XCircle size={40} color="white" />
                )}
              </div>
            </div>
            
            {/* Título */}
            <h2 style={{
              color: 'white',
              fontSize: '1.5rem',
              fontWeight: 'bold',
              textAlign: 'center',
              marginBottom: '0.5rem'
            }}>
              {modalConfirmacion.tipo === 'confirmar' 
                ? 'Confirmar Reserva' 
                : 'Rechazar Reserva'}
            </h2>
            
            {/* Mensaje */}
            <p style={{
              color: 'rgba(255, 255, 255, 0.8)',
              fontSize: '14px',
              textAlign: 'center',
              marginBottom: '1.5rem',
              lineHeight: '1.6'
            }}>
              {modalConfirmacion.tipo === 'confirmar' 
                ? '¿Estás seguro de que deseas confirmar esta reserva?\n\nAl confirmar, el cliente será notificado y la reserva pasará a estado CONFIRMADA.'
                : '¿Estás seguro de que deseas rechazar esta reserva?\n\nPor favor, proporciona un motivo para el rechazo.'}
            </p>
            
            {/* Información de la reserva */}
            {modalConfirmacion.datosReserva && (
              <div style={{
                background: 'rgba(255, 255, 255, 0.05)',
                borderRadius: '12px',
                padding: '1rem',
                marginBottom: '1.5rem',
                border: '1px solid rgba(255, 255, 255, 0.1)'
              }}>
                <div style={{
                  display: 'grid',
                  gridTemplateColumns: '1fr 1fr',
                  gap: '0.75rem',
                  fontSize: '13px'
                }}>
                  <div>
                    <span style={{ color: 'rgba(255, 255, 255, 0.6)' }}>Servicio:</span>
                    <p style={{ color: 'white', margin: '4px 0 0 0', fontWeight: '500' }}>
                      {modalConfirmacion.datosReserva.nombreServicio || modalConfirmacion.datosReserva.servicio?.nombre || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <span style={{ color: 'rgba(255, 255, 255, 0.6)' }}>Cliente:</span>
                    <p style={{ color: 'white', margin: '4px 0 0 0', fontWeight: '500' }}>
                      {modalConfirmacion.datosReserva.nombreCliente || modalConfirmacion.datosReserva.cliente?.nombre || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <span style={{ color: 'rgba(255, 255, 255, 0.6)' }}>Fecha:</span>
                    <p style={{ color: 'white', margin: '4px 0 0 0', fontWeight: '500' }}>
                      {formatearFecha(modalConfirmacion.datosReserva.fechaReserva)}
                    </p>
                  </div>
                  <div>
                    <span style={{ color: 'rgba(255, 255, 255, 0.6)' }}>Hora:</span>
                    <p style={{ color: 'white', margin: '4px 0 0 0', fontWeight: '500' }}>
                      {formatearHora(modalConfirmacion.datosReserva.horaReserva)}
                    </p>
                  </div>
                </div>
              </div>
            )}
            
            {/* Campo de motivo para rechazo */}
            {modalConfirmacion.tipo === 'rechazar' && (
              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{
                  display: 'block',
                  fontSize: '14px',
                  fontWeight: '500',
                  color: 'rgba(255, 255, 255, 0.9)',
                  marginBottom: '0.5rem'
                }}>
                  Motivo del rechazo *
                </label>
                <textarea
                  value={modalConfirmacion.motivoRechazo}
                  onChange={(e) => setModalConfirmacion({
                    ...modalConfirmacion,
                    motivoRechazo: e.target.value
                  })}
                  placeholder="Describe el motivo del rechazo..."
                  rows={3}
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
              </div>
            )}
            
            {/* Botones de acción */}
            <div style={{
              display: 'flex',
              gap: '1rem'
            }}>
              <button
                onClick={cerrarModalConfirmacion}
                style={{
                  flex: 1,
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
                onMouseOver={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.2)';
                }}
                onMouseOut={(e) => {
                  e.target.style.background = 'rgba(255, 255, 255, 0.1)';
                }}
              >
                Cancelar
              </button>
              <button
                onClick={modalConfirmacion.tipo === 'confirmar' ? confirmarReserva : rechazarReserva}
                disabled={accionLoading === modalConfirmacion.idReserva || (modalConfirmacion.tipo === 'rechazar' && !modalConfirmacion.motivoRechazo.trim())}
                style={{
                  flex: 1,
                  padding: '0.75rem 1.5rem',
                  background: modalConfirmacion.tipo === 'confirmar'
                    ? 'linear-gradient(135deg, #28a745 0%, #20c997 100%)'
                    : 'linear-gradient(135deg, #dc3545 0%, #c82333 100%)',
                  border: 'none',
                  borderRadius: '8px',
                  color: 'white',
                  fontSize: '14px',
                  fontWeight: '500',
                  cursor: (accionLoading === modalConfirmacion.idReserva || (modalConfirmacion.tipo === 'rechazar' && !modalConfirmacion.motivoRechazo.trim())) 
                    ? 'not-allowed' 
                    : 'pointer',
                  opacity: (accionLoading === modalConfirmacion.idReserva || (modalConfirmacion.tipo === 'rechazar' && !modalConfirmacion.motivoRechazo.trim())) 
                    ? 0.6 
                    : 1,
                  transition: 'all 0.2s',
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.3)'
                }}
                onMouseOver={(e) => {
                  if (!e.target.disabled) {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 6px 16px rgba(0, 0, 0, 0.4)';
                  }
                }}
                onMouseOut={(e) => {
                  if (!e.target.disabled) {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.3)';
                  }
                }}
              >
                {accionLoading === modalConfirmacion.idReserva 
                  ? (modalConfirmacion.tipo === 'confirmar' ? 'Confirmando...' : 'Rechazando...')
                  : (modalConfirmacion.tipo === 'confirmar' ? 'Confirmar' : 'Rechazar')}
              </button>
            </div>
          </div>
        </div>
      )}
      
      {/* Modal de Mensaje (Éxito/Error) */}
      {modalMensaje.mostrar && (
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
        onClick={() => setModalMensaje({ mostrar: false, tipo: '', titulo: '', mensaje: '' })}
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
            {/* Icono */}
            <div style={{
              display: 'flex',
              justifyContent: 'center',
              marginBottom: '1.5rem'
            }}>
              <div style={{
                width: '100px',
                height: '100px',
                borderRadius: '50%',
                background: modalMensaje.tipo === 'exito'
                  ? 'linear-gradient(135deg, #28a745 0%, #20c997 100%)'
                  : 'linear-gradient(135deg, #dc3545 0%, #c82333 100%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: `0 8px 20px ${modalMensaje.tipo === 'exito' ? 'rgba(40, 167, 69, 0.4)' : 'rgba(220, 53, 69, 0.4)'}`,
                animation: 'scaleIn 0.5s ease-out'
              }}>
                {modalMensaje.tipo === 'exito' ? (
                  <CheckCircle size={50} color="white" />
                ) : (
                  <AlertCircle size={50} color="white" />
                )}
              </div>
            </div>
            
            {/* Título */}
            <h2 style={{
              color: 'white',
              fontSize: '1.75rem',
              fontWeight: 'bold',
              marginBottom: '0.5rem'
            }}>
              {modalMensaje.titulo}
            </h2>
            
            {/* Mensaje */}
            <p style={{
              color: 'rgba(255, 255, 255, 0.8)',
              fontSize: '15px',
              marginBottom: '1.5rem',
              lineHeight: '1.6'
            }}>
              {modalMensaje.mensaje}
            </p>
            
            {/* Botón de cerrar */}
            <button
              onClick={() => setModalMensaje({ mostrar: false, tipo: '', titulo: '', mensaje: '' })}
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

export default ProviderReservas;
