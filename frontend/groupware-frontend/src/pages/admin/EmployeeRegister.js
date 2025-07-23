import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../components/common/Header';
import './EmployeeRegister.css';

const EmployeeRegister = ({ userData, onLogout }) => {
  const navigate = useNavigate();

  const [employee, setEmployee] = useState({
    empName: '',
    hireDate: '',
    phone1: '010',
    phone2: '',
    phone3: '',
    deptNo: '',
    positionNo: ''
  });

  const [departments, setDepartments] = useState([]);
  const [positions, setPositions] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [empPreview, setEmpPreview] = useState({
    empId: '',
    email: '',
    password: ''
  });

  const currentYear = new Date().getFullYear().toString().substr(2);

  const generateEmpPreview = useCallback(() => {
    const sequenceStr = '001';
    const newEmpId = employee.deptNo + currentYear + sequenceStr;
    
    setEmpPreview({
      empId: newEmpId,
      email: `${newEmpId}@flokr.com`,
      password: `${newEmpId}init`
    });
  }, [employee.deptNo, currentYear]);

  useEffect(() => {
    fetchCommonData();
  }, []);

  useEffect(() => {
    if (employee.deptNo) {
      generateEmpPreview();
    } else {
      setEmpPreview({ empId: '', email: '', password: '' });
    }
  }, [employee.deptNo, generateEmpPreview]);

  const fetchCommonData = async () => {
    setLoading(true);
    setError('');

    try {
      const token = localStorage.getItem('accessToken');
      const headers = {};
      if (token) headers['Authorization'] = `Bearer ${token}`;

      const [deptResponse, posResponse] = await Promise.all([
        fetch('http://localhost:8080/api/departments', { headers }),
        fetch('http://localhost:8080/api/positions', { headers })
      ]);

      if (deptResponse.ok) {
        const deptData = await deptResponse.json();
        if (deptData.success) {
          setDepartments(deptData.data || []);
        } else {
          setError('부서 목록을 불러오는데 실패했습니다.');
        }
      } else if (deptResponse.status === 401) {
        setError('인증이 만료되었습니다. 다시 로그인해주세요.');
      }

      if (posResponse.ok) {
        const posData = await posResponse.json();
        if (posData.success) {
          setPositions(posData.data || []);
        } else {
          setError(prev => prev + (prev ? ' ' : '') + '직급 목록을 불러오는데 실패했습니다.');
        }
      } else if (posResponse.status === 401) {
        setError(prev => prev + (prev ? ' ' : '') + '직급 API 인증 실패');
      }

    } catch (err) {
      console.error('공통 데이터 로딩 오류:', err);
      setError('서버 연결에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setEmployee(prev => ({ ...prev, [name]: value }));
  };

  const handlePhoneChange = (e) => {
    const { name, value } = e.target;
    const numericValue = value.replace(/[^0-9]/g, '');
    setEmployee(prev => ({ ...prev, [name]: numericValue }));
  };

  const validateForm = () => {
    if (!employee.empName.trim()) {
      setError('이름을 입력해주세요.');
      return false;
    }
    if (!employee.hireDate) {
      setError('입사일을 선택해주세요.');
      return false;
    }
    if (!employee.deptNo) {
      setError('부서를 선택해주세요.');
      return false;
    }
    if (!employee.positionNo) {
      setError('직급을 선택해주세요.');
      return false;
    }
    if (employee.phone2 && (employee.phone2.length < 3 || employee.phone2.length > 4)) {
      setError('전화번호 두번째 자리는 3~4자리여야 합니다.');
      return false;
    }
    if (employee.phone3 && employee.phone3.length !== 4) {
      setError('전화번호 세번째 자리는 4자리여야 합니다.');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const token = localStorage.getItem('accessToken');
      const headers = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = `Bearer ${token}`;

      const phone = employee.phone2 && employee.phone3 
        ? `${employee.phone1}-${employee.phone2}-${employee.phone3}`
        : null;

      const employeeData = {
        empName: employee.empName.trim(),
        password: 'temp123', // 임시 값, 백엔드에서 자동으로 "사번+init"으로 변경됨
        hireDate: employee.hireDate + 'T00:00:00',
        phone: phone,
        deptNo: parseInt(employee.deptNo),
        positionNo: parseInt(employee.positionNo),
        isAdmin: 'N'
      };

      console.log('사원 등록 요청 데이터:', employeeData);

      const registerResponse = await fetch('http://localhost:8080/api/employees', {
        method: 'POST',
        headers,
        body: JSON.stringify(employeeData)
      });
      
      if (registerResponse.ok) {
        const data = await registerResponse.json();
        console.log('등록 성공 응답:', data);
        
        if (data.success) {
          const createdEmployee = data.data;
          const actualEmpId = createdEmployee?.empId || '확인필요';
          const actualPassword = actualEmpId + 'init';
          
          setSuccess(`사원이 성공적으로 등록되었습니다!\n실제 생성된 사번: ${actualEmpId}\n초기 비밀번호: ${actualPassword}`);
          
          setEmployee({
            empName: '',
            hireDate: '',
            phone1: '010',
            phone2: '',
            phone3: '',
            deptNo: '',
            positionNo: ''
          });
          setEmpPreview({ empId: '', email: '', password: '' });
          
          setTimeout(() => {
            navigate('/admin/employees/list');
          }, 4000); // 4초로 늘려서 정보 확인 시간 제공
        } else {
          setError(data.message || '사원 등록에 실패했습니다.');
        }
      } else if (registerResponse.status === 401) {
        setError('인증이 만료되었습니다. 다시 로그인해주세요.');
      } else {
        const errorData = await registerResponse.json().catch(() => ({}));
        setError(errorData.message || `서버 오류 (${registerResponse.status}): 사원 등록에 실패했습니다.`);
      }
    } catch (err) {
      console.error('사원 등록 오류:', err);
      setError('네트워크 연결에 실패했습니다. 서버 상태를 확인해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="employee-register-container">
      <Header userData={userData} onLogout={onLogout} isAdmin={true} />
      
      <div className="register-container">
        <h2 className="register-title">사원 등록</h2>
        
        {loading && (
          <div className="loading-message">
            <div className="loading-spinner"></div>
            <p>처리 중...</p>
          </div>
        )}
        
        {error && (
          <div className="error-message">
            <i className="fas fa-exclamation-circle"></i>
            {error}
          </div>
        )}
        
        {success && (
          <div className="success-message">
            <i className="fas fa-check-circle"></i>
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} id="employeeForm">
          <div className="register-row">
            <div className="register-col">
              <div className="register-group">
                <label className="register-label">이름</label>
                <input 
                  type="text" 
                  name="empName" 
                  className="register-input" 
                  value={employee.empName}
                  onChange={handleChange}
                  required 
                />
              </div>
            </div>
            <div className="register-col">
              <div className="register-group">
                <label className="register-label">입사일</label>
                <input 
                  type="date" 
                  name="hireDate" 
                  className="register-input" 
                  value={employee.hireDate}
                  onChange={handleChange}
                  required 
                />
              </div>
            </div>
          </div>
          
          <div className="register-row">
            <div className="register-col">
              <div className="register-group">
                <label className="register-label">전화번호</label>
                <div className="phone-input-group">
                  <input 
                    type="text" 
                    name="phone1" 
                    className="phone-input" 
                    maxLength="3" 
                    value={employee.phone1}
                    onChange={handlePhoneChange}
                  />
                  <span className="phone-separator">-</span>
                  <input 
                    type="text" 
                    name="phone2" 
                    className="phone-input" 
                    maxLength="4"
                    value={employee.phone2}
                    onChange={handlePhoneChange}
                  />
                  <span className="phone-separator">-</span>
                  <input 
                    type="text" 
                    name="phone3" 
                    className="phone-input" 
                    maxLength="4"
                    value={employee.phone3}
                    onChange={handlePhoneChange}
                  />
                </div>
              </div>
            </div>
            <div className="register-col">
              <div className="register-group">
                <label className="register-label">이메일</label>
                <div className="email-info">
                  {empPreview.email 
                    ? `이메일은 ${empPreview.email} 형식으로 자동 생성됩니다.`
                    : '이메일은 사번@flokr.com 형식으로 자동 생성됩니다.'
                  }
                </div>
              </div>
            </div>
          </div>
          
          <div className="register-row">
            <div className="register-col">
              <div className="register-group">
                <label className="register-label">부서</label>
                <select 
                  name="deptNo" 
                  id="deptNo" 
                  className="register-select" 
                  value={employee.deptNo}
                  onChange={handleChange}
                  required
                >
                  <option value="">부서 선택</option>
                  {departments.map(dept => (
                    <option key={dept.deptNo} value={dept.deptNo}>
                      {dept.deptName}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="register-col">
              <div className="register-group">
                <label className="register-label">직급</label>
                <select 
                  name="positionNo" 
                  className="register-select" 
                  value={employee.positionNo}
                  onChange={handleChange}
                  required
                >
                  <option value="">직급 선택</option>
                  {positions.map(position => (
                    <option key={position.positionNo} value={position.positionNo}>
                      {position.positionName}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>
          
          <div className="register-group">
            <p className="password-hint" id="passwordHint">
              {empPreview.password 
                ? `초기 비밀번호는 사번 + "init"으로 자동 생성됩니다. 예: ${empPreview.password}`
                : '초기 비밀번호는 사번 + "init"으로 자동 생성됩니다.'
              }
            </p>
            <p className="auto-generate-info">
              {empPreview.empId 
                ? `사번은 부서코드+연도+순번으로 자동 생성됩니다. 예: ${empPreview.empId}`
                : '사번은 부서코드+연도+순번으로 자동 생성됩니다.'
              }
            </p>
          </div>
          
          <input type="hidden" name="isAdmin" value="N" />
          
          <div className="register-btn-container">
            <button 
              type="submit" 
              className="register-btn-primary"
              disabled={loading}
            >
              {loading ? '등록 중...' : '등록하기'}
            </button>
            <button 
              type="button" 
              className="register-btn-secondary"
              onClick={() => navigate('/admin/employees/list')}
              disabled={loading}
            >
              목록으로
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default EmployeeRegister;