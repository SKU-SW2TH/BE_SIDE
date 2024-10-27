package sw.study.user.service;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.config.Constant;
import sw.study.config.jwt.TokenProvider;
import sw.study.exception.*;
import sw.study.user.domain.*;
import sw.study.user.dto.*;
import sw.study.user.repository.*;
import sw.study.user.role.Role;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

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
    private final InterestAreaRepository interestAreaRepository;
    private final MemberInterestRepository interestRepository;
    private final String uploadDirectory = "BE_SIDE/study/src/main/resources/profile"; // 파일 저장 경로 (서버에서 실제 파일이 저장되는 위치)
    private final MemberInterestRepository memberInterestRepository;

    @Transactional
    public Long join(JoinDto joinDto) {
        List<NotificationCategory> categories = notificationCategoryRepository.findAll();

        Member member = Member.createMember(
                joinDto.getEmail(), encoder.encode(joinDto.getPassword()),
                joinDto.getNickname(), Role.USER, categories
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

    @Transactional
    public Member updateMemberProfile(String accessToken, UpdateProfileRequest updateProfileRequest, MultipartFile profilePicture) throws IOException{
        String email = extractEmail(accessToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (updateProfileRequest.getNickname() != null && !updateProfileRequest.getNickname().isEmpty() && !member.getNickname().equals(updateProfileRequest.getNickname())) {
            checkNicknameDuplication(updateProfileRequest.getNickname());
            member.updateNickname(updateProfileRequest.getNickname());
        }

        // 프로필 사진 업데이트
        if (profilePicture != null) {
            // 빈 파일인지 확인
            if (!profilePicture.isEmpty()) {
                String profilePicturePath = saveProfilePicture(profilePicture);
                member.updateProfilePicture(profilePicturePath);
            }
            // 빈 파일일 경우, 그냥 건너뛰기
        }

        // 자기소개 업데이트
        if (updateProfileRequest.getIntroduction() != null && !updateProfileRequest.getIntroduction().isEmpty() && !member.getIntroduce().equals(updateProfileRequest.getIntroduction())) {
            member.updateIntroduction(updateProfileRequest.getIntroduction());
        }

        memberRepository.save(member);
        return member;
    }

    @Transactional
    public Member updateMemberProfileWithoutPicture(String accessToken, UpdateProfileRequest updateProfileRequest) throws IOException{
        String email = extractEmail(accessToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (updateProfileRequest.getNickname() != null && !updateProfileRequest.getNickname().isEmpty() && !member.getNickname().equals(updateProfileRequest.getNickname())) {
            checkNicknameDuplication(updateProfileRequest.getNickname());
            member.updateNickname(updateProfileRequest.getNickname());
        }

        // 자기소개 업데이트
        if (updateProfileRequest.getIntroduction() != null && !updateProfileRequest.getIntroduction().isEmpty() && !member.getIntroduce().equals(updateProfileRequest.getIntroduction())) {
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

    @Transactional
    public void updateNotification(SettingRequest dto) {
        NotificationSetting setting = notificationSettingRepository.findById(dto.getSettingId())
                .orElseThrow(() -> new EntityNotFoundException("NotificationSetting with ID " + dto.getSettingId() + " not found"));

        setting.setEnabled(dto.isEnabled());
        notificationSettingRepository.save(setting); // 또는 flush()를 사용할 수 있음
    }

    public List<InterestAreaDTO> getInterestAreas() {
        List<InterestArea> interestAreas = interestAreaRepository.findAll();
        List<InterestAreaDTO> interestAreasDTO = new ArrayList<>();

        for (InterestArea interestArea : interestAreas) {
            InterestAreaDTO interestAreaDTO = new InterestAreaDTO();
            interestAreaDTO.setId(interestArea.getId());
            interestAreaDTO.setAreaName(interestArea.getAreaName());
            interestAreaDTO.setLevel(interestArea.getLevel());

            // Check if parent is null and set parentId accordingly
            if (interestArea.getParent() != null) {
                interestAreaDTO.setParentId(interestArea.getParent().getId());
            } else {
                interestAreaDTO.setParentId(0L); // Set to 0 if parent is null
            }

            interestAreasDTO.add(interestAreaDTO); // Add the DTO to the list
        }

        return interestAreasDTO;
    }

    @Transactional
    public List<MemberInterestDTO> initInterest(String token, InterestRequest interestRequest) {
        List<MemberInterestDTO> dtos = new ArrayList<>();

        String email = extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        List<Long> interestIds = interestRequest.getIds();
        if (interestIds == null) {
            interestIds = new ArrayList<>(); // null 체크 및 초기화
        }

        // 관심 항목 추가
        for (Long interestId : interestIds) {
            InterestArea interestArea = interestAreaRepository.findById(interestId)
                    .orElseThrow(() -> new InterestNotFoundException("Interest not found with ID: " + interestId));

            MemberInterest newInterest = MemberInterest.CreateMemberInterest(member, interestArea);
            memberInterestRepository.save(newInterest);
        }

        // 업데이트된 관심 분야 DTO 생성
        List<MemberInterest> updateInterests = memberInterestRepository.findByMemberId(member.getId());
        for (MemberInterest interest : updateInterests) {
            MemberInterestDTO dto = new MemberInterestDTO();
            dto.setId(interest.getId());
            dto.setInterestId(interest.getInterestArea().getId());
            dto.setName(interest.getInterestArea().getAreaName());
            dtos.add(dto);
        }

        return dtos;
    }

    @Transactional
    public List<MemberInterestDTO> updateInterest(String token, InterestRequest interestRequest) {
        List<MemberInterestDTO> dtos = new ArrayList<>();

        // 토큰에서 이메일 추출
        String email = extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // 요청에서 관심사 ID 목록 가져오기 (null 방지)
        List<Long> interestIds = Optional.ofNullable(interestRequest.getIds()).orElse(new ArrayList<>());

        // 기존의 관심사를 조회하여 ID 목록으로 변환
        List<MemberInterest> existingInterests = memberInterestRepository.findByMemberId(member.getId());
        Set<Long> existingInterestIds = existingInterests.stream()
                .map(memberInterest -> memberInterest.getInterestArea().getId())
                .collect(Collectors.toSet());

        // 추가할 관심사: 새로운 요청의 ID 중 기존에 없는 ID들
        Set<Long> interestsToAdd = interestIds.stream()
                .filter(id -> !existingInterestIds.contains(id))
                .collect(Collectors.toSet());

        // 삭제할 관심사: 기존 관심사 중 새로운 요청에 없는 ID들
        Set<Long> interestsToRemove = existingInterestIds.stream()
                .filter(id -> !interestIds.contains(id))
                .collect(Collectors.toSet());

        // 관심사 추가
        for (Long interestId : interestsToAdd) {
            InterestArea interestArea = interestAreaRepository.findById(interestId)
                    .orElseThrow(() -> new InterestNotFoundException("Interest not found with ID: " + interestId));
            MemberInterest newInterest = MemberInterest.CreateMemberInterest(member, interestArea);
            memberInterestRepository.save(newInterest);
        }

        // 관심사 삭제
        for (Long interestId : interestsToRemove) {
            MemberInterest existingInterest = memberInterestRepository.findByMemberIdAndInterestAreaId(member.getId(), interestId)
                    .orElseThrow(() -> new InterestNotFoundException("Interest not found with ID: " + interestId));
            member.removeInterest(existingInterest);
            memberInterestRepository.delete(existingInterest);
        }

        // 업데이트된 관심사 목록 DTO 생성
        List<MemberInterest> updatedInterests = memberInterestRepository.findByMemberId(member.getId());
        for (MemberInterest interest : updatedInterests) {
            MemberInterestDTO dto = new MemberInterestDTO();
            dto.setId(interest.getId());
            dto.setInterestId(interest.getInterestArea().getId());
            dto.setName(interest.getInterestArea().getAreaName());
            dtos.add(dto);
        }

        return dtos;
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
        String fileUrl =  Constant.URL + "/api/member/profile/" + uniqueFileName;

        // 파일 URL 반환
        return fileUrl;
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && // 최소 길이
                password.matches(".*\\d.*") && // 숫자 포함 여부
                password.matches(".*[!@#$%^&*()].*"); // 특수문자 포함 여부
    }

}
