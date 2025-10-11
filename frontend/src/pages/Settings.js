import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance, { setNavigate } from '../axiosInstance';
import './AuthForm.css';

const CURRENCIES = ['USD', 'EUR', 'GBP', 'PLN', 'JPY'];
const DISTANCE_UNITS = ['km', 'mi'];
const VOLUME_UNITS = ['L', 'gal'];
const TIME_ZONES = ['UTC', 'Europe/Warsaw', 'America/New_York', 'Asia/Tokyo'];

export default function Settings() {
  const [profile, setProfile] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);
  const navigate = useNavigate();

  setNavigate(navigate);

  useEffect(() => {
    async function fetchProfile() {
      try {
        const res = await axiosInstance.get('/api/user/profile');
        setProfile(res.data);
        setLoading(false);
      } catch (err) {
        setError('Failed to fetch profile');
        setLoading(false);
      }
    }
    fetchProfile();
  }, [navigate]);

  const handleChange = e => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSave = async e => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await axiosInstance.put('/api/user/profile', profile);
      setSaving(false);
    } catch (err) {
      setError('Failed to save profile');
      setSaving(false);
    }
  };

  if (loading) return <div className="auth-form-container"><p>Loading settings...</p></div>;
  if (error) return <div className="auth-form-container"><div className="error">{error}</div></div>;

  return (
    <div className="auth-form-container" style={{maxWidth: '500px'}}>
      <h2 style={{marginBottom: '2rem'}}>Settings</h2>
      <form onSubmit={handleSave} style={{width: '100%'}}>
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
        <button type="submit" style={{marginTop: '2rem'}} disabled={saving}>
          {saving ? 'Saving...' : 'Save Settings'}
        </button>
      </form>
    </div>
  );
}
