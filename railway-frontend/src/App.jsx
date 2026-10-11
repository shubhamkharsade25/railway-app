import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Home from './pages/Home';
import Results from './pages/Results';
import Book from './pages/Book';
import TicketView from './pages/TicketView';
import PnrStatus from './pages/PnrStatus';
import Admin from './pages/Admin';
import Reports from './pages/Reports';
import QueryLab from './pages/QueryLab';

export default function App() {
  return (
    <BrowserRouter>
      <div className="app-container">
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/results" element={<Results />} />
            <Route path="/book" element={<Book />} />
            <Route path="/ticket/:pnr" element={<TicketView />} />
            <Route path="/pnr" element={<PnrStatus />} />
            <Route path="/admin" element={<Admin />} />
            <Route path="/admin/reports" element={<Reports />} />
            <Route path="/query-lab" element={<QueryLab />} />
          </Routes>
        </main>
        <Footer />
      </div>
    </BrowserRouter>
  );
}
