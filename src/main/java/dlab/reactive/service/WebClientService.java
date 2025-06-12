package dlab.reactive.service;

import dlab.reactive.factory.WebClientFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class WebClientService {

    private final WebClientFactory webClientFactory;

    private final String defaultSubUrl = "";
    private final String defaultHttpMethod = "POST";

    public WebClientService(WebClientFactory webClientFactory) {
        this.webClientFactory = webClientFactory;
    }
    public Mono<Map<String, Object>> send(Map<String, Object> map, String orgName){
        return send(map, orgName, defaultSubUrl, defaultHttpMethod);
    }

    public Mono<Map<String, Object>> send(Map<String, Object> map, String orgName, String subUrl, String targetHttpMethod) {

        WebClient webClient = webClientFactory.getClient(orgName);

        HttpMethod httpMethod = HttpMethod.valueOf(targetHttpMethod);

        WebClient.RequestBodySpec request =  webClient.method(httpMethod)
                .uri(subUrl);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) map.get("body");

        // onStatus 에러시 동작
        if (HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod) || HttpMethod.PATCH.equals(httpMethod)) {
            return request
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                            .flatMap(error -> Mono.error(new RuntimeException("HTTP Error: " + error))))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    ;
                    //.log();
        } else {
            return request
                    .retrieve()
                    .onStatus(HttpStatus::isError, response -> response.bodyToMono(String.class)
                            .flatMap(error -> Mono.error(new RuntimeException("HTTP Error: " + error))))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    ;
                    //.log();

        }

    }
}
