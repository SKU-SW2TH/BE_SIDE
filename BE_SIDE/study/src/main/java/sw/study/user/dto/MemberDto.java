package sw.study.user.dto;

import lombok.Data;
import lombok.Getter;

@Data
public class MemberDto {
    private String email;
    private String nickname;
    private String profile;
    private String introduce;
}
