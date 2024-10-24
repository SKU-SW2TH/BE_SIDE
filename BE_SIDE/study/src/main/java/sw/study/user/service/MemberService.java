package sw.study.user.service;

import io.jsonwebtoken.Claims;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.config.jwt.TokenProvider;
import sw.study.exception.DuplicateNicknameException;
import sw.study.exception.InvalidPasswordException;
import sw.study.exception.InvalidTokenException;
import sw.study.exception.UserNotFoundException;
import sw.study.user.domain.Member;
import sw.study.user.dto.JoinDto;
import sw.study.user.domain.NotificationCategory;
import sw.study.user.domain.NotificationSetting;
import sw.study.user.dto.MemberDto;
import sw.study.user.dto.NotificationSettingDTO;
import sw.study.user.dto.UpdateProfileRequest;
import sw.study.user.repository.MemberRepository;
import sw.study.user.repository.NotificationCategoryRepository;
import sw.study.user.repository.NotificationSettingRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder encoder;
    private final TokenProvider tokenProvider;
    private final NotificationSettingRepository notificationSettingRepository;
    private final NotificationCategoryRepository notificationCategoryRepository;
    // 파일 저장 경로 (서버에서 실제 파일이 저장되는 위치)
    private final String uploadDirectory = "src/main/resources/profile";

    @Transactional
    public Long join(JoinDto joinDto) {

        Member member = Member.createMember(
                joinDto.getEmail(), encoder.encode(joinDto.getPassword()),
                joinDto.getNickname()
        );

        return memberRepository.save(member).getId();
    }

    public boolean verifyNickname(JoinDto joinDto) {
        Optional<Member> findMember = memberRepository.findByNickname(joinDto.getNickname());
        return findMember.isEmpty();
    }

    public boolean verifyEmail(JoinDto joinDto) {
        Optional<Member> findMember = memberRepository.findByEmail(joinDto.getEmail());
        return findMember.isEmpty();
    }

    public String createCode() {
        int length = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("MemberService.createCode() exception occur");
            throw new IllegalStateException("Secure random algorithm not found", e);
        }
    }

    public Member getMemberByToken(String token) {
        // 토큰 유효성 검사
        if (!tokenProvider.validateToken(token)) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        String email = extractEmail(token);

        // 이메일로 사용자 조회
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    public Member updateMemberProfile(String accessToken, UpdateProfileRequest updateProfileRequest) throws IOException{
        String email = extractEmail(accessToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (updateProfileRequest.getNickname() != null && !updateProfileRequest.getNickname().isEmpty()) {
            checkNicknameDuplication(updateProfileRequest.getNickname());
            member.updateNickname(updateProfileRequest.getNickname());
        }

        // 프로필 사진 업데이트
        if (updateProfileRequest.getProfilePicture() != null && !updateProfileRequest.getProfilePicture().isEmpty()) {
            String profilePicturePath = saveProfilePicture(updateProfileRequest.getProfilePicture());
            member.updateProfilePicture(profilePicturePath);
        }

        // 자기소개 업데이트
        if (updateProfileRequest.getIntroduction() != null && !updateProfileRequest.getIntroduction().isEmpty()) {
            member.updateIntroduction(updateProfileRequest.getIntroduction());
        }

        memberRepository.save(member);

        return member;
    }

    @Transactional
    public void changePassword(String accessToken, String oldPassword, String newPassword) {
        String email = extractEmail(accessToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // 비밀번호 공백 제거
        String trimmedOldPassword = oldPassword.trim();
        String trimmedNewPassword = newPassword.trim();

        // 비밀번호 일치 확인
        if (!encoder.matches(trimmedOldPassword, member.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect.");
        }

        // 새 비밀번호 유효성 검사
        if (isValidPassword(trimmedNewPassword)) {
            String encodedNewPassword = encoder.encode(trimmedNewPassword);
            member.changePassword(encodedNewPassword);
            memberRepository.save(member);
        } else {
            throw new InvalidPasswordException("Password does not meet the requirements.");
        }
    }

    public void updateNotification(String accessToken, NotificationSettingDTO dto) {
        String email = extractEmail(accessToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        NotificationCategory category = notificationCategoryRepository.findById(dto.getCategoryId()).orElseThrow(
                () -> new UserNotFoundException("Category not found with id: " + dto.getCategoryId()));

        NotificationSetting setting = notificationSettingRepository
                .findByMemberAndCategory(member, category)
                .orElseThrow(() -> new EntityNotFoundException("NotificationSetting not found for email: " + email + " and categoryId: " + dto.getCategoryId()));

        setting.setEnabled(dto.isEnabled());
        notificationSettingRepository.save(setting);
    }

    private String extractEmail(String token) {
        Claims claims = tokenProvider.parseClaims(token);
        String email = claims.getSubject();
        System.out.println(email);
        return email;
    }

    private void checkNicknameDuplication(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);
        if (exists) {
            throw new DuplicateNicknameException("Nickname already in use: " + nickname);
        }
    }

    private String saveProfilePicture(MultipartFile file) throws IOException {
        // 파일 이름을 고유하게 만들기 위해 UUID를 사용
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // 저장할 전체 파일 경로 생성
        Path uploadPath = Paths.get(uploadDirectory);
        Path filePath = uploadPath.resolve(uniqueFileName);

        // 디렉토리가 없으면 생성
        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장 (파일이 존재하면 덮어쓰기)
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 파일 URL 생성 (웹에서 접근 가능한 경로)
        String fileUrl = "http://yourdomain.com/api/member/profile" + uniqueFileName;

        // 파일 URL 반환
        return fileUrl;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && // 최소 길이
                password.matches(".*\\d.*") && // 숫자 포함 여부
                password.matches(".*[!@#$%^&*()].*"); // 특수문자 포함 여부
    }

}
