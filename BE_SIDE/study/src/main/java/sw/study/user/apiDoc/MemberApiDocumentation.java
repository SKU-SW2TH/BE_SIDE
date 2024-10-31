package sw.study.user.apiDoc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import sw.study.user.dto.*;

import java.io.IOException;

public interface MemberApiDocumentation {
    @Operation(summary = "맴버 정보", description = "맴버 정보를 준다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "토큰 형식이 맞지 않음"),
            @ApiResponse(responseCode = "404", description = "유저 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생")
    })
    ResponseEntity<?> getMemberInfo(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token")
            @RequestHeader("Authorization") String accessToken);

    @Operation(summary = "멤버 프로필 업데이트", description = "멤버의 닉네임, 자기소개, 프로필 사진을 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 업데이트됨", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateProfileResponse.class))),
            @ApiResponse(responseCode = "409", description = "닉네임 중복으로 인한 업데이트 실패"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "잘못된 엑세스 토큰"),
            @ApiResponse(responseCode = "500", description = "파일 업로드 실패 또는 기타 서버 에러")
    })
    ResponseEntity<?> updateMemberProfile(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token")
            @RequestHeader("Authorization") String accessToken,

            @Parameter(name = "nickname", description = "변경할 닉네임", required = false, example = "코난")
            @RequestParam(value = "nickname", required = false) String nickname,

            @Parameter(name = "introduction", description = "변경할 자기소개", required = false, example = "하이")
            @RequestParam(value = "introduction", required = false) String introduction,

            @Parameter(name = "profilePicture", description = "변경할 프로필 사진 파일", required = false)
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture
    ) throws IOException;


    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "사용자가 잘못된 비밀번호 형식을 사용하거나 요구되는 보안 규칙을 충족하지 못한 비밀번호를 입력했을 때 발생합니다."),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생")
    })
    ResponseEntity<?> changePassword(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token")
            @RequestHeader("Authorization") String accessToken,
                                     @RequestBody PasswordChangeRequest request);



    @Operation(summary = "알림 변경", description = "알림을 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "토큰 형식이 맞지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾지 못함"),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생")
    })
    ResponseEntity<?> updateNotification(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token")
            @RequestHeader("Authorization") String accessToken,
                                         @RequestBody SettingRequest dto);

    @Operation(summary = "프로필 사진", description = "프로필 사진 가져오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생")
    })
    ResponseEntity<Resource> getProfile(@PathVariable("filename") String filename);

    @Operation(summary = "관심 분야 리스트", description = "관심 분야 리스트를 받는다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러가 발생")
    })
    ResponseEntity<?> getInterestList();


    @Operation(summary = "관심사 초기화", description = "회원의 관심사를 초기화합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberInterestDTO.class)))),
            @ApiResponse(responseCode = "404", description = "사용자 또는 관심사를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "토큰 형식이 맞지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생")
    })
    ResponseEntity<?> initInterest(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token")
            @RequestHeader("Authorization") String accessToken,
            @Parameter(description = "초기화할 관심사 요청 데이터", required = true)
            @RequestBody InterestRequest interestRequest);

    @Operation(summary = "관심사 업데이트", description = "회원의 관심사를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberInterestDTO.class)))),
            @ApiResponse(responseCode = "400", description = "토큰 형식이 맞지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 관심사를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생")
    })
    ResponseEntity<?> updateInterest(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token", required = true)
            @RequestHeader("Authorization") String accessToken,
            @Parameter(description = "업데이트할 관심사 요청 데이터", required = true)
            @RequestBody InterestRequest interestRequest);

    @Operation(summary = "읽기 업데이트", description = "알림 읽음을 업데이트 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "토큰 형식이 맞지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생")
    })
    ResponseEntity<?> updateRead(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token", required = true)
            @RequestHeader("Authorization") String accessToken);


    @Operation(summary = "알림 리스트", description = "알림 리스트를 보낸다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "토큰 형식이 맞지 않음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 에러 발생")
    })
    ResponseEntity<?> getNotificationList(
            @Parameter(name = "Authorization", description = "엑세스 토큰", example = "Bearer your_access_token", required = true)
            @RequestHeader("Authorization") String accessToken);
}
