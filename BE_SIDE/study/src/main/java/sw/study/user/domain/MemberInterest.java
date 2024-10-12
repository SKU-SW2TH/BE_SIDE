package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.community.domain.InterestArea;

import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class MemberInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_interest_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "interest_area_id")
    private InterestArea interestArea;

    // 회원, 관심분야 각각 하나씩 받아서 저장하는 메서드?
    // 그럼 1대1 대응 아닌가?
    // 이게 맞는거 같긴함
    //== 생성 메서드 ==//
    public static MemberInterest CreateMemberInterest(Member member, InterestArea interestArea) {
        MemberInterest memberInterest = new MemberInterest();
        memberInterest.member = member;
        memberInterest.interestArea = interestArea;

        return memberInterest;
    }
}
