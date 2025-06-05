package dlab.reactive.repositoty;

import dlab.reactive.model.ChatRoom;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ChatRoomRepository extends ReactiveCrudRepository<ChatRoom, Long> {

}
