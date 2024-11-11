package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.JWTService;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.studyGroup.DailyLogNotFoundException;
import sw.study.exception.studyGroup.StudyGroupNotFoundException;
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.studyGroup.domain.DailyLog;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.DailyLogResponseDto;
import sw.study.studyGroup.repository.DailyLogRepository;
import sw.study.studyGroup.repository.ParticipantRepository;
import sw.study.studyGroup.repository.StudyGroupRepository;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DailyLogService {

    private MemberRepository memberRepository;
    private ParticipantRepository participantRepository;
    private StudyGroupRepository studyGroupRepository;
    private DailyLogRepository dailyLogRepository;

    private JWTService jwtService;

    // 토큰에서 사용자 이메일 정보 얻어서 Member 객체 가져오기
    private Member currentLogginedInfo(String accessToken) {
        // accessToken 유효성 검사
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("[ERROR] 유효하지 않는 토큰 형식입니다.");
        }

        // "Bearer " 부분 제거 후 실제 토큰만 추출
        String token = accessToken.substring(7);

        String email = jwtService.extractEmail(token);

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 데일리 로그 작성
    public void createDailyLog(String accessToken, long groupId, String title, String content){

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("해당 그룹에 참가하지 않은 비정상적인 접근입니다."));

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new StudyGroupNotFoundException("스터디 그룹이 존재하지 않습니다."));

        DailyLog dailyLog = DailyLog.createDailyLog(studyGroup, participant, title, content);
        dailyLogRepository.save(dailyLog);
    }

    // 데일리 로그 조회
    @Transactional(readOnly = true)
    public List<DailyLogResponseDto> listOfDailyLog(String accessToken, long groupId){

        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("해당 그룹에 참가하지 않은 비정상적인 접근입니다."));

        List<DailyLog> logs = dailyLogRepository.findAllByStudyGroupId(groupId);
        return logs.stream().map(DailyLogResponseDto::new).toList();
        // 스트림 형태로 변환, 각 요소를 Dto 형태로 바꿔서 리턴
    }

    // 데일리 로그 수정
    public void updateDailyLog(String accessToken, long groupId, long logId, String title,String content){
        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("해당 그룹에 참가하지 않은 비정상적인 접근입니다."));

        DailyLog dailyLog =dailyLogRepository.findById(logId)
                .orElseThrow(()-> new DailyLogNotFoundException("해당하는 데일리 로그는 존재하지 않습니다."));

        if(!dailyLog.getAuthor().getId().equals(participant.getId()))
            throw new UnauthorizedException("수정 권한이 없습니다.");

        dailyLog.updateLog(title, content);
    }

    // 데일리 로그 삭제
    public void deleteDailyLog(String accessToken, long groupId, long logId){
        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("해당 그룹에 참가하지 않은 비정상적인 접근입니다."));

        DailyLog dailyLog =dailyLogRepository.findById(logId)
                .orElseThrow(()-> new DailyLogNotFoundException("해당하는 데일리 로그는 존재하지 않습니다."));

        if (dailyLog.getAuthor().getId().equals(participant.getId()) || participant.getRole() == Participant.Role.LEADER) {
            // 작성자 본인이거나 혹은 방장일 경우에
            dailyLogRepository.deleteById(logId);
        } else {
            throw new UnauthorizedException("삭제 권한이 없습니다.");
        }
    }
}
