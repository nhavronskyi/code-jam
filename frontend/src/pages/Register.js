import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AuthForm.css';

const Register = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      if (!res.ok) throw new Error('Registration failed');
      // After signup, fetch profile to get userId
      const profileRes = await fetch('/api/user/profile', { credentials: 'include' });
      if (profileRes.ok) {
        const profile = await profileRes.json();
        localStorage.setItem('userId', profile.id);
      }
      navigate('/');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="auth-form-container">
      <form onSubmit={handleSubmit} style={{width: '100%'}}>
        <h2>Register</h2>
        <input value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" autoFocus />
        <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" />
        <button type="submit">Register</button>
        {error && <div className="error">{error}</div>}
      </form>
    </div>
  );
};

export default Register;
