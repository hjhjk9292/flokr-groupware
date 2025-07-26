// frontend/groupware-frontend/src/App.js

import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import './App.css';

import Auth from './pages/Auth';
import AdminDashboard from './pages/admin/AdminDashboard';
import UserDashboard from './pages/user/UserDashboard';
import EmployeeRegister from './pages/admin/EmployeeRegister';
import EmployeeList from './pages/admin/EmployeeList';
import AdminFacilityManagement from './pages/admin/AdminFacilityManagement';
import UserFacilityReservation from './pages/user/UserFacilityReservation';

import Header from './components/common/Header';
import PrivateRoute from './components/common/PrivateRoute';
import { NotificationProvider } from './context/NotificationContext';

import {
  getUserData,
  getAuthToken,
  clearAuthData,
  setAuthData,
} from './utils/authUtils';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const storedUserData = localStorage.getItem('userData');

    if (token && storedUserData) {
      try {
        setUserData(JSON.parse(storedUserData));
        setIsAuthenticated(true);
      } catch (error) {
        console.error('사용자 데이터 파싱 오류:', error);
        clearAuthData();
      }
    }
    setLoading(false);
  }, []);

  const handleLogin = (data) => {
    setAuthData(data.accessToken, data);
    setUserData(data);
    setIsAuthenticated(true);
    
    // 로그인 성공 후 라우팅을 여기서 직접 처리
    const userRole = data?.role === 'ADMIN' || data?.isAdmin === 'Y' ? 'ADMIN' : 'USER';
    if (userRole === 'ADMIN') {
      navigate('/admin', { replace: true });
    } else {
      navigate('/user', { replace: true });
    }
  };

  const handleLogout = () => {
    clearAuthData();
    setUserData(null);
    setIsAuthenticated(false);
    navigate('/login', { replace: true });
  };

  const isAdmin = (user) => {
    return user?.role === 'ADMIN' || user?.isAdmin === 'Y';
  };

  if (loading) {
    return (
      <div className="app-loading">
        <div className="loading-spinner"></div>
        <p>로딩 중...</p>
      </div>
    );
  }

  return (
    <div className="App">
      <NotificationProvider userData={userData}>
        {isAuthenticated && <Header userData={userData} onLogout={handleLogout} isAdmin={isAdmin(userData)} />}
        <Routes>
          <Route
            path="/login"
            element={
              isAuthenticated ? (
                isAdmin(userData) ? (
                  <Navigate to="/admin" replace />
                ) : (
                  <Navigate to="/user" replace />
                )
              ) : (
                <Auth onLogin={handleLogin} />
              )
            }
          />
          <Route
            path="/"
            element={
              isAuthenticated ? (
                isAdmin(userData) ? (
                  <Navigate to="/admin" replace />
                ) : (
                  <Navigate to="/user" replace />
                )
              ) : (
                <Navigate to="/login" replace />
              )
            }
          />
          <Route
            path="/admin"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="ADMIN">
                <AdminDashboard userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/employees/register"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="ADMIN">
                <EmployeeRegister userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/employees/list"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="ADMIN">
                <EmployeeList userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/facilities"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="ADMIN">
                <AdminFacilityManagement userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/user"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="USER">
                <UserDashboard userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/user/facilities"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="USER">
                <UserFacilityReservation userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route path="*" element={<div>404 Not Found</div>} />
        </Routes>
      </NotificationProvider>
    </div>
  );
}

export default App;