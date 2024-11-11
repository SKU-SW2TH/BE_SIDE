package sw.study.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AreaRequest {
    @Schema(description = "관심분야 id")
    List<Long> ids = new ArrayList<>();
}
