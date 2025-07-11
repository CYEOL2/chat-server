package dlab.reactive.handler;

import dlab.reactive.manage.NotificationSinkManager;
import dlab.reactive.model.ChatRoom;
import dlab.reactive.service.ChatService;
import dlab.reactive.session.WebSocketSessionManager;
import io.netty.handler.codec.http2.Http2PushPromiseFrame;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;

@Component
public class ChatHandler {

    private final ChatService service;
    private final WebSocketSessionManager sessionManager;
    private final NotificationSinkManager notificationSinkManager;
    public ChatHandler(ChatService service, WebSocketSessionManager sessionManager, NotificationSinkManager notificationSinkManager) {
        this.service = service;
        this.sessionManager = sessionManager;
        this.notificationSinkManager = notificationSinkManager;
    }
    
    // 채팅방 생성
    public Mono<ServerResponse> createChatRoom(ServerRequest request) {
        return request.bodyToMono(ChatRoom.class)
                .flatMap(chatRoom -> service.createChatRoom(chatRoom)
                        .flatMap(createdChatRoom -> ServerResponse.status(HttpStatus.CREATED).bodyValue(createdChatRoom))
                        .switchIfEmpty(ServerResponse.status(HttpStatus.BAD_REQUEST).build()));

    }

    // 전체 채팅방 조회
    public Mono<ServerResponse> getAllChatRooms(ServerRequest request) {
        return service.getAllChatRooms()
                .collectList()
                .flatMap(chatRooms -> ServerResponse.ok().bodyValue(chatRooms));
    }

    // 채팅방 조회 (chatRoomId)
    public Mono<ServerResponse> getChatRoomById(ServerRequest request) {
        long chatRoomId = Long.parseLong(request.pathVariable("id"));
        return service.getChatRoomById(chatRoomId)
                .flatMap(chatRoom ->  ServerResponse.ok().bodyValue(chatRoom)
                        .switchIfEmpty(ServerResponse.status(HttpStatus.NOT_FOUND).build()));
    }

    // 채팅방 삭제 (CASCADE)
    public Mono<ServerResponse> deleteChatRoomById(ServerRequest request){
        long chatRoomId = Long.parseLong(request.pathVariable("id"));
        // then 사용이유 Mono<Void>를 리턴하기 때문에.
        return service.deleteChatRoomById(chatRoomId)
                .then(service.deleteChatRoomGuestByChatRoomId(chatRoomId)
                        .then(service.deleteChatMessageByChatRoomId(chatRoomId))
                        .then(notificationSinkManager.removeSinks(String.valueOf(chatRoomId))))
                .then(ServerResponse.ok().build());
    }

    // 채팅방 나가기
    public Mono<ServerResponse> deleteChatRoomGuestById(ServerRequest request){
        long chatRoomId = Long.parseLong(request.pathVariable("id"));
        String nickName = request.pathVariable("nickName");
        // then 사용이유 Mono<Void>를 리턴하기 때문에.
        return service.deleteChatRoomGuestByChatRoomIdAndNickName(chatRoomId, nickName)
                .flatMap(unused -> Mono.fromRunnable(() -> sessionManager.removeSession(String.valueOf(chatRoomId), nickName)))
                .then(ServerResponse.ok().build());
    }

    // 채팅 메세지 조회 (chatRoomId)
    public Mono<ServerResponse> getChatMessageByChatRoomId(ServerRequest request) {
        long chatRoomId = Long.parseLong(request.pathVariable("chatRoomId"));
        long limit = request.queryParam("limit").map(Long::parseLong).orElse(100L);
        long pageNo = request.queryParam("pageNo").map(Long::parseLong).orElse(1L);
        long offset = limit * (pageNo-1);
        return service.getChatMessageByChatRoomId(chatRoomId, limit, offset)
                .collectList()
                .flatMap(chatMessages -> ServerResponse.ok().bodyValue(chatMessages));
    }

    // 채팅 게스트 조회 (chatRoomId)
    public Mono<ServerResponse> getChatRoomGuestByChatRoomId(ServerRequest request) {
        long chatRoomId = Long.parseLong(request.pathVariable("chatRoomId"));
        return service.getChatRoomGuestByChatRoomId(chatRoomId)
                .collectList()
                .flatMap(chatRoomGuests -> ServerResponse.ok().bodyValue(chatRoomGuests));
    }

    // 사용자가 속한 채팅방 조회
    public Mono<ServerResponse> getChatRoomsByNickName(ServerRequest request) {
        String nickName = request.pathVariable("nickName");
        return service.getChatRoomsByNickName(nickName)
                .collectList()
                .flatMap(chatRooms -> ServerResponse.ok().bodyValue(chatRooms));
    }

    // 채팅방에 접속중인 사용자 조회
    public Mono<ServerResponse> getSessionsByChatRoomId(ServerRequest request) {
        String chatRoomId = request.pathVariable("chatRoomId");

        return Mono.fromCallable(() -> sessionManager.getSessions(chatRoomId))
                .map(sessionMap -> {
                    if (sessionMap == null || sessionMap.isEmpty()) {
                        return Collections.emptyList();
                    }else{
                        return new ArrayList<>(sessionMap.keySet());
                    }
                })
                .flatMap(nickNames ->
                        ServerResponse.ok()
                                .bodyValue(nickNames)
                )
                .onErrorResume(e ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue("Error retrieving sessions for chatRoom: " + chatRoomId)
                );
    }
}
