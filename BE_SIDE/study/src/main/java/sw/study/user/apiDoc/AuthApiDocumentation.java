package sw.study.user.apiDoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import sw.study.config.jwt.TokenDTO;
import sw.study.user.dto.*;

public interface AuthApiDocumentation {

    ResponseEntity<String> sendVerificationEmail(@RequestBody EmailDto emailDto);

    ResponseEntity<String> verifyEmail(@RequestBody EmailVerificationRequest request);

    ResponseEntity<String> verifyNickname(@RequestBody JoinDto joinDto);

    @Operation(summary = "회원가입", description = "회원가입 할 때 사용하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입이 완료되었습니다."),
            @ApiResponse(responseCode = "400", description = "이미 사용 중인 이메일입니다.")
    })
    @Parameters(value = {
            @Parameter(name = "email", description = "이메일", example = "ksh123@naver.com"),
            @Parameter(name = "password", description = "비밀번호"),
            @Parameter(name = "nickname", description = "닉네임", example = "코난123"),
            @Parameter(name = "profile", description = "프로필 사진"),
            @Parameter(name = "introduce", description = "자기소개 글", example = "안녕하세요! 저는..."),
    })
    ResponseEntity<String> join(@RequestBody JoinDto joinDto);



    ResponseEntity<?> login(@RequestBody LoginRequest loginRequest);
    ResponseEntity<String> logout(@RequestBody TokenRequest logoutRequest);
    ResponseEntity<?> reissue(@RequestBody TokenRequest tokenRequest);
}
