import React from 'react';
import { NavLink } from 'react-router-dom';
import { Train, Search, Ticket, Database, BarChart3, LayoutDashboard } from 'lucide-react';

export default function Navbar() {
  return (
    <header className="navbar no-print">
      <div className="nav-inner">
        <NavLink to="/" className="nav-brand">
          <Train size={24} color="#f59e0b" />
          <span>RailTrack</span>
          <span className="badge">DBMS</span>
        </NavLink>

        <nav className="nav-links">
          <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Search & Book
          </NavLink>
          <NavLink to="/pnr" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            PNR Status
          </NavLink>
          <NavLink to="/query-lab" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            SQL Query Lab
          </NavLink>
          <NavLink to="/admin" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Management
          </NavLink>
          <NavLink to="/admin/reports" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Analytics
          </NavLink>
        </nav>
      </div>
    </header>
  );
}
