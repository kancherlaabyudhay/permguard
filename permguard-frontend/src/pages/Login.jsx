import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authApi } from '../services/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const res = await authApi.login({ email, password });
      const d = res.data;
      login({ userId: d.userId, fullName: d.fullName, email: d.email, role: d.role, rollNumber: d.rollNumber }, d.token);
      const routes = { ADMIN: '/admin', FACULTY: '/faculty', STUDENT: '/student', SECURITY: '/security' };
      navigate(routes[d.role] || '/');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials. Please try again.');
    } finally { setLoading(false); }
  };

  return (
    <div style={styles.page}>
      {/* Left Panel */}
      <div style={styles.left}>
        <div style={styles.leftContent}>
          <div style={styles.logoArea}>
            <div style={styles.logoCircle}>PG</div>
            <h1 style={styles.logoText}>PermGuard</h1>
            <p style={styles.logoSub}>Malla Reddy University</p>
          </div>
          <div style={styles.features}>
            {[
              { icon: '🎓', text: 'Smart permission management for students' },
              { icon: '📱', text: 'QR-based gate verification system' },
              { icon: '📊', text: 'Real-time analytics & fraud detection' },
              { icon: '🛡️', text: 'AI-powered risk scoring engine' },
            ].map((f, i) => (
              <div key={i} style={styles.featureItem}>
                <span style={styles.featureIcon}>{f.icon}</span>
                <span style={styles.featureText}>{f.text}</span>
              </div>
            ))}
          </div>
          <div style={styles.decorCircle1} />
          <div style={styles.decorCircle2} />
        </div>
      </div>

      {/* Right Panel */}
      <div style={styles.right}>
        <div style={styles.loginCard}>
          <div style={styles.loginHeader}>
            <h2 style={styles.loginTitle}>Welcome back</h2>
            <p style={styles.loginSub}>Sign in to your PermGuard account</p>
          </div>

          {error && <div className="alert-banner error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Email Address</label>
              <input
                className="form-control"
                type="email" value={email} placeholder="your@mru.com"
                onChange={e => setEmail(e.target.value)} required
              />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input
                className="form-control"
                type="password" value={password} placeholder="••••••••"
                onChange={e => setPassword(e.target.value)} required
              />
            </div>
            <button
              type="submit" className="btn btn-primary"
              style={{ width: '100%', padding: '13px', fontSize: '1rem', marginTop: '8px' }}
              disabled={loading}
            >
              {loading ? '⏳ Signing in…' : '→ Sign In'}
            </button>
          </form>

          <div style={styles.roleHint}>
            <p style={styles.roleHintTitle}>Role-based access</p>
            <div style={styles.roleList}>
              {[
                { role: 'Admin', color: '#7B1C1C' },
                { role: 'Faculty', color: '#1D4ED8' },
                { role: 'Student', color: '#065F46' },
                { role: 'Security', color: '#92400E' },
              ].map(r => (
                <span key={r.role} style={{ ...styles.roleTag, background: r.color }}>{r.role}</span>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  page: { display: 'flex', minHeight: '100vh', fontFamily: "'DM Sans', sans-serif" },
  left: {
    flex: 1, background: 'linear-gradient(145deg, #4A0E0E 0%, #7B1C1C 50%, #9A2828 100%)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    position: 'relative', overflow: 'hidden',
  },
  leftContent: { padding: '48px', maxWidth: '480px', position: 'relative', zIndex: 1 },
  logoArea: { marginBottom: '48px' },
  logoCircle: {
    width: 64, height: 64, borderRadius: '18px',
    background: 'rgba(201,151,58,0.9)', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    fontSize: '1.6rem', fontWeight: 900, color: '#4A0E0E',
    fontFamily: "'Playfair Display', serif", marginBottom: '16px',
    boxShadow: '0 8px 24px rgba(0,0,0,0.3)',
  },
  logoText: {
    fontFamily: "'Playfair Display', serif",
    fontSize: '2.4rem', fontWeight: 900, color: '#F0C060',
    letterSpacing: '-1px', margin: 0,
  },
  logoSub: { color: 'rgba(255,255,255,0.5)', fontSize: '0.85rem', marginTop: '4px', letterSpacing: '2px', textTransform: 'uppercase' },
  features: { display: 'flex', flexDirection: 'column', gap: '18px' },
  featureItem: { display: 'flex', alignItems: 'center', gap: '14px' },
  featureIcon: { fontSize: '1.4rem', width: '36px', height: '36px', background: 'rgba(255,255,255,0.1)', borderRadius: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 },
  featureText: { color: 'rgba(255,255,255,0.8)', fontSize: '0.92rem', lineHeight: 1.5 },
  decorCircle1: { position: 'absolute', width: 300, height: 300, borderRadius: '50%', border: '1px solid rgba(255,255,255,0.06)', top: -80, right: -80 },
  decorCircle2: { position: 'absolute', width: 200, height: 200, borderRadius: '50%', border: '1px solid rgba(255,255,255,0.04)', bottom: 40, left: -60 },
  right: {
    width: '480px', background: '#FDF8F3',
    display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px',
  },
  loginCard: { width: '100%', maxWidth: '380px' },
  loginHeader: { marginBottom: '28px' },
  loginTitle: { fontFamily: "'Playfair Display', serif", fontSize: '2rem', color: '#4A0E0E', fontWeight: 700 },
  loginSub: { color: '#888', fontSize: '0.9rem', marginTop: '6px' },
  roleHint: { marginTop: '28px', paddingTop: '20px', borderTop: '1px solid #E8DADA' },
  roleHintTitle: { fontSize: '0.72rem', color: '#aaa', textTransform: 'uppercase', letterSpacing: '1px', marginBottom: '10px' },
  roleList: { display: 'flex', gap: '8px', flexWrap: 'wrap' },
  roleTag: { padding: '4px 12px', borderRadius: '20px', color: 'white', fontSize: '0.75rem', fontWeight: 600 },
};