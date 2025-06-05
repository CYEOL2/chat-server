package dlab.reactive.factory;

import dlab.reactive.property.ApiBaseUrlProperties;
import io.netty.handler.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebClientFactory {
    private final ApiBaseUrlProperties urls;
    private final Map<String, WebClient> webClients = new ConcurrentHashMap<>();

    public WebClientFactory(ApiBaseUrlProperties urls) {
        this.urls = urls;
    }

    public WebClient getClient(String org) {
        //computeIfAbsent key가 없으면 값을 생성후 put + value 반환, 있으면 value 반환
        return webClients.computeIfAbsent(org, key -> {
            String baseUrl = urls.getUrls().get(key);
            if (baseUrl == null) {
                throw new IllegalArgumentException("Unknown org: " + key);
            }
            // clientConnector 추가 이유 : 기본동작도 ReactorClientHttpConnector로 하지만 httpClient를 추가하기위함.
            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)))
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        });
    }

}
