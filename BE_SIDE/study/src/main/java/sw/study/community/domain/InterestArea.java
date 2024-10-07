package sw.study.community.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class InterestArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_area_id")
    private Long id;

    private int level;
    private String areaName;

    @OneToMany(mappedBy = "parent")
    private List<InterestArea> child = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private InterestArea parent;

    //얘는 생성날짜 수정날짜 필요 없을듯?


    //== 생성 메서드 ==//
    public static InterestArea createInterest(int level, String FieldName) {
        InterestArea interest = new InterestArea();
        interest.level = level;
        interest.areaName = FieldName;

        return interest;
    }

    public void addChildInterest(InterestArea child) {
        this.child.add(child);
        child.addParentInterest(this);
    }

    public void addParentInterest(InterestArea parent) {
        this.parent = parent;
    }
}
