/**
 * STOMP 기반 알림 시스템 클라이언트
 * 그룹웨어의 실시간 알림 처리를 담당하는 전역 모듈
 * @author YourName
 * @version 1.0.0
 */

// 중복 알림 방지를 위한 전역 변수
let lastNotificationTitle = '';
let lastNotificationTime = 0;
const NOTIFICATION_DEBOUNCE_TIME = 2000; // 2초 내에 동일 내용의 알림 방지

// 프라이빗 변수
let stompClient = null;         // STOMP 클라이언트 인스턴스
let socket = null;              // WebSocket 객체
let reconnectInterval = 5000;   // 재연결 간격 (밀리초)
let isConnecting = false;       // 연결 중 상태 플래그
let reconnectCount = 0;         // 재연결 시도 횟수
let maxReconnectAttempts = 5;   // 최대 재연결 시도 횟수
let subscribedTopics = {};      // 구독 중인 토픽 목록

// DOM 요소 캐싱 변수
let notificationBadgeElement = null;       // 알림 배지 요소
let notificationListElement = null;        // 알림 목록 요소
let notificationDropdownElement = null;    // 알림 드롭다운 요소
let notificationBtnElement = null;         // 알림 버튼 요소

/**
 * STOMP WebSocket 연결 설정
 * 기존 연결을 정리하고 새 연결 시도
 */
function connectWebSocket() {
    if (isConnecting) return;
    isConnecting = true;
    
    // 기존 연결 정리
    if (stompClient !== null) {
        try {
            stompClient.disconnect();
        } catch (e) {
            console.error("Disconnect error", e);
        }
    }
    
    try {
        console.log("STOMP 연결 시도...");
        // contextPath 동적 처리
        const contextPath = $("#contextPath").val() || '';
        socket = new SockJS(contextPath + '/ws-stomp');
        stompClient = Stomp.over(socket);
        
        // 프로덕션에서는 디버그 비활성화
        // stompClient.debug = null;
        
        // STOMP 디버그 로그 활성화 - 문제 해결 위해 유지
        stompClient.debug = function(str) {
            console.log('STOMP DEBUG:', str);
        };
        
        // 연결 시도 (하트비트 설정)
        let connectHeaders = {
            'heart-beat': '10000,10000'  // 10초 간격 하트비트 설정
        };
        console.log("STOMP 연결 시도 (하트비트 10초)..."); 
        stompClient.connect(connectHeaders, onConnected, onError);
        
    } catch (e) {
        console.error("WebSocket 연결 시도 중 오류 발생", e);
        isConnecting = false;
        scheduleReconnect();
    }
}

/**
 * 연결 성공 시 호출되는 콜백
 * 토픽 구독 및 초기 알림 목록 요청
 * @param {Object} frame STOMP 연결 프레임
 */
function onConnected(frame) {
    console.log('STOMP 연결 성공: ' + frame);
    isConnecting = false;
    reconnectCount = 0;
    
    // 전체 알림 구독
    subscribeToTopic('/topic/notification.new', handleNewNotification);
    
    // 부서별 알림 구독 (사용자 부서만)
    const deptNo = $("#userDeptNo").val();
    if (deptNo) {
        subscribeToTopic('/topic/department/' + deptNo + '/notification.new', handleNewNotification);
    }
    
    // 사용자 개인 알림 구독
    const userId = $("#loginUserId").val();
    if (userId) {
        console.log('사용자 ID:', userId, '에 대한 알림 구독 시작');
        
        // 읽지 않은 알림 목록 요청 구독
        subscribeToTopic('/user/' + userId + '/queue/notifications.unread', handleUnreadNotifications);
        
        // 새 알림 구독
        subscribeToTopic('/user/' + userId + '/queue/notification.new', handleNewNotification);
        
        // 알림 읽음 처리 응답 구독
        subscribeToTopic('/user/' + userId + '/queue/notification.read.response', handleReadResponse);
        
        // 알림 목록 요청 메시지 전송
        setTimeout(function() {
            if (stompClient && stompClient.connected) {
                console.log('초기 알림 목록 요청 전송');
                stompClient.send("/app/notification.subscribe", {}, JSON.stringify({}));
                
                // 백업: 서버 응답이 없을 경우 REST API로 읽지 않은 알림 가져오기
                setTimeout(fetchUnreadNotificationsViaREST, 2000);
            }
        }, 500);
    }
    
    // 연결 완료 이벤트 발생
    $(document).trigger('notification:connected');
}

/**
 * REST API를 통해 읽지 않은 알림 가져오기 (STOMP 백업용)
 * WebSocket 연결 실패 시 대체 수단으로 활용
 */
function fetchUnreadNotificationsViaREST() {
    if (!notificationBadgeElement) {
        notificationBadgeElement = document.getElementById('header-notification-badge');
    }
    
    if (!notificationBadgeElement || notificationBadgeElement.style.display !== 'none') return; // 이미 뱃지가 표시되어 있으면 요청하지 않음
    
    console.log('REST API로 읽지 않은 알림 요청 (STOMP 백업)');
    const contextPath = $("#contextPath").val() || '';
    
    $.ajax({
        url: contextPath + '/notificationCount',
        type: 'GET',
        timeout: 5000,
        success: function(count) {
            console.log('REST API 응답: 읽지 않은 알림 ', count, '개');
            updateUnreadCount(count);
            
            // 알림이 있으면 내용도 가져오기
            if (count > 0) {
                $.ajax({
                    url: contextPath + '/unreadNotifications',
                    type: 'GET',
                    dataType: 'json',
                    timeout: 5000,
                    success: function(notifications) {
                        updateNotificationList(notifications);
                    },
                    error: function(xhr, status, error) {
                        console.error('알림 내용 요청 실패:', status, error);
                    }
                });
            }
        },
        error: function(xhr, status, error) {
            console.error('알림 카운트 요청 실패:', status, error);
            if (status === 'timeout') {
                console.warn('서버 응답 시간 초과');
            }
        }
    });
}

/**
 * STOMP 토픽 구독 관리
 * @param {string} topic 구독할 토픽 주소
 * @param {Function} callback 메시지 수신 시 호출할 콜백 함수
 */
function subscribeToTopic(topic, callback) {
    if (stompClient && stompClient.connected && !subscribedTopics[topic]) {
        console.log('토픽 구독 시도:', topic);
        try {
            subscribedTopics[topic] = stompClient.subscribe(topic, callback);
            console.log('토픽 구독 완료:', topic);
        } catch (e) {
            console.error('토픽 구독 실패:', topic, e);
        }
    } else if (!stompClient || !stompClient.connected) {
        console.warn('STOMP 클라이언트가 연결되지 않아 구독할 수 없습니다:', topic);
    } else if (subscribedTopics[topic]) {
        console.log('이미 구독 중인 토픽:', topic);
    }
}

/**
 * 연결 오류 시 호출되는 콜백
 * @param {Object} error 발생한 오류 객체
 */
function onError(error) {
    console.error('STOMP 연결 오류: ', error);
    isConnecting = false;
    
    // 연결 상태 초기화
    stompClient = null;
    socket = null;
    
    // 연결 실패 시 재시도
    scheduleReconnect();
    
    // 사용자에게 연결 문제 알림 (선택적)
    if (reconnectCount > 3) {
        showNotificationToast('서버 연결에 문제가 있습니다. 자동으로 재연결을 시도합니다.');
    }
}

/**
 * 재연결 스케줄링
 * 지수 백오프 방식으로 재연결 간격 증가
 */
function scheduleReconnect() {
    if (reconnectCount >= maxReconnectAttempts) {
        console.error("최대 재연결 시도 횟수를 초과했습니다.");
        return;
    }
    
    reconnectCount++;
    const timeout = reconnectInterval * Math.pow(1.5, reconnectCount - 1); // 지수 백오프
    console.log(`재연결 시도 ${reconnectCount}/${maxReconnectAttempts} (${timeout}ms 후)`);
    
    setTimeout(function() {
        connectWebSocket();
    }, timeout);
}

/**
 * 읽지 않은 알림 목록 처리
 * @param {Object} message STOMP 메시지 객체
 */
function handleUnreadNotifications(message) {
    try {
        console.log('읽지 않은 알림 메시지 수신:', message);
        const notifications = JSON.parse(message.body);
        console.log('읽지 않은 알림 수신:', notifications.length + '개', notifications);
        
        // 중요: 알림 배지 업데이트
        updateUnreadCount(notifications.length);
        
        // 중요: 알림 목록 업데이트
        updateNotificationList(notifications);
    } catch (e) {
        console.error("읽지 않은 알림 처리 중 오류 발생", e);
    }
}

/**
 * 새 알림 처리
 * @param {Object} message STOMP 메시지 객체
 */
function handleNewNotification(message) {
    try {
        const notification = JSON.parse(message.body);
        console.log('새 알림 수신:', notification);
        
        // 알림 카운트 업데이트
        increaseUnreadCount();
        
        // 알림 목록에 추가
        addNewNotification(notification);
        
        // 토스트 알림 표시 - 글로벌 함수 사용
        const title = notification.title || notification.TITLE || '새 알림';
        showNotificationToast(title);
        
        // 새 알림 이벤트 발생
        $(document).trigger('notification:new', [notification]);
    } catch (e) {
        console.error("새 알림 처리 중 오류 발생", e);
    }
}

/**
 * 알림 읽음 처리 응답 처리
 * @param {Object} message STOMP 메시지 객체
 */
function handleReadResponse(message) {
    try {
        const response = JSON.parse(message.body);
        console.log('알림 읽음 처리 응답:', response);
        
        if (response.status === "success") {
            // 알림 UI 업데이트 (필요시)
        }
    } catch (e) {
        console.error("알림 읽음 처리 응답 중 오류 발생", e);
    }
}

/**
 * 알림 읽음 처리 요청 - 즉시 UI 업데이트 보장
 * @param {number} notificationNo 알림 번호
 */
function markAsRead(notificationNo) {
    console.log('알림 읽음 처리 시작:', notificationNo);
    
    // 즉시 UI 업데이트 (서버 응답 대기하지 않음)
    decreaseUnreadCount();
    
    // 알림 목록에서 읽음 상태로 표시
    updateNotificationReadStatus(notificationNo);
    
    if (!stompClient || !stompClient.connected) {
        console.warn("WebSocket 연결이 없습니다. REST API로 읽음 처리합니다.");
        
        // 대체 방법: REST API로 읽음 처리
        const contextPath = $("#contextPath").val() || '';
        $.ajax({
            url: contextPath + '/notificationRead/' + notificationNo,
            type: 'POST',
            timeout: 5000,
            success: function(response) {
                if(response.success) {
                    console.log('REST API 알림 읽음 처리 성공:', notificationNo);
                }
            },
            error: function(xhr, status, error) {
                console.error('알림 읽음 처리 요청 실패:', status, error);
            }
        });
        return;
    }
    
    stompClient.send("/app/notification.read", {}, 
        JSON.stringify({ notificationNo: notificationNo })
    );
}

/**
 * 알림 읽음 상태 UI 업데이트
 * @param {number} notificationNo 알림 번호
 */
function updateNotificationReadStatus(notificationNo) {
    if (!notificationListElement) {
        notificationListElement = document.getElementById('header-notification-list');
    }
    if (!notificationListElement) return;
    
    const notificationItem = notificationListElement.querySelector(`.header-notification-link[data-id="${notificationNo}"]`);
    if (notificationItem) {
        const parentItem = notificationItem.closest('.header-notification-item');
        if (parentItem) {
            parentItem.classList.remove('header-new');
        }
    }
}

/**
 * 알림 배지 숫자 증가
 */
function increaseUnreadCount() {
    if (!notificationBadgeElement) {
        notificationBadgeElement = document.getElementById('header-notification-badge');
    }
    if (!notificationBadgeElement) return;
    
    let count = parseInt(notificationBadgeElement.textContent || '0') + 1;
    updateUnreadCount(count);
}

/**
 * 알림 배지 숫자 감소
 */
function decreaseUnreadCount() {
    if (!notificationBadgeElement) {
        notificationBadgeElement = document.getElementById('header-notification-badge');
    }
    if (!notificationBadgeElement) return;
    
    let count = parseInt(notificationBadgeElement.textContent || '0') - 1;
    if (count < 0) count = 0;
    updateUnreadCount(count);
}

/**
 * 알림 배지 업데이트
 * @param {number} count 알림 개수
 */
function updateUnreadCount(count) {
    if (!notificationBadgeElement) {
        notificationBadgeElement = document.getElementById('header-notification-badge');
    }
    if (!notificationBadgeElement) return;
    
    console.log('알림 배지 업데이트:', count);
    
    if (count > 0) {
        // 99개 초과 시 "99+"로 표시
        notificationBadgeElement.textContent = count > 99 ? "99+" : count;
        notificationBadgeElement.style.display = 'block';
    } else {
        notificationBadgeElement.textContent = '0';
        notificationBadgeElement.style.display = 'none';
    }
}

/**
 * 알림 목록 업데이트 - 읽은/읽지 않은 알림 구분
 * @param {Array} notifications 알림 객체 배열
 */
function updateNotificationList(notifications) {
 if (!notificationListElement) {
     notificationListElement = document.getElementById('header-notification-list');
 }
 if (!notificationListElement) {
     console.error('알림 목록을 찾을 수 없습니다. 요소 ID: header-notification-list');
     return;
 }
 
 console.log('updateNotificationList 함수 실행됨! 받은 알림 배열:', notifications);
 
 // 목록 비우기
 notificationListElement.innerHTML = '';
 
 if (!notifications || notifications.length === 0) {
     console.log('알림 목록이 비어있음.');
     notificationListElement.innerHTML = '<li class="header-empty-notification">새로운 알림이 없습니다.</li>';
     return;
 }
 
 notifications.forEach(function(notification) {
     console.log('목록 생성 중인 알림 항목:', notification);
     
     const item = document.createElement('li');
     
     // 읽음 상태에 따라 클래스 추가
     const readStatus = notification.READ_STATUS || notification.readStatus || 'N';
     item.className = readStatus === 'Y' ? 'header-notification-item' : 'header-notification-item header-new';
     
     // 필드 정규화 - 대문자 또는 소문자 키 처리
     const notificationNo = notification.NOTIFICATION_NO || notification.notificationNo;
     const title = notification.TITLE || notification.title || '제목 없음';  
     const content = notification.NOTIFICATION_CONTENT || notification.content || '';
     
     // 타임스탬프 관련 필드명 확인 - 모든 가능한 필드명 체크
     // 디버깅용 - 객체의 모든 키 로깅
     console.log('notification 객체의 모든 키:', Object.keys(notification));
     
     // 가능한 모든 날짜 필드 시도
     let timestamp = notification.CREATE_DATE || notification.createDate || 
                     notification.TIMESTAMP || notification.timestamp || 
                     notification.CREATE_DT || notification.createDt ||
                     notification.REGDATE || notification.regDate ||
                     notification.CREATE_TIME || notification.createTime;
     
     console.log('찾은 타임스탬프:', timestamp);
     
     const refType = notification.REF_TYPE || notification.refType;
     const refNo = notification.REF_NO || notification.refNo;
     
     const link = document.createElement('a');
     if (refType && refNo) {
         link.href = getNotificationUrl(refType, refNo);
     } else {
         link.href = "javascript:void(0)";
     }
     
     link.className = 'header-notification-link';
     link.dataset.id = notificationNo;
     link.onclick = function(e) {
         e.preventDefault();
         markAsRead(notificationNo);
         
         if (refType && refNo) {
             setTimeout(function() {
                 window.location.href = getNotificationUrl(refType, refNo);
             }, 300);
         }
     };
     
     const titleElem = document.createElement('div');
     titleElem.className = 'header-notification-title';
     titleElem.textContent = title;
     
     const contentElem = document.createElement('div');
     contentElem.className = 'header-notification-content';
     contentElem.textContent = content;
     
     const timeElem = document.createElement('div');
     timeElem.className = 'header-notification-time';
     
     // 타임스탬프가 없는 경우 기본값 표시
     // 알림 번호를 활용하여 상대적 순서 표시 
     // (번호가 클수록 최근에 생성된 알림으로 가정)
     if (!timestamp) {
         // 알림 번호가 100만 이상인 경우 (최근 알림으로 가정)
         if (notificationNo > 1000000) {
             timeElem.textContent = '방금 전';
         } 
         // 알림 번호가 중간 범위인 경우
         else if (notificationNo > 950000) {
             timeElem.textContent = '오늘';
         }
         // 이전 알림인 경우
         else {
             timeElem.textContent = '이전';
         }
     } else {
         // 타임스탬프가 있는 경우 정상 포맷팅
         timeElem.textContent = formatDate(timestamp);
     }
     
     link.appendChild(titleElem);
     if (content) {
         link.appendChild(contentElem);
     }
     link.appendChild(timeElem);
     item.appendChild(link);
     notificationListElement.appendChild(item);
 });
}

/**
 * 새 알림 추가 - 항상 읽지 않은 상태로 추가
 * @param {Object} notification 알림 객체
 */
function addNewNotification(notification) {
    if (!notificationListElement) {
        notificationListElement = document.getElementById('header-notification-list');
    }
    if (!notificationListElement) return;
    
    // 빈 알림 메시지 제거
    const emptyNotification = notificationListElement.querySelector('.header-empty-notification');
    if (emptyNotification) {
        notificationListElement.removeChild(emptyNotification);
    }
    
    const item = document.createElement('li');
    item.className = 'header-notification-item header-new';
    
    // 필드 정규화
    const notificationNo = notification.NOTIFICATION_NO || notification.notificationNo;
    const title = notification.TITLE || notification.title;
    const content = notification.NOTIFICATION_CONTENT || notification.content || '';
    const refType = notification.REF_TYPE || notification.refType;
    const refNo = notification.REF_NO || notification.refNo;
    
    const link = document.createElement('a');
    if (refType && refNo) {
        link.href = getNotificationUrl(refType, refNo);
    } else {
        link.href = "javascript:void(0)";
    }
    
    link.className = 'header-notification-link';
    link.dataset.id = notificationNo;
    link.onclick = function(e) {
        e.preventDefault();
        markAsRead(notificationNo);
        
        if (refType && refNo) {
            setTimeout(function() {
                window.location.href = getNotificationUrl(refType, refNo);
            }, 300);
        }
    };
    
    const titleElem = document.createElement('div');
    titleElem.className = 'header-notification-title';
    titleElem.textContent = title;
    
    const contentElem = document.createElement('div');
    contentElem.className = 'header-notification-content';
    contentElem.textContent = content;
    
    const timeElem = document.createElement('div');
    timeElem.className = 'header-notification-time';
    timeElem.textContent = '방금 전';
    
    link.appendChild(titleElem);
    if (content) {
        link.appendChild(contentElem);
    }
    link.appendChild(timeElem);
    item.appendChild(link);
    
    // 목록 맨 위에 추가
    notificationListElement.insertBefore(item, notificationListElement.firstChild);
}

/**
 * 알림 타입에 따른 URL 생성 함수
 * @param {string} refType 참조 타입
 * @param {string} refNo 참조 번호
 * @return {string} 생성된 URL
 */
function getNotificationUrl(refType, refNo) {
    const contextPath = $("#contextPath").val() || '';
    
    // refType 정규화 (대소문자 무시)
    const type = (refType || '').toLowerCase();
    
    switch(type) {
        case 'notice':
            return contextPath + '/noticeDetail/' + refNo;
        case 'approval':
            return contextPath + '/approval/detail/' + refNo;
        case 'task':
            return contextPath + '/task/detail/' + refNo;
        case 'schedule':
            return contextPath + '/schedule/detail/' + refNo;
        case 'facility':
            // 관리자인 경우 관리자 페이지로, 일반 사용자는 예약 페이지로 이동
            const isAdmin = $('#loginUserIsAdmin').val() === 'Y';
            if (isAdmin) {
                // 관리자는 예약 관리 탭으로 이동
                return contextPath + '/adminFacility?tab=reservationList';
            } else {
                // 일반 사용자는 내 예약 탭으로 이동
                return contextPath + '/facilityReservation?tab=myReservations';
            }
        default:
            return contextPath + '/notificationAll';
    }
}

/**
 * 알림 토스트 표시 함수
 * @param {string} message 표시할 메시지
 */
function showNotificationToast(message) {
    // 중복 알림 방지 로직
    const now = Date.now();
    if (message === lastNotificationTitle && now - lastNotificationTime < NOTIFICATION_DEBOUNCE_TIME) {
        console.log('중복 알림 방지:', message);
        return;
    }
    
    // 현재 알림 정보 저장
    lastNotificationTitle = message;
    lastNotificationTime = now;
    
    console.log('토스트 알림 표시:', message);
    
    // 이미 표시된 토스트가 있으면 제거
    const existingToasts = document.querySelectorAll('.header-notification-toast');
    existingToasts.forEach(toast => {
        toast.classList.remove('header-show');
        setTimeout(() => {
            if(toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    });
    
    const toast = document.createElement('div');
    toast.className = 'header-notification-toast';
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    // 애니메이션 적용을 위한 지연 처리
    requestAnimationFrame(function() {
        requestAnimationFrame(function() {
            toast.classList.add('header-show');
        });
    });
    
    // 5초 후 제거 (모든 페이지에서 동일하게 적용)
    setTimeout(function() {
        toast.classList.remove('header-show');
        setTimeout(function() {
            if(toast.parentNode) {
                document.body.removeChild(toast);
            }
        }, 300);
    }, 5000);
}

/**
 * 알림 발송 성공 메시지 표시 함수
 * @param {string} message 표시할 메시지
 */
function showSuccessMessage(message) {
    // 중복 알림 방지 로직
    const now = Date.now();
    if (message === lastNotificationTitle && now - lastNotificationTime < NOTIFICATION_DEBOUNCE_TIME) {
        console.log('중복 성공 메시지 방지:', message);
        return;
    }
    
    // 현재 알림 정보 저장
    lastNotificationTitle = message;
    lastNotificationTime = now;
    
    // 기존 메시지가 있으면 제거
    const existingMessage = document.querySelector('.alertify-notifier.ajs-bottom.ajs-right .ajs-message.ajs-success');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // alertify 요소들이 있는지 확인
    let notifier = document.querySelector('.alertify-notifier.ajs-bottom.ajs-right');
    
    // 없으면 생성
    if (!notifier) {
        notifier = document.createElement('div');
        notifier.className = 'alertify-notifier ajs-bottom ajs-right';
        document.body.appendChild(notifier);
    }
    
    // 메시지 요소 생성
    const messageContainer = document.createElement('div');
    messageContainer.className = 'ajs-message ajs-success ajs-visible';
    messageContainer.textContent = message;
    
    // 트랜지션 미리 설정 (바로 적용)
    messageContainer.style.transition = 'opacity 0.5s ease';
    
    // 알림 추가
    notifier.appendChild(messageContainer);
    
    // 5초 후 사라지게 설정 (모든 페이지에서 동일하게 적용)
    setTimeout(() => {
        messageContainer.style.opacity = '0';
        
        // 0.5초 후 DOM에서 제거 (애니메이션 완료 후)
        setTimeout(() => {
            if (messageContainer.parentNode) {
                messageContainer.parentNode.removeChild(messageContainer);
            }
        }, 500);
    }, 5000);
}

/**
 * 날짜 포맷팅
 * @param {string|Date} dateStr 날짜 문자열 또는 Date 객체
 * @return {string} 포맷팅된 날짜 문자열
 */
function formatDate(dateStr) {
    if(!dateStr) return ''; // '알 수 없음' 대신 빈 문자열 반환
    
    try {
        const date = new Date(dateStr);
        
        // 날짜가 유효하지 않으면 원본 내용 그대로 반환
        if(isNaN(date.getTime())) return dateStr; 
        
        const now = new Date();
        const diff = Math.floor((now - date) / 1000 / 60); // 분 단위 차이
        
        if(diff < 1) return '방금 전';
        if(diff < 60) return diff + '분 전';
        
        const hours = Math.floor(diff / 60);
        if(hours < 24) return hours + '시간 전';
        
        const days = Math.floor(hours / 24);
        if(days < 7) return days + '일 전';
        
        // 일주일 이상이면 날짜 형식으로 표시
        return date.getFullYear() + '.' + 
               (date.getMonth() + 1).toString().padStart(2, '0') + '.' + 
               date.getDate().toString().padStart(2, '0');
    } catch (e) {
        console.error('날짜 포맷팅 오류:', e);
        return dateStr; // 오류 발생 시 원본 내용 반환
    }
}

/**
 * 페이지 로드 시 알림 배지 즉시 업데이트 (STOMP 연결 전에)
 */
function updateNotificationBadgeOnLoad() {
    const contextPath = $("#contextPath").val() || '';
    
    $.ajax({
        url: contextPath + '/notificationCount',
        type: 'GET',
        timeout: 5000,
        success: function(count) {
            console.log('페이지 로드 시 알림 배지 업데이트:', count);
            updateUnreadCount(count);
        },
        error: function(xhr, status, error) {
            console.error('초기 알림 카운트 요청 실패:', status, error);
        }
    });
}

/**
 * 알림 드롭다운 토글 함수
 * @param {Event} event 클릭 이벤트 객체
 */
function toggleNotificationDropdown(event) {
    if (!notificationDropdownElement) {
        notificationDropdownElement = document.getElementById('header-notification-dropdown');
    }
    
    if (!notificationDropdownElement) return;
    
    event.preventDefault();
    event.stopPropagation();
    
    console.log("알림 버튼 클릭됨");
    notificationDropdownElement.classList.toggle('header-show');
    
    // 드롭다운이 표시되면 알림 목록 로드
    if (notificationDropdownElement.classList.contains('header-show')) {
        loadNotifications();
    }
}

/**
 * 알림 목록 로드 함수
 */
function loadNotifications() {
    const contextPath = $('#contextPath').val() || '';
    console.log("알림 목록 로드 시도");
    
    // 알림 개수 가져오기
    $.ajax({
        url: contextPath + '/notificationCount',
        type: 'GET',
        timeout: 5000,
        success: function(count) {
            console.log('읽지 않은 알림 수:', count);
            
            // 배지 업데이트
            updateUnreadCount(count);
            
            // 알림 내용 가져오기
            if(parseInt(count) > 0) {
                $.ajax({
                    url: contextPath + '/unreadNotifications',
                    type: 'GET',
                    dataType: 'json',
                    timeout: 5000,
                    success: function(notifications) {
                        updateNotificationList(notifications);
                    },
                    error: function(error) {
                        console.error('알림 목록 가져오기 오류:', error);
                    }
                });
            } else {
                // 알림이 없는 경우
                if (!notificationListElement) {
                    notificationListElement = document.getElementById('header-notification-list');
                }
                if (notificationListElement) {
                    notificationListElement.innerHTML = '<li class="header-empty-notification">새로운 알림이 없습니다.</li>';
                }
            }
        },
        error: function(error) {
            console.error('알림 개수 가져오기 오류:', error);
        }
    });
}

/**
 * 알림 발송 함수 - 내부 구현
 * @param {Object} options 알림 발송 옵션
 */
function _sendNotificationInternal(options) {
    const contextPath = $("#contextPath").val() || '';
    
    // 버튼 요소와 원본 텍스트 저장 (있는 경우)
    const $btn = $('#sendBtn');
    const originalText = $btn.length > 0 ? $btn.text() : null;
    
    // 버튼 비활성화 (있는 경우)
    if($btn.length > 0) {
        $btn.prop('disabled', true).html('<div class="spinner"></div> 전송 중...');
    }
    
    // 버튼 상태 복원 함수
    const restoreButton = function() {
        if($btn.length > 0 && originalText) {
            $btn.prop('disabled', false).text(originalText);
        }
    };
    
    // AJAX 요청 설정
    $.ajax({
        url: contextPath + '/notificationAdminSend',
        type: 'POST',
        data: options.data || {},
        success: function(response) {
            if(response.success) {
                // 토스트 알림에는 사용자가 입력한 알림 제목 표시
                const title = options.data.title || '새 알림';
                showNotificationToast(title);
                
                // 초록색 알림창에는 서버 응답 메시지 표시
                showSuccessMessage(response.message || '알림이 발송되었습니다.');
                
                if(typeof options.success === 'function') {
                    options.success(response);
                }
            } else {
                console.error('알림 발송 실패:', response.message);
                if(typeof options.error === 'function') {
                    options.error(response);
                }
            }
            
            // 성공 시 버튼 상태 복원
            restoreButton();
        },
        error: function(xhr, status, error) {
            console.error('알림 발송 요청 오류:', status, error);
            if(typeof options.error === 'function') {
                options.error(xhr);
            }
            
            // 오류 시 버튼 상태 복원
            restoreButton();
        },
        complete: function() {
            // 항상 버튼 상태 복원 (추가 보장)
            restoreButton();
            
            if(typeof options.complete === 'function') {
                options.complete();
            }
        }
    });
}

/**
 * 직원 검색 함수 - 내부 구현
 * @param {string} keyword 검색 키워드
 * @param {string} resultsSelector 결과 표시할 요소 선택자
 * @param {string} selectedEmpNoSelector 선택된 직원 번호를 저장할 요소 선택자
 * @param {string} selectedInfoSelector 선택된 직원 정보 표시할 요소 선택자
 */
function _searchEmployeeInternal(keyword, resultsSelector, selectedEmpNoSelector, selectedInfoSelector) {
    if(!keyword) {
        alertify.alert("검색어를 입력하세요");
        return;
    }
    
    const resultsElement = document.querySelector(resultsSelector);
    if(!resultsElement) return;
    
    // 검색 중 표시
    resultsElement.innerHTML = '<div>검색 중...</div>';
    
    // AJAX 요청
    const contextPath = $("#contextPath").val() || '';
    $.ajax({
        url: contextPath + '/employeeSearch',
        type: 'GET',
        data: { keyword: keyword },
        dataType: 'json',
        success: function(data) {
            console.log("검색 결과:", data);
            
            if(!data || data.length === 0) {
                resultsElement.innerHTML = '<div>검색 결과가 없습니다</div>';
                return;
            }
            
            // 결과 컨테이너 생성
            const resultsList = document.createElement('div');
            resultsList.className = 'emp_result_list';
            
            // 각 직원에 대한 항목 생성
            data.forEach(function(emp) {
                // 대문자 필드 사용
                const empNo = emp.EMPNO || emp.empNo || '';
                const empName = emp.EMPNAME || emp.empName || '';
                const empId = emp.EMPID || emp.empId || '';
                const deptName = emp.DEPTNAME || emp.deptName || '';
                
                // 직원 항목 생성
                const empItem = document.createElement('div');
                empItem.className = 'emp_result_item';
                empItem.textContent = empName + ' (' + empId + ') - ' + deptName;
                
                // 클릭 이벤트 추가
                empItem.addEventListener('click', function() {
                    // 선택된 직원 정보 저장
                    document.querySelector(selectedEmpNoSelector).value = empNo;
                    
                    // 선택된 직원 정보 표시
                    const infoElement = document.querySelector(selectedInfoSelector);
                    if(infoElement) {
                        infoElement.innerHTML = '<div>선택된 직원: ' + empName + ' (' + empId + ')</div>';
                        infoElement.style.display = 'block';
                    }
                    
                    // 검색 결과 초기화
                    resultsElement.innerHTML = '';
                    document.getElementById('emp_search_input').value = '';
                });
                
                resultsList.appendChild(empItem);
            });
            
            // 결과 표시
            resultsElement.innerHTML = '';
            resultsElement.appendChild(resultsList);
        },
        error: function(xhr, status, error) {
            console.error("검색 오류:", error);
            resultsElement.innerHTML = '<div>검색 중 오류가 발생했습니다</div>';
        }
    });
}

/**
 * 공지사항 폼 관련 기능 설정
 */
function setupNoticeNotifications() {
    // 공지사항 폼 찾기
    const noticeForm = document.getElementById('noticeForm');
    if (!noticeForm) return;
    
    // 공지사항 폼 제출 처리
    noticeForm.addEventListener('submit', function(event) {
        // 알림 체크박스 확인
        const sendNotificationCheckbox = document.getElementById('sendNotification');
        if (sendNotificationCheckbox && sendNotificationCheckbox.checked) {
            event.preventDefault();
            
            // 필수 필드 검증
            const title = document.getElementById('noticeTitle').value.trim();
            const category = document.getElementById('category').value;
            const content = document.getElementById('noticeContent').value.trim();
            
            if (!title) {
                alertify.error('제목을 입력해주세요.');
                document.getElementById('noticeTitle').focus();
                return false;
            }
            
            if (!category) {
                alertify.error('분류를 선택해주세요.');
                document.getElementById('category').focus();
                return false;
            }
            
            if (!content) {
                alertify.error('내용을 입력해주세요.');
                document.getElementById('noticeContent').focus();
                return false;
            }
            
            // 필독 체크박스 값 처리
            const isMandatory = document.getElementById('isMandatory');
            if (isMandatory && !isMandatory.checked) {
                const hiddenField = document.createElement('input');
                hiddenField.type = 'hidden';
                hiddenField.name = 'isMandatory';
                hiddenField.value = '0';
                noticeForm.appendChild(hiddenField);
            }
            
            // 제출 버튼 비활성화
            const submitBtn = document.getElementById('submitBtn');
            const originalButtonText = submitBtn.innerHTML;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 처리중...';
            
            // AJAX 제출
            const formData = new FormData(noticeForm);
            fetch(noticeForm.action, {
                method: 'POST',
                body: new URLSearchParams(formData),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 응답 오류');
                }
                return response.text();
            })
            .then(() => {
                // 서버에서 WebSocket으로 알림이 전송될 것이므로
                // 여기서는 직접 알림을 표시하지 않음
                
                // 페이지 이동
                setTimeout(() => {
                    const contextPath = document.getElementById('contextPath').value || '';
                    const isUpdate = noticeForm.action.includes('noticeUpdate');
                    const noticeNo = formData.get('noticeNo');
                    
                    if (isUpdate && noticeNo) {
                        window.location.href = `${contextPath}/noticeDetail/${noticeNo}`;
                    } else {
                        window.location.href = `${contextPath}/noticeList`;
                    }
                }, 1000);
            })
            .catch(error => {
                console.error('공지사항 제출 오류:', error);
                alertify.error(`공지사항 ${noticeForm.action.includes('noticeUpdate') ? '수정' : '등록'} 중 오류가 발생했습니다.`);
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalButtonText;
            });
            
            return false;
        }
    });
}

/**
 * 공지사항 등록 후 알림 발송 - 내부 함수 구현 
 * @param {string} title 공지사항 제목
 * @param {string} content 공지사항 내용
 * @return {boolean} 성공 여부
 */
function _sendNotificationAfterNoticeCreateInternal(title, content) {
    try {
        console.log("공지사항 등록 후 알림 발송:", title);
        // 토스트 알림에는 공지사항 제목 표시
        showNotificationToast(title);
        // 초록색 알림창에는 성공 메시지 표시
        showSuccessMessage('공지사항 등록 완료. 전체 직원에게 알림이 발송되었습니다.');
    } catch (e) {
        console.error("공지사항 알림 발송 중 오류:", e);
    }
    return true;
}

/**
 * 공지사항 수정 후 알림 발송 - 내부 함수 구현
 * @param {string} title 공지사항 제목
 * @param {string} content 공지사항 내용
 * @return {boolean} 성공 여부
 */
function _sendNotificationAfterNoticeUpdateInternal(title, content) {
    try {
        console.log("공지사항 수정 후 알림 발송:", title);
        // 토스트 알림에는 공지사항 제목 표시
        showNotificationToast(title);
        // 초록색 알림창에는 성공 메시지 표시
        showSuccessMessage('공지사항 수정 완료. 전체 직원에게 알림이 발송되었습니다.');
    } catch (e) {
        console.error("공지사항 알림 발송 중 오류:", e);
    }
    return true;
}

// 초기화 코드
$(document).ready(function() {
    // DOM 요소 캐싱
    notificationBadgeElement = document.getElementById('header-notification-badge');
    notificationListElement = document.getElementById('header-notification-list');
    notificationDropdownElement = document.getElementById('header-notification-dropdown');
    notificationBtnElement = document.getElementById('header-notification-btn');
    
    // 연결에 필요한 사용자 정보가 있는지 확인
    const userId = $("#loginUserId").val();
    if (userId) {
        // STOMP 연결
        connectWebSocket();
        
        // 페이지 로드 즉시 읽지 않은 알림 가져오기 (STOMP 연결 전에)
        updateNotificationBadgeOnLoad();
        
        // 문서 클릭 시 드롭다운 닫기 이벤트 등록
        $(document).on('click.notificationDropdown', function(e) {
            if (notificationDropdownElement && notificationDropdownElement.classList.contains('header-show') && 
                !e.target.closest('#header-notification-btn') && 
                !e.target.closest('#header-notification-dropdown')) {
                notificationDropdownElement.classList.remove('header-show');
            }
        });
        
        // 알림 버튼 클릭 이벤트 등록
        if (notificationBtnElement && notificationDropdownElement) {
            notificationBtnElement.addEventListener('click', toggleNotificationDropdown);
        }
        
        // 공지사항 알림 기능 설정
        setupNoticeNotifications();
    } else {
        console.warn('로그인 사용자 ID를 찾을 수 없어 알림 시스템이 초기화되지 않았습니다.');
    }
    
    // 페이지 언로드 시 연결 종료 및 이벤트 정리
    $(window).on('beforeunload', function() {
        if (stompClient !== null && stompClient.connected) {
            stompClient.disconnect();
        }
        $(document).off('click.notificationDropdown');
        
        // 이벤트 핸들러 제거
        if (notificationBtnElement) {
            notificationBtnElement.removeEventListener('click', toggleNotificationDropdown);
        }
    });
});

// 전역 함수로 노출 - 함수명과 내부 구현 함수명을 다르게 하여 무한 재귀 방지
window.showToastNotification = showNotificationToast;
window.showSuccessMessage = showSuccessMessage;
window.sendNotification = function(options) {
    _sendNotificationInternal(options);
};
window.searchEmployee = function(keyword, resultsSelector, selectedEmpNoSelector, selectedInfoSelector) {
    _searchEmployeeInternal(keyword, resultsSelector, selectedEmpNoSelector, selectedInfoSelector);
};
window.sendNotificationAfterNoticeCreate = function(title, content) {
    return _sendNotificationAfterNoticeCreateInternal(title, content);
};
window.sendNotificationAfterNoticeUpdate = function(title, content) {
    return _sendNotificationAfterNoticeUpdateInternal(title, content);
};