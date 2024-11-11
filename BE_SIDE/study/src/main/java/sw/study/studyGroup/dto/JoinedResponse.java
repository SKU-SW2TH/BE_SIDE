package sw.study.studyGroup.dto;

import lombok.Data;

@Data
public class JoinedResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private int memberCount;

    private JoinedResponse(Long groupId, String groupName, String description, int memberCount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
    }

    public static JoinedResponse createJoinedResponse(Long groupId, String groupName, String description, int memberCount) {
        return new JoinedResponse(groupId, groupName, description, memberCount);
    }
}
