package sw.study.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.community.domain.Post;
import sw.study.community.domain.PostLike;
import sw.study.user.domain.Member;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndMember(Post post, Member member);
    Optional<PostLike> findByPostAndMember(Post post, Member member);
}
