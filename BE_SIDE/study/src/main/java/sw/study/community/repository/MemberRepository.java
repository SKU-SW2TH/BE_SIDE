package sw.study.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.community.domain.Member;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

}
