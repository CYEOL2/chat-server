// src/main/resources/static/js/services/sse.js

import { appState } from '../state/appState.js';
import { showNotification } from '../utils/notification.js';
import { updateRoomListWithNewMessage } from '../ui/chatRoomUI.js';

export function connectSSE() {
    if (!appState.nickName || !appState.isLoggedIn) {
        console.log('Cannot connect SSE: not logged in');
        return;
    }

    // 이미 연결되어 있거나 연결 중이라면 중복 연결 방지
    if (appState.sseConnection) {
        if (appState.sseConnection.readyState === EventSource.OPEN) {
            console.log('SSE already connected (OPEN), skipping...', appState.sseConnection.readyState);
            return;
        } else if (appState.sseConnection.readyState === EventSource.CONNECTING) {
            console.log('SSE already connecting (CONNECTING), skipping...', appState.sseConnection.readyState);
            return;
        } else {
            console.log('SSE in CLOSED state, cleaning up before new connection...', appState.sseConnection.readyState);
            appState.sseConnection.close();
            appState.sseConnection = null;
        }
    }

    try {
        const sseUrl = `/sse/${appState.nickName}`;
        console.log(`Attempting SSE connection to: ${sseUrl}`);
        
        appState.sseConnection = new EventSource(sseUrl, {
            withCredentials: false
        });

        appState.sseConnection.onopen = (event) => {
            console.log('✅ SSE connection opened successfully');
            console.log('SSE readyState:', appState.sseConnection.readyState);
            appState.sseReconnectAttempts = 0;

            if (appState.reconnectTimeout) {
                clearTimeout(appState.reconnectTimeout);
                appState.reconnectTimeout = null;
            }

            showNotification('Connected', 'Real-time notifications enabled', 'success', 3000);
        };

        // 백엔드에서는 오직 'alert-message' 이벤트만 전송함
        appState.sseConnection.addEventListener('alert-message', (event) => {
            console.log('🔔 SSE alert-message received:', event);
            try {
                const notification = JSON.parse(event.data);
                handleNotification(notification);
            } catch (error) {
                console.error('Error parsing alert-message:', error);
            }
        });

        appState.sseConnection.onerror = (error) => {
            console.error('❌ SSE error occurred:', error);
            console.log('SSE readyState on error:', appState.sseConnection ? appState.sseConnection.readyState : 'null');

            showNotification('SSE Connection Error', 'Real-time notifications disabled.', 'warning', 10000);

            if (appState.sseConnection) {
                appState.sseConnection.close();
                appState.sseConnection = null;
            }

            appState.sseReconnectAttempts = appState.maxReconnectAttempts;
        };

        appState.sseConnection.onclose = (event) => {
            console.log('🔒 SSE connection closed:', event);
            appState.sseConnection = null;
            appState.sseReconnectAttempts = appState.maxReconnectAttempts;
        };

    } catch (error) {
        console.error('Failed to create SSE connection:', error);
        showNotification('Connection Failed', 'Could not establish real-time connection', 'error', 5000);
    }
}

export function disconnectSSE() {
    console.log('🔌 Disconnecting SSE...');

    appState.isLoggedIn = false;

    if (appState.reconnectTimeout) {
        clearTimeout(appState.reconnectTimeout);
        appState.reconnectTimeout = null;
    }

    appState.sseReconnectAttempts = appState.maxReconnectAttempts;

    if (appState.sseConnection) {
        console.log('Closing SSE connection...');
        appState.sseConnection.close();
        appState.sseConnection = null;
    }

    console.log('SSE disconnection complete');
}

// 백엔드 Notification 처리 (alert-message 이벤트에서만 호출됨)
function handleNotification(notification) {
    console.log('Received notification:', notification);

    const { roomId, title, message } = notification;

    // 현재 채팅방이 아닌 경우에만 알림 표시
    if (appState.currentRoomId !== roomId) {
        showNotification(
            title || 'New Notification',
            message || 'You have a new message',
            'info',
            8000
        );

        // 채팅방 목록에서 해당 채팅방 강조
        updateRoomListWithNewMessage(roomId);
    }
}
