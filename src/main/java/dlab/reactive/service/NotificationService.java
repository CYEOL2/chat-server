package dlab.reactive.service;

import dlab.reactive.manage.NotificationSinkManager;
import dlab.reactive.model.Notification;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class NotificationService {

    private final NotificationSinkManager sinkManager;

    public NotificationService(NotificationSinkManager sinkManager) {
        this.sinkManager = sinkManager;
    }

    // 유저별 sink
    public Flux<Notification> getNotificationStream(String nickName){
        return sinkManager.getSinks(nickName).asFlux();
    }

    // 채팅방 별 sink를 하나의 flux로 merge
//    public Flux<Notification> getNotificationStream(String nickName){
//        return chatService.getChatRoomGuestByChatRoomId(nickName)
//                       .map(chatRoomGuest -> sinkManager.getSinks(String.valueOf(chatRoomGuest.getChatRoomId())))
//                       .map(Sinks.Many::asFlux)
//                       .collectList()
//                       .flatMapMany(Flux::merge);
//
//    }

}
