package dlab.reactive.repositoty;

import dlab.reactive.model.ChatMessage;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, Long> {

    Mono<Void> deleteByChatRoomId(long chatRoomId);

    @Query("SELECT * FROM chat_message WHERE chat_room_id = :chatRoomId ORDER BY send_dtime DESC LIMIT :limit OFFSET :offset")
    Flux<ChatMessage> findByChatRoomId(long chatRoomId, long limit, long offset);

}
