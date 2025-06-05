package dlab.reactive.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class Notification {

    private String roomId;

    private String title;

    private String message;

    private LocalDateTime createDtime;

}
