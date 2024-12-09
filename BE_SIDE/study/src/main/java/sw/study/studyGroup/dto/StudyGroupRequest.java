package sw.study.studyGroup.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudyGroupRequest {
    private String groupName;
    private String description;
    private List<String> selectedNicknames;
    private String leaderNickname;
}
