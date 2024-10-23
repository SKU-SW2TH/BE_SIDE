package sw.study.chatting.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.studyGroup.domain.StudyGroup;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id")
    private Long chatRoomId; // PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false) // FK (스터디 그룹 참조)
    private StudyGroup studyGroup;

    @Column(name = "room_name", nullable = false)
    private String roomName; // 방 이름

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdDate; // 생성 날짜

    @Column(name = "modified_at")
    private LocalDateTime modifiedDate; // 수정 날짜

    public static ChatRoom createChatRoom(StudyGroup studyGroup, String roomName) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.studyGroup = studyGroup;
        chatRoom.roomName = roomName;
        chatRoom.createdDate = LocalDateTime.now();
        return chatRoom;
    }
}