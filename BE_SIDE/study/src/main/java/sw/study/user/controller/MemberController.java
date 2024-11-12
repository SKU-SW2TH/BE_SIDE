package sw.study.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sw.study.exception.*;
import sw.study.exception.s3.S3UploadException;
import sw.study.user.apiDoc.MemberApiDocumentation;
import sw.study.user.dto.*;
import sw.study.user.service.MemberService;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "Member", description = "Member API")
public class MemberController implements MemberApiDocumentation {
    private final MemberService memberService;

    @Override
    @GetMapping("/info")
    public ResponseEntity<?> getMemberInfo(@RequestHeader("Authorization") String accessToken) {
        try {
            MemberDto memberDTO = memberService.getMemberByToken(accessToken);
            return ResponseEntity.ok(memberDTO);


        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예상지 못한 오류 발생");
        }
    }

    @Override
    @PatchMapping(value = "/update/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateMemberProfile(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "introduction", required = false) String introduction,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {

            UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest(nickname, introduction);
            UpdateProfileResponse response = memberService.updateMemberProfile(accessToken, updateProfileRequest, profilePicture);

            // 성공적으로 업데이트되면 200 OK 응답
            return ResponseEntity.status(HttpStatus.OK).body(response);


        } catch (DuplicateNicknameException e) {
            // 닉네임 중복 시 409 Conflict 반환
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UserNotFoundException e) {
            // 사용자를 찾지 못한 경우 404 Not Found 응답
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidTokenException e) {
            // 잘못된 토큰이면 401 Unauthorized 응답
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 토큰입니다.");
        } catch (FileUploadException e) {
            // 파일 업로드 실패 시 500 Internal Server Error 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패");
        }catch (S3UploadException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("S3 업로드 실패: " + e.getMessage());
        } catch (Exception e) {
            // 그 외의 예기치 않은 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @PatchMapping("/change/password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody PasswordChangeRequest request){
        try {
            memberService.changePassword(accessToken, request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.status(HttpStatus.OK).body("비밀번호가 변경되었습니다.");


        } catch (UserNotFoundException e) {
            // 사용자를 찾을 수 없을 때 예외 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InvalidPasswordException e) {
            // 비밀번호 유효성 검사 실패 예외 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }  catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // 그 외 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @PatchMapping("/update/notification")
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

    @Override
    @GetMapping("/interestList")
    public ResponseEntity<?> getInterestList() {
        try {
            List<AreaDTO> areaDTOList = memberService.getInterestAreas();
            return ResponseEntity.ok(areaDTOList);
        } catch (Exception e) {
            // 예외 로그 기록 (선택적)
            e.printStackTrace(); // 콘솔에 예외 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("예기치 못한 에러 발생");
        }
    }

    @Override
    @PostMapping("/init/interest")
    public ResponseEntity<?> initInterest(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody AreaRequest areaRequest){
        try {
            List<MemberAreaDTO> dtos = memberService.initInterest(accessToken, areaRequest);
            return ResponseEntity.status(HttpStatus.OK).body(dtos);


        } catch (UserNotFoundException | InterestNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예기치 못한 에러 발생");
        }
    }

    @Override
    @PutMapping("/update/interest")
    public ResponseEntity<?> updateInterest(@RequestHeader("Authorization") String accessToken,
                                            @RequestBody AreaRequest areaRequest){
        try {
            List<MemberAreaDTO> dtos = memberService.updateInterest(accessToken, areaRequest);
            return ResponseEntity.status(HttpStatus.OK).body(dtos);


        } catch (UserNotFoundException | InterestNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예기치 못한 에러 발생");
        }
    }

    @Override
    @PatchMapping("/update/notification/read")
    public ResponseEntity<?> updateRead(@RequestHeader("Authorization") String accessToken) {
        try {
            memberService.updateNotificationRead(accessToken); // 철자 수정
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예기치 못한 에러 발생");
        }
    }

    @Override
    @GetMapping("/notificationList")
    public ResponseEntity<?> getNotificationList(@RequestHeader("Authorization") String accessToken) {
        try {
            List<NotificationDTO> dtos = memberService.getNotifications(accessToken);
            return ResponseEntity.ok(dtos);


        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예기치 못한 에러 발생");
        }
    }

    @Override
    @GetMapping("/notification/unread")
    public ResponseEntity<?> unReadNotification(@RequestHeader("Authorization") String accessToken) {
        try {
            List<NotificationDTO> dtos = memberService.unReadNotification(accessToken);
            return ResponseEntity.ok(dtos);


        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("예기치 못한 에러 발생");
        }
    }

}
