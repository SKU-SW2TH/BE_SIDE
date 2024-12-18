package sw.study.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.admin.domain.Report;
import sw.study.admin.dto.ReportRequest;
import sw.study.admin.role.ReportStatus;
import sw.study.admin.service.ReportService;
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
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ReportService reportService;

    /**
     * 댓글 생성
     */
    @Transactional
    public Long save(CommentRequest commentRequest, Long postId, Long commenterId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member findMember = memberRepository.findById(commenterId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        Comment comment = Comment.createComment(findPost, findMember,
                commentRequest.getContent(), commentRequest.getLevel());
        commentRepository.save(comment);
        log.info("댓글이 성공적으로 반영: commentId={}, postId={}", comment.getId(), postId);
        return comment.getId();
    }

    /**
     * 대댓글 작성
     */
    @Transactional
    public Long reply(CommentRequest replyRequest, Long postId, Long commentId, Long replierId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(replierId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당하는 댓글을 찾을 수 없습니다."));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment)) {
            throw new CommentNotBelongToPostException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        Comment reply = Comment.createReply(comment, member, replyRequest.getContent(), replyRequest.getLevel());
        commentRepository.save(reply);
        log.info("대댓글이 성공적으로 반영: postId={}, commentId={}, replyId={}", postId, comment.getId(), reply.getId());
        return reply.getId();
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void delete(Long postId, Long commentId, Long memberId) {
        // 게시글과 댓글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment)) {
            throw new CommentNotBelongToPostException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 본인이 작성한 댓글인지 확인
        if (!comment.getMember().getId().equals(memberId)) {
            throw new UnauthorizedException("작성자만 삭제할 수 있습니다");
        }

        // 댓글 삭제(논리적 삭제)
        comment.deleteComment();
        log.info("댓글이 성공적으로 삭제(논리적): commentId={}, postId={}", comment.getId(), postId);
    }

    /**
     * 댓글 좋아요
     */
    @Transactional
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
    @Transactional
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
     * 대댓글 좋아요 취소
     */
    @Transactional
    public void cancelReplyLike(Long postId, Long commentId, Long replyId, Long cancelerId) {
        // 게시글, 댓글, 대댓글, 좋아요를 취소하려는 사람 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new CommentNotFoundException("해당 대댓글을 찾을 수 없습니다."));

        Member member = memberRepository.findById(cancelerId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다."));
        CommentLike commentLike = commentLikeRepository.findByCommentAndMember(reply, member)
                .orElseThrow(() -> new LikeNotFoundException("좋아요가 존재하지 않습니다"));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment) || !comment.getChild().contains(reply)) {
            throw new CommentNotBelongToPostException("대댓글이 해당 게시글에 속하지 않거나, 댓글에 속하지 않습니다.");
        }

        // 대댓글 좋아요 취소
        reply.deletedCommentLike(commentLike);
        commentLikeRepository.delete(commentLike);
        log.info("대댓글에 성공적으로 좋아요 취소: replyId={}, commentId={}, postId={}, memberId={}", replyId, commentId, postId, cancelerId);
    }


    /**
     * 댓글 신고
     */
    @Transactional
    public Long report(ReportRequest reportRequest, Long postId, Long commentId, Long reporterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 댓글을 찾을 수 없습니다."));
        Member targetMember = memberRepository.findById(comment.getMember().getId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 피신고자를 찾을 수 없습니다."));
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 신고자를 찾을 수 없습니다."));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment)) {
            throw new CommentNotBelongToPostException("댓글이 해당 게시글에 속하지 않습니다.");
        }

        // 신고 생성
        Report report = Report.createReport(reporter, targetMember, commentId,
                reportRequest.getDescription(),
                reportRequest.getReportTargetType(),
                reportRequest.getReportReason(),
                ReportStatus.PENDING);

        // 댓글 신고 수 증가
        comment.incrementReportCount();
        Long reportId = reportService.save(report);
        log.info("댓글 신고 요청 완료: commentId = {}, reporterId = {}, targetMemberId = {}", commentId, reportId, targetMember.getId());
        return reportId;
    }

    /**
     * 대댓글 삭제
     */
    @Transactional
    public void deleteReply(Long postId, Long commentId, Long replyId, Long memberId) {
        // 게시글, 댓글, 대댓글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new CommentNotFoundException("해당 대댓글을 찾을 수 없습니다."));

        // 댓글이 해당 게시글에 속하는지 검증
        if (!post.getComments().contains(comment) || !comment.getChild().contains(reply)) {
            throw new CommentNotBelongToPostException("대댓글이 해당 게시글에 속하지 않거나, 댓글에 속하지 않습니다.");
        }

        // 본인이 작성한 대댓글인지 확인
        if (!reply.getMember().getId().equals(memberId)) {
            throw new UnauthorizedException("작성자만 삭제할 수 있습니다");
        }

        // 대댓글 삭제(논리적 삭제)
        reply.deleteComment();
        System.out.println("reply.isDeleted() = " + reply.isDeleted());
        log.info("대댓글이 성공적으로 삭제(논리적): replyId={}, commentId={}, postId={}", replyId, commentId, postId);
    }

    /**
     * 대댓글 좋아요
     */
    @Transactional
    public void addReplyLike(Long postId, Long commentId, Long replyId, Long likerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("해당 댓글을 찾을 수 없습니다."));
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new CommentNotFoundException("해당 대댓글을 찾을 수 없습니다."));
        Member member = memberRepository.findById(likerId)
                .orElseThrow(() -> new UserNotFoundException("해당 사용자를 찾을 수 없습니다."));

        if (!post.getComments().contains(comment) || !comment.getChild().contains(reply)) {
            throw new CommentNotBelongToPostException("대댓글이 해당 게시글에 속하지 않거나, 댓글에 속하지 않습니다.");
        }

        // 대댓글 좋아요
        CommentLike commentLike = CommentLike.createCommentLike(reply, member);
        commentLikeRepository.save(commentLike);
        log.info("대댓글에 성공적으로 좋아요: replyId={}, commentId={}, postId={}, memberId={}", replyId, commentId, postId, likerId);
    }

    /**
     * 대댓글 신고
     */
    @Transactional
    public Long reportReply(ReportRequest reportRequest, Long postId, Long commentId, Long replyId, Long reporterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 댓글을 찾을 수 없습니다."));
        Comment reply = commentRepository.findById(replyId)
                .orElseThrow(() -> new CommentNotFoundException("해당 대댓글을 찾을 수 없습니다."));

        Member targetMember = memberRepository.findById(reply.getMember().getId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 피신고자를 찾을 수 없습니다."));
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 신고자를 찾을 수 없습니다."));

        if (!post.getComments().contains(comment) || !comment.getChild().contains(reply)) {
            throw new CommentNotBelongToPostException("대댓글이 해당 게시글에 속하지 않거나, 댓글에 속하지 않습니다.");
        }

        Report report = Report.createReport(reporter, targetMember, replyId,
                reportRequest.getDescription(),
                reportRequest.getReportTargetType(),
                reportRequest.getReportReason(),
                ReportStatus.PENDING);

        // 대댓글 신고 수 증가
        reply.incrementReportCount();
        Long reportId = reportService.save(report);
        log.info("대댓글 신고 요청 완료: replyId = {}, reporterId = {}, targetMemberId = {}", replyId, reportId, targetMember.getId());
        return reportId;
    }
}
