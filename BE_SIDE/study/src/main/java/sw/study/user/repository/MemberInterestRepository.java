package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.MemberInterest;

public interface MemberInterestRepository extends JpaRepository<MemberInterest, Integer> {

}
