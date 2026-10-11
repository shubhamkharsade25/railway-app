import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getStations } from '../api/client';
import { ArrowLeftRight, Search, Calendar, Users, Train } from 'lucide-react';

export default function Home() {
  const navigate = useNavigate();
  const [stations, setStations] = useState([]);
  const [source, setSource] = useState('');
  const [destination, setDestination] = useState('');
  const [date, setDate] = useState(() => {
    const today = new Date();
    today.setDate(today.getDate() + 1);
    return today.toISOString().split('T')[0];
  });
  const [passengers, setPassengers] = useState(1);
  const [classType, setClassType] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getStations()
      .then((data) => {
        setStations(data);
        if (data.length >= 2) {
          setSource(String(data[0].stationId));
          setDestination(String(data[1].stationId));
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const handleSwap = () => {
    setSource(destination);
    setDestination(source);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    if (!source || !destination) {
      setError('Please select both source and destination stations.');
      return;
    }
    if (source === destination) {
      setError('Source and destination stations cannot be the same.');
      return;
    }
    if (!date) {
      setError('Please choose a valid journey date.');
      return;
    }

    setError('');
    let query = `/results?source=${source}&destination=${destination}&date=${date}&passengers=${passengers}`;
    if (classType) query += `&classType=${classType}`;
    navigate(query);
  };

  const minDate = new Date().toISOString().split('T')[0];
  const maxDate = new Date(Date.now() + 120 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      {/* Hero section */}
      <div style={{
        background: 'linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%)',
        color: 'white',
        padding: '3rem 2rem',
        borderRadius: '12px',
        textAlign: 'center',
        boxShadow: '0 10px 15px -3px rgba(30, 58, 138, 0.2)'
      }}>
        <h1 style={{ fontSize: '2.25rem', fontWeight: 800, marginBottom: '0.75rem', letterSpacing: '-0.025em' }}>
          Seamless Train Bookings & Real-Time Availability
        </h1>
        <p style={{ fontSize: '1.1rem', color: '#bfdbfe', maxWidth: '650px', margin: '0 auto' }}>
          Explore routes, inspect seat reservations, and manage tickets with raw SQL performance and ACID guarantees.
        </p>
      </div>

      {/* Search card */}
      <div className="card" style={{ maxWidth: '850px', margin: '-1rem auto 0', width: '100%' }}>
        {error && (
          <div className="banner banner-error">
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSearch}>
          <div className="grid-2" style={{ alignItems: 'flex-end', marginBottom: '1rem' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Source Station</label>
              <select
                className="form-select"
                value={source}
                onChange={(e) => setSource(e.target.value)}
                disabled={loading}
              >
                {stations.map((st) => (
                  <option key={st.stationId} value={st.stationId}>
                    {st.stationName} ({st.stationCode})
                  </option>
                ))}
              </select>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <button
                type="button"
                className="btn btn-outline"
                onClick={handleSwap}
                title="Swap source and destination"
                style={{ height: '42px', padding: '0 0.85rem' }}
              >
                <ArrowLeftRight size={18} />
              </button>

              <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                <label className="form-label">Destination Station</label>
                <select
                  className="form-select"
                  value={destination}
                  onChange={(e) => setDestination(e.target.value)}
                  disabled={loading}
                >
                  {stations.map((st) => (
                    <option key={st.stationId} value={st.stationId}>
                      {st.stationName} ({st.stationCode})
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <div className="grid-3" style={{ marginBottom: '1.5rem' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Journey Date</label>
              <input
                type="date"
                className="form-input"
                value={date}
                min={minDate}
                max={maxDate}
                onChange={(e) => setDate(e.target.value)}
                required
              />
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Passengers (1–6)</label>
              <select
                className="form-select"
                value={passengers}
                onChange={(e) => setPassengers(Number(e.target.value))}
              >
                {[1, 2, 3, 4, 5, 6].map((num) => (
                  <option key={num} value={num}>{num} {num === 1 ? 'Passenger' : 'Passengers'}</option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Class Filter (Optional)</label>
              <select
                className="form-select"
                value={classType}
                onChange={(e) => setClassType(e.target.value)}
              >
                <option value="">All Classes</option>
                <option value="SL">Sleeper (SL)</option>
                <option value="3A">AC 3 Tier (3A)</option>
                <option value="2A">AC 2 Tier (2A)</option>
                <option value="1A">AC First Class (1A)</option>
                <option value="CC">AC Chair Car (CC)</option>
              </select>
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-accent"
            style={{ width: '100%', fontSize: '1.05rem', padding: '0.75rem' }}
            disabled={loading}
          >
            <Search size={20} />
            Search Trains
          </button>
        </form>
      </div>
    </div>
  );
}
