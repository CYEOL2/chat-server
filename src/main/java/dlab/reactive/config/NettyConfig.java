package dlab.reactive.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.resources.LoopResources;

@Configuration
public class NettyConfig {

    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();

        factory.addServerCustomizers(httpServer ->
                httpServer
                        // 서버가 동시에 수용 가능한 연결 요청 수 (TCP Listen Queue 길이)
                        // backlog가 부족하면 다수 연결이 동시에 들어올 때 connection refused 발생 가능
                        .option(ChannelOption.SO_BACKLOG, 4096)

                        // 클라이언트가 연결을 시도할 때, 연결 수립까지 허용할 최대 대기 시간 (밀리초)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)

                        // 연결을 지속시킴 (keep-alive) → TCP 연결 재활용 가능, 짧은 요청 간격에 효과적
                        .childOption(ChannelOption.SO_KEEPALIVE, true)

                        // 작은 패킷도 지연 없이 바로 전송 (Nagle 알고리즘 비활성화) → 지연 시간 최소화
                        .childOption(ChannelOption.TCP_NODELAY, true)

                        // TCP 수신 버퍼 크기 설정 (1MB) → 클라이언트가 보내는 데이터 처리 여유 확보
                        .childOption(ChannelOption.SO_RCVBUF, 1024 * 1024)

                        // TCP 송신 버퍼 크기 설정 (1MB) → 서버가 클라이언트에 데이터 보낼 때 안정성 증가
                        .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)

                        // Netty의 이벤트 루프 리소스를 커스터마이징하여 동시 처리 성능 향상
                        // - 첫 번째 인자: 쓰레드 이름 prefix
                        // - 두 번째 인자: I/O 작업을 담당할 쓰레드 수 (CPU 수의 4배 설정)
                        // - 세 번째 인자: selector thread 수 (보통 I/O multiplexing 담당)
                        // - 네 번째 인자: 데몬 쓰레드 여부
                        .runOn(LoopResources.create(

                                "event-loop",                             // 스레드 이름 prefix
                                Runtime.getRuntime().availableProcessors() * 4, // 이벤트 루프 스레드 수 (I/O 처리 담당)
                                200,                                      // selector thread 수
                                true)));                                   // 데몬 쓰레드로 실행 (애플리케이션 종료 시 자동 정리)

        return factory;
    }
}
