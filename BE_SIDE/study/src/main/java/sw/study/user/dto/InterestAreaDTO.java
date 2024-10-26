package sw.study.user.dto;

import lombok.Data;

@Data
public class InterestAreaDTO {
    private Long id;
    private int level;
    private String areaName;
    private Long parentId;
}
