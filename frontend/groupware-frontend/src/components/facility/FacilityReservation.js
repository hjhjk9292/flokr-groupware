import React, { useState, useEffect } from 'react';
import { Building2, Users, MapPin, Calendar, Clock, X, Edit2, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import { facilityApi } from '../../api/facilityApi';
import { isAuthenticated, handleAuthError } from '../../utils/authUtils';
import './FacilityReservation.css';

const FacilityReservation = ({ userData, initialTab = 'facilities' }) => {
 const [facilities, setFacilities] = useState([]);
 const [reservations, setReservations] = useState([]);
 const [activeTab, setActiveTab] = useState(initialTab);
 const [showReserveModal, setShowReserveModal] = useState(false);
 const [showEditModal, setShowEditModal] = useState(false);
 const [selectedFacility, setSelectedFacility] = useState(null);
 const [selectedReservation, setSelectedReservation] = useState(null);
 const [loading, setLoading] = useState(false);
 const [error, setError] = useState(null);
 const [searchTerm, setSearchTerm] = useState('');
 const [reservationForm, setReservationForm] = useState({
   facilityNo: '',
   startDate: '',
   startTime: '',
   endTime: '',
   purpose: ''
 });

 const purposeOptions = [
   '회의',
   '교육',
   '세미나',
   '면접',
   '기타'
 ];

 const timeOptions = [
   '09:00', '10:00', '11:00', '12:00', '13:00', '14:00', 
   '15:00', '16:00', '17:00', '18:00', '19:00', '20:00'
 ];

 // 현재 시간 기준으로 사용 가능한 시간대 계산
 const getAvailableTimeOptions = (selectedDate) => {
   const today = new Date();
   const isToday = selectedDate === today.toISOString().split('T')[0];
   
   if (!isToday) {
     return timeOptions; // 오늘이 아니면 모든 시간 선택 가능
   }
   
   const currentHour = today.getHours();
   const currentMinute = today.getMinutes();
   
   return timeOptions.filter(time => {
     const [hour] = time.split(':').map(Number);
     return hour > currentHour || (hour === currentHour && currentMinute < 30);
   });
 };

 // 시작 시간 이후의 종료 시간 옵션 계산
 const getAvailableEndTimeOptions = (selectedDate, startTime) => {
   const availableTimes = getAvailableTimeOptions(selectedDate);
   
   if (!startTime) return availableTimes;
   
   const [startHour] = startTime.split(':').map(Number);
   
   return timeOptions.filter(time => {
     const [hour] = time.split(':').map(Number);
     return hour > startHour;
   });
 };

 // 현재 시간 기준 기본값 계산
 const getDefaultTimeValues = () => {
   const now = new Date();
   const currentHour = now.getHours();
   const currentMinute = now.getMinutes();
   
   // 현재 시간이 업무 시간 이전이면 9시부터
   if (currentHour < 9) {
     return { startTime: '09:00', endTime: '10:00' };
   }
   
   // 현재 시간이 업무 시간 이후면 내일 9시부터
   if (currentHour >= 18) {
     return { startTime: '09:00', endTime: '10:00' };
   }
   
   // 현재 시간 기준으로 다음 정시 계산
   let nextHour = currentHour;
   if (currentMinute >= 30) {
     nextHour += 1;
   }
   
   // 업무 시간 내에서만
   if (nextHour >= 18) {
     return { startTime: '09:00', endTime: '10:00' };
   }
   
   const startTime = `${nextHour.toString().padStart(2, '0')}:00`;
   const endHour = Math.min(nextHour + 1, 18);
   const endTime = `${endHour.toString().padStart(2, '0')}:00`;
   
   return { startTime, endTime };
 };

 // 기본 날짜 계산 (오늘이 업무 시간 이후면 내일)
 const getDefaultDate = () => {
   const now = new Date();
   const currentHour = now.getHours();
   
   if (currentHour >= 18) {
     // 업무 시간 이후면 내일로 설정
     const tomorrow = new Date(now);
     tomorrow.setDate(tomorrow.getDate() + 1);
     return tomorrow.toISOString().split('T')[0];
   }
   
   return now.toISOString().split('T')[0];
 };

 const fetchFacilities = async () => {
   try {
     setLoading(true);
     setError(null);
     
     const response = await facilityApi.getFacilities();
     
     if (Array.isArray(response)) {
       setFacilities(response);
     } else if (response && response.data && Array.isArray(response.data)) {
       setFacilities(response.data);
     } else {
       setFacilities([]);
     }
   } catch (error) {
     console.error('시설 조회 중 오류:', error);
     setError(`시설 목록을 가져올 수 없습니다: ${error.message}`);
     setFacilities([]);
     
     if (handleAuthError(error)) {
       return;
     }
   } finally {
     setLoading(false);
   }
 };

 const fetchMyReservations = async () => {
   try {
     setLoading(true);
     setError(null);
     const response = await facilityApi.getMyReservations();
     
     if (Array.isArray(response)) {
       setReservations(response);
     } else if (response && response.data && Array.isArray(response.data)) {
       setReservations(response.data);
     } else {
       setReservations([]);
     }
   } catch (error) {
     console.error('내 예약 조회 오류:', error);
     
     if (handleAuthError(error)) {
       return;
     }
     
     setError('예약 목록을 가져오는데 실패했습니다.');
     setReservations([]);
   } finally {
     setLoading(false);
   }
 };

 const handleReservation = async () => {
   if (!reservationForm.startDate || !reservationForm.startTime || !reservationForm.endTime || !reservationForm.purpose) {
     alert('모든 필드를 입력해주세요.');
     return;
   }

   if (reservationForm.startTime >= reservationForm.endTime) {
     alert('종료 시간은 시작 시간보다 늦어야 합니다.');
     return;
   }

   try {
     setLoading(true);
     
     const startDateTime = `${reservationForm.startDate}T${reservationForm.startTime}:00`;
     const endDateTime = `${reservationForm.startDate}T${reservationForm.endTime}:00`;
     
     const reservationData = {
       facilityNo: selectedFacility.facilityNo,
       startTime: startDateTime,
       endTime: endDateTime,
       purpose: reservationForm.purpose
     };
     
     await facilityApi.createReservation(reservationData);
     alert('예약이 신청되었습니다. 승인을 기다려주세요.');
     
     setShowReserveModal(false);
     resetReservationForm();
     await fetchMyReservations();
   } catch (error) {
     console.error('예약 신청 오류:', error);
     
     if (handleAuthError(error)) {
       return;
     }
     
     alert(error.message);
   } finally {
     setLoading(false);
   }
 };

 const handleEditReservation = async () => {
   if (!reservationForm.startDate || !reservationForm.startTime || !reservationForm.endTime || !reservationForm.purpose) {
     alert('모든 필드를 입력해주세요.');
     return;
   }

   if (reservationForm.startTime >= reservationForm.endTime) {
     alert('종료 시간은 시작 시간보다 늦어야 합니다.');
     return;
   }

   try {
     setLoading(true);
     
     const startDateTime = `${reservationForm.startDate}T${reservationForm.startTime}:00`;
     const endDateTime = `${reservationForm.startDate}T${reservationForm.endTime}:00`;
     
     await facilityApi.updateMyReservation(selectedReservation.reservationNo, {
       facilityNo: selectedReservation.facilityNo,
       startTime: startDateTime,
       endTime: endDateTime,
       purpose: reservationForm.purpose
     });
     
     alert('예약이 수정되었습니다.');
     
     setShowEditModal(false);
     resetReservationForm();
     await fetchMyReservations();
     
   } catch (error) {
     console.error('예약 수정 오류:', error);
     
     if (handleAuthError(error)) {
       return;
     }
     
     alert(`예약 수정에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
   } finally {
     setLoading(false);
   }
 };

 const handleCancelReservation = async (reservationNo) => {
   if (!window.confirm('정말로 이 예약을 취소하시겠습니까?')) {
     return;
   }

   try {
     setLoading(true);
     await facilityApi.cancelReservation(reservationNo);
     alert('예약이 취소되었습니다.');
     await fetchMyReservations();
   } catch (error) {
     console.error('예약 취소 오류:', error);
     
     if (handleAuthError(error)) {
       return;
     }
     
     alert(`예약 취소에 실패했습니다: ${error.message || '알 수 없는 오류'}`);
   } finally {
     setLoading(false);
   }
 };

 const resetReservationForm = () => {
   setReservationForm({
     facilityNo: '',
     startDate: '',
     startTime: '',
     endTime: '',
     purpose: ''
   });
   setSelectedFacility(null);
   setSelectedReservation(null);
 };

 // 시작 시간 변경 시 종료 시간 자동 조정
 const handleStartTimeChange = (startTime) => {
   const updatedForm = { ...reservationForm, startTime };
   
   // 시작 시간이 종료 시간보다 늦거나 같으면 종료 시간을 1시간 뒤로 조정
   if (startTime >= reservationForm.endTime) {
     const [startHour] = startTime.split(':').map(Number);
     const nextHour = Math.min(startHour + 1, 20); // 최대 20시까지
     updatedForm.endTime = `${nextHour.toString().padStart(2, '0')}:00`;
   }
   
   setReservationForm(updatedForm);
 };

 // 날짜 변경 시 시간 옵션 재계산
 const handleDateChange = (selectedDate) => {
   const updatedForm = { ...reservationForm, startDate: selectedDate };
   
   const availableTimes = getAvailableTimeOptions(selectedDate);
   
   // 현재 선택된 시작 시간이 사용 불가능하면 첫 번째 사용 가능한 시간으로 변경
   if (availableTimes.length > 0 && !availableTimes.includes(reservationForm.startTime)) {
     updatedForm.startTime = availableTimes[0];
     
     // 종료 시간도 자동 조정
     const [startHour] = availableTimes[0].split(':').map(Number);
     const nextHour = Math.min(startHour + 1, 20);
     updatedForm.endTime = `${nextHour.toString().padStart(2, '0')}:00`;
   }
   
   setReservationForm(updatedForm);
 };

 useEffect(() => {
   if (!isAuthenticated()) {
     setError('로그인이 필요합니다.');
     return;
   }

   const loadInitialData = async () => {
     await Promise.all([
       fetchFacilities(),
       fetchMyReservations()
     ]);
   };

   loadInitialData();
 }, []);

 useEffect(() => {
   const urlParams = new URLSearchParams(window.location.search);
   const tabParam = urlParams.get('tab');
   
   if (tabParam === 'reservations') {
     setActiveTab('reservations');
   } else {
     setActiveTab('facilities');
   }
 }, []);

 const handleTabChange = (tabName) => {
   setActiveTab(tabName);
   
   const url = new URL(window.location);
   url.searchParams.set('tab', tabName);
   window.history.pushState({}, '', url);
   
   if (tabName === 'reservations') {
     fetchMyReservations();
   } else if (tabName === 'facilities') {
     fetchFacilities();
   }
 };

 const formatDateTime = (dateTime) => {
   if (!dateTime) return '';
   try {
     return new Date(dateTime).toLocaleString('ko-KR', {
       year: 'numeric',
       month: '2-digit',
       day: '2-digit',
       hour: '2-digit',
       minute: '2-digit'
     });
   } catch (error) {
     return dateTime;
   }
 };

 const getStatusBadge = (status) => {
   switch (status) {
     case 'PENDING':
       return (
         <span className="status-badge status-pending">
           <AlertCircle className="status-icon" />
           승인 대기
         </span>
       );
     case 'APPROVED':
       return (
         <span className="status-badge status-approved">
           <CheckCircle className="status-icon" />
           승인 완료
         </span>
       );
     case 'CANCELED':
       return (
         <span className="status-badge status-canceled">
           <XCircle className="status-icon" />
           승인 거절
         </span>
       );
     default:
       return (
         <span className="status-badge status-pending">
           <AlertCircle className="status-icon" />
           승인 대기
         </span>
       );
   }
 };

 const getFacilityIcon = (facilityName) => {
   return <Building2 className="facility-type-icon" />;
 };

 const filteredFacilities = facilities.filter(facility => 
   !searchTerm || facility.facilityName.toLowerCase().includes(searchTerm.toLowerCase())
 );

 const openReserveModal = (facility) => {
   setSelectedFacility(facility);
   
   const defaultDate = getDefaultDate();
   const { startTime, endTime } = getDefaultTimeValues();
   
   setReservationForm({
     facilityNo: facility.facilityNo,
     startDate: defaultDate,
     startTime: startTime,
     endTime: endTime,
     purpose: '회의'
   });
   setShowReserveModal(true);
 };

 const openEditModal = (reservation) => {
   setSelectedReservation(reservation);
   
   const startDate = new Date(reservation.startTime);
   const endDate = new Date(reservation.endTime);
   
   setReservationForm({
     facilityNo: reservation.facilityNo,
     startDate: startDate.toISOString().split('T')[0],
     startTime: startDate.toTimeString().slice(0, 5),
     endTime: endDate.toTimeString().slice(0, 5),
     purpose: reservation.purpose || '회의'
   });
   setShowEditModal(true);
 };

 const canModifyReservation = (reservation) => {
   return reservation.status === 'PENDING';
 };

 const canCancelReservation = (reservation) => {
   return reservation.status === 'PENDING' || reservation.status === 'APPROVED';
 };

 if (!isAuthenticated()) {
   return (
     <div className="user-facility-page">
       <div className="user-facility-container">
         <div className="auth-required">
           <Building2 className="auth-icon" />
           <h2>로그인이 필요합니다</h2>
           <p>시설 예약 서비스를 이용하려면 로그인해주세요.</p>
         </div>
       </div>
     </div>
   );
 }

 return (
   <div className="user-facility-page">
     <div className="user-facility-container">
       <div className="user-facility-header">
         <h1>
           <i className="fa-solid fa-calendar-check"></i>
           시설 예약
         </h1>
         <p>회사 시설을 예약하고 관리하세요</p>
       </div>

       <div className="facility-tabs">
         <button
           className={`tab-button ${activeTab === 'facilities' ? 'active' : ''}`}
           onClick={() => handleTabChange('facilities')}
         >
           <Building2 className="tab-icon" />
           시설 예약
         </button>
         <button
           className={`tab-button ${activeTab === 'reservations' ? 'active' : ''}`}
           onClick={() => handleTabChange('reservations')}
         >
           <Calendar className="tab-icon" />
           예약 내역
         </button>
       </div>

       {error && (
         <div className="alert alert-error">
           <span>{error}</span>
         </div>
       )}

       {activeTab === 'facilities' && (
         <div className="tab-content">
           <div className="filter-container">
             <div className="filter-row">
               <div className="filter-item">
                 <label>시설명 검색</label>
                 <input
                   type="text"
                   value={searchTerm}
                   onChange={(e) => setSearchTerm(e.target.value)}
                   placeholder="시설명으로 검색..."
                   className="form-control"
                 />
               </div>
             </div>
           </div>

           {loading ? (
             <div className="loading-container">
               <div className="loading-spinner"></div>
               <p>로딩 중...</p>
             </div>
           ) : (
             <div className="facility-grid">
               {filteredFacilities.length === 0 ? (
                 <div className="empty-state">
                   <Building2 className="empty-icon" />
                   <p>등록된 시설이 없습니다.</p>
                 </div>
               ) : (
                 filteredFacilities.map((facility) => (
                   <div key={facility.facilityNo} className="facility-card">
                     <div className="facility-icon-container">
                       {getFacilityIcon(facility.facilityName)}
                       <div className="facility-status">
                         <span className="status-available">예약 가능</span>
                       </div>
                     </div>
                     
                     <div className="facility-content">
                       <h3 className="facility-name">{facility.facilityName}</h3>
                       
                       <div className="facility-info">
                         {facility.capacity && (
                           <p><Users className="info-icon" /> 최대 {facility.capacity}명</p>
                         )}
                         
                         {facility.facilityLocation && (
                           <p><MapPin className="info-icon" /> {facility.facilityLocation}</p>
                         )}
                         
                         {facility.description && (
                           <p className="facility-description">{facility.description}</p>
                         )}
                       </div>
                       
                       <div className="facility-actions">
                         <button
                           onClick={() => openReserveModal(facility)}
                           className="btn btn-primary"
                           disabled={loading}
                           type="button"
                         >
                           <Calendar className="btn-icon" />
                           예약하기
                         </button>
                       </div>
                     </div>
                   </div>
                 ))
               )}
             </div>
           )}
         </div>
       )}

       {activeTab === 'reservations' && (
         <div className="tab-content">
           {loading ? (
             <div className="loading-container">
               <div className="loading-spinner"></div>
               <p>로딩 중...</p>
             </div>
           ) : (
             <div className="table-responsive">
               <table className="reservation-table">
                 <thead>
                   <tr>
                     <th>시설명</th>
                     <th>예약 시간</th>
                     <th>사용 목적</th>
                     <th>예약 상태</th>
                     <th>관리</th>
                   </tr>
                 </thead>
                 <tbody>
                   {reservations.length === 0 ? (
                     <tr>
                       <td colSpan="5" className="text-center">
                         예약 내역이 없습니다.
                       </td>
                     </tr>
                   ) : (
                     reservations.map((reservation) => (
                       <tr key={reservation.reservationNo}>
                         <td>
                           <div className="facility-cell">
                             <div className="facility-name">{reservation.facilityName}</div>
                           </div>
                         </td>
                         <td>
                           <div className="reservation-time">
                             <div className="start-time">
                               {formatDateTime(reservation.startTime)}
                             </div>
                             <div className="time-divider">~</div>
                             <div className="end-time">
                               {formatDateTime(reservation.endTime)}
                             </div>
                           </div>
                         </td>
                         <td>
                           <div className="purpose-cell">{reservation.purpose}</div>
                         </td>
                         <td>{getStatusBadge(reservation.status)}</td>
                         <td>
                           <div className="action-buttons">
                             {canModifyReservation(reservation) && (
                               <button
                                 onClick={() => openEditModal(reservation)}
                                 className="action-btn edit-btn"
                                 disabled={loading}
                                 type="button"
                                 title="승인 대기 중인 예약만 수정 가능합니다"
                               >
                                 <Edit2 className="btn-icon" />
                                 수정
                               </button>
                             )}
                             {canCancelReservation(reservation) && (
                               <button
                                 onClick={() => handleCancelReservation(reservation.reservationNo)}
                                 className="action-btn cancel-btn"
                                 disabled={loading}
                                 type="button"
                               >
                                 <X className="btn-icon" />
                                 취소
                               </button>
                             )}
                           </div>
                         </td>
                       </tr>
                     ))
                   )}
                 </tbody>
               </table>
             </div>
           )}
         </div>
       )}
     </div>

     {showReserveModal && selectedFacility && (
       <div className="modal-overlay" onClick={(e) => {
         if (e.target === e.currentTarget) {
           setShowReserveModal(false);
         }
       }}>
         <div className="modal-content">
           <div className="modal-header">
             <h2>{selectedFacility.facilityName} 예약</h2>
             <button 
               onClick={() => setShowReserveModal(false)} 
               className="modal-close"
               type="button"
             >
               ×
             </button>
           </div>
           <div className="modal-body">
             <div className="facility-info-section">
               <div className="facility-details">
                 {selectedFacility.capacity && (
                   <div className="detail-item">
                     <Users className="detail-icon" />
                     <span>최대 {selectedFacility.capacity}명</span>
                   </div>
                 )}
                 {selectedFacility.facilityLocation && (
                   <div className="detail-item">
                     <MapPin className="detail-icon" />
                     <span>{selectedFacility.facilityLocation}</span>
                   </div>
                 )}
               </div>
               {selectedFacility.description && (
                 <p className="facility-description">{selectedFacility.description}</p>
               )}
             </div>
             
             <div className="form-section">
               <div className="form-group">
                 <label>예약 날짜 *</label>
                 <input
                   type="date"
                   value={reservationForm.startDate}
                   onChange={(e) => handleDateChange(e.target.value)}
                   className="form-control"
                   min={new Date().toISOString().split('T')[0]}
                   required
                 />
               </div>
               
               <div className="form-row">
                 <div className="form-group">
                   <label>시작 시간 *</label>
                   <select
                     value={reservationForm.startTime}
                     onChange={(e) => handleStartTimeChange(e.target.value)}
                     className="form-control"
                     required
                   >
                     {getAvailableTimeOptions(reservationForm.startDate).map(time => (
                       <option key={time} value={time}>{time}</option>
                     ))}
                   </select>
                 </div>
                 
                 <div className="form-group">
                   <label>종료 시간 *</label>
                   <select
                     value={reservationForm.endTime}
                     onChange={(e) => setReservationForm({...reservationForm, endTime: e.target.value})}
                     className="form-control"
                     required
                   >
                     {getAvailableEndTimeOptions(reservationForm.startDate, reservationForm.startTime).map(time => (
                       <option key={time} value={time}>{time}</option>
                     ))}
                   </select>
                 </div>
               </div>
               
               <div className="form-group">
                 <label>사용 목적 *</label>
                 <select
                   value={reservationForm.purpose}
                   onChange={(e) => setReservationForm({...reservationForm, purpose: e.target.value})}
                   className="form-control"
                   required
                 >
                   {purposeOptions.map(purpose => (
                     <option key={purpose} value={purpose}>{purpose}</option>
                   ))}
                 </select>
               </div>
             </div>
           </div>
           <div className="modal-footer">
             <button
               onClick={() => setShowReserveModal(false)}
               className="btn btn-secondary"
               disabled={loading}
               type="button"
             >
               취소
             </button>
             <button
               onClick={handleReservation}
               className="btn btn-primary"
               disabled={loading}
               type="button"
             >
               {loading ? '처리 중...' : '예약 신청'}
             </button>
           </div>
         </div>
       </div>
     )}

     {showEditModal && selectedReservation && (
       <div className="modal-overlay" onClick={(e) => {
         if (e.target === e.currentTarget) {
           setShowEditModal(false);
         }
       }}>
         <div className="modal-content">
           <div className="modal-header">
             <h2>{selectedReservation.facilityName} 예약 수정</h2>
             <button 
               onClick={() => setShowEditModal(false)} 
               className="modal-close"
               type="button"
             >
               ×
             </button>
           </div>
           <div className="modal-body">
             <div className="edit-notice">
               <AlertCircle className="notice-icon" />
               <p>승인 대기 중인 예약만 수정할 수 있습니다.</p>
             </div>
             
             <div className="form-section">
               <div className="form-group">
                 <label>예약 날짜 *</label>
                 <input
                   type="date"
                   value={reservationForm.startDate}
                   onChange={(e) => handleDateChange(e.target.value)}
                   className="form-control"
                   min={new Date().toISOString().split('T')[0]}
                   required
                 />
               </div>
               
               <div className="form-row">
                 <div className="form-group">
                   <label>시작 시간 *</label>
                   <select
                     value={reservationForm.startTime}
                     onChange={(e) => handleStartTimeChange(e.target.value)}
                     className="form-control"
                     required
                   >
                     {getAvailableTimeOptions(reservationForm.startDate).map(time => (
                       <option key={time} value={time}>{time}</option>
                     ))}
                   </select>
                 </div>
                 
                 <div className="form-group">
                   <label>종료 시간 *</label>
                   <select
                     value={reservationForm.endTime}
                     onChange={(e) => setReservationForm({...reservationForm, endTime: e.target.value})}
                     className="form-control"
                     required
                   >
                     {getAvailableEndTimeOptions(reservationForm.startDate, reservationForm.startTime).map(time => (
                       <option key={time} value={time}>{time}</option>
                     ))}
                   </select>
                 </div>
               </div>
               
               <div className="form-group">
                 <label>사용 목적 *</label>
                 <select
                   value={reservationForm.purpose}
                   onChange={(e) => setReservationForm({...reservationForm, purpose: e.target.value})}
                   className="form-control"
                   required
                 >
                   {purposeOptions.map(purpose => (
                     <option key={purpose} value={purpose}>{purpose}</option>
                   ))}
                 </select>
               </div>
             </div>
           </div>
           <div className="modal-footer">
             <button
               onClick={() => setShowEditModal(false)}
               className="btn btn-secondary"
               disabled={loading}
               type="button"
             >
               취소
             </button>
             <button
               onClick={handleEditReservation}
               className="btn btn-primary"
               disabled={loading}
               type="button"
             >
               {loading ? '처리 중...' : '수정 완료'}
             </button>
           </div>
         </div>
       </div>
     )}
   </div>
 );
};

export default FacilityReservation;