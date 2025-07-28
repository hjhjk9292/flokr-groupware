import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './EmployeeRegister.css';

const EmployeeRegister = ({ userData, onLogout }) => {
  const navigate = useNavigate();

  // 폼 상태
  const [employee, setEmployee] = useState({
    empName: '',
    hireDate: '',
    phone1: '010',
    phone2: '',
    phone3: '',
    deptNo: '',
    positionNo: ''
  });

  // 기본 데이터
  const [departments, setDepartments] = useState([]);
  const [positions, setPositions] = useState([]);

  // UI 상태
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // 사번 미리보기
  const [empPreview, setEmpPreview] = useState({
    empId: '',
    email: '',
    password: ''
  });

  const currentYear = new Date().getFullYear().toString().substr(2);

  // 초기 데이터 로드
  useEffect(() => {
    fetchCommonData();
  }, []);

  // 부서 선택 시 사번 미리보기
  useEffect(() => {
    if (employee.deptNo) {
      generateEmpPreview();
    } else {
      setEmpPreview({ empId: '', email: '', password: '' });
    }
  }, [employee.deptNo]);

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

      // 부서 데이터 처리
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

      // 직급 데이터 처리
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

  const generateEmpPreview = () => {
    // 클라이언트에서 사번 예시 생성 (백엔드 API가 없을 경우)
    const sequenceStr = '001'; // 예시 순번
    const newEmpId = employee.deptNo + currentYear + sequenceStr;
    
    setEmpPreview({
      empId: newEmpId,
      email: `${newEmpId}@flokr.com`,
      password: `${newEmpId}init`
    });
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

      // 전화번호 조합
      const phone = employee.phone2 && employee.phone3 
        ? `${employee.phone1}-${employee.phone2}-${employee.phone3}`
        : null;

      // 백엔드 EmployeeRequest DTO에 맞는 데이터 구성
      const employeeData = {
        empName: employee.empName.trim(),
        password: 'temp123', // 임시 비밀번호 (백엔드에서 자동 생성된 것으로 덮어씀)
        hireDate: employee.hireDate + 'T00:00:00', // LocalDateTime 형식
        phone: phone,
        deptNo: parseInt(employee.deptNo),
        positionNo: parseInt(employee.positionNo),
        isAdmin: 'N'
      };

      console.log('사원 등록 데이터:', employeeData);

      const registerResponse = await fetch('http://localhost:8080/api/employees', {
        method: 'POST',
        headers,
        body: JSON.stringify(employeeData)
      });

      console.log('등록 응답 상태:', registerResponse.status);
      
      if (registerResponse.ok) {
        const data = await registerResponse.json();
        console.log('등록 응답 데이터:', data);
        
        if (data.success) {
          setSuccess('사원이 성공적으로 등록되었습니다.');
          
          // 폼 초기화
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
          
          // 1초 후 목록 페이지로 이동
          setTimeout(() => {
            navigate('/admin/employees/list');
          }, 1000);
        } else {
          setError(data.message || '사원 등록에 실패했습니다.');
        }
      } else if (registerResponse.status === 401) {
        setError('인증이 만료되었습니다. 다시 로그인해주세요.');
      } else {
        const errorData = await registerResponse.json().catch(() => ({}));
        console.error('등록 오류 응답:', errorData);
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