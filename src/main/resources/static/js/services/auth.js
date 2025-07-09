// src/main/resources/static/js/services/auth.js

import { appState, resetAppState } from '../state/appState.js';
import { showNotification } from '../utils/notification.js';
import { setDisplay, setTextContent } from '../utils/dom.js';
import { disconnectSSE, connectSSE } from './sse.js';
import { disconnectWebSocket } from './websocket.js';
import { fetchMyRooms } from './api.js';
import { renderRoomList } from '../ui/chatRoomUI.js';

const loginSection = document.getElementById('login-section');
const userStatus = document.getElementById('user-status');
const userNick = document.getElementById('user-nick');
const mainContainer = document.getElementById('main-container');
const currentUser = document.getElementById('current-user');
const loginBtn = document.getElementById('login-btn');
const myRoomList = document.getElementById('my-room-list');

// 모든 채팅방의 알림 표시 초기화 함수
function clearAllNotificationIndicators() {
    console.log('🧹 Clearing all notification indicators...');
    
    // 모든 채팅방 목록에서 알림 표시 제거
    const allRoomItems = document.querySelectorAll('#all-room-list li, #my-room-list li');
    
    allRoomItems.forEach(item => {
        // 스타일 초기화
        item.style.fontWeight = '';
        item.style.backgroundColor = '';
        item.style.border = '';
        
        // 알림 클래스 제거
        item.classList.remove('has-new-message');
        
        // 메시지 카운트 배지 제거
        const badge = item.querySelector('.message-count-badge');
        if (badge) {
            badge.remove();
        }
    });
    
    // appState의 메시지 카운트도 초기화
    appState.newMessageCounts = {};
    
    console.log('✅ All notification indicators cleared');
}

export function login(inputNickName) {
    if (!inputNickName) {
        showNotification('Error', 'Please enter a nickname.', 'error');
        return;
    }

    // 이미 로그인된 경우 중복 방지
    if (appState.isLoggedIn || appState.nickName) {
        console.log('Already logged in as:', appState.nickName, 'isLoggedIn:', appState.isLoggedIn);
        return;
    }

    // 로그인 버튼 비활성화 (중복 클릭 방지)
    loginBtn.disabled = true;

    // 🧹 새 로그인 시 모든 알림 초기화
    clearAllNotificationIndicators();

    appState.nickName = inputNickName;
    appState.isLoggedIn = true;
    appState.sseReconnectAttempts = 0;

    // UI 업데이트
    setDisplay(loginSection, 'none');
    setDisplay(userStatus, 'flex');
    setTextContent(userNick, appState.nickName);
    setTextContent(currentUser, `Logged in as: ${appState.nickName}`);
    setDisplay(mainContainer, 'flex');

    // 로그인 버튼 다시 활성화
    loginBtn.disabled = false;

    // 내 채팅방 목록 로드 - My Rooms에는 나가기 버튼 표시
    fetchMyRooms(appState.nickName).then(rooms => {
        renderRoomList(myRoomList, rooms, true); // ✅ My Rooms - 나가기 버튼 있음
    });

    showNotification('Success', `Welcome, ${appState.nickName}!`, 'success', 3000);

    // SSE 즉시 연결
    console.log('🚀 Starting SSE connection for user:', appState.nickName);
    connectSSE();
}

export function logout() {
    console.log('🚪 Logging out...');

    // 먼저 상태를 변경하여 재연결 방지
    appState.isLoggedIn = false;

    // 🧹 로그아웃 시에도 모든 알림 초기화
    clearAllNotificationIndicators();

    // SSE 연결 해제 (재연결 방지)
    disconnectSSE();

    // WebSocket 연결 해제
    disconnectWebSocket();

    // 상태 리셋
    resetAppState();

    // UI 리셋
    setDisplay(loginSection, 'flex');
    setDisplay(userStatus, 'none');
    setDisplay(mainContainer, 'none');
    setTextContent(userNick, '');
    setTextContent(currentUser, '');
    document.getElementById('nick-name').value = ''; // 닉네임 입력 필드 초기화

    // 내 방 목록 초기화
    myRoomList.innerHTML = '';
    const li = document.createElement('li');
    li.textContent = 'Please login to see your rooms.';
    li.dataset.roomName = '';
    li.classList.add('no-interaction');
    myRoomList.appendChild(li);

    showNotification('Info', 'You have been logged out.', 'info', 3000);
}
