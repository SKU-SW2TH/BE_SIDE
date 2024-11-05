package sw.study.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class BatchScheduler {
    private final MemberRepository memberRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void deleteInactiveMembers() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        // 3개월 이상 지난 탈퇴 요청 회원 조회 및 삭제
        List<Member> membersToDelete = memberRepository.findAllByDeletedAtBefore(threeMonthsAgo);

        for (Member member : membersToDelete) {
            member.onDeleted();
        }

        memberRepository.saveAll(membersToDelete);
    }
}
