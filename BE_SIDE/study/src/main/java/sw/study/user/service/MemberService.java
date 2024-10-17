package sw.study.user.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.TokenProvider;
import sw.study.exception.InvalidTokenException;
import sw.study.exception.UserNotFoundException;
import sw.study.user.domain.Member;
import sw.study.user.dto.MemberDto;
import sw.study.user.repository.MemberRepository;
import sw.study.user.util.RedisUtil;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final TokenProvider tokenProvider;

    @Transactional
    public Long join(MemberDto memberDto) {

        Member member = Member.createMember(
                memberDto.getEmail(), encoder.encode(memberDto.getPassword()),
                memberDto.getNickname()
        );

        return memberRepository.save(member).getId();
    }

    public boolean verifyNickname(MemberDto memberDto) {
        Optional<Member> findMember = memberRepository.findByNickname(memberDto.getNickname());
        return findMember.isEmpty();
    }

    public boolean verifyEmail(MemberDto memberDto) {
        Optional<Member> findMember = memberRepository.findByEmail(memberDto.getEmail());
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

        Claims claims = tokenProvider.parseClaims(token);
        String email = claims.getSubject();
        System.out.println(email);

        // 이메일로 사용자 조회
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

}
