import React, { useEffect, useState } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from "recharts";
import axiosInstance from '../axiosInstance';

const Statistics = () => {
  const [perFill, setPerFill] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState('all');
  const [selectedPeriod, setSelectedPeriod] = useState('30');
  const [comparison, setComparison] = useState([]);
  const [loadingComparison, setLoadingComparison] = useState(true);
  const [errorComparison, setErrorComparison] = useState(null);
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
    // Fetch perFill for selected vehicle/period
    let params = {};
    if (selectedVehicle !== 'all') params.vehicleId = selectedVehicle;
    const now = new Date();
    let periodDays = selectedPeriod === 'YTD' ? 365 : parseInt(selectedPeriod, 10);
    let periodStart = selectedPeriod === 'YTD'
      ? new Date(now.getFullYear(), 0, 1)
      : new Date(now.getTime() - periodDays * 24 * 60 * 60 * 1000);
    params.startDate = periodStart.toISOString().slice(0, 10);
    params.endDate = now.toISOString().slice(0, 10);
    axiosInstance.get('/api/fuel-entries/per-fill', { params })
      .then(res => setPerFill(Array.isArray(res.data) ? res.data : []))
      .catch(() => setPerFill([]));
  }, [selectedVehicle, selectedPeriod]);

  useEffect(() => {
    setLoadingComparison(true);
    let params = {};
    if (selectedVehicle !== 'all') params.vehicleId = selectedVehicle;
    const now = new Date();
    let periodDays = selectedPeriod === 'YTD' ? 365 : parseInt(selectedPeriod, 10);
    let periodStart = selectedPeriod === 'YTD'
      ? new Date(now.getFullYear(), 0, 1)
      : new Date(now.getTime() - periodDays * 24 * 60 * 60 * 1000);
    params.startDate = periodStart.toISOString().slice(0, 10);
    params.endDate = now.toISOString().slice(0, 10);
    axiosInstance.get('/api/fuel-entries/brand-grade-comparison', { params })
      .then(res => setComparison(Array.isArray(res.data) ? res.data : []))
      .catch(() => setErrorComparison('Failed to load brand/grade comparison'))
      .finally(() => setLoadingComparison(false));
  }, [selectedVehicle, selectedPeriod]);

  useEffect(() => {
    axiosInstance.get('/api/vehicles')
      .then(res => setVehicles(Array.isArray(res.data) ? res.data : []))
      .catch(() => setVehicles([]));
  }, []);

  return (
    <div style={{ padding: "2rem" }}>
      <h2>Statistics & Graphs</h2>
      <h3>Fuel Consumption Over Time</h3>
      {perFill.length === 0 ? (
        <div style={{ color: '#888' }}>No data for selected period/vehicle.</div>
      ) : (
        <LineChart width={600} height={300} data={perFill} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="date" />
          <YAxis label={{ value: "L/100km", angle: -90, position: "insideLeft" }} />
          <Tooltip />
          <Legend />
          <Line type="monotone" dataKey="consumptionLPer100km" stroke="#8884d8" name="Consumption (L/100km)" />
        </LineChart>
      )}

      <h3>Brand/Grade Comparison</h3>
      <div style={{ marginBottom: '1rem' }}>
        <label><strong>Vehicle:</strong> </label>
        <select value={selectedVehicle} onChange={e => setSelectedVehicle(e.target.value)}>
          <option value="all">All vehicles</option>
          {vehicles.map(v => (
            <option key={v.id} value={v.id}>{v.name || `${v.make} ${v.model}`}</option>
          ))}
        </select>
        <label style={{ marginLeft: '2rem' }}><strong>Period:</strong> </label>
        <select value={selectedPeriod} onChange={e => setSelectedPeriod(e.target.value)}>
          <option value="30">Last 30 days</option>
          <option value="90">Last 90 days</option>
          <option value="YTD">Year to date</option>
        </select>
      </div>
      {loadingComparison ? (
        <div>Loading comparison...</div>
      ) : errorComparison ? (
        <div style={{ color: 'red' }}>{errorComparison}</div>
      ) : comparison.length === 0 ? (
        <div style={{ color: '#888' }}>No data for selected period/vehicle.</div>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '2rem' }}>
          <thead>
            <tr style={{ background: '#f4f8fb' }}>
              <th style={{ padding: '0.5rem', border: '1px solid #ddd' }}>Brand</th>
              <th style={{ padding: '0.5rem', border: '1px solid #ddd' }}>Grade</th>
              <th style={{ padding: '0.5rem', border: '1px solid #ddd' }}>Avg Cost/L</th>
              <th style={{ padding: '0.5rem', border: '1px solid #ddd' }}>Avg Consumption</th>
              <th style={{ padding: '0.5rem', border: '1px solid #ddd' }}>Fill-ups</th>
            </tr>
          </thead>
          <tbody>
            {comparison.map((row, idx) => (
              <tr key={idx}>
                <td style={{ padding: '0.5rem', border: '1px solid #ddd' }}>{row.brand}</td>
                <td style={{ padding: '0.5rem', border: '1px solid #ddd' }}>{row.grade}</td>
                <td style={{ padding: '0.5rem', border: '1px solid #ddd' }}>{row.avgCostPerLiter != null ? row.avgCostPerLiter.toFixed(2) : '--'}</td>
                <td style={{ padding: '0.5rem', border: '1px solid #ddd' }}>{row.avgConsumption != null ? row.avgConsumption.toFixed(2) : '--'}</td>
                <td style={{ padding: '0.5rem', border: '1px solid #ddd' }}>{row.fillUpCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default Statistics;
