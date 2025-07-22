// frontend/groupware-frontend/src/components/common/PrivateRoute.js

import React from 'react';
import { Navigate } from 'react-router-dom';

const PrivateRoute = ({ children, isAuthenticated, userData, requiredRole }) => {
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // 역할 기반 접근 제어
  if (requiredRole === 'ADMIN' && !(userData?.role === 'ADMIN' || userData?.isAdmin === 'Y')) {
    console.warn(`접근 거부: ${userData?.empName} (${userData?.role}) 님이 관리자 권한 없이 관리자 페이지에 접근 시도.`);
    return <Navigate to="/user" replace />; // 관리자 권한 없으면 일반 사용자 대시보드로
  }
  
  return children;
};

export default PrivateRoute;