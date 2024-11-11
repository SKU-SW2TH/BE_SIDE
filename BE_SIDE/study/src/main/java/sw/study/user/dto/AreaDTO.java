package sw.study.user.dto;

import lombok.Data;

@Data
public class AreaDTO {
    private Long id;
    private int level;
    private String areaName;
    private Long parentId;
}
