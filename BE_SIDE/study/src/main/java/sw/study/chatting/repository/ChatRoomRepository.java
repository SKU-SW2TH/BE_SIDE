package sw.study.chatting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.chatting.domain.ChatRoom;

@Repository
public interface ChatRoomRepository extends JpaRepository <ChatRoom, Long> {
}
