package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.MemberInterest;

import java.util.List;
import java.util.Optional;

public interface MemberInterestRepository extends JpaRepository<MemberInterest, Integer> {
    List<MemberInterest> findByMemberId(Long memberId);

    Optional<MemberInterest> findByMemberIdAndInterestAreaId(Long memberId, Long interestAreaId);
}
