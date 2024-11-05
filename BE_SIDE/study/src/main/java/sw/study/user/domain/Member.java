package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.admin.domain.Punishment;
import sw.study.admin.domain.Report;
import sw.study.user.role.Role;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", unique = true) // 이메일에 unique 제약 추가
    private String email;
    private String password;

    private String nickname;
    private String profile;
    private String introduce;

    private boolean isDeleted = false; // 삭제 여부를 확인
    private boolean isSuspended = false; // 정지 여부를 확인
    private int warningCnt = 0; // 누적 경고 횟수

    @Enumerated(EnumType.ORDINAL)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;



    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationSetting> settings = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Punishment> punishments = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberInterest> interests = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    //== 생성 메서드 ==//
    public static Member createMember(String email, String password, String nickname, Role role, List<NotificationCategory> categories) {

        Member member = new Member();
        member.email = email;
        member.password = password;
        member.nickname = nickname;
        member.introduce = "";
        member.role = role;

        // 알림설정 저장
        for(NotificationCategory category : categories) {
            NotificationSetting setting = NotificationSetting.createSetting(member, category);
            member.addSetting(setting); // 설정을 Member에 추가
        }

        return member;
    }

    public void onDeleted() {
        this.isDeleted = true;
    }

    public void requestDeactivation() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }

    public void addSetting(NotificationSetting setting) {
        settings.add(setting);
    }

    public void addInterest(MemberInterest interest) {
        interests.add(interest);
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    public void removeInterest(MemberInterest interest) {
        interests.remove(interest);
    }

    // 개별 프로필 필드를 선택적으로 업데이트하는 메소드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfilePicture(String profile) {
        this.profile = profile;
    }

    public void updateIntroduction(String introduce ) {
        this.introduce = introduce;
    }

    public void changePassword(String password) {
        this.password = password;
    }

}

