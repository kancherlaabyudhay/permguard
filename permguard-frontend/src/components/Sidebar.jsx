import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Sidebar({ navItems, activeTab, setActiveTab }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="sidebar">
      <div className="sidebar-logo">
        <h1>PermGuard</h1>
        <p>Malla Reddy University</p>
      </div>

      <div className="sidebar-user">
        <div className="role-badge">{user?.role}</div>
        <div className="name">{user?.fullName}</div>
        <div className="email">{user?.email}</div>
        {user?.rollNumber && <div className="email">Roll: {user.rollNumber}</div>}
      </div>

      <nav className="sidebar-nav">
        {navItems.map((section, si) => (
          <div key={si}>
            {section.label && <div className="nav-section-label">{section.label}</div>}
            {section.items.map((item, ii) => (
              <div
                key={ii}
                className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
                onClick={() => setActiveTab(item.id)}
              >
                <span className="nav-icon">{item.icon}</span>
                <span>{item.label}</span>
              </div>
            ))}
          </div>
        ))}
      </nav>

      <div className="sidebar-footer">
        <button className="logout-btn" onClick={handleLogout}>⬅ Sign Out</button>
      </div>
    </div>
  );
}