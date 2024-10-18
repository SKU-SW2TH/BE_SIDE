package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    boolean existsByNickname(String nickname); // 닉네임 중복 확인 쿼리
}
