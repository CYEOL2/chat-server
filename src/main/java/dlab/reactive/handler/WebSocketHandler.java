package dlab.reactive.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dlab.reactive.manage.NotificationSinkManager;
import dlab.reactive.model.ChatMessage;
import dlab.reactive.model.Notification;
import dlab.reactive.service.ChatService;
import dlab.reactive.session.WebSocketSessionManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class WebSocketHandler implements org.springframework.web.reactive.socket.WebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final NotificationSinkManager sinkManager;
    public WebSocketHandler(WebSocketSessionManager sessionManager, ChatService chatService, ObjectMapper objectMapper, NotificationSinkManager sinkManager){
        this.sessionManager = sessionManager;
        this.chatService = chatService;
        this.objectMapper = objectMapper;
        this.sinkManager = sinkManager;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String chatRoomId = getQueryParam(session, "chatRoomId");
        String nickName = getQueryParam(session, "nickName");

        if (chatRoomId == null || nickName == null) {
            return session.close(CloseStatus.BAD_DATA);
        }

        //closeStatus : 세션이 닫히는 순간을 감지하는 이벤트
        //doOnTerminate : Mono가 종료될때 무조건 수행되는 후처리 로직
        //subscribe : 실제 실행을 트리거함
        session.closeStatus()
                .doOnTerminate(() -> {
                    System.out.println("chatRoomId : " + chatRoomId + ", nickName : " + nickName);
                    System.out.println("removeSession");
                    sessionManager.removeSession(chatRoomId, nickName);})
                .subscribe();

        return chatService.existsById(Long.parseLong(chatRoomId))
                .flatMap(exists -> {
                    //채팅방 존재여부 확인
                    if (!exists) {
                        return session.close(CloseStatus.BAD_DATA);
                    }
                    //세션 추가
                    sessionManager.addSession(chatRoomId, nickName,session);

                    //thenMany 사용이유 session.receive가 flux를 반환하기 때문.
                    return chatService.joinGuest(chatRoomId, nickName)
                            .thenMany(session.receive()
                                    .flatMap(webSocketMessage -> {
                                        String payload = webSocketMessage.getPayloadAsText();
                                        System.out.println("chatRoomId : " + chatRoomId + ", nickName : " + nickName);
                                        System.out.println("수신 메시지: " + payload);
                                         ChatMessage chatMessage;
                                        try {
                                            //json 변환
                                            chatMessage = objectMapper.readValue(payload, ChatMessage.class);
                                            chatMessage.setSendDtime(LocalDateTime.now());
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException(e);
                                        }
                                        System.out.println("세션 수: " + sessionManager.getSessions(chatRoomId).size());
                                        //DB에 insert
                                        return chatService.saveChatMessage(chatMessage)
                                                .thenMany(Flux.fromIterable(sessionManager.getSessions(chatRoomId).values()))
                                                .flatMap(targetSession -> {
                                                    if (targetSession.isOpen()) {
                                                        try {
                                                            return targetSession.send(Mono.just(targetSession.textMessage(objectMapper.writeValueAsString(chatMessage))));
                                                        } catch (JsonProcessingException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                    } else {
                                                        return Mono.empty();
                                                    }
                                                })
                                                .thenMany(chatService.getChatRoomGuestByChatRoomId(Long.parseLong(chatRoomId)))
                                                .flatMap(chatRoomGuest -> {
                                                    if(!nickName.equals(chatRoomGuest.getNickName()) && !sessionManager.getSessions(chatRoomId).containsKey(chatRoomGuest.getNickName())){
                                                        Notification notification = Notification.builder()
                                                                .roomId(chatRoomId)
                                                                .title("새 메시지")
                                                                .message(nickName + "님이 메시지를 보냈습니다.")
                                                                .createDtime(LocalDateTime.now())
                                                                .build();
                                                        sinkManager.getSinks(chatRoomGuest.getNickName()).tryEmitNext(notification);
                                                    }
                                                    return Mono.empty();
                                                });
                                    })
                            ).then();
                });
    }

    private String getQueryParam(WebSocketSession session, String key){
        UriComponents components = UriComponentsBuilder.fromUri(session.getHandshakeInfo().getUri()).build();
        return components.getQueryParams().getFirst(key);
    }
}
