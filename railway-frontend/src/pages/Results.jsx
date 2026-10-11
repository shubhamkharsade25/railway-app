import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { searchTrains, getStation } from '../api/client';
import { formatCurrency, formatTime, formatDate } from '../utils/formatters';
import { Train, Clock, ArrowRight, AlertCircle } from 'lucide-react';

export default function Results() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const sourceId = searchParams.get('source');
  const destinationId = searchParams.get('destination');
  const date = searchParams.get('date');
  const passengers = searchParams.get('passengers') || '1';
  const classFilter = searchParams.get('classType') || '';

  const [trains, setTrains] = useState([]);
  const [sourceName, setSourceName] = useState('');
  const [destName, setDestName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sortBy, setSortBy] = useState('departure');

  useEffect(() => {
    if (!sourceId || !destinationId || !date) {
      setError('Missing search parameters');
      setLoading(false);
      return;
    }

    Promise.all([
      getStation(sourceId),
      getStation(destinationId),
      searchTrains({ source: sourceId, destination: destinationId, date, classType: classFilter })
    ])
      .then(([src, dst, trainList]) => {
        setSourceName(`${src.stationName} (${src.stationCode})`);
        setDestName(`${dst.stationName} (${dst.stationCode})`);
        setTrains(trainList);
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, [sourceId, destinationId, date, classFilter]);

  const sortedTrains = [...trains].sort((a, b) => {
    if (sortBy === 'departure') {
      return (a.departureTime || '').localeCompare(b.departureTime || '');
    }
    // Lowest fare among classes
    const minFareA = Math.min(...(a.classAvailability?.map((c) => c.fare) || [9999]));
    const minFareB = Math.min(...(b.classAvailability?.map((c) => c.fare) || [9999]));
    return minFareA - minFareB;
  });

  const handleBook = (train, cls) => {
    navigate('/book', {
      state: {
        trainId: train.trainId,
        trainNumber: train.trainNumber,
        trainName: train.trainName,
        sourceStationId: Number(sourceId),
        destinationStationId: Number(destinationId),
        sourceName,
        destName,
        journeyDate: date,
        classType: cls.classType,
        fare: cls.fare,
        passengers: Number(passengers)
      }
    });
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '3rem' }}>Searching trains across routes...</div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* Route header */}
      <div className="card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '1.25rem', fontWeight: 700 }}>
            <span>{sourceName}</span>
            <ArrowRight size={20} color="var(--primary)" />
            <span>{destName}</span>
          </div>
          <div style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '0.25rem' }}>
            Journey Date: <strong>{formatDate(date)}</strong> • {passengers} Passenger(s)
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Sort by:</label>
          <select
            className="form-select"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            style={{ padding: '0.35rem 0.65rem' }}
          >
            <option value="departure">Departure Time</option>
            <option value="fare">Lowest Fare</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="banner banner-error">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {/* Train list */}
      {sortedTrains.length === 0 && !error ? (
        <div className="card" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
          <p style={{ fontSize: '1.1rem', fontWeight: 600 }}>No active trains found for this route.</p>
          <p style={{ fontSize: '0.9rem', marginTop: '0.5rem' }}>Try swapping stations or picking an alternative date.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {sortedTrains.map((train) => (
            <div key={train.trainId} className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '0.5rem' }}>
                <div>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--primary)' }}>
                    {train.trainName} ({train.trainNumber})
                  </h3>
                  <span className="badge badge-info">{train.trainType}</span>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '1.5rem', textAlign: 'right' }}>
                  <div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{formatTime(train.departureTime)}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Departure</div>
                  </div>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                    <Clock size={14} style={{ verticalAlign: 'middle', marginRight: '3px' }} />
                    {train.durationFormatted} ({train.distanceKm} km)
                  </div>
                  <div>
                    <div style={{ fontSize: '1.1rem', fontWeight: 700 }}>{formatTime(train.arrivalTime)}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Arrival</div>
                  </div>
                </div>
              </div>

              <div style={{ borderTop: '1px solid var(--border)', paddingTop: '0.75rem' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                  AVAILABLE CLASSES & FARES:
                </div>
                <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
                  {train.classAvailability?.map((cls) => {
                    const isAvailable = cls.availableSeats >= Number(passengers);
                    return (
                      <div
                        key={cls.classType}
                        style={{
                          border: '1px solid var(--border)',
                          borderRadius: '8px',
                          padding: '0.65rem 0.85rem',
                          minWidth: '140px',
                          background: isAvailable ? '#f8fafc' : '#fef2f2',
                          display: 'flex',
                          flexDirection: 'column',
                          gap: '0.25rem'
                        }}
                      >
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700 }}>
                          <span>{cls.classType}</span>
                          <span style={{ color: 'var(--primary)' }}>{formatCurrency(cls.fare)}</span>
                        </div>
                        <div style={{ fontSize: '0.8rem', color: isAvailable ? 'var(--success)' : 'var(--danger)' }}>
                          {isAvailable ? `Available: ${cls.availableSeats}` : 'NOT AVAILABLE'}
                        </div>
                        <button
                          type="button"
                          className="btn btn-primary"
                          style={{ marginTop: '0.25rem', padding: '0.35rem 0.5rem', fontSize: '0.8rem' }}
                          disabled={!isAvailable}
                          onClick={() => handleBook(train, cls)}
                        >
                          Book Now
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
