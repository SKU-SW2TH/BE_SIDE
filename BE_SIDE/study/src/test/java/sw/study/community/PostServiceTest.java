package sw.study.community;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.admin.domain.Report;
import sw.study.admin.dto.ReportRequest;
import sw.study.admin.repository.ReportRepository;
import sw.study.admin.role.ReportReason;
import sw.study.admin.role.ReportTargetType;
import sw.study.community.domain.Comment;
import sw.study.community.domain.CommentLike;
import sw.study.community.domain.Post;
import sw.study.community.dto.CommentRequest;
import sw.study.community.dto.PostRequest;
import sw.study.community.repository.CommentLikeRepository;
import sw.study.community.repository.CommentRepository;
import sw.study.community.repository.PostLikeRepository;
import sw.study.community.repository.PostRepository;
import sw.study.community.service.CommentService;
import sw.study.community.service.PostService;
import sw.study.user.domain.Member;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.repository.AreaRepository;
import sw.study.user.repository.MemberRepository;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.role.Role;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@Transactional
public class PostServiceTest {
    @Autowired PostService postService;
    @Autowired PostRepository postRepository;
    @Autowired NotificationCategoryRepository notificationCategoryRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired EntityManager em;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired AreaRepository areaRepository;
    @Autowired private CommentService commentService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private CommentLikeRepository commentLikeRepository;


    // 추가적으로 예외 상황 테스트도 추가해야한다.


    @Test
    void 게시글_저장() throws Exception {
        // given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);

        // when
        Long postId = postService.save(postRequest);

        // then
        Post post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getId()).isEqualTo(postId);
    }

    @Test
    void 게시글_삭제() throws Exception {
        // given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        Long postId = postService.save(postRequest);
        em.flush();

        // when
        postService.delete(postId);
        em.flush();

        // then
        assertThat(postRepository.findById(postId).get().isDeleted()).isTrue();
    }

    @Test
    void 게시글_좋아요() throws Exception {
        // given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member member2 = createMember("pok@naver.com", "password2", "989898", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        Long postId = postService.save(postRequest);
        em.flush();

        // when
        postService.addLike(postId, member2.getId());

        // then
        int likeCnt = postRepository.findById(postId).get().getLikes().size();
        assertThat(likeCnt).isEqualTo(1);
    }

    @Test
    void 게시글_좋아요_취소() throws Exception {
        // given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member member2 = createMember("pok@naver.com", "password2", "989898", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        Long postId = postService.save(postRequest);
        postService.addLike(postId, member2.getId());
        em.flush();

        // when
        postService.cancelLike(postId, member2.getId());
        em.flush();

        // then
        int likeCnt = postRepository.findById(postId).get().getLikes().size();
        assertThat(likeCnt).isEqualTo(0);
        assertThat(postLikeRepository.findByPostAndMember(postRepository.findById(postId).orElseThrow(), member2)).isEqualTo(Optional.empty());
    }

    @Test
    void 게시글_신고() throws Exception {
        // given
        Member reporter = createMember("pok@naver.com", "password2", "989898", Role.USER);
        Member postOwner = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        PostRequest postRequest = createPostRequest(postOwner.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        Long postId = postService.save(postRequest);

        ReportRequest reportRequest = createReportRequest(reporter.getId(), "보고싶지않은게시글!!ㅡㅡ", ReportReason.INAPPROPRIATE_EXPRESSION, ReportTargetType.POST);

        // when
        Long reportId = postService.report(reportRequest, postId);

        // then
        Report report = reportRepository.findById(reportId).orElseThrow();
        assertThat(report.getReportTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(report.getReportReason()).isEqualTo(ReportReason.INAPPROPRIATE_EXPRESSION);
        assertThat(postRepository.findById(postId).orElseThrow().getReportCount()).isEqualTo(1);
        assertThat(reporter.getReports().size()).isEqualTo(1);
    }

    @Test
    void 댓글_생성() throws Exception {
        // given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member commenter = createMember("pok@naver.com", "password2", "989898", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        Long postId = postService.save(postRequest);
        CommentRequest commentRequest = createCommentRequest(commenter.getId(), 1, "좋은 글 감사합니다");

        // when
        Long commentId = commentService.save(commentRequest, postId);

        // then
        Optional<Comment> findComment = commentRepository.findById(commentId);
        assertThat(findComment.isPresent()).isTrue();
        assertThat(postRepository.findById(postId).orElseThrow().getComments().size()).isEqualTo(1);
    }

    @Test
    void 댓글_삭제() throws Exception {
        //given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member commenter = createMember("pok@naver.com", "password2", "989898", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        Long postId = postService.save(postRequest);
        CommentRequest commentRequest = createCommentRequest(commenter.getId(), 1, "좋은 글 감사합니다");
        Long commentId = commentService.save(commentRequest, postId);

        //when
        commentService.delete(postId, commentId);

        //then
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        assertThat(comment.isDeleted()).isTrue();
    }

    @Test
    void 댓글_좋아요() throws Exception {
        //given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member commenter = createMember("pok@naver.com", "password2", "989898", Role.USER);
        Member liker = createMember("like@naver.com", "asdasd!!!!", "좋아요를누르는사람", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        CommentRequest commentRequest = createCommentRequest(commenter.getId(), 1, "좋은 글 감사합니다");
        Long postId = postService.save(postRequest);
        Long commentId = commentService.save(commentRequest, postId);

        //when
        commentService.addLike(postId, commentId, liker.getId());

        //then
        Comment comment = commentRepository.findById(commentId).get();
        CommentLike commentLike = commentLikeRepository.findByCommentAndMember(comment, liker).get();
        assertThat(comment.getCommentLikes().size()).isEqualTo(1);
    }

    @Test
    void 댓글_좋아요_취소() throws Exception {
        //given
        Member member = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member commenter = createMember("pok@naver.com", "password2", "989898", Role.USER);
        Member liker = createMember("like@naver.com", "asdasd!!!!", "좋아요를누르는사람", Role.USER);
        PostRequest postRequest = createPostRequest(member.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        CommentRequest commentRequest = createCommentRequest(commenter.getId(), 1, "좋은 글 감사합니다");
        Long postId = postService.save(postRequest);
        Long commentId = commentService.save(commentRequest, postId);
        commentService.addLike(postId, commentId, liker.getId());

        //when
        Comment comment = commentRepository.findById(commentId).get();

        //then
        commentService.cancelLike(postId, commentId, liker.getId());
        assertThat(comment.getCommentLikes().size()).isEqualTo(0);
        assertThat(commentLikeRepository.findByCommentAndMember(comment, liker).isEmpty()).isTrue();
    }

    @Test
    void 댓글_신고() throws Exception {
        //given
        Member poster = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member commenter = createMember("pok@naver.com", "password2", "989898", Role.USER);
        Member reporter = createMember("like@naver.com", "asdasd!!!!", "신고하는사람", Role.USER);
        PostRequest postRequest = createPostRequest(poster.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        CommentRequest commentRequest = createCommentRequest(commenter.getId(), 1, "좋은 글 감사합니다");
        
        Long postId = postService.save(postRequest);
        Long commentId = commentService.save(commentRequest, postId);
        
        //when
        ReportRequest reportRequest = 
                createReportRequest(reporter.getId(), "보고싶지않은댓글!!ㅡㅡ", 
                        ReportReason.INAPPROPRIATE_EXPRESSION, ReportTargetType.COMMENT);
        Long reportId = commentService.report(reportRequest, postId, commentId);

        //then
        Comment comment = commentRepository.findById(commentId).get();
        Report report = reportRepository.findById(reportId).orElseThrow();
        assertThat(report.getReportTargetType()).isEqualTo(ReportTargetType.COMMENT);
        assertThat(report.getReportReason()).isEqualTo(ReportReason.INAPPROPRIATE_EXPRESSION);
        assertThat(comment.getReportCount()).isEqualTo(1);
        assertThat(reporter.getReports().size()).isEqualTo(1);
        
    }

    @Test
    void 대댓글_작성() throws Exception {
        //given
        Member poster = createMember("ksh990408@naver.com", "password1", "감자탕", Role.USER);
        Member commenter = createMember("pok@naver.com", "password2", "989898", Role.USER);
        Member replier = createMember("like@naver.com", "asdasd!!!!", "좋아요를누르는사람", Role.USER);
        PostRequest postRequest = createPostRequest(poster.getId(), "반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
        CommentRequest commentRequest = createCommentRequest(commenter.getId(), 1, "좋은 글 감사합니다");

        Long postId = postService.save(postRequest);
        Long commentId = commentService.save(commentRequest, postId);

        //when
        CommentRequest replyRequest = createCommentRequest(replier.getId(), 2, "대댓글 단다");
        commentService.reply(replyRequest, postId, commentId);

        //then
        Comment parent = commentRepository.findById(commentId).orElseThrow();
        Comment reply = parent.getChild().get(0);
        assertThat(parent.getChild().size()).isEqualTo(1);
        assertThat(reply.getParent()).isEqualTo(parent);
    }

//    @Test
//    void 게시글_수정() throws Exception {
//        //given
//        Member member = getMember();
//        PostDTO postDTO = getPostDTO(member.getId());
//        Long postId = postService.save(postDTO);
//
//        //when
//        Post post = postRepository.findById(postId).orElseThrow();
//
//
//        //then
//
//    }
//
//    @Test
//    void 게시글_불러오기() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//
//
//    @Test
//    void 게시글_페이징() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//
//    @Test
//    void 게시글_제목_검색() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//    @Test
//    void 게시글_작성자_검색() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//    @Test
//    void 게시글_관심분야_검색() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }


    // 객체
    private Member createMember(String email, String password, String nickname, Role role) {
        List<NotificationCategory> categories = notificationCategoryRepository.findAll();
        Member member = Member.createMember(email, password, nickname, role, categories);
        memberRepository.save(member);
        return member;
    }

    private PostRequest createPostRequest(Long memberId, String title, String content, String category, List<String> areas, String filePath) {
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle(title);
        postRequest.setContent(content);
        postRequest.setCategory(category);
        postRequest.setMemberId(memberId);

        if (areas != null) {
            postRequest.setArea(new ArrayList<>(areas));
        }

        List<MultipartFile> files = new ArrayList<>();
        if (filePath != null) {
            try (FileInputStream inputStream = new FileInputStream(filePath)) {
                MultipartFile file = new MockMultipartFile("file", filePath.substring(filePath.lastIndexOf('/') + 1), "image/jpeg", inputStream);
                files.add(file);
            } catch (IOException e) {
                throw new UncheckedIOException("파일을 읽을 수 없습니다: " + filePath, e);
            }
        }
        postRequest.setFiles(files);

        return postRequest;
    }

    public CommentRequest createCommentRequest(Long memberId, int level, String content) {
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setLevel(level);
        commentRequest.setMemberId(memberId);
        commentRequest.setContent(content);
        return commentRequest;
    }

    private ReportRequest createReportRequest(Long reporterId, String description, ReportReason reason, ReportTargetType targetType) {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReporterId(reporterId);
        reportRequest.setDescription(description);
        reportRequest.setReportReason(reason);
        reportRequest.setReportTargetType(targetType);
        return reportRequest;
    }

}
