import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance, { setNavigate } from '../axiosInstance';

export default function Home() {
  const [vehicles, setVehicles] = useState([]);
  const [fuelEntries, setFuelEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const isLoggedIn = !!localStorage.getItem('userId');
  const navigate = useNavigate();

  useEffect(() => {
    setNavigate(navigate);
    if (!isLoggedIn) {
      setLoading(false);
      return;
    }
    setLoading(true);
    Promise.all([
      axiosInstance.get('/api/vehicles').then(res => res.data),
      axiosInstance.get('/api/fuel-entries/history').then(res => res.data)
    ])
      .then(([vehiclesData, fuelEntriesData]) => {
        setVehicles(vehiclesData);
        setFuelEntries(Array.isArray(fuelEntriesData?.content) ? fuelEntriesData.content : []);
        setLoading(false);
      })
      .catch(err => {
        setError('Failed to load dashboard info');
        setLoading(false);
      });
  }, [isLoggedIn, navigate]);

  // Create a lookup for vehicleId to vehicle name
  const vehicleNameMap = vehicles.reduce((map, v) => {
    map[v.id] = v.name || `${v.make} ${v.model}`;
    return map;
  }, {});

  // Basic stats calculations
  const totalVehicles = vehicles.length;
  const totalFuelEntries = fuelEntries.length;
  const totalFuel = fuelEntries.reduce((sum, entry) => sum + (entry.liters || 0), 0);
  const totalSpent = fuelEntries.reduce((sum, entry) => sum + (entry.totalAmount || 0), 0);
  // Average consumption (L/100km) if odometer and liters available
  let avgConsumption = null;
  const validEntries = fuelEntries.filter(e => e.liters && e.odometer);
  if (validEntries.length > 1) {
    let totalDistance = 0;
    let totalLiters = 0;
    for (let i = 1; i < validEntries.length; i++) {
      const dist = validEntries[i-1].odometer - validEntries[i].odometer;
      if (dist > 0) {
        totalDistance += dist;
        totalLiters += validEntries[i].liters;
      }
    }
    if (totalDistance > 0) {
      avgConsumption = ((totalLiters / totalDistance) * 100).toFixed(2);
    }
  }

  return (
    <div style={{
      background: '#fff',
      borderRadius: '12px',
      boxShadow: '0 2px 12px rgba(0,0,0,0.07)',
      padding: '2rem',
      maxWidth: '600px',
      margin: '2rem auto',
      textAlign: 'center'
    }}>
      {loading && <div style={{margin: '2rem 0'}}>Loading your dashboard...</div>}
      {error && <div style={{color: 'red', margin: '1rem 0'}}>{error}</div>}
      {!loading && isLoggedIn && (
        <>
          <div style={{background: '#f4f8fb', borderRadius: '8px', padding: '1.2rem 1rem', marginBottom: '2rem', display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
            <h2 style={{marginTop: '0', marginBottom: '1rem'}}>Your Stats</h2>
            <div style={{display: 'flex', gap: '2.5rem', flexWrap: 'wrap', justifyContent: 'center'}}>
              <div><strong>{totalVehicles}</strong><br/><span style={{color: '#888'}}>Vehicles</span></div>
              <div><strong>{totalFuelEntries}</strong><br/><span style={{color: '#888'}}>Fuel Entries</span></div>
              <div><strong>{totalFuel.toFixed(2)}</strong><br/><span style={{color: '#888'}}>Total Liters</span></div>
              <div><strong>{totalSpent.toFixed(2)}</strong><br/><span style={{color: '#888'}}>Total Spent</span></div>
              {avgConsumption && <div><strong>{avgConsumption}</strong><br/><span style={{color: '#888'}}>Avg L/100km</span></div>}
            </div>
          </div>
          <h2 style={{marginTop: '2rem', marginBottom: '1rem'}}>Your Vehicles</h2>
          {vehicles.length === 0 ? (
            <div style={{color: '#888'}}>No vehicles found. Add one to get started!</div>
          ) : (
            <ul style={{listStyle: 'none', padding: 0, marginBottom: '2rem'}}>
              {vehicles.map(v => (
                <li key={v.id} style={{marginBottom: '0.7rem', fontWeight: 500}}>
                  {v.name} <span style={{color: '#888'}}>({v.make} {v.model}, {v.year}, {v.fuelType})</span>
                </li>
              ))}
            </ul>
          )}
          <h2 style={{marginBottom: '1rem'}}>Recent Fuel Entries</h2>
          {fuelEntries.length === 0 ? (
            <div style={{color: '#888'}}>No recent fuel entries found.</div>
          ) : (
            <ul style={{listStyle: 'none', padding: 0}}>
              {fuelEntries.slice(0,5).map(entry => (
                <li key={entry.id} style={{marginBottom: '0.7rem'}}>
                  {entry.date}: {entry.liters}L, {entry.totalAmount} for {vehicleNameMap[entry.vehicleId] || `vehicle ${entry.vehicleId}`}
                </li>
              ))}
            </ul>
          )}
        </>
      )}
      {!loading && !isLoggedIn && (
        <div style={{marginTop: '2rem', color: '#555'}}>
          <strong>Sign in or register</strong> to start tracking your vehicles and fuel entries!
        </div>
      )}
    </div>
  );
}
