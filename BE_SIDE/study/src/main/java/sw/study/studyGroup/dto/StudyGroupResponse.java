package sw.study.studyGroup.dto;

import lombok.Data;

@Data
public class StudyGroupResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private int memberCount;

    private StudyGroupResponse(Long groupId, String groupName, String description, int memberCount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
    }

    public static StudyGroupResponse createStudyGroupResponse(Long groupId, String groupName, String description, int memberCount) {
        return new StudyGroupResponse(groupId, groupName, description, memberCount);
    }
}
