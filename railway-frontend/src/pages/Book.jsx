import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { createBooking } from '../api/client';
import { formatCurrency, formatDate } from '../utils/formatters';
import { CreditCard, User, AlertCircle, CheckCircle } from 'lucide-react';

export default function Book() {
  const { state } = useLocation();
  const navigate = useNavigate();

  if (!state) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '3rem' }}>
        <p>No booking information provided. Please start from the search page.</p>
        <button className="btn btn-primary" style={{ marginTop: '1rem' }} onClick={() => navigate('/')}>
          Go to Search
        </button>
      </div>
    );
  }

  const {
    trainId,
    trainNumber,
    trainName,
    sourceStationId,
    destinationStationId,
    sourceName,
    destName,
    journeyDate,
    classType,
    fare,
    passengers: passengerCount
  } = state;

  const [passengers, setPassengers] = useState(() =>
    Array.from({ length: passengerCount || 1 }, () => ({
      name: '',
      age: '',
      gender: 'M',
      preferredSeatId: null
    }))
  );

  const [contactEmail, setContactEmail] = useState('');
  const [contactPhone, setContactPhone] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('UPI');
  const [simulatedPaymentStatus, setSimulatedPaymentStatus] = useState('SUCCESS');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const totalFare = Number(fare) * passengers.length;

  const handlePassengerChange = (index, field, value) => {
    setPassengers((prev) => {
      const copy = [...prev];
      copy[index] = { ...copy[index], [field]: value };
      return copy;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // Validation
    for (let i = 0; i < passengers.length; i++) {
      const p = passengers[i];
      if (!p.name.trim()) {
        setError(`Please enter name for passenger ${i + 1}`);
        return;
      }
      if (!p.age || p.age < 0 || p.age > 120) {
        setError(`Please enter valid age (0–120) for passenger ${i + 1}`);
        return;
      }
    }

    if (!contactEmail.includes('@')) {
      setError('Please provide a valid contact email address.');
      return;
    }

    if (!/^\d{10}$/.test(contactPhone)) {
      setError('Contact phone must be exactly 10 digits.');
      return;
    }

    setLoading(true);

    const payload = {
      trainId,
      sourceStationId,
      destinationStationId,
      journeyDate,
      classType,
      contactEmail,
      contactPhone,
      paymentMethod,
      simulatedPaymentStatus,
      passengers: passengers.map((p) => ({
        name: p.name.trim(),
        age: Number(p.age),
        gender: p.gender,
        preferredSeatId: null
      }))
    };

    try {
      const result = await createBooking(payload);
      navigate(`/ticket/${result.pnr}`);
    } catch (err) {
      if (err.status === 409) {
        setError('Seats just got booked by another user or are insufficient. Please search again.');
      } else {
        setError(err.message || 'Booking failed.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Train summary banner */}
      <div className="card" style={{ background: '#f8fafc', borderLeft: '4px solid var(--primary)' }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--primary)' }}>
          {trainName} ({trainNumber})
        </h2>
        <div style={{ fontSize: '0.95rem', color: '#475569', marginTop: '0.25rem' }}>
          {sourceName} → {destName} • <strong>{formatDate(journeyDate)}</strong> • Class: <strong>{classType}</strong>
        </div>
      </div>

      {error && (
        <div className="banner banner-error">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        {/* Passenger details */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <User size={20} color="var(--primary)" />
            Passenger Details ({passengers.length})
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {passengers.map((p, idx) => (
              <div key={idx} style={{ padding: '0.85rem', background: '#f8fafc', borderRadius: '8px', border: '1px solid var(--border)' }}>
                <div style={{ fontWeight: 600, fontSize: '0.85rem', marginBottom: '0.5rem', color: 'var(--text-muted)' }}>
                  Passenger {idx + 1}
                </div>
                <div className="grid-3">
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label">Full Name</label>
                    <input
                      type="text"
                      className="form-input"
                      placeholder="e.g. Ramesh Sharma"
                      value={p.name}
                      onChange={(e) => handlePassengerChange(idx, 'name', e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label">Age</label>
                    <input
                      type="number"
                      min="0"
                      max="120"
                      className="form-input"
                      placeholder="e.g. 28"
                      value={p.age}
                      onChange={(e) => handlePassengerChange(idx, 'age', e.target.value)}
                      required
                    />
                  </div>
                  <div className="form-group" style={{ marginBottom: 0 }}>
                    <label className="form-label">Gender</label>
                    <select
                      className="form-select"
                      value={p.gender}
                      onChange={(e) => handlePassengerChange(idx, 'gender', e.target.value)}
                    >
                      <option value="M">Male (M)</option>
                      <option value="F">Female (F)</option>
                      <option value="O">Other (O)</option>
                    </select>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Contact info */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>Contact Information</h3>
          <div className="grid-2">
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Email Address (for ticket)</label>
              <input
                type="email"
                className="form-input"
                placeholder="name@example.com"
                value={contactEmail}
                onChange={(e) => setContactEmail(e.target.value)}
                required
              />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Mobile Number (10 digits)</label>
              <input
                type="tel"
                maxLength="10"
                className="form-input"
                placeholder="9876543210"
                value={contactPhone}
                onChange={(e) => setContactPhone(e.target.value)}
                required
              />
            </div>
          </div>
        </div>

        {/* Simulated payment */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <CreditCard size={20} color="var(--primary)" />
            Payment Simulator
          </h3>

          <div className="grid-2">
            <div className="form-group">
              <label className="form-label">Payment Method</label>
              <select
                className="form-select"
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value)}
              >
                <option value="UPI">UPI</option>
                <option value="CARD">Debit / Credit Card</option>
                <option value="NETBANKING">Net Banking</option>
                <option value="CASH">Cash at Counter</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Simulated Status</label>
              <select
                className="form-select"
                value={simulatedPaymentStatus}
                onChange={(e) => setSimulatedPaymentStatus(e.target.value)}
              >
                <option value="SUCCESS">SUCCESS (Booking Confirmed)</option>
                <option value="FAILED">FAILED (Simulate Payment Failure)</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem', borderTop: '1px solid var(--border)', paddingTop: '1rem' }}>
            <div style={{ fontSize: '1.25rem', fontWeight: 700 }}>
              Total Payable: <span style={{ color: 'var(--primary)' }}>{formatCurrency(totalFare)}</span>
            </div>

            <button
              type="submit"
              className="btn btn-accent"
              style={{ fontSize: '1rem', padding: '0.75rem 1.75rem' }}
              disabled={loading}
            >
              {loading ? 'Processing Transaction...' : 'Confirm & Book'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
