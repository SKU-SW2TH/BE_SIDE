package sw.study.user.dto;

import lombok.Data;

@Data
public class NotificationSettingDTO {
    long categoryId;
    boolean isEnabled;
}
