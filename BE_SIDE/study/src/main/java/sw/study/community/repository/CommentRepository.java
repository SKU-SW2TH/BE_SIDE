package sw.study.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.community.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
