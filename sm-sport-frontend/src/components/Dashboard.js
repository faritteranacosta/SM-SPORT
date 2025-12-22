import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const Dashboard = ({ onLogout }) => {
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    // Cargamos los datos básicos del usuario desde localStorage
    try {
      const storedUser = localStorage.getItem('user');
      const userData = JSON.parse(storedUser || '{}');
      console.log("DEBUG: Dashboard - userData:", userData);
      console.log("DEBUG: Dashboard - rol:", userData?.rol);
      if (storedUser) {
        const parsedUser = JSON.parse(storedUser);
        setUserData(parsedUser);
      } else {
        setError('No se encontraron datos de usuario. Inicia sesión nuevamente.');
      }
    } catch (err) {
      setError('Error al leer los datos del usuario');
      console.error('Error al parsear user de localStorage:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleLogout = () => {
    onLogout();
    navigate('/login');
  };

  if (loading) {
    return <div>Cargando datos del usuario...</div>;
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Bienvenido a SM Sport</h1>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
          <button 
            onClick={() => navigate('/perfil')}
            style={{
              padding: '0.5rem 1rem',
              background: 'rgba(255, 107, 0, 0.2)',
              border: '1px solid #FF6B00',
              borderRadius: '8px',
              color: 'white',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: '500',
              transition: 'all 0.2s'
            }}
            onMouseOver={(e) => {
              e.target.style.background = 'rgba(255, 107, 0, 0.3)';
              e.target.style.borderColor = '#ff8533';
            }}
            onMouseOut={(e) => {
              e.target.style.background = 'rgba(255, 107, 0, 0.2)';
              e.target.style.borderColor = '#FF6B00';
            }}
          >
            Perfil
          </button>
          <button onClick={handleLogout} className="logout-button">
            Cerrar Sesión
          </button>
        </div>
      </header>

      <main className="dashboard-content">
        {error && <div className="error-message">{error}</div>}
        
        {userData && (
          <div className="user-profile">
            <h2>Tu Perfil</h2>
            <p><strong>Nombre:</strong> {userData.nombre}</p>
            <p><strong>Correo:</strong> {userData.correo}</p>
            <p><strong>Rol:</strong> {userData.rol}</p>
            {/* Agrega más información del usuario según sea necesario */}
          </div>
        )}

        <div className="dashboard-actions">
          <h3>Acciones Rápidas</h3>
          <div className="action-buttons">
            {userData?.rol === 'PROVEEDOR' ? (
              // Panel para PROVEEDOR
              <>
                <button 
                  className="action-button"
                  onClick={() => navigate('/mis-servicios?tab=reservas')}
                >
                   Ver Mis Reservas
                </button>
                <button 
                  className="action-button"
                  onClick={() => navigate('/servicios')}
                >
                   Explorar Otros Servicios
                </button>
              </>
            ) : userData?.rol === 'ADMINISTRADOR' ? (
              // Panel para ADMINISTRADOR
              <>
                <button 
                  className="action-button"
                  onClick={() => navigate('/servicios')}
                >
                   Ver Servicios Creados
                </button>
              </>
            ) : (
              // Panel para CLIENTE
              <>
                <button 
                  className="action-button"
                  onClick={() => navigate('/reservas')}
                >
                   Ver Mis Reservas
                </button>
                <button 
                  className="action-button"
                  onClick={() => navigate('/servicios')}
                >
                   Explorar Servicios
                </button>
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
