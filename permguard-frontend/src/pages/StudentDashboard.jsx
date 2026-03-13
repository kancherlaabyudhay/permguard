import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import { permissionsApi, qrApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

const NAV = [
  { label: 'My Permissions', items: [
    { id: 'new', icon: '➕', label: 'New Request' },
    { id: 'my', icon: '📋', label: 'My Requests' },
    { id: 'qr', icon: '📱', label: 'My QR Codes' },
  ]},
];

const TYPES = ['HOME_VISIT','MEDICAL','PERSONAL','EMERGENCY','OTHER'];

export default function StudentDashboard() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('my');
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState('');
  const [qrModal, setQrModal] = useState(null);

  // Form state
  const [form, setForm] = useState({ type: 'HOME_VISIT', reason: '', expiryTime: '' });
  const [submitting, setSubmitting] = useState(false);

  const loadPermissions = async () => {
    setLoading(true);
    try { const r = await permissionsApi.getMyPermissions(); setPermissions(r.data); }
    catch {} finally { setLoading(false); }
  };

  useEffect(() => { if (activeTab !== 'new') loadPermissions(); }, [activeTab]);

  const submitRequest = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      // ✅ Ensure expiryTime is in ISO format without 'Z' (LocalDateTime needs no timezone)
      const payload = {
        ...form,
        expiryTime: form.expiryTime.length === 16
          ? form.expiryTime + ':00'   // "2026-03-04T10:00" → "2026-03-04T10:00:00"
          : form.expiryTime
      };
      await permissionsApi.submit(payload);
      setMsg('✅ Permission request submitted successfully!');
      setForm({ type: 'HOME_VISIT', reason: '', expiryTime: '' });
      setTimeout(() => setMsg(''), 4000);
    } catch (err) {
      setMsg('❌ ' + (err.response?.data?.message || 'Submission failed'));
    } finally { setSubmitting(false); }
  };

  const viewQr = async (p) => {
    try {
      const r = await qrApi.get(p.id);
      setQrModal({ permission: p, qrBase64: r.data.qrBase64 });
    } catch { setMsg('❌ QR not available yet. Wait for approval.'); setTimeout(() => setMsg(''), 3000); }
  };

  const statusBadge = (s) => {
    const map = { PENDING:'badge-pending',APPROVED:'badge-approved',REJECTED:'badge-rejected',EXPIRED:'badge-expired' };
    return map[s] || 'badge-pending';
  };

  const approved = permissions.filter(p => p.status === 'APPROVED');
  const pending = permissions.filter(p => p.status === 'PENDING');
  const rejected = permissions.filter(p => p.status === 'REJECTED');

  return (
    <div className="dashboard-layout">
      <Sidebar navItems={NAV} activeTab={activeTab} setActiveTab={setActiveTab} />
      <main className="main-content">

        {msg && <div className={`alert-banner ${msg.startsWith('✅') ? 'success' : 'error'}`}>{msg}</div>}

        {/* ── NEW REQUEST ── */}
        {activeTab === 'new' && (
          <>
            <div className="page-header">
              <h2>New Permission Request</h2>
              <p>Submit a request for leave or campus exit</p>
            </div>
            <div className="panel" style={{maxWidth:560}}>
              <div className="panel-header"><h3>Request Details</h3></div>
              <div className="panel-body">
                <form onSubmit={submitRequest}>
                  <div className="form-group">
                    <label>Permission Type</label>
                    <select className="form-control" value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
                      {TYPES.map(t => <option key={t} value={t}>{t.replace('_',' ')}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Reason</label>
                    <textarea
                      className="form-control" rows={4} required
                      value={form.reason} onChange={e => setForm({...form, reason: e.target.value})}
                      placeholder="Describe why you need this permission…"
                      style={{resize:'vertical'}}
                    />
                  </div>
                  <div className="form-group">
                    <label>Return By (Expiry Time)</label>
                    <input
                      className="form-control" type="datetime-local" required
                      value={form.expiryTime} onChange={e => setForm({...form, expiryTime: e.target.value})}
                      min={new Date().toISOString().slice(0,16)}
                    />
                  </div>
                  <button className="btn btn-primary" type="submit" disabled={submitting} style={{width:'100%', padding:'12px'}}>
                    {submitting ? '⏳ Submitting…' : '📤 Submit Request'}
                  </button>
                </form>
              </div>
            </div>
          </>
        )}

        {/* ── MY REQUESTS ── */}
        {activeTab === 'my' && (
          <>
            <div className="page-header">
              <h2>My Permission Requests</h2>
              <p>Track the status of all your requests</p>
            </div>

            <div className="stat-grid" style={{gridTemplateColumns:'repeat(3,1fr)', maxWidth:540}}>
              <div className="stat-card success"><div className="stat-label">Approved</div><div className="stat-value">{approved.length}</div></div>
              <div className="stat-card"><div className="stat-label">Pending</div><div className="stat-value" style={{color:'#C9973A'}}>{pending.length}</div></div>
              <div className="stat-card danger"><div className="stat-label">Rejected</div><div className="stat-value">{rejected.length}</div></div>
            </div>

            <div className="panel">
              <div className="panel-body">
                {loading ? <div className="loading">Loading…</div> :
                permissions.length === 0 ? (
                  <div className="empty-state">
                    <div className="empty-icon">📋</div>
                    <p>No requests yet. <span style={{color:'#7B1C1C', cursor:'pointer'}} onClick={() => setActiveTab('new')}>Submit your first request →</span></p>
                  </div>
                ) : (
                  <div style={{display:'flex', flexDirection:'column', gap:14}}>
                    {permissions.map(p => (
                      <div key={p.id} style={styles.reqCard}>
                        <div style={styles.reqLeft}>
                          <div style={styles.reqTop}>
                            <span className={`badge ${statusBadge(p.status)}`}>{p.status}</span>
                            <span style={styles.reqType}>{p.type?.replace('_',' ')}</span>
                            <span style={{color:'#bbb', fontSize:'0.78rem'}}>#{p.id}</span>
                          </div>
                          <div style={styles.reqReason}>"{p.reason}"</div>
                          <div style={styles.reqMeta}>
                            <span>📅 {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '—'}</span>
                            <span>⏰ Expires: {p.expiryTime ? new Date(p.expiryTime).toLocaleString() : '—'}</span>
                            {p.approvedBy && <span>✅ By: {p.approvedBy}</span>}
                          </div>
                          {p.status === 'REJECTED' && p.rejectionReason && (
                            <div style={styles.rejectNote}>Reason: {p.rejectionReason}</div>
                          )}
                        </div>
                        {p.status === 'APPROVED' && (
                          <button className="btn btn-ghost btn-sm" onClick={() => viewQr(p)}>📱 View QR</button>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </>
        )}

        {/* ── QR CODES ── */}
        {activeTab === 'qr' && (
          <>
            <div className="page-header">
              <h2>My QR Codes</h2>
              <p>QR codes for approved permissions — show at gate</p>
            </div>
            <div style={{display:'grid', gridTemplateColumns:'repeat(auto-fill, minmax(280px, 1fr))', gap:16}}>
              {approved.length === 0 ? (
                <div className="panel" style={{gridColumn:'1/-1'}}>
                  <div className="panel-body">
                    <div className="empty-state"><div className="empty-icon">📱</div><p>No approved permissions with QR codes yet</p></div>
                  </div>
                </div>
              ) : approved.map(p => (
                <div key={p.id} className="panel" style={{marginBottom:0}}>
                  <div className="panel-header">
                    <h3 style={{fontSize:'0.95rem'}}>{p.type?.replace('_',' ')}</h3>
                    <span className="badge badge-approved">APPROVED</span>
                  </div>
                  <div className="panel-body" style={{textAlign:'center'}}>
                    <p style={{fontSize:'0.82rem', color:'#888', marginBottom:14}}>"{p.reason}"</p>
                    <p style={{fontSize:'0.78rem', color:'#aaa', marginBottom:16}}>
                      Expires: {p.expiryTime ? new Date(p.expiryTime).toLocaleString() : '—'}
                    </p>
                    <button className="btn btn-primary" style={{width:'100%'}} onClick={() => viewQr(p)}>
                      📱 Show QR Code
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        {/* QR Modal */}
        {qrModal && (
          <div className="modal-overlay" onClick={() => setQrModal(null)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <div className="modal-header">
                <h3>QR Code — {qrModal.permission.type?.replace('_',' ')}</h3>
                <button className="modal-close" onClick={() => setQrModal(null)}>×</button>
              </div>
              <div className="modal-body">
                <div className="qr-container">
                  {qrModal.qrBase64 ? (
                    <img src={`data:image/png;base64,${qrModal.qrBase64}`} alt="QR Code" />
                  ) : <div className="loading">Loading QR…</div>}
                  <p style={{fontSize:'0.85rem', color:'#555', textAlign:'center'}}>
                    Show this QR code to security staff at the gate
                  </p>
                  <div style={{background:'#FDF5F5', padding:'12px 20px', borderRadius:8, width:'100%', textAlign:'center'}}>
                    <div style={{fontSize:'0.78rem', color:'#888'}}>Expires</div>
                    <div style={{fontWeight:700, color:'#7B1C1C'}}>
                      {qrModal.permission.expiryTime ? new Date(qrModal.permission.expiryTime).toLocaleString() : '—'}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

const styles = {
  reqCard: { display:'flex', alignItems:'center', justifyContent:'space-between', gap:16, padding:'16px', background:'#FDFAFA', borderRadius:10, border:'1px solid #F0E8E8' },
  reqLeft: { flex:1 },
  reqTop: { display:'flex', alignItems:'center', gap:10, marginBottom:8 },
  reqType: { fontWeight:600, color:'#7B1C1C', fontSize:'0.88rem' },
  reqReason: { fontStyle:'italic', color:'#555', fontSize:'0.88rem', marginBottom:8 },
  reqMeta: { display:'flex', gap:16, flexWrap:'wrap', fontSize:'0.76rem', color:'#999' },
  rejectNote: { marginTop:8, fontSize:'0.8rem', color:'#B91C1C', background:'#FEE2E2', padding:'6px 12px', borderRadius:6 },
};