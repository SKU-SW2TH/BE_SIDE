package sw.study.community;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.community.domain.Post;
import sw.study.community.dto.PostDTO;
import sw.study.community.repository.PostRepository;
import sw.study.community.service.PostService;
import sw.study.user.domain.InterestArea;
import sw.study.user.domain.Member;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.repository.InterestAreaRepository;
import sw.study.user.repository.MemberRepository;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.role.Role;
import sw.study.user.service.MemberService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class PostServiceTest {
    @Autowired PostService postService;
    @Autowired PostRepository postRepository;
    @Autowired NotificationCategoryRepository notificationCategoryRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired InterestAreaRepository interestAreaRepository;


    // 추가적으로 예외 상황 테스트도 추가해야한다.


    @Test
    @Rollback(false)
    void 게시글_저장() throws Exception {
        //given
        Member member = getMember();
        PostDTO postDTO = getPostDTO(member.getId());

        //when
        Long postId = postService.save(postDTO);

        //then
        Post post = postRepository.findById(postId).orElseThrow();
        assertThat(post.getId()).isEqualTo(postId);
    }

//    @Test
//    void 게시글_수정() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//    @Test
//    void 게시글_삭제() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//    @Test
//    void 게시글_좋아요() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//    @Test
//    void 게시글_좋아요_취소() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
//
//    @Test
//    void 게시글_신고() throws Exception {
//        //given
//
//        //when
//
//        //then
//
//    }
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
        Member member = Member.createMember("ksh990409@naver.com", "ksks12", "감자탕",
                Role.USER, categories);
        memberRepository.save(member);
        return member;
    }

    private PostDTO getPostDTO(Long memberId) {

        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("반갑습니다");
        postDTO.setContent("안녕하세요 으아아아");
        postDTO.setCategory("FREE");
        postDTO.setMemberId(memberId);

        List<String> interestAreas = new ArrayList<>();
        interestAreas.add("Java");
        postDTO.setInterests(interestAreas);

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

        postDTO.setFiles(files);

        return postDTO;
    }
}
