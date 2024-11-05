package sw.study.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SettingRequest {
    @Schema(description = "알림 ID",example = "1")
    long settingId;

    @Schema(description = "알림 유무", example = "false")
    boolean isEnabled;
}
