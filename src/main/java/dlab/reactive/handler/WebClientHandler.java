package dlab.reactive.handler;

import dlab.reactive.factory.WebClientFactory;
import dlab.reactive.model.ChatRoom;
import dlab.reactive.property.ApiBaseUrlProperties;
import dlab.reactive.service.WebClientService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class WebClientHandler {

    private final WebClientService webClientService;

    public WebClientHandler(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    public Mono<ServerResponse> handle(ServerRequest request) {
        String orgName = request.pathVariable("org");
        return request.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(map -> {
                    String targetHttpMethod = (String) map.get("targetHttpMethod");
                    String subUrl = (String) map.get("subUrl");
                    if(targetHttpMethod == null || targetHttpMethod.isEmpty()){
                        return Mono.error(() -> new IllegalArgumentException("method is null"));
                    };
                    return webClientService.send(map, orgName, subUrl,targetHttpMethod);
                })
                .flatMap(resMap -> ServerResponse.ok().bodyValue(resMap));

    }

}

