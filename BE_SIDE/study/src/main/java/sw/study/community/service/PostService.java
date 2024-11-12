package sw.study.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.admin.domain.Report;
import sw.study.admin.dto.ReportRequestDTO;
import sw.study.admin.role.ReportStatus;
import sw.study.admin.service.ReportService;
import sw.study.community.domain.Category;
import sw.study.community.domain.Post;
import sw.study.community.domain.PostLike;
import sw.study.community.dto.PostDTO;
import sw.study.community.repository.CategoryRepository;
import sw.study.community.repository.PostLikeRepository;
import sw.study.community.repository.PostRepository;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.community.*;
import sw.study.user.domain.InterestArea;
import sw.study.user.domain.Member;
import sw.study.user.repository.InterestAreaRepository;
import sw.study.user.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final InterestAreaRepository interestAreaRepository;
    private final S3Service s3Service;
    private final PostLikeRepository postLikeRepository;
    private final ReportService reportService;

    /**
     * 게시글 생성
     */
    @Transactional
    public Long save(PostDTO postDto) {
        Category category = categoryRepository.findByName(postDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("해당하는 카테고리가 존재하지 않습니다."));
        Member member = memberRepository.findById(postDto.getMemberId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자가 존재하지 않습니다."));

        List<InterestArea> interestAreas = new ArrayList<>();
        for (String areaName : postDto.getInterests()) {
            interestAreas.add(interestAreaRepository.findByAreaName(areaName)
                    .orElseThrow(() -> new AreaNotFoundException("해당하는 분야가 존재하지 않습니다.")));
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : postDto.getFiles()) {
            String url = s3Service.upload(file, "post/");
            urls.add(url);
        }

        Post post = Post.createPost(postDto.getTitle(), postDto.getContent(), category, member, interestAreas, urls);
        log.info("게시글 생성 완료: postId = {}", post.getId());
        return postRepository.save(post).getId();
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void delete(Long postId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        findPost.deletePost();
        log.info("게시글 삭제 완료: postId = {}", postId);
    }

    /**
     * 게시글 좋아요
     */
    @Transactional
    public void addLike(Long postId, Long memberId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        // 중복 좋아요 확인
        boolean alreadyLiked = postLikeRepository.existsByPostAndMember(findPost, findMember);
        if (alreadyLiked) {
            throw new DuplicateLikeException("이미 좋아요를 눌렀습니다.");
        }

        PostLike postLike = PostLike.createPostLike(findPost, findMember);
        postLikeRepository.save(postLike);
        log.info("게시글 좋아요 요청 완료: postId = {}, memberId = {}", postId, memberId);
    }
    /**
     * 게시글 좋아요 취소
     */
    @Transactional
    public void cancelLike(Long postId, Long memberId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        PostLike postLike = postLikeRepository.findByPostAndMember(findPost, findMember)
                .orElseThrow(() -> new LikeNotFoundException("좋아요가 존재하지 않습니다."));

        // Post의 likes 리스트에서 제거 (list에서 삭제하는 것은 수동으로 해야함)
        findPost.removeLike(postLike);

        // 좋아요 취소
        postLikeRepository.deleteById(postLike.getId());
        log.info("게시글 좋아요 취소 요청 완료: postId = {}, memberId = {}", postId, memberId);
    }

    /**
     * 게시글 신고
     */
    @Transactional
    public Long report(ReportRequestDTO reportRequestDTO, Long postId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member findTargetMember = memberRepository.findById(findPost.getMember().getId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 피신고자를 찾을 수 없습니다."));
        Member findReporter = memberRepository.findById(reportRequestDTO.getReporterId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 신고자를 찾을 수 없습니다."));

        // 신고 생성
        Report report = Report.createReport(findReporter, findTargetMember, postId,
                reportRequestDTO.getDescription(),
                reportRequestDTO.getReportTargetType(),
                reportRequestDTO.getReportReason(),
                ReportStatus.PENDING);

        // 게시글 신고 수 증가
        findPost.incrementReportCount();

        return reportService.save(report);
    }
}
