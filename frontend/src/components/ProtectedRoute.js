import React from 'react';
import { Navigate } from 'react-router-dom';

const isLoggedIn = () => !!(localStorage.getItem('userId') || sessionStorage.getItem('userId'));

const ProtectedRoute = ({ children }) => {
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

export default ProtectedRoute;

