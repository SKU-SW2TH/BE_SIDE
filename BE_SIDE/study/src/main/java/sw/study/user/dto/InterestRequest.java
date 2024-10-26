package sw.study.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InterestRequest {
    List<Long> ids = new ArrayList<>();
}
