package sw.study.user.apiDoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.config.Constant;
import sw.study.exception.SamePasswordException;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.email.EmailNotFoundException;
import sw.study.exception.email.EmailSendException;
import sw.study.user.dto.*;

public interface AuthApiDocumentation {

    @Operation(summary = "이메일 인증 코드 발송", description = "이메일 인증 코드를 발송하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드가 전송되었습니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다."),
            @ApiResponse(responseCode = "500", description = "인증 코드 생성 중 오류가 발생했습니다."),
            @ApiResponse(responseCode = "503", description = "인증 코드를 저장하는 중 오류가 발생했습니다."),
            @ApiResponse(responseCode = "500", description = "이메일 전송 중 오류가 발생했습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "email", description = "이메일", example = "ksh123@naver.com")
    })
    ResponseEntity<String> sendVerificationEmail(@RequestBody EmailDto emailDto);


    @Operation(summary = "이메일 인증 코드 확인", description = "이메일 인증 코드 확인 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 인증이 완료되었습니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다."),
            @ApiResponse(responseCode = "422", description = "인증 코드가 올바르지 않습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "email", description = "이메일", example = "ksh123@naver.com"),
            @Parameter(name = "verificationCode", description = "인증 코드", example = "...")
    })
    ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequest request);


    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 확인 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용 가능한 닉네임입니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임입니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "nickname", description = "닉네임", example = "코난123")
    })
    ResponseEntity<String> verifyNickname(@RequestBody NicknameDto nicknameDto);


    @Operation(summary = "회원가입", description = "회원가입 할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입이 완료되었습니다."),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일입니다."),
            @ApiResponse(responseCode = "500", description = "회원 가입 중 오류가 발생했습니다."),
            @ApiResponse(responseCode = "500", description = "회원 가입 중 데이터베이스 오류가 발생했습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "email", description = "이메일", example = "ksh123@naver.com"),
            @Parameter(name = "password", description = "비밀번호"),
            @Parameter(name = "nickname", description = "닉네임", example = "코난123")
    })
    ResponseEntity<String> join(@RequestBody JoinDto joinDto);


    @Operation(summary = "로그인", description = "사용자가 로그인하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공, 토큰 반환."),
            @ApiResponse(responseCode = "401", description = "잘못된 자격 증명입니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    ResponseEntity<?> login(@RequestBody LoginRequest loginRequest);


    @Operation(summary = "로그아웃", description = "사용자가 로그아웃하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생했습니다.")
    })
    ResponseEntity<String> logout(
            @RequestBody TokenRequest logoutRequest);


    @Operation(summary = "토큰 재발행", description = "리프레시 토큰을 사용하여 액세스 토큰을 재발행하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 재발행 성공."),
            @ApiResponse(responseCode = "401", description = "토큰 재발행에 실패했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생했습니다.")
    })
    ResponseEntity<?> reissue(@RequestBody TokenRequest tokenRequest);

    @Operation(summary = "계정 탈퇴", description = "계정을 탈퇴합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 업데이트됨"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    ResponseEntity<String> deleteAccount(
            @Parameter(name = "Authorization", description = "리프레쉬 토큰", example = "Bearer your_refresh_token")
            @RequestHeader("Authorization") String refreshToken);

    @Operation(summary = "계정 복구", description = "계정을 복구합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 업데이트됨"),
            @ApiResponse(responseCode = "401", description = "[ERROR] 유효하지 않은 토큰 형식입니다."),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    ResponseEntity<String> restoreMember(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token", required = true)
            @RequestHeader("Authorization") String accessToken);


    @Operation(summary = "비밀번호 재설정 페이지에서 사용될 resetToken 전송", description = "비밀번호 재설정 페이지에서 사용될 resetToken 전송하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 페이지에서 사용될 resetToken 전송"),
            @ApiResponse(responseCode = "404", description = "가입되어 있지 않은 이메일 입니다."),
            @ApiResponse(responseCode = "500", description = "이메일 전송 중 오류가 발생했습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "email", description = "이메일 ※전송 시 큰 따옴표는 제거하고 보내야함", example = "ksh990409@naver.com")
    })
    ResponseEntity<String> sendResetToken(@RequestBody String email);


    @Operation(summary = "resetToken의 유효성 검사 ", description = "비밀번호 재설정 페이지에서 resetToken의 유효성을 검사하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 인증을 성공하였습니다."),
            @ApiResponse(responseCode = "401", description = "[ERROR] 유효하지 않은 토큰 형식입니다."),
            @ApiResponse(responseCode = "401", description = "유효성 검사를 통과하지 못했습니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    public ResponseEntity<String> validResetToken(@RequestHeader("Authorization") String token);

    @Operation(summary = "비회원 상태로 비밀번호 변경", description = "비회원 상태로 비밀번호 변경하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호가 변경되었습니다."),
            @ApiResponse(responseCode = "400", description = "변경하려는 비밀번호가 기존 비밀번호와 같습니다."),
            @ApiResponse(responseCode = "401", description = "[ERROR] 유효하지 않은 토큰 형식입니다."),
            @ApiResponse(responseCode = "404", description = "가입되어 있지 않은 이메일 입니다."),
            @ApiResponse(responseCode = "500", description = "예기치 못한 오류가 발생했습니다.")
    })
    @Parameters(value = {
            @Parameter(name = "newPassword", description = "새로운 비밀번호 ※전송 시 큰 따옴표는 제거하고 보내야함", example = "pok123!!")
    })
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String token, @RequestBody String newPassword);
}
