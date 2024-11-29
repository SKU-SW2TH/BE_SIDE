package sw.study.community.dto;

import lombok.Data;

@Data
public class PostAreaResponse {
    private int level;
    private String AreaName;

    public PostAreaResponse(int level, String areaName) {
        this.level = level;
        AreaName = areaName;
    }
}
