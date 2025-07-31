// src/App.js

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
import UserNotificationsPage from './pages/user/UserNotificationsPage';
import AdminNotificationManagement from './pages/admin/AdminNotificationManagement';

import Header from './components/common/Header';
import PrivateRoute from './components/common/PrivateRoute';
import { NotificationProvider } from './context/NotificationContext';

import {
  isAuthenticated as checkAuth,
  getUserData,
  clearAuthData,
  setAuthData,
} from './utils/authUtils';

// 메인 앱 컴포넌트 (NotificationProvider 내부)
function AppContent() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const isAdmin = (user) => {
    return user?.role === 'ADMIN' || user?.isAdmin === 'Y';
  };

  useEffect(() => {
    const initAuth = async () => {
      if (checkAuth()) {
        const user = getUserData();
        if (user) {
          setUserData(user);
          setIsAuthenticated(true);
        } else {
          clearAuthData();
        }
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const handleLogin = (token, userData) => {
    console.log('App.js handleLogin 호출:', { token: token ? '존재' : '없음', userData });
    
    // 토큰과 사용자 데이터를 localStorage에 저장
    setAuthData(token, userData);
    
    // 상태 업데이트
    setUserData(userData);
    setIsAuthenticated(true);
    
    // 역할에 따른 라우팅
    const userRole = userData?.role === 'ADMIN' || userData?.isAdmin === 'Y' ? 'ADMIN' : 'USER';
    console.log('사용자 역할:', userRole);
    
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

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        fontSize: '18px'
      }}>
        로딩 중...
      </div>
    );
  }

  return (
    <div className="App">
      {isAuthenticated && (
        <Header 
          userData={userData} 
          onLogout={handleLogout} 
          isAdmin={isAdmin(userData)} 
        />
      )}
      
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

        {/* 관리자 라우트 */}
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
          path="/admin/notifications"
          element={
            <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="ADMIN">
              <AdminNotificationManagement userData={userData} />
            </PrivateRoute>
          }
        />

        {/* 사용자 라우트 */}
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
        <Route
          path="/user/notifications"
          element={
            <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="USER">
              <UserNotificationsPage userData={userData} />
            </PrivateRoute>
          }
        />

        <Route path="*" element={<div>404 Not Found</div>} />
      </Routes>
    </div>
  );
}

// 최상위 App 컴포넌트 - NotificationProvider로 감싸기
function App() {
  return (
    <NotificationProvider>
      <AppContent />
    </NotificationProvider>
  );
}

export default App;