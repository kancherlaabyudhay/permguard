import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import { permissionsApi } from '../services/api';

const NAV = [
  { label: 'My Work', items: [
    { id: 'pending', icon: '⏳', label: 'Pending Approvals' },
    { id: 'history', icon: '📜', label: 'Approval History' },
  ]},
];

export default function FacultyDashboard() {
  const [activeTab, setActiveTab] = useState('pending');
  const [pending, setPending] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [msg, setMsg] = useState('');

  const loadPending = async () => {
    setLoading(true);
    try { const r = await permissionsApi.getPending(); setPending(r.data); }
    catch {} finally { setLoading(false); }
  };

  const loadHistory = async () => {
    setLoading(true);
    try { const r = await permissionsApi.getAll(); setHistory(r.data.filter(p => p.status !== 'PENDING')); }
    catch {} finally { setLoading(false); }
  };

  useEffect(() => {
    if (activeTab === 'pending') loadPending();
    else loadHistory();
  }, [activeTab]);

  const approve = async (id) => {
    try {
      await permissionsApi.approve(id);
      setMsg('✅ Permission approved!');
      setPending(p => p.filter(x => x.id !== id));
      setTimeout(() => setMsg(''), 3000);
    } catch (e) { setMsg('❌ ' + (e.response?.data?.message || 'Failed')); }
  };

  const openReject = (p) => { setSelected(p); setRejectReason(''); setShowRejectModal(true); };

  const reject = async () => {
    if (!rejectReason.trim()) return;
    try {
      await permissionsApi.reject(selected.id, rejectReason);
      setMsg('Permission rejected.');
      setPending(p => p.filter(x => x.id !== selected.id));
      setShowRejectModal(false);
      setTimeout(() => setMsg(''), 3000);
    } catch (e) { setMsg('❌ ' + (e.response?.data?.message || 'Failed')); }
  };

  const statusBadge = (s) => {
    const map = { PENDING:'badge-pending',APPROVED:'badge-approved',REJECTED:'badge-rejected',EXPIRED:'badge-expired' };
    return map[s] || 'badge-pending';
  };

  return (
    <div className="dashboard-layout">
      <Sidebar navItems={NAV} activeTab={activeTab} setActiveTab={setActiveTab} />
      <main className="main-content">

        {msg && <div className={`alert-banner ${msg.startsWith('✅') ? 'success' : 'error'}`}>{msg}</div>}

        {/* ── PENDING APPROVALS ── */}
        {activeTab === 'pending' && (
          <>
            <div className="page-header">
              <h2>Pending Approvals</h2>
              <p>{pending.length} permission{pending.length !== 1 ? 's' : ''} awaiting your review</p>
            </div>

            {loading ? <div className="loading">Loading…</div> : pending.length === 0 ? (
              <div className="panel">
                <div className="panel-body">
                  <div className="empty-state">
                    <div className="empty-icon">🎉</div>
                    <p>All caught up! No pending approvals.</p>
                  </div>
                </div>
              </div>
            ) : (
              <div style={{display:'flex', flexDirection:'column', gap:16}}>
                {pending.map(p => (
                  <div key={p.id} className="panel" style={{marginBottom:0}}>
                    <div className="panel-body">
                      <div style={styles.card}>
                        <div style={styles.cardLeft}>
                          <div style={styles.cardTop}>
                            <span className="badge badge-pending">PENDING</span>
                            <span style={styles.permType}>{p.type?.replace('_',' ')}</span>
                            <span style={styles.permId}>#{p.id}</span>
                          </div>
                          <div style={styles.studentName}>{p.studentName || p.student?.fullName || 'Student'}</div>
                          <div style={styles.studentRoll}>Roll: {p.studentRoll || p.student?.rollNumber || '—'}</div>
                          <div style={styles.reason}>"{p.reason}"</div>
                          <div style={styles.meta}>
                            <span>📅 Submitted: {p.createdAt ? new Date(p.createdAt).toLocaleDateString() : '—'}</span>
                            <span>⏰ Expires: {p.expiryTime ? new Date(p.expiryTime).toLocaleString() : '—'}</span>
                          </div>
                        </div>
                        <div style={styles.cardActions}>
                          <button className="btn btn-success" onClick={() => approve(p.id)}>✓ Approve</button>
                          <button className="btn btn-danger btn-sm" onClick={() => openReject(p)}>✗ Reject</button>
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        {/* ── HISTORY ── */}
        {activeTab === 'history' && (
          <>
            <div className="page-header">
              <h2>Approval History</h2>
              <p>All permissions you have reviewed</p>
            </div>
            <div className="panel">
              <div className="panel-body">
                {loading ? <div className="loading">Loading…</div> : (
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr><th>ID</th><th>Student</th><th>Type</th><th>Reason</th><th>Status</th><th>Reviewed</th></tr>
                      </thead>
                      <tbody>
                        {history.length === 0 ? (
                          <tr><td colSpan={6}><div className="empty-state"><div className="empty-icon">📜</div><p>No history yet</p></div></td></tr>
                        ) : history.map(p => (
                          <tr key={p.id}>
                            <td>#{p.id}</td>
                            <td><strong>{p.studentName || p.student?.fullName || '—'}</strong></td>
                            <td>{p.type?.replace('_',' ')}</td>
                            <td style={{maxWidth:200,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{p.reason}</td>
                            <td><span className={`badge ${statusBadge(p.status)}`}>{p.status}</span></td>
                            <td>{p.approvedAt ? new Date(p.approvedAt).toLocaleDateString() : '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </>
        )}

        {/* Reject Modal */}
        {showRejectModal && (
          <div className="modal-overlay" onClick={() => setShowRejectModal(false)}>
            <div className="modal" onClick={e => e.stopPropagation()}>
              <div className="modal-header">
                <h3>Reject Permission</h3>
                <button className="modal-close" onClick={() => setShowRejectModal(false)}>×</button>
              </div>
              <div className="modal-body">
                <p style={{marginBottom:16, color:'#666', fontSize:'0.9rem'}}>
                  Rejecting request from <strong>{selected?.studentName || selected?.student?.fullName}</strong>
                </p>
                <div className="form-group">
                  <label>Reason for Rejection</label>
                  <textarea
                    className="form-control"
                    rows={3} value={rejectReason}
                    onChange={e => setRejectReason(e.target.value)}
                    placeholder="Provide a reason for rejection…"
                    style={{resize:'vertical'}}
                  />
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-ghost" onClick={() => setShowRejectModal(false)}>Cancel</button>
                <button className="btn btn-danger" onClick={reject}>Confirm Reject</button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}

const styles = {
  card: { display:'flex', alignItems:'flex-start', justifyContent:'space-between', gap:20 },
  cardLeft: { flex:1 },
  cardTop: { display:'flex', alignItems:'center', gap:10, marginBottom:10 },
  permType: { fontWeight:700, fontSize:'0.9rem', color:'#7B1C1C' },
  permId: { color:'#aaa', fontSize:'0.8rem' },
  studentName: { fontSize:'1.1rem', fontWeight:700, color:'#2D2D2D' },
  studentRoll: { fontSize:'0.8rem', color:'#888', marginBottom:8 },
  reason: { fontStyle:'italic', color:'#555', fontSize:'0.9rem', marginBottom:10, background:'#FDF5F5', padding:'10px 14px', borderRadius:8, borderLeft:'3px solid #7B1C1C' },
  meta: { display:'flex', gap:20, fontSize:'0.78rem', color:'#888' },
  cardActions: { display:'flex', flexDirection:'column', gap:8, flexShrink:0 },
};