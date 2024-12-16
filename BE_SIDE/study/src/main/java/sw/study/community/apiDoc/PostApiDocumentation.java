package sw.study.community.apiDoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sw.study.admin.dto.ReportRequest;
import sw.study.community.dto.CommentRequest;

import java.util.List;

@Tag(name = "Post", description = "커뮤니티 관련 API")
public interface PostApiDocumentation {

    @Operation(summary = "게시글 생성", description = "게시글 생성하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성된 게시글의 postId를 반환."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "해당하는 카테고리가 존재하지 않습니다."),
            @ApiResponse(responseCode = "404", description = "해당하는 사용자가 존재하지 않습니다."),
            @ApiResponse(responseCode = "404", description = "해당하는 분야가 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "title", description = "제목", example = "안녕하세요 질문있습니다..."),
            @Parameter(name = "content", description = "내용", example = "제가 진로관련 질문이 생겨서 여쭤봐요...."),
            @Parameter(name = "category", description = "카테고리(FREE/QUESTION)", example = "FREE"),
            @Parameter(name = "area", description = "해당 게시글이 속하는 분야를 리스트 형태로 입력", example = "Java"),
            @Parameter(name = "files", description = "게시글에 포함된 파일 리스트", example = "사진이나 파일"),
    })
    ResponseEntity<?> createPost(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam(value = "area", required = false) List<String> area,
            @RequestParam(value = "files", required = false) List<MultipartFile> files);



    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 정보가 반환됩니다."),
            @ApiResponse(responseCode = "400", description = "이미 삭제된 게시글입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "postId", description = "조회할 게시글의 ID", example = "1")
    })
    ResponseEntity<?> getPostDetail(
            @PathVariable("postId") Long postId);



    @Operation(summary = "게시글 삭제", description = "특정 게시글을 논리적으로 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글이 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "401", description = "본인이 작성한 게시글만 삭제할 수 있습니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "postId", description = "삭제할 게시글의 ID", example = "1")
    })
    ResponseEntity<?> deletePost(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId);



    @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "좋아요가 성공적으로 추가되었습니다."),
            @ApiResponse(responseCode = "400", description = "중복 요청입니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "postId", description = "좋아요를 추가할 게시글의 ID", example = "1")
    })
    ResponseEntity<?> likePost(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId);



    @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글의 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요가 성공적으로 취소되었습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글 또는 좋아요를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "postId", description = "좋아요를 취소할 게시글의 ID", example = "1")
    })
    ResponseEntity<?> cancelLikePost(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId);



    @Operation(summary = "게시글 신고", description = "특정 게시글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고가 성공적으로 접수되었습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."),
            @Parameter(name = "postId", description = "신고할 게시글의 ID", example = "1"),
            @Parameter(name = "reportRequest", description = "reportTargetType : POST, COMMENT\nreportReason : SPAM_ADVERTISING, ILLEGAL_CONTENT, CONTENT_FOR_YOUTH, VULGAR_OR_DISCRIMINATORY_EXPRESSION, PERSONAL_INFORMATION_LEAK, INAPPROPRIATE_EXPRESSION",
                    example = "{\n" +
                    "  \"description\": \"이 게시글은 불법적인 내용을 포함하고 있습니다.\",\n" +
                    "  \"reportReason\": \"ILLEGAL_CONTENT\",\n" +
                    "  \"reportTargetType\": \"POST\"\n" +
                    "}\n")
    })
    ResponseEntity<?> reportPost(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @RequestBody ReportRequest reportRequest);


    
    @Operation(summary = "댓글 생성", description = "특정 게시글에 댓글을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글이 성공적으로 추가되었습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "댓글을 추가할 게시글의 ID", example = "1"),
            @Parameter(name = "content", description = "댓글 내용", example = "댓글 내용 예시")
    })
    ResponseEntity<?> createComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @RequestBody CommentRequest commentRequest);

    
    
    @Operation(summary = "댓글 삭제", description = "특정 게시글에 달린 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글이 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "401", description = "작성자만 댓글을 삭제할 수 있습니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "삭제할 댓글 ID", example = "2")
    })
    ResponseEntity<?> deleteComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId);

    
    
    @Operation(summary = "댓글 좋아요", description = "특정 댓글에 좋아요를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "좋아요가 성공적으로 추가되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "좋아요를 추가할 댓글 ID", example = "2")
    })
    ResponseEntity<?> likeComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId);



    @Operation(summary = "댓글 좋아요 취소", description = "특정 댓글의 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요가 성공적으로 취소되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "좋아요를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "좋아요를 취소할 댓글 ID", example = "2")
    })
    ResponseEntity<?> cancelLikeComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId);



    @Operation(summary = "댓글 신고", description = "특정 댓글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고가 성공적으로 접수되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "신고할 댓글 ID", example = "2"),
            @Parameter(name = "reportRequest", description = "신고 내용, 세자세한 입력 예시는 게시글 신고 API 참고", example = "부적절한 내용 신고")
    })
    ResponseEntity<?> reportComment(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody ReportRequest reportRequest);



    @Operation(summary = "대댓글 생성", description = "특정 댓글에 대댓글을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "대댓글이 성공적으로 추가되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "대댓글을 추가할 댓글 ID", example = "2"),
            @Parameter(name = "content", description = "대댓글 내용", example = "대댓글 내용 예시")
    })
    ResponseEntity<?> createReply(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @RequestBody CommentRequest commentRequest);



    @Operation(summary = "대댓글 삭제", description = "특정 대댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대댓글이 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "401", description = "본인만 대댓글을 삭제할 수 있습니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "대댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "댓글 ID", example = "2"),
            @Parameter(name = "replyId", description = "삭제할 대댓글 ID", example = "3")
    })
    ResponseEntity<?> deleteReply(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @PathVariable("replyId") Long replyId);


    @Operation(summary = "대댓글 좋아요", description = "특정 대댓글에 좋아요를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "좋아요가 성공적으로 추가되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "대댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "댓글 ID", example = "2"),
            @Parameter(name = "replyId", description = "좋아요를 추가할 대댓글 ID", example = "3")
    })
    ResponseEntity<?> likeReply(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @PathVariable("replyId") Long replyId);


    @Operation(summary = "대댓글 좋아요 취소", description = "특정 대댓글의 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요가 성공적으로 취소되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "대댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "좋아요를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "댓글 ID", example = "2"),
            @Parameter(name = "replyId", description = "좋아요를 취소할 대댓글 ID", example = "3")
    })
    ResponseEntity<?> cancelLikeReply(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @PathVariable("replyId") Long replyId);



    @Operation(summary = "대댓글 신고", description = "특정 대댓글을 신고합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고가 성공적으로 접수되었습니다."),
            @ApiResponse(responseCode = "400", description = "댓글이 해당 게시글에 속하지 않습니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "대댓글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", example = "Bearer token"),
            @Parameter(name = "postId", description = "게시글 ID", example = "1"),
            @Parameter(name = "commentId", description = "댓글 ID", example = "2"),
            @Parameter(name = "replyId", description = "신고할 대댓글 ID", example = "3"),
            @Parameter(name = "reportRequest", description = "신고 내용, 자세한 입력 예시는 게시글 신고 API 참고", example = "부적절한 대댓글 신고")
    })
    ResponseEntity<?> reportReply(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("postId") Long postId,
            @PathVariable("commentId") Long commentId,
            @PathVariable("replyId") Long replyId,
            @RequestBody ReportRequest reportRequest);



    @Operation(summary = "내가 작성한 게시글 조회", description = "사용자가 작성한 게시글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자가 작성한 게시글 리스트가 반환됩니다."),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "Authorization", description = "사용자 인증 토큰", required = true, example = "Bearer token"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
    })
    ResponseEntity<?> getMyPosts(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(value = "page", defaultValue = "0") int page);




    @Operation(summary = "게시글 리스트 조회", description = "특정 카테고리의 게시글 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 리스트가 성공적으로 반환됩니다."),
            @ApiResponse(responseCode = "404", description = "해당 카테고리에 게시글이 존재하지 않습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "category", description = "조회할 게시글의 카테고리 (FREE/QUESTION)", required = true, example = "FREE"),
            @Parameter(name = "sortBy", description = "정렬 기준 (기본값: createdAt)", example = "viewCount/commentCount/createdAt"),
            @Parameter(name = "searchType", description = "검색 기준 (title: 제목, author: 작성자)", example = "title/author/title+author"),
            @Parameter(name = "keyword", description = "검색 키워드", example = "Spring Boot"),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
    })
    ResponseEntity<?> getPostsByCategory(
            @RequestParam String category,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page);

}
