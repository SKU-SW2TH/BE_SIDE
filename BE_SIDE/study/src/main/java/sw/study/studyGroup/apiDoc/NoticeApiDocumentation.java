package sw.study.studyGroup.apiDoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.dto.NoticeRequestDto;


public interface NoticeApiDocumentation {

    // 공지사항 작성
    @Operation(summary = "공지사항 작성",
            description = "리더 혹은 운영진만 가능함")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "공지사항이 성공적으로 작성되었습니다."),
            @ApiResponse(responseCode = "403", description = "작성할 수 있는 권한이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "title", description = "게시글 제목", example = "공지사항 제목 예시"),
            @Parameter(name = "content", description = "게시글 본문", example = "본문 예시입니다. 본문 내용이니까..")
    })
    ResponseEntity<?> createNotice(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody NoticeRequestDto requestDto);

    // 공지사항 목록 조회
    @Operation(summary = "공지사항 리스트 조회",
            description = "페이지네이션 : 페이지 번호와 보여질 페이지마다의 데이터 수 필요")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회에 성공하였습니다. + 게시글 리스트 응답"),
            @ApiResponse(responseCode = "404", description = "공지사항이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "page", description = "현재 페이지 ( 0 부터 시작하는것 주의 ) ", example = "0"),
            @Parameter(name = "size", description = "페이지당 보여질 항목 수", example = "5")
    })
    ResponseEntity<?> listOfNotices(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestParam int page,
            @RequestParam int size);


    // 특정 게시글 상세 조회
    @Operation(summary = "공지사항 상세 조회",
            description = "특정 게시글 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회에 성공하였습니다. + 특정 게시글 상세 정보 응답"),
            @ApiResponse(responseCode = "404", description = "공지사항이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "noticeId", description = "게시글 ID", example = "1")
    })
    ResponseEntity<?> noticeDetail(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("noticeId") Long noticeId);


    // 공지사항 수정
    @Operation(summary = "공지사항 수정",
            description = "특정 공지사항 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공지사항이 성공적으로 수정되었습니다."),
            @ApiResponse(responseCode = "403", description = "수정 할 수 있는 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "공지사항이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "noticeId", description = "게시글 ID", example = "1"),
            @Parameter(name = "title", description = "게시글 제목", example = "공지사항 제목 예시"),
            @Parameter(name = "content", description = "게시글 본문", example = "본문 예시입니다. 본문 내용이니까..")
    })
    ResponseEntity<?> updateNotice(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId")Long groupId,
            @PathVariable("noticeId") Long noticeId,
            @RequestBody NoticeRequestDto noticeRequestDto);

    // 공지사항 삭제
    @Operation(summary = "공지사항 삭제",
            description = "특정 공지사항 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "공지사항이 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "403", description = "삭제 할 수 있는 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "공지사항이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "groupId", description = "스터디 그룹의 ID", example = "1"),
            @Parameter(name = "noticeId", description = "게시글 ID", example = "1")
    })
    ResponseEntity<?> deleteNotice(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("noticeId") Long noticeId);
}
