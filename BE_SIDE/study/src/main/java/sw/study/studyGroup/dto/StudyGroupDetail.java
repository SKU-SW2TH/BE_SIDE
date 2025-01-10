package sw.study.studyGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudyGroupDetail {
    private String groupName;
    private String description;
    private int memberCount;
    private List<String> areas;
    private String leaderNickname;
    private String backgroundImg;
}
