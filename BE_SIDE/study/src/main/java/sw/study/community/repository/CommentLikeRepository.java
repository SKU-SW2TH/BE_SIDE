package sw.study.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.community.domain.Comment;
import sw.study.community.domain.CommentLike;
import sw.study.user.domain.Member;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);
}
