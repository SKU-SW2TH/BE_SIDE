package sw.study.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sw.study.community.domain.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 카테고리 이름으로 게시글 조회
    Page<Post> findByCategoryName(String categoryName, Pageable pageable);

    Page<Post> findByCategoryNameAndTitleContaining(String category, String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category.name = :category AND p.member.nickname LIKE %:keyword%")
    Page<Post> findByCategoryNameAndAuthorContaining(@Param("category") String category,
                                                     @Param("keyword") String keyword,
                                                     Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category.name = :category AND " +
            "(p.title LIKE %:keyword% OR p.member.nickname LIKE %:keyword%)")
    Page<Post> findByCategoryNameAndTitleContainingOrAuthorContaining(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);
}
