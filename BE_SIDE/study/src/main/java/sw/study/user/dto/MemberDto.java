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
    private String role;
    private List<NotificationSettingDTO> settings = new ArrayList<>();
    private List<MemberInterestDTO> interests = new ArrayList<>();
    private List<NotificationDTO> notifications = new ArrayList<>();
}
