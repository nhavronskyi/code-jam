import React from 'react';

export default function Home() {
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
      <h1>Welcome to Code Jam!</h1>
      <p style={{fontSize: '1.25rem', color: '#555', marginBottom: '1.5rem'}}>
        Track your fuel entries, manage vehicles, and view statistics with ease.
      </p>
      <img src="/logo192.png" alt="Code Jam Logo" style={{width: '96px', marginBottom: '1rem'}} />
    </div>
  );
}
