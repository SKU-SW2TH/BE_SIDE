package sw.study.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sw.study.admin.dto.ReportRequest;
import sw.study.community.dto.CommentRequest;
import sw.study.community.dto.PostDetailResponse;
import sw.study.community.dto.PostRequest;
import sw.study.community.service.CommentService;
import sw.study.community.service.PostService;
import sw.study.exception.InvalidTokenException;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.community.*;
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.user.service.MemberService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final MemberService memberService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("category") String category,
            @RequestParam(value = "area", required = false) List<String> area, // 다중값 받기
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        log.info("게시글 생성 요청: title = {}, content = {}, category = {}, area = {}", title, content, category, area);

        try {
            Long memberId = memberService.getMemberIdByToken(accessToken);

            PostRequest postRequest = new PostRequest();
            postRequest.setTitle(title);
            postRequest.setContent(content);
            postRequest.setCategory(category);
            postRequest.setArea(area);
            postRequest.setFiles(files);

            Long postId = postService.save(postRequest, memberId);

            return ResponseEntity.status(HttpStatus.CREATED).body(postId + " : 성공적으로 게시글을 만들었습니다.");
        } catch (CategoryNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AreaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        log.info("게시글 상세 조회 요청: postId = {}", postId);
        try {
            PostDetailResponse postDetailResponse = postService.getPostById(postId);
            return ResponseEntity.status(HttpStatus.OK).body(postDetailResponse);


        } catch (PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId) {
        log.info("게시글 삭제 요청: postId = {}", postId);
        try {
            Long memberId = memberService.getMemberIdByToken(accessToken);
            postService.delete(postId, memberId);
            return ResponseEntity.ok("게시글이 정상적으로 삭제되었습니다");


        } catch (PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); // 본인이 작성한 게시글이 아니면 401
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId) {
        try{
            Long memberId = memberService.getMemberIdByToken(accessToken);
            log.info("게시글 좋아요 요청: postId = {}, memberId = {}", postId, memberId);
            postService.addLike(postId, memberId);
            return ResponseEntity.status(HttpStatus.CREATED).body("게시글에 좋아요가 정상적으로 추가되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (DuplicateLikeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> cancelLikePost(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId) {
        try {
            Long memberId = memberService.getMemberIdByToken(accessToken);
            postService.cancelLike(postId, memberId);
            log.info("게시글 좋아요 취소 요청: postId = {}, memberId = {}", postId, memberId);
            return ResponseEntity.ok("성공적으로 좋아요를 취소했습니다.");


        } catch (PostNotFoundException | UserNotFoundException | LikeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    @PostMapping("/{postId}/report")
    public ResponseEntity<?> reportPost(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @RequestBody ReportRequest reportRequest) {
        try {
            Long reporterId = memberService.getMemberIdByToken(accessToken);
            log.info("게시글 신고 요청: targetId = {}, reporterId = {}", postId, reporterId);
            postService.report(reportRequest, postId, reporterId);
            return ResponseEntity.ok("신고가 성공적으로 접수되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<?> createComment(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @RequestBody CommentRequest commentRequest) {
        try {
            Long commenterId = memberService.getMemberIdByToken(accessToken);
            log.info("게시글 댓글 요청: postId = {}, commenterId = {}", postId, commenterId);
            commentService.save(commentRequest, postId, commenterId);
            return ResponseEntity.status(HttpStatus.CREATED).body("정상적으로 댓글이 생성되었습니다.");
        } catch (PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId) {
        log.info("게시글 댓글 삭제 요청: postId = {}, commentId = {}", postId, commentId);
        try {
            Long memberId = memberService.getMemberIdByToken(accessToken);
            commentService.delete(postId, commentId, memberId);
            return ResponseEntity.ok("정상적으로 댓글이 삭제되었습니다.");


        } catch (PostNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); // 본인이 작성한 댓글이 아닐 경우 401
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment/{commentId}/like")
    public ResponseEntity<?> likeComment(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId) {
        log.info("댓글 좋아요 요청: postId = {}, commentId = {}", postId, commentId);
        try {
            Long likerId = memberService.getMemberIdByToken(accessToken);
            commentService.addLike(postId, commentId, likerId);
            return ResponseEntity.ok("성공적으로 좋아요를 달았습니다.");


        } catch (CommentNotFoundException | PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/comment/{commentId}/like")
    public ResponseEntity<?> cancelLikeComment(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId) {
        log.info("댓글 좋아요 취소 요청: postId = {}, commentId = {}", postId, commentId);
        try {
            Long cancelerId = memberService.getMemberIdByToken(accessToken);
            commentService.cancelLike(postId, commentId, cancelerId);
            return ResponseEntity.ok("성공적으로 좋아요를 취소했습니다.");


        } catch (CommentNotFoundException | PostNotFoundException | UserNotFoundException | LikeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment /{commentId}/report")
    public ResponseEntity<?> reportComment(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId, @RequestBody ReportRequest reportRequest) {
        try {
            Long reporterId = memberService.getMemberIdByToken(accessToken);
            log.info("댓글 신고 요청: targetId = {}, reporterId = {}", commentId, reporterId);
            commentService.report(reportRequest, postId, commentId, reporterId);
            return ResponseEntity.ok("신고가 성공적으로 접수되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment/{commentId}/reply")
    public ResponseEntity<?> createReply(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId, @RequestBody CommentRequest commentRequest) {
        try {
            Long replierId = memberService.getMemberIdByToken(accessToken);
            log.info("대댓글 요청: postId = {}, commentId={}, replierId = {}", postId, commentId, replierId);
            commentService.reply(commentRequest, postId, commentId, replierId);
            return ResponseEntity.status(HttpStatus.CREATED).body("정상적으로 대댓글이 생성되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment/{commentId}/reply/{replyId}/like")
    public ResponseEntity<?> likeReply(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId, @PathVariable Long replyId) {
        try {
            Long likerId = memberService.getMemberIdByToken(accessToken);
            log.info("대댓글 좋아요 요청");
            commentService.addReplyLike(postId, commentId, replyId, likerId);
            return ResponseEntity.status(HttpStatus.CREATED).body("대댓글에 좋아요가 성공적으로 추가되었습니다.");
        } catch (PostNotFoundException | CommentNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 대댓글 좋아요 취소
    @DeleteMapping("/{postId}/comment/{commentId}/reply/{replyId}/like")
    public ResponseEntity<?> cancelLikeReply(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId, @PathVariable Long replyId) {
        log.info("대댓글 좋아요 취소 요청");
        try {
            Long cancelerId = memberService.getMemberIdByToken(accessToken);
            commentService.cancelReplyLike(postId, commentId, replyId, cancelerId);
            return ResponseEntity.ok("대댓글 좋아요가 성공적으로 취소되었습니다.");
        } catch (PostNotFoundException | CommentNotFoundException | UserNotFoundException | LikeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 대댓글 삭제
    @DeleteMapping("/{postId}/comment/{commentId}/reply/{replyId}")
    public ResponseEntity<?> deleteReply(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId, @PathVariable Long replyId) {
        log.info("대댓글 삭제 요청");
        try {
            Long memberId = memberService.getMemberIdByToken(accessToken);
            commentService.deleteReply(postId, commentId, replyId, memberId);
            return ResponseEntity.ok("대댓글이 성공적으로 삭제되었습니다.");


        } catch (PostNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); // 본인이 작성한 대댓글이 아닐 경우 401
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 대댓글 신고
    @PostMapping("/{postId}/comment/{commentId}/reply/{replyId}/report")
    public ResponseEntity<?> reportReply(@RequestHeader("Authorization") String accessToken, @PathVariable Long postId, @PathVariable Long commentId, @PathVariable Long replyId, @RequestBody ReportRequest reportRequest) {
        try {
            Long reporterId = memberService.getMemberIdByToken(accessToken);
            log.info("대댓글 신고 요청: replyId = {}, reporterId = {}", replyId, reporterId);
            Long reportId = commentService.reportReply(reportRequest, postId, commentId, replyId, reporterId);
            return ResponseEntity.ok(reportId);


        } catch (PostNotFoundException | UserNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }  catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다."); // 잘못된 토큰이면 401 Unauthorized 응답
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
