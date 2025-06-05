package dlab.reactive.handler;

import dlab.reactive.model.Notification;
import dlab.reactive.service.NotificationService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SSEHandler {

    private final NotificationService notificationService;

    public SSEHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public Mono<ServerResponse> stream(ServerRequest request) {
        String nickName = request.pathVariable("nickName");

        Flux<ServerSentEvent<Notification>> eventStream = notificationService.getNotificationStream(nickName)
                .map(notification -> ServerSentEvent.<Notification>builder()
                        .id(notification.getRoomId() + System.currentTimeMillis())
                        .event("alert-message")
                        .data(notification)
                        .build()
                );


        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventStream, new ParameterizedTypeReference<ServerSentEvent<Notification>>(){});
    }
}
