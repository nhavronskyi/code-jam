import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance, { setNavigate } from '../axiosInstance';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

export default function Home() {
  const [vehicles, setVehicles] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedVehicle, setSelectedVehicle] = useState('all');
  const [selectedPeriod, setSelectedPeriod] = useState('30');
  const isLoggedIn = !!localStorage.getItem('userId');
  const navigate = useNavigate();

  useEffect(() => {
    setNavigate(navigate);
    if (!isLoggedIn) {
      setLoading(false);
      return;
    }
    setLoading(true);
    let params = {};
    if (selectedVehicle !== 'all') params.vehicleId = selectedVehicle;
    const now = new Date();
    let periodDays = selectedPeriod === 'YTD' ? 365 : parseInt(selectedPeriod, 10);
    let periodStart = selectedPeriod === 'YTD'
      ? new Date(now.getFullYear(), 0, 1)
      : new Date(now.getTime() - periodDays * 24 * 60 * 60 * 1000);
    params.startDate = periodStart.toISOString().slice(0, 10);
    params.endDate = now.toISOString().slice(0, 10);
    Promise.all([
      axiosInstance.get('/api/vehicles').then(res => res.data),
      axiosInstance.get('/api/fuel-entries/dashboard', { params }).then(res => res.data)
    ])
      .then(([vehiclesData, dashboardData]) => {
        setVehicles(vehiclesData);
        setDashboard(dashboardData);
        setLoading(false);
      })
      .catch(err => {
        setError('Failed to load dashboard info');
        setLoading(false);
      });
  }, [isLoggedIn, navigate, selectedVehicle, selectedPeriod]);

  return (
    <div style={{background: '#fff', borderRadius: '12px', boxShadow: '0 2px 12px rgba(0,0,0,0.07)', padding: '2rem', maxWidth: '900px', margin: '2rem auto', textAlign: 'center'}}>
      {/* Selectors */}
      {isLoggedIn && (
        <div style={{display: 'flex', gap: '2rem', justifyContent: 'center', marginBottom: '2rem', flexWrap: 'wrap'}}>
          <div>
            <label htmlFor="vehicle-select"><strong>Vehicle:</strong> </label>
            <select id="vehicle-select" value={selectedVehicle} onChange={e => setSelectedVehicle(e.target.value)}>
              <option value="all">All vehicles</option>
              {vehicles.map(v => (
                <option key={v.id} value={v.id}>{v.name || `${v.make} ${v.model}`}</option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="period-select"><strong>Period:</strong> </label>
            <select id="period-select" value={selectedPeriod} onChange={e => setSelectedPeriod(e.target.value)}>
              <option value="30">Last 30 days</option>
              <option value="90">Last 90 days</option>
              <option value="YTD">Year to date</option>
            </select>
          </div>
        </div>
      )}
      {/* Stats cards */}
      {isLoggedIn && !loading && dashboard && (
        <div style={{background: '#f4f8fb', borderRadius: '8px', padding: '1.2rem 1rem', marginBottom: '2rem', display: 'flex', gap: '2.5rem', flexWrap: 'wrap', justifyContent: 'center'}}>
          <div><strong>{dashboard.avgCostPerLiter != null ? dashboard.avgCostPerLiter.toFixed(2) : '--'}</strong><br/><span style={{color: '#888'}}>Avg Cost/L</span></div>
          <div><strong>{dashboard.avgConsumption != null ? dashboard.avgConsumption.toFixed(2) : '--'}</strong><br/><span style={{color: '#888'}}>Avg L/100km</span></div>
          <div><strong>{dashboard.totalSpend != null ? dashboard.totalSpend.toFixed(2) : '--'}</strong><br/><span style={{color: '#888'}}>Total Spend</span></div>
          <div><strong>{dashboard.totalDistance != null ? dashboard.totalDistance : '--'}</strong><br/><span style={{color: '#888'}}>Total Distance</span></div>
          <div><strong>{dashboard.avgCostPerKm != null ? dashboard.avgCostPerKm.toFixed(2) : '--'}</strong><br/><span style={{color: '#888'}}>Avg Cost/km</span></div>
          <div><strong>{dashboard.avgDistancePerDay != null ? dashboard.avgDistancePerDay.toFixed(2) : '--'}</strong><br/><span style={{color: '#888'}}>Avg Distance/day</span></div>
        </div>
      )}
      {/* Charts */}
      {isLoggedIn && !loading && dashboard && (
        <div style={{display: 'flex', gap: '2rem', flexWrap: 'wrap', justifyContent: 'center', marginBottom: '2rem'}}>
          <div style={{width: '400px', height: '250px', background: '#f8f9fa', borderRadius: '8px', padding: '1rem'}}>
            <h4 style={{marginBottom: '0.5rem'}}>Cost per Liter Over Time</h4>
            <ResponsiveContainer width="100%" height={180}>
              <LineChart data={dashboard.costPerLiterData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="value" stroke="#2196f3" dot={false} />
              </LineChart>
            </ResponsiveContainer>
            {(!dashboard.costPerLiterData || dashboard.costPerLiterData.length === 0) && <div style={{color: '#888'}}>No data for selected period/vehicle.</div>}
          </div>
          <div style={{width: '400px', height: '250px', background: '#f8f9fa', borderRadius: '8px', padding: '1rem'}}>
            <h4 style={{marginBottom: '0.5rem'}}>Consumption Over Time</h4>
            <ResponsiveContainer width="100%" height={180}>
              <LineChart data={dashboard.consumptionData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="value" stroke="#4caf50" dot={false} />
              </LineChart>
            </ResponsiveContainer>
            {(!dashboard.consumptionData || dashboard.consumptionData.length === 0) && <div style={{color: '#888'}}>No data for selected period/vehicle.</div>}
          </div>
        </div>
      )}
      {loading && <div style={{margin: '2rem 0'}}>Loading your dashboard...</div>}
      {error && <div style={{color: 'red', margin: '1rem 0'}}>{error}</div>}
      {!loading && !isLoggedIn && (
        <div style={{marginTop: '2rem', color: '#555'}}>
          <strong>Sign in or register</strong> to start tracking your vehicles and fuel entries!
        </div>
      )}
    </div>
  );
}
