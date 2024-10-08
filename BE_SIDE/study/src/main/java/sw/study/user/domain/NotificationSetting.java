package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import sw.study.community.domain.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "Setting")
@Getter
@RequiredArgsConstructor
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @NotNull
    @JoinColumn(name = "member_id") // 외래키 이름 설정
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @NotNull
    @JoinColumn(name = "category_id") // 외래키 이름 설정
    private NotificationCategory category;

    private boolean isEnabled = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public static NotificationSetting createSetting(Member member, NotificationCategory category) {
        NotificationSetting setting = new NotificationSetting();
        setting.member = member;
        setting.category = category;
        return setting;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
