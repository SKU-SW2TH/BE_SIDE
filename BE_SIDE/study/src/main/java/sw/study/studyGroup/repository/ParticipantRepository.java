package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.Participant;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByMemberId(Long memberId); // 특정 사용자가 참가중인 모든 그룹
    
    boolean validateByNickname(String nickname); // 스터디그룹 닉네임 중복확인

    Participant findParticipantByNickname(String nickname);

    long countByMemberId(Long memberId); // 특정 사용자가 참가중인 그룹의 수

    Optional<Participant> findByMemberIdAndGroupId(Long memberId, Long groupId); // 특정 사용자가 특정 그룹에 참여중인지 확인

    List<Participant> findAllByGroupId(Long groupId); // 전체 사용자 조회
    List<Participant> findAllByGroupIdAndRole(Long groupId, Participant.Role role); // 신분에 따른 조회

    Optional<Participant> findByGroupIdAndNickname(Long groupId,String nickname); // 추방 시 or 그룹 내부에서 닉네임 변경시
}
