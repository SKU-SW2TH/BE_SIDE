package sw.study.studyGroup.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.CreateStudyGroup;
import sw.study.studyGroup.dto.InvitedResponse;
import sw.study.studyGroup.dto.JoinedResponse;
import sw.study.studyGroup.service.StudyGroupService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studyGroup")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping("/searchMembers") // 사용자 검색
    public ResponseEntity<?> searchMembers(
            @RequestParam String nickname,
            @RequestParam int page,
            @RequestParam int size) {
        List<String> results = studyGroupService.searchByNickname(nickname,page,size);

        if(results.isEmpty()) {
            return ResponseEntity.ok("조회된 결과가 없습니다.");
            // Status : 200 에 Body 내부 Msg 추가해서 response
        }
        return ResponseEntity.ok(results);
    }

    @PostMapping("/create") // 스터디 그룹 생성
    public ResponseEntity<Map<String,Object>> createStudyGroup(
            @RequestBody CreateStudyGroup requestDto) {

        StudyGroup createdGroup = studyGroupService.createStudyGroup(
                requestDto.getGroupName(),
                requestDto.getDescription(),
                requestDto.getSelectedNicknames(),
                requestDto.getLeaderNickname()
                // 스터디 그룹 생성시
        );

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("message","스터디 그룹이 성공적으로 생성되었습니다.");
        apiResponse.put("group",createdGroup);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/invitedList")
    public ResponseEntity<?> checkInvitedList() {
        List<InvitedResponse> invitedResponses = studyGroupService.checkInvited();

        if (invitedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("초대 받은 내역이 존재하지 않습니다."); // 적절한 메시지 반환
        }

        return ResponseEntity.ok(invitedResponses);
    }

    @GetMapping("/joinedList")
    public ResponseEntity<?> checkJoinedList() {
        List<JoinedResponse> joinedResponses = studyGroupService.checkJoined();

        if (joinedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("참여중인 스터디그룹이 존재하지 않습니다.");
        }

        return ResponseEntity.ok(joinedResponses);
    }

}
