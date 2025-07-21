// src/components/LoginForm.js
import React, { useState, useEffect } from 'react';
import './LoginForm.css';

const LoginForm = ({ onLogin }) => {
  const [credentials, setCredentials] = useState({
    empId: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [rememberMe, setRememberMe] = useState(false);

  // 컴포넌트 마운트 시 저장된 ID 불러오기
  useEffect(() => {
    const savedEmpId = localStorage.getItem('rememberedEmpId');
    if (savedEmpId) {
      setCredentials(prev => ({ ...prev, empId: savedEmpId }));
      setRememberMe(true);
    }
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCredentials(prev => ({
      ...prev,
      [name]: value
    }));

    // ID 저장하기가 체크된 상태에서 ID 입력 시 실시간 저장
    if (name === 'empId' && rememberMe) {
      localStorage.setItem('rememberedEmpId', value);
    }
  };

  const handleRememberMeChange = (e) => {
    const checked = e.target.checked;
    setRememberMe(checked);

    if (checked) {
      // 체크 시 현재 ID 저장
      localStorage.setItem('rememberedEmpId', credentials.empId);
    } else {
      // 체크 해제 시 저장된 ID 삭제
      localStorage.removeItem('rememberedEmpId');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials)
      });

      const data = await response.json();

      if (data.success) {
        // JWT 토큰과 사용자 정보 저장
        localStorage.setItem('accessToken', data.data.accessToken);
        localStorage.setItem('userData', JSON.stringify(data.data));

        // ID 저장하기 처리
        if (rememberMe) {
          localStorage.setItem('rememberedEmpId', credentials.empId);
        }

        onLogin(data.data);
      } else {
        setError(data.message || '로그인에 실패했습니다.');
      }
    } catch (error) {
      console.error('로그인 오류:', error);
      setError('서버 연결에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-logo">
          <img
            src="/images/Flokr_logo.png"
            alt="Flokr"
            className="logo-image"
          />
          <h1 className="logo-text">FLOKR 그룹웨어</h1>
        </div>

        <div className="login-header">
          <h2>로그인</h2>
          <p>계정 정보를 입력하여 로그인하세요</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="empId" className="form-label">
              <i className="fas fa-user form-icon"></i>
              아이디
            </label>
            <input
              type="text"
              id="empId"
              name="empId"
              value={credentials.empId}
              onChange={handleChange}
              className="form-input"
              placeholder="아이디를 입력하세요"
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">
              <i className="fas fa-lock form-icon"></i>
              비밀번호
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={credentials.password}
              onChange={handleChange}
              className="form-input"
              placeholder="비밀번호를 입력하세요"
              required
              disabled={loading}
            />
          </div>

          <div className="login-options">
            <label className="remember-me">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={handleRememberMeChange}
                disabled={loading}
              />
              <span className="checkmark"></span>
              아이디 저장
            </label>
          </div>

          {error && (
            <div className="error-message">
              <i className="fas fa-exclamation-circle"></i>
              {error}
            </div>
          )}

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? (
              <>
                <i className="fas fa-spinner fa-spin"></i>
                로그인 중...
              </>
            ) : (
              '로그인'
            )}
          </button>
        </form>

        <div className="test-account">
          <p>테스트 계정:</p>
          <div className="test-credentials">
            <span className="test-item">
              <strong>아이디:</strong> admin
            </span>
            <span className="test-item">
              <strong>비밀번호:</strong> 1234
            </span>
          </div>
        </div>

        <div className="login-footer">
          © 2025 Flokr Groupware. All rights reserved.<br/>
          <small>문의: support@flokr.com / 02-1234-5678</small>
        </div>
      </div>
    </div>
  );
};

export default LoginForm;