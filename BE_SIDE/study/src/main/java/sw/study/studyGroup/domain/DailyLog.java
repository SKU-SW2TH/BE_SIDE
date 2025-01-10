package sw.study.studyGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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

    @OneToMany(mappedBy = "dailyLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyLogFile> fileUrls = new ArrayList<>();

    // 데일리 로그 생성 메서드
    public static DailyLog createDailyLog(StudyGroup group, Participant author, String title, String content, List<String> fileUrls) {
        DailyLog log = new DailyLog();
        log.studyGroup = group;
        log.author = author;
        log.title = title;
        log.content = content;
        log.createdAt = LocalDateTime.now();
        log.updatedAt = null;

        if(fileUrls != null){
            for(String urls : fileUrls){
                DailyLogFile dailyLogFile = DailyLogFile.createDailyLogFile(log, urls);
                log.fileUrls.add(dailyLogFile);
            }
        }

        return log;
    }

    // 수정 메소드
    public void updateLog(String title, String content, List<String> fileUrls) {
        this.title = title;
        this.content = content;
        this.updatedAt = LocalDateTime.now();

        if(fileUrls != null && !fileUrls.isEmpty()){
            this.fileUrls.clear();

            for(String urls : fileUrls){
                DailyLogFile dailyLogFile = DailyLogFile.createDailyLogFile(this,urls);
                this.fileUrls.add(dailyLogFile);
            }
        }
    }
}