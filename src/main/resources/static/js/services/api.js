// src/main/resources/static/js/services/api.js

import { showNotification } from '../utils/notification.js';

export async function fetchAllRooms() {
    try {
        const response = await fetch('/chat-room');
        if (!response.ok) throw new Error('Failed to fetch all rooms');
        return await response.json();
    } catch (error) {
        console.error('Error fetching all rooms:', error);
        showNotification('Error', 'Failed to load chat rooms.', 'error');
        return [];
    }
}

export async function fetchMyRooms(nickName) {
    if (!nickName) {
        return [];
    }
    try {
        const response = await fetch(`/chat-room/user/${nickName}`);
        if (!response.ok) {
            showNotification('Error', 'Could not fetch your rooms.', 'error');
            throw new Error('Failed to fetch my rooms');
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching my rooms:', error);
        return [];
    }
}

export async function createRoom(roomName) {
    try {
        const response = await fetch('/chat-room', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name: roomName })
        });
        if (!response.ok) throw new Error('Failed to create room');
        return true;
    } catch (error) {
        console.error('Error creating room:', error);
        showNotification('Error', 'Failed to create room.', 'error');
        return false;
    }
}

export async function leaveRoom(roomId, nickName) {
    try {
        const response = await fetch(`/chat-room-guest/${roomId}/${nickName}`, {
            method: 'DELETE',
            headers: { 'Accept': 'application/json' }
        });
        if (!response.ok) throw new Error('Failed to leave room');
        return true;
    } catch (error) {
        console.error('Error leaving room:', error);
        showNotification('Error', 'Failed to leave room.', 'error');
        return false;
    }
}

export async function loadChatHistory(roomId) {
    try {
        const response = await fetch(`/chat-message/${roomId}`);
        if (!response.ok) throw new Error('Failed to fetch chat history');
        const history = await response.json();
        // 메시지를 시간순으로 정렬 (오래된 것부터)
        return history.sort((a, b) => {
            if (a.createdAt && b.createdAt) {
                return new Date(a.createdAt) - new Date(b.createdAt);
            }
            return 0;
        });
    } catch (error) {
        console.error('Error loading chat history:', error);
        showNotification('Error', 'Failed to load chat history.', 'error');
        return [];
    }
}
