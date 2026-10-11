import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getBooking } from '../api/client';
import { formatCurrency, formatDate } from '../utils/formatters';
import { Search, AlertCircle, ArrowRight } from 'lucide-react';

export default function PnrStatus() {
  const [pnr, setPnr] = useState('');
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!/^\d{10}$/.test(pnr.trim())) {
      setError('PNR must be exactly 10 digits.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const data = await getBooking(pnr.trim());
      setBooking(data);
    } catch (err) {
      setError(err.message || 'Booking not found for this PNR.');
      setBooking(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '750px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div className="card">
        <h2 style={{ fontSize: '1.35rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--primary)' }}>
          Check PNR Status
        </h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.25rem' }}>
          Enter your 10-digit Passenger Name Record (PNR) number to inspect reservation status and ticket details.
        </p>

        {error && (
          <div className="banner banner-error">
            <AlertCircle size={20} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.75rem' }}>
          <input
            type="text"
            maxLength="10"
            className="form-input"
            placeholder="Enter 10-digit PNR"
            value={pnr}
            onChange={(e) => setPnr(e.target.value.replace(/\D/g, ''))}
            style={{ fontSize: '1.1rem', letterSpacing: '2px', fontWeight: 600 }}
            required
          />
          <button type="submit" className="btn btn-primary" disabled={loading} style={{ padding: '0 1.5rem' }}>
            <Search size={18} />
            {loading ? 'Checking...' : 'Check Status'}
          </button>
        </form>
      </div>

      {booking && (
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '0.75rem' }}>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>PNR: </span>
              <strong style={{ fontSize: '1.1rem', color: 'var(--primary)' }}>{booking.pnr}</strong>
            </div>
            <span className={`badge ${booking.status === 'CONFIRMED' ? 'badge-success' : 'badge-danger'}`}>
              {booking.status}
            </span>
          </div>

          <div className="grid-2">
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TRAIN</div>
              <div style={{ fontWeight: 700 }}>{booking.trainName} ({booking.trainNumber})</div>
            </div>
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>JOURNEY DATE</div>
              <div style={{ fontWeight: 700 }}>{formatDate(booking.journeyDate)}</div>
            </div>
          </div>

          <div style={{ background: '#f8fafc', padding: '0.85rem', borderRadius: '6px', border: '1px solid var(--border)' }}>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>ROUTE</div>
            <div style={{ fontWeight: 600 }}>
              {booking.sourceStationName} → {booking.destinationStationName}
            </div>
          </div>

          <div>
            <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
              PASSENGERS ({booking.passengers?.length || 0})
            </div>
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Age</th>
                    <th>Gender</th>
                    <th>Coach</th>
                    <th>Seat</th>
                    <th>Berth</th>
                  </tr>
                </thead>
                <tbody>
                  {booking.passengers?.map((p, idx) => (
                    <tr key={idx}>
                      <td><strong>{p.name}</strong></td>
                      <td>{p.age}</td>
                      <td>{p.gender}</td>
                      <td><span className="badge badge-info">{p.coachNumber || 'N/A'}</span></td>
                      <td><strong>{p.seatNumber ?? 'N/A'}</strong></td>
                      <td>{p.seatType || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--border)', paddingTop: '0.75rem', marginTop: '0.5rem' }}>
            <div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Total Fare: </span>
              <strong>{formatCurrency(booking.totalFare)}</strong>
            </div>

            <button className="btn btn-outline" onClick={() => navigate(`/ticket/${booking.pnr}`)}>
              View Full Ticket
              <ArrowRight size={16} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
