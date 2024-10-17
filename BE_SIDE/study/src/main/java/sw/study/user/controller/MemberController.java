package sw.study.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sw.study.exception.InvalidTokenException;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.dto.ErrorResponse;
import sw.study.user.domain.Member;
import sw.study.user.dto.MemberDto;
import sw.study.user.service.MemberService;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/info")
    public ResponseEntity<?> getMemberInfo(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.substring(7); // "Bearer " 이후 부분 추출

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

}
