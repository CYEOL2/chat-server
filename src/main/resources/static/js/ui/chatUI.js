// src/main/resources/static/js/ui/chatUI.js

import { getElement, setDisabled, setTextContent } from '../utils/dom.js';
import { appState } from '../state/appState.js';
import { clearNewMessageIndicators } from './chatRoomUI.js';

const chatRoomNameH2 = getElement('chat-room-name');
const messageInput = getElement('message-input');
const sendBtn = getElement('send-btn');
const disconnectBtn = getElement('disconnect-btn');
const messagesDiv = getElement('messages');

export function displayMessage(message) {
    const messageElement = document.createElement('div');
    messageElement.classList.add('message', message.senderNickname === appState.nickName ? 'sent' : 'received');

    const senderElement = document.createElement('span');
    senderElement.classList.add('sender');
    senderElement.textContent = message.senderNickname;

    messageElement.appendChild(senderElement);
    messageElement.append(document.createTextNode(message.content));

    messagesDiv.appendChild(messageElement);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

export function updateChatUIToConnected(roomName) {
    setTextContent(chatRoomNameH2, roomName);
    setDisabled(messageInput, false);
    setDisabled(sendBtn, false);
    setDisabled(disconnectBtn, false);
    messagesDiv.innerHTML = '';

    // 모든 채팅방 목록에서 새 메시지 표시 제거 (현재 접속한 방)
    if (appState.currentRoomId) {
        clearNewMessageIndicators(appState.currentRoomId);
    }

    // 활성 룸 요소에서도 새 메시지 표시 제거
    if (appState.activeRoomElement) {
        appState.activeRoomElement.style.fontWeight = '';
        appState.activeRoomElement.style.backgroundColor = '';
        appState.activeRoomElement.style.border = '';
        appState.activeRoomElement.classList.remove('has-new-message');

        const badge = appState.activeRoomElement.querySelector('.message-count-badge');
        if (badge) {
            badge.remove();
        }
    }
}

export function resetChatUI() {
    setTextContent(chatRoomNameH2, 'Select a room to start');
    setDisabled(messageInput, true);
    setDisabled(sendBtn, true);
    setDisabled(disconnectBtn, true);
    messagesDiv.innerHTML = '<div class="message-placeholder">Please select a room to start chatting.</div>';
    if(appState.activeRoomElement) {
        appState.activeRoomElement.classList.remove('active');
        appState.activeRoomElement = null;
    }
}
