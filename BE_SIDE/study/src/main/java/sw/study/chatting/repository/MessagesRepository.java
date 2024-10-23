package sw.study.chatting.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sw.study.chatting.domain.Messages;

import java.util.List;

public interface MessagesRepository extends MongoRepository<Messages, String> {
    List<Messages> findByChatRoomId(String chatRoomId);
}
