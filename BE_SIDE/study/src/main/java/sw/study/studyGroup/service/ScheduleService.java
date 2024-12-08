package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.JWTService;
import sw.study.exception.BaseException;
import sw.study.exception.ErrorCode;
import sw.study.exception.UserNotFoundException;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.Schedule;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.ScheduleDetailResponse;
import sw.study.studyGroup.dto.ScheduleListResponse;
import sw.study.studyGroup.repository.ParticipantRepository;
import sw.study.studyGroup.repository.ScheduleRepository;
import sw.study.studyGroup.repository.StudyGroupRepository;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {

    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ScheduleRepository scheduleRepository;
    private final JWTService jwtService;

    private Member currentLogginedInfo(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 일정 생성
    @Transactional
    public void createSchedule(String accessToken, Long groupId, String title, String description, LocalDate startDate, LocalDate endDate) {

        try{
            Member member = currentLogginedInfo(accessToken);

            // 그룹 존재 여부
            StudyGroup studygroup = studyGroupRepository.findById(groupId)
                    .orElseThrow(()->new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

            // 그룹 참가 여부
            Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(),groupId)
                    .orElseThrow(()->new BaseException(ErrorCode.UNAUTHORIZED));

            // 신분 확인
            if(participant.getRole()==Participant.Role.MEMBER)
                throw new BaseException(ErrorCode.PERMISSION_DENIED);

            Schedule schedule = Schedule.createSchedule(studygroup, title, description, startDate,endDate);
            scheduleRepository.save(schedule);
        }catch(Exception e){
            log.error("Error while creating schedule", e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 1달 간의 전체 일정 조회
    @Transactional(readOnly = true)
    public List<ScheduleListResponse> getScheduleList(String accessToken, Long groupId, int year, int month){

        Member member = currentLogginedInfo(accessToken);

        // 그룹에 참가중이지 않음
        participantRepository.findByMemberIdAndStudyGroupId(member.getId(),groupId)
                .orElseThrow(()->new BaseException(ErrorCode.UNAUTHORIZED));

        // 그룹이 존재하지 않음
        studyGroupRepository.findById(groupId).orElseThrow(()->new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        YearMonth yearMonth = YearMonth.of(year,month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        List<Schedule> scheduleList = scheduleRepository.findByStartDateBetweenAndStudyGroupId(startOfMonth, endOfMonth, groupId);

        return scheduleList.stream().map(schedule -> new ScheduleListResponse(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getStartDate(),
                schedule.getEndDate()
        )).collect(Collectors.toList());
    }

    // 특정한 일정 상세 조회
    @Transactional(readOnly = true)
    public ScheduleDetailResponse getScheduleDetails(String accessToken, Long groupId, Long scheduleId){

        Member member = currentLogginedInfo(accessToken);

        // 그룹에 참가중이지 않음
        participantRepository.findByMemberIdAndStudyGroupId(member.getId(),groupId)
                .orElseThrow(()-> new BaseException(ErrorCode.UNAUTHORIZED));

        // 일정이 존재하지 않음
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()->new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        // 그룹이 존재하지 않음
        studyGroupRepository.findById(groupId).orElseThrow(()->new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        return new ScheduleDetailResponse(
                scheduleId,
                schedule.getTitle(),
                schedule.getDescription(),
                schedule.getStartDate(),
                schedule.getEndDate()
        );
    }

    // 일정 수정
    @Transactional
    public void updateSchedule(String accessToken, Long scheduleId, String title, String description, LocalDate startDate, LocalDate endDate){

        Member member = currentLogginedInfo(accessToken);

        // 해당 일정 존재 여부
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()->new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        StudyGroup studyGroup = schedule.getStudyGroup();

        // 그룹 참가 여부
        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), studyGroup.getId())
                .orElseThrow(()->new BaseException(ErrorCode.UNAUTHORIZED));

        // 그룹 내 권한 여부
        if(participant.getRole()==Participant.Role.MEMBER)
            throw new BaseException(ErrorCode.PERMISSION_DENIED);

        schedule.updateSchedule(title, description, startDate, endDate);
    }


    // 일정 삭제
    @Transactional
    public void deleteSchedule(String accessToken,Long scheduleId) {

        Member member = currentLogginedInfo(accessToken);

        // 일정 존재 여부
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()->new BaseException(ErrorCode.SCHEDULE_NOT_FOUND));

        StudyGroup studygroup =  schedule.getStudyGroup();

        // 그룹 참가 여부
        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), studygroup.getId())
                .orElseThrow(()->new BaseException(ErrorCode.UNAUTHORIZED));

        // 신분 확인
        if(participant.getRole()==Participant.Role.MEMBER)
            throw new BaseException(ErrorCode.PERMISSION_DENIED);

        scheduleRepository.delete(schedule);
    }
}
