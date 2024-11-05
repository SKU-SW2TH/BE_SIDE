package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.Member;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.domain.NotificationSetting;

import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    Optional<NotificationSetting> findByMemberAndCategory(Member member, NotificationCategory category);
    Optional<NotificationSetting> findById(Long id);
}
