package sw.study.studyGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import sw.study.studyGroup.domain.Notice;
import sw.study.studyGroup.domain.NoticeFile;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class NoticeDetailResponse {

    private Long id;
    private String nickname;
    private String title;
    private String content;
    private boolean isChecked;
    private int numOfChecks;
    private int viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> fileUrls;

    // createNoticeDetail 메서드에서 fileUrls을 변환
    public static NoticeDetailResponse createNoticeDetail(Notice notice, boolean isChecked, int numOfChecks) {

        // 존재하면, 각각의 url 을 list 에 추가하고 없으면 빈 리스트로 처리
        List<String> fileUrls = (notice.getFileUrls() != null ?
                notice.getFileUrls().stream()
                        .map(NoticeFile::getFileUrl)
                        .collect(Collectors.toList())
                : Collections.emptyList());

        // NoticeDetailResponse 생성자 호출
        return new NoticeDetailResponse(
                notice.getId(),
                notice.getAuthor().getNickname(),
                notice.getTitle(),
                notice.getContent(),
                isChecked,
                numOfChecks,
                notice.getViewCount(),
                notice.getCreatedAt(),
                notice.getUpdatedAt(),
                fileUrls
        );
    }
}
