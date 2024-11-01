package sw.study.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String nickname);

    boolean existsByNickname(String nickname); // 닉네임 중복 확인 쿼리

    Page<Member> findMembersByNicknameStartingWith(String nickname, Pageable pageable); // 닉네임을 통한 사용자 검색
    List<Member> findByNicknameIn(List<String> nicknames); // 선택된 닉네임 list -> 한번에 Member 객체들을 받아오는 메소드
}
