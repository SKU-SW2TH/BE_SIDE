package sw.study;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sw.study.community.domain.Category;
import sw.study.user.domain.Member;
import sw.study.user.domain.Notification;
import sw.study.user.domain.NotificationCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sw.study.user.domain.InterestArea;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.role.Role;
import sw.study.user.service.NotificationService;

@Component
@RequiredArgsConstructor
public class InitDb {
    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.initInterestArea();
        initService.initCategory();
        initService.initNotificationCategory();
        initService.initMember();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final NotificationCategoryRepository notificationCategoryRepository;
        private final EntityManager em;
        private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        private List<NotificationCategory> categories = new ArrayList<>();

        public void initInterestArea() {

            InterestArea interest1 = InterestArea.createInterest(1, "개발/프로그래밍");
            InterestArea interest2 = InterestArea.createInterest(2, "백엔드");
            InterestArea interest3 = InterestArea.createInterest(2, "프론트");
            InterestArea interest4 = InterestArea.createInterest(2, "프로그래밍 언어");
            InterestArea interest5 = InterestArea.createInterest(3, "Spring-Boot");
            InterestArea interest6 = InterestArea.createInterest(3, "React");
            InterestArea interest7 = InterestArea.createInterest(3, "Java");

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
            em.persist(member1);
        }
    }
}
