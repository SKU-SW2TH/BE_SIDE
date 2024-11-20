package sw.study.studyGroup.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.*;
import sw.study.exception.studyGroup.*;
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
            @RequestParam String nickname,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) Long groupId) {

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
            @RequestBody CreateStudyGroup requestDto) {

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
        List<InvitedResponse> invitedResponses = studyGroupService.checkInvited(accessToken);

        if (invitedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("초대 받은 내역이 존재하지 않습니다.");
        }
        return ResponseEntity.ok(invitedResponses);
    }

    @Override
    @GetMapping("/joinedList")
    public ResponseEntity<?> checkJoinedList(@RequestHeader("Authorization") String accessToken) {
        List<JoinedResponse> joinedResponses = studyGroupService.checkJoined(accessToken);

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
            @PathVariable long groupId,
            @RequestBody nicknameDto searchByNickname) {

        String nickname = searchByNickname.getNickname();

        try {
            studyGroupService.acceptInvitation(accessToken,groupId, nickname);
            return ResponseEntity.ok("초대를 수락하였습니다.");
        } catch (DuplicateNicknameException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()); // 409 Conflict
        } catch (MaxStudyGroupException ex) { // 개인이 참가할 수 있는 최대 그룹 수 초과
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage()); // 403 Forbidden
        } catch (StudyGroupFullException ex) { // 스터디 그룹 인원이 가득찼을 때
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage()); // 400 Bad Request
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @PostMapping("/{groupId}/reject")
    public ResponseEntity<?> rejectInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId){
        studyGroupService.rejectInvitation(accessToken,groupId);
        return ResponseEntity.ok("초대를 거절하였습니다.");
    }


    @Override
    @GetMapping("/{groupId}/list/all")
    public ResponseEntity<?> listOfAll(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId) {
        try {
            List<GroupParticipants> participants = studyGroupService.listOfEveryone(accessToken,groupId);
            return ResponseEntity.ok(participants);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @GetMapping("/{groupId}/list/managers")
    public ResponseEntity<?> listOfManagers(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId) {
        try {
            List<GroupParticipants> managers = studyGroupService.listOfManagers(accessToken,groupId);
            return ResponseEntity.ok(managers);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @GetMapping("/{groupId}/list/members")
    public ResponseEntity<?> listOfMembers(
            @PathVariable long groupId,
            @RequestHeader("Authorization") String accessToken) {
        try {
            List<GroupParticipants> members = studyGroupService.listOfMembers(accessToken,groupId);
            return ResponseEntity.ok(members);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @PatchMapping("/{groupId}/participant/{nickname}/changeRole")
    public ResponseEntity<?> changeRole(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable String nickname) {
        try {
            studyGroupService.changeRole(accessToken,groupId, nickname);
            return ResponseEntity.ok("성공적으로 권한이 수정되었습니다.");
        } catch (UnauthorizedException | PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @GetMapping("/{groupId}/list/waiting")
    public ResponseEntity<?> checkWaiting(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId){
        try{
            List<String> nicknames = studyGroupService.listOfWaiting(accessToken,groupId);
            return ResponseEntity.ok(nicknames);
        } catch (UnauthorizedException | PermissionDeniedException e) {
            // 특정 스터디그룹에 참가하지 않은 비정상적 케이스
            // 혹은 그룹 내에서 권한이 부여되지 않은 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @DeleteMapping("/{groupId}/waitingList/cancellation/{nickname}")
    public ResponseEntity<?> rejectInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable String nickname) {
        try {
            boolean isCancelled = studyGroupService.cancelInvitation(accessToken,groupId, nickname);
            if (isCancelled) {
                return ResponseEntity.ok("초대를 취소 하였습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("초대 취소에 실패하였습니다.");
            }
        } catch (UnauthorizedException | PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @PatchMapping("/{groupId}/participants/nickname")
    public ResponseEntity<?> changeNickname(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @RequestBody nicknameDto nicknameDto){
        try{
            studyGroupService.changeParticipantNickname(accessToken,groupId, nicknameDto.getNickname());
            return ResponseEntity.ok("닉네임 변경에 성공하였습니다.");
        }catch (UnauthorizedException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }catch (DuplicateNicknameException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @PostMapping("/{groupId}/participants/invite")
    public ResponseEntity<?> inviteNewMember(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @RequestBody InviteNewMember listOfMembers){
        try {
            studyGroupService.inviteNewMember(accessToken,groupId, listOfMembers.getSelectedNicknames());
            return ResponseEntity.ok(String.format("총 %d 명에게 초대가 전송되었습니다.", listOfMembers.getSelectedNicknames().size()));
        } catch (StudyGroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (UnauthorizedException | PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @DeleteMapping("/{groupId}/participants/kick/{nickname}")
    public ResponseEntity<?> kickParticipant(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable String nickname){
        try {
            studyGroupService.userKick(accessToken,groupId, nickname);
            return ResponseEntity.ok(String.format("%s 님을 추방하였습니다.", nickname));
        } catch (UnauthorizedException | PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    @Override
    @DeleteMapping("/{groupId}/quit")
    public ResponseEntity<?> quitStudyGroup(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId){
        try {
            studyGroupService.quitGroup(accessToken, groupId);
            return ResponseEntity.ok("해당 스터디그룹을 탈퇴하였습니다.");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
