package sw.study.community.dto;

import lombok.Data;

@Data
public class PostAuthorResponse {
    private String nickname;
    private String profile;
    private boolean isDeleted; // 해당 사용자가 탈퇴한 회원이라면 (알수 없음)으로 표시하기 위해
}
