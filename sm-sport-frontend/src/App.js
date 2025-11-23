import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import axios from 'axios';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import ProviderServices from './components/ProviderServices';
import './App.css';

// Configuración de axios para incluir el token en las peticiones
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    if (token && user) {
      // Aquí podrías verificar la validez del token con el backend
      setIsAuthenticated(true);
    }
    setLoading(false);
  }, []);

  const handleLogin = (authData) => {
    // authData viene del backend como AuthResponse
    // { token, refreshToken, tipo, expiresIn, idUsuario, nombre, correo, rol }
    if (authData?.token) {
      localStorage.setItem('token', authData.token);
    }

    const userData = {
      idUsuario: authData.idUsuario,
      nombre: authData.nombre,
      correo: authData.correo,
      rol: authData.rol,
    };

    localStorage.setItem('user', JSON.stringify(userData));
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsAuthenticated(false);
  };

  if (loading) {
    return <div>Cargando...</div>;
  }

  return (
    <div className="App">
      <Routes>
        <Route 
          path="/login" 
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" />
            ) : (
              <Login onLogin={handleLogin} />
            )
          } 
        />
        <Route
          path="/mis-servicios"
          element={
            isAuthenticated ? (
              <ProviderServices />
            ) : (
              <Navigate to="/login" />
            )
          }
        />
        <Route 
          path="/register" 
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" />
            ) : (
              <Register />
            )
          } 
        />
        <Route 
          path="/dashboard" 
          element={
            isAuthenticated ? (
              <Dashboard onLogout={handleLogout} />
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
        <Route 
          path="/" 
          element={
            isAuthenticated ? (
              <Navigate to="/dashboard" />
            ) : (
              <Navigate to="/login" />
            )
          } 
        />
      </Routes>
    </div>
  );
}

export default App;
