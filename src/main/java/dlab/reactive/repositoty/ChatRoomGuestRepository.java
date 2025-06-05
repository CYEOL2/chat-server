package dlab.reactive.repositoty;

import dlab.reactive.model.ChatRoomGuest;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ChatRoomGuestRepository extends ReactiveCrudRepository<ChatRoomGuest, Long> {

    Mono<Boolean> existsByChatRoomIdAndNickName(long chatRoomId, String NickName);

    Mono<Void> deleteByChatRoomIdAndNickName(long chatRoomId, String NickName);

    Mono<Void> deleteByChatRoomId(long chatRoomId);

    Flux<ChatRoomGuest> findByChatRoomId(long chatRoomId);
}
