// src/api/apiService.js
import axios from 'axios';

const authApi = axios.create({
  baseURL: '/api',
});

authApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

authApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('empName');
      alert('인증이 만료되었거나 유효하지 않습니다. 다시 로그인해주세요.');
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export const login = async (empId, password) => {
  const response = await axios.post('/api/auth/login', { empId, password });
  return response.data;
};

export const getDepartments = async () => {
  const response = await authApi.get('/departments');
  return response.data;
};

export const getPositions = async () => {
  const response = await authApi.get('/positions');
  return response.data;
};

export const getEmployees = async () => {
  const response = await authApi.get('/employees');
  return response.data;
};

export default authApi;