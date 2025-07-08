package dlab.reactive.manage;

import dlab.reactive.model.Notification;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSinkManager {

    private final Map<String, Sinks.Many<Notification>> sinksMap = new ConcurrentHashMap<>();

    public Sinks.Many<Notification> createSinks(String nickName){
        System.out.println("creating sink for user: " + nickName);
        Sinks.Many<Notification> sinks = Sinks.many()
                .multicast()
                .onBackpressureBuffer();
        sinksMap.put(nickName, sinks);
        return sinks;
    }

    public Sinks.Many<Notification> getSinks(String nickName){
        System.out.println("Getting sink for user: " + nickName);
        System.out.println("Total sinks in map: " + sinksMap.size());
        return sinksMap.get(nickName);
    }

    public Mono<Void> removeSinks(String nickName) {
        sinksMap.remove(nickName);
        return Mono.empty();
    }

}
