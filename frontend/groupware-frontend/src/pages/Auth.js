// frontend/groupware-frontend/src/pages/Auth.js

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Auth.css';

const Auth = ({ onLogin }) => {
  const navigate = useNavigate();

  const [credentials, setCredentials] = useState({
    empId: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [rememberMe, setRememberMe] = useState(false);

  useEffect(() => {
    const savedEmpId = localStorage.getItem('rememberedEmpId');
    if (savedEmpId) {
      setCredentials(prev => ({ ...prev, empId: savedEmpId }));
      setRememberMe(true);
    }
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCredentials(prev => ({ ...prev, [name]: value }));
    if (name === 'empId' && rememberMe) {
      localStorage.setItem('rememberedEmpId', value);
    }
  };

  const handleRememberMeChange = (e) => {
    const checked = e.target.checked;
    setRememberMe(checked);
    if (checked) {
      localStorage.setItem('rememberedEmpId', credentials.empId);
    } else {
      localStorage.removeItem('rememberedEmpId');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const apiBaseUrl = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080';
      const response = await fetch(`${apiBaseUrl}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials)
      });

      const data = await response.json();
      
      console.log('API 응답 데이터:', data); 

      if (data.success && data.data) {
        // 토큰과 사용자 데이터를 분리해서 전달
        const responseData = data.data;
        
        // 토큰 추출 (여러 가능한 필드명 확인)
        const token = responseData.token || responseData.accessToken || responseData.authToken;
        
        // 실제 응답 데이터 구조 확인
        console.log('전체 응답 데이터 구조:', responseData);
        console.log('응답 데이터의 모든 키:', Object.keys(responseData));
        
        // 사용자 데이터 추출 (여러 가능한 필드명 확인)
        const userData = {
          // empNo 관련 - 여러 가능한 필드명 확인
          empNo: responseData.empNo || responseData.emp_no || responseData.employeeNo || responseData.userNo || responseData.userId,
          empId: responseData.empId || responseData.emp_id || responseData.employeeId || responseData.username || responseData.userId,
          empName: responseData.empName || responseData.emp_name || responseData.employeeName || responseData.userName || responseData.name,
          
          // 부서 관련
          deptNo: responseData.deptNo || responseData.dept_no || responseData.departmentNo,
          deptName: responseData.deptName || responseData.dept_name || responseData.departmentName || responseData.department,
          
          // 직급 관련
          positionNo: responseData.positionNo || responseData.position_no,
          positionName: responseData.positionName || responseData.position_name,
          
          // 권한 관련
          role: responseData.role || responseData.authority || responseData.auth,
          isAdmin: responseData.isAdmin || responseData.is_admin,
          
          // 기타 정보
          email: responseData.email,
          phone: responseData.phone,
          hireDate: responseData.hireDate || responseData.hire_date
        };

        console.log('분리된 토큰:', token ? '존재' : '없음');
        console.log('분리된 사용자 데이터:', userData);
        console.log('empNo 값:', userData.empNo, '타입:', typeof userData.empNo);

        // empNo 또는 empId 중 하나라도 있으면 진행
        if (token && (userData.empNo || userData.empId)) {
          // 토큰과 사용자 데이터를 분리해서 전달
          onLogin(token, userData);
        } else {
          console.error('토큰 또는 사용자 식별 정보가 없습니다:', { 
            token: token ? '존재' : '없음', 
            empNo: userData.empNo, 
            empId: userData.empId,
            전체데이터: userData 
          });
          setError('로그인 응답에 필요한 정보가 없습니다.');
        }
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
          <p>테스트 계정</p>
          <div className="test-credentials">
            <span className="test-item">
              <strong>아이디:</strong> admin
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

export default Auth;