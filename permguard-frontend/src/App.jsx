import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';
import AdminDashboard from './pages/AdminDashboard';
import FacultyDashboard from './pages/FacultyDashboard';
import StudentDashboard from './pages/StudentDashboard';
import SecurityDashboard from './pages/SecurityDashboard';

function RoleRoute({ children, role }) {
  const { user, loading } = useAuth();
  if (loading) return <div className="splash">Loading…</div>;
  if (!user) return <Navigate to="/login" />;
  if (role && user.role !== role) return <Navigate to="/login" />;
  return children;
}

function RootRedirect() {
  const { user, loading } = useAuth();
  if (loading) return <div className="splash">Loading…</div>;
  if (!user) return <Navigate to="/login" />;
  const routes = { ADMIN: '/admin', FACULTY: '/faculty', STUDENT: '/student', SECURITY: '/security' };
  return <Navigate to={routes[user.role] || '/login'} />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<RootRedirect />} />
          <Route path="/admin" element={<RoleRoute role="ADMIN"><AdminDashboard /></RoleRoute>} />
          <Route path="/faculty" element={<RoleRoute role="FACULTY"><FacultyDashboard /></RoleRoute>} />
          <Route path="/student" element={<RoleRoute role="STUDENT"><StudentDashboard /></RoleRoute>} />
          <Route path="/security" element={<RoleRoute role="SECURITY"><SecurityDashboard /></RoleRoute>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}