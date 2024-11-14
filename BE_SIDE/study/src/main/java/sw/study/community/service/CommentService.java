package sw.study.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sw.study.community.domain.Comment;
import sw.study.community.domain.Post;
import sw.study.community.dto.CommentRequestDTO;
import sw.study.community.repository.CommentRepository;
import sw.study.community.repository.PostRepository;
import sw.study.exception.UserNotFoundException;
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

    /**
     * 댓글 생성
     */
    public Long save(CommentRequestDTO commentRequestDTO, Long postId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member findMember = memberRepository.findById(commentRequestDTO.getMemberId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));


        Comment comment = Comment.createComment(findPost, findMember,
                commentRequestDTO.getContent(), commentRequestDTO.getLevel());
        commentRepository.save(comment);
        log.info("댓글이 성공적으로 반영: commentId={}, postId={}", comment.getId(), postId);
        return comment.getId();
    }
}
