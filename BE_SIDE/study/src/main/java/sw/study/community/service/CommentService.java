package sw.study.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sw.study.community.domain.Comment;
import sw.study.community.domain.CommentLike;
import sw.study.community.domain.Post;
import sw.study.community.dto.CommentRequest;
import sw.study.community.repository.CommentLikeRepository;
import sw.study.community.repository.CommentRepository;
import sw.study.community.repository.PostRepository;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.community.CommentNotBelongToPostException;
import sw.study.exception.community.CommentNotFoundException;
import sw.study.exception.community.LikeNotFoundException;
import sw.study.exception.community.PostNotFoundException;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentLikeRepository commentLikeRepository;

    /**
     * 댓글 생성
     */
    public Long save(CommentRequest commentRequest, Long postId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member findMember = memberRepository.findById(commentRequest.getMemberId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        Comment comment = Comment.createComment(findPost, findMember,
                commentRequest.getContent(), commentRequest.getLevel());
        commentRepository.save(comment);
        log.info("댓글이 성공적으로 반영: commentId={}, postId={}", comment.getId(), postId);
        return comment.getId();
    }

    /**
     * 댓글 삭제
     */
    public void delete(Long postId, Long commentId) {
        // 게시글과 댓글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment)) {
            throw new CommentNotBelongToPostException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 댓글 삭제(논리적 삭제)
        comment.deleteComment();
        log.info("댓글이 성공적으로 삭제(논리적): commentId={}, postId={}", comment.getId(), postId);
    }

    /**
     * 댓글 좋아요
     */
    public void addLike(Long postId, Long commentId, Long memberId) {
        // 게시글, 댓글, 좋아요를 누른 사람 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment)) {
            throw new CommentNotBelongToPostException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 댓글 좋아요
        CommentLike commentLike = CommentLike.createCommentLike(comment, member);
        commentLikeRepository.save(commentLike);
        log.info("댓글에 성공적으로 좋아요: commentId={}, postId={}, memberId={}", commentId, postId, memberId);
    }

    /**
     * 댓글 좋아요 취소
     */
    public void cancelLike(Long postId, Long commentId, Long memberId) {
        // 게시글, 댓글, 좋아요를 취소하려는 사람, 해당 좋아요 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));
        CommentLike commentLike = commentLikeRepository.findByCommentAndMember(comment, member)
                .orElseThrow(() -> new LikeNotFoundException("좋아요가 존재하지 않습니다"));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment)) {
            throw new CommentNotBelongToPostException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 좋아요 취소
        comment.deletedCommentLike(commentLike);
        commentLikeRepository.delete(commentLike);
        log.info("댓글에 성공적으로 좋아요 취소: commentId={}, postId={}, memberId={}", commentId, postId, memberId);
    }

    /**
     * 댓글 신고
     */
}
