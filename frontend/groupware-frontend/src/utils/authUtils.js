// src/utils/authUtils.js

export const getAuthToken = () => {
    // 'authToken' 또는 'accessToken' 키로 토큰을 가져옵니다.
    const token = localStorage.getItem('authToken') || localStorage.getItem('accessToken');
    console.log('토큰 조회:', token ? '존재함' : '없음');
    return token;
};

export const getAuthHeaders = () => {
  const token = getAuthToken();
  
  if (!token) {
    console.warn('토큰이 없습니다');
    return {
      'Content-Type': 'application/json'
    };
  }
  
  let authToken = token;
  
  if (!token.startsWith('Bearer ')) {
    authToken = `Bearer ${token}`;
  }
  
  console.log('인증 헤더 생성:', {
    sent: authToken.substring(0, 30) + '...',
    hasBearerPrefix: authToken.startsWith('Bearer ')
  });
  
  return {
    'Content-Type': 'application/json',
    'Authorization': authToken,
  };
};

export const isAuthenticated = () => {
  const token = getAuthToken();
  const userData = getUserData();
  
  const isValid = !!(token && userData);
  console.log('인증 상태 확인:', isValid ? '유효' : '무효');
  
  return isValid;
};

export const getUserData = () => {
  try {
    const userData = localStorage.getItem('userData');
    return userData ? JSON.parse(userData) : null;
  } catch (error) {
    console.error('사용자 데이터 파싱 오류:', error);
    return null;
  }
};

export const setAuthData = (token, userData) => {
  console.log('인증 데이터 저장:', { token: token ? '존재' : '없음', userData: userData ? '존재' : '없음' });
  
  if (token) {
    const cleanToken = token.replace('Bearer ', '');
    const authToken = `Bearer ${cleanToken}`;
    
    // 'authToken'과 'accessToken' 두 가지 키로 모두 저장합니다.
    localStorage.setItem('authToken', authToken);
    localStorage.setItem('accessToken', cleanToken);
    
    console.log('토큰 저장 완료:', {
      authToken: authToken.substring(0, 30) + '...',
      accessToken: cleanToken.substring(0, 30) + '...'
    });
  }
  
  if (userData) {
    localStorage.setItem('userData', JSON.stringify(userData));
    console.log('사용자 데이터 저장 완료:', userData.empName);
  }
};

export const clearAuthData = () => {
  console.log('인증 데이터 삭제');
  localStorage.removeItem('authToken');
  localStorage.removeItem('accessToken');
  localStorage.removeItem('userData');
};

export const handleAuthError = (error) => {
  console.error('인증 오류:', error);
  
  if (error.message && error.message.includes('토큰이 만료되었거나')) {
    console.log('토큰 만료 감지 - 사용자에게 알림');
    
    const token = getAuthToken();
    const userData = getUserData();
    
    if (!token || !userData) {
      console.log('토큰 또는 사용자 데이터 없음 - 로그아웃 처리');
      clearAuthData();
      
      if (window.location.pathname !== '/login') {
        alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
        window.location.href = '/login';
      }
      return true;
    } else {
      console.log('토큰과 사용자 데이터 존재 - 로그아웃 하지 않음');
      return false;
    }
  }
  
  return false;
};

export const validateToken = async () => {
  const token = getAuthToken();
  
  if (!token) {
    console.log('토큰이 없어 유효성 검사 불가');
    return false;
  }
  
  try {
    const response = await fetch('http://localhost:8080/api/auth/validate', {
      method: 'GET',
      headers: getAuthHeaders()
    });
    
    const isValid = response.ok;
    console.log('토큰 유효성 검사 결과:', isValid ? '유효' : '무효');
    
    if (!isValid) {
      clearAuthData();
    }
    
    return isValid;
  } catch (error) {
    console.error('토큰 유효성 검사 오류:', error);
    return false;
  }
};