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
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.*;
import sw.study.studyGroup.service.StudyGroupService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studyGroup")
@RequiredArgsConstructor
@Tag(name = "StudyGroup", description = "스터디 그룹 내 모든 API ( 모두 로그인 이후 Token 핸들링 필요 )")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping("/searchMembers")
    @Operation(summary = "사용자 검색 ( 신규 그룹 생성 / 추가 사용자 초대 )",
            description = "스터디 그룹 생성 시 활용되는 사용자 검색.<br>"
                    + "groupId 전달 시 스터디 그룹 내에서 신규 사용자 초대시 활용됨. ( 검색 결과에서 기존 그룹 내 참여자는 제외되는 형태 )<br>"
                    + "groupId 를 전달하지 않은 ( 초기 그룹 생성 ) 케이스일 경우에는 전체 검색 결과 반환하는 형태임."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 검색이 완료되었습니다."),
            @ApiResponse(responseCode = "404", description = "조회된 결과가 없습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "nickname", description = "닉네임"),
            @Parameter(name = "page", description = "현재 페이지"),
            @Parameter(name = "size", description = "페이지당 보여질 항목의 수"),
            @Parameter(name = "groupId", description = "그룹 ID")
    })
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

    @PostMapping("/create")
    @Operation(summary = "스터디그룹 생성", description = "검색 결과를 통해 선택된 닉네임(유저)들을 대상으로 함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스터디 그룹이 성공적으로 생성되었습니다."),
    })
    @Parameters(value = {
            @Parameter(name = "groupName", description = "그룹 이름"),
            @Parameter(name = "description", description = "그룹에 대한 설명"),
            @Parameter(name = "selectedNicknames", description = "선택된 닉네임 리스트(초대 대상)"),
            @Parameter(name = "leaderNickname", description = "방장 닉네임")
    })
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
    
    @GetMapping("/invitedList")
    @Operation(summary = "받은 초대 내역 확인", description = "받은 초대 내역을 확인하는 API ( 받은 초대 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대를 받은 내역이 존재합니다."),
            @ApiResponse(responseCode = "404", description = "초대를 받은 내역이 존재하지 않습니다."),
    })
    public ResponseEntity<?> checkInvitedList(@RequestHeader("Authorization") String accessToken) {
        List<InvitedResponse> invitedResponses = studyGroupService.checkInvited(accessToken);

        if (invitedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("초대 받은 내역이 존재하지 않습니다.");
        }
        return ResponseEntity.ok(invitedResponses);
    }

    @GetMapping("/joinedList")
    @Operation(summary = "참가중인 그룹 내역 확인", description = "참가중인 그룹 리스트 반환 ( 나의 그룹 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참여중인 내역이 존재합니다."),
            @ApiResponse(responseCode = "404", description = "참여중인 그룹이 존재하지 않습니다."),
    })
    public ResponseEntity<?> checkJoinedList(@RequestHeader("Authorization") String accessToken) {
        List<JoinedResponse> joinedResponses = studyGroupService.checkJoined(accessToken);

        if (joinedResponses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("참여중인 스터디그룹이 존재하지 않습니다.");
            //해당 부분은 custom Exception 으로 수정 예정
        }

        return ResponseEntity.ok(joinedResponses);
    }
    
    @PostMapping("/{groupId}/accept")
    @Operation(summary = "받은 초대 수락", description = "받은 초대를 수락할 때 사용")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대를 수락하였습니다."),
            @ApiResponse(responseCode = "409", description = "해당 닉네임은 그룹 내에서 이미 사용중입니다."),
            @ApiResponse(responseCode = "403", description = "참여할 수 있는 최대 그룹 수를 초과했습니다."),
            @ApiResponse(responseCode = "400", description = "이미 그룹 내 인원이 가득 찼습니다.")
    })
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

    @PostMapping("/{groupId}/reject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대를 거절하였습니다."),
    })
    @Operation(summary = "받은 초대 거절", description = "받은 초대를 거절할 때 사용")
    public ResponseEntity<?> rejectInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId){
        studyGroupService.rejectInvitation(accessToken,groupId);
        return ResponseEntity.ok("초대를 거절하였습니다.");
    }


    @GetMapping("/{groupId}/list/all")
    @Operation(summary = "스터디 그룹 내 참가자 확인", description = "그룹 내 모든 참가자 내역 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참가자 리스트 조회에 성공하였습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
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

    @GetMapping("/{groupId}/list/managers")
    @Operation(summary = "스터디 그룹 내 참가자 ( 운영진 ) 확인", description = "그룹 내 모든 운영진 확인 ( 방장 제외 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "운영진 리스트 조회에 성공하였습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
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

    @GetMapping("/{groupId}/list/members")
    @Operation(summary = "스터디 그룹 내 참가자 ( 멤버 ) 확인", description = "일반 멤버 리스트 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멤버 목록을 반환했습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음)."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
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

    @PatchMapping("/{groupId}/participant/{nickname}/changeRole")
    @Operation(summary = "스터디 그룹 내 신분 변경 ( 승격 / 강등 ) ", description = "단일 API 로 구성 : 운영진 <-> 멤버 간")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한이 수정되었습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
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

    @GetMapping("/{groupId}/list/waiting")
    @Operation(summary = "스터디 그룹 내 초대 명단 확인", description = "초대 명단 확인 ( 방장, 운영진만 가능 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대 명단을 확인했습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
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

    @DeleteMapping("/{groupId}/waitingList/cancellation/{nickname}")
    @Operation(summary = "기존에 발송된 초대 취소", description = "수락/거절 대기중인 특정 사용자의 초대를 강제로 취소한다. ( 방장, 운영진만 가능 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대가 취소되었습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
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

    @PatchMapping("/{groupId}/participants/nickname")
    @Operation(summary = "스터디 그룹 내 닉네임 변경", description = "스터디 그룹 내 변경이며, 중복 확인 로직이 포함 된 형태")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 변경에 성공하였습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다. ( 그룹에 참여중이지 않음 )"),
            @ApiResponse(responseCode = "409", description = "해당 닉네임은 이미 사용중입니다.")
    })
    @Parameters(value = {
            @Parameter(name = "nickname", description = "닉네임"),
    })
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

    @PostMapping("/{groupId}/participants/invite")
    @Operation(summary = "그룹 내 새로운 멤버 초대", description = "사용자들을 추가로 추가하고 싶을 때 사용하는 API ( 방장 / 운영진 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대 전송 성공"),
            @ApiResponse(responseCode = "404", description = "스터디 그룹을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)")
    })
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

    @DeleteMapping("/{groupId}/participants/kick/{nickname}")
    @Operation(summary = "그룹 내 사용자 추방", description = "특정 사용자를 강퇴한다. 단 우선은 방장만 해당 권한 보유.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 추방 성공"),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)")
    })
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

    @DeleteMapping("/{groupId}/quit")
    @Operation(summary = "스터디 그룹 탈퇴", description = "특정 스터디 그룹 탈퇴 ( 방장은 탈퇴 불가 -> 위임 필요 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스터디 그룹 탈퇴 성공"),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음)")
    })
    public ResponseEntity<?> quitStudyGroup(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId){
        try {
            studyGroupService.quitGroup(accessToken, groupId);
            return ResponseEntity.ok("해당 스터디그룹을 탈퇴하였습니다.");
        } catch (UnauthorizedException | PermissionDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
