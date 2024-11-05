package sw.study.user.dto;

import lombok.Data;

@Data
public class NotificationSettingDTO {
    Long settingId; // 알림 설정 ID
    boolean isEnabled;
    NotificationCategoryDTO categoryDTO;
}
