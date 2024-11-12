package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "Notification")
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "member_id") // 외래키 이름 설정
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "category_id") // 외래키 이름 설정
    private NotificationCategory category;

    private Long targetId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private boolean isRead = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static Notification createNotification(NotificationCategory category, String title, String content, Long targetId) {
        Notification notification = new Notification();
        notification.category = category;
        notification.title = title;
        notification.content = content;
        notification.targetId = targetId;

        return notification;
    }

    public void addMember(Member member) {
        this.member = member;
    }

    public void markAsRead() {
        this.isRead = true;
    }

}
