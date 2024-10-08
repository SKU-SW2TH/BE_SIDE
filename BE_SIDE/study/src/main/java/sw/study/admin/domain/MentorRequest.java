package sw.study.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sw.study.admin.MentorRequestStatus;
import sw.study.community.domain.Member;
import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "mentorRequest")
@Getter
@RequiredArgsConstructor(access = PROTECTED)
public class MentorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mentorRequest_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "member_id") // 외래키 이름 설정
    private Member member;

    @Lob // 만약 document가 대용량 데이터라면
    @Column(name = "document", columnDefinition = "TEXT")
    private String document;

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    private MentorRequestStatus status;

    private LocalDateTime requestDate;
    private LocalDateTime reviewDate;

    @PrePersist
    public void onCreate() {
        requestDate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        reviewDate = LocalDateTime.now();
    }

    public static MentorRequest of(Member member, String document, MentorRequestStatus status) {
        MentorRequest mentorRequest = new MentorRequest();
        mentorRequest.member = member;
        mentorRequest.document = document;
        mentorRequest.status = status;

        return mentorRequest;
    }

}
