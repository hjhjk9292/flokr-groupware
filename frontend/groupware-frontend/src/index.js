// src/index.js
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

const root = ReactDOM.createRoot(document.getElementById('root'));

// 배포 환경에서는 StrictMode 제거 (WebSocket 중복 연결 방지)
if (process.env.NODE_ENV === 'development') {
  root.render(
    <React.StrictMode>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </React.StrictMode>
  );
} else {
  // 배포 환경에서는 StrictMode 없이 렌더링
  root.render(
    <BrowserRouter>
      <App />
    </BrowserRouter>
  );
}

reportWebVitals();