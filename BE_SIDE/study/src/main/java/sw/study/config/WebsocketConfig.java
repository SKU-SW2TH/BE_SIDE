package sw.study.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 메세지 핸들링
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

    // 메세지 브로커 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 각각에 대한 prefix 설정

        // sub 는 구독 prefix ( 메세지 수신 )
        config.enableSimpleBroker("/sub");

        // pub 는 서버에 메세지를 보낼 때 사용
        // @MessageMapping이 붙은 메서드를 호출
        config.setApplicationDestinationPrefixes("/pub");

        // /sub/chat/{roomId} 의 형태로 경로를 구분하면 된다.
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹 소켓 연결 엔드포인트 설정. FE 와 연동시 Origin 수정 필요
        // E.G ) localhost:8080/ws
        registry.addEndpoint("/ws").setAllowedOrigins("*")
                .withSockJS();

        // withSockJs 는 소켓이 지원되지 않는 브라우저 호환을 위한 것.
    }
}
