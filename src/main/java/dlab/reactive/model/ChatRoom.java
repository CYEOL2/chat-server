package dlab.reactive.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Slf4j
@Data
@Builder
@Table(name = "chat_room")
@Schema(description = "채팅방")
public class ChatRoom {

    @Id
    @Schema(hidden = true)
    private long chatRoomId;
    @Schema(description = "채팅방 이름", example = "방1")
    private String name;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(hidden = true)
    private LocalDateTime createDtime;

}
