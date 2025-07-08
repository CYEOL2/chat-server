// src/main/resources/static/js/state/appState.js

export const appState = {
    websocket: null,
    sseConnection: null,
    nickName: null,
    isLoggedIn: false,
    currentRoomId: null,
    activeRoomElement: null,
    sseReconnectAttempts: 0,
    maxReconnectAttempts: 3,
    reconnectTimeout: null,
    connectionId: null, // 고유 연결 ID 추가
    newMessageCounts: {}, // 방별 새 메시지 수 저장
};

export function resetAppState() {
    appState.websocket = null;
    appState.sseConnection = null;
    appState.nickName = null;
    appState.isLoggedIn = false;
    appState.currentRoomId = null;
    appState.activeRoomElement = null;
    appState.sseReconnectAttempts = 0;
    appState.maxReconnectAttempts = 3;
    appState.reconnectTimeout = null;
    appState.connectionId = null;
    appState.newMessageCounts = {};
}
