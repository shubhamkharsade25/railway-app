import React, { useState, useEffect } from 'react';
import { getQueryLabCatalog, runQueryLab } from '../api/client';
import { Database, Play, Copy, Check, Clock, Table, AlertCircle } from 'lucide-react';

export default function QueryLab() {
  const [catalog, setCatalog] = useState([]);
  const [selectedId, setSelectedId] = useState('');
  const [result, setResult] = useState(null);
  const [loadingCatalog, setLoadingCatalog] = useState(true);
  const [executing, setExecuting] = useState(false);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    getQueryLabCatalog()
      .then((data) => {
        setCatalog(data);
        if (data.length > 0) {
          setSelectedId(data[0].id);
        }
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoadingCatalog(false));
  }, []);

  const activeQuery = catalog.find((q) => q.id === selectedId);

  const handleRun = async () => {
    if (!selectedId) return;
    setExecuting(true);
    setError('');
    try {
      const res = await runQueryLab(selectedId);
      setResult(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setExecuting(false);
    }
  };

  const handleCopy = () => {
    if (activeQuery) {
      navigator.clipboard.writeText(activeQuery.sql);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  if (loadingCatalog) {
    return <div style={{ textAlign: 'center', padding: '3rem' }}>Loading SQL Query Lab catalog...</div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      <div>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Database size={28} color="var(--accent)" />
          SQL Query Lab (Academic Viva Showpiece)
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          Interactive educational showcase of core DBMS constructs: Joins, Correlated Subqueries, Window Functions, Views, and CTEs.
        </p>
      </div>

      {error && (
        <div className="banner banner-error">
          <AlertCircle size={20} />
          <span>{error}</span>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(260px, 320px) 1fr', gap: '1.5rem', alignItems: 'flex-start' }}>
        {/* Left catalog list */}
        <div className="card" style={{ padding: '0.75rem', maxHeight: '700px', overflowY: 'auto' }}>
          <div style={{ fontSize: '0.8rem', fontWeight: 700, color: 'var(--text-muted)', padding: '0.5rem 0.75rem' }}>
            DEMO QUERY CATALOG ({catalog.length})
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
            {catalog.map((q) => (
              <button
                key={q.id}
                type="button"
                onClick={() => {
                  setSelectedId(q.id);
                  setResult(null);
                }}
                style={{
                  textAlign: 'left',
                  padding: '0.65rem 0.75rem',
                  borderRadius: '6px',
                  border: '1px solid',
                  borderColor: q.id === selectedId ? 'var(--primary)' : 'transparent',
                  background: q.id === selectedId ? 'var(--primary-light)' : 'transparent',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '0.2rem',
                  transition: 'all 0.15s ease'
                }}
              >
                <div style={{ fontWeight: 700, fontSize: '0.875rem', color: q.id === selectedId ? 'var(--primary)' : 'var(--text-main)' }}>
                  {q.title}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  {q.concept}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Right query display & results */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {activeQuery && (
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.5rem' }}>
                <div>
                  <h2 style={{ fontSize: '1.25rem', fontWeight: 700 }}>{activeQuery.title}</h2>
                  <span className="badge badge-info" style={{ marginTop: '0.25rem' }}>{activeQuery.concept}</span>
                  <p style={{ color: '#475569', fontSize: '0.9rem', marginTop: '0.5rem' }}>
                    {activeQuery.description}
                  </p>
                </div>

                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button type="button" className="btn btn-outline" onClick={handleCopy}>
                    {copied ? <Check size={16} color="var(--success)" /> : <Copy size={16} />}
                    {copied ? 'Copied' : 'Copy SQL'}
                  </button>
                  <button type="button" className="btn btn-accent" onClick={handleRun} disabled={executing}>
                    <Play size={16} />
                    {executing ? 'Executing...' : 'Run Query'}
                  </button>
                </div>
              </div>

              {/* SQL Code Block */}
              <div style={{
                background: '#0f172a',
                color: '#e2e8f0',
                padding: '1rem 1.25rem',
                borderRadius: '8px',
                fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
                fontSize: '0.875rem',
                overflowX: 'auto',
                lineHeight: 1.6,
                border: '1px solid #1e293b'
              }}>
                <pre style={{ margin: 0 }}>{activeQuery.sql}</pre>
              </div>
            </div>
          )}

          {/* Results panel */}
          {result && (
            <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 700 }}>
                  <Table size={18} color="var(--primary)" />
                  <span>Query Results ({result.rowCount} rows returned)</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                  <Clock size={15} />
                  <span>Execution time: <strong>{result.executionMillis} ms</strong></span>
                </div>
              </div>

              <div className="table-container" style={{ maxHeight: '420px', overflowY: 'auto' }}>
                <table className="data-table">
                  <thead>
                    <tr>
                      {result.columns?.map((col) => (
                        <th key={col}>{col}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {result.rows?.length === 0 ? (
                      <tr>
                        <td colSpan={result.columns?.length || 1} style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
                          Query executed successfully. Zero rows returned.
                        </td>
                      </tr>
                    ) : (
                      result.rows?.map((row, idx) => (
                        <tr key={idx}>
                          {result.columns?.map((col) => (
                            <td key={col}>{String(row[col] ?? 'NULL')}</td>
                          ))}
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
