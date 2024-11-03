package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.WaitingPeople;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingPeopleRepository extends JpaRepository<WaitingPeople,Long> {

    List<WaitingPeople> findByMemberId(Long memberId); // 특정 사용자의 받은 초대 리스트

    void deleteByMemberId(Long memberId); // 초대 거절 : 대기명단 삭제

    Optional<WaitingPeople> findByMemberIdAndStudyGroupId(Long memberId, Long groupId);

    List<WaitingPeople> findByGroupId(Long groupId); // 특정 그룹 내 초대 명단 확인
}
