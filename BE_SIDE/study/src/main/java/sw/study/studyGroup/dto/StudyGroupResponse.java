package sw.study.studyGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import sw.study.user.domain.Area;

import java.util.List;

@Data
@AllArgsConstructor
public class StudyGroupResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private int memberCount;
    private List<String> areas;
    private String backgroundImg;

    public static StudyGroupResponse createStudyGroupResponse(Long groupId, String groupName, String description, int memberCount, List<String> areas, String backgroundImg) {
        return new StudyGroupResponse(groupId, groupName, description, memberCount, areas, backgroundImg);
    }
}
