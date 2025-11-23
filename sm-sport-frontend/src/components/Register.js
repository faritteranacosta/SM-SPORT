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

  const validateForm = () => {
    const newErrors = {};

    if (!formData.nombre || formData.nombre.length < 3) {
      newErrors.nombre = 'El nombre debe tener al menos 3 caracteres';
    }

    if (!formData.correo || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.correo)) {
      newErrors.correo = 'Ingresa un correo válido';
    }

    if (!formData.contrasena || formData.contrasena.length < 8) {
      newErrors.contrasena = 'La contraseña debe tener al menos 8 caracteres';
    } else if (!validatePassword(formData.contrasena)) {
      newErrors.contrasena = 'Debe contener mayúscula, minúscula, número y carácter especial (@#$%^&+=!)';
    }

    if (formData.contrasena !== formData.confirmarContrasena) {
      newErrors.confirmarContrasena = 'Las contraseñas no coinciden';
    }

    if (formData.telefono && !/^[0-9]{10}$/.test(formData.telefono)) {
      newErrors.telefono = 'El teléfono debe tener 10 dígitos';
    }

    if (!formData.rol) {
      newErrors.rol = 'Debes seleccionar un rol';
    }

    // Validaciones específicas por rol
    if (formData.rol === 'CLIENTE' && formData.fechaNacimiento) {
      const fechaNac = new Date(formData.fechaNacimiento);
      const hoy = new Date();
      if (fechaNac >= hoy) {
        newErrors.fechaNacimiento = 'La fecha de nacimiento debe ser en el pasado';
      }
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
      setErrors({
        ...errors,
        [name]: undefined
      });
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl w-full">
        <div className="bg-white rounded-2xl shadow-2xl overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-green-600 px-8 py-6">
            <h2 className="text-3xl font-bold text-white text-center">
              Crear Cuenta
            </h2>
            <p className="text-blue-100 text-center mt-2">
              Únete a la comunidad deportiva de Santa Marta
            </p>
          </div>

          {/* Error General */}
          {errors.submit && (
            <div className="mx-8 mt-6 bg-red-50 border border-red-200 rounded-lg p-4 flex items-center gap-3">
              <AlertCircle className="text-red-600 flex-shrink-0" size={20} />
              <p className="text-red-800 text-sm">{errors.submit}</p>
            </div>
          )}

          <div className="px-8 py-8">
            <div className="space-y-6">
              {/* Información Básica */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Nombre Completo *
                </label>
                <div className="relative">
                  <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                  <input
                    type="text"
                    name="nombre"
                    value={formData.nombre}
                    onChange={handleChange}
                    placeholder="Ej: Juan Pérez"
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                {errors.nombre && (
                  <p className="mt-1 text-sm text-red-600">{errors.nombre}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Correo Electrónico *
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                  <input
                    type="email"
                    name="correo"
                    value={formData.correo}
                    onChange={handleChange}
                    placeholder="correo@ejemplo.com"
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                </div>
                {errors.correo && (
                  <p className="mt-1 text-sm text-red-600">{errors.correo}</p>
                )}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Contraseña *
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type={showPassword ? "text" : "password"}
                      name="contrasena"
                      value={formData.contrasena}
                      onChange={handleChange}
                      placeholder="Mínimo 8 caracteres"
                      className="w-full pl-10 pr-10 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                    </button>
                  </div>
                  {errors.contrasena && (
                    <p className="mt-1 text-sm text-red-600">{errors.contrasena}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Confirmar Contraseña *
                  </label>
                  <div className="relative">
                    <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type={showPassword ? "text" : "password"}
                      name="confirmarContrasena"
                      value={formData.confirmarContrasena}
                      onChange={handleChange}
                      placeholder="Repite la contraseña"
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  {errors.confirmarContrasena && (
                    <p className="mt-1 text-sm text-red-600">{errors.confirmarContrasena}</p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Teléfono
                  </label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type="tel"
                      name="telefono"
                      value={formData.telefono}
                      onChange={handleChange}
                      placeholder="3001234567"
                      maxLength={10}
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  {errors.telefono && (
                    <p className="mt-1 text-sm text-red-600">{errors.telefono}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Dirección
                  </label>
                  <div className="relative">
                    <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                    <input
                      type="text"
                      name="direccion"
                      value={formData.direccion}
                      onChange={handleChange}
                      placeholder="Calle 22 #5-20"
                      className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>
              </div>

              {/* Selección de Rol */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-3">
                  Tipo de Cuenta *
                </label>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <button
                    type="button"
                    onClick={() => setFormData({ ...formData, rol: 'CLIENTE' })}
                    className={`p-4 border-2 rounded-lg transition-all ${
                      formData.rol === 'CLIENTE'
                        ? 'border-blue-600 bg-blue-50'
                        : 'border-gray-300 hover:border-blue-300'
                    }`}
                  >
                    <Heart className={`mx-auto mb-2 ${formData.rol === 'CLIENTE' ? 'text-blue-600' : 'text-gray-400'}`} size={32} />
                    <h3 className="font-semibold text-gray-900">Cliente</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Busca y reserva servicios deportivos
                    </p>
                  </button>

                  <button
                    type="button"
                    onClick={() => setFormData({ ...formData, rol: 'PROVEEDOR' })}
                    className={`p-4 border-2 rounded-lg transition-all ${
                      formData.rol === 'PROVEEDOR'
                        ? 'border-green-600 bg-green-50'
                        : 'border-gray-300 hover:border-green-300'
                    }`}
                  >
                    <Briefcase className={`mx-auto mb-2 ${formData.rol === 'PROVEEDOR' ? 'text-green-600' : 'text-gray-400'}`} size={32} />
                    <h3 className="font-semibold text-gray-900">Proveedor</h3>
                    <p className="text-sm text-gray-600 mt-1">
                      Ofrece servicios deportivos
                    </p>
                  </button>
                </div>
                {errors.rol && (
                  <p className="mt-2 text-sm text-red-600">{errors.rol}</p>
                )}
              </div>

              {/* Campos específicos para CLIENTE */}
              {formData.rol === 'CLIENTE' && (
                <div className="space-y-4 bg-blue-50 p-4 rounded-lg border border-blue-200">
                  <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                    <Heart size={20} className="text-blue-600" />
                    Información de Cliente
                  </h3>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Deportes de Interés
                    </label>
                    <select
                      name="preferenciaDeportes"
                      value={formData.preferenciaDeportes}
                      onChange={handleChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="">Selecciona un deporte</option>
                      {deportesPopulares.map(deporte => (
                        <option key={deporte} value={deporte}>{deporte}</option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Nivel de Experiencia
                    </label>
                    <div className="relative">
                      <TrendingUp className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                      <select
                        name="nivelExperiencia"
                        value={formData.nivelExperiencia}
                        onChange={handleChange}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                      >
                        <option value="">Selecciona tu nivel</option>
                        {nivelesExperiencia.map(nivel => (
                          <option key={nivel} value={nivel}>{nivel}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Fecha de Nacimiento
                    </label>
                    <div className="relative">
                      <Calendar className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                      <input
                        type="date"
                        name="fechaNacimiento"
                        value={formData.fechaNacimiento}
                        onChange={handleChange}
                        className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    {errors.fechaNacimiento && (
                      <p className="mt-1 text-sm text-red-600">{errors.fechaNacimiento}</p>
                    )}
                  </div>
                </div>
              )}

              {/* Campos específicos para PROVEEDOR */}
              {formData.rol === 'PROVEEDOR' && (
                <div className="space-y-4 bg-green-50 p-4 rounded-lg border border-green-200">
                  <h3 className="font-semibold text-gray-900 flex items-center gap-2">
                    <Briefcase size={20} className="text-green-600" />
                    Información de Negocio
                  </h3>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Descripción del Negocio
                    </label>
                    <textarea
                      name="descripcionNegocio"
                      value={formData.descripcionNegocio}
                      onChange={handleChange}
                      placeholder="Describe tu negocio, servicios que ofreces, experiencia..."
                      rows={4}
                      maxLength={200}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 resize-none"
                    />
                    <p className="mt-1 text-sm text-gray-500">
                      {formData.descripcionNegocio.length}/200 caracteres
                    </p>
                  </div>
                </div>
              )}

              {/* Botón de Registro */}
              <button
                type="button"
                onClick={handleSubmit}
                disabled={loading}
                className="w-full bg-gradient-to-r from-blue-600 to-green-600 text-white py-3 px-6 rounded-lg hover:from-blue-700 hover:to-green-700 disabled:from-gray-400 disabled:to-gray-400 disabled:cursor-not-allowed font-medium transition-all transform hover:scale-[1.02] active:scale-[0.98]"
              >
                {loading ? 'Registrando...' : 'Crear Cuenta'}
              </button>

              {/* Link a Login */}
              <div className="text-center">
                <p className="text-gray-600">
                  ¿Ya tienes cuenta?{' '}
                  <Link to="/login" className="text-blue-600 hover:text-blue-700 font-medium">
                    Inicia sesión aquí
                  </Link>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;