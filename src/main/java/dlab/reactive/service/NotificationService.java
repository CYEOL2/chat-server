package dlab.reactive.service;

import dlab.reactive.manage.NotificationSinkManager;
import dlab.reactive.model.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class NotificationService {

    private NotificationSinkManager sinkManager;
    private ChatService chatService;
    public NotificationService(NotificationSinkManager sinkManager, ChatService chatService) {
        this.sinkManager = sinkManager;
        this.chatService = chatService;
    }

    // 채팅방 별 sink를 하나의 flux로 merge
    public Flux<Notification> getNotificationStream(String nickName){
        return chatService.getChatRoomGuestByChatRoomId(nickName)
                       .map(chatRoomGuest -> sinkManager.getSinks(String.valueOf(chatRoomGuest.getChatRoomId())))
                       .map(Sinks.Many::asFlux)
                       .collectList()
                       .flatMapMany(Flux::merge);

    }

}
