package sw.study.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private long id;
    private long targetId;
    private String content;
    private String type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
