package dlab.reactive.session;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    // roomId -> (nickName -> session)
    private final Map<String, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public void addSession(String roomId, String nickName, WebSocketSession session) {
        //roomId 별로 ConcurrentHashMap 생성
        sessions.computeIfAbsent(roomId, key -> new ConcurrentHashMap<>())
                .put(nickName, session);
    }

    public void removeSession(String chatRoomId, String nickName) {
        Map<String, WebSocketSession> roomSessions = sessions.get(chatRoomId);
        if (roomSessions != null) {
            roomSessions.remove(nickName);
            if (roomSessions.isEmpty()) {
                sessions.remove(chatRoomId);
            }
        }
    }

    public Map<String, WebSocketSession> getSessions(String chatRoomId) {
        return sessions.get(chatRoomId);
    }
}
