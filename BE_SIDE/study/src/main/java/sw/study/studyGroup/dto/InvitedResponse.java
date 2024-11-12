package sw.study.studyGroup.dto;

import lombok.Data;

@Data
public class InvitedResponse {
    private Long groupId;
    private String groupName;
    private String description;
    private int memberCount;

    private InvitedResponse(Long groupId, String groupName, String description, int memberCount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
    }

    public static InvitedResponse createInvitedResponse(Long groupId, String groupName, String description, int memberCount) {
        return new InvitedResponse(groupId, groupName, description, memberCount);
    }
}
