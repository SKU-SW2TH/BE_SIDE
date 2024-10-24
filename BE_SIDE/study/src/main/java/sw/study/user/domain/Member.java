package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.domain.NotificationSetting;

import java.time.LocalDateTime;
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

    private String email;
    private String password;

    private String nickname;
    private String profile;
    private String introduce;

    private boolean isDeleted = false; // 삭제 여부를 확인
    private boolean isSuspended = false; // 정지 여부를 확인
    private int warningCnt = 0; // 누적 경고 횟수

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


    //== 생성 메서드 ==//
    public static Member createMember(String email, String password, String nickname) {

        Member member = new Member();
        member.email = email;
        member.password = password;
        member.nickname = nickname;


        // 알림설정 저장
//        for(NotificationCategory category : categories) {
//            NotificationSetting.createSetting(member, category);
//        }

        return member;
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

