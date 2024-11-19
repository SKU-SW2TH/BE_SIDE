package sw.study.community;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.admin.domain.Report;
import sw.study.admin.dto.ReportRequestDTO;
import sw.study.admin.repository.ReportRepository;
import sw.study.admin.role.ReportReason;
import sw.study.admin.role.ReportTargetType;
import sw.study.community.domain.Comment;
import sw.study.community.domain.Post;
import sw.study.community.dto.CommentRequestDTO;
import sw.study.community.dto.PostRequestDTO;
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
import java.io.FileNotFoundException;
import java.io.IOException;
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
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;


    // 추가적으로 예외 상황 테스트도 추가해야한다.


    @Test
    void 게시글_저장() throws Exception {
        //given
        Member member = getMember();
        PostRequestDTO postRequestDTO = getPostDTO(member.getId());

        //when
        Long postId = postService.save(postRequestDTO);

        //then
        Post post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getId()).isEqualTo(postId);
    }

    @Test
    void 게시글_삭제() throws Exception {
        //given
        Member member = getMember();
        PostRequestDTO postRequestDTO = getPostDTO(member.getId());
        Long postId = postService.save(postRequestDTO);
        em.flush();

        //when
        postService.delete(postId);
        em.flush();

        //then
        assertThat(postRepository.findById(postId).get().isDeleted()).isTrue();
    }

    @Test
    void 게시글_좋아요() throws Exception {
        //given
        Member member = getMember();
        PostRequestDTO postRequestDTO = getPostDTO(member.getId());
        Long postId = postService.save(postRequestDTO);

        Member member2 = getMember2();
        em.flush();

        //when
        postService.addLike(postId, member2.getId());

        //then
        int likeCnt = postRepository.findById(postId).get().getLikes().size();
        assertThat(likeCnt).isEqualTo(1);
    }

    @Test
    void 게시글_좋아요_취소() throws Exception {
        //given
        Member member = getMember();
        Member member2 = getMember2();
        PostRequestDTO postRequestDTO = getPostDTO(member.getId());
        Long postId = postService.save(postRequestDTO);
        Post post = postRepository.findById(postId).orElseThrow();

        postService.addLike(postId, member2.getId());
        em.flush();

        //when
        postService.cancelLike(postId, member2.getId());
        em.flush();

        //then
        int likeCnt = postRepository.findById(postId).get().getLikes().size();
        assertThat(likeCnt).isEqualTo(0);
        assertThat(postLikeRepository.findByPostAndMember(post, member2)).isEqualTo(Optional.empty());
    }

    @Test
    void 게시글_신고() throws Exception {
        //given
        Member member = getMember();
        PostRequestDTO postRequestDTO = getPostDTO(member.getId());
        Member member2 = getMember2();
        Long postId = postService.save(postRequestDTO);
        Post post = postRepository.findById(postId).orElseThrow();

        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setReporterId(member2.getId());
        reportRequestDTO.setDescription("보고싶지않은게시글!!ㅡㅡ");
        reportRequestDTO.setReportReason(ReportReason.INAPPROPRIATE_EXPRESSION);
        reportRequestDTO.setReportTargetType(ReportTargetType.POST);

        //when
        Long reportId = postService.report(reportRequestDTO, postId);

        //then
        Report report = reportRepository.findById(reportId).get();


        assertThat(post.getReportCount()).isEqualTo(1);
        assertThat(member2.getReports().size()).isEqualTo(1);
        assertThat(report.getReportTargetType()).isEqualTo(ReportTargetType.POST);
        assertThat(report.getReportReason()).isEqualTo(ReportReason.INAPPROPRIATE_EXPRESSION);
    }

    @Test
    void 댓글_생성() throws Exception {
        //given
        Member member = getMember();
        PostRequestDTO postRequestDTO = getPostDTO(member.getId());
        Member member2 = getMember2();
        Long postId = postService.save(postRequestDTO);

        CommentRequestDTO commentRequestDTO = getCommentRequestDTO(member2.getId());

        //when
        Long commentId = commentService.save(commentRequestDTO, postId);

        //then
        Optional<Comment> findComment = commentRepository.findById(commentId);
        Post findPost = postRepository.findById(postId).orElseThrow();
        assertThat(findComment.isPresent()).isTrue();
        assertThat(findPost.getComments().size()).isEqualTo(1);
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


    private Member getMember() {
        List<NotificationCategory> categories = notificationCategoryRepository.findAll();
        Member member = Member.createMember("ksh990408@naver.com", "ksks12", "감자탕",
                Role.USER, categories);
        memberRepository.save(member);
        return member;
    }

    private Member getMember2() {
        List<NotificationCategory> categories = notificationCategoryRepository.findAll();
        Member member = Member.createMember("pok@naver.com", "pokpok", "989898",
                Role.USER, categories);
        memberRepository.save(member);
        return member;
    }

    private PostRequestDTO getPostDTO(Long memberId) {

        PostRequestDTO postRequestDTO = new PostRequestDTO();
        postRequestDTO.setTitle("반갑습니다");
        postRequestDTO.setContent("안녕하세요 으아아아");
        postRequestDTO.setCategory("FREE");
        postRequestDTO.setMemberId(memberId);

        List<String> areas = new ArrayList<>();
        areas.add("Java");
        postRequestDTO.setArea(areas);

        List<MultipartFile> files = new ArrayList<>();
        String filePath = "/home/kim/Desktop/sk2th/BE_SIDE/BE_SIDE/study/src/test/java/sw/study/file/nicedochi.jpg";

        // 파일 경로로 MultipartFile 생성
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            MultipartFile file = new MockMultipartFile("file", "nicedochi.jpg", "image/jpeg", inputStream);
            files.add(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        postRequestDTO.setFiles(files);

        return postRequestDTO;
    }

    public CommentRequestDTO getCommentRequestDTO(Long memberId) {
        CommentRequestDTO commentRequestDTO = new CommentRequestDTO();
        commentRequestDTO.setLevel(1);
        commentRequestDTO.setMemberId(memberId);
        commentRequestDTO.setContent("좋은 글 감사합니다");
        return commentRequestDTO;
    }


}
