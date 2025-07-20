/*
* STOMP 기반 채팅 배지 시스템 클라이언트
* 헤더에 안 읽은 채팅 수를 실시간으로 표시하는 모듈
* notification.js와 독립적으로 작동합니다.
*/

// DOM 요소 캐싱 변수
let chatBadgeElement = null;

// 별도의 STOMP 클라이언트 인스턴스
let stompClientChat = null;
let socketChat = null;
let reconnectIntervalChat = 5000; // 재연결 간격 (밀리초)
let isConnectingChat = false;     // 연결 중 상태 플래그
let reconnectCountChat = 0;       // 재연결 시도 횟수
let maxReconnectAttemptsChat = 10; // 최대 재연결 시도 횟수 (notification.js와 다르게 설정 가능)
let chatBadgeSubscription = null; // 채팅 배지 업데이트 토픽 구독 객체

/**
 * 채팅 배지 WebSocket 연결 설정
 */
function connectChatWebSocket() {
    if (isConnectingChat) {
        console.log("채팅 배지 WS: 이미 연결 시도 중입니다.");
        return;
    }
    if (stompClientChat && stompClientChat.connected) {
        console.log("채팅 배지 WS: 이미 연결되어 있습니다.");
        return;
    }

    isConnectingChat = true;

    // 기존 연결 정리 (만약 있다면)
    if (stompClientChat !== null) {
        try {
            stompClientChat.disconnect(function() {
                console.log("채팅 배지 WS: 기존 연결 해제 완료");
            }, { receipt: 'disconnect-receipt' });
        } catch (e) {
            console.error("채팅 배지 WS: 기존 연결 해제 오류", e);
        }
        stompClientChat = null;
    }
    if (socketChat) {
        socketChat = null;
    }

    try {
        console.log("채팅 배지 WS: 연결 시도...");
        // contextPath 동적 처리
        const contextPath = $("#contextPath").val() || '';
        // notification.js와 동일한 WebSocket 엔드포인트 사용
        socketChat = new SockJS(contextPath + '/ws-stomp');
        stompClientChat = Stomp.over(socketChat);

        // STOMP 디버그 로그 (필요시 주석 처리)
        stompClientChat.debug = function(str) {
            if (str.includes('>>> CONNECT') || str.includes('<<< CONNECTED') || str.includes('PING') || str.includes('PONG')) {
                // 연결/하트비트 로그는 간단히 표시
                // console.log('STOMP CHAT DEBUG:', str.substring(0, 50) + '...');
            } else {
                // 다른 로그는 자세히 표시
                console.log('STOMP CHAT DEBUG:', str);
            }
        };


        // 연결 시도 (하트비트 설정 포함)
        let connectHeaders = {
            'heart-beat': '10000,10000' // 10초 간격 하트비트
        };
        stompClientChat.connect(connectHeaders, onChatConnected, onChatError);

    } catch (e) {
        console.error("채팅 배지 WS: 연결 시도 중 오류 발생", e);
        isConnectingChat = false;
        scheduleChatReconnect(); // 재연결 시도
    }
}

/**
 * 채팅 배지 WebSocket 연결 성공 시 호출
 * @param {Object} frame STOMP 연결 프레임
 */
function onChatConnected(frame) {
    console.log('채팅 배지 WS: 연결 성공: ' + frame);
    isConnectingChat = false;
    reconnectCountChat = 0; // 재연결 성공 시 카운트 초기화

    // 사용자 개인 안 읽은 채팅 총 수 업데이트 토픽 구독
    const userId = $("#loginUserId").val(); // empId
    if (userId) {
        const topic = '/user/' + userId + '/queue/chat.unread.total';
        if (!chatBadgeSubscription) { // 중복 구독 방지
            console.log('채팅 배지 WS: 토픽 구독 시도:', topic);
            try {
                chatBadgeSubscription = stompClientChat.subscribe(topic, handleUnreadChatTotalUpdate);
                console.log('채팅 배지 WS: 토픽 구독 완료:', topic);

                // 연결 성공 후 초기 안 읽은 채팅 수 요청 (REST API 활용)
                // 구독 완료 후 바로 서버에서 totalUnreadCount를 푸시해주면 이 요청은 필요 없음
                // 서버 푸시가 없으면 여기서 요청해야 합니다.
                // fetchUnreadChatCountViaREST(); // 필요시 주석 해제

            } catch(e) {
                console.error('채팅 배지 WS: 토픽 구독 실패:', topic, e);
                chatBadgeSubscription = null; // 구독 실패 표시
            }
        } else {
            console.log('채팅 배지 WS: 이미 구독 중인 토픽:', topic);
        }

    } else {
        console.warn('채팅 배지 WS: 로그인 사용자 ID를 찾을 수 없어 토픽 구독을 건너뜁니다.');
    }

    // 연결 완료 이벤트 발생 (필요시 다른 모듈에게 알림)
    $(document).trigger('chatBadge:connected');
}

/**
 * 채팅 배지 WebSocket 연결 오류 시 호출
 * @param {Object} error 오류 객체
 */
function onChatError(error) {
    console.error('채팅 배지 WS: 연결 오류: ', error);
    isConnectingChat = false;
    chatBadgeSubscription = null; // 구독 상태 해제

    // 연결 상태 초기화
    stompClientChat = null;
    socketChat = null;

    // 재시도 스케줄링
    scheduleChatReconnect();

    // 사용자에게 연결 문제 알림 (선택적)
    if (reconnectCountChat > 3) {
        // showNotificationToast('채팅 서버 연결에 문제가 있습니다. 자동으로 재연결을 시도합니다.'); // notification.js의 토스트 사용 예시
    }
}

/**
 * 채팅 배지 WebSocket 재연결 스케줄링
 */
function scheduleChatReconnect() {
    if (isConnectingChat) {
        console.log("채팅 배지 WS: 이미 재연결 스케줄링 또는 연결 시도 중입니다.");
        return;
    }

    if (reconnectCountChat >= maxReconnectAttemptsChat) {
        console.error("채팅 배지 WS: 최대 재연결 시도 횟수를 초과했습니다.");
        return;
    }

    reconnectCountChat++;
    const timeout = reconnectIntervalChat * Math.pow(1.5, reconnectCountChat - 1); // 지수 백오프
    console.log(`채팅 배지 WS: 재연결 시도 ${reconnectCountChat}/${maxReconnectAttemptsChat} (${timeout}ms 후)`);

    setTimeout(function() {
        connectChatWebSocket(); // 재연결 시도
    }, timeout);
}


/**
 * 안 읽은 채팅 총 수 업데이트 메시지 처리
 * 서버에서 '/user/{userId}/queue/chat.unread.total' 토픽으로 보낸 메시지를 수신합니다.
 * @param {Object} message STOMP 메시지 객체. message.body에 새로운 총 개수가 들어있다고 가정.
 */
function handleUnreadChatTotalUpdate(message) {
    try {
        console.log('채팅 배지 WS: 안 읽은 채팅 총 수 업데이트 메시지 수신:', message);
        // 메시지 본문은 새로운 전체 안 읽은 채팅 수 (숫자)
        const newTotalCount = parseInt(message.body || '0');

        // 헤더 배지 업데이트 함수 호출
        updateUnreadChatCount(newTotalCount);

        // ★ 선택 사항: 채팅 목록 페이지인 경우 각 방의 안 읽은 수 업데이트 로직도 필요 ★
        // 이 메시지만으로는 어떤 방의 수가 줄었는지 알 수 없으므로,
        // 클라이언트에서 다시 전체 채팅방 목록을 가져와서 업데이트 해야 합니다.
        // 만약 현재 페이지가 chatList.ch라면, 채팅 목록 UI도 업데이트하는 별도의 함수를 호출할 수 있습니다.
        // 이 로직은 chatList.js 파일에 정의하는 것이 더 적합합니다.
        // 예: if (window.location.pathname.endsWith('/chatList.ch')) { updateChatListUI(); }

    } catch (e) {
        console.error("채팅 배지 WS: 안 읽은 채팅 총 수 업데이트 처리 중 오류 발생", e);
    }
}

// ================================================================
//          채팅 배지 UI 업데이트 함수 (헤더 배지 전용)
// ================================================================

/**
 * 채팅 배지 업데이트 (헤더 배지 전용)
 * @param {number} count 안 읽은 채팅 총 개수
 */
function updateUnreadChatCount(count) {
    if (!chatBadgeElement) {
        chatBadgeElement = document.getElementById('header-chat-badge');
    }
    if (!chatBadgeElement) {
        console.error("채팅 배지 요소(header-chat-badge)를 찾을 수 없습니다.");
        return;
    }

    // console.log('채팅 배지 업데이트:', count); // 디버그 로그

    const currentText = chatBadgeElement.textContent;
    const newText = count > 99 ? "99+" : count; // 99+ 처리

    // 실제 개수 변화가 있거나, 텍스트 표시가 변경될 때만 업데이트
    // 또는 현재 숨겨져 있다가 개수가 0보다 커질 때
    if (parseInt(currentText || '0') !== count || chatBadgeElement.style.display === 'none' && count > 0) {
        chatBadgeElement.textContent = newText;

        if (count > 0) {
            chatBadgeElement.style.display = 'flex'; // 보이게 (CSS에 맞게)
        } else {
            chatBadgeElement.style.display = 'none'; // 숨김
        }
        console.log('채팅 배지 업데이트 완료:', newText, ' (display:', chatBadgeElement.style.display, ')');
    } else {
        // console.log('채팅 배지 변화 없음. 업데이트 건너뜁니다.'); // 디버그 로그
    }
}


/**
 * 채팅 배지 숫자 증가 (헤더 배지 전용)
 */
function increaseUnreadChatCount() {
    if (!chatBadgeElement) {
        chatBadgeElement = document.getElementById('header-chat-badge');
    }
    if (!chatBadgeElement) {
        console.error("채팅 배지 요소(header-chat-badge)를 찾을 수 없습니다.");
        return;
    }

    // 현재 값을 파싱하되, "99+"인 경우는 99로 간주하여 계산
    let currentCount = parseInt(chatBadgeElement.textContent === "99+" ? "99" : chatBadgeElement.textContent || '0');
    if (currentCount < 99) { // 99+ 이상에서는 더 이상 증가시키지 않음
        let newCount = currentCount + 1;
        updateUnreadChatCount(newCount);
    } else if (chatBadgeElement.textContent !== "99+") {
        // 99가 되었고 아직 "99+"가 아니면 "99+"로 업데이트
        updateUnreadChatCount(99); // updateUnreadChatCount 함수가 99+ 처리
    } else {
        // 이미 99+ 인 경우 변화 없음
    }
}

/**
 * 채팅 배지 숫자 감소 (헤더 배지 전용)
 */
function decreaseUnreadChatCount() {
    if (!chatBadgeElement) {
        chatBadgeElement = document.getElementById('header-chat-badge');
    }
    if (!chatBadgeElement) {
        console.error("채팅 배지 요소(header-chat-badge)를 찾을 수 없습니다.");
        return;
    }

    // 현재 값을 파싱하되, "99+"인 경우는 99로 간주하여 계산
    let currentCount = parseInt(chatBadgeElement.textContent === "99+" ? "99" : chatBadgeElement.textContent || '0');
    let newCount = currentCount - 1;
    if (newCount < 0) newCount = 0; // 개수는 0 미만이 될 수 없습니다.
    updateUnreadChatCount(newCount);
}


// ================================================================
//          초기 로드 및 연결 관리
// ================================================================

/**
 * 페이지 로드 시 초기 안 읽은 채팅 개수 가져오기 (REST API 활용)
 */
function fetchUnreadChatCountViaREST() {
    const contextPath = $("#contextPath").val() || '';
    const loginUserId = $("#loginUserId").val(); // 로그인 사용자 ID (empId)

    if (!loginUserId) {
        console.warn('채팅 배지 WS: 로그인 사용자 ID를 찾을 수 없어 초기 안 읽은 채팅 수 가져오기를 건너뜁니다.');
        return;
    }
    // TODO: 백엔드에 전체 안 읽은 채팅 수를 조회하는 API 엔드포인트 구현 필요 (/chat/unreadCount)
    // ChatController에 @GetMapping("/chat/unreadCount") 메소드 추가

    $.ajax({
        url: contextPath + '/chat/unreadCount', // 새로 만들 API 경로
        type: 'GET',
        dataType: 'json', // 응답이 JSON이라고 가정 ({ "totalUnreadCount": 5 } 형식)
        timeout: 5000, // 5초 타임아웃
        success: function(response) {
            console.log('채팅 배지 WS: REST API 응답: 전체 안 읽은 채팅 수', response); // 응답 형태 확인 필요

            // 응답 형태에 따라 실제 count 값을 추출
            // 백엔드에서 { "totalUnreadCount": 5 } 와 같은 형식으로 반환한다고 가정
            const totalCount = response.totalUnreadCount || 0;

            // 헤더 배지 업데이트
            updateUnreadChatCount(totalCount);

            // REST API로 초기 목록을 가져오는 경우, 개별 목록 UI 업데이트는 chatList.js에서 처리
            if (window.location.pathname.endsWith('/chatList.ch')) {
                // chatList.js에서 채팅방 목록 로드 및 개별 unreadCount 표시 로직 구현 필요
            }

        },
        error: function(xhr, status, error) {
            console.error('채팅 배지 WS: 초기 안 읽은 채팅 수 요청 실패:', status, error);
            // 실패 시 배지 숨김 처리
            updateUnreadChatCount(0);
        }
    });
}


// 페이지 로드 완료 시 실행
$(document).ready(function() {
    // DOM 요소 캐싱
    chatBadgeElement = document.getElementById('header-chat-badge');

    // 로그인 사용자 정보가 있는지 확인
    const loginUserId = $("#loginUserId").val(); // empId
    if (loginUserId) {
        console.log('채팅 배지 WS: 로그인 사용자 확인, WebSocket 연결 시도 및 초기 카운트 가져오기');

        // WebSocket 연결 시도
        connectChatWebSocket();

        // 초기 안 읽은 채팅 수 가져오기 (REST API)
        // WebSocket 연결이 바로 성공하여 메시지를 푸시받을 수도 있지만,
        // 페이지 로드 시 초기 값은 REST API로 가져오는 것이 안정적입니다.
        fetchUnreadChatCountViaREST();

        // 페이지 언로드 시 WebSocket 연결 종료
        $(window).on('beforeunload', function() {
            if (stompClientChat !== null && stompClientChat.connected) {
                console.log('채팅 배지 WS: 페이지 언로드 시 연결 해제 시도');
                try {
                    stompClientChat.disconnect(function() {
                        console.log("채팅 배지 WS: 연결 해제 완료 (beforeunload)");
                    }, { receipt: 'disconnect-beforeunload' });
                } catch(e) {
                    console.error("채팅 배지 WS: 연결 해제 중 오류 (beforeunload)", e);
                }
            }
        });

        // 다른 파일에서 updateUnreadChatCount 함수를 호출할 수 있도록 전역 노출 (선택 사항, 필요시)
        // 예: chatList.js에서 채팅방 읽음 처리 후 이 함수를 호출해야 할 때 사용
        window.updateUnreadChatCount = updateUnreadChatCount; // 전역 함수로 등록

    } else {
        console.warn('채팅 배지 WS: 로그인 사용자 ID를 찾을 수 없어 채팅 배지 시스템이 초기화되지 않았습니다.');
        // 로그인되지 않은 경우 배지 숨김 상태 유지 (기본 HTML 상태)
    }
});