package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.JWTService;
import sw.study.exception.BaseException;
import sw.study.exception.ErrorCode;
import sw.study.exception.UserNotFoundException;
import sw.study.studyGroup.domain.Notice;
import sw.study.studyGroup.domain.NoticeCheck;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.repository.NoticeCheckRepository;
import sw.study.studyGroup.repository.NoticeRepository;
import sw.study.studyGroup.repository.ParticipantRepository;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeCheckService {

    private final MemberRepository memberRepository;
    private final NoticeRepository noticeRepository;
    private final ParticipantRepository participantRepository;
    private final NoticeCheckRepository noticeCheckRepository;
    private final JWTService jwtService;

    private Member currentLoggedInfo(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 단일 메소드로 로직 구성 ( 체크 등록 / 삭제 )
    @Transactional
    public void toggleCheck(String accessToken, Long studyGroupId, Long noticeId){

        Member member = currentLoggedInfo(accessToken);

        //참여 여부 판단
        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), studyGroupId)
                .orElseThrow(()-> new BaseException(ErrorCode.UNAUTHORIZED));

        //공지사항 여부 판단
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(()-> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        //특정 공지에 대한 Check 여부 판단
        NoticeCheck noticeCheck = noticeCheckRepository.findByNoticeIdAndParticipantId(noticeId, participant.getId())
                .orElse(null);

        if(noticeCheck==null){
            // 특정 사용자가 체크 표시를 하지 않은경우
            noticeCheck = NoticeCheck.createNoticeCheck(notice,participant);
            noticeCheckRepository.save(noticeCheck);
        }
        else{
            noticeCheckRepository.delete(noticeCheck);
        }
    }
}
