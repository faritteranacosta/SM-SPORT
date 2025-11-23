import React, { useState } from 'react';
import axios from 'axios';

const initialForm = {
  nombre: '',
  deporte: '',
  descripcion: '',
  precio: '',
  ubicacion: {
    direccion: '',
    ciudad: '',
    departamento: '',
    pais: '',
  },
  disponibilidad: [
    {
      fecha: '',
      horaInicio: '',
      horaFin: '',
      cuposDisponibles: 1,
    },
  ],
};

const ProviderServices = () => {
  const [form, setForm] = useState(initialForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleUbicacionChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      ubicacion: {
        ...prev.ubicacion,
        [name]: value,
      },
    }));
  };

  const handleDisponibilidadChange = (index, e) => {
    const { name, value } = e.target;
    setForm((prev) => {
      const updated = [...prev.disponibilidad];
      updated[index] = {
        ...updated[index],
        [name]: value,
      };
      return { ...prev, disponibilidad: updated };
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      // Adaptar valores de fecha/hora al formato esperado por el backend
      const disponibilidadPayload = form.disponibilidad.map((d) => ({
        fecha: d.fecha, // yyyy-MM-dd (input type="date")
        horaInicio: d.horaInicio, // HH:mm (input type="time")
        horaFin: d.horaFin,
        cuposDisponibles: Number(d.cuposDisponibles),
      }));

      const payload = {
        nombre: form.nombre,
        deporte: form.deporte,
        descripcion: form.descripcion || undefined,
        precio: Number(form.precio),
        ubicacion: {
          direccion: form.ubicacion.direccion,
          ciudad: form.ubicacion.ciudad,
          departamento: form.ubicacion.departamento,
          pais: form.ubicacion.pais,
        },
        disponibilidad: disponibilidadPayload,
      };

      const response = await axios.post('/api/v1/servicios', payload);

      setSuccess(`Servicio creado con ID: ${response.data.idServicio}`);
      setForm(initialForm);
    } catch (err) {
      console.error('Error al crear servicio:', err);
      const message =
        err.response?.data?.message || 'Error al crear el servicio. Revisa los datos.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Publicar nuevo servicio</h2>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label>Nombre del servicio</label>
          <input
            type="text"
            name="nombre"
            value={form.nombre}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Deporte</label>
          <input
            type="text"
            name="deporte"
            value={form.deporte}
            onChange={handleChange}
            required
          />
        </div>

        <div className="form-group">
          <label>Descripción</label>
          <textarea
            name="descripcion"
            value={form.descripcion}
            onChange={handleChange}
            rows={3}
          />
        </div>

        <div className="form-group">
          <label>Precio (COP)</label>
          <input
            type="number"
            name="precio"
            value={form.precio}
            onChange={handleChange}
            required
            min="0.01"
            step="0.01"
          />
        </div>

        <h3>Ubicación</h3>
        <div className="form-group">
          <label>Dirección</label>
          <input
            type="text"
            name="direccion"
            value={form.ubicacion.direccion}
            onChange={handleUbicacionChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Ciudad</label>
          <input
            type="text"
            name="ciudad"
            value={form.ubicacion.ciudad}
            onChange={handleUbicacionChange}
            required
          />
        </div>
        <div className="form-group">
          <label>Departamento</label>
          <input
            type="text"
            name="departamento"
            value={form.ubicacion.departamento}
            onChange={handleUbicacionChange}
            required
          />
        </div>
        <div className="form-group">
          <label>País</label>
          <input
            type="text"
            name="pais"
            value={form.ubicacion.pais}
            onChange={handleUbicacionChange}
            required
          />
        </div>

        <h3>Disponibilidad</h3>
        {form.disponibilidad.map((d, index) => (
          <div key={index} className="form-group">
            <label>Fecha</label>
            <input
              type="date"
              name="fecha"
              value={d.fecha}
              onChange={(e) => handleDisponibilidadChange(index, e)}
              required
            />
            <label>Hora inicio</label>
            <input
              type="time"
              name="horaInicio"
              value={d.horaInicio}
              onChange={(e) => handleDisponibilidadChange(index, e)}
              required
            />
            <label>Hora fin</label>
            <input
              type="time"
              name="horaFin"
              value={d.horaFin}
              onChange={(e) => handleDisponibilidadChange(index, e)}
              required
            />
            <label>Cupos disponibles</label>
            <input
              type="number"
              name="cuposDisponibles"
              value={d.cuposDisponibles}
              min="1"
              max="100"
              onChange={(e) => handleDisponibilidadChange(index, e)}
              required
            />
          </div>
        ))}

        <button type="submit" disabled={loading}>
          {loading ? 'Publicando...' : 'Publicar servicio'}
        </button>
      </form>
    </div>
  );
};

export default ProviderServices;
