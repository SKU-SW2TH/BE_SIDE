package sw.study.studyGroup.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.DuplicateNicknameException;
import sw.study.exception.MaxStudyGroupException;
import sw.study.exception.StudyGroupFullException;
import sw.study.exception.UnauthorizedException;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.*;
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
            //해당 부분은 custom Exception 으로 수정 예정
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
        );

        Map<String, Object> apiResponse = new HashMap<>();
        apiResponse.put("message","스터디 그룹이 성공적으로 생성되었습니다.");
        apiResponse.put("group",createdGroup);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    // 받은 초대 확인
    @GetMapping("/invitedList")
    public ResponseEntity<?> checkInvitedList() {
        List<InvitedResponse> invitedResponses = studyGroupService.checkInvited();

        if (invitedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("초대 받은 내역이 존재하지 않습니다.");
            //해당 부분은 custom Exception 으로 수정 예정
        }

        return ResponseEntity.ok(invitedResponses);
    }

    // 참가 중인 그룹 확인
    @GetMapping("/joinedList")
    public ResponseEntity<?> checkJoinedList() {
        List<JoinedResponse> joinedResponses = studyGroupService.checkJoined();

        if (joinedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("참여중인 스터디그룹이 존재하지 않습니다.");
            //해당 부분은 custom Exception 으로 수정 예정
        }

        return ResponseEntity.ok(joinedResponses);
    }

    // 초대 수락
    @PostMapping("/{groupId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable long groupId, @RequestBody SearchByNickname searchByNickname) {

        String nickname = searchByNickname.getNickname();

        try {
            studyGroupService.acceptInvitation(groupId, nickname);
            return ResponseEntity.ok("초대를 수락하였습니다.");
        } catch (DuplicateNicknameException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()); // 409 Conflict
        } catch (MaxStudyGroupException ex) { // 개인이 참가할 수 있는 최대 그룹 수 초과
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage()); // 403 Forbidden
        } catch (StudyGroupFullException ex) { // 스터디 그룹 인원이 가득찼을 때
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()); // 400 Bad Request
        }
    }

    // 초대 거절
    @PostMapping("/{groupId}/reject")
    public ResponseEntity<?> rejectInvitation(@PathVariable long groupId){
        studyGroupService.rejectInvitation(groupId);
        return ResponseEntity.ok("초대를 거절하였습니다.");
    }

    // 모든 참가자 확인
    @GetMapping("/{groupId}/list/all")
    public ResponseEntity<?> listOfAll(@PathVariable long groupId) {
        try {
            List<GroupParticipants> participants = studyGroupService.listOfEveryone(groupId);
            return ResponseEntity.ok(participants);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 운영진 리스트 확인
    @GetMapping("/{groupId}/list/managers")
    public ResponseEntity<?> listOfManagers(@PathVariable long groupId) {
        try {
            List<GroupParticipants> managers = studyGroupService.listOfManagers(groupId);
            return ResponseEntity.ok(managers);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 팀원 리스트 확인
    @GetMapping("/{groupId}/list/members")
    public ResponseEntity<?> listOfMembers(@PathVariable long groupId) {
        try {
            List<GroupParticipants> members = studyGroupService.listOfMembers(groupId);
            return ResponseEntity.ok(members);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
