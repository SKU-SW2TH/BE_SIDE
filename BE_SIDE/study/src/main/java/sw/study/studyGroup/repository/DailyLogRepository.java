package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.DailyLog;

import java.util.List;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long>{

    List<DailyLog> findAllByStudyGroupId(long groupId);
}
