package sw.study;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sw.study.community.domain.Category;
import sw.study.community.domain.InterestArea;
import sw.study.community.domain.Member;
import sw.study.user.domain.NotificationCategory;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitDb {
    private final InitService initService;

    @PostConstruct
    public void init() {
        initService.initInterestArea();
        initService.initCategory();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        public void initInterestArea() {

            InterestArea interest1 = InterestArea.createInterest(1, "웹 개발");
            InterestArea interest2 = InterestArea.createInterest(2, "백엔드");
            InterestArea interest3 = InterestArea.createInterest(2, "프론트");

            // 영속성 컨텍스트에 추가하므로써 이후 자식부모 설정부분을 제대로 관리할 수 있게함
            em.persist(interest1);
            em.persist(interest2);
            em.persist(interest3);
//           em.flush(); //필요는 없음

            List<InterestArea> interestAreas = new ArrayList<>();
            List<NotificationCategory> categories = new ArrayList<>();

            Member member1 = Member.createMember(
                    "limjh0703@naver.com",
                    encoder.encode("1q2w3e4r!"), // 비밀번호 암호화
                    "User One",
                    "profile1.jpg",
                    "안녕하세요, 저는 User One입니다.",
                    interestAreas,
                    categories
            );
            em.persist(member1);

            interest1.addChildInterest(interest2);
            interest1.addChildInterest(interest3);
        }


        public void initCategory() {

            Category category1 = Category.createCategory("FREE");
            em.persist(category1);

            Category category2 = Category.createCategory("QUESTION");
            em.persist(category2);
        }
    }
}
