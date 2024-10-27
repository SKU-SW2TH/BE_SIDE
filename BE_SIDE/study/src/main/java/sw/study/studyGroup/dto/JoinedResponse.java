package sw.study.studyGroup.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JoinedResponse {
    private String groupName;
    private String description;
    private int memberCount;
    private LocalDateTime createdAt; // 초대 일시

    private JoinedResponse(String groupName, String description, int memberCount, LocalDateTime createdAt) {
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
    }

    public static JoinedResponse createJoinedResponse(String groupName, String description, int memberCount, LocalDateTime createdAt) {
        return new JoinedResponse(groupName, description, memberCount, createdAt);
    }
}
