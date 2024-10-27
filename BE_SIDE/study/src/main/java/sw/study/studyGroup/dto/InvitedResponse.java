package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.StudyGroup;

import java.time.LocalDateTime;

@Data
public class InvitedResponse {
    private String groupName;
    private String description;
    private int memberCount;
    private LocalDateTime createdAt; // 초대 일시

    private InvitedResponse(String groupName, String description, int memberCount, LocalDateTime createdAt) {
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public static InvitedResponse createInvitedResponse(String groupName, String description, int memberCount, LocalDateTime createdAt) {
        return new InvitedResponse(groupName, description, memberCount, createdAt);
    }
}
