package dlab.reactive.config;

import dlab.reactive.handler.ChatHandler;
import dlab.reactive.handler.ClientIdHandler;
import dlab.reactive.handler.SSEHandler;
import dlab.reactive.handler.WebClientHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> chatRoutes(ChatHandler chatHandler){
        return RouterFunctions
                .route(RequestPredicates.POST("/chat-room")
                        .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON))
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  chatHandler::createChatRoom)
                .andRoute(RequestPredicates.GET("/chat-room").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  chatHandler::getAllChatRooms)
                .andRoute(RequestPredicates.GET("/chat-room/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  chatHandler::getChatRoomById)
                .andRoute(RequestPredicates.DELETE("/chat-room/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  chatHandler::deleteChatRoomById)
                .andRoute(RequestPredicates.DELETE("/chat-room-guest/{id}/{nickName}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  chatHandler::deleteChatRoomGuestById)
                .andRoute(RequestPredicates.GET("/chat-message/{chatRoomId}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  chatHandler::getChatMessageByChatRoomId)
                .andRoute(RequestPredicates.GET("/chat-guest/{chatRoomId}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  chatHandler::getChatRoomGuestByChatRoomId)
                ;
    }

    @Bean
    public RouterFunction<ServerResponse> webClientRoutes(WebClientHandler webClientHandler){
        return RouterFunctions.route(RequestPredicates.POST("/api/{org}")
                        .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON))
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  webClientHandler::handle)
                ;
    }

    @Bean
    public RouterFunction<ServerResponse> assignRoutes(ClientIdHandler clientIdHandler){
        return RouterFunctions.route(RequestPredicates.POST("/assignClientId").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  clientIdHandler::assignClientId)
                ;
    }

    @Bean
    public RouterFunction<ServerResponse> sseRoutes(SSEHandler sseHandler){
        return RouterFunctions.route(RequestPredicates.GET("/sse").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  sseHandler::stream)
                ;
    }
}
