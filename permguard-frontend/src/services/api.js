import axios from 'axios';

// Uses Vite proxy: /api → http://localhost:8080
// For production set VITE_API_URL in .env
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
});

api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

api.interceptors.response.use(
  r => r,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authApi = {
  login: (data) => api.post('/auth/login', data),
};

export const permissionsApi = {
  getMyPermissions: ()        => api.get('/permissions/my'),
  submit:           (data)    => api.post('/permissions', data),
  getPending:       ()        => api.get('/permissions/pending'),
  approve:          (id)      => api.post(`/permissions/${id}/approve`),
  reject:           (id)      => api.post(`/permissions/${id}/reject`),
  getAll:           ()        => api.get('/permissions'),
  getById:          (id)      => api.get(`/permissions/${id}`),
};

export const qrApi = {
  generate: (id) => api.post(`/qr/${id}/generate`),
  get:      (id) => api.get(`/qr/${id}`),
};

export const gateApi = {
  scan:    (data) => api.post('/gate/scan', data),
  history: (id)   => api.get(`/gate/history/${id}`),
};

export const analyticsApi = {
  summary:     () => api.get('/analytics/summary'),
  byType:      () => api.get('/analytics/by-type'),
  byStatus:    () => api.get('/analytics/by-status'),
  byDept:      () => api.get('/analytics/by-dept'),
  dailyTrend:  () => api.get('/analytics/daily-trend'),
  topStudents: () => api.get('/analytics/top-students'),
  recentScans: () => api.get('/analytics/recent-scans'),
};

export const fraudApi = {
  dashboard:    () =>   api.get('/fraud/dashboard'),
  alerts:       () =>   api.get('/fraud/alerts'),
  riskProfiles: () =>   api.get('/fraud/risk-profiles'),
  resolveAlert: (id) => api.post(`/fraud/alerts/${id}/resolve`),
};

export const adminApi = {
  getUsers:      ()       => api.get('/admin/users/students'),
  getFaculty:    ()       => api.get('/admin/users/faculty'),
  createUser:    (data)   => api.post('/admin/users', data),
  toggleStatus:  (id)     => api.put(`/admin/users/${id}/toggle`),
  unlockAccount: (id)     => api.put(`/admin/users/${id}/unlock`),
  resetPassword: (id)     => api.put(`/admin/users/${id}/reset-password`),
  getDepartments:()       => api.get('/admin/departments'),
};

export default api;