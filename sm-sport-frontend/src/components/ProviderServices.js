import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { AlertCircle, MapPin, Calendar, Plus, Trash2, DollarSign, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import ProviderReservas from './ProviderReservas';

const ProviderServices = () => {
  const navigate = useNavigate();
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
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="mb-8">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <button
                  onClick={() => navigate('/dashboard')}
                  className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                >
                  <ArrowLeft className="w-5 h-5" />
                </button>
                <h1 className="text-2xl font-bold text-gray-900">Panel de Proveedor</h1>
              </div>
            </div>

            <div className="mt-6 border-b border-gray-200">
              <nav className="-mb-px flex space-x-8">
                <button
                  onClick={() => setActiveTab('servicios')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm ${
                    activeTab === 'servicios'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  Mis Servicios
                </button>
                <button
                  onClick={() => setActiveTab('reservas')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm ${
                    activeTab === 'reservas'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  Gestión de Reservas
                </button>
              </nav>
            </div>
          </div>

          <ProviderReservas />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <div className="mb-8">
            <button
              onClick={() => navigate('/dashboard')}
              className="mb-4 flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
              Volver al Dashboard
            </button>
            
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Publicar Nuevo Servicio</h1>
            <p className="text-gray-600">Completa el formulario para publicar tu servicio deportivo</p>

            <div className="mt-6 border-b border-gray-200">
              <nav className="-mb-px flex space-x-8">
                <button
                  onClick={() => setActiveTab('servicios')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm ${
                    activeTab === 'servicios'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  Mis Servicios
                </button>
                <button
                  onClick={() => setActiveTab('reservas')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm ${
                    activeTab === 'reservas'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  Gestión de Reservas
                </button>
              </nav>
            </div>
          </div>

          {successMessage && (
            <div className="mb-6 bg-green-50 border border-green-200 rounded-lg p-4 flex items-center gap-3">
              <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
                <span className="text-green-600 text-xl">✓</span>
              </div>
              <p className="text-green-800 font-medium">{successMessage}</p>
            </div>
          )}

          {errors.submit && (
            <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4 flex items-center gap-3">
              <AlertCircle className="text-red-600 flex-shrink-0" size={20} />
              <p className="text-red-800">{errors.submit}</p>
            </div>
          )}

          <div className="space-y-8">
            <section>
              <h2 className="text-xl font-semibold text-gray-900 mb-4 pb-2 border-b">
                Información Básica
              </h2>
              
              <div className="space-y-5">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre del Servicio *
                  </label>
                  <input
                    type="text"
                    name="nombre"
                    value={formData.nombre}
                    onChange={handleChange}
                    placeholder="Ej: Clases de Fútbol para Principiantes"
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    maxLength={150}
                  />
                  {errors.nombre && (
                    <p className="mt-1 text-sm text-red-600">{errors.nombre}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Deporte *
                  </label>
                  <select
                    name="deporte"
                    value={formData.deporte}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="">Selecciona un deporte</option>
                    {deportes.map(deporte => (
                      <option key={deporte} value={deporte}>{deporte}</option>
                    ))}
                  </select>
                  {errors.deporte && (
                    <p className="mt-1 text-sm text-red-600">{errors.deporte}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Descripción
                  </label>
                  <textarea
                    name="descripcion"
                    value={formData.descripcion}
                    onChange={handleChange}
                    placeholder="Describe tu servicio, qué incluye, qué nivel, etc."
                    rows={4}
                    maxLength={1000}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
                  />
                  <p className="mt-1 text-sm text-gray-500">
                    {formData.descripcion.length}/1000 caracteres
                  </p>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Precio (COP) *
                  </label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type="number"
                      name="precio"
                      value={formData.precio}
                      onChange={handleChange}
                      placeholder="0.00"
                      step="0.01"
                      min="0.01"
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  {errors.precio && (
                    <p className="mt-1 text-sm text-red-600">{errors.precio}</p>
                  )}
                </div>
              </div>
            </section>

            <section>
              <h2 className="text-xl font-semibold text-gray-900 mb-4 pb-2 border-b flex items-center gap-2">
                <MapPin size={20} />
                Ubicación
              </h2>

              <div className="space-y-5">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Dirección *
                  </label>
                  <input
                    type="text"
                    name="ubicacion.direccion"
                    value={formData.ubicacion.direccion}
                    onChange={handleChange}
                    placeholder="Ej: Calle 22 #5-20, Rodadero"
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  {errors.direccion && (
                    <p className="mt-1 text-sm text-red-600">{errors.direccion}</p>
                  )}
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Ciudad
                    </label>
                    <input
                      type="text"
                      name="ubicacion.ciudad"
                      value={formData.ubicacion.ciudad}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Departamento
                    </label>
                    <input
                      type="text"
                      name="ubicacion.departamento"
                      value={formData.ubicacion.departamento}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      País
                    </label>
                    <input
                      type="text"
                      name="ubicacion.pais"
                      value={formData.ubicacion.pais}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Latitud *
                    </label>
                    <input
                      type="number"
                      name="ubicacion.latitud"
                      value={formData.ubicacion.latitud}
                      onChange={handleChange}
                      placeholder="11.0041"
                      step="0.000001"
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Longitud *
                    </label>
                    <input
                      type="number"
                      name="ubicacion.longitud"
                      value={formData.ubicacion.longitud}
                      onChange={handleChange}
                      placeholder="-74.8070"
                      step="0.000001"
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                {errors.coordenadas && (
                  <p className="text-sm text-red-600">{errors.coordenadas}</p>
                )}

                <button
                  type="button"
                  onClick={obtenerUbicacionActual}
                  className="text-blue-600 hover:text-blue-700 text-sm font-medium flex items-center gap-2"
                >
                  <MapPin size={16} />
                  Usar mi ubicación actual
                </button>
              </div>
            </section>

            <section>
              <div className="flex items-center justify-between mb-4 pb-2 border-b">
                <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
                  <Calendar size={20} />
                  Disponibilidad (Opcional)
                </h2>
                <button
                  type="button"
                  onClick={agregarDisponibilidad}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  <Plus size={16} />
                  Agregar Disponibilidad
                </button>
              </div>

              {formData.disponibilidad.length === 0 ? (
                <div className="text-center py-8 border-2 border-dashed border-gray-300 rounded-lg">
                  <Calendar className="mx-auto text-gray-400 mb-2" size={40} />
                  <p className="text-gray-500">Sin disponibilidad configurada</p>
                  <p className="text-sm text-gray-400 mt-1">
                    Puedes agregar disponibilidad más tarde desde la edición del servicio
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {formData.disponibilidad.map((disp, index) => (
                    <div key={index} className="border border-gray-200 rounded-lg p-4">
                      <div className="flex justify-between items-start mb-3">
                        <h3 className="font-medium text-gray-900">Disponibilidad #{index + 1}</h3>
                        <button
                          type="button"
                          onClick={() => eliminarDisponibilidad(index)}
                          className="text-red-500 hover:text-red-700"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            Fecha
                          </label>
                          <input
                            type="date"
                            value={disp.fecha}
                            onChange={(e) => handleDisponibilidadChange(index, 'fecha', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            Hora Inicio
                          </label>
                          <input
                            type="time"
                            value={disp.horaInicio}
                            onChange={(e) => handleDisponibilidadChange(index, 'horaInicio', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            Hora Fin
                          </label>
                          <input
                            type="time"
                            value={disp.horaFin}
                            onChange={(e) => handleDisponibilidadChange(index, 'horaFin', e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            Cupos
                          </label>
                          <input
                            type="number"
                            value={disp.cuposDisponibles}
                            onChange={(e) => handleDisponibilidadChange(index, 'cuposDisponibles', parseInt(e.target.value))}
                            min="1"
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <div className="flex gap-4 pt-6 border-t">
              <button
                type="button"
                onClick={handleSubmit}
                disabled={loading}
                className="flex-1 bg-blue-600 text-white py-3 px-6 rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed font-medium transition-colors"
              >
                {loading ? 'Publicando...' : 'Publicar Servicio'}
              </button>
              
              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                className="px-6 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
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
