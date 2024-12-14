package sw.study.studyGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup; // FK (스터디 그룹 참조)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant author; // FK (작성자, 참가자 참조)

    @Column(nullable = false)
    private String title; // 공지 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 공지 내용

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 작성일

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 삭제일

    private int viewCount; // 조회수

    // 공지사항 생성 메서드
    public static Notice createNotice(StudyGroup group, Participant author, String title, String content) {
        Notice notice = new Notice();
        notice.studyGroup = group;
        notice.author = author;
        notice.title = title;
        notice.content = content;
        notice.createdAt = LocalDateTime.now();
        notice.updatedAt = null;
        notice.viewCount = 0;
        return notice;
    }

    public void updateContent(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void IncreaseViewCount(){
        this.viewCount++;
    }
}