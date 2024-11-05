package sw.study.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.community.domain.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

}
