package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.Participant;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
