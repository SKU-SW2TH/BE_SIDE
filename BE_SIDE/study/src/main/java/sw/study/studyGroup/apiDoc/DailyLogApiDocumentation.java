package sw.study.studyGroup.apiDoc;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.dto.DailyLogRequestDto;

public interface DailyLogApiDocumentation {

    // 데일리 로그 작성
    @Operation(summary = "데일리 로그 작성",
            description = "데일리 로그 작성 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "로그가 성공적으로 작성되었습니다."),
            @ApiResponse(responseCode = "403", description = "그룹에 참가중이지 않은 비정상적 접근입니다."),
            @ApiResponse(responseCode = "404", description = "해당 스터디그룹은 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "title", description = "데일리 로그 제목", example = "제목 예시"),
            @Parameter(name = "content", description = "데일리 로그 본문", example = "본문 예시입니다. 본문 내용이니까..")
    })
    ResponseEntity<?> createDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @RequestBody DailyLogRequestDto requestDto);
    
    // 데일리 로그 조회
    @Operation(summary = "데일리 로그 조회",
            description = "전체 로그 리스트를 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공시 조회된 객체들 리스트 응답"),
            @ApiResponse(responseCode = "403", description = "그룹에 참가중이지 않은 비정상적 접근입니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1")
    })
    ResponseEntity<?> listOfDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId);


    // 데일리 로그 수정
    @Operation(summary = "데일리 로그 수정",
            description = "특정 로그를 수정하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그가 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "403", description = "수정할 수 있는 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "해당하는 로그가 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "logId", description = "데일리 로그 ID", example = "1"),
            @Parameter(name = "title", description = "로그 제목", example = "제목 예시"),
            @Parameter(name = "content", description = "로그 본문", example = "본문 예시입니다. 본문 내용이니까..")
    })
    ResponseEntity<?> updateDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable long logId,
            @RequestBody DailyLogRequestDto requestDto);


    // 데일리 로그 삭제
    @Operation(summary = "공지사항 작성",
            description = "리더 혹은 운영진만 가능함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "로그가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "403", description = "삭제 할 수 있는 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "해당하는 로그가 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "logId", description = "데일리 로그 ID", example = "1")
    })
    ResponseEntity<?> deleteDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable long logId);
}
