import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

const APP_NAME = 'Code Jam';

const Navbar = () => {
  const navigate = useNavigate();
  const isLoggedIn = !!(localStorage.getItem('userId') || sessionStorage.getItem('userId'));

  const handleLogout = () => {
    localStorage.clear();
    sessionStorage.clear();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-links">
        {isLoggedIn ? (
          <>
            <Link to="/">Home</Link>
            <Link to="/fuel-entries">Fuel Entries</Link>
            <Link to="/vehicles">Vehicles</Link>
            <Link to="/statistics">Statistics</Link>
            <Link to="/profile">Profile</Link>
            <Link to="/settings">Settings</Link>
          </>
        ) : (
          <span className="app-name">{APP_NAME}</span>
        )}
      </div>
      <div className="navbar-actions">
        {!isLoggedIn && (
          <>
            <button className="nav-btn" onClick={() => navigate('/login')}>Login</button>
            <button className="nav-btn" onClick={() => navigate('/register')}>Register</button>
          </>
        )}
        {isLoggedIn && (
          <button className="nav-btn logout" onClick={handleLogout}>Logout</button>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
