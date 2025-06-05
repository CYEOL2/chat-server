package dlab.reactive.config;

import dlab.reactive.handler.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    // HandshakeWebSocketService를 생성하는 빈
    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy()); // WebSocket 업그레이드를 위한 전략 설정
    }


    //실제 WebSocket 프로토콜을 지원하도록 Spring에 알려줌
    @Bean
    public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService){
        return new WebSocketHandlerAdapter(webSocketService);
    }

    //URI → 핸들러 매핑
    //WebSocketHandlerMapping - Spring MVC (Servlet 기반)
    //SimpleUrlHandlerMapping - Spring WebFlux (Reactive 기반)
    @Bean
    public SimpleUrlHandlerMapping websocketMapping(ChatWebSocketHandler chatWebSocketHandler){
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/chat", chatWebSocketHandler); // URI에 따라 핸들러 매핑


        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1); // 우선순위
        mapping.setUrlMap(map);
        return mapping;
    }

}
