package dlab.reactive.webclient;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
public class WebClientAsyncTest {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Test
    void webClient_is_non_blocking() {
        WebClient webClient = webClientBuilder
                .baseUrl("http://fbc.digitalab.co.kr:18080/dims-dev-service")
                .build();

        Map<String, Object> body = new HashMap<>();

        body.put("codeGroupId", "FRM10007");
        body.put("lang", "ko");


        List<Mono<String>> requests = IntStream.range(0, 5)
                .mapToObj(i -> {
                    String label = "REQ-" + i;
                    return webClient.post()
                            .uri("/dlabapi/code/list")
                            .bodyValue(body) // httpbin이 2초 지연 응답
                            .retrieve()
                            .bodyToMono(String.class)
                            .doOnSubscribe(sub -> log.info("[{}] Subscribed on thread: {}", label, Thread.currentThread().getName()))
                            .doOnNext(res -> log.info("[{}] Response received on thread: {}", label, Thread.currentThread().getName()))
                            .doOnError(e -> log.error("[{}] Error: {}", label, e.getMessage()));
                })
                .collect(Collectors.toList());

        // 병렬 subscribe → 논블로킹 검증
        Mono<Void> combined = Mono.when(requests);

        StepVerifier.create(combined)
                .expectSubscription()
                .verifyComplete();
    }
}
