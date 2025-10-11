import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './AuthForm.css';

const Vehicles = () => {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [newVehicle, setNewVehicle] = useState({ name: '', make: '', model: '', year: '', fuelType: '' });
  const [adding, setAdding] = useState(false);
  const navigate = useNavigate();

  const isLoggedIn = !!localStorage.getItem('userId');

  // Fetch vehicles on mount
  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login');
      return;
    }
    setLoading(true);
    fetch('/api/vehicles', { credentials: 'include' })
      .then(res => {
        if (!res.ok) throw new Error('Failed to fetch vehicles');
        return res.json();
      })
      .then(data => {
        setVehicles(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, [isLoggedIn, navigate]);

  // Add vehicle handler
  const handleAddVehicle = (e) => {
    e.preventDefault();
    setAdding(true);
    setError(null);
    fetch('/api/vehicles', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify(newVehicle)
    })
      .then(res => {
        if (!res.ok) throw new Error('Failed to add vehicle');
        return res.json();
      })
      .then(vehicle => {
        setVehicles([...vehicles, vehicle]);
        setNewVehicle({ name: '', make: '', model: '', year: '', fuelType: '' });
        setAdding(false);
      })
      .catch(err => {
        setError(err.message);
        setAdding(false);
      });
  };

  // Delete vehicle handler
  const handleDeleteVehicle = (id) => {
    setError(null);
    fetch(`/api/vehicles/${id}`, {
      method: 'DELETE',
      credentials: 'include'
    })
      .then(res => {
        if (!res.ok) throw new Error('Failed to delete vehicle');
        setVehicles(vehicles.filter(v => v.id !== id));
      })
      .catch(err => setError(err.message));
  };

  if (!isLoggedIn) {
    return <div>Please log in to view your vehicles.</div>;
  }

  return (
    <div className="auth-form-container" style={{maxWidth: '700px'}}>
      <h2 style={{marginBottom: '2rem'}}>Vehicles</h2>
      <h3 style={{marginBottom: '1rem'}}>Add Vehicle</h3>
      <form onSubmit={handleAddVehicle} style={{display: 'flex', flexWrap: 'wrap', gap: '1rem', marginBottom: '2rem'}}>
        <input
          type="text"
          placeholder="Name"
          value={newVehicle.name}
          onChange={e => setNewVehicle({ ...newVehicle, name: e.target.value })}
          required
          style={{flex: '1 1 120px', minWidth: '120px'}}
        />
        <input
          type="text"
          placeholder="Make"
          value={newVehicle.make}
          onChange={e => setNewVehicle({ ...newVehicle, make: e.target.value })}
          required
          style={{flex: '1 1 120px', minWidth: '120px'}}
        />
        <input
          type="text"
          placeholder="Model"
          value={newVehicle.model}
          onChange={e => setNewVehicle({ ...newVehicle, model: e.target.value })}
          required
          style={{flex: '1 1 120px', minWidth: '120px'}}
        />
        <input
          type="number"
          placeholder="Year"
          value={newVehicle.year}
          onChange={e => setNewVehicle({ ...newVehicle, year: e.target.value })}
          required
          style={{flex: '1 1 90px', minWidth: '90px'}}
        />
        <input
          type="text"
          placeholder="Fuel Type"
          value={newVehicle.fuelType}
          onChange={e => setNewVehicle({ ...newVehicle, fuelType: e.target.value })}
          required
          style={{flex: '1 1 120px', minWidth: '120px'}}
        />
        <button type="submit" disabled={adding} style={{background: '#61dafb', color: '#222', border: 'none', borderRadius: '6px', padding: '0.6rem 1.2rem', fontWeight: 600, cursor: 'pointer', minWidth: '100px'}}>Add</button>
      </form>
      <h3 style={{marginBottom: '1.2rem', marginTop: '2rem', textAlign: 'center'}}>Your Vehicle List</h3>
      {loading && <p>Loading...</p>}
      {error && <div className="error">{error}</div>}
      <div style={{display: 'flex', flexWrap: 'wrap', gap: '1.5rem', marginBottom: '2rem', justifyContent: 'center'}}>
        {vehicles.map(vehicle => (
          <div key={vehicle.id} style={{background: '#f8f9fa', borderRadius: '10px', boxShadow: '0 2px 8px rgba(33,161,243,0.08)', padding: '1.2rem 1.5rem', width: '260px', display: 'flex', flexDirection: 'column'}}>
            <div>
              <div style={{fontWeight: 'bold', fontSize: '1.15rem', marginBottom: '0.5rem', wordBreak: 'break-word'}}>{vehicle.name}</div>
              <div style={{marginBottom: '0.3rem'}}>{vehicle.make} {vehicle.model} <span style={{color: '#888'}}>({vehicle.year})</span></div>
              <div style={{marginBottom: '0.7rem'}}>Fuel: <span style={{fontWeight: 500}}>{vehicle.fuelType}</span></div>
            </div>
            <div style={{display: 'flex', justifyContent: 'flex-end', marginTop: 'auto'}}>
              <button onClick={() => handleDeleteVehicle(vehicle.id)} style={{background: '#e74c3c', color: '#fff', border: 'none', borderRadius: '6px', padding: '0.4rem 1.1rem', cursor: 'pointer', fontWeight: 500}}>Delete</button>
            </div>
          </div>
        ))}
        {vehicles.length === 0 && !loading && <div style={{color: '#888'}}>No vehicles found.</div>}
      </div>
    </div>
  );
};

export default Vehicles;
