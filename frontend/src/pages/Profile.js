import React, { useEffect, useState } from 'react';
import './AuthForm.css';
import axiosInstance from '../axiosInstance';
import { useNavigate } from 'react-router-dom';

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchProfile() {
      try {
        const res = await axiosInstance.get('/api/user/profile', { withCredentials: true });
        setProfile(res.data);
      } catch (err) {
        setError(err.response?.data?.message || err.message);
      } finally {
        setLoading(false);
      }
    }
    fetchProfile();
  }, []);

  if (loading) return <div className="auth-form-container"><p>Loading profile...</p></div>;
  if (error) return <div className="auth-form-container"><div className="error">{error}</div></div>;
  if (!profile) return null;

  return (
    <div className="auth-form-container" style={{maxWidth: '500px', padding: '1rem'}}>
      <h2>Profile Information</h2>
      <button style={{float: 'right', marginBottom: '1rem'}} onClick={() => navigate('/settings')}>Edit profile</button>
      <div style={{width: '100%', textAlign: 'left'}}>
        <p><strong>User ID:</strong> {profile.id}</p>
        <p><strong>Email:</strong> {profile.email}</p>
        <p><strong>Display Name:</strong> {profile.displayName || <span style={{color:'#888'}}>Not set</span>}</p>
        <p><strong>Unit system:</strong> {profile.unitSystem === 'imperial' ? 'Imperial' : 'Metric'}</p>
        <div style={{marginTop: '1.5rem'}}>
          <strong>Vehicles:</strong>
          {profile.vehicles && profile.vehicles.length > 0 ? (
            <ul style={{paddingLeft: '1.2rem'}}>
              {profile.vehicles.map(vehicle => (
                <li key={vehicle.id} style={{marginBottom: '1rem'}}>
                  <strong>{vehicle.name || <span style={{color:'#888'}}>No name</span>}</strong> {vehicle.year ? `(${vehicle.year})` : ''}<br/>
                  Make: {vehicle.make || <span style={{color:'#888'}}>Not set</span>}, Model: {vehicle.model || <span style={{color:'#888'}}>Not set</span>}<br/>
                  Fuel Type: {vehicle.fuelType || <span style={{color:'#888'}}>Not set</span>}
                </li>
              ))}
            </ul>
          ) : (
            <div style={{marginLeft: '1rem'}}>
              <p>No vehicles found.</p>
              <button onClick={() => navigate('/vehicles/add')}>Add vehicle</button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
