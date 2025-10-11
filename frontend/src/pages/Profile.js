import React, { useEffect, useState } from 'react';
import './AuthForm.css';

const Profile = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

  if (loading) return <div className="auth-form-container"><p>Loading profile...</p></div>;
  if (error) return <div className="auth-form-container"><div className="error">{error}</div></div>;
  if (!profile) return null;

  return (
    <div className="auth-form-container" style={{maxWidth: '500px'}}>
      <h2>Profile Information</h2>
      <div style={{width: '100%', textAlign: 'left'}}>
        <p><strong>User ID:</strong> {profile.id}</p>
        <p><strong>Email:</strong> {profile.email}</p>
        <p><strong>Display Name:</strong> {profile.displayName || 'N/A'}</p>
        <p><strong>Currency:</strong> {profile.currency || 'N/A'}</p>
        <p><strong>Distance Unit:</strong> {profile.distanceUnit || 'N/A'}</p>
        <p><strong>Volume Unit:</strong> {profile.volumeUnit || 'N/A'}</p>
        <p><strong>Time Zone:</strong> {profile.timeZone || 'N/A'}</p>
        <div style={{marginTop: '1.5rem'}}>
          <strong>Vehicles:</strong>
          {profile.vehicles && profile.vehicles.length > 0 ? (
            <ul style={{paddingLeft: '1.2rem'}}>
              {profile.vehicles.map(vehicle => (
                <li key={vehicle.id}>
                  <strong>{vehicle.name}</strong> ({vehicle.year})<br/>
                  Make: {vehicle.make}, Model: {vehicle.model}<br/>
                  Fuel Type: {vehicle.fuelType}
                </li>
              ))}
            </ul>
          ) : (
            <p style={{marginLeft: '1rem'}}>No vehicles found.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Profile;
