package sw.study.user.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sw.study.exception.*;
import sw.study.user.domain.Member;
import sw.study.user.dto.*;
import sw.study.user.service.MemberService;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/info")
    public ResponseEntity<?> getMemberInfo(@RequestHeader("Authorization") String accessToken) {
        try {
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken; // "Bearer " 이후 부분 추출

            // 토큰 검증 및 사용자 정보 조회 로직
            Member member = memberService.getMemberByToken(token);

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

            // DTO 설정
            memberDto.setSettings(dtos);
            memberDto.setInterests(interestDtos);

            return ResponseEntity.ok(memberDto);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping(value = "/update/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMemberProfile(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "introduction", required = false) String introduction,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) throws IOException {
        try {
            // 서비스에서 프로필 업데이트 로직 실행
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken; // "Bearer " 이후 부분 추출

            Member member;
            UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(nickname, introduction);

            if (profilePicture != null && !profilePicture.isEmpty()) {
                // Call service to update member profile with the new picture
                member = memberService.updateMemberProfile(token, updateProfileRequest, profilePicture);
            } else {
                // Call service to update member profile without the picture
                member = memberService.updateMemberProfileWithoutPicture(token, updateProfileRequest);
            }

            UpdateProfileResponse response = new UpdateProfileResponse(
                    member.getNickname(),
                    member.getIntroduce(),
                    member.getProfile()
            );

            // 성공적으로 업데이트되면 200 OK 응답
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (DuplicateNicknameException ex) {
            // 닉네임 중복 시 409 Conflict 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }catch (UserNotFoundException ex) {
            // 사용자를 찾지 못한 경우 404 Not Found 응답
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (InvalidTokenException ex) {
            // 잘못된 토큰이면 401 Unauthorized 응답
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
        } catch (FileUploadException ex) {
            // 파일 업로드 실패 시 500 Internal Server Error 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        } catch (IOException ex) {
            // 파일 저장 중 발생하는 I/O 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while saving file");
        } catch (Exception ex) {
            // 그 외의 예기치 않은 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @PutMapping("/change/password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody PasswordChangeRequest request){
        try {
            // 현재 비밀번호 확인 및 비밀번호 변경 처리
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

            memberService.changePassword(token, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully.");
        } catch (UserNotFoundException e) {
            // 사용자를 찾을 수 없을 때 예외 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidPasswordException e) {
            // 비밀번호 유효성 검사 실패 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 그 외 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update/notification")
    public ResponseEntity<?> updateNotification(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody SettingRequest dto){
        try {
            memberService.updateNotification(dto);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
        } catch (EntityNotFoundException e) {
            // 사용자를 찾을 수 없을 때 예외 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // 그 외 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/profile/{filename}")
    public ResponseEntity<Resource> getProfile(@PathVariable("filename") String filename) {
        try {
            Path filePath = Paths.get("BE_SIDE/study/src/main/resources/profile").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/interestList")
    public ResponseEntity<?> getInterestList() {
        try {
            List<InterestAreaDTO> interestAreaDTOList = memberService.getInterestAreas();
            return ResponseEntity.ok(interestAreaDTOList);
        } catch (Exception e) {
            // 예외 로그 기록 (선택적)
            e.printStackTrace(); // 콘솔에 예외 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/init/interest")
    public ResponseEntity<?> initInterest(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody InterestRequest interestRequest){
        try {
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
            List<MemberInterestDTO> dtos = memberService.initInterest(token, interestRequest);
            return ResponseEntity.status(HttpStatus.OK).body(dtos);
        } catch (UserNotFoundException | InterestNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/update/interest")
    public ResponseEntity<?> updateInterest(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody InterestRequest interestRequest){
        try {
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
            List<MemberInterestDTO> dtos = memberService.updateInterest(token, interestRequest);
            return ResponseEntity.status(HttpStatus.OK).body(dtos);
        } catch (UserNotFoundException | InterestNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());
        }
    }

}
