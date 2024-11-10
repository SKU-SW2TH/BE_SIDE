package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.JWTService;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.studyGroup.NoticeNotFoundException;
import sw.study.exception.studyGroup.StudyGroupNotFoundException;
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.studyGroup.domain.Notice;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.NoticeResponseDto;
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

    private MemberRepository memberRepository;
    private ParticipantRepository participantRepository;
    private StudyGroupRepository studyGroupRepository;
    private NoticeRepository noticeRepository;

    private final JWTService jwtService;

    // 토큰에서 사용자 이메일 정보 얻어서 Member 객체 가져오기
    private Member currentLogginedInfo(String accessToken) {
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("[ERROR] 유효하지 않는 토큰 형식입니다.");
        }
        String token = accessToken.substring(7);

        String email = jwtService.extractEmail(token);

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 그룹에 참가중인지 확인
    private Participant checkGroupParticipant(long groupId, Member member) {
        return participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("해당 그룹에 참가하지 않은 비정상적인 접근입니다."));
    }

    // 공지사항 작성
    @Transactional
    public void createNotice(String accessToken, long groupId, String title, String content){

        Member member = currentLogginedInfo(accessToken);

        Participant participant = checkGroupParticipant(groupId, member);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new StudyGroupNotFoundException("스터디 그룹이 존재하지 않습니다."));

        if(participant.getRole()== Participant.Role.MEMBER){
            // 리더 혹은 운영진만 가능
            throw new UnauthorizedException("작성할 수 있는 권한이 없습니다.");
        }

        Notice notice = Notice.createNotice(studyGroup,participant,title,content);
        noticeRepository.save(notice);
    }

    // 공지사항 조회 ( 목록 )
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> listOfNotice(String accessToken, long groupId, int page, int size){

        Member member = currentLogginedInfo(accessToken);

        checkGroupParticipant(groupId, member);

        Pageable pageable = PageRequest.of(page, size);

        Page<Notice> noticePage = noticeRepository.findAllByStudyGroup_Id(groupId);

        if(noticePage.isEmpty())
            throw new NoticeNotFoundException("조회된 공지사항이 존재하지 않습니다.");

        return noticePage.stream().map(NoticeResponseDto::fromList).toList();
    }

    // 공지사항 조회 ( 상세 )
    @Transactional(readOnly = true)
    public NoticeResponseDto noticeDetail(String accessToken, long groupId, long noticeId){

        Member member = currentLogginedInfo(accessToken);

        checkGroupParticipant(groupId, member);

        Notice notice = noticeRepository.findByIdAndGroup_Id(noticeId, groupId)
                .orElseThrow(()-> new NoticeNotFoundException("해당하는 공지사항이 존재하지 않습니다."));

        return NoticeResponseDto.fromDetail(notice);
    }

    // 공지사항 수정
    @Transactional
    public void updateNotice(String accessToken, long groupId, long noticeId, String title, String content){

        Member member = currentLogginedInfo(accessToken);

        Participant participant = checkGroupParticipant(groupId, member);

        Notice notice = noticeRepository.findByIdAndGroup_Id(noticeId, groupId)
                .orElseThrow(()-> new NoticeNotFoundException("해당하는 공지사항이 존재하지 않습니다."));

        if(participant.getRole()== Participant.Role.MEMBER){
            // 리더 혹은 운영진만 가능
            throw new UnauthorizedException("수정할 수 있는 권한이 없습니다.");
        }

        notice.updateContent(title, content);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteNotice(String accessToken, long groupId, long noticeId){
        Member member = currentLogginedInfo(accessToken);

        Participant participant = checkGroupParticipant(groupId, member);

        Notice notice = noticeRepository.findByIdAndGroup_Id(noticeId, groupId)
                .orElseThrow(()-> new NoticeNotFoundException("해당하는 공지사항이 존재하지 않습니다."));


        if(participant.getRole()== Participant.Role.MEMBER){
            // 리더 혹은 운영진만 가능
            throw new UnauthorizedException("삭제 할 수 있는 권한이 없습니다.");
        }

        noticeRepository.delete(notice);
    }
}
