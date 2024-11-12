package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.MemberArea;

import java.util.List;
import java.util.Optional;

public interface MemberAreaRepository extends JpaRepository<MemberArea, Integer> {
    List<MemberArea> findByMemberId(Long memberId);

    Optional<MemberArea> findByMemberIdAndAreaId(Long memberId, Long interestAreaId);
}
