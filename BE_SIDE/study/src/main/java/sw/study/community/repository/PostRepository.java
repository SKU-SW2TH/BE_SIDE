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
    Page<Post> findByCategoryNameAndIsDeletedFalse(String categoryName, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category.name = :category AND p.isDeleted = false AND p.title LIKE %:keyword%")
    Page<Post> findByCategoryNameAndTitleContainingAndIsDeletedFalse(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category.name = :category AND p.isDeleted = false AND p.member.nickname LIKE %:keyword%")
    Page<Post> findByCategoryNameAndAuthorContainingAndIsDeletedFalse(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.category.name = :category AND p.isDeleted = false AND " +
            "(p.title LIKE %:keyword% OR p.member.nickname LIKE %:keyword%)")
    Page<Post> findByCategoryNameAndTitleContainingOrAuthorContainingAndIsDeletedFalse(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);


    Page<Post> findByMember_Id(Long memberId, Pageable pageable);
}
