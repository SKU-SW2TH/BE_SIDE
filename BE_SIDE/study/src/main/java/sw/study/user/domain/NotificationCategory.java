package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "NotificationCategory")
@Getter
@RequiredArgsConstructor
public class NotificationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notificationCategory_id")
    private Long id;

    @Column(name = "categoryName", unique = true)
    private String categoryName;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true) // cascade 및 orphanRemoval 설정
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true) // cascade 및 orphanRemoval 설정
    private List<NotificationSetting> settings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static NotificationCategory createNotificationCategory(String categoryName) {
        NotificationCategory notificationCategory = new NotificationCategory();
        notificationCategory.categoryName = categoryName;
        return notificationCategory;
    }

}
