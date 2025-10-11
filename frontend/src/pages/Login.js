import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance, { setNavigate } from '../axiosInstance';
import './AuthForm.css';

export default function Login() {
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  setNavigate(navigate);

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await axiosInstance.post('/api/auth/signin', form);
      const data = res.data;
      if (data.userId) {
        localStorage.setItem('userId', data.userId);
        navigate('/');
      } else {
        // fallback: fetch profile to get userId
        const profileRes = await axiosInstance.get('/api/user/profile');
        localStorage.setItem('userId', profileRes.data.id);
        navigate('/');
      }
    } catch (err) {
      setError('Login failed');
    }
    setLoading(false);
  };

  return (
    <div className="auth-form-container">
      <form onSubmit={handleSubmit} className="auth-form">
        <h2>Login</h2>
        <input
          type="email"
          name="email"
          value={form.email}
          onChange={handleChange}
          placeholder="Email"
          required
        />
        <input
          type="password"
          name="password"
          value={form.password}
          onChange={handleChange}
          placeholder="Password"
          required
        />
        <button type="submit" disabled={loading}>Login</button>
        {error && <div className="error">{error}</div>}
      </form>
    </div>
  );
}
