package sw.study.studyGroup.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JoinedResponse {
    private String groupName;
    private String description;
    private int memberCount;

    private JoinedResponse(String groupName, String description, int memberCount) {
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
    }

    public static JoinedResponse createJoinedResponse(String groupName, String description, int memberCount) {
        return new JoinedResponse(groupName, description, memberCount);
    }
}
