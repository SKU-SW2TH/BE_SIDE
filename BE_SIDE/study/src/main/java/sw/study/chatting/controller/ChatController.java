package sw.study.chatting.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import sw.study.chatting.domain.Messages;
import sw.study.chatting.repository.MessagesRepository;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    // 프론트엔드 연동 이전 우선 Websocket - sub/pub 구조 동작하는지 정도 확인

    private final MessagesRepository messagesRepository;
    // private final SimpMessageSendingOperations messagingTemplate; ( apic 으로도 가능하나.. )

    // socketConfig -> /pub -> @MessageMapping 호출
    @MessageMapping("/chat/{roomId}")
    @SendTo("/sub/chat/{roomId}") // 메시지를 구독하는 클라이언트에게 전송
    public Messages sendMessage(@DestinationVariable String roomId, Messages message) {

        // 메시지 저장 -> 이후 API 형식으로 수정
        message = Messages.createMessage(roomId, message.getSenderId(), message.getContent(), message.getAttachments());
        messagesRepository.save(message); // MongoDB에 저장

        log.info("Message received: {}", message);

        return message;
    }
}
