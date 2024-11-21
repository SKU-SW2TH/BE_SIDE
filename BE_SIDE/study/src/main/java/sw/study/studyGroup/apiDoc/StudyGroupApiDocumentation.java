package sw.study.studyGroup.apiDoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.dto.*;
import java.util.Map;

public interface StudyGroupApiDocumentation {

    // 닉네임 검색
    @Operation(summary = "사용자 검색 ( 신규 그룹 생성 / 추가 사용자 초대 )",
            description = "스터디 그룹 생성 시 활용되는 사용자 검색.<br>"
                    + "groupId 전달 시 스터디 그룹 내에서 신규 사용자 초대시 활용됨. ( 검색 결과에서 기존 그룹 내 참여자는 제외되는 형태 )<br>"
                    + "groupId 를 전달하지 않은 ( 초기 그룹 생성 ) 케이스일 경우에는 전체 검색 결과 반환하는 형태임.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List<String> 으로 결과값 리턴"),
            @ApiResponse(responseCode = "404", description = "조회된 결과가 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "nickname", description = "검색할 닉네임", example = "코난123"),
            @Parameter(name = "page", description = "현재 페이지", example = "1"),
            @Parameter(name = "size", description = "페이지 당 보여질 항목의 수", example = "5"),
            @Parameter(
                    name = "groupId",
                    description = "그룹 ID ( 필수 파라미터 아님, 같은 API 인데 그룹 ID를 전달하면 스터디 그룹에 참여중인 유저들을 제외함",
                    example = "1"
            )
    })
    ResponseEntity<?> searchMembers(@RequestHeader("Authorization") String accessToken,
                                    @RequestParam String nickname,
                                    @RequestParam int page,
                                    @RequestParam int size,
                                    @RequestParam(required = false) Long groupId);

    // 그룹 생성
    @Operation(summary = "스터디그룹 생성", description = "검색 결과를 통해 선택된 닉네임(유저)들을 대상으로 함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공시 생성한 그룹 객체 리턴"),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupName", description = "그룹 이름", example = "리액트 초보방"),
            @Parameter(name = "description", description = "그룹 소개", example = "진짜 처음 하시는 분들만 오시면 좋겠어요. 고수 사절.."),
            @Parameter(name = "selectedNicknames", description = "검색 이후 선택한 닉네임들 (배열의 형태)", example = "[\"스폰지밥\", \"뚱이\", \"집게사장\"]"),
            @Parameter(name = "leaderNickname", description = "그룹 내 사용할 방장의 닉네임", example = "코난123")
    })
    ResponseEntity<Map<String,Object>> createStudyGroup(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody CreateStudyGroup requestDto);
    
    // 받은 초대 확인
    @Operation(summary = "받은 초대 내역 확인", description = "받은 초대 리스트를 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 수락 대기중인 그룹 정보 리스트 응답"),
            @ApiResponse(responseCode = "404", description = "초대를 받은 내역이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    })
    ResponseEntity<?> checkInvitedList(@RequestHeader("Authorization") String accessToken);

    // 참가 중인 그룹 확인
    @Operation(summary = "참가중인 그룹 내역 확인", description = "참가중인 그룹 리스트 반환 ( 나의 그룹 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 참가중인 그룹 정보 리스트 응답"),
            @ApiResponse(responseCode = "404", description = "참여중인 그룹이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    })
    ResponseEntity<?> checkJoinedList(@RequestHeader("Authorization") String accessToken);

    // 초대 수락
    @Operation(summary = "받은 초대 수락", description = "받은 초대를 수락할 때 사용")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대를 수락하였습니다."),
            @ApiResponse(responseCode = "409", description = "해당 닉네임은 그룹 내에서 이미 사용중입니다."),
            @ApiResponse(responseCode = "403", description = "참여할 수 있는 최대 그룹 수를 초과했습니다. ( 각 유저별 최대 참가 그룹 수 제한 - 20)"),
            @ApiResponse(responseCode = "400", description = "이미 그룹 내 인원이 가득 찼습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1"),
            @Parameter(name = "nickname", description = "그룹 내 사용할 닉네임", example = "코난123")
    })
    ResponseEntity<?> acceptInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @RequestBody nicknameDto searchByNickname);

    // 초대 거절
    @Operation(summary = "받은 초대 거절", description = "받은 초대를 거절할 때 사용")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대를 거절하였습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1")
    })
    ResponseEntity<?> rejectInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId);

    // 그룹 내 모든 참가자 확인
    @Operation(summary = "스터디 그룹 내 참가자 확인", description = "그룹 내 모든 참가자 내역 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 참가자 리스트 응답"),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1")
    })
    ResponseEntity<?> listOfAll(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId);

    // 그룹 내 운영진 확인
    @Operation(summary = "스터디 그룹 내 운영진 확인", description = "그룹 내 모든 운영진 확인 ( 방장 제외 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 운영진 리스트 응답"),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1")
    })
    ResponseEntity<?> listOfManagers(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId);

    // 그룹 내 참가자 확인
    @Operation(summary = "스터디 그룹 내 일반 멤버 확인", description = "일반 멤버 리스트 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 일반 멤버 리스트 응답"),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음)."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1")
    })
    ResponseEntity<?> listOfMembers(
            @PathVariable long groupId,
            @RequestHeader("Authorization") String accessToken);

    // 그룹 내 특정 참가자 신분 변경
    @Operation(summary = "스터디 그룹 내 신분 변경 ( 승격 / 강등 ) ", description = "단일 API 로 구성 : 운영진 <-> 멤버 간")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한이 수정되었습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1"),
            @Parameter(name = "nickname", description = "닉네임", example = "코난123"),
    })
    ResponseEntity<?> changeRole(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable String nickname);

    // 스터디 그룹 내 초대 명단 확인
    @Operation(summary = "스터디 그룹 내 초대 명단 확인", description = "초대 명단 확인 ( 방장, 운영진만 가능 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 수락 대기중인 유저 닉네임 리스트 응답"),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1"),
    })
    ResponseEntity<?> checkWaiting(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId);

    // 기존에 발송된 초대 취소
    @Operation(summary = "기존에 발송된 초대 취소", description = "수락/거절 대기중인 특정 사용자의 초대를 강제로 취소한다. ( 방장, 운영진만 가능 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "초대가 취소되었습니다."),
            @ApiResponse(responseCode = "400", description = "초대 취소에 실패하였습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 권한 없음.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1"),
            @Parameter(name = "nickname", description = "닉네임", example = "코난123"),
    })
    ResponseEntity<?> rejectInvitation(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable String nickname);

    // 그룹 내 닉네임 변경
    @Operation(summary = "스터디 그룹 내 닉네임 변경", description = "스터디 그룹 내 변경이며, 중복 확인 로직이 포함 된 형태")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 변경에 성공하였습니다."),
            @ApiResponse(responseCode = "403", description = "해당 그룹에 참가하지 않은 비정상적인 접근입니다."),
            @ApiResponse(responseCode = "409", description = "해당 닉네임은 이미 사용 중입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 Id", example = "1"),
            @Parameter(name = "nickname", description = "닉네임", example = "코난123"),
    })
    ResponseEntity<?> changeNickname(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @RequestBody nicknameDto nicknameDto);

    // 그룹 내 신규 초대
    @Operation(summary = "그룹 내 새로운 멤버 초대",
            description = "그룹의 방장 또는 운영진이 신규 멤버를 초대할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "총 n 명에게 초대가 전송되었습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근 (그룹에 참여 중이지 않거나 권한이 없음)"),
            @ApiResponse(responseCode = "404", description = "해당 그룹이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "selectedNicknames", description = "초대할 사용자들의 닉네임 리스트",
                    example = "[\"스폰지밥\", \"뚱이\", \"집게사장\"]")
    })
    ResponseEntity<?> inviteNewMember(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @RequestBody InviteNewMember listOfMembers);


    // 그룹 내 사용자 추방
    @Operation(summary = "그룹 내 사용자 추방",
            description = "특정 사용자를 그룹에서 강퇴 (방장만 사용 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "[닉네임] 님을 추방하였습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근 (그룹에 참여 중이지 않거나 권한이 없음)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "nickname", description = "추방할 사용자의 닉네임", example = "코난123")
    })
    ResponseEntity<?> kickParticipant(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable String nickname);

    // 그룹 탈퇴
    @Operation(summary = "스터디 그룹 탈퇴", description = "특정 스터디 그룹 탈퇴 ( 방장은 탈퇴 불가 -> 위임 필요 )")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해당 스터디그룹을 탈티하였습니다."),
            @ApiResponse(responseCode = "403", description = "비정상적인 접근입니다 (그룹에 참여중이지 않음 혹은 스터디장은 탈퇴가 불가능합니다.)"),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", required = true, example = "1")
    })
    ResponseEntity<?> quitStudyGroup(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId);

}

