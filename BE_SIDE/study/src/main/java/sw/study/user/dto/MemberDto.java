package sw.study.user.dto;

import lombok.Data;

@Data
public class MemberDto {
    private String email;
    private String password;
    private String nickname;
    private String profile;
    private String introduce;
}
