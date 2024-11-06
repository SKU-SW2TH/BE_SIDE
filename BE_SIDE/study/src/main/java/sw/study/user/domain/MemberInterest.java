package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.InterestArea;

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

    //== 생성 메서드 ==//
    public static MemberInterest CreateMemberInterest(InterestArea interestArea) {
        MemberInterest memberInterest = new MemberInterest();
        memberInterest.interestArea = interestArea;

        return memberInterest;
    }

    public void addMember(Member member) {
        this.member = member;
    }

}
