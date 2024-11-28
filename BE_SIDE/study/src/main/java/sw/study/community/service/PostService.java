package sw.study.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.admin.domain.Report;
import sw.study.admin.dto.ReportRequest;
import sw.study.admin.role.ReportStatus;
import sw.study.admin.service.ReportService;
import sw.study.community.domain.Category;
import sw.study.community.domain.Post;
import sw.study.community.domain.PostLike;
import sw.study.community.dto.PostRequest;
import sw.study.community.repository.CategoryRepository;
import sw.study.community.repository.PostLikeRepository;
import sw.study.community.repository.PostRepository;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.community.*;
import sw.study.exception.community.AreaNotFoundException;
import sw.study.exception.community.CategoryNotFoundException;
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.user.domain.Area;
import sw.study.user.domain.Member;
import sw.study.user.repository.AreaRepository;
import sw.study.user.repository.MemberRepository;
import sw.study.user.service.MemberService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final AreaRepository areaRepository;
    private final S3Service s3Service;
    private final PostLikeRepository postLikeRepository;
    private final ReportService reportService;
    private final MemberService memberService;

    /**
     * 게시글 생성
     */
    @Transactional
    public Long save(PostRequest postRequest, Long memberId) {
        Category category = categoryRepository.findByName(postRequest.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException("해당하는 카테고리가 존재하지 않습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자가 존재하지 않습니다."));

        List<Area> areas = new ArrayList<>();
        for (String areaName : postRequest.getArea()) {
            areas.add(areaRepository.findByAreaName(areaName)
                    .orElseThrow(() -> new AreaNotFoundException("해당하는 분야가 존재하지 않습니다.")));
        }

        List<String> urls = new ArrayList<>();
        if (!postRequest.getFiles().isEmpty()) {
            for (MultipartFile file : postRequest.getFiles()) {
                String url = s3Service.upload(file, "post/");
                urls.add(url);
            }
        }


        Post post = Post.createPost(postRequest.getTitle(), postRequest.getContent(), category, member, areas, urls);
        log.info("게시글 생성 완료: postId = {}", post.getId());
        return postRepository.save(post).getId();
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void delete(Long postId, Long memberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));

        // 작성자가 아닐 경우
        if(!post.getMember().getId().equals(memberId)) {
            throw new UnauthorizedException("작성자만 삭제할 수 있습니다");
        }

        post.deletePost();
        log.info("게시글 삭제 완료: postId = {}", postId);
    }

    /**
     * 게시글 좋아요
     */
    @Transactional
    public void addLike(Long postId, Long likerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member liker = memberRepository.findById(likerId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        // 중복 좋아요 확인
        boolean alreadyLiked = postLikeRepository.existsByPostAndMember(post, liker);
        if (alreadyLiked) {
            throw new DuplicateLikeException("이미 좋아요를 눌렀습니다.");
        }

        PostLike postLike = PostLike.createPostLike(post, liker);
        postLikeRepository.save(postLike);
        log.info("게시글 좋아요 요청 완료: postId = {}, memberId = {}", postId, likerId);
    }

    /**
     * 게시글 좋아요 취소
     */
    @Transactional
    public void cancelLike(Long postId, Long cancelerId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member canceler = memberRepository.findById(cancelerId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 사용자를 찾을 수 없습니다."));

        PostLike postLike = postLikeRepository.findByPostAndMember(post, canceler)
                .orElseThrow(() -> new LikeNotFoundException("좋아요가 존재하지 않습니다."));

        // 좋아요 취소
        post.deleteLike(postLike); // Post의 likes 리스트에서 제거 (list에서 삭제하는 것은 수동으로 해야함)
        postLikeRepository.deleteById(postLike.getId());
        log.info("게시글 좋아요 취소 요청 완료: postId = {}, memberId = {}", postId, cancelerId);
    }

    /**
     * 게시글 신고
     */
    @Transactional
    public Long report(ReportRequest reportRequest, Long postId, Long reporterId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당하는 게시글을 찾을 수 없습니다."));
        Member findTargetMember = memberRepository.findById(findPost.getMember().getId())
                .orElseThrow(() -> new UserNotFoundException("해당하는 피신고자를 찾을 수 없습니다."));
        Member findReporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new UserNotFoundException("해당하는 신고자를 찾을 수 없습니다."));

        // 신고 생성
        Report report = Report.createReport(findReporter, findTargetMember, postId,
                reportRequest.getDescription(),
                reportRequest.getReportTargetType(),
                reportRequest.getReportReason(),
                ReportStatus.PENDING);

        // 게시글 신고 수 증가
        findPost.incrementReportCount();
        Long reportId = reportService.save(report);
        log.info("게시글 신고 요청 완료: targetId = {}, reporterId = {}, targetMemberId = {}", postId, reportId, findTargetMember.getId());
        return reportId;
    }
}
