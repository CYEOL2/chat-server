package dlab.reactive.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Slf4j
@Data
@Builder
@Table(name = "chat_room_guest")
public class ChatRoomGuest {

    @Id
    private long chatRoomGuestId;

    private String nickName;

    private long chatRoomId;

    private LocalDateTime joinedDtime;

    private LocalDateTime lastPingDtime;

}
