package sw.study.auth;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.TokenDTO;
import sw.study.exception.InvalidCredentialsException;
import sw.study.exception.SamePasswordException;
import sw.study.user.domain.Member;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.dto.LoginRequest;
import sw.study.user.repository.MemberRepository;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.role.Role;
import sw.study.user.service.AuthService;
import sw.study.user.util.RedisUtil;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthServiceTest {
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;
    @Autowired
    NotificationCategoryRepository notificationCategoryRepository;

    @Autowired
    private AuthService authService; // 테스트 대상 서비스

    @Autowired
    private RedisUtil redisUtil;

    static String email = "test@examplie.com";
    static String password = "1q2w3e4r!";

    @BeforeEach
    void setUp() {
        // 필요한 초기 설정을 수행합니다. (예: 테스트 데이터 추가)
        List<NotificationCategory> categories = notificationCategoryRepository.findAll();
        memberRepository.save(Member.createMember(email, encoder.encode(password), "Tester", Role.USER, categories));
    }

    @AfterEach
    void tearDown() {
        // Redis의 모든 데이터를 삭제
        redisUtil.flushAll();
    }

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Act
        TokenDTO tokenDTO = authService.login(loginRequest);

        // Assert
        assertNotNull(tokenDTO);
        assertNotNull(tokenDTO.getAccessToken());
        assertNotNull(tokenDTO.getRefreshToken());
    }

    @Test
    void testLogin_Failure_InvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("wrong@example.com", "wrongpassword");

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogout_Success() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(email, password);
        TokenDTO tokenDTO = authService.login(loginRequest);

        // Act
        authService.logout(tokenDTO.getRefreshToken());

        // Assert
        String refreshTokenKey = "RT:"+email;
        String refreshToken = redisUtil.getData(refreshTokenKey);
        assertNull(refreshToken); // Redis에서 Refresh Token이 삭제되었는지 확인
    }

    @Test
    void testChangePassword_Success() {
        // Arrange
        String newPassword = "1q2w3e4r@";
        String accessToken = "Bearer " + authService.login(new LoginRequest(email, password)).getAccessToken();

        // Act
        authService.changePassword(accessToken, newPassword);

        // Assert
        Member updatedMember = memberRepository.findByEmail(email).orElseThrow();
        assertTrue(encoder.matches(newPassword, updatedMember.getPassword()));
    }

    @Test
    void testChangePassword_Failure_SamePassword() {
        // Arrange
        String newPassword = "1q2w3e4r!";
        String accessToken = "Bearer " + authService.login(new LoginRequest(email, password)).getAccessToken();

        // Act
        assertThrows(SamePasswordException.class, () -> authService.changePassword(accessToken, newPassword));
    }

    @Test
    void testDeleteMember_Success() {
        LoginRequest loginRequest = new LoginRequest(email, password);
        TokenDTO tokenDTO = authService.login(loginRequest);
        String refreshToken = "Bearer " + tokenDTO.getRefreshToken();

        authService.deleteMember(refreshToken);

        Member member = memberRepository.findByEmail(email).orElseThrow();
        assertNotNull(member.getDeletedAt());
    }

    @Test
    void testRestoreMember_Success() {
        LoginRequest loginRequest = new LoginRequest(email, password);
        TokenDTO tokenDTO = authService.login(loginRequest);
        String accessToken = "Bearer " + tokenDTO.getAccessToken();

        authService.restoreMember(accessToken);

        Member member = memberRepository.findByEmail(email).orElseThrow();
        assertNull(member.getDeletedAt());
    }

}
