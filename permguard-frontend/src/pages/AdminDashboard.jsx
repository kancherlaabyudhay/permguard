import { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import { analyticsApi, fraudApi, adminApi, permissionsApi } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line } from 'recharts';

const NAV = [
  { label: 'Overview', items: [
    { id: 'dashboard', icon: '📊', label: 'Dashboard' },
    { id: 'analytics', icon: '📈', label: 'Analytics' },
  ]},
  { label: 'Management', items: [
    { id: 'permissions', icon: '📋', label: 'All Permissions' },
    { id: 'users', icon: '👥', label: 'Users' },
    { id: 'fraud', icon: '🛡️', label: 'Fraud & Alerts' },
  ]},
];

const COLORS = ['#7B1C1C','#C9973A','#2D7A4F','#1D4ED8','#9A3412','#6B7280'];

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [summary, setSummary] = useState(null);
  const [byType, setByType] = useState([]);
  const [byStatus, setByStatus] = useState([]);
  const [byDept, setByDept] = useState([]);
  const [trend, setTrend] = useState([]);
  const [topStudents, setTopStudents] = useState([]);
  const [fraudData, setFraudData] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [riskProfiles, setRiskProfiles] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadAll(); }, []);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [s, bt, bs, bd, tr, ts, fd, al, rp] = await Promise.allSettled([
        analyticsApi.summary(), analyticsApi.byType(), analyticsApi.byStatus(),
        analyticsApi.byDept(), analyticsApi.dailyTrend(), analyticsApi.topStudents(),
        fraudApi.dashboard(), fraudApi.alerts(), fraudApi.riskProfiles(),
      ]);
      if (s.status === 'fulfilled') setSummary(s.value.data);
      if (bt.status === 'fulfilled') setByType(bt.value.data);
      if (bs.status === 'fulfilled') setByStatus(bs.value.data);
      if (bd.status === 'fulfilled') setByDept(bd.value.data);
      if (tr.status === 'fulfilled') setTrend(tr.value.data);
      if (ts.status === 'fulfilled') setTopStudents(ts.value.data);
      if (fd.status === 'fulfilled') setFraudData(fd.value.data);
      if (al.status === 'fulfilled') setAlerts(al.value.data);
      if (rp.status === 'fulfilled') setRiskProfiles(rp.value.data);
    } finally { setLoading(false); }
  };

  const loadPermissions = async () => {
    try { const r = await permissionsApi.getAll(); setPermissions(r.data); } catch {}
  };

  const loadUsers = async () => {
    try {
      const [s, f] = await Promise.allSettled([adminApi.getUsers(), adminApi.getFaculty()]);
      const students = s.status === 'fulfilled' ? s.value.data : [];
      const faculty = f.status === 'fulfilled' ? f.value.data : [];
      setUsers([...students, ...faculty]);
    } catch {}
  };

  const toggleStatus = async (id) => {
    try { await adminApi.toggleStatus(id); loadUsers(); } catch {}
  };

  const unlockAccount = async (id) => {
    try { await adminApi.unlockAccount(id); loadUsers(); } catch {}
  };

  const resetPassword = async (id) => {
    try { await adminApi.resetPassword(id); alert('Password reset to default!'); } catch {}
  };

  useEffect(() => {
    if (activeTab === 'permissions') loadPermissions();
    if (activeTab === 'users') loadUsers();
  }, [activeTab]);

  const resolveAlert = async (id) => {
    try { await fraudApi.resolveAlert(id); setAlerts(a => a.filter(x => x.alertId !== id)); } catch {}
  };

  const statusColor = (s) => {
    const map = { PENDING:'badge-pending',APPROVED:'badge-approved',REJECTED:'badge-rejected',EXPIRED:'badge-expired',FLAGGED:'badge-flagged' };
    return map[s] || 'badge-pending';
  };

  const riskColor = (score) => {
    if (score >= 90) return '#4A0E0E';
    if (score >= 70) return '#B91C1C';
    if (score >= 40) return '#C9973A';
    return '#2D7A4F';
  };

  if (loading) return <div className="splash">⏳ Loading dashboard…</div>;

  return (
    <div className="dashboard-layout">
      <Sidebar navItems={NAV} activeTab={activeTab} setActiveTab={setActiveTab} />
      <main className="main-content">

        {/* ── DASHBOARD ── */}
        {activeTab === 'dashboard' && (
          <>
            <div className="page-header">
              <h2>Admin Dashboard</h2>
              <p>System overview — Malla Reddy University PermGuard</p>
            </div>

            <div className="stat-grid">
              <div className="stat-card">
                <div className="stat-label">Total Permissions</div>
                <div className="stat-value">{summary?.totalPermissions ?? '—'}</div>
                <div className="stat-sub">All time</div>
              </div>
              <div className="stat-card success">
                <div className="stat-label">Approved</div>
                <div className="stat-value">{summary?.approvedPermissions ?? '—'}</div>
                <div className="stat-sub">Approval rate: {summary?.approvalRate}%</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Pending</div>
                <div className="stat-value" style={{color:'#C9973A'}}>{summary?.pendingPermissions ?? '—'}</div>
                <div className="stat-sub">Awaiting review</div>
              </div>
              <div className="stat-card danger">
                <div className="stat-label">Fraud Alerts</div>
                <div className="stat-value">{fraudData?.unresolvedAlerts ?? '—'}</div>
                <div className="stat-sub">Unresolved</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Gate Scans</div>
                <div className="stat-value">{summary?.totalGateScans ?? '—'}</div>
                <div className="stat-sub">Total scans</div>
              </div>
              <div className="stat-card gold">
                <div className="stat-label">Students</div>
                <div className="stat-value">{summary?.totalStudents ?? '—'}</div>
                <div className="stat-sub">Registered</div>
              </div>
            </div>

            <div className="two-col">
              <div className="panel">
                <div className="panel-header"><h3>Permission Types</h3></div>
                <div className="panel-body">
                  <ResponsiveContainer width="100%" height={220}>
                    <BarChart data={byType}>
                      <XAxis dataKey="type" tick={{fontSize:11}} />
                      <YAxis tick={{fontSize:11}} />
                      <Tooltip />
                      <Bar dataKey="count" fill="#7B1C1C" radius={[4,4,0,0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
              <div className="panel">
                <div className="panel-header"><h3>Status Breakdown</h3></div>
                <div className="panel-body">
                  <ResponsiveContainer width="100%" height={220}>
                    <PieChart>
                      <Pie data={byStatus} dataKey="count" nameKey="status" cx="50%" cy="50%" outerRadius={80} label={({status,count})=>`${status}: ${count}`} labelLine={false}>
                        {byStatus.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>

            <div className="panel">
              <div className="panel-header"><h3>Daily Trend (Last 30 Days)</h3></div>
              <div className="panel-body">
                <ResponsiveContainer width="100%" height={200}>
                  <LineChart data={trend}>
                    <XAxis dataKey="date" tick={{fontSize:10}} />
                    <YAxis tick={{fontSize:11}} />
                    <Tooltip />
                    <Line type="monotone" dataKey="count" stroke="#7B1C1C" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="two-col">
              <div className="panel">
                <div className="panel-header"><h3>Top Students by Requests</h3></div>
                <div className="panel-body">
                  <div className="table-wrap">
                    <table>
                      <thead><tr><th>#</th><th>Student</th><th>Roll No</th><th>Requests</th></tr></thead>
                      <tbody>
                        {topStudents.slice(0,8).map((s, i) => (
                          <tr key={i}>
                            <td><strong>{i+1}</strong></td>
                            <td>{s.studentName}</td>
                            <td>{s.rollNumber}</td>
                            <td><span className="badge badge-approved">{s.count}</span></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              <div className="panel">
                <div className="panel-header"><h3>Department Usage</h3></div>
                <div className="panel-body">
                  <ResponsiveContainer width="100%" height={220}>
                    <BarChart data={byDept} layout="vertical">
                      <XAxis type="number" tick={{fontSize:11}} />
                      <YAxis dataKey="department" type="category" tick={{fontSize:10}} width={90} />
                      <Tooltip />
                      <Bar dataKey="count" fill="#C9973A" radius={[0,4,4,0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </>
        )}

        {/* ── ANALYTICS ── */}
        {activeTab === 'analytics' && (
          <>
            <div className="page-header">
              <h2>Analytics</h2>
              <p>Deep insights into permission usage patterns</p>
            </div>
            <div className="panel">
              <div className="panel-header"><h3>Daily Permission Trend</h3></div>
              <div className="panel-body">
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={trend}>
                    <XAxis dataKey="date" tick={{fontSize:10}} />
                    <YAxis tick={{fontSize:11}} />
                    <Tooltip />
                    <Line type="monotone" dataKey="count" stroke="#7B1C1C" strokeWidth={2.5} dot={{r:3,fill:'#7B1C1C'}} />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
            <div className="two-col">
              <div className="panel">
                <div className="panel-header"><h3>By Permission Type</h3></div>
                <div className="panel-body">
                  <ResponsiveContainer width="100%" height={260}>
                    <BarChart data={byType}>
                      <XAxis dataKey="type" tick={{fontSize:11}} />
                      <YAxis tick={{fontSize:11}} />
                      <Tooltip />
                      <Bar dataKey="count" fill="#7B1C1C" radius={[4,4,0,0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
              <div className="panel">
                <div className="panel-header"><h3>By Department</h3></div>
                <div className="panel-body">
                  <ResponsiveContainer width="100%" height={260}>
                    <BarChart data={byDept} layout="vertical">
                      <XAxis type="number" tick={{fontSize:11}} />
                      <YAxis dataKey="department" type="category" tick={{fontSize:10}} width={100} />
                      <Tooltip />
                      <Bar dataKey="count" fill="#C9973A" radius={[0,4,4,0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          </>
        )}

        {/* ── PERMISSIONS ── */}
        {activeTab === 'permissions' && (
          <>
            <div className="page-header">
              <h2>All Permissions</h2>
              <p>Complete permission history across all students</p>
            </div>
            <div className="panel">
              <div className="panel-body">
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr><th>ID</th><th>Student</th><th>Type</th><th>Reason</th><th>Status</th><th>Expiry</th></tr>
                    </thead>
                    <tbody>
                      {permissions.length === 0 ? (
                        <tr><td colSpan={6}><div className="empty-state"><div className="empty-icon">📋</div><p>No permissions found</p></div></td></tr>
                      ) : permissions.map(p => (
                        <tr key={p.id}>
                          <td>#{p.id}</td>
                          <td>{p.studentName || p.student?.fullName || '—'}</td>
                          <td>{p.type}</td>
                          <td style={{maxWidth:200,overflow:'hidden',textOverflow:'ellipsis',whiteSpace:'nowrap'}}>{p.reason}</td>
                          <td><span className={`badge ${statusColor(p.status)}`}>{p.status}</span></td>
                          <td>{p.expiryTime ? new Date(p.expiryTime).toLocaleString() : '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </>
        )}

        {/* ── USERS ── */}
        {activeTab === 'users' && (
          <>
            <div className="page-header">
              <h2>User Management</h2>
              <p>All registered users in the system</p>
            </div>
            <div className="panel">
              <div className="panel-body">
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Roll No</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {users.length === 0 ? (
                        <tr><td colSpan={7}><div className="empty-state"><div className="empty-icon">👥</div><p>No users found</p></div></td></tr>
                      ) : users.map(u => (
                        <tr key={u.userId}>
                          <td>#{u.userId}</td>
                          <td><strong>{u.fullName}</strong></td>
                          <td>{u.email}</td>
                          <td><span className="badge badge-pending">{u.role}</span></td>
                          <td>{u.rollNumber || '—'}</td>
                          <td><span className={`badge ${u.isActive ? 'badge-approved' : 'badge-rejected'}`}>{u.isActive ? 'Active' : 'Inactive'}</span></td>
                          <td style={{display:'flex',gap:6,flexWrap:'wrap'}}>
                            <button className="btn btn-sm" style={{fontSize:'0.72rem',padding:'3px 8px'}} onClick={() => toggleStatus(u.userId)}>{u.isActive ? 'Deactivate' : 'Activate'}</button>
                            {u.accountLocked && <button className="btn btn-success btn-sm" style={{fontSize:'0.72rem',padding:'3px 8px'}} onClick={() => unlockAccount(u.userId)}>🔓 Unlock</button>}
                            <button className="btn btn-sm" style={{fontSize:'0.72rem',padding:'3px 8px',background:'#C9973A',color:'white',border:'none',borderRadius:4,cursor:'pointer'}} onClick={() => resetPassword(u.userId)}>🔑 Reset</button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </>
        )}

        {/* ── FRAUD ── */}
        {activeTab === 'fraud' && (
          <>
            <div className="page-header">
              <h2>Fraud Detection</h2>
              <p>Risk profiles and security alerts</p>
            </div>

            <div className="stat-grid">
              <div className="stat-card danger"><div className="stat-label">Unresolved Alerts</div><div className="stat-value">{fraudData?.unresolvedAlerts ?? alerts.length}</div></div>
              <div className="stat-card"><div className="stat-label">High Risk Alerts</div><div className="stat-value" style={{color:'#B91C1C'}}>{fraudData?.highRiskAlerts ?? '—'}</div></div>
              <div className="stat-card gold"><div className="stat-label">Medium Risk</div><div className="stat-value">{fraudData?.mediumRiskAlerts ?? '—'}</div></div>
              <div className="stat-card danger"><div className="stat-label">High Risk Students</div><div className="stat-value">{fraudData?.highRiskStudents ?? '—'}</div></div>
            </div>

            <div className="two-col">
              <div className="panel">
                <div className="panel-header"><h3>🚨 Active Alerts</h3></div>
                <div className="panel-body">
                  {alerts.length === 0 ? (
                    <div className="empty-state"><div className="empty-icon">✅</div><p>No active alerts</p></div>
                  ) : alerts.map(a => (
                    <div key={a.alertId} style={styles.alertItem}>
                      <div style={styles.alertLeft}>
                        <span className={`badge badge-${a.riskLevel.toLowerCase()}`}>{a.riskLevel}</span>
                        <div style={styles.alertName}>{a.studentName}</div>
                        <div style={styles.alertMsg}>{a.message}</div>
                        <div style={styles.alertTime}>{a.createdAt ? new Date(a.createdAt).toLocaleString() : ''}</div>
                      </div>
                      <button className="btn btn-success btn-sm" onClick={() => resolveAlert(a.alertId)}>✓ Resolve</button>
                    </div>
                  ))}
                </div>
              </div>

              <div className="panel">
                <div className="panel-header"><h3>📊 Risk Profiles</h3></div>
                <div className="panel-body">
                  {riskProfiles.length === 0 ? (
                    <div className="empty-state"><div className="empty-icon">🛡️</div><p>No risk data yet</p></div>
                  ) : riskProfiles.slice(0,10).map(p => (
                    <div key={p.studentId} style={styles.riskItem}>
                      <div style={styles.riskLeft}>
                        <div style={styles.riskName}>{p.studentName}</div>
                        <div style={styles.riskRoll}>{p.studentRoll}</div>
                        <div className="risk-meter">
                          <div className="risk-bar-bg">
                            <div className="risk-bar-fill" style={{width:`${p.currentScore}%`, background: riskColor(p.currentScore)}} />
                          </div>
                        </div>
                      </div>
                      <div style={{textAlign:'right'}}>
                        <div style={{fontWeight:700, color: riskColor(p.currentScore), fontSize:'1.1rem'}}>{p.currentScore.toFixed(1)}</div>
                        <span className={`badge badge-${p.riskLevel.toLowerCase()}`}>{p.riskLevel}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </>
        )}
      </main>
    </div>
  );
}

const styles = {
  alertItem: { display:'flex', alignItems:'center', justifyContent:'space-between', padding:'14px 0', borderBottom:'1px solid #F5EAEA' },
  alertLeft: { flex:1, marginRight:16 },
  alertName: { fontWeight:600, fontSize:'0.9rem', marginTop:4 },
  alertMsg: { fontSize:'0.8rem', color:'#666', marginTop:2 },
  alertTime: { fontSize:'0.72rem', color:'#aaa', marginTop:2 },
  riskItem: { display:'flex', alignItems:'center', justifyContent:'space-between', padding:'12px 0', borderBottom:'1px solid #F5EAEA', gap:16 },
  riskLeft: { flex:1 },
  riskName: { fontWeight:600, fontSize:'0.9rem' },
  riskRoll: { fontSize:'0.75rem', color:'#888', marginBottom:6 },
};