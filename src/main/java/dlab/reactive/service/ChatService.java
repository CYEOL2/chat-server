package dlab.reactive.service;

import dlab.reactive.model.ChatMessage;
import dlab.reactive.model.ChatRoom;
import dlab.reactive.model.ChatRoomGuest;
import dlab.reactive.repositoty.ChatMessageRepository;
import dlab.reactive.repositoty.ChatRoomGuestRepository;
import dlab.reactive.repositoty.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomGuestRepository chatRoomGuestRepository;


    public ChatService(ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository, ChatRoomGuestRepository chatRoomGuestRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatRoomGuestRepository = chatRoomGuestRepository;
    }

    // 채팅방 생성
    public Mono<ChatRoom> createChatRoom(ChatRoom chatRoom) {
        return chatRoomRepository.save(chatRoom);
    }

    // 채팅방 전체조회
    public Flux<ChatRoom> getAllChatRooms() {
        return chatRoomRepository.findAll();
    }

    // 채팅방 조회
    public Mono<ChatRoom> getChatRoomById(long id) {
        return chatRoomRepository.findById(id);
    }

    // 채팅메시지 저장
    public Mono<ChatMessage> saveChatMessage(ChatMessage chatMessage){
        return chatMessageRepository.save(chatMessage);
    }

    public Mono<Boolean> existsById(long chatRoomId) {
        return chatRoomRepository.existsById(chatRoomId);

    }

    // 채팅방 접속
    public Mono<ChatRoomGuest> joinGuest(String chatRoomId, String nickName) {
        return chatRoomGuestRepository.existsByChatRoomIdAndNickName(Long.parseLong(chatRoomId), nickName)
                .flatMap(exists -> {
                    if(!exists){
                    ChatRoomGuest chatRoomGuest = ChatRoomGuest.builder()
                            .chatRoomId(Long.parseLong(chatRoomId))
                            .nickName(nickName)
                            .joinedDtime(LocalDateTime.now())
                            .lastPingDtime(LocalDateTime.now())
                            .build();
                    return chatRoomGuestRepository.save(chatRoomGuest);
                }else{
                    return Mono.empty();
                }});
    }
    // 채팅방 삭제
    public Mono<Void> deleteChatRoomById(long chatRoomId) {
        return chatRoomRepository.deleteById(chatRoomId);
    }

    // 채팅방 게스트 삭제
    public Mono<Void> deleteChatRoomGuestByChatRoomId(long chatRoomId) {
        return chatRoomGuestRepository.deleteByChatRoomId(chatRoomId);
    }

    // 채팅방 메세지 삭제
    public Mono<Void> deleteChatMessageByChatRoomId(long chatRoomId) {
        return chatMessageRepository.deleteByChatRoomId(chatRoomId);
    }
    
    // 채팅방 나가기
    public Mono<Void> deleteChatRoomGuestByChatRoomIdAndNickName(long chatRoomId, String nickName) {
        return chatRoomGuestRepository.existsByChatRoomIdAndNickName(chatRoomId, nickName)
                .flatMap(exists -> {
                    if(exists){
                        return chatRoomGuestRepository.deleteByChatRoomIdAndNickName(chatRoomId, nickName);
                    }else{
                        return Mono.empty();
                    }});

    }
    
    // 채팅메시지 조회
    public Flux<ChatMessage> getChatMessageByChatRoomId(long chatRoomId, long limit, long offset) {
        return chatMessageRepository.findByChatRoomId(chatRoomId, limit, offset);
    }

    // 채팅방 인원 조회
    public Flux<ChatRoomGuest> getChatRoomGuestByChatRoomId(long chatRoomId) {
        return chatRoomGuestRepository.findByChatRoomId(chatRoomId);
    }
    // 유저별 채팅방게스트 조회
    public Flux<ChatRoomGuest> getChatRoomGuestByNickName(String nickName) {
        return chatRoomGuestRepository.findByNickName(nickName);
    }
    // 유저별 채팅방 조회
    public Flux<ChatRoom> getChatRoomsByNickName(String nickName) {
        return chatRoomGuestRepository.findJoinedChatRoomsByNickName(nickName);
    }
}
