import React, { useEffect, useState } from 'react';

export default function Legal() {
  const [content, setContent] = useState('');

  useEffect(() => {
    fetch('/api/legal/terms')
      .then(res => res.text())
      .then(data => {
        setContent(data || 'No legal info available');
      });
  }, []);

  return (
    <div>
      <h2>Legal Information</h2>
      <div>{content}</div>
    </div>
  );
}
