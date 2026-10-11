import React, { useState, useEffect } from 'react';
import {
  getStations, createStation, updateStation, deleteStation,
  getTrains, createTrain, updateTrain, deleteTrain,
  getTrainRoute, addRouteStop, deleteRouteStop, addCoach,
  getAllBookings, updateBooking, deleteBooking
} from '../api/client';
import { formatCurrency, formatDate } from '../utils/formatters';
import { Plus, Edit2, Trash2, Route, Layers, AlertCircle, CheckCircle } from 'lucide-react';

export default function Admin() {
  const [tab, setTab] = useState('trains'); // 'trains' | 'stations' | 'routes' | 'bookings'

  // Data states
  const [trains, setTrains] = useState([]);
  const [stations, setStations] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  // Modals & forms
  const [stationForm, setStationForm] = useState({ stationName: '', stationCode: '', city: '' });
  const [editingStationId, setEditingStationId] = useState(null);

  const [trainForm, setTrainForm] = useState({ trainNumber: '', trainName: '', trainType: 'EXPRESS', isActive: true });
  const [editingTrainId, setEditingTrainId] = useState(null);

  // Booking edit modal state
  const [editingBookingPnr, setEditingBookingPnr] = useState(null);
  const [bookingForm, setBookingForm] = useState({ status: 'CONFIRMED', contactEmail: '', contactPhone: '' });

  // Route editor states
  const [selectedTrainId, setSelectedTrainId] = useState('');
  const [routeStops, setRouteStops] = useState([]);
  const [stopForm, setStopForm] = useState({ stationId: '', stopNo: 1, arrivalTime: '', departureTime: '', distanceKm: 0 });

  // Coach modal states
  const [coachTrainId, setCoachTrainId] = useState(null);
  const [coachForm, setCoachForm] = useState({ coachNumber: '', classType: 'SL', seatCount: 72 });

  const loadData = () => {
    setLoading(true);
    setError('');
    Promise.all([
      getTrains(true),
      getStations(),
      getAllBookings()
    ])
      .then(([trainList, stationList, bookingList]) => {
        setTrains(trainList);
        setStations(stationList);
        setBookings(bookingList);
        if (trainList.length > 0 && !selectedTrainId) {
          setSelectedTrainId(String(trainList[0].trainId));
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (selectedTrainId) {
      getTrainRoute(selectedTrainId)
        .then((stops) => {
          setRouteStops(stops);
          setStopForm((prev) => ({ ...prev, stopNo: stops.length + 1 }));
        })
        .catch((err) => setError(err.message));
    }
  }, [selectedTrainId]);

  // Station Handlers
  const handleSaveStation = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    try {
      if (editingStationId) {
        await updateStation(editingStationId, stationForm);
        setMessage('Station updated successfully.');
      } else {
        await createStation(stationForm);
        setMessage('Station created successfully.');
      }
      setStationForm({ stationName: '', stationCode: '', city: '' });
      setEditingStationId(null);
      loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteStation = async (id) => {
    if (!window.confirm('Are you sure you want to delete this station?')) return;
    setError('');
    setMessage('');
    try {
      await deleteStation(id);
      setMessage('Station deleted successfully.');
      loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  // Train Handlers
  const handleSaveTrain = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    try {
      if (editingTrainId) {
        await updateTrain(editingTrainId, trainForm);
        setMessage('Train updated successfully.');
      } else {
        await createTrain(trainForm);
        setMessage('Train created successfully.');
      }
      setTrainForm({ trainNumber: '', trainName: '', trainType: 'EXPRESS', isActive: true });
      setEditingTrainId(null);
      loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteTrain = async (id) => {
    if (!window.confirm('Delete this train and all its coaches/seats? (Only allowed if zero bookings exist)')) return;
    setError('');
    setMessage('');
    try {
      await deleteTrain(id);
      setMessage('Train deleted successfully.');
      loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  // Route Stop Handlers
  const handleAddStop = async (e) => {
    e.preventDefault();
    if (!selectedTrainId) return;
    setError('');
    setMessage('');
    try {
      await addRouteStop(selectedTrainId, {
        stationId: Number(stopForm.stationId),
        stopNo: Number(stopForm.stopNo),
        arrivalTime: stopForm.arrivalTime || null,
        departureTime: stopForm.departureTime || null,
        distanceKm: Number(stopForm.distanceKm)
      });
      setMessage('Stop added to route successfully.');
      const stops = await getTrainRoute(selectedTrainId);
      setRouteStops(stops);
      setStopForm({ stationId: '', stopNo: stops.length + 1, arrivalTime: '', departureTime: '', distanceKm: 0 });
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteRouteStop = async (stationId) => {
    if (!selectedTrainId) return;
    if (!window.confirm('Are you sure you want to remove this stop from the route?')) return;
    setError('');
    setMessage('');
    try {
      await deleteRouteStop(selectedTrainId, stationId);
      setMessage('Route stop removed successfully.');
      const stops = await getTrainRoute(selectedTrainId);
      setRouteStops(stops);
    } catch (err) {
      setError(err.message);
    }
  };

  // Booking CRUD Handlers
  const handleUpdateBooking = async (e) => {
    e.preventDefault();
    if (!editingBookingPnr) return;
    setError('');
    setMessage('');
    try {
      await updateBooking(editingBookingPnr, bookingForm);
      setMessage(`Booking PNR ${editingBookingPnr} updated successfully in database.`);
      setEditingBookingPnr(null);
      loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteBooking = async (pnr) => {
    if (!window.confirm(`Permanently DELETE booking PNR ${pnr} from database? This will remove all associated ticket and payment records and release the reserved seats.`)) return;
    setError('');
    setMessage('');
    try {
      await deleteBooking(pnr);
      setMessage(`Booking ${pnr} completely deleted from database and seats freed.`);
      loadData();
    } catch (err) {
      setError(err.message);
    }
  };

  // Coach Handler
  const handleAddCoach = async (e) => {
    e.preventDefault();
    if (!coachTrainId) return;
    setError('');
    setMessage('');
    try {
      await addCoach(coachTrainId, {
        coachNumber: coachForm.coachNumber,
        classType: coachForm.classType,
        seatCount: Number(coachForm.seatCount)
      });
      setMessage(`Coach ${coachForm.coachNumber} with ${coachForm.seatCount} seats added.`);
      setCoachTrainId(null);
      setCoachForm({ coachNumber: '', classType: 'SL', seatCount: 72 });
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary)' }}>Railway Management Dashboard</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Administrative CRUD management for stations, trains, intermediate stops, and live booking logs.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className={`btn ${tab === 'trains' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('trains')}>
            Trains
          </button>
          <button className={`btn ${tab === 'stations' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('stations')}>
            Stations
          </button>
          <button className={`btn ${tab === 'routes' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('routes')}>
            Route Editor
          </button>
          <button className={`btn ${tab === 'bookings' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setTab('bookings')}>
            Bookings Log
          </button>
        </div>
      </div>

      {error && (
        <div className="banner banner-error">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      {message && (
        <div className="banner banner-success">
          <CheckCircle size={20} />
          <span>{message}</span>
        </div>
      )}

      {/* TAB 1: TRAINS */}
      {tab === 'trains' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>
              {editingTrainId ? 'Edit Train' : 'Add New Train'}
            </h3>
            <form onSubmit={handleSaveTrain} className="grid-4" style={{ alignItems: 'flex-end' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Train Number</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. 12951"
                  value={trainForm.trainNumber}
                  onChange={(e) => setTrainForm({ ...trainForm, trainNumber: e.target.value })}
                  disabled={!!editingTrainId}
                  required
                />
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Train Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Mumbai Express"
                  value={trainForm.trainName}
                  onChange={(e) => setTrainForm({ ...trainForm, trainName: e.target.value })}
                  required
                />
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Type</label>
                <select
                  className="form-select"
                  value={trainForm.trainType}
                  onChange={(e) => setTrainForm({ ...trainForm, trainType: e.target.value })}
                >
                  <option value="EXPRESS">EXPRESS</option>
                  <option value="SUPERFAST">SUPERFAST</option>
                  <option value="LOCAL">LOCAL</option>
                  <option value="RAJDHANI">RAJDHANI</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                  {editingTrainId ? 'Update' : 'Add Train'}
                </button>
                {editingTrainId && (
                  <button
                    type="button"
                    className="btn btn-outline"
                    onClick={() => {
                      setEditingTrainId(null);
                      setTrainForm({ trainNumber: '', trainName: '', trainType: 'EXPRESS', isActive: true });
                    }}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Number</th>
                  <th>Name</th>
                  <th>Type</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {trains.map((t) => (
                  <tr key={t.trainId}>
                    <td><strong>{t.trainNumber}</strong></td>
                    <td>{t.trainName}</td>
                    <td><span className="badge badge-info">{t.trainType}</span></td>
                    <td>
                      <span className={`badge ${t.isActive ? 'badge-success' : 'badge-danger'}`}>
                        {t.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                      <button
                        className="btn btn-outline"
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                        title="Add Coach"
                        onClick={() => setCoachTrainId(t.trainId)}
                      >
                        <Layers size={14} /> Coach
                      </button>
                      <button
                        className="btn btn-outline"
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                        onClick={() => {
                          setEditingTrainId(t.trainId);
                          setTrainForm({ trainNumber: t.trainNumber, trainName: t.trainName, trainType: t.trainType, isActive: t.isActive });
                        }}
                      >
                        <Edit2 size={14} />
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                        onClick={() => handleDeleteTrain(t.trainId)}
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 2: STATIONS */}
      {tab === 'stations' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card">
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '1rem' }}>
              {editingStationId ? 'Edit Station' : 'Add New Station'}
            </h3>
            <form onSubmit={handleSaveStation} className="grid-4" style={{ alignItems: 'flex-end' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Station Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Pune Junction"
                  value={stationForm.stationName}
                  onChange={(e) => setStationForm({ ...stationForm, stationName: e.target.value })}
                  required
                />
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Station Code (2–10 UPPER)</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. PUNE"
                  value={stationForm.stationCode}
                  onChange={(e) => setStationForm({ ...stationForm, stationCode: e.target.value.toUpperCase() })}
                  required
                />
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">City</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Pune"
                  value={stationForm.city}
                  onChange={(e) => setStationForm({ ...stationForm, city: e.target.value })}
                  required
                />
              </div>

              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                  {editingStationId ? 'Update' : 'Add Station'}
                </button>
                {editingStationId && (
                  <button
                    type="button"
                    className="btn btn-outline"
                    onClick={() => {
                      setEditingStationId(null);
                      setStationForm({ stationName: '', stationCode: '', city: '' });
                    }}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </form>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Station Name</th>
                  <th>City</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {stations.map((s) => (
                  <tr key={s.stationId}>
                    <td><strong>{s.stationCode}</strong></td>
                    <td>{s.stationName}</td>
                    <td>{s.city}</td>
                    <td style={{ textAlign: 'right', display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                      <button
                        className="btn btn-outline"
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                        onClick={() => {
                          setEditingStationId(s.stationId);
                          setStationForm({ stationName: s.stationName, stationCode: s.stationCode, city: s.city });
                        }}
                      >
                        <Edit2 size={14} />
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                        onClick={() => handleDeleteStation(s.stationId)}
                      >
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 3: ROUTE EDITOR */}
      {tab === 'routes' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div className="card">
            <div className="form-group" style={{ maxWidth: '400px' }}>
              <label className="form-label">Select Train to Edit Route</label>
              <select
                className="form-select"
                value={selectedTrainId}
                onChange={(e) => setSelectedTrainId(e.target.value)}
              >
                {trains.map((t) => (
                  <option key={t.trainId} value={t.trainId}>
                    {t.trainNumber} - {t.trainName}
                  </option>
                ))}
              </select>
            </div>

            <h4 style={{ fontSize: '1rem', fontWeight: 700, margin: '1rem 0 0.5rem' }}>Add Intermediate Stop</h4>
            <form onSubmit={handleAddStop} className="grid-4" style={{ alignItems: 'flex-end' }}>
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Station</label>
                <select
                  className="form-select"
                  value={stopForm.stationId}
                  onChange={(e) => setStopForm({ ...stopForm, stationId: e.target.value })}
                  required
                >
                  <option value="">Select Station</option>
                  {stations.map((s) => (
                    <option key={s.stationId} value={s.stationId}>
                      {s.stationName} ({s.stationCode})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Stop #</label>
                <input
                  type="number"
                  min="1"
                  className="form-input"
                  value={stopForm.stopNo}
                  onChange={(e) => setStopForm({ ...stopForm, stopNo: e.target.value })}
                  required
                />
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Arrival / Departure</label>
                <div style={{ display: 'flex', gap: '0.25rem' }}>
                  <input
                    type="time"
                    className="form-input"
                    value={stopForm.arrivalTime}
                    onChange={(e) => setStopForm({ ...stopForm, arrivalTime: e.target.value })}
                  />
                  <input
                    type="time"
                    className="form-input"
                    value={stopForm.departureTime}
                    onChange={(e) => setStopForm({ ...stopForm, departureTime: e.target.value })}
                  />
                </div>
              </div>

              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Cumulative Distance (KM)</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    type="number"
                    min="0"
                    className="form-input"
                    value={stopForm.distanceKm}
                    onChange={(e) => setStopForm({ ...stopForm, distanceKm: e.target.value })}
                    required
                  />
                  <button type="submit" className="btn btn-primary">
                    <Plus size={16} /> Add
                  </button>
                </div>
              </div>
            </form>
          </div>

          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Stop #</th>
                  <th>Station</th>
                  <th>Arrival Time</th>
                  <th>Departure Time</th>
                  <th>Cumulative Distance</th>
                  <th style={{ textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {routeStops.length === 0 ? (
                  <tr><td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>No stops added yet.</td></tr>
                ) : (
                  routeStops.map((st) => (
                    <tr key={st.stopNo}>
                      <td><strong>{st.stopNo}</strong></td>
                      <td>{st.stationName} ({st.stationCode})</td>
                      <td>{st.arrivalTime ? st.arrivalTime.substring(0, 5) : 'ORIGIN'}</td>
                      <td>{st.departureTime ? st.departureTime.substring(0, 5) : 'TERMINUS'}</td>
                      <td>{st.distanceKm} km</td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          type="button"
                          className="btn btn-danger"
                          style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem' }}
                          title="Remove Stop"
                          onClick={() => handleDeleteRouteStop(st.stationId)}
                        >
                          <Trash2 size={13} />
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* TAB 4: BOOKINGS LOG */}
      {tab === 'bookings' && (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>PNR</th>
                <th>Train</th>
                <th>Route</th>
                <th>Journey Date</th>
                <th>Class</th>
                <th>Fare</th>
                <th>Payment</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <tr key={b.pnr}>
                  <td><strong>{b.pnr}</strong></td>
                  <td>{b.trainName} ({b.trainNumber})</td>
                  <td>{b.sourceStationName} → {b.destStationName}</td>
                  <td>{formatDate(b.journeyDate)}</td>
                  <td><span className="badge badge-info">{b.classType}</span></td>
                  <td>{formatCurrency(b.totalFare)}</td>
                  <td>{b.paymentStatus}</td>
                  <td>
                    <span className={`badge ${
                      b.status === 'CONFIRMED' ? 'badge-success' :
                      b.status === 'CANCELLED' ? 'badge-warning' : 'badge-danger'
                    }`}>
                      {b.status}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right', display: 'flex', gap: '0.4rem', justifyContent: 'flex-end' }}>
                    <button
                      type="button"
                      className="btn btn-outline"
                      style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                      title="Edit Booking"
                      onClick={() => {
                        setEditingBookingPnr(b.pnr);
                        setBookingForm({
                          status: b.status,
                          contactEmail: b.contactEmail || '',
                          contactPhone: b.contactPhone || ''
                        });
                      }}
                    >
                      <Edit2 size={13} /> Edit
                    </button>
                    <button
                      type="button"
                      className="btn btn-danger"
                      style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                      title="Delete Booking"
                      onClick={() => handleDeleteBooking(b.pnr)}
                    >
                      <Trash2 size={13} /> Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Coach Modal */}
      {coachTrainId && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="card" style={{ maxWidth: '420px', width: '90%' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '1rem' }}>
              Add Coach to Train
            </h3>
            <form onSubmit={handleAddCoach}>
              <div className="form-group">
                <label className="form-label">Coach Number (e.g. S3, B2, A2)</label>
                <input
                  type="text"
                  maxLength="5"
                  className="form-input"
                  value={coachForm.coachNumber}
                  onChange={(e) => setCoachForm({ ...coachForm, coachNumber: e.target.value.toUpperCase() })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Class</label>
                <select
                  className="form-select"
                  value={coachForm.classType}
                  onChange={(e) => setCoachForm({ ...coachForm, classType: e.target.value })}
                >
                  <option value="SL">SL (Sleeper)</option>
                  <option value="3A">3A (AC 3 Tier)</option>
                  <option value="2A">2A (AC 2 Tier)</option>
                  <option value="1A">1A (AC First)</option>
                  <option value="CC">CC (Chair Car)</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Total Seats</label>
                <input
                  type="number"
                  min="1"
                  max="120"
                  className="form-input"
                  value={coachForm.seatCount}
                  onChange={(e) => setCoachForm({ ...coachForm, seatCount: e.target.value })}
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
                <button type="button" className="btn btn-outline" onClick={() => setCoachTrainId(null)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Insert Coach & Seats
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Booking Modal */}
      {editingBookingPnr && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="card" style={{ maxWidth: '440px', width: '90%' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '0.5rem' }}>
              Edit Booking PNR: {editingBookingPnr}
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              Update ticket reservation status, email address, or passenger contact phone.
            </p>

            <form onSubmit={handleUpdateBooking}>
              <div className="form-group">
                <label className="form-label">Booking Status</label>
                <select
                  className="form-select"
                  value={bookingForm.status}
                  onChange={(e) => setBookingForm({ ...bookingForm, status: e.target.value })}
                >
                  <option value="CONFIRMED">CONFIRMED</option>
                  <option value="CANCELLED">CANCELLED (Releases Seat)</option>
                  <option value="FAILED">FAILED</option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Contact Email</label>
                <input
                  type="email"
                  className="form-input"
                  value={bookingForm.contactEmail}
                  onChange={(e) => setBookingForm({ ...bookingForm, contactEmail: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Contact Phone (10 digits)</label>
                <input
                  type="tel"
                  maxLength="10"
                  className="form-input"
                  value={bookingForm.contactPhone}
                  onChange={(e) => setBookingForm({ ...bookingForm, contactPhone: e.target.value })}
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
                <button type="button" className="btn btn-outline" onClick={() => setEditingBookingPnr(null)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Save Changes to DB
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
