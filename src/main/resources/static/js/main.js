// src/main/resources/static/js/main.js

import { getElement, setInputValue, getInputValue, querySelectorAll, querySelector } from './utils/dom.js';
import { login, logout } from './services/auth.js';
import { disconnectWebSocket, sendChatMessage, leaveCurrentRoom } from './services/websocket.js';
import { fetchMyRooms, fetchAllRooms, createRoom, fetchChatRoomGuests } from './services/api.js';
import { renderRoomList, setActiveRoom, updateRoomListWithNewMessage, clearNewMessageIndicators } from './ui/chatRoomUI.js';
import { displayMessage, resetChatUI } from './ui/chatUI.js';
import { showNotification } from './utils/notification.js';
import { renderChatGuests, clearChatGuests } from './ui/chatGuestUI.js';
import { appState } from './state/appState.js';
import { disconnectSSE } from './services/sse.js';

document.addEventListener('DOMContentLoaded', () => {
    // --- DOM Elements ---
    const nickNameInput = getElement('nick-name');
    const loginBtn = getElement('login-btn');
    const logoutBtn = getElement('logout-btn');
    const disconnectBtn = getElement('disconnect-btn');
    const messageInput = getElement('message-input');
    const sendBtn = getElement('send-btn');
    const newRoomNameInput = getElement('new-room-name');
    const createRoomBtn = getElement('create-room-btn');
    const roomSearchInput = getElement('room-search');
    const tabBtns = querySelectorAll('.tab-btn');
    const tabContents = querySelectorAll('.room-tab-content');
    const allRoomList = getElement('all-room-list');
    const myRoomList = getElement('my-room-list');

    // --- Event Listeners ---
    loginBtn.addEventListener('click', () => login(getInputValue(nickNameInput)));
    logoutBtn.addEventListener('click', logout);
    disconnectBtn.addEventListener('click', leaveCurrentRoom); // ✅ 채팅방 나가기로 변경
    sendBtn.addEventListener('click', () => {
        const messageContent = getInputValue(messageInput);
        if (sendChatMessage(messageContent)) {
            setInputValue(messageInput, '');
        }
    });

    nickNameInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') login(getInputValue(nickNameInput));
    });

    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            const messageContent = getInputValue(messageInput);
            if (sendChatMessage(messageContent)) {
                setInputValue(messageInput, '');
            }
        }
    });

    createRoomBtn.addEventListener('click', async () => {
        const roomName = getInputValue(newRoomNameInput);
        if (await createRoom(roomName)) {
            setInputValue(newRoomNameInput, '');
            await fetchAllRooms().then(rooms => renderRoomList(allRoomList, rooms, false)); // ✅ All Rooms - 나가기 버튼 없음
            if (appState.isLoggedIn) {
                await fetchMyRooms(appState.nickName).then(rooms => renderRoomList(myRoomList, rooms, true)); // ✅ My Rooms - 나가기 버튼 있음
            }
            // Switch to all rooms tab to show the new room
            querySelector('.tab-btn[data-tab="all-rooms"]').click();
        }
    });

    roomSearchInput.addEventListener('keyup', filterRooms);

    tabBtns.forEach(btn => {
        btn.addEventListener('click', async () => {
            tabBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            const tabId = btn.dataset.tab;
            tabContents.forEach(content => {
                content.classList.toggle('active', content.id === tabId);
            });

            if (tabId === 'my-rooms') {
                if (appState.isLoggedIn) {
                    await fetchMyRooms(appState.nickName).then(rooms => renderRoomList(myRoomList, rooms, true)); // ✅ My Rooms - 나가기 버튼 있음
                } else {
                    // 로그인하지 않은 경우 내 방 목록 비우기
                    myRoomList.innerHTML = '';
                    const li = document.createElement('li');
                    li.textContent = 'Please login to see your rooms.';
                    li.dataset.roomName = '';
                    li.classList.add('no-interaction');
                    myRoomList.appendChild(li);
                }
            } else {
                await fetchAllRooms().then(rooms => renderRoomList(allRoomList, rooms, false)); // ✅ All Rooms - 나가기 버튼 없음
            }
            filterRooms();
        });
    });

    // --- Initial Load ---
    fetchAllRooms().then(rooms => renderRoomList(allRoomList, rooms, false)); // ✅ All Rooms - 나가기 버튼 없음

    // 페이지 언로드 시 연결 정리
    window.addEventListener('beforeunload', () => {
        console.log('Page unloading, cleaning up connections...');
        appState.isLoggedIn = false; // 재연결 방지

        if (appState.reconnectTimeout) {
            clearTimeout(appState.reconnectTimeout);
        }

        disconnectSSE();

        if (appState.websocket) {
            appState.websocket.close();
        }
    });

    // 페이지 가시성 변경 시 처리 (탭 전환 등) - 재연결 비활성화
    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            console.log('Page hidden');
        } else {
            console.log('Page visible - SSE reconnection disabled');
            // 페이지 가시성 변경으로 인한 재연결은 비활성화
            // 사용자가 의도적으로 재연결이 필요한 경우 로그아웃/로그인 사용
        }
    });

    function filterRooms() {
        const searchTerm = getInputValue(roomSearchInput).toLowerCase();
        const activeList = querySelector('.room-tab-content.active ul');
        if (!activeList) return;

        const rooms = activeList.querySelectorAll('li');
        rooms.forEach(room => {
            const roomName = room.dataset.roomName;
            if (roomName && roomName.toLowerCase().includes(searchTerm)) {
                room.style.display = '';
            } else {
                room.style.display = 'none';
            }
        });
    }
});
