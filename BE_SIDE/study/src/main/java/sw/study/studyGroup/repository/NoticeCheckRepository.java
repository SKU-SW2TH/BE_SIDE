package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.studyGroup.domain.Notice;
import sw.study.studyGroup.domain.NoticeCheck;

import java.util.Optional;

@Repository
public interface NoticeCheckRepository extends JpaRepository<NoticeCheck, Long> {

    Optional<NoticeCheck> findByNoticeIdAndParticipantId(Long noticeId, Long participantId);

    // 공지사항 총 체크 수
    int countByNoticeId(Long noticeId);

    // 특정 게시글에 체크 표시 여부 판단
    boolean existsByNoticeIdAndParticipantId(Long noticeId, Long participantId);
}
