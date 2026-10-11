import React from 'react';

export default function Footer() {
  return (
    <footer className="footer no-print">
      <p>
        <strong>Railway Management System</strong> — Academic DBMS Project demo powered by Spring Boot (Raw JdbcTemplate) & React.
      </p>
      <p style={{ marginTop: '0.25rem', fontSize: '0.8rem' }}>
        Sample fictional data for demonstration purposes only. Not affiliated with Indian Railways.
      </p>
    </footer>
  );
}
