import { useState, useEffect, useRef } from 'react';
import Sidebar from '../components/Sidebar';
import { gateApi, analyticsApi } from '../services/api';

const NAV = [
  { label: 'Gate Operations', items: [
    { id: 'scan', icon: '📷', label: 'Scan QR' },
    { id: 'history', icon: '📜', label: 'Scan History' },
  ]},
];

export default function SecurityDashboard() {
  const [activeTab, setActiveTab] = useState('scan');
  const [token, setToken] = useState('');
  const [scanType, setScanType] = useState('EXIT');
  const [result, setResult] = useState(null);
  const [scanning, setScanning] = useState(false);
  const [history, setHistory] = useState([]);
  const [stats, setStats] = useState(null);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const [cameraActive, setCameraActive] = useState(false);
  const [cameraError, setCameraError] = useState('');
  const [scannerLibLoaded, setScannerLibLoaded] = useState(false);
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const scanIntervalRef = useRef(null);
  const streamRef = useRef(null);

  useEffect(() => {
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/jsqr@1.4.0/dist/jsQR.js';
    script.onload = () => setScannerLibLoaded(true);
    script.onerror = () => setCameraError('Failed to load QR scanner library');
    document.head.appendChild(script);
    return () => {
      document.head.removeChild(script);
      stopCamera();
    };
  }, []);

  const loadHistory = async () => {
    setLoadingHistory(true);
    try { 
        const r = await analyticsApi.recentScans(); 
        setHistory(r.data || []); 
    } catch (err) {
        console.error('History error:', err);
        setHistory([]); // stop infinite loading
    } finally { 
        setLoadingHistory(false); 
    }
};

  const loadStats = async () => {
    try { const r = await analyticsApi.summary(); setStats(r.data); } catch {}
  };

  useEffect(() => {
    loadStats();
    if (activeTab === 'history') loadHistory();
  }, [activeTab]);

  const startCamera = async () => {
    setCameraError('');
    try {
      const constraints = {
        video: {
          facingMode: { ideal: 'environment' },
          width: { ideal: 640 },
          height: { ideal: 480 }
        }
      };
      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.onloadedmetadata = () => {
          videoRef.current.play();
          setCameraActive(true);
          startScanning();
        };
      } else {
        setCameraActive(true);
        startScanning();
      }
    } catch (err) {
      if (err.name === 'NotAllowedError') {
        setCameraError('Camera permission denied. Please allow camera access and try again.');
      } else if (err.name === 'NotFoundError') {
        setCameraError('No camera found on this device.');
      } else {
        setCameraError('Camera error: ' + err.message);
      }
    }
  };

  const stopCamera = () => {
    if (scanIntervalRef.current) {
      clearInterval(scanIntervalRef.current);
      scanIntervalRef.current = null;
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
      streamRef.current = null;
    }
    setCameraActive(false);
  };

  const startScanning = () => {
    scanIntervalRef.current = setInterval(() => {
      if (!videoRef.current || !canvasRef.current) return;
      const video = videoRef.current;
      const canvas = canvasRef.current;
      if (video.readyState !== video.HAVE_ENOUGH_DATA) return;

      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);

      if (window.jsQR) {
        const code = window.jsQR(imageData.data, imageData.width, imageData.height, {
          inversionAttempts: 'dontInvert'
        });
        if (code && code.data) {
          stopCamera();
          setToken(code.data);
          handleAutoScan(code.data);
        }
      }
    }, 300);
  };

  const handleAutoScan = async (qrToken) => {
    if (!qrToken.trim()) return;

    let extractedToken = qrToken.trim();
    try {
      const parsed = JSON.parse(qrToken.trim());
      if (parsed.token) extractedToken = parsed.token;
    } catch (e) {}

    setScanning(true);
    setResult(null);
    try {
      const r = await gateApi.scan({ qrToken: extractedToken, scanType });
      setResult(r.data);
      loadStats(); // always refresh stats after any scan
    } catch (err) {
      setResult({ valid: false, message: err.response?.data?.message || 'Scan failed', reason: 'ERROR' });
    } finally { setScanning(false); }
  };

  const handleManualScan = async (e) => {
    e.preventDefault();
    if (!token.trim()) return;
    await handleAutoScan(token);
  };

  const clearResult = () => {
    setResult(null);
    setToken('');
  };

  return (
    <div className="dashboard-layout">
      <Sidebar navItems={NAV} activeTab={activeTab} setActiveTab={setActiveTab} />
      <main className="main-content">

        {activeTab === 'scan' && (
          <>
            <div className="page-header">
              <h2>Gate Scanner</h2>
              <p>Scan student QR codes to verify permissions</p>
            </div>

            <div className="stat-grid" style={{gridTemplateColumns:'repeat(3,1fr)', maxWidth:480}}>
              <div className="stat-card">
                <div className="stat-label">Total Scans</div>
                <div className="stat-value">{stats?.totalGateScans ?? '—'}</div>
              </div>
              <div className="stat-card success">
                <div className="stat-label">Approved</div>
                <div className="stat-value">{stats?.approvedPermissions ?? '—'}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Pending</div>
                <div className="stat-value" style={{color:'#C9973A'}}>{stats?.pendingPermissions ?? '—'}</div>
              </div>
            </div>

            <div style={styles.scanTypeRow}>
              {['EXIT','RETURN'].map(t => (
                <button
                  key={t} type="button"
                  className={`btn ${scanType === t ? 'btn-primary' : 'btn-ghost'}`}
                  style={{flex:1, padding:'12px', fontSize:'1rem'}}
                  onClick={() => setScanType(t)}
                >
                  {t === 'EXIT' ? '🚪 Exit Campus' : '🏠 Return to Campus'}
                </button>
              ))}
            </div>

            <div className="two-col" style={{marginTop:20}}>

              <div className="panel">
                <div className="panel-header">
                  <h3>📷 Camera Scanner</h3>
                  {cameraActive && <span style={styles.liveBadge}>● LIVE</span>}
                </div>
                <div className="panel-body">
                  {!cameraActive ? (
                    <div style={styles.cameraOff}>
                      <div style={styles.cameraIcon}>📷</div>
                      <p style={styles.cameraHint}>Point camera at student's QR code</p>
                      {cameraError && (
                        <div className="alert-banner error" style={{marginBottom:16}}>{cameraError}</div>
                      )}
                      <button
                        className="btn btn-primary"
                        style={{width:'100%', padding:'13px', fontSize:'1rem'}}
                        onClick={startCamera}
                        disabled={!scannerLibLoaded}
                      >
                        {scannerLibLoaded ? '📷 Start Camera' : '⏳ Loading scanner…'}
                      </button>
                    </div>
                  ) : (
                    <div style={styles.cameraOn}>
                      <div style={styles.videoWrapper}>
                        <video
                          ref={videoRef}
                          style={styles.video}
                          playsInline
                          muted
                          autoPlay
                        />
                        <div style={styles.scanOverlay}>
                          <div style={styles.scanCornerTL} />
                          <div style={styles.scanCornerTR} />
                          <div style={styles.scanCornerBL} />
                          <div style={styles.scanCornerBR} />
                          <div style={styles.scanLine} />
                        </div>
                        <canvas ref={canvasRef} style={{display:'none'}} />
                      </div>
                      <p style={styles.scanningText}>
                        {scanning ? '⏳ Verifying…' : '🔍 Scanning for QR code…'}
                      </p>
                      <button
                        className="btn btn-danger"
                        style={{width:'100%', marginTop:12}}
                        onClick={stopCamera}
                      >
                        ⏹ Stop Camera
                      </button>
                    </div>
                  )}
                </div>
              </div>

              <div className="panel">
                <div className="panel-header"><h3>⌨️ Manual Entry</h3></div>
                <div className="panel-body">
                  <form onSubmit={handleManualScan}>
                    <div className="form-group">
                      <label>QR Token</label>
                      <input
                        className="form-control"
                        value={token}
                        onChange={e => setToken(e.target.value)}
                        placeholder="Paste QR token or JSON here…"
                        style={{fontFamily:'monospace', fontSize:'0.82rem'}}
                      />
                      <div style={{fontSize:'0.75rem', color:'#aaa', marginTop:4}}>
                        Auto-filled when camera detects a QR code
                      </div>
                    </div>
                    <div style={{display:'flex', gap:10}}>
                      <button
                        type="submit"
                        className="btn btn-primary"
                        style={{flex:1, padding:'12px'}}
                        disabled={scanning || !token.trim()}
                      >
                        {scanning ? '⏳ Verifying…' : `🔍 Verify ${scanType}`}
                      </button>
                      {token && (
                        <button type="button" className="btn btn-ghost" onClick={clearResult}>✕</button>
                      )}
                    </div>
                  </form>

                  {result && (
                    <div className={`scan-result ${result.valid ? 'valid' : 'invalid'}`} style={{marginTop:20}}>
                      <div className="scan-icon">{result.valid ? '✅' : '❌'}</div>
                      <h3 style={{color: result.valid ? '#065F46' : '#991B1B'}}>
                        {result.valid ? 'ACCESS GRANTED' : 'ACCESS DENIED'}
                      </h3>
                      <p style={{marginBottom:12}}>{result.message}</p>
                      {result.valid && (
                        <div style={styles.resultDetails}>
                          {[
                            ['Student', result.studentName],
                            ['Roll No', result.studentRoll],
                            ['Type', result.permissionType?.replace('_',' ')],
                            ['Purpose', result.leaveReason],
                            ['Expires', result.expiryTime ? new Date(result.expiryTime).toLocaleString() : '—'],
                            ['Approved By', result.approvedBy],
                          ].map(([label, value]) => (
                            <div key={label} style={styles.resultRow}>
                              <span style={styles.resultLabel}>{label}</span>
                              <span style={{...styles.resultValue, color: label==='Expires' ? '#B91C1C' : '#2D2D2D'}}>{value}</span>
                            </div>
                          ))}
                        </div>
                      )}
                      {!result.valid && (
                        <span className={`badge badge-${result.reason === 'EXPIRED' ? 'expired' : 'rejected'}`}>
                          {result.reason}
                        </span>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>

            <div className="panel" style={{marginTop:0}}>
              <div className="panel-header"><h3>📖 Quick Guide</h3></div>
              <div className="panel-body" style={{display:'grid', gridTemplateColumns:'repeat(auto-fit, minmax(200px,1fr))', gap:16}}>
                {[
                  { icon:'📷', title:'Camera Scan', desc:'Click "Start Camera", point at QR code — auto-detects and verifies instantly.' },
                  { icon:'⌨️', title:'Manual Entry', desc:'Paste the QR token or full JSON string and click Verify.' },
                  { icon:'🚪', title:'Exit', desc:'Student leaving campus — select EXIT before scanning.' },
                  { icon:'🏠', title:'Return', desc:'Student returning — select RETURN before scanning.' },
                  { icon:'✅', title:'Green = Allow', desc:'Valid permission. Allow student through.' },
                  { icon:'❌', title:'Red = Deny', desc:'Expired or invalid. Do not allow. Report if needed.' },
                ].map((g,i) => (
                  <div key={i} style={styles.guideItem}>
                    <div style={styles.guideIcon}>{g.icon}</div>
                    <div>
                      <div style={styles.guideTitle}>{g.title}</div>
                      <div style={styles.guideDesc}>{g.desc}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}

        {activeTab === 'history' && (
          <>
            <div className="page-header">
              <h2>Scan History</h2>
              <p>Recent gate scan activity — EXIT and RETURN logs</p>
            </div>
            <div className="panel">
              <div className="panel-header">
                <h3>Recent Scans</h3>
                <button className="btn btn-ghost btn-sm" onClick={loadHistory}>↻ Refresh</button>
              </div>
              <div className="panel-body">
                {loadingHistory ? <div className="loading">Loading…</div> : (
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr>
                          <th>Time</th>
                          <th>Student</th>
                          <th>Roll No</th>
                          <th>Permission</th>
                          <th>Scan Type</th>
                          <th>Outcome</th>
                          <th>Notes</th>
                        </tr>
                      </thead>
                      <tbody>
                        {history.length === 0 ? (
                          <tr><td colSpan={7}>
                            <div className="empty-state">
                              <div className="empty-icon">📜</div>
                              <p>No scan history yet</p>
                            </div>
                          </td></tr>
                        ) : history.map((s, i) => (
                          <tr key={i}>
                            <td style={{whiteSpace:'nowrap', fontSize:'0.82rem'}}>
                              {s.scannedAt ? new Date(s.scannedAt).toLocaleString() : '—'}
                            </td>
                            <td><strong>{s.studentName || '—'}</strong></td>
                            <td style={{fontSize:'0.82rem'}}>{s.studentRoll || '—'}</td>
                            <td style={{fontSize:'0.82rem'}}>{s.permissionType?.replace('_',' ') || '—'}</td>
                            <td>
                              <span className={`badge ${s.scanType === 'EXIT' ? 'badge-pending' : 'badge-approved'}`}>
                                {s.scanType === 'EXIT' ? '🚪 EXIT' : '🏠 RETURN'}
                              </span>
                            </td>
                            <td>
                              <span className={`badge badge-${s.outcome?.toLowerCase()}`}>
                                {s.outcome === 'ALLOWED' ? '✅ ALLOWED' : '❌ DENIED'}
                              </span>
                            </td>
                            <td style={{fontSize:'0.82rem', color:'#888'}}>{s.notes || '—'}</td>
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
      </main>
    </div>
  );
}

const styles = {
  scanTypeRow: { display:'flex', gap:12, marginBottom:4 },
  liveBadge: { background:'#D1FAE5', color:'#065F46', fontSize:'0.75rem', fontWeight:700, padding:'3px 10px', borderRadius:20, animation:'pulse 1.5s infinite' },
  cameraOff: { display:'flex', flexDirection:'column', alignItems:'center', padding:'32px 0', gap:16 },
  cameraIcon: { fontSize:'4rem', opacity:0.3 },
  cameraHint: { color:'#888', fontSize:'0.9rem', textAlign:'center' },
  cameraOn: { display:'flex', flexDirection:'column', alignItems:'center' },
  videoWrapper: { position:'relative', width:'100%', borderRadius:12, overflow:'hidden', background:'#000' },
  video: { width:'100%', display:'block', borderRadius:12 },
  scanOverlay: { position:'absolute', inset:0, display:'flex', alignItems:'center', justifyContent:'center' },
  scanCornerTL: { position:'absolute', top:20, left:20, width:30, height:30, borderTop:'3px solid #F0C060', borderLeft:'3px solid #F0C060', borderRadius:'4px 0 0 0' },
  scanCornerTR: { position:'absolute', top:20, right:20, width:30, height:30, borderTop:'3px solid #F0C060', borderRight:'3px solid #F0C060', borderRadius:'0 4px 0 0' },
  scanCornerBL: { position:'absolute', bottom:20, left:20, width:30, height:30, borderBottom:'3px solid #F0C060', borderLeft:'3px solid #F0C060', borderRadius:'0 0 0 4px' },
  scanCornerBR: { position:'absolute', bottom:20, right:20, width:30, height:30, borderBottom:'3px solid #F0C060', borderRight:'3px solid #F0C060', borderRadius:'0 0 4px 0' },
  scanLine: { position:'absolute', left:'10%', right:'10%', height:2, background:'rgba(240,192,96,0.7)', animation:'scanLine 2s linear infinite', boxShadow:'0 0 8px rgba(240,192,96,0.8)' },
  scanningText: { marginTop:12, color:'#7B1C1C', fontWeight:600, fontSize:'0.9rem' },
  resultDetails: { background:'rgba(255,255,255,0.7)', borderRadius:8, padding:'12px 16px', marginTop:12, textAlign:'left' },
  resultRow: { display:'flex', justifyContent:'space-between', padding:'5px 0', borderBottom:'1px solid rgba(0,0,0,0.06)', fontSize:'0.85rem' },
  resultLabel: { color:'#666', fontWeight:500 },
  resultValue: { fontWeight:600 },
  guideItem: { display:'flex', gap:12, alignItems:'flex-start' },
  guideIcon: { width:36, height:36, background:'#FDF5F5', borderRadius:8, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'1.1rem', flexShrink:0 },
  guideTitle: { fontWeight:700, fontSize:'0.85rem', color:'#4A0E0E', marginBottom:2 },
  guideDesc: { fontSize:'0.78rem', color:'#666', lineHeight:1.5 },
};
