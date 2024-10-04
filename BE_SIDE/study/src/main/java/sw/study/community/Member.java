package sw.study.community;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@RequiredArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

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


    // 멘토멘티 X , ADMIN, USER
    @Enumerated(EnumType.STRING)
    private Role role;


    // 생성 시 자동으로 createdAt 필드를 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 업데이트 시 자동으로 updatedAt 필드를 설정
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 회원 생성 팩토리 메서드
    public static Member createMember(String email, String password, String nickname, String profile, String introduce, Role role) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.nickname = nickname;
        member.profile = profile;
        member.introduce = introduce;
        member.role = role;

        // 이곳에 관심분야 관련
        // 알림 관련
        // 엔티티 추가하는 로직이 들어가야한다
        // 물론 매개변수로 받아와야한다.
        // 그리고 관심분야 엔티티와 관심분야 생성 메서드도 필요할 것같음


        return member;
    }
}

