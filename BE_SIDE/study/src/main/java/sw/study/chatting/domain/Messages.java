package sw.study.chatting.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;


@Document(collection = "messages")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Messages {

    public enum MessageType{
        // 메세지 타입 : 신규 입장, 채팅
        // 입장 메세지 및 추후 초대되어 참여할 사람 고려

        ENTER,
        TALK,
        ATTACHMENT
        // Attachment? 도 별도로 설정해서 메세지가 첨부파일인 형태도 고려
    }

    @Id
    private String id;
    private String chatRoomId;
    private String senderId;
    private MessageType messageType;
    private String content;
    private List<Attachment> attachments = new ArrayList<>();
    private LocalDateTime timestamp;
    private boolean isDeleted;

    // 정적 팩토리 메서드
    public static Messages createMessage(String chatRoomId, String senderId, String content, List<Attachment> attachments) {
        Messages message = new Messages();
        message.chatRoomId = chatRoomId;
        message.senderId = senderId;
        message.content = content;
        message.attachments = attachments;
        message.timestamp = LocalDateTime.now();
        message.isDeleted = false;
        return message;
    }

    @Getter
    public static class Attachment {
        private String type; // 이미지 혹은 링크 혹은 파일?
        private String url;  // 첨부파일 URL

        public Attachment(String type, String url) {
            this.type = type;
            this.url = url;
        }
    }
}

