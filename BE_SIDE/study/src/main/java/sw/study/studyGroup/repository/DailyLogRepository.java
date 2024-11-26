package sw.study.studyGroup.repository;

import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.DailyLog;
import java.time.LocalDateTime;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long>{

    Page<DailyLog> findAllByStudyGroup_IdAndCreatedAtBetween(long groupId, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
