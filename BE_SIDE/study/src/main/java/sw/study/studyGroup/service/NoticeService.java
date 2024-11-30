package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.JWTService;
import sw.study.exception.BaseException;
import sw.study.exception.ErrorCode;
import sw.study.exception.UserNotFoundException;
import sw.study.studyGroup.domain.Notice;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.NoticeResponse;
import sw.study.studyGroup.repository.NoticeRepository;
import sw.study.studyGroup.repository.ParticipantRepository;
import sw.study.studyGroup.repository.StudyGroupRepository;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeService {

    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final NoticeRepository noticeRepository;

    private final JWTService jwtService;

    // 토큰에서 사용자 이메일 정보 얻어서 Member 객체 가져오기
    private Member currentLogginedInfo(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 그룹에 참가중인지 확인
    private Participant checkGroupParticipant(long groupId, Member member) {
        return participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));
    }

    // 공지사항 작성
    @Transactional
    public void createNotice(String accessToken, long groupId, String title, String content){

        Member member = currentLogginedInfo(accessToken);

        Participant participant = checkGroupParticipant(groupId, member);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        if(participant.getRole()== Participant.Role.MEMBER){
            // 리더 혹은 운영진만 가능
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        Notice notice = Notice.createNotice(studyGroup,participant,title,content);
        noticeRepository.save(notice);
    }

    // 공지사항 조회 ( 목록 )
    @Transactional(readOnly = true)
    public List<NoticeResponse> listOfNotice(String accessToken, long groupId, int page, int size){

        Member member = currentLogginedInfo(accessToken);

        checkGroupParticipant(groupId, member);

        Pageable pageable = PageRequest.of(page, size);

        Page<Notice> noticePage = noticeRepository.findAllByStudyGroup_Id(groupId, pageable);

        if(noticePage.isEmpty())
            throw new BaseException(ErrorCode.NOTICE_NOT_FOUND);

        return noticePage.stream().map(NoticeResponse::fromList).toList();
    }

    // 공지사항 조회 ( 상세 )
    @Transactional(readOnly = true)
    public NoticeResponse noticeDetail(String accessToken, long groupId, long noticeId){

        Member member = currentLogginedInfo(accessToken);

        checkGroupParticipant(groupId, member);

        Notice notice = noticeRepository.findByIdAndStudyGroup_Id(noticeId, groupId)
                .orElseThrow(()-> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        return NoticeResponse.fromDetail(notice);
    }

    // 공지사항 수정
    @Transactional
    public void updateNotice(String accessToken, long groupId, long noticeId, String title, String content){

        Member member = currentLogginedInfo(accessToken);

        Participant participant = checkGroupParticipant(groupId, member);

        Notice notice = noticeRepository.findByIdAndStudyGroup_Id(noticeId, groupId)
                .orElseThrow(()-> new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        if(participant.getRole()== Participant.Role.MEMBER){
            // 리더 혹은 운영진만 가능
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        notice.updateContent(title, content);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(String accessToken, long groupId, long noticeId){
        Member member = currentLogginedInfo(accessToken);

        Participant participant = checkGroupParticipant(groupId, member);

        Notice notice = noticeRepository.findByIdAndStudyGroup_Id(noticeId, groupId)
                .orElseThrow(()->new BaseException(ErrorCode.NOTICE_NOT_FOUND));

        if(participant.getRole()== Participant.Role.MEMBER){
            // 리더 혹은 운영진만 가능
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        noticeRepository.delete(notice);
    }
}
