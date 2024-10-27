package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.WaitingPeople;

import java.util.List;

@Repository
public interface WaitingPeopleRepository extends JpaRepository<WaitingPeople,Long> {

    List<WaitingPeople> findByMemberId(Long memberId);

}
