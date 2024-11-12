package sw.study.studyGroup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.Notice;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByStudyGroup_Id(Long groupId, Pageable pageable);
    Optional<Notice> findByIdAndStudyGroup_Id(Long noticeId, Long groupId);
}
