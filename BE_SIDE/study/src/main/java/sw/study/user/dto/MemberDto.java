package sw.study.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MemberDto {
    private String email;
    private String nickname;
    private String profile;
    private String introduce;
    List<NotificationSettingDTO> settings = new ArrayList<>();
}
