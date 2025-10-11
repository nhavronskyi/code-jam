import React, { useEffect, useState } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, BarChart, Bar, PieChart, Pie, Cell } from "recharts";

const API_BASE = "/api/statistics";

const Statistics = () => {
  const [perFill, setPerFill] = useState([]);
  const [brandStats, setBrandStats] = useState([]);
  const [gradeStats, setGradeStats] = useState([]);
  const [monthlyStats, setMonthlyStats] = useState([]);
  const userId = 1; // TODO: Replace with actual userId from auth context
  const vehicleId = 1; // TODO: Replace with actual vehicleId selection
  const year = new Date().getFullYear();

  useEffect(() => {
    fetch(`${API_BASE}/per-fill?userId=${userId}&vehicleId=${vehicleId}`)
      .then(res => res.json())
      .then(data => setPerFill(Array.isArray(data) ? data : []));
    fetch(`${API_BASE}/brand-grade?userId=${userId}&vehicleId=${vehicleId}`)
      .then(res => res.json())
      .then(data => setBrandStats(Array.isArray(data) ? data : []));
    fetch(`${API_BASE}/grade?userId=${userId}&vehicleId=${vehicleId}`)
      .then(res => res.json())
      .then(data => setGradeStats(Array.isArray(data) ? data : []));
    fetch(`${API_BASE}/monthly?userId=${userId}&vehicleId=${vehicleId}&year=${year}`)
      .then(res => res.json())
      .then(data => {
        setMonthlyStats(
          data && typeof data === 'object'
            ? Object.entries(data).map(([month, stats]) => ({ month, ...stats }))
            : []
        );
      });
  }, [userId, vehicleId, year]);

  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#A28BFE", "#FEA8B0"];

  return (
    <div style={{ padding: "2rem" }}>
      <h2>Statistics & Graphs</h2>
      <h3>Fuel Consumption Over Time</h3>
      <LineChart width={600} height={300} data={perFill} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="date" />
        <YAxis label={{ value: "L/100km", angle: -90, position: "insideLeft" }} />
        <Tooltip />
        <Legend />
        <Line type="monotone" dataKey="consumptionLPer100km" stroke="#8884d8" name="Consumption (L/100km)" />
      </LineChart>

      <h3>Cost Per Fill</h3>
      <BarChart width={600} height={300} data={perFill} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="date" />
        <YAxis label={{ value: "Total Amount", angle: -90, position: "insideLeft" }} />
        <Tooltip />
        <Legend />
        <Bar dataKey="totalAmount" fill="#82ca9d" name="Total Amount" />
      </BarChart>

      <h3>Monthly Fuel Spend</h3>
      <LineChart width={600} height={300} data={monthlyStats} margin={{ top: 20, right: 30, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="month" />
        <YAxis label={{ value: "Total Spend", angle: -90, position: "insideLeft" }} />
        <Tooltip />
        <Legend />
        <Line type="monotone" dataKey="totalSpend" stroke="#ff7300" name="Total Spend" />
      </LineChart>

      <h3>Brand Distribution</h3>
      <PieChart width={400} height={300}>
        <Pie data={Array.isArray(brandStats) ? brandStats : []} dataKey="numFillUps" nameKey="fuelBrand" cx="50%" cy="50%" outerRadius={100} label>
          {(Array.isArray(brandStats) ? brandStats : []).map((entry, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip />
      </PieChart>

      <h3>Grade Distribution</h3>
      <PieChart width={400} height={300}>
        <Pie data={Array.isArray(gradeStats) ? gradeStats : []} dataKey="numFillUps" nameKey="fuelGrade" cx="50%" cy="50%" outerRadius={100} label>
          {(Array.isArray(gradeStats) ? gradeStats : []).map((entry, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip />
      </PieChart>
    </div>
  );
};

export default Statistics;
