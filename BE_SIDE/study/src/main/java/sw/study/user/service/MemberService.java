package sw.study.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.community.service.S3Service;
import sw.study.config.jwt.JWTService;
import sw.study.config.jwt.TokenProvider;
import sw.study.exception.*;
import sw.study.exception.email.DuplicateEmailException;
import sw.study.exception.email.EmailNotFoundException;
import sw.study.exception.email.VerificationCodeGenerationException;
import sw.study.user.domain.*;
import sw.study.user.dto.*;
import sw.study.user.repository.*;
import sw.study.user.role.Role;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
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
    private final AreaRepository areaRepository;
    private final MemberAreaRepository memberAreaRepository;
    private final NotificationRepository notificationRepository;
    private final S3Service s3Service;
    private final JWTService jwtService;

    @Transactional
    public Long join(JoinDto joinDto) {
        try {
            List<NotificationCategory> categories = notificationCategoryRepository.findAll();

            // Member 생성 중 예외 발생 가능성
            Member member = Member.createMember(
                    joinDto.getEmail(), encoder.encode(joinDto.getPassword()),
                    joinDto.getNickname(), Role.USER, categories
            );

            // Member 저장 중 예외 발생 가능성
            return memberRepository.save(member).getId();

        } catch (DataAccessException e) {
            // 데이터베이스 관련 예외 처리
            throw new MemberCreationException("회원 가입 중 데이터베이스 오류가 발생했습니다.", e);
        } catch (Exception e) {
            // 기타 예상치 못한 예외 처리
            throw new MemberCreationException("회원 가입 중 오류가 발생했습니다.", e);
        }
    }

    public void verifyNickname(NicknameDto nicknameDto)  {
        Optional<Member> findMember = memberRepository.findByNickname(nicknameDto.getNickname());
        if (findMember.isPresent()) throw new DuplicateNicknameException(findMember.get().getNickname());
    }

    public void verifyEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isPresent()) throw new DuplicateEmailException("이미 사용 중인 이메일입니다.");
    }

    public void checkEmail(String email) {
        Optional<Member> findMember = memberRepository.findByEmail(email);
        if (findMember.isEmpty()) throw new EmailNotFoundException("가입되어 있지 않은 이메일 입니다.");
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
            throw new VerificationCodeGenerationException("인증 코드 생성 중 오류가 발생했습니다.", e);
        }
    }

    public MemberDto getMemberByToken(String token) {
        // 토큰 유효성 검사
        token = jwtService.extractToken(token);

        if (!tokenProvider.validateToken(token)) {
            throw new InvalidTokenException("유효하지 않은 토큰입니다.");
        }

        String email = jwtService.extractEmail(token);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // MemberDto 생성
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail(member.getEmail());
        memberDto.setNickname(member.getNickname());
        memberDto.setProfile(member.getProfile());
        memberDto.setIntroduce(member.getIntroduce());
        memberDto.setRole(member.getRole().toString());
        memberDto.setDeleted(memberDto.isDeleted());

        if (member.getDeletedAt() != null) {
            memberDto.setDeletedAt(LocalDate.from(member.getDeletedAt()));
        }

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
        List<MemberAreaDTO> interestDtos = member.getMemberAreas().stream()
                .map(interest -> {
                    MemberAreaDTO dto = new MemberAreaDTO();
                    dto.setId(interest.getId());
                    dto.setInterestId(interest.getArea().getId());
                    dto.setName(interest.getArea().getAreaName());
                    return dto;
                })
                .collect(Collectors.toList());

        // DTO 설정
        memberDto.setSettings(dtos);
        memberDto.setInterests(interestDtos);

        // 이메일로 사용자 조회
        return memberDto;
    }

    @Transactional
    public UpdateProfileResponse updateMemberProfile(String accessToken, UpdateProfileRequest updateProfileRequest, MultipartFile profilePicture) throws IOException{
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

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
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 비밀번호 공백 제거
        String trimmedOldPassword = oldPassword.trim();
        String trimmedNewPassword = newPassword.trim();

        // 비밀번호 일치 확인
        if (!encoder.matches(trimmedOldPassword, member.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호와 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사
        if (!isValidPassword(trimmedNewPassword)) {
            throw new InvalidPasswordException("비밀번호 유효성 검사에 실패했습니다.");
        }

        String encodedNewPassword = encoder.encode(trimmedNewPassword);
        member.changePassword(encodedNewPassword);
        memberRepository.save(member);
    }

    @Transactional
    public void updateNotification(SettingRequest dto) {
        NotificationSetting setting = notificationSettingRepository.findById(dto.getSettingId())
                .orElseThrow(() -> new EntityNotFoundException("ID를 찾지 못했습니다."));

        setting.setEnabled(dto.isEnabled());
        notificationSettingRepository.save(setting); // 또는 flush()를 사용할 수 있음
    }

    public List<AreaDTO> getInterestAreas() {
        List<Area> areas = areaRepository.findAll();
        List<AreaDTO> interestAreasDTO = new ArrayList<>();

        for (Area area : areas) {
            AreaDTO areaDTO = new AreaDTO();
            areaDTO.setId(area.getId());
            areaDTO.setAreaName(area.getAreaName());
            areaDTO.setLevel(area.getLevel());

            // Check if parent is null and set parentId accordingly
            if (area.getParent() != null) {
                areaDTO.setParentId(area.getParent().getId());
            } else {
                areaDTO.setParentId(0L); // Set to 0 if parent is null
            }

            interestAreasDTO.add(areaDTO); // Add the DTO to the list
        }

        return interestAreasDTO;
    }

    @Transactional
    public List<MemberAreaDTO> initInterest(String accessToken, AreaRequest areaRequest) {
        List<MemberAreaDTO> dtos = new ArrayList<>();

        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        List<Long> areaRequestIds = areaRequest.getIds();
        if (areaRequestIds == null) {
            areaRequestIds = new ArrayList<>(); // null 체크 및 초기화
        }

        // 관심 항목 추가
        for (Long areaId : areaRequestIds) {
            Area area = areaRepository.findById(areaId)
                    .orElseThrow(() -> new InterestNotFoundException("관심 분야를 찾지 못했습니다."));

            MemberArea newInterest = MemberArea.CreateMemberArea(area);
            member.addMemberArea(newInterest);
            memberAreaRepository.save(newInterest);
        }

        // 업데이트된 관심 분야 DTO 생성
        List<MemberArea> updateAreas = memberAreaRepository.findByMemberId(member.getId());
        for (MemberArea memberArea : updateAreas) {
            MemberAreaDTO dto = new MemberAreaDTO();
            dto.setId(memberArea.getId());
            dto.setInterestId(memberArea.getArea().getId());
            dto.setName(memberArea.getArea().getAreaName());
            dtos.add(dto);
        }

        return dtos;
    }

    @Transactional
    public List<MemberAreaDTO> updateInterest(String accessToken, AreaRequest areaRequest) {
        List<MemberAreaDTO> dtos = new ArrayList<>();

        // 토큰에서 이메일 추출
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 요청에서 관심사 ID 목록 가져오기 (null 방지)
        List<Long> interestIds = Optional.ofNullable(areaRequest.getIds()).orElse(new ArrayList<>());

        // 기존의 관심사를 조회하여 ID 목록으로 변환
        List<MemberArea> existingInterests = memberAreaRepository.findByMemberId(member.getId());
        Set<Long> existingInterestIds = existingInterests.stream()
                .map(memberArea -> memberArea.getArea().getId())
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
            Area area = areaRepository.findById(interestId)
                    .orElseThrow(() -> new InterestNotFoundException("관심 분야를 찾지 못했습니다."));
            MemberArea newInterest = MemberArea.CreateMemberArea(area);
            member.addMemberArea(newInterest);
            memberAreaRepository.save(newInterest);
        }

        // 관심사 삭제
        for (Long interestId : interestsToRemove) {
            MemberArea existingInterest = memberAreaRepository.findByMemberIdAndAreaId(member.getId(), interestId)
                    .orElseThrow(() -> new InterestNotFoundException("관심 분야를 찾지 못했습니다."));
            member.removeInterest(existingInterest);
            memberAreaRepository.delete(existingInterest);
        }

        // 업데이트된 관심사 목록 DTO 생성
        List<MemberArea> updatedInterests = memberAreaRepository.findByMemberId(member.getId());
        for (MemberArea interest : updatedInterests) {
            MemberAreaDTO dto = new MemberAreaDTO();
            dto.setId(interest.getId());
            dto.setInterestId(interest.getArea().getId());
            dto.setName(interest.getArea().getAreaName());
            dtos.add(dto);
        }

        return dtos;
    }

    @Transactional
    public void updateNotificationRead(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token); // 토큰에서 이메일 추출
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 읽지 않은 알림 리스트 가져오기
        List<Notification> notifications = notificationRepository.findByMemberAndIsReadFalse(member);

        // 읽음 상태로 변경하고 저장하기
        for (Notification notification : notifications) {
            notification.markAsRead(); // 읽음 처리
        }

        // 변경된 알림들을 데이터베이스에 저장
        notificationRepository.saveAll(notifications);
    }

    public List<NotificationDTO> getNotifications(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

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

        return notificationDTOS;
    }

    public List<NotificationDTO> unReadNotification(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

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
