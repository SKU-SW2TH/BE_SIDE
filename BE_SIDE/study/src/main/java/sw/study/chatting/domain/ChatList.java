package sw.study.chatting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.studyGroup.domain.Participant;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class ChatList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_list_id")
    private Long chatListId; // 채팅목록 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false) // FK (채팅방 참조)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false) // FK (참가자 참조)
    private Participant participant;

    public static ChatList createChatList(ChatRoom chatRoom, Participant participant) {
        ChatList chatList = new ChatList();
        chatList.chatRoom = chatRoom;
        chatList.participant = participant;
        return chatList;
    }

}
