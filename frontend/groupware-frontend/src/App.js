// src/App.js
import React, { useState, useEffect } from 'react';
import LoginForm from './components/LoginForm';
import AdminDashboard from './components/AdminDashboard';
import UserDashboard from './components/UserDashboard';
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userData, setUserData] = useState(null);
  const [loading, setLoading] = useState(true);

  // 앱 시작 시 기존 토큰 확인
  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const storedUserData = localStorage.getItem('userData');

    if (token && storedUserData) {
      try {
        const parsedUserData = JSON.parse(storedUserData);
        setUserData(parsedUserData);
        setIsAuthenticated(true);
      } catch (error) {
        console.error('사용자 데이터 파싱 오류:', error);
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userData');
      }
    }
    setLoading(false);
  }, []);

  const handleLogin = (userData) => {
    setUserData(userData);
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
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
      {isAuthenticated && userData ? (
        <div>
          {/* 관리자와 일반 사용자 구분 */}
          {userData.role === 'ADMIN' || userData.isAdmin === 'Y' ? (
            <AdminDashboard userData={userData} onLogout={handleLogout} />
          ) : (
            <UserDashboard userData={userData} onLogout={handleLogout} />
          )}
        </div>
      ) : (
        <LoginForm onLogin={handleLogin} />
      )}
    </div>
  );
}

export default App;