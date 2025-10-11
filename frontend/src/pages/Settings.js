import React, { useEffect, useState } from 'react';
import './AuthForm.css';

const CURRENCIES = ['USD', 'EUR', 'GBP', 'PLN', 'JPY'];
const DISTANCE_UNITS = ['km', 'mi'];
const VOLUME_UNITS = ['L', 'gal'];
const TIME_ZONES = ['UTC', 'Europe/Warsaw', 'America/New_York', 'Asia/Tokyo'];

export default function Settings() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    async function fetchProfile() {
      try {
        const res = await fetch('/api/user/profile', { credentials: 'include' });
        if (!res.ok) throw new Error('Failed to fetch profile');
        const data = await res.json();
        setProfile(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    fetchProfile();
  }, []);

  const handleChange = e => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
    setSuccess('');
    setError('');
  };

  const handleSubmit = async e => {
    e.preventDefault();
    setSuccess('');
    setError('');
    try {
      const res = await fetch('/api/user/profile', {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          currency: profile.currency,
          distanceUnit: profile.distanceUnit,
          volumeUnit: profile.volumeUnit,
          timeZone: profile.timeZone
        })
      });
      if (!res.ok) throw new Error('Failed to update settings');
      setSuccess('Settings updated successfully!');
    } catch (err) {
      setError(err.message);
    }
  };

  if (loading) return <div className="auth-form-container"><p>Loading settings...</p></div>;
  if (error) return <div className="auth-form-container"><div className="error">{error}</div></div>;
  if (!profile) return null;

  return (
    <div className="auth-form-container" style={{maxWidth: '500px'}}>
      <h2 style={{marginBottom: '2rem'}}>Settings</h2>
      <form onSubmit={handleSubmit} style={{width: '100%'}}>
        <div style={{display: 'flex', flexDirection: 'column', gap: '1.2rem'}}>
          <label style={{fontWeight: 500, color: '#222'}}>
            Currency
            <select name="currency" value={profile.currency || ''} onChange={handleChange} className="auth-select">
              <option value="">Select currency</option>
              {CURRENCIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </label>
          <label style={{fontWeight: 500, color: '#222'}}>
            Distance Unit
            <select name="distanceUnit" value={profile.distanceUnit || ''} onChange={handleChange} className="auth-select">
              <option value="">Select unit</option>
              {DISTANCE_UNITS.map(u => <option key={u} value={u}>{u}</option>)}
            </select>
          </label>
          <label style={{fontWeight: 500, color: '#222'}}>
            Volume Unit
            <select name="volumeUnit" value={profile.volumeUnit || ''} onChange={handleChange} className="auth-select">
              <option value="">Select unit</option>
              {VOLUME_UNITS.map(u => <option key={u} value={u}>{u}</option>)}
            </select>
          </label>
          <label style={{fontWeight: 500, color: '#222'}}>
            Time Zone
            <select name="timeZone" value={profile.timeZone || ''} onChange={handleChange} className="auth-select">
              <option value="">Select time zone</option>
              {TIME_ZONES.map(z => <option key={z} value={z}>{z}</option>)}
            </select>
          </label>
        </div>
        <button type="submit" style={{marginTop: '2rem'}}>Save Settings</button>
        {success && <div style={{color: 'green', marginTop: '1rem', textAlign: 'center'}}>{success}</div>}
        {error && <div className="error">{error}</div>}
      </form>
    </div>
  );
}
