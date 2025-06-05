package dlab.reactive.manage;

import dlab.reactive.model.Notification;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationSinkManager {

    private final Map<String, Sinks.Many<Notification>> sinksMap = new ConcurrentHashMap<>();

    public Sinks.Many<Notification> getSinks(String id){
        return sinksMap.computeIfAbsent(id, key -> Sinks.many()
                .multicast()
                .onBackpressureBuffer());
    }

    public void removeSinks(String id) {
        sinksMap.remove(id);
    }

}
