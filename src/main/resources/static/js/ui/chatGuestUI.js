// src/main/resources/static/js/ui/chatGuestUI.js

import { getElement } from '../utils/dom.js';
import { fetchChatRoomGuests, fetchOnlineUsers } from '../services/api.js'; // API 호출 함수 임포트

const chatGuestList = getElement('chat-guest-list');

// 현재 온라인 사용자들을 추적하는 Set
let currentOnlineUsers = new Set();

export async function renderChatGuests(chatRoomId) {
    chatGuestList.innerHTML = ''; // 기존 목록 초기화

    if (!chatRoomId) {
        const li = document.createElement('li');
        li.textContent = '채팅방을 선택하여 참가자를 확인하세요.';
        chatGuestList.appendChild(li);
        return;
    }

    try {
        const [guests, onlineUsers] = await Promise.all([
            fetchChatRoomGuests(chatRoomId),
            fetchOnlineUsers(chatRoomId)
        ]);

        if (guests && guests.length > 0) {
            // 온라인 사용자 Set 업데이트
            currentOnlineUsers = new Set(onlineUsers);

            // 온라인/오프라인 사용자 분리
            const onlineGuests = [];
            const offlineGuests = [];

            guests.forEach(guest => {
                if (currentOnlineUsers.has(guest.nickName)) {
                    onlineGuests.push(guest);
                } else {
                    offlineGuests.push(guest);
                }
            });

            // 온라인 사용자 먼저 렌더링
            onlineGuests.forEach(guest => {
                const li = createGuestListItem(guest.nickName, true);
                chatGuestList.appendChild(li);
            });

            // 오프라인 사용자 렌더링
            offlineGuests.forEach(guest => {
                const li = createGuestListItem(guest.nickName, false);
                chatGuestList.appendChild(li);
            });

        } else {
            const li = document.createElement('li');
            li.textContent = '이 채팅방에는 참가자가 없습니다.';
            chatGuestList.appendChild(li);
        }
    } catch (error) {
        console.error('Failed to fetch chat room guests:', error);
        const li = document.createElement('li');
        li.textContent = '참가자 로드 중 오류 발생.';
        chatGuestList.appendChild(li);
    }
}

// 사용자 목록 아이템 생성 함수
function createGuestListItem(nickName, isOnline) {
    const li = document.createElement('li');
    li.setAttribute('data-nickname', nickName);
    li.innerHTML = `
        <span class="status-circle ${isOnline ? 'online' : 'offline'}"></span> 
        <span class="nickname">${nickName}</span>
    `;
    return li;
}

// WebSocket 메시지를 통한 실시간 온라인 상태 업데이트
export function handleUserStatusUpdate(message) {
    if (!message.messageType) return;

    const nickname = message.senderNickname;
    const messageType = message.messageType;

    if (messageType === 'JOIN') {
        updateUserOnlineStatus(nickname, true);
        showStatusNotification(nickname, 'joined');
    } else if (messageType === 'LEAVE') {
        updateUserOnlineStatus(nickname, false);
        showStatusNotification(nickname, 'left');
    }
}

// 특정 사용자의 온라인 상태 업데이트
function updateUserOnlineStatus(nickname, isOnline) {
    // 현재 온라인 사용자 Set 업데이트
    if (isOnline) {
        currentOnlineUsers.add(nickname);
    } else {
        currentOnlineUsers.delete(nickname);
    }

    // DOM에서 해당 사용자 찾기
    const userItem = chatGuestList.querySelector(`li[data-nickname="${nickname}"]`);
    
    if (userItem) {
        const statusCircle = userItem.querySelector('.status-circle');
        const li = userItem;

        if (isOnline) {
            // 온라인으로 변경
            statusCircle.classList.remove('offline');
            statusCircle.classList.add('online');
            
            // 온라인 사용자를 목록 상단으로 이동
            moveToOnlineSection(li);
            
            // 접속 애니메이션
            li.classList.add('user-join-animation');
            setTimeout(() => li.classList.remove('user-join-animation'), 1000);
            
        } else {
            // 오프라인으로 변경
            statusCircle.classList.remove('online');
            statusCircle.classList.add('offline');
            
            // 오프라인 사용자를 목록 하단으로 이동
            moveToOfflineSection(li);
            
            // 퇴장 애니메이션
            li.classList.add('user-leave-animation');
            setTimeout(() => li.classList.remove('user-leave-animation'), 1000);
        }
    }
}

// 온라인 섹션으로 이동
function moveToOnlineSection(userItem) {
    const allItems = Array.from(chatGuestList.children);
    const onlineItems = allItems.filter(item => 
        item.querySelector('.status-circle.online')
    );
    
    // 첫 번째 오프라인 사용자 앞에 삽입
    const firstOfflineItem = allItems.find(item => 
        item.querySelector('.status-circle.offline')
    );
    
    if (firstOfflineItem) {
        chatGuestList.insertBefore(userItem, firstOfflineItem);
    } else {
        // 모든 사용자가 온라인이면 마지막에 추가
        chatGuestList.appendChild(userItem);
    }
}

// 오프라인 섹션으로 이동
function moveToOfflineSection(userItem) {
    // 목록 마지막으로 이동
    chatGuestList.appendChild(userItem);
}

// 상태 변경 알림 표시
function showStatusNotification(nickname, action) {
    // 기존 알림이 있으면 제거
    const existingNotification = document.querySelector('.status-notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    // 새 알림 생성
    const notification = document.createElement('div');
    notification.className = 'status-notification';
    notification.textContent = `${nickname}님이 ${action === 'joined' ? '입장' : '퇴장'}했습니다.`;
    
    // 스타일 적용
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${action === 'joined' ? '#28a745' : '#dc3545'};
        color: white;
        padding: 10px 15px;
        border-radius: 5px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.2);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    `;

    document.body.appendChild(notification);

    // 3초 후 자동 제거
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease-in';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// 온라인 사용자 수 반환
export function getOnlineUserCount() {
    return currentOnlineUsers.size;
}

// 현재 온라인 사용자 목록 반환
export function getCurrentOnlineUsers() {
    return Array.from(currentOnlineUsers);
}

export function clearChatGuests() {
    chatGuestList.innerHTML = '';
    currentOnlineUsers.clear();
    const li = document.createElement('li');
    li.textContent = '채팅방을 선택하여 참가자를 확인하세요.';
    chatGuestList.appendChild(li);
}