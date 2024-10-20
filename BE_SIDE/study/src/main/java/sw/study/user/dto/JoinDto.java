package sw.study.user.dto;

import lombok.Data;

@Data
public class JoinDto {
    private String email;
    private String password;
    private String nickname;
}
