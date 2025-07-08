// src/main/resources/static/js/services/websocket.js

import { appState } from '../state/appState.js';
import { showNotification } from '../utils/notification.js';
import { displayMessage, updateChatUIToConnected, resetChatUI } from '../ui/chatUI.js';
import { loadChatHistory, leaveRoom, fetchMyRooms, fetchAllRooms } from './api.js';
import { renderRoomList } from '../ui/chatRoomUI.js';

// 연결 상태 추적을 위한 플래그
let isConnecting = false;
let connectionTimeout = null; // setTimeout 참조 저장

// 새로운 WebSocket 연결 생성 함수
function createNewConnection(roomId, roomName) {
    if (isConnecting) {
        console.log('🔄 Connection already in progress, skipping...');
        return;
    }

    isConnecting = true;
    
    const wsUrl = `ws://${window.location.host}/ws/chat?chatRoomId=${roomId}&nickName=${appState.nickName}`;
    console.log(`🔌 Creating new WebSocket connection to: ${wsUrl}`);
    
    appState.websocket = new WebSocket(wsUrl);

    appState.websocket.onopen = () => {
        console.log(`✅ WebSocket connection established for room ${roomId}`);
        isConnecting = false; // 연결 완료
        
        // UI 강제 업데이트 - Send 버튼 활성화 보장
        const messageInput = document.getElementById('message-input');
        const sendBtn = document.getElementById('send-btn');
        const disconnectBtn = document.getElementById('disconnect-btn');
        
        if (messageInput) messageInput.disabled = false;
        if (sendBtn) sendBtn.disabled = false;
        if (disconnectBtn) disconnectBtn.disabled = false;
        
        loadChatHistory(roomId).then(history => {
            history.forEach(msg => displayMessage(msg));
        });
        
        showNotification('Connected', `Joined ${roomName}`, 'success', 3000);
    };

    appState.websocket.onmessage = (event) => {
        const message = JSON.parse(event.data);
        displayMessage(message);
    };

    appState.websocket.onclose = (event) => {
        console.log('🔒 WebSocket connection closed:', event.code, event.reason);
        isConnecting = false; // 연결 종료 시 플래그 리셋
        
        if (appState.currentRoomId === roomId) {
            resetChatUI();
        }
    };

    appState.websocket.onerror = (error) => {
        console.error('❌ WebSocket error:', error);
        isConnecting = false; // 에러 시 플래그 리셋
        showNotification('Error', 'Failed to connect to chat room.', 'error');
        resetChatUI();
    };
}

export function connectWebSocket(roomId, roomName) {
    console.log(`🚀 connectWebSocket called for room ${roomId} (${roomName})`);
    
    if (!appState.isLoggedIn) {
        showNotification('Error', 'Please login first.', 'error');
        return;
    }

    // 같은 방에 이미 연결되어 있는지 확인 (더 엄격한 체크)
    if (appState.currentRoomId === roomId && 
        appState.websocket && 
        appState.websocket.readyState === WebSocket.OPEN) {
        console.log(`🔄 Already connected to room ${roomId}, skipping connection`);
        return;
    }

    // 이미 연결 중인지 확인 (근본원인 해결의 핵심)
    if (isConnecting) {
        console.log('🔄 Connection already in progress, skipping duplicate call');
        return;
    }

    // 기존 예약된 연결 시도가 있다면 취소 (중복 방지)
    if (connectionTimeout) {
        console.log('⏰ Canceling previous connection timeout');
        clearTimeout(connectionTimeout);
        connectionTimeout = null;
    }

    appState.currentRoomId = roomId;
    
    // UI를 먼저 업데이트 (알림 없이)
    updateChatUIToConnected(roomName);

    // 기존 연결 정리
    if (appState.websocket) {
        console.log('🔌 Closing existing WebSocket connection...');
        
        // 이벤트 핸들러 제거
        appState.websocket.onclose = () => {};
        appState.websocket.onerror = () => {};
        appState.websocket.onmessage = () => {};
        appState.websocket.onopen = () => {};
        
        appState.websocket.close();
        appState.websocket = null;
        
        // 연결 정리 후 지연 (타임아웃 참조 저장)
        connectionTimeout = setTimeout(() => {
            connectionTimeout = null; // 타임아웃 완료 후 참조 제거
            createNewConnection(roomId, roomName);
        }, 150);
    } else {
        // 기존 연결이 없으면 즉시 생성
        createNewConnection(roomId, roomName);
    }
}

export function disconnectWebSocket() {
    if (appState.websocket) {
        console.log('🔌 Disconnecting WebSocket...');
        
        // 이벤트 핸들러 정리
        appState.websocket.onclose = () => {};
        appState.websocket.onerror = () => {};
        appState.websocket.onmessage = () => {};
        appState.websocket.onopen = () => {};
        
        appState.websocket.close();
        appState.websocket = null;
    }
    
    // 예약된 연결 시도도 취소
    if (connectionTimeout) {
        clearTimeout(connectionTimeout);
        connectionTimeout = null;
    }
    
    isConnecting = false; // 플래그 리셋
    resetChatUI();
    showNotification('Info', 'Left the chat room.', 'info', 3000);
}

// 현재 채팅방에서 나가기 (API 호출 + WebSocket 해제)
export async function leaveCurrentRoom() {
    if (!appState.currentRoomId || !appState.isLoggedIn) {
        showNotification('Error', 'No room to leave.', 'error');
        return;
    }

    const roomName = document.getElementById('chat-room-name').textContent;
    
    if (!confirm(`정말로 "${roomName}" 채팅방에서 나가시겠습니까?`)) {
        return;
    }

    const success = await leaveRoom(appState.currentRoomId, appState.nickName);
    if (success) {
        // WebSocket 연결 해제
        disconnectWebSocket();
        
        showNotification('Success', `"${roomName}" 채팅방에서 나갔습니다.`, 'success', 3000);
        
        // 채팅방 목록 새로고침
        const myRoomList = document.getElementById('my-room-list');
        const allRoomList = document.getElementById('all-room-list');
        
        const [myRooms, allRooms] = await Promise.all([
            fetchMyRooms(appState.nickName),
            fetchAllRooms()
        ]);
        
        renderRoomList(myRoomList, myRooms, true);   // My Rooms - 나가기 버튼 있음
        renderRoomList(allRoomList, allRooms, false); // All Rooms - 나가기 버튼 없음
    }
}

export function sendChatMessage(messageContent) {
    if (!appState.isLoggedIn) {
        showNotification('Error', 'Please login first.', 'error');
        return false;
    }

    // WebSocket 상태 상세 확인
    if (!appState.websocket) {
        console.warn('⚠️ No WebSocket connection available');
        showNotification('Error', 'No connection to chat room.', 'error');
        return false;
    }

    if (appState.websocket.readyState !== WebSocket.OPEN) {
        console.warn('⚠️ WebSocket not ready:', appState.websocket.readyState);
        showNotification('Error', 'Connection not ready. Please wait.', 'error');
        return false;
    }

    if (messageContent && messageContent.trim()) {
        const chatMessage = {
            chatRoomId: appState.currentRoomId,
            senderNickname: appState.nickName,
            content: messageContent.trim(),
        };
        
        try {
            appState.websocket.send(JSON.stringify(chatMessage));
            return true;
        } catch (error) {
            console.error('❌ Failed to send message:', error);
            showNotification('Error', 'Failed to send message.', 'error');
            return false;
        }
    }
    
    return false;
}
