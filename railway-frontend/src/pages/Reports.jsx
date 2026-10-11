import React, { useState, useEffect } from 'react';
import { getSummaryReport, getRevenueReport, getPopularTrains, getOccupancyReport } from '../api/client';
import { formatCurrency, formatDate } from '../utils/formatters';
import { TrendingUp, Users, Train, CheckCircle2, AlertOctagon, RotateCcw } from 'lucide-react';

export default function Reports() {
  const [summary, setSummary] = useState(null);
  const [revenue, setRevenue] = useState([]);
  const [popularTrains, setPopularTrains] = useState([]);
  const [occupancy, setOccupancy] = useState([]);
  const [occupancyDate, setOccupancyDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadReports = () => {
    setLoading(true);
    setError('');
    Promise.all([
      getSummaryReport(),
      getRevenueReport(),
      getPopularTrains(5),
      getOccupancyReport(occupancyDate)
    ])
      .then(([sum, rev, pop, occ]) => {
        setSummary(sum);
        setRevenue(rev);
        setPopularTrains(pop);
        setOccupancy(occ);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadReports();
  }, [occupancyDate]);

  if (loading && !summary) {
    return <div style={{ textAlign: 'center', padding: '3rem' }}>Computing real-time analytics...</div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary)' }}>Business Reports & Analytics</h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          Real-time metrics, revenue ledger, and train occupancy computed via optimized raw SQL aggregations.
        </p>
      </div>

      {error && <div className="banner banner-error">{error}</div>}

      {/* Summary KPI Cards */}
      {summary && (
        <div className="grid-4">
          <div className="card" style={{ borderLeft: '4px solid var(--primary)' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>NET REALIZED REVENUE</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--primary)', marginTop: '0.25rem' }}>
              {formatCurrency(summary.netRevenue)}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Gross: {formatCurrency(summary.grossRevenue)} • Refunds: {formatCurrency(summary.totalRefunds)}
            </div>
          </div>

          <div className="card" style={{ borderLeft: '4px solid var(--success)' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>CONFIRMED BOOKINGS</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--success)', marginTop: '0.25rem' }}>
              {summary.confirmedBookings}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Cancelled: {summary.cancelledBookings} • Failed: {summary.failedBookings}
            </div>
          </div>

          <div className="card" style={{ borderLeft: '4px solid var(--accent)' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TOTAL PASSENGERS</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent)', marginTop: '0.25rem' }}>
              {summary.totalPassengers}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Across all booked journeys
            </div>
          </div>

          <div className="card" style={{ borderLeft: '4px solid #6366f1' }}>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>ACTIVE NETWORK</div>
            <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#4f46e5', marginTop: '0.25rem' }}>
              {summary.totalActiveTrains} Trains
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
              Across {summary.totalStations} Stations
            </div>
          </div>
        </div>
      )}

      {/* Revenue Ledger Chart / Table */}
      <div className="card">
        <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <TrendingUp size={20} color="var(--primary)" />
          Daily Revenue Ledger (Last 30 Days)
        </h3>
        <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
          Net revenue = Gross Successful Payments minus Cancelled Ticket Refunds.
        </p>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Booking Date</th>
                <th>Gross Payments</th>
                <th>Refunds Deducted</th>
                <th>Net Revenue</th>
              </tr>
            </thead>
            <tbody>
              {revenue.length === 0 ? (
                <tr><td colSpan="4" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No payment transactions found.</td></tr>
              ) : (
                revenue.map((row) => (
                  <tr key={row.day}>
                    <td><strong>{formatDate(row.day)}</strong></td>
                    <td style={{ color: 'var(--success)' }}>{formatCurrency(row.gross)}</td>
                    <td style={{ color: 'var(--danger)' }}>{formatCurrency(row.refunds)}</td>
                    <td style={{ fontWeight: 700, color: 'var(--primary)' }}>{formatCurrency(row.net)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Popular Trains & Occupancy Grid */}
      <div className="grid-2">
        <div className="card">
          <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Train size={20} color="var(--primary)" />
            Top 5 Popular Trains
          </h3>
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Train</th>
                  <th>Confirmed Bookings</th>
                  <th>Total Revenue</th>
                </tr>
              </thead>
              <tbody>
                {popularTrains.map((t) => (
                  <tr key={t.trainId}>
                    <td>
                      <strong>{t.trainName}</strong>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>#{t.trainNumber}</div>
                    </td>
                    <td><span className="badge badge-info">{t.bookingCount} bookings</span></td>
                    <td><strong>{formatCurrency(t.totalRevenue)}</strong></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Users size={20} color="var(--primary)" />
              Seat Occupancy %
            </h3>
            <input
              type="date"
              className="form-input"
              style={{ padding: '0.25rem 0.5rem', fontSize: '0.85rem' }}
              value={occupancyDate}
              onChange={(e) => setOccupancyDate(e.target.value)}
            />
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Train</th>
                  <th>Class</th>
                  <th>Reserved / Total</th>
                  <th>Occupancy</th>
                </tr>
              </thead>
              <tbody>
                {occupancy.slice(0, 8).map((row, idx) => (
                  <tr key={idx}>
                    <td><strong>{row.trainName}</strong></td>
                    <td><span className="badge badge-info">{row.classType}</span></td>
                    <td>{row.reservedSeats} / {row.totalSeats}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <div style={{
                          flex: 1, background: '#e2e8f0', height: '6px', borderRadius: '3px', overflow: 'hidden'
                        }}>
                          <div style={{
                            width: `${Math.min(row.occupancyPercentage, 100)}%`,
                            background: row.occupancyPercentage > 80 ? 'var(--danger)' : 'var(--success)',
                            height: '100%'
                          }} />
                        </div>
                        <span style={{ fontSize: '0.8rem', fontWeight: 700 }}>{row.occupancyPercentage}%</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
