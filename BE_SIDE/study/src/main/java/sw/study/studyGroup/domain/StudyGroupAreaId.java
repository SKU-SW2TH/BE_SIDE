package sw.study.studyGroup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
// 복합 키 구성을 위한 별도의 Id 클래스
public class StudyGroupAreaId implements Serializable {

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "area_id")
    private Long areaId;

    // 엔티티를 저장하고 비교할 때, 비교를 위한 기준이 필요
    // 복합 키 구성 : 클래스의 객체를 비교하거나 - equals
    // 혹은, 컬렉션에서 사용할 때를 위해 반드시 필요 - hashCode

    // 두 객체(Long->equals를 통한)가 동일한지 확인 ( groupId 와 areaId )
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null || getClass() != obj.getClass())
            return false;
        StudyGroupAreaId that = (StudyGroupAreaId) obj;
        return Objects.equals(groupId, that.groupId) && Objects.equals(areaId,that.areaId);
    }

    // 해시 기반 컬렉션에서 객체 효율적인 비교, 검색을 위해 반드시 필요
    @Override
    public int hashCode() {
        return Objects.hash(groupId, areaId);
    }
}
