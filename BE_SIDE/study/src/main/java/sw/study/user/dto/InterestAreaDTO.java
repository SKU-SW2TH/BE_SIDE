package sw.study.user.dto;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import sw.study.user.domain.InterestArea;

import java.util.ArrayList;
import java.util.List;

@Data
public class InterestAreaDTO {
    private Long id;
    private int level;
    private String areaName;
    private Long parentId;
}
