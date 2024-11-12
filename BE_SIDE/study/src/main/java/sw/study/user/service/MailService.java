package sw.study.user.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.exception.email.EmailSendException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender emailSender;

    public void sendEmail(String toEmail,
                          String title,
                          String text) {
        try {
            SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);
            emailSender.send(emailForm);
        } catch (RuntimeException e) {
            log.debug("MailService.sendEmail exception occur toEmail: {}, " +
                    "title: {}, text: {}", toEmail, title, text);
            throw new EmailSendException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }

    public void sendEmailWithLink(String toEmail, String title, String url) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(title);

            // HTML 형식의 이메일 본문 생성
            String text = "<p>비밀번호 변경을 위해 아래 링크를 클릭하세요:</p>" +
                    "<p><a href='" + url + "'>비밀번호 변경하기</a></p>";

            helper.setText(text, true); // true로 설정하여 HTML 본문을 사용

            emailSender.send(message);
        } catch (Exception e) {
            log.debug("MailService.sendEmailWithLink exception occur toEmail: {}, title: {}, url: {}", toEmail, title, url);
            throw new EmailSendException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }

    // 발신할 이메일 데이터 세팅
    private SimpleMailMessage createEmailForm(String toEmail, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);
        return message;
    }
}