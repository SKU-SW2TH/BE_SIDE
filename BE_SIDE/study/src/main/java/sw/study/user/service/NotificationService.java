package sw.study.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.user.domain.Member;
import sw.study.user.domain.Notification;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.domain.NotificationSetting;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.repository.NotificationRepository;
import sw.study.user.repository.NotificationSettingRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationCategoryRepository notificationCategoryRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public void sendNotification(Member member, String title, String content, String name, Long targetId) {
        if (member == null || title == null || content == null || targetId == null) {
            throw new IllegalArgumentException("파라미터가 null입니다.");
        }

        NotificationCategory category = notificationCategoryRepository.findByCategoryName(name)
                .orElseThrow(() -> new NoSuchElementException("카테고리를 찾지 못했습니다."));

        Optional<NotificationSetting> notificationSetting = notificationSettingRepository.findByMemberAndCategory(member, category);

        if (notificationSetting.isPresent() && notificationSetting.get().isEnabled()) {
            Notification notification = Notification.createNotification(member, category, title, content, targetId);
            notificationRepository.save(notification);
        }
    }

}
