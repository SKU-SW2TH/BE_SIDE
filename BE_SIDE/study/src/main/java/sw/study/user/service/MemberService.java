package sw.study.user.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.config.jwt.TokenProvider;
import sw.study.exception.DuplicateNicknameException;
import sw.study.exception.InvalidTokenException;
import sw.study.exception.UserNotFoundException;
import sw.study.user.domain.Member;
import sw.study.user.dto.MemberDto;
import sw.study.user.dto.UpdateProfileRequest;
import sw.study.user.repository.MemberRepository;
import sw.study.user.util.RedisUtil;

import java.io.File;
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
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final TokenProvider tokenProvider;
    // 파일 저장 경로 (서버에서 실제 파일이 저장되는 위치)
    private final String uploadDirectory = "src/main/resources/profile";

    @Transactional
    public Long join(MemberDto memberDto) {

        Member member = Member.createMember(
                memberDto.getEmail(), encoder.encode(memberDto.getPassword()),
                memberDto.getNickname()
        );

        return memberRepository.save(member).getId();
    }

    public boolean verifyNickname(MemberDto memberDto) {
        Optional<Member> findMember = memberRepository.findByNickname(memberDto.getNickname());
        return findMember.isEmpty();
    }

    public boolean verifyEmail(MemberDto memberDto) {
        Optional<Member> findMember = memberRepository.findByEmail(memberDto.getEmail());
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

    private String extractEmail(String token) {
        Claims claims = tokenProvider.parseClaims(token);
        String email = claims.getSubject();
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

}
