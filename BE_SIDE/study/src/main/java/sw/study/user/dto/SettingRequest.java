package sw.study.user.dto;

import lombok.Data;

@Data
public class SettingRequest {
    long settingId;
    boolean isEnabled;
}
