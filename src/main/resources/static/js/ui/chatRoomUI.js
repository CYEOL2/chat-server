// src/main/resources/static/js/ui/chatRoomUI.js

import { getElement, addClass, removeClass } from '../utils/dom.js';
import { appState } from '../state/appState.js';
import { connectWebSocket, disconnectWebSocket } from '../services/websocket.js';
import { leaveRoom, fetchMyRooms, fetchAllRooms } from '../services/api.js';
import { showNotification } from '../utils/notification.js';

const allRoomList = getElement('all-room-list');
const myRoomList = getElement('my-room-list');

// 채팅방 나가기 함수
async function handleLeaveRoom(roomId, roomName, event) {
    event.stopPropagation(); // 이벤트 버블링 방지
    
    if (!confirm(`정말로 "${roomName}" 채팅방에서 나가시겠습니까?`)) {
        return;
    }

    const success = await leaveRoom(roomId, appState.nickName);
    if (success) {
        showNotification('Success', `"${roomName}" 채팅방에서 나갔습니다.`, 'success', 3000);
        
        // 현재 접속 중인 방이었다면 연결 해제
        if (appState.currentRoomId === roomId) {
            disconnectWebSocket();
        }
        
        // 내 채팅방 목록 새로고침
        const myRooms = await fetchMyRooms(appState.nickName);
        renderRoomList(myRoomList, myRooms, true); // My Rooms에는 나가기 버튼 표시
        
        // 전체 채팅방 목록도 새로고침 (참여자 수 변경 반영)
        const allRooms = await fetchAllRooms();
        renderRoomList(allRoomList, allRooms, false); // All Rooms에는 나가기 버튼 없음
    }
}

export function renderRoomList(listElement, rooms, showLeaveButton = false) {
    listElement.innerHTML = '';
    if (rooms.length === 0) {
        const li = document.createElement('li');
        li.textContent = showLeaveButton ? 'No rooms joined.' : 'No rooms found.';
        li.dataset.roomName = '';
        addClass(li, 'no-interaction');
        listElement.appendChild(li);
        return;
    }
    
    rooms.forEach(room => {
        const li = document.createElement('li');
        li.dataset.roomId = room.chatRoomId;
        li.dataset.roomName = room.name;
        addClass(li, 'room-item'); // 스타일링을 위한 클래스

        // 룸 컨테이너 (방 이름 + 버튼)
        const roomContainer = document.createElement('div');
        roomContainer.className = 'room-container';

        // 방 이름 영역
        const roomNameSpan = document.createElement('span');
        roomNameSpan.className = 'room-name';
        roomNameSpan.textContent = room.name;
        
        // 방 이름 클릭 시 채팅방 접속
        roomNameSpan.addEventListener('click', () => {
            setActiveRoom(li);
            connectWebSocket(room.chatRoomId, room.name);
        });

        roomContainer.appendChild(roomNameSpan);

        // My Rooms에만 나가기 버튼 추가
        if (showLeaveButton) {
            const leaveBtn = document.createElement('button');
            leaveBtn.className = 'leave-room-btn';
            leaveBtn.textContent = 'X';
            leaveBtn.title = `${room.name} 채팅방 나가기`;
            
            leaveBtn.addEventListener('click', (event) => {
                handleLeaveRoom(room.chatRoomId, room.name, event);
            });
            
            roomContainer.appendChild(leaveBtn);
        }

        li.appendChild(roomContainer);

        // 현재 접속 중인 방이면 active 클래스 추가
        if (appState.currentRoomId && appState.currentRoomId == room.chatRoomId) {
            addClass(li, 'active');
            appState.activeRoomElement = li;
        }

        // 새 메시지가 있는 경우 UI 복원
        const messageCount = appState.newMessageCounts[room.chatRoomId];
        if (messageCount && messageCount > 0) {
            addClass(li, 'has-new-message');

            // 메시지 카운트 배지 추가
            const badge = document.createElement('span');
            badge.className = 'message-count-badge';
            badge.textContent = messageCount;
            roomNameSpan.appendChild(badge);
        }

        // All Rooms의 경우에만 전체 li 클릭 가능 (My Rooms는 roomNameSpan만 클릭 가능)
        if (!showLeaveButton) {
            li.addEventListener('click', () => {
                setActiveRoom(li);
                connectWebSocket(room.chatRoomId, room.name);
            });
        }

        listElement.appendChild(li);
    });
}

export function setActiveRoom(roomElement) {
    if (appState.activeRoomElement) {
        removeClass(appState.activeRoomElement, 'active');
    }
    appState.activeRoomElement = roomElement;
    addClass(appState.activeRoomElement, 'active');
}

export function updateRoomListWithNewMessage(chatRoomId) {
    console.log('Updating room list for new message in room:', chatRoomId);

    // 메시지 카운트 증가
    appState.newMessageCounts[chatRoomId] = (appState.newMessageCounts[chatRoomId] || 0) + 1;

    // 채팅방 목록에서 해당 채팅방에 새 메시지 표시
    const allRoomItems = allRoomList.querySelectorAll('li');
    const myRoomItems = myRoomList.querySelectorAll('li');

    let updated = false;
    [...allRoomItems, ...myRoomItems].forEach(item => {
        if (item.dataset.roomId == chatRoomId) {
            // 기존 스타일 제거
            item.style.fontWeight = '';
            item.style.backgroundColor = '';
            item.style.border = '';

            // 새로운 클래스 추가
            addClass(item, 'has-new-message');

            // 기존 배지 제거
            const existingBadge = item.querySelector('.message-count-badge');
            if (existingBadge) {
                existingBadge.remove();
            }

            // 새 메시지 카운트 배지 추가
            const badge = document.createElement('span');
            badge.className = 'message-count-badge';
            badge.textContent = appState.newMessageCounts[chatRoomId];
            const roomNameSpan = item.querySelector('.room-name');
            if (roomNameSpan) {
                roomNameSpan.appendChild(badge);
            }

            updated = true;
            console.log(`Updated room item: ${item.textContent} (${appState.newMessageCounts[chatRoomId]} new messages)`);
        }
    });

    if (!updated) {
        console.log('No room item found to update for roomId:', chatRoomId);
    }
}

export function clearNewMessageIndicators(roomId) {
    console.log('Clearing new message indicators for room:', roomId);

    // 메시지 카운트 리셋
    appState.newMessageCounts[roomId] = 0;

    // 모든 탭의 채팅방 목록에서 해당 roomId의 새 메시지 표시 제거
    const allRoomItems = allRoomList.querySelectorAll('li');
    const myRoomItems = myRoomList.querySelectorAll('li');

    [...allRoomItems, ...myRoomItems].forEach(item => {
        if (item.dataset.roomId == roomId) {
            // 기존 스타일 초기화
            item.style.fontWeight = '';
            item.style.backgroundColor = '';
            item.style.border = '';

            // 새 메시지 클래스 제거
            removeClass(item, 'has-new-message');

            // 메시지 카운트 배지 제거
            const badge = item.querySelector('.message-count-badge');
            if (badge) {
                badge.remove();
            }

            console.log('Cleared new message indicator for room:', item.textContent);
        }
    });
}
