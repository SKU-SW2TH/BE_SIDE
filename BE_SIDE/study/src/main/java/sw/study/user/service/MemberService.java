package sw.study.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.user.domain.Member;
import sw.study.user.dto.MemberDto;
import sw.study.user.repository.MemberRepository;

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

    @Transactional
    public Long join(MemberDto memberDto) {

        Member member = Member.createMember(
                memberDto.getEmail(), memberDto.getPassword(),
                memberDto.getNickname(), memberDto.getProfile(),
                memberDto.getIntroduce()
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
}
