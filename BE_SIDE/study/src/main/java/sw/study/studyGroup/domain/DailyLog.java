package sw.study.studyGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup studyGroup; // FK (스터디 그룹 참조)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Participant author; // FK (작성자, 참가자 참조)

    @Column(name ="title", nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 데일리 로그 내용

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 작성일

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정일

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 삭제일

    // 데일리 로그 생성 메서드
    public static DailyLog createDailyLog(StudyGroup group, Participant author, String title, String content) {
        DailyLog log = new DailyLog();
        log.studyGroup = group;
        log.author = author;
        log.title = title;
        log.content = content;
        log.createdAt = LocalDateTime.now();
        log.updatedAt = null;
        return log;
    }

    public void updateLog(String title, String content) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}