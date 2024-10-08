package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import sw.study.community.domain.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Getter
@RequiredArgsConstructor
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

    @NotNull
    private String title;

    @NotNull
    private String content;

    private boolean read = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static Notification createNotification(Member member, NotificationCategory category, String title, String content) {
        Notification notification = new Notification();
        notification.member = member;
        notification.category = category;
        notification.title = title;
        notification.content = content;
        return notification;
    }

    public void markAsRead() {
        this.read = true;
    }

}
