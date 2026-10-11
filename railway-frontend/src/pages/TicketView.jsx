import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTicket, cancelBooking } from '../api/client';
import { formatCurrency, formatDate, formatTime } from '../utils/formatters';
import { Printer, XCircle, AlertCircle, CheckCircle, Train } from 'lucide-react';

export default function TicketView() {
  const { pnr } = useParams();
  const navigate = useNavigate();

  const [ticket, setTicket] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancelReason, setCancelReason] = useState('');
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [cancelResult, setCancelResult] = useState(null);

  const loadTicket = () => {
    setLoading(true);
    getTicket(pnr)
      .then((data) => setTicket(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadTicket();
  }, [pnr]);

  const handlePrint = () => {
    window.print();
  };

  const handleCancel = async (e) => {
    e.preventDefault();
    if (!cancelReason.trim()) return;

    setCancelling(true);
    setError('');

    try {
      const res = await cancelBooking(pnr, { reason: cancelReason.trim() });
      setCancelResult(res);
      setShowCancelModal(false);
      loadTicket();
    } catch (err) {
      setError(err.message);
    } finally {
      setCancelling(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: '3rem' }}>Loading ticket details...</div>;

  if (error && !ticket) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '2rem' }}>
        <p style={{ color: 'var(--danger)', fontWeight: 600 }}>{error}</p>
        <button className="btn btn-outline" style={{ marginTop: '1rem' }} onClick={() => navigate('/pnr')}>
          Check Another PNR
        </button>
      </div>
    );
  }

  const isConfirmed = ticket.bookingStatus === 'CONFIRMED';

  return (
    <div style={{ maxWidth: '850px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Top action bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }} className="no-print">
        <button className="btn btn-outline" onClick={() => navigate('/')}>
          ← Back to Home
        </button>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {isConfirmed && (
            <button className="btn btn-danger" onClick={() => setShowCancelModal(true)}>
              <XCircle size={18} />
              Cancel Ticket
            </button>
          )}
          <button className="btn btn-primary" onClick={handlePrint}>
            <Printer size={18} />
            Print Ticket
          </button>
        </div>
      </div>

      {cancelResult && (
        <div className="banner banner-success no-print">
          <CheckCircle size={20} />
          <span>
            Ticket cancelled successfully. Refund amount: <strong>{formatCurrency(cancelResult.refundAmount)}</strong>.
          </span>
        </div>
      )}

      {/* Ticket document */}
      <div className="card" style={{ border: '2px solid #cbd5e1', padding: '2rem', background: 'white' }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '2px dashed var(--border)', paddingBottom: '1.25rem', marginBottom: '1.25rem' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary)', fontWeight: 800, fontSize: '1.35rem' }}>
              <Train size={24} />
              <span>INDIAN RAILWAYS E-TICKET</span>
            </div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Electronic Reservation Slip (ERS) • Academic Project Replica
            </div>
          </div>

          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>PNR NUMBER</div>
            <div style={{ fontSize: '1.5rem', fontWeight: 800, letterSpacing: '2px', color: 'var(--primary)' }}>
              {ticket.pnr}
            </div>
            <span className={`badge ${isConfirmed ? 'badge-success' : 'badge-danger'}`} style={{ marginTop: '0.25rem' }}>
              {ticket.bookingStatus}
            </span>
          </div>
        </div>

        {/* Train & Journey info */}
        <div className="grid-2" style={{ marginBottom: '1.5rem', gap: '1.5rem' }}>
          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>TRAIN DETAILS</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>
              {ticket.train?.trainName} ({ticket.train?.trainNumber})
            </div>
            <div style={{ fontSize: '0.9rem', color: '#475569' }}>
              Class: <strong>{ticket.classType}</strong> • Type: {ticket.train?.trainType}
            </div>
          </div>

          <div>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>JOURNEY DATE</div>
            <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>
              {formatDate(ticket.journeyDate)}
            </div>
            <div style={{ fontSize: '0.9rem', color: '#475569' }}>
              Booked on: {formatDate(ticket.bookedAt)}
            </div>
          </div>
        </div>

        {/* Route itinerary */}
        <div style={{ background: '#f8fafc', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border)', marginBottom: '1.5rem' }}>
          <div className="grid-2">
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>BOARDING FROM</div>
              <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>{ticket.source?.stationName}</div>
              <div style={{ fontSize: '0.9rem', color: 'var(--primary)' }}>
                Departure: <strong>{formatTime(ticket.source?.departureOrArrivalTime)}</strong>
              </div>
            </div>

            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>DESTINATION</div>
              <div style={{ fontWeight: 700, fontSize: '1.05rem' }}>{ticket.destination?.stationName}</div>
              <div style={{ fontSize: '0.9rem', color: 'var(--primary)' }}>
                Arrival: <strong>{formatTime(ticket.destination?.departureOrArrivalTime)}</strong>
              </div>
            </div>
          </div>
        </div>

        {/* Passenger table */}
        <div style={{ marginBottom: '1.5rem' }}>
          <div style={{ fontSize: '0.85rem', fontWeight: 700, color: '#334155', marginBottom: '0.5rem' }}>
            PASSENGER & SEAT ASSIGNMENT
          </div>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Passenger Name</th>
                  <th>Age</th>
                  <th>Gender</th>
                  <th>Coach</th>
                  <th>Seat Number</th>
                  <th>Berth Type</th>
                </tr>
              </thead>
              <tbody>
                {ticket.passengers?.map((p, idx) => (
                  <tr key={idx}>
                    <td>{idx + 1}</td>
                    <td><strong>{p.name}</strong></td>
                    <td>{p.age}</td>
                    <td>{p.gender}</td>
                    <td><span className="badge badge-info">{p.coachNumber || 'WL'}</span></td>
                    <td><strong>{p.seatNumber ?? 'WL'}</strong></td>
                    <td>{p.seatType || 'N/A'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Payment & Contact summary */}
        <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '2px dashed var(--border)', paddingTop: '1.25rem', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>CONTACT DETAILS</div>
            <div style={{ fontSize: '0.9rem' }}>{ticket.contactEmail} • {ticket.contactPhone}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Payment Method: {ticket.payment?.paymentMethod || 'UPI'} ({ticket.payment?.paymentStatus})
            </div>
          </div>

          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>TOTAL FARE PAID</div>
            <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--primary)' }}>
              {formatCurrency(ticket.totalFare)}
            </div>
          </div>
        </div>
      </div>

      {/* Cancellation Modal */}
      {showCancelModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="card" style={{ maxWidth: '450px', width: '90%' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.5rem', color: 'var(--danger)' }}>
              Confirm Cancellation
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              Refund rules: &gt;48h = 90%, 12–48h = 50%, &lt;12h = 0%. Once cancelled, your seats are immediately released.
            </p>

            <form onSubmit={handleCancel}>
              <div className="form-group">
                <label className="form-label">Cancellation Reason</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Change of schedule, personal reason"
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
                <button type="button" className="btn btn-outline" onClick={() => setShowCancelModal(false)}>
                  Keep Ticket
                </button>
                <button type="submit" className="btn btn-danger" disabled={cancelling}>
                  {cancelling ? 'Cancelling...' : 'Confirm Cancellation'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
