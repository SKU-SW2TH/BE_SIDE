package sw.study;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.admin.dto.ReportRequest;
import sw.study.admin.role.ReportReason;
import sw.study.admin.role.ReportTargetType;
import sw.study.community.domain.Category;
import sw.study.community.dto.CommentRequest;
import sw.study.community.dto.PostRequest;
import sw.study.community.repository.CommentRepository;
import sw.study.community.repository.PostRepository;
import sw.study.community.service.CommentService;
import sw.study.community.service.PostService;
import sw.study.user.domain.Area;
import sw.study.user.domain.Member;
import sw.study.user.domain.Notification;
import sw.study.user.domain.NotificationCategory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import sw.study.user.repository.MemberRepository;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.repository.NotificationRepository;
import sw.study.user.role.Role;
import sw.study.user.service.AuthService;
import sw.study.user.service.MemberService;

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
        initService.initNotification();
        initService.initPost();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final PostService postService;
        private final CommentService commentService;
        private final NotificationCategoryRepository notificationCategoryRepository;
        private final AuthService authService;
        private final MemberService memberService;
        private final EntityManager em;
        private final MemberRepository memberRepository;
        private final NotificationRepository notificationRepository;
        private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        private List<NotificationCategory> categories = new ArrayList<>();

        public void initInterestArea() {

            Area interest1 = Area.createInterest(1, "개발/프로그래밍");
            Area interest3 = Area.createInterest(2, "프론트");
            Area interest2 = Area.createInterest(2, "백엔드");
            Area interest4 = Area.createInterest(2, "프로그래밍 언어");
            Area interest6 = Area.createInterest(3, "React");
            Area interest24 = Area.createInterest(3, "Angular");
            Area interest25 = Area.createInterest(3, "Vue.js");
            Area interest26 = Area.createInterest(3, "Svelte");
            Area interest27 = Area.createInterest(3, "jQuery");
            Area interest28 = Area.createInterest(3, "Backbone.js");
            Area interest29 = Area.createInterest(3, "Preact");
            Area interest30 = Area.createInterest(3, "Ember.js");
            Area interest5 = Area.createInterest(3, "Spring-Boot");
            Area interest17 = Area.createInterest(3, "Node.js");
            Area interest18 = Area.createInterest(3, "Spring");
            Area interest19 = Area.createInterest(3, "Django");
            Area interest20 = Area.createInterest(3, "Flask");
            Area interest21 = Area.createInterest(3, "Laravel");
            Area interest22 = Area.createInterest(3, "Ruby on Rails");
            Area interest23 = Area.createInterest(3, "CakePHP");
            Area interest7 = Area.createInterest(3, "Java");
            Area interest8 = Area.createInterest(3, "Python");
            Area interest9 = Area.createInterest(3, "C");
            Area interest10 = Area.createInterest(3, "C++");
            Area interest11 = Area.createInterest(3, "Ruby");
            Area interest12 = Area.createInterest(3, "JavaScript");
            Area interest13 = Area.createInterest(3, "Go");
            Area interest14 = Area.createInterest(3, "PHP");
            Area interest15 = Area.createInterest(3, "Kotlin");
            Area interest16 = Area.createInterest(3, "Swift");



            // 영속성 컨텍스트에 추가하므로써 이후 자식부모 설정부분을 제대로 관리할 수 있게함
            em.persist(interest1);
            em.persist(interest3);
            em.persist(interest2);
            em.persist(interest4);
            em.persist(interest6);
            em.persist(interest24);
            em.persist(interest25);
            em.persist(interest26);
            em.persist(interest27);
            em.persist(interest28);
            em.persist(interest29);
            em.persist(interest30);
            em.persist(interest5);
            em.persist(interest17);
            em.persist(interest18);
            em.persist(interest19);
            em.persist(interest20);
            em.persist(interest21);
            em.persist(interest22);
            em.persist(interest23);
            em.persist(interest7);
            em.persist(interest8);
            em.persist(interest9);
            em.persist(interest10);
            em.persist(interest11);
            em.persist(interest12);
            em.persist(interest13);
            em.persist(interest14);
            em.persist(interest15);
            em.persist(interest16);
//           em.flush(); //필요는 없음

            interest1.addChildInterest(interest3);
            interest1.addChildInterest(interest2);
            interest1.addChildInterest(interest4);
            interest3.addChildInterest(interest6);
            interest3.addChildInterest(interest24);
            interest3.addChildInterest(interest25);
            interest3.addChildInterest(interest26);
            interest3.addChildInterest(interest27);
            interest3.addChildInterest(interest28);
            interest3.addChildInterest(interest29);
            interest3.addChildInterest(interest30);
            interest2.addChildInterest(interest5);
            interest2.addChildInterest(interest17);
            interest2.addChildInterest(interest18);
            interest2.addChildInterest(interest19);
            interest2.addChildInterest(interest20);
            interest2.addChildInterest(interest21);
            interest2.addChildInterest(interest22);
            interest2.addChildInterest(interest23);
            interest4.addChildInterest(interest7);
            interest4.addChildInterest(interest8);
            interest4.addChildInterest(interest9);
            interest4.addChildInterest(interest10);
            interest4.addChildInterest(interest11);
            interest4.addChildInterest(interest12);
            interest4.addChildInterest(interest13);
            interest4.addChildInterest(interest14);
            interest4.addChildInterest(interest15);
            interest4.addChildInterest(interest16);
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

        public void initNotification(){
            List<NotificationCategory> notificationCategories = notificationCategoryRepository.findAll();

            Member member = Member.createMember(
                    "limjh0703@naver.com",
                    encoder.encode("1q2w3e4r!"), // 비밀번호 암호화
                    "user", Role.USER, notificationCategories
            );
            memberRepository.save(member);

            for(int i = 1; i < 21; i++){
                String content = "내용" + i;
                Long id = Long.valueOf(i);
                Notification notification = Notification.createNotification(notificationCategories.get(0), content, id);
                notification.addMember(member);
                notificationRepository.save(notification);
            }

            Member member2 = memberRepository.findByEmail("bj10111@naver.com").orElseThrow();

            for(int i = 1; i < 22; i++){
                String content = "내용" + i;
                Long id = Long.valueOf(i);
                Notification notification = Notification.createNotification(notificationCategories.get(0), content, id);
                notification.addMember(member2);
                notificationRepository.save(notification);
            }

        }

        public void initPost() {
            Member poster = createMember("poster11@naver.com", encoder.encode("password1"), "게시글쓴사람", Role.USER);
            Member commenter1 = createMember("commenter1@naver.com", encoder.encode("password2"), "댓글쓴사람", Role.USER);
            Member commenter2 = createMember("commenter2@naver.com", encoder.encode("password3"), "2번째댓글쓴사람", Role.USER);
            Member replier = createMember("replier@naver.com", encoder.encode("1111"), "1번째 댓글에 대댓글쓰는사람", Role.USER);
            Member liker1 = createMember("like1@naver.com", "asdasd!!!!", "좋아요를누르는사람", Role.USER);
            Member liker2 = createMember("like2@naver.com", "asdasd!!!!", "좋아요를누르는사람2", Role.USER);
            Member liker3 = createMember("like3@naver.com", "asdasd!!!!", "좋아요를누르는사람3", Role.USER);
            Member liker4 = createMember("like4@naver.com", "asdasd!!!!", "좋아요를누르는사람4", Role.USER);

            PostRequest postRequest = createPostRequest("반갑습니다", "안녕하세요 으아아아", "FREE", List.of("Java"), null);
            CommentRequest commentRequest1 = createCommentRequest(1, "좋은 글 감사합니다");
            CommentRequest commentRequest2 = createCommentRequest(1, "나는 두번째 댓글임");
            CommentRequest replyRequest = createCommentRequest(2, "대댓글 단다");


            Long postId = postService.save(postRequest, poster.getId());
            Long commentId = commentService.save(commentRequest1, postId, commenter1.getId());
            Long commentId2 = commentService.save(commentRequest2, postId, commenter2.getId());
            Long replyId = commentService.reply(replyRequest, postId, commentId, replier.getId());

            // 게시글의 좋아요 수는 1개다.
            postService.addLike(postId, commentId);

            // 댓글의 좋아요 수는 2개다.
            commentService.addLike(postId, commentId, liker1.getId());
            commentService.addLike(postId, commentId, liker2.getId());


            // 대댓글의 좋아요 수는 4개다.
            commentService.addReplyLike(postId, commentId, replyId, liker1.getId());
            commentService.addReplyLike(postId, commentId, replyId, liker2.getId());
            commentService.addReplyLike(postId, commentId, replyId, liker3.getId());
            commentService.addReplyLike(postId, commentId, replyId, liker4.getId());
        }

        // 객체
        private Member createMember(String email, String password, String nickname, Role role) {
            List<NotificationCategory> categories = notificationCategoryRepository.findAll();
            Member member = Member.createMember(email, password, nickname, role, categories);
            memberRepository.save(member);
            return member;
        }

        private PostRequest createPostRequest(String title, String content, String category, List<String> areas, String filePath) {
            PostRequest postRequest = new PostRequest();
            postRequest.setTitle(title);
            postRequest.setContent(content);
            postRequest.setCategory(category);

            if (areas != null) {
                postRequest.setArea(new ArrayList<>(areas));
            }

            List<MultipartFile> files = new ArrayList<>();
            postRequest.setFiles(files);

            return postRequest;
        }

        public CommentRequest createCommentRequest(int level, String content) {
            CommentRequest commentRequest = new CommentRequest();
            commentRequest.setLevel(level);
            commentRequest.setContent(content);
            return commentRequest;
        }

        private ReportRequest createReportRequest(String description, ReportReason reason, ReportTargetType targetType) {
            ReportRequest reportRequest = new ReportRequest();
            reportRequest.setDescription(description);
            reportRequest.setReportReason(reason);
            reportRequest.setReportTargetType(targetType);
            return reportRequest;
        }
    }
}
