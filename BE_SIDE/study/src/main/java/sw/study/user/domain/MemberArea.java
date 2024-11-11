package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Area;

import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class MemberArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_interest_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "interest_area_id")
    private Area area;

    //== 생성 메서드 ==//
    public static MemberArea CreateMemberArea(Area area) {
        MemberArea memberArea = new MemberArea();
        memberArea.area = area;

        return memberArea;
    }

    public void addMember(Member member) {
        this.member = member;
    }

}
