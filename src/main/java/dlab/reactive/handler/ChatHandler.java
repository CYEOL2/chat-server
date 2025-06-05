package dlab.reactive.handler;

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

@Component
public class ChatHandler {

    private final ChatService service;
    private final WebSocketSessionManager sessionManager;
    public ChatHandler(ChatService service, WebSocketSessionManager sessionManager) {
        this.service = service;
        this.sessionManager = sessionManager;
    }

    public Mono<ServerResponse> createChatRoom(ServerRequest request) {
        return request.bodyToMono(ChatRoom.class)
                .flatMap(chatRoom -> service.createChatRoom(chatRoom)
                        .flatMap(createdChatRoom -> ServerResponse.status(HttpStatus.CREATED).bodyValue(createdChatRoom))
                        .switchIfEmpty(ServerResponse.status(HttpStatus.BAD_REQUEST).build()));

    }

    public Mono<ServerResponse> getAllChatRooms(ServerRequest request) {
        return service.getAllChatRooms()
                .collectList()
                .flatMap(chatRooms -> ServerResponse.ok().bodyValue(chatRooms));
    }

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
                        .then(service.deleteChatMessageByChatRoomId(chatRoomId)))
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

    public Mono<ServerResponse> getChatMessageByChatRoomId(ServerRequest request) {
        long chatRoomId = Long.parseLong(request.pathVariable("chatRoomId"));
        long limit = request.queryParam("limit").map(Long::parseLong).orElse(100L);
        long pageNo = request.queryParam("pageNo").map(Long::parseLong).orElse(1L);
        long offset = limit * (pageNo-1);
        return service.getChatMessageByChatRoomId(chatRoomId, limit, offset)
                .collectList()
                .flatMap(chatMessages -> ServerResponse.ok().bodyValue(chatMessages));
    }

    public Mono<ServerResponse> getChatRoomGuestByChatRoomId(ServerRequest request) {
        long chatRoomId = Long.parseLong(request.pathVariable("chatRoomId"));
        return service.getChatRoomGuestByChatRoomId(chatRoomId)
                .collectList()
                .flatMap(chatRoomGuests -> ServerResponse.ok().bodyValue(chatRoomGuests));
    }

}
