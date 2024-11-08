package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.domain.NotificationSetting;

import java.util.List;
import java.util.Optional;

public interface NotificationCategoryRepository extends JpaRepository<NotificationCategory, Long> {
    Optional<NotificationCategory> findById(Long id);
    Optional<NotificationCategory>  findByCategoryName(String name);

    List<NotificationCategory> findAll();

}
