package sw.study.studyGroup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.apiDoc.StudyGroupApiDocumentation;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.*;
import sw.study.studyGroup.service.StudyGroupService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studyGroup")
@RequiredArgsConstructor
@Tag(name = "StudyGroup", description = "스터디 그룹 관련 API ( Access Token 필요 )")
public class StudyGroupController implements StudyGroupApiDocumentation{

    private final StudyGroupService studyGroupService;

    @Override
    @GetMapping("/searchMembers")
    public ResponseEntity<?> searchMembers(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(name = "nickname") String nickname,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size,
            @RequestParam(name = "groupId", required = false) Long groupId) {
        List<String> results = studyGroupService.searchByNickname(accessToken,nickname,page,size,groupId);

        if(results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("조회된 결과가 없습니다.");
        }
        return ResponseEntity.ok(results);
    }

    @Override
    @PostMapping("/create")
    public ResponseEntity<Map<String,Object>> createStudyGroup(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody StudyGroupRequest requestDto) {

        StudyGroup createdGroup = studyGroupService.createStudyGroup(
                accessToken,
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

    @Override
    @GetMapping("/invitedList")
    public ResponseEntity<?> checkInvitedList(@RequestHeader("Authorization") String accessToken) {
        List<StudyGroupResponse> invitedResponses = studyGroupService.checkInvited(accessToken);

        if (invitedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("초대 받은 내역이 존재하지 않습니다.");
        }
        return ResponseEntity.ok(invitedResponses);
    }

    @Override
    @GetMapping("/joinedList")
    public ResponseEntity<?> checkJoinedList(@RequestHeader("Authorization") String accessToken) {
        List<StudyGroupResponse> joinedResponses = studyGroupService.checkJoined(accessToken);

        if (joinedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("참여중인 스터디그룹이 존재하지 않습니다.");
        }
        return ResponseEntity.ok(joinedResponses);
    }

    @Override
    @PostMapping("/{groupId}/accept")
    public ResponseEntity<?> acceptInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody String nickname) {

        studyGroupService.acceptInvitation(accessToken,groupId, nickname);
        return ResponseEntity.ok("초대를 수락하였습니다.");
    }

    @Override
    @PostMapping("/{groupId}/reject")
    public ResponseEntity<?> rejectInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId){

        studyGroupService.rejectInvitation(accessToken,groupId);
        return ResponseEntity.ok("초대를 거절하였습니다.");
    }


    @Override
    @GetMapping("/{groupId}/list/all")
    public ResponseEntity<?> listOfAll(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId) {

        List<ParticipantsResponse> participants = studyGroupService.listOfEveryone(accessToken,groupId);
        return ResponseEntity.ok(participants);
    }

    @Override
    @GetMapping("/{groupId}/list/managers")
    public ResponseEntity<?> listOfManagers(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId) {

        List<ParticipantsResponse> managers = studyGroupService.listOfManagers(accessToken,groupId);
        return ResponseEntity.ok(managers);
    }

    @Override
    @GetMapping("/{groupId}/list/members")
    public ResponseEntity<?> listOfMembers(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId) {

        List<ParticipantsResponse> members = studyGroupService.listOfMembers(accessToken,groupId);
        return ResponseEntity.ok(members);
    }

    @Override
    @PatchMapping("/{groupId}/participants/changeRole/{nickname}")
    public ResponseEntity<?> changeRole(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("nickname") String nickname) {

        studyGroupService.changeRole(accessToken,groupId, nickname);
        return ResponseEntity.ok("성공적으로 권한이 수정되었습니다.");
    }

    @Override
    @GetMapping("/{groupId}/list/waiting")
    public ResponseEntity<?> checkWaiting(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId){

        List<String> nicknames = studyGroupService.listOfWaiting(accessToken,groupId);
        return ResponseEntity.ok(nicknames);
    }

    @Override
    @DeleteMapping("/{groupId}/waiting/cancel/{nickname}")
    public ResponseEntity<?> rejectInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("nickname") String nickname) {

        boolean isCancelled = studyGroupService.cancelInvitation(accessToken,groupId, nickname);
        if (isCancelled) {
            return ResponseEntity.ok("초대를 취소 하였습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("초대 취소에 실패하였습니다.");
        }
    }

    @Override
    @PatchMapping("/{groupId}/participants/changeNickname")
    public ResponseEntity<?> changeNickname(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody String nickname){

        studyGroupService.changeParticipantNickname(accessToken,groupId, nickname);
        return ResponseEntity.ok("닉네임 변경에 성공하였습니다.");
    }

    @Override
    @PostMapping("/{groupId}/participants/invite")
    public ResponseEntity<?> inviteNewMember(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody List<String> nicknames){

        studyGroupService.inviteNewMember(accessToken,groupId, nicknames);
        return ResponseEntity.ok(String.format("총 %d 명에게 초대가 전송되었습니다.", nicknames.size()));
    }

    @Override
    @DeleteMapping("/{groupId}/participants/kick/{nickname}")
    public ResponseEntity<?> kickParticipant(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("nickname") String nickname){

        studyGroupService.userKick(accessToken,groupId, nickname);
        return ResponseEntity.ok(String.format("%s 님을 추방하였습니다.", nickname));
    }

    @Override
    @DeleteMapping("/{groupId}/quit")
    public ResponseEntity<?> quitStudyGroup(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId){

        studyGroupService.quitGroup(accessToken, groupId);
        return ResponseEntity.ok("해당 스터디그룹을 탈퇴하였습니다.");
    }
}
