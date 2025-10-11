import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AuthForm.css';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await fetch('/api/auth/signin', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      if (!res.ok) throw new Error('Login failed');
      const data = await res.json();
      // Assume backend returns userId in response, otherwise fetch profile
      if (data.userId) {
        localStorage.setItem('userId', data.userId);
      } else {
        // fallback: fetch profile to get userId
        const profileRes = await fetch('/api/user/profile', { credentials: 'include' });
        if (profileRes.ok) {
          const profile = await profileRes.json();
          localStorage.setItem('userId', profile.id);
        }
      }
      navigate('/');
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="auth-form-container">
      <form onSubmit={handleSubmit} style={{width: '100%'}}>
        <h2>Login</h2>
        <input value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" autoFocus />
        <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Password" />
        <button type="submit">Login</button>
        {error && <div className="error">{error}</div>}
      </form>
    </div>
  );
}
