package sw.study.studyGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyLogFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false)
    private DailyLog dailyLog;

    @Column(nullable = false)
    private String fileUrl;

    public static DailyLogFile createDailyLogFile(DailyLog dailyLog, String fileUrl) {
        DailyLogFile dailyLogFile = new DailyLogFile();
        dailyLogFile.dailyLog = dailyLog;
        dailyLogFile.fileUrl = fileUrl;
        return dailyLogFile;
    }
}
