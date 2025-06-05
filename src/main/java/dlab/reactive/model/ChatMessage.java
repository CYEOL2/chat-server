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
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    private long chatMessageId;

    private long chatRoomId;

    private String senderNickname;

    private String content;

    private LocalDateTime sendDtime;

}
