import React from 'react';
import { Link, useNavigate } from 'react-router-dom';

const Navbar = () => {
  const navigate = useNavigate();
  const isLoggedIn = !!(localStorage.getItem('userId') || sessionStorage.getItem('userId'));

  const handleLogout = () => {
    localStorage.clear();
    sessionStorage.clear();
    navigate('/login');
  };

  return (
    <nav>
      <Link to="/">Home</Link> |
      {!isLoggedIn && <><Link to="/login">Login</Link> | <Link to="/register">Register</Link> |</>}
      <Link to="/fuel-entries">Fuel Entries</Link> |
      <Link to="/vehicles">Vehicles</Link> |
      <Link to="/statistics">Statistics</Link> |
      <Link to="/profile">Profile</Link> |
      <Link to="/settings">Settings</Link> |
      <Link to="/legal">Legal</Link> |
      <Link to="/terms">Terms</Link> |
      {isLoggedIn && <><span style={{marginLeft: '10px', color: 'green'}}>Logged in</span><button onClick={handleLogout} style={{marginLeft: '10px'}}>Logout</button></>}
    </nav>
  );
};

export default Navbar;
