import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

const Register = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    email: '',
    password: '',
    confirmPassword: '',
    tipoUsuario: 'CLIENTE' // Por defecto como CLIENTE
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (formData.password !== formData.confirmPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }
    
    setError('');
    setLoading(true);
    
    try {
      // Eliminamos confirmPassword ya que no lo necesita el backend
      const { confirmPassword, ...rest } = formData;

      // Construimos el payload que espera el backend
      const payload = {
        nombre: rest.nombre,
        correo: rest.email,
        contrasena: rest.password,
        rol: rest.tipoUsuario,
        // Campos opcionales: puedes luego exponerlos en el formulario si los necesitas
        telefono: rest.telefono,
        direccion: rest.direccion,
        preferenciaDeportes: rest.preferenciaDeportes,
        nivelExperiencia: rest.nivelExperiencia,
        fechaNacimiento: rest.fechaNacimiento,
        descripcionNegocio: rest.descripcionNegocio,
      };

      await axios.post('/api/v1/auth/registro', payload);
      
      // Redirigir a login después de registro exitoso
      navigate('/login', { state: { success: '¡Registro exitoso! Por favor inicia sesión.' } });
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Error al registrar el usuario';
      setError(errorMessage);
      console.error('Error al registrar:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Registro de Usuario</h2>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label>Nombre</label>
          <input
            type="text"
            name="nombre"
            value={formData.nombre}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Apellido</label>
          <input
            type="text"
            name="apellido"
            value={formData.apellido}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Correo Electrónico</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Contraseña</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            minLength="6"
          />
          <small>Mínimo 6 caracteres, incluyendo mayúsculas, minúsculas y números</small>
        </div>
        <div className="form-group">
          <label>Confirmar Contraseña</label>
          <input
            type="password"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Tipo de Usuario</label>
          <select 
            name="tipoUsuario" 
            value={formData.tipoUsuario}
            onChange={handleChange}
            className="form-control"
          >
            <option value="CLIENTE">Cliente</option>
            <option value="PROVEEDOR">Proveedor</option>
          </select>
        </div>
        <button type="submit" disabled={loading}>
          {loading ? 'Registrando...' : 'Registrarse'}
        </button>
      </form>
      <p>
        ¿Ya tienes una cuenta? <Link to="/login">Inicia sesión aquí</Link>
      </p>
    </div>
  );
};

export default Register;
