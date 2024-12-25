package sw.study.studyGroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.studyGroup.domain.StudyGroupArea;
import sw.study.studyGroup.domain.StudyGroupAreaId;

import java.util.List;

public interface StudyGroupAreaRepository extends JpaRepository<StudyGroupArea, StudyGroupAreaId> {

    List<StudyGroupArea> findByIdGroupId(Long groupId);
    // 복합키의 구성인 id 필드의 groupId 로 조회
    // studyGroupAreaId.id.groupId 라는 의미
}
