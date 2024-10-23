package sw.study.chatting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.chatting.domain.ChatList;

@Repository
public interface ChatListRepository extends JpaRepository<ChatList, Long> {
}
