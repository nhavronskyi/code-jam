import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

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
    <div>
      <h1>Vehicles</h1>
      <p>View and manage your vehicles.</p>
      {loading ? <p>Loading...</p> : null}
      {error ? <p style={{ color: 'red' }}>{error}</p> : null}
      <ul>
        {vehicles.map(vehicle => (
          <li key={vehicle.id}>
            <strong>{vehicle.name}</strong> | {vehicle.make} {vehicle.model} ({vehicle.year}) | Fuel: {vehicle.fuelType}
            <button onClick={() => handleDeleteVehicle(vehicle.id)} style={{ marginLeft: '10px' }}>Delete</button>
          </li>
        ))}
      </ul>
      <h2>Add Vehicle</h2>
      <form onSubmit={handleAddVehicle}>
        <input
          type="text"
          placeholder="Name"
          value={newVehicle.name}
          onChange={e => setNewVehicle({ ...newVehicle, name: e.target.value })}
          required
        />
        <input
          type="text"
          placeholder="Make"
          value={newVehicle.make}
          onChange={e => setNewVehicle({ ...newVehicle, make: e.target.value })}
          required
        />
        <input
          type="text"
          placeholder="Model"
          value={newVehicle.model}
          onChange={e => setNewVehicle({ ...newVehicle, model: e.target.value })}
          required
        />
        <input
          type="number"
          placeholder="Year"
          value={newVehicle.year}
          onChange={e => setNewVehicle({ ...newVehicle, year: e.target.value })}
          required
        />
        <input
          type="text"
          placeholder="Fuel Type"
          value={newVehicle.fuelType}
          onChange={e => setNewVehicle({ ...newVehicle, fuelType: e.target.value })}
          required
        />
        <button type="submit" disabled={adding}>Add</button>
      </form>
    </div>
  );
};

export default Vehicles;
