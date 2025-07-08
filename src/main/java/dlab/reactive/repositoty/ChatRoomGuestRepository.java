package dlab.reactive.repositoty;

import dlab.reactive.model.ChatRoom;
import dlab.reactive.model.ChatRoomGuest;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ChatRoomGuestRepository extends ReactiveCrudRepository<ChatRoomGuest, Long> {

    Mono<Boolean> existsByChatRoomIdAndNickName(long chatRoomId, String nickname);

    Mono<Void> deleteByChatRoomIdAndNickName(long chatRoomId, String nickname);

    Mono<Void> deleteByChatRoomId(long chatRoomId);

    Flux<ChatRoomGuest> findByChatRoomId(long chatRoomId);

    Flux<ChatRoomGuest> findByNickName(String nickname);

    @Query("SELECT cr.* FROM chat_room_guest crg JOIN chat_room cr ON crg.chat_room_id = cr.chat_room_id WHERE crg.nick_name = :nickName")
    Flux<ChatRoom> findJoinedChatRoomsByNickName(String nickName);

}
