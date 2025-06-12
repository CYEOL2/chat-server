package dlab.reactive.config;

import dlab.reactive.handler.ChatHandler;
import dlab.reactive.handler.ClientIdHandler;
import dlab.reactive.handler.SSEHandler;
import dlab.reactive.handler.WebClientHandler;
import dlab.reactive.model.ChatRoom;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.*;

import java.util.Map;


@Configuration
public class RouterConfig {


    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/chat-room",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.POST,
                    beanClass = ChatHandler.class,
                    beanMethod = "createChatRoom",
                    operation = @Operation(
                            operationId = "createChatRoom",
                            summary = "채팅방 생성",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = ChatRoom.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            ),
            @RouterOperation(
                path = "/chat-room",
                produces = { MediaType.APPLICATION_JSON_VALUE },
                method = RequestMethod.GET,
                beanClass = ChatHandler.class,
                beanMethod = "getAllChatRooms",
                operation = @Operation(
                        operationId = "getAllChatRooms",
                        summary = "전체 채팅방 조회"
                )
            ),
            @RouterOperation(
                    path = "/chat-room/{id}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = ChatHandler.class,
                    beanMethod = "getChatRoomById",
                    operation = @Operation(
                            operationId = "getChatRoomById",
                            summary = "채팅방 조회 (ChatRoomId)",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/chat-room/{id}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.DELETE,
                    beanClass = ChatHandler.class,
                    beanMethod = "deleteChatRoomById",
                    operation = @Operation(
                            operationId = "deleteChatRoomById",
                            summary = "채팅방 삭제 (ChatRoomId)",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/chat-room-guest/{id}/{nickName}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.DELETE,
                    beanClass = ChatHandler.class,
                    beanMethod = "deleteChatRoomGuestById",
                    operation = @Operation(
                            operationId = "deleteChatRoomGuestById",
                            summary = "채팅방 나가기 (ChatRoomId, NickName)",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/chat-message/{chatRoomId}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = ChatHandler.class,
                    beanMethod = "getChatMessageByChatRoomId",
                    operation = @Operation(
                            operationId = "getChatMessageByChatRoomId",
                            summary = "채팅 메세지 조회 (chatRoomId)",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/chat-guest/{chatRoomId}",
                    produces = { MediaType.APPLICATION_JSON_VALUE },
                    method = RequestMethod.GET,
                    beanClass = ChatHandler.class,
                    beanMethod = "getChatRoomGuestByChatRoomId",
                    operation = @Operation(
                            operationId = "getChatRoomGuestByChatRoomId",
                            summary = "채팅방 인원 조회 (chatRoomId)",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            ),
            // 여기에 다른 엔드포인트도 동일한 방식으로 추가
    })
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
    @RouterOperations({
            @RouterOperation(
                    path = "/api/{org}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = WebClientHandler.class,
                    beanMethod = "handle",
                    operation = @Operation(
                            operationId = "handle",
                            summary = "외부 api 연계 호출",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(type = "object", example = "{\"targetHttpMethod\":\"POST\",\"subUrl\":\"/dlabapi/code/list\",\"body\":{}}")
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> webClientRoutes(WebClientHandler webClientHandler){
        return RouterFunctions.route(RequestPredicates.POST("/api/{org}")
                        .and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)),  webClientHandler::handle)
                ;
    }

    @RouterOperations({
            @RouterOperation(
                    path = "/sse/{nickName}",
                    produces = {MediaType.TEXT_EVENT_STREAM_VALUE},
                    method = RequestMethod.GET,
                    beanClass = SSEHandler.class,
                    beanMethod = "stream",
                    operation = @Operation(
                            operationId = "stream",
                            summary = "SSE 이벤트 스트림 연결",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "성공")
                            }
                    )
            )
    })
    @Bean
    public RouterFunction<ServerResponse> sseRoutes(SSEHandler sseHandler) {
        return RouterFunctions.route(RequestPredicates.GET("/sse/{nickName}").and(RequestPredicates.accept(MediaType.TEXT_EVENT_STREAM)),sseHandler::stream)
                ;
    }

    @Bean
    public RouterFunction<ServerResponse> assignRoutes(ClientIdHandler clientIdHandler){
        return RouterFunctions.route(RequestPredicates.POST("/assignClientId").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),  clientIdHandler::assignClientId)
                ;
    }
}
