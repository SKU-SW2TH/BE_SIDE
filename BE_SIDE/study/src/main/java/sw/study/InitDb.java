package sw.study;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.community.domain.Category;
import sw.study.community.domain.Post;
import sw.study.community.dto.PostDTO;
import sw.study.community.repository.PostRepository;
import sw.study.community.service.PostService;
import sw.study.user.domain.Area;
import sw.study.user.domain.Member;
import sw.study.user.domain.Notification;
import sw.study.user.domain.NotificationCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sw.study.user.repository.MemberRepository;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.role.Role;
import sw.study.user.service.NotificationService;

@Component
@RequiredArgsConstructor
public class InitDb {
    private final InitService initService;
    private final PostRepository postRepository;

    @PostConstruct
    public void init() {
        initService.initInterestArea();
        initService.initCategory();
        initService.initNotificationCategory();
        initService.initMember();
        initService.initPost();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final PostService postService;
        private final NotificationCategoryRepository notificationCategoryRepository;
        private final EntityManager em;
        private final MemberRepository memberRepository;
        private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        private List<NotificationCategory> categories = new ArrayList<>();

        public void initInterestArea() {

            Area interest1 = Area.createInterest(1, "개발/프로그래밍");
            Area interest2 = Area.createInterest(2, "백엔드");
            Area interest3 = Area.createInterest(2, "프론트");
            Area interest4 = Area.createInterest(2, "프로그래밍 언어");
            Area interest5 = Area.createInterest(3, "Spring-Boot");
            Area interest6 = Area.createInterest(3, "React");
            Area interest7 = Area.createInterest(3, "Java");

            // 영속성 컨텍스트에 추가하므로써 이후 자식부모 설정부분을 제대로 관리할 수 있게함
            em.persist(interest1);
            em.persist(interest2);
            em.persist(interest3);
            em.persist(interest4);
            em.persist(interest5);
            em.persist(interest6);
            em.persist(interest7);
//           em.flush(); //필요는 없음

            interest1.addChildInterest(interest2);
            interest1.addChildInterest(interest3);
            interest1.addChildInterest(interest4);
            interest2.addChildInterest(interest5);
            interest3.addChildInterest(interest6);
            interest4.addChildInterest(interest7);
        }


        public void initCategory() {

            Category category1 = Category.createCategory("FREE");
            em.persist(category1);

            Category category2 = Category.createCategory("QUESTION");
            em.persist(category2);
        }

        public void initNotificationCategory() {
            NotificationCategory category1 = NotificationCategory.createNotificationCategory("게시판");
            NotificationCategory category2 = NotificationCategory.createNotificationCategory("스터디");
            em.persist(category1);
            em.persist(category2);

            categories.add(category1);
            categories.add(category2);
        }

        public void initMember(){

            List<NotificationCategory> notificationCategories = notificationCategoryRepository.findAll();

            Member member1 = Member.createMember(
                    "bj10111@naver.com",
                    encoder.encode("jong631012@"), // 비밀번호 암호화
                    "park", Role.USER, notificationCategories
            );
            memberRepository.save(member1);
//            em.persist(member1);
        }

        public void initPost() {
            List<NotificationCategory> notificationCategories = notificationCategoryRepository.findAll();

            Member member = Member.createMember(
                    "ksh990409@naver.com",
                    encoder.encode("poket1357!"), // 비밀번호 암호화
                    "testUser", Role.USER, notificationCategories
            );
            em.persist(member);

            // postDTO
            PostDTO postDTO = new PostDTO();
            postDTO.setTitle("반갑습니다");
            postDTO.setContent("안녕하세요 으아아아");
            postDTO.setCategory("FREE");
            postDTO.setMemberId(member.getId());

            List<String> interestAreas = new ArrayList<>();
            interestAreas.add("Java");
            postDTO.setArea(interestAreas);

            List<MultipartFile> files = new ArrayList<>();
            postDTO.setFiles(files);

            Long postId = postService.save(postDTO);
        }
    }
}
