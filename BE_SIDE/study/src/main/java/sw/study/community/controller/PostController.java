package sw.study.community.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.admin.dto.ReportRequestDTO;
import sw.study.community.dto.PostDTO;
import sw.study.community.service.PostService;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.community.*;

@Slf4j
@RestController
@RequestMapping("/api/community/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostDTO postDTO) {
        log.info("게시글 생성 요청: postDTO = {}", postDTO.toString());
        try {
            Long postId = postService.save(postDTO);
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
    public ResponseEntity<?> reportPost(@PathVariable Long postId, @RequestBody ReportRequestDTO reportRequestDTO) {
        log.info("게시글 신고 요청: targetId = {}, reporterId = {}", postId, reportRequestDTO.getReporterId());
        try {
            postService.report(reportRequestDTO, postId);
            return ResponseEntity.ok("신고가 성공적으로 접수되었습니다.");


        } catch (PostNotFoundException | UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // 기타 예외 발생
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
