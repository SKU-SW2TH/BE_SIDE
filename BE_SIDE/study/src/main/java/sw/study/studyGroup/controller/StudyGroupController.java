package sw.study.studyGroup.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.CreateStudyGroup;
import sw.study.studyGroup.service.StudyGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/studyGroup")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping("/searchMembers") // 사용자 검색
    public ResponseEntity<List<String>> searchMembers(
            @RequestParam String nickname,
            @RequestParam int page,
            @RequestParam int size) {
        List<String> results = studyGroupService.searchByNickname(nickname,page,size);

        // 검색되는거 없어도 -> 404 리턴하지 말고 204 리턴 ( 성공 - noContent )
        if(results.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(results);
    }

    @PostMapping // 스터디 그룹 생성
    public ResponseEntity<StudyGroup> createStudyGroup(
            @RequestBody CreateStudyGroup requestDto) {

        StudyGroup createdGroup = studyGroupService.createStudyGroup(
                requestDto.getGroupName(),
                requestDto.getDescription(),
                requestDto.getSelectedNicknames(),
                requestDto.getLeaderNickname()
        );

        // 예외처리에 대한 부분 추가 필요.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

}
