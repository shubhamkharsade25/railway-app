const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

async function request(path, options = {}) {
  const url = `${API_BASE}${path}`;
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  const response = await fetch(url, { ...options, headers });

  if (response.status === 204) {
    return null;
  }

  const data = await response.json().catch(() => null);

  if (!response.ok) {
    const errorMsg = data?.message || data?.error || `HTTP error ${response.status}`;
    const error = new Error(errorMsg);
    error.status = response.status;
    error.data = data;
    throw error;
  }

  return data;
}

// Stations API
export const getStations = (q) => request(`/stations${q ? `?q=${encodeURIComponent(q)}` : ''}`);
export const getStation = (id) => request(`/stations/${id}`);
export const createStation = (payload) => request('/stations', { method: 'POST', body: JSON.stringify(payload) });
export const updateStation = (id, payload) => request(`/stations/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
export const deleteStation = (id) => request(`/stations/${id}`, { method: 'DELETE' });

// Trains API
export const getTrains = (all = false) => request(`/trains${all ? '?all=true' : ''}`);
export const getTrain = (id) => request(`/trains/${id}`);
export const createTrain = (payload) => request('/trains', { method: 'POST', body: JSON.stringify(payload) });
export const updateTrain = (id, payload) => request(`/trains/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
export const deleteTrain = (id) => request(`/trains/${id}`, { method: 'DELETE' });
export const getTrainRoute = (id) => request(`/trains/${id}/route`);
export const addRouteStop = (trainId, payload) => request(`/trains/${trainId}/route/stops`, { method: 'POST', body: JSON.stringify(payload) });
export const addCoach = (trainId, payload) => request(`/trains/${trainId}/coaches`, { method: 'POST', body: JSON.stringify(payload) });

// Search & Availability
export const searchTrains = ({ source, destination, date, classType }) => {
  let url = `/trains/search?source=${source}&destination=${destination}&date=${date}`;
  if (classType) url += `&classType=${classType}`;
  return request(url);
};
export const getAvailability = (trainId, { date, classType, includeSeats = false }) =>
  request(`/trains/${trainId}/availability?date=${date}&classType=${classType}&includeSeats=${includeSeats}`);

// Bookings
export const createBooking = (payload) => request('/bookings', { method: 'POST', body: JSON.stringify(payload) });
export const getBooking = (pnr) => request(`/bookings/${pnr}`);
export const getTicket = (pnr) => request(`/bookings/${pnr}/ticket`);
export const getPayment = (pnr) => request(`/bookings/${pnr}/payment`);
export const cancelBooking = (pnr, payload) => request(`/bookings/${pnr}/cancel`, { method: 'POST', body: JSON.stringify(payload) });
export const getAllBookings = (status) => request(`/bookings${status ? `?status=${encodeURIComponent(status)}` : ''}`);

// Reports & Query Lab
export const getSummaryReport = () => request('/reports/summary');
export const getRevenueReport = (from, to) => {
  let url = '/reports/revenue';
  const params = [];
  if (from) params.push(`from=${from}`);
  if (to) params.push(`to=${to}`);
  if (params.length) url += `?${params.join('&')}`;
  return request(url);
};
export const getPopularTrains = (limit = 5) => request(`/reports/popular-trains?limit=${limit}`);
export const getOccupancyReport = (date) => request(`/reports/occupancy${date ? `?date=${date}` : ''}`);

export const getQueryLabCatalog = () => request('/query-lab');
export const runQueryLab = (queryId) => request(`/query-lab/${queryId}/result`);
