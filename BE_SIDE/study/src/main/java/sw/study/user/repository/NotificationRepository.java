package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.Member;
import sw.study.user.domain.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMemberAndIsReadFalse(Member member);
}
