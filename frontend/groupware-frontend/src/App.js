// frontend/groupware-frontend/src/App.js

import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

// 새로운 컨벤션에 맞게 임포트 경로 및 이름 변경
import Auth from './pages/Auth';
import AdminDashboard from './pages/admin/AdminDashboard';
import UserDashboard from './pages/user/UserDashboard';

import EmployeeRegister from './pages/admin/EmployeeRegister';
import EmployeeList from './pages/admin/EmployeeList';

import PrivateRoute from './components/common/PrivateRoute'; // PrivateRoute는 재사용 컴포넌트

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const storedUserData = localStorage.getItem('userData');

    if (token && storedUserData) {
      try {
        setUserData(JSON.parse(storedUserData));
        setIsAuthenticated(true);
      } catch (error) {
        console.error('사용자 데이터 파싱 오류:', error);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userData');
      }
    }
    setLoading(false);
  }, []);

  const handleLogin = (data) => {
    setUserData(data);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userData');
    setUserData(null);
    setIsAuthenticated(false);
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
      <Router>
        <Routes>
          <Route
            path="/login"
            element={
              isAuthenticated ? (
                userData?.role === 'ADMIN' || userData?.isAdmin === 'Y' ? (
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
                userData?.role === 'ADMIN' || userData?.isAdmin === 'Y' ? (
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
                {/* EmployeeRegister에 userData와 onLogout prop 전달 */}
                <EmployeeRegister userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          <Route
            path="/admin/employees/list"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="ADMIN">
                {/* EmployeeList에 userData와 onLogout prop 전달 */}
                <EmployeeList userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          {/* TODO: 다른 관리자 페이지 라우트 추가 */}

          {/* 일반 사용자 라우트 */}
          <Route
            path="/user"
            element={
              <PrivateRoute isAuthenticated={isAuthenticated} userData={userData} requiredRole="USER">
                <UserDashboard userData={userData} onLogout={handleLogout} />
              </PrivateRoute>
            }
          />
          {/* TODO: 다른 사용자 페이지 라우트 추가 */}

          {/* 404 페이지 */}
          <Route path="*" element={<div>404 Not Found</div>} />
        </Routes>
      </Router>
    </div>
  );
}

export default App;