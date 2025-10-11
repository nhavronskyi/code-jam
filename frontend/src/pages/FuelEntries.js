import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance, { setNavigate } from '../axiosInstance';
import './FuelEntries.css';

const getToday = () => new Date().toISOString().slice(0, 10);

const FuelEntries = () => {
  const [vehicles, setVehicles] = useState([]);
  const [fuelEntries, setFuelEntries] = useState([]);
  const [selectedVehicle, setSelectedVehicle] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({
    date: getToday(),
    odometer: '',
    stationName: '',
    fuelBrand: '',
    fuelGrade: '',
    liters: '',
    totalAmount: '',
    notes: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    setNavigate(navigate);
    const fetchVehicles = async () => {
      try {
        const response = await axiosInstance.get('/api/vehicles');
        setVehicles(response.data);
        setLoading(false);
      } catch (err) {
        setError(err);
        setLoading(false);
      }
    };
    fetchVehicles();
  }, [navigate]);

  useEffect(() => {
    const fetchFuelEntries = async () => {
      if (!selectedVehicle) return;
      setLoading(true);
      try {
        const response = await axiosInstance.get(`/api/fuel-entries/vehicle/${selectedVehicle}`);
        setFuelEntries(response.data);
        setLoading(false);
      } catch (err) {
        setError(err);
        setLoading(false);
      }
    };
    fetchFuelEntries();
  }, [selectedVehicle, navigate]);

  const handleVehicleChange = (e) => {
    setSelectedVehicle(e.target.value);
  };

  const handleFormChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleAddEntry = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await axiosInstance.post('/api/fuel-entries', { ...form, vehicleId: selectedVehicle });
      setForm({
        date: getToday(),
        odometer: '',
        stationName: '',
        fuelBrand: '',
        fuelGrade: '',
        liters: '',
        totalAmount: '',
        notes: ''
      });
      // Refresh entries
      const entriesRes = await axiosInstance.get(`/api/fuel-entries/vehicle/${selectedVehicle}`);
      setFuelEntries(entriesRes.data);
    } catch (err) {
      setError(err);
    }
    setSubmitting(false);
  };

  const handleDeleteEntry = async (id) => {
    if (!window.confirm('Delete this entry?')) return;
    try {
      await axiosInstance.delete(`/api/fuel-entries/${id}`);
      setFuelEntries(fuelEntries.filter(entry => entry.id !== id));
    } catch (err) {
      setError(err);
    }
  };

  // Error alert component
  const ErrorAlert = ({ message, onClose }) => (
    <div style={{
      background: '#ffeaea',
      color: '#b71c1c',
      border: '1px solid #f5c2c7',
      borderRadius: '8px',
      padding: '0.8rem 1.2rem',
      marginBottom: '1.2rem',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      fontWeight: 500,
      boxShadow: '0 2px 8px rgba(0,0,0,0.04)'
    }}>
      <span>{message}</span>
      <button onClick={onClose} style={{
        background: 'none',
        border: 'none',
        color: '#b71c1c',
        fontWeight: 'bold',
        fontSize: '1.2rem',
        cursor: 'pointer',
        marginLeft: '1rem'
      }} aria-label="Close">×</button>
    </div>
  );

  if (loading) return <div>Loading...</div>;

  return (
    <div className="fuel-entries-container">
      <div className="fuel-entries-header">
        <h1>Fuel Entries</h1>
        <p>View and manage fuel entries for your vehicles.</p>
      </div>

      {error && <ErrorAlert message={typeof error === 'string' ? error : error?.error || 'An error occurred'} onClose={() => setError(null)} />}

      <label htmlFor="vehicle-select">Select a vehicle: </label>
      <select
        id="vehicle-select"
        className="fuel-entries-select"
        value={selectedVehicle}
        onChange={handleVehicleChange}
      >
        <option value="">--Please choose an option--</option>
        {vehicles.map((vehicle) => (
          <option key={vehicle.id} value={vehicle.id}>
            {vehicle.make} {vehicle.model} ({vehicle.year})
          </option>
        ))}
      </select>

      {selectedVehicle && (
        <form onSubmit={handleAddEntry} className="fuel-entries-form">
          <h2 style={{flexBasis: '100%'}}>Add Fuel Entry</h2>
          <input type="date" name="date" value={form.date} onChange={handleFormChange} required />
          <input type="number" name="odometer" value={form.odometer} onChange={handleFormChange} placeholder="Odometer" required />
          <input type="text" name="stationName" value={form.stationName} onChange={handleFormChange} placeholder="Station Name" />
          <input type="text" name="fuelBrand" value={form.fuelBrand} onChange={handleFormChange} placeholder="Fuel Brand" />
          <input type="text" name="fuelGrade" value={form.fuelGrade} onChange={handleFormChange} placeholder="Fuel Grade" />
          <input type="number" step="0.01" name="liters" value={form.liters} onChange={handleFormChange} placeholder="Liters" required />
          <input type="number" step="0.01" name="totalAmount" value={form.totalAmount} onChange={handleFormChange} placeholder="Total Amount" required />
          <input type="text" name="notes" value={form.notes} onChange={handleFormChange} placeholder="Notes" />
          <button type="submit" disabled={submitting}>Add Entry</button>
        </form>
      )}

      {selectedVehicle && (
        <div style={{marginTop: '2rem'}}>
          <h2>Entries</h2>
          {fuelEntries.length > 0 ? (
            <table className="fuel-entries-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Odometer</th>
                  <th>Station</th>
                  <th>Brand</th>
                  <th>Grade</th>
                  <th>Liters</th>
                  <th>Total Amount</th>
                  <th>Notes</th>
                  <th>Delete</th>
                </tr>
              </thead>
              <tbody>
                {fuelEntries.map((entry) => (
                  <tr key={entry.id}>
                    <td>{entry.date}</td>
                    <td>{entry.odometer}</td>
                    <td>{entry.stationName}</td>
                    <td>{entry.fuelBrand}</td>
                    <td>{entry.fuelGrade}</td>
                    <td>{entry.liters}</td>
                    <td>{entry.totalAmount}</td>
                    <td>{entry.notes}</td>
                    <td><button onClick={() => handleDeleteEntry(entry.id)}>Delete</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p>No fuel entries found for this vehicle.</p>
          )}
        </div>
      )}
    </div>
  );
};

export default FuelEntries;
