package sw.study.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long id;

    private int level;
    private String areaName;

    @OneToMany(mappedBy = "parent")
    private List<Area> child = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Area parent;


    //== 생성 메서드 ==//
    public static Area createInterest(int level, String FieldName) {
        Area area = new Area();
        area.level = level;
        area.areaName = FieldName;

        return area;
    }

    public void addChildInterest(Area child) {
        this.child.add(child);
        child.addParentInterest(this);
    }

    public void addParentInterest(Area parent) {
        this.parent = parent;
    }
}
