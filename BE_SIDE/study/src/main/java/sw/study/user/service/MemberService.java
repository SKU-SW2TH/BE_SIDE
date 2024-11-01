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
import sw.study.community.service.S3Service;
import sw.study.config.Constant;
import sw.study.config.jwt.JWTService;
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
    private final MemberInterestRepository memberInterestRepository;
    private final NotificationRepository notificationRepository;
    private final S3Service s3Service;
    private final JWTService jwtService;

    @Transactional
    public Long join(JoinDto joinDto) {
        List<NotificationCategory> categories = notificationCategoryRepository.findAll();

        Member member = Member.createMember(
                joinDto.getEmail(), encoder.encode(joinDto.getPassword()),
                joinDto.getNickname(), Role.USER, categories
        );

        return memberRepository.save(member).getId();
    }

    public void verifyNickname(NicknameDto nicknameDto)  {
        Optional<Member> findMember = memberRepository.findByNickname(nicknameDto.getNickname());
        if (findMember.isPresent()) throw new DuplicateNicknameException(findMember.get().getNickname());
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

    public MemberDto getMemberByToken(String token) {
        // 토큰 유효성 검사
        if (!tokenProvider.validateToken(token)) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        String email = jwtService.extractEmail(token);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // MemberDto 생성
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail(member.getEmail());
        memberDto.setNickname(member.getNickname());
        memberDto.setProfile(member.getProfile());
        memberDto.setIntroduce(member.getIntroduce());
        memberDto.setRole(member.getRole().toString());

        // 알림 설정 DTO 변환
        List<NotificationSettingDTO> dtos = member.getSettings().stream()
                .map(s -> {
                    NotificationSettingDTO dto = new NotificationSettingDTO();
                    NotificationCategoryDTO categoryDTO = new NotificationCategoryDTO();
                    categoryDTO.setId(s.getCategory().getId());
                    categoryDTO.setName(s.getCategory().getCategoryName());

                    dto.setSettingId(s.getId());
                    dto.setEnabled(s.isEnabled());
                    dto.setCategoryDTO(categoryDTO);
                    return dto;
                })
                .collect(Collectors.toList());

        // 관심 분야 DTO 변환
        List<MemberInterestDTO> interestDtos = member.getInterests().stream()
                .map(interest -> {
                    MemberInterestDTO dto = new MemberInterestDTO();
                    dto.setId(interest.getId());
                    dto.setInterestId(interest.getInterestArea().getId());
                    dto.setName(interest.getInterestArea().getAreaName());
                    return dto;
                })
                .collect(Collectors.toList());

        List<NotificationDTO> notificationDTOS = member.getNotifications().stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed()) // createdAt 기준 내림차순 정렬
                .map(notification -> {
                    NotificationDTO dto = new NotificationDTO();
                    dto.setId(notification.getId());
                    dto.setTitle(notification.getTitle());
                    dto.setContent(notification.getContent());
                    dto.setRead(notification.isRead());
                    dto.setName(notification.getCategory().getCategoryName());
                    dto.setCreatedAt(notification.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());

        // DTO 설정
        memberDto.setSettings(dtos);
        memberDto.setInterests(interestDtos);
        memberDto.setNotifications(notificationDTOS);

        // 이메일로 사용자 조회
        return memberDto;
    }

    @Transactional
    public UpdateProfileResponse updateMemberProfile(String accessToken, UpdateProfileRequest updateProfileRequest, MultipartFile profilePicture) throws IOException{
        String email = jwtService.extractEmail(accessToken);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (updateProfileRequest.getNickname() != null && !updateProfileRequest.getNickname().isEmpty() && !member.getNickname().equals(updateProfileRequest.getNickname())) {
            checkNicknameDuplication(updateProfileRequest.getNickname());
            member.updateNickname(updateProfileRequest.getNickname());
        }

        // 프로필 사진 업데이트
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePictureUrl = s3Service.upload(profilePicture, "profile/");
            member.updateProfilePicture(profilePictureUrl);
        }

        // 자기소개 업데이트
        if (updateProfileRequest.getIntroduction() != null && !updateProfileRequest.getIntroduction().isEmpty() && !member.getIntroduce().equals(updateProfileRequest.getIntroduction())) {
            member.updateIntroduction(updateProfileRequest.getIntroduction());
        }

        memberRepository.save(member);

        return  new UpdateProfileResponse(member.getNickname(), member.getIntroduce(), member.getProfile());
    }

    @Transactional
    public void changePassword(String accessToken, String oldPassword, String newPassword) {
        String email = jwtService.extractEmail(accessToken);
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

        String email = jwtService.extractEmail(token);
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
        String email = jwtService.extractEmail(token);
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

    @Transactional
    public void updateNotificationRead(String token) {
        String email = jwtService.extractEmail(token); // 토큰에서 이메일 추출
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // 읽지 않은 알림 리스트 가져오기
        List<Notification> notifications = notificationRepository.findByMemberAndIsReadFalse(member);

        // 읽음 상태로 변경하고 저장하기
        for (Notification notification : notifications) {
            notification.markAsRead(); // 읽음 처리
        }

        // 변경된 알림들을 데이터베이스에 저장
        notificationRepository.saveAll(notifications);
    }

    public List<NotificationDTO> getNotifications(String token) {
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        List<Notification> notifications = notificationRepository.findByMember(member);
        List<NotificationDTO> dtos = new ArrayList<>();

        for (Notification notification : notifications) {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(notification.getId());
            dto.setTitle(notification.getTitle());
            dto.setContent(notification.getContent());
            dto.setName(notification.getCategory().getCategoryName());
            dto.setRead(notification.isRead());
            dto.setCreatedAt(notification.getCreatedAt());
            dtos.add(dto);
        }

        return dtos;
    }

    public List<NotificationDTO> unReadNotification(String token) {
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        List<Notification> notifications = notificationRepository.findByMemberAndIsReadFalse(member);
        List<NotificationDTO> dtos = new ArrayList<>();

        for (Notification notification : notifications) {
            NotificationDTO dto = new NotificationDTO();
            dto.setId(notification.getId());
            dto.setTitle(notification.getTitle());
            dto.setContent(notification.getContent());
            dto.setName(notification.getCategory().getCategoryName());
            dto.setRead(notification.isRead());
            dto.setCreatedAt(notification.getCreatedAt());
            dtos.add(dto);
        }

        return dtos;
    }

    private void checkNicknameDuplication(String nickname) {
        boolean exists = memberRepository.existsByNickname(nickname);
        if (exists) {
            throw new DuplicateNicknameException("이미 존재하는 닉네임: " + nickname);
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && // 최소 길이
                password.matches(".*\\d.*") && // 숫자 포함 여부
                password.matches(".*[!@#$%^&*()].*"); // 특수문자 포함 여부
    }

}
