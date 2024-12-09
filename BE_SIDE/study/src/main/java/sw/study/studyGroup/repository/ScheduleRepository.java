package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 특정 연도/월의 일정 조회
    List<Schedule> findByStartDateBetweenAndStudyGroupId(LocalDate startDate, LocalDate endDate, Long groupId);
}
