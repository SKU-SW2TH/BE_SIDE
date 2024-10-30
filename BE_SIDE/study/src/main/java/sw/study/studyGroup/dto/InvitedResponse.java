package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.StudyGroup;

import java.time.LocalDateTime;

@Data
public class InvitedResponse {
    private String groupName;
    private String description;
    private int memberCount;

    private InvitedResponse(String groupName, String description, int memberCount) {
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
    }

    public static InvitedResponse createInvitedResponse(String groupName, String description, int memberCount) {
        return new InvitedResponse(groupName, description, memberCount);
    }
}
