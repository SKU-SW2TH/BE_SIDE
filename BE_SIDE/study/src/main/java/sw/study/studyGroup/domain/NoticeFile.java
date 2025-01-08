package sw.study.studyGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(nullable = false)
    private String fileUrl;

    public static NoticeFile createNoticeFile(Notice notice, String fileUrl) {
        NoticeFile noticeFile = new NoticeFile();
        noticeFile.notice = notice;
        noticeFile.fileUrl = fileUrl;
        return noticeFile;
    }
}
