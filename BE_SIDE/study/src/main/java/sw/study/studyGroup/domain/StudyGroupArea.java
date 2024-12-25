package sw.study.studyGroup.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sw.study.user.domain.Area;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class StudyGroupArea {

    @EmbeddedId
    private StudyGroupAreaId id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성날짜

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 복합 키 구성 : 스터디그룹과 관심분야
    public static StudyGroupArea createStudyGroupArea(StudyGroup studyGroup, Area area) {
        StudyGroupArea studyGroupArea = new StudyGroupArea();
        studyGroupArea.id = new StudyGroupAreaId(studyGroup.getId(), area.getId()); // 복합키 설정

        studyGroupArea.createdAt = LocalDateTime.now(); // 생성날짜
        studyGroupArea.updatedAt = null;
        return studyGroupArea;
    }

    // 그룹 내에서 관심분야 수정시
    public void updateStudyGroupArea() {
        this.updatedAt = LocalDateTime.now(); // 반영되도록
    }
}
