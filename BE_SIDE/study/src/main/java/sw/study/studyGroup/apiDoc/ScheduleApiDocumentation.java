package sw.study.studyGroup.apiDoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.dto.ScheduleRequest;

public interface ScheduleApiDocumentation {

    // 일정 생성
    @Operation(summary = "일정 생성 API",
            description = "방장 및 운영진만 호출 가능")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "일정이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "403", description = "그룹에 참여중이지 않습니다. / 운영진 권한을 가지고 있지 않습니다."),
            @ApiResponse(responseCode = "404", description = "해당 스터디그룹이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 ID", example = "1"),
            @Parameter(name = "title", description = "제목", example = "리액트 공부하자"),
            @Parameter(name = "description", description = "설명", example = "오늘은 useEffect 공부해보자고"),
            @Parameter(name = "startDate", description = "시작일 (yyyy-MM-DD)", example = "2024-12-08"),
            @Parameter(name = "endDate", description = "종료일", example = "2024-12-10"),
    })
    public ResponseEntity<?> createSchedule(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody ScheduleRequest requestDto);

    // 1달 전체 일정 조회
    @Operation(summary = "1달 일정 조회 (description 제외)",
            description = "연도, 월 입력 데이터 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회에 성공하였습니다 + 일정 리스트 리턴"),
            @ApiResponse(responseCode = "403", description = "그룹에 참여중이지 않습니다."),
            @ApiResponse(responseCode = "404", description = "해당 스터디그룹이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 ID", example = "1"),
            @Parameter(name = "year", description = "연", example = "2024"),
            @Parameter(name = "month", description = "월", example = "12"),
    })
    public ResponseEntity<?> listOfSchedules(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month);

    // 특정 일정 상세
    @Operation(summary = "특정 일정 상세 조회 (description 포함)",
            description = "연도, 월 입력 데이터 필요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회에 성공하였습니다 + 특정 스케줄 정보 리턴"),
            @ApiResponse(responseCode = "403", description = "그룹에 참여중이지 않습니다."),
            @ApiResponse(responseCode = "404", description = "해당 스터디그룹이 존재하지 않습니다. / 일정이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 ID", example = "1"),
            @Parameter(name = "scheduleId", description = "스케줄 ID", example = "1"),
    })
    public ResponseEntity<?> scheduleDetail(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("scheduleId") Long scheduleId);

    // 일정 수정
    @Operation(summary = "특정 스케줄을 수정하는 API",
            description = "방장 혹은 운영진 권한 필요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일정이 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "403", description = "그룹에 참여중이지 않습니다. / 운영진 권한을 가지고 있지 않습니다."),
            @ApiResponse(responseCode = "404", description = "해당 일정이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 ID", example = "1"),
            @Parameter(name = "scheduleId", description = "스케줄 ID", example = "1"),
            @Parameter(name = "title", description = "제목", example = "리액트 공부하자"),
            @Parameter(name = "description", description = "설명", example = "오늘은 useEffect 공부해보자고"),
            @Parameter(name = "startDate", description = "시작일 (yyyy-MM-DD)", example = "2024-12-08"),
            @Parameter(name = "endDate", description = "종료일 (yyyy-MM-DD)", example = "2024-12-10"),
    })
    public ResponseEntity<?> updateSchedule(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody ScheduleRequest scheduleRequest);

    // 일정 삭제
    @Operation(summary = "특정 스케줄을 삭제하는 API",
            description = "방장 혹은 운영진 권한 필요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "본문 없음! ( status 만 반환 ) "),
            @ApiResponse(responseCode = "403", description = "그룹에 참여중이지 않습니다. / 운영진 권한을 가지고 있지 않습니다."),
            @ApiResponse(responseCode = "404", description = "해당 일정이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생하였습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "그룹 ID", example = "1"),
            @Parameter(name = "scheduleId", description = "스케줄 ID", example = "1"),
    })
    public ResponseEntity<?> deleteSchedule(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("scheduleId") Long scheduleId);
}
