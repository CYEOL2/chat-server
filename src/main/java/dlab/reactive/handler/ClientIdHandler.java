package dlab.reactive.handler;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ClientIdHandler {

    public Mono<ServerResponse> assignClientId(ServerRequest request){
        String clientId = UUID.randomUUID().toString();
        Map<String, String> body = new HashMap<>();
        body.put("message", "success");

        return ServerResponse.ok()
                .cookie(ResponseCookie.from("clientId", clientId)
                        .httpOnly(true)
                        .path("/")
                        .maxAge(Duration.ofDays(1))
                        .build())
                .bodyValue(body);
    }
}
