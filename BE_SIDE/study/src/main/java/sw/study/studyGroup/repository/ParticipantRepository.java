package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.WaitingPeople;

import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByMemberId(Long memberId);
    
    boolean findByNickname(String nickname); // 스터디그룹 닉네임 중복확인
    
    long countByMemberId(Long memberId); // 특정 사용자가 참가중인 그룹의 수
}
