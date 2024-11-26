package sw.study.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.admin.dto.ReportRequest;
import sw.study.community.dto.CommentRequest;
import sw.study.community.dto.PostRequest;
import sw.study.community.service.CommentService;
import sw.study.community.service.PostService;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.community.*;

@Slf4j
@RestController
@RequestMapping("/api/community/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequest postRequest) {
        log.info("게시글 생성 요청: postDTO = {}", postRequest.toString());
        try {
            Long postId = postService.save(postRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(postId + " : 성공적으로 게시글을 만들었습니다.");


        } catch (CategoryNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (AreaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        log.info("게시글 삭제 요청: postId = {}", postId);
        try {
            postService.delete(postId);
            return ResponseEntity.ok("게시글이 정상적으로 삭제되었습니다");


        } catch (PostNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId, @RequestBody Long memberId) {
        log.info("게시글 좋아요 요청: postId = {}, memberId = {}", postId, memberId);
        try{
            postService.addLike(postId, memberId);
            return ResponseEntity.status(HttpStatus.CREATED).body("게시글에 좋아요가 정상적으로 추가되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (DuplicateLikeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> cancelLikePost(@PathVariable Long postId, @RequestBody Long memberId) {
        log.info("게시글 좋아요 취소 요청: postId = {}, memberId = {}", postId, memberId);
        try {
            postService.cancelLike(postId, memberId);
            return ResponseEntity.ok("성공적으로 좋아요를 취소했습니다.");


        } catch (PostNotFoundException | UserNotFoundException | LikeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); // 500
        }
    }

    @PostMapping("/{postId}/report")
    public ResponseEntity<?> reportPost(@PathVariable Long postId, @RequestBody ReportRequest reportRequest) {
        log.info("게시글 신고 요청: targetId = {}, reporterId = {}", postId, reportRequest.getReporterId());
        try {
            postService.report(reportRequest, postId);
            return ResponseEntity.ok("신고가 성공적으로 접수되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<?> createComment(@PathVariable Long postId, @RequestBody CommentRequest commentRequest) {
        log.info("게시글 댓글 요청: postId = {}, memberId = {}", postId, commentRequest.getMemberId());
        try {
            commentService.save(commentRequest, postId);
            return ResponseEntity.status(HttpStatus.CREATED).body("정상적으로 댓글이 생성되었습니다.");
        } catch (PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        log.info("게시글 댓글 삭제 요청: postId = {}, commentId = {}", postId, commentId);
        try {
            deleteComment(postId, commentId);
            return ResponseEntity.ok("정상적으로 댓글이 삭제되었습니다.");
        } catch (PostNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody Long memberId) {
        try {
            commentService.addLike(postId, commentId, memberId);
            return ResponseEntity.ok("성공적으로 좋아요를 달았습니다.");
        } catch (CommentNotFoundException | PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}/comment/{commentId}/like")
    public ResponseEntity<?> cancelLikeComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody Long memberId) {
        try {
            commentService.cancelLike(postId, commentId, memberId);
            return ResponseEntity.ok("성공적으로 좋아요를 취소했습니다.");
        } catch (CommentNotFoundException | PostNotFoundException | UserNotFoundException | LikeNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment /{commentId}/report")
    public ResponseEntity<?> reportComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody ReportRequest reportRequest) {
        log.info("댓글 신고 요청: targetId = {}, reporterId = {}", commentId, reportRequest.getReporterId());
        try {
            commentService.report(reportRequest, postId, commentId);
            return ResponseEntity.ok("신고가 성공적으로 접수되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{postId}/comment/{commentId}/reply")
    public ResponseEntity<?> createReply(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody CommentRequest commentRequest) {
        log.info("대댓글 요청: postId = {}, commentId={}, replierId = {}", postId, commentId,commentRequest.getMemberId());
        try {
            commentService.reply(commentRequest, postId, commentId);
            return ResponseEntity.status(HttpStatus.CREATED).body("정상적으로 대댓글이 생성되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException | CommentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
        } catch (CommentNotBelongToPostException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage()); // 400
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
