package sw.study.studyGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

// 스터디그룹 정보 수정 Dto
@Data
@AllArgsConstructor
public class StudyGroupUpdate {
    private String groupName;
    private String description;
    private List<Long> areaIds;
}
