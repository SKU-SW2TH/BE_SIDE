package sw.study.user.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.DuplicateNicknameException;
import sw.study.exception.InvalidPasswordException;
import sw.study.exception.InvalidTokenException;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.dto.ErrorResponse;
import sw.study.user.domain.Member;
import sw.study.user.dto.*;
import sw.study.user.service.MemberService;

import java.io.IOException;

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

            MemberDto memberDto = new MemberDto();
            memberDto.setEmail(member.getEmail());
            memberDto.setNickname(member.getNickname());
            memberDto.setProfile(member.getProfile());
            memberDto.setIntroduce(member.getIntroduce());

            return ResponseEntity.ok(memberDto);
        }catch (InvalidTokenException e) {
            // 유효하지 않은 토큰일 경우 401 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(e.getMessage()));
        } catch (UserNotFoundException e) {
            // 사용자가 존재하지 않을 경우 404 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // 일반적인 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/update/profile")
    public ResponseEntity<?> updateMemberProfile(
            @RequestHeader("Authorization") String accessToken,
            @ModelAttribute UpdateProfileRequest updateProfileRequest) throws IOException {
        try {
            // 서비스에서 프로필 업데이트 로직 실행
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken; // "Bearer " 이후 부분 추출
            Member member = memberService.updateMemberProfile(token, updateProfileRequest);

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
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
                                            @RequestBody NotificationSettingDTO dto){
        try {
            String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;

            memberService.updateNotification(token, dto);
            return ResponseEntity.status(HttpStatus.OK).body("updated successfully.");
        } catch (EntityNotFoundException e) {
            // 사용자를 찾을 수 없을 때 예외 처리
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // 그 외 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
