package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sw.study.community.service.S3Service;
import sw.study.config.jwt.JWTService;
import sw.study.exception.BaseException;
import sw.study.exception.ErrorCode;
import sw.study.exception.UserNotFoundException;
import sw.study.exception.s3.FileUploadException;
import sw.study.exception.s3.S3UploadException;
import sw.study.studyGroup.domain.DailyLog;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.DailyLogResponse;
import sw.study.studyGroup.repository.DailyLogRepository;
import sw.study.studyGroup.repository.ParticipantRepository;
import sw.study.studyGroup.repository.StudyGroupRepository;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class DailyLogService {

    private final MemberRepository memberRepository;
    private final ParticipantRepository participantRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final DailyLogRepository dailyLogRepository;

    private final JWTService jwtService;
    private final S3Service s3Service;

    // 토큰에서 사용자 이메일 정보 얻어서 Member 객체 가져오기
    private Member currentLogginedInfo(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 데일리 로그 작성
    @Transactional
    public void createDailyLog(String accessToken, Long groupId, String title, String content, List<MultipartFile> files){

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        List<String> fileUrls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    // 공지사항에 맞는 디렉토리 경로로 파일 업로드
                    String fileUrl = s3Service.upload(file, "dailyLog/");
                    fileUrls.add(fileUrl);
                } catch (FileUploadException e) {
                    throw new BaseException(ErrorCode.FILE_UPLOAD_ERROR);
                } catch (S3UploadException e) {
                    throw new BaseException(ErrorCode.S3_UPLOAD_ERROR);
                }
            }
        }

        DailyLog dailyLog = DailyLog.createDailyLog(studyGroup, participant, title, content, fileUrls);
        dailyLogRepository.save(dailyLog);
    }

    // 데일리 로그 조회
    @Transactional(readOnly = true)
    public List<DailyLogResponse> listOfDailyLog(String accessToken, int page, int size, Long groupId, String dateStr){

        Pageable pageable = PageRequest.of(page, size);

        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        LocalDateTime startOfDay = date.atStartOfDay(); // 특정일의 자정
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        Page<DailyLog> logs = dailyLogRepository.findAllByStudyGroup_IdAndCreatedAtBetween(groupId, startOfDay, endOfDay, pageable);

        return logs.stream().map(DailyLogResponse::createDailyLogResponse).toList();
    }

    // 데일리 로그 수정
    @Transactional
    public void updateDailyLog(String accessToken, Long groupId, Long logId, String title, String content, List<MultipartFile> files){
        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        DailyLog dailyLog =dailyLogRepository.findById(logId)
                .orElseThrow(()-> new BaseException(ErrorCode.DAILYLOG_NOT_FOUND));

        if(!dailyLog.getAuthor().getId().equals(participant.getId()))
            throw new BaseException(ErrorCode.PERMISSION_DENIED);

        List<String> fileUrls = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    // 공지사항에 맞는 디렉토리 경로로 파일 업로드
                    String fileUrl = s3Service.upload(file, "dailyLog/");
                    fileUrls.add(fileUrl);
                } catch (FileUploadException e) {
                    throw new BaseException(ErrorCode.FILE_UPLOAD_ERROR);
                } catch (S3UploadException e) {
                    throw new BaseException(ErrorCode.S3_UPLOAD_ERROR);
                }
            }
        }

        dailyLog.updateLog(title, content, fileUrls);
    }

    // 데일리 로그 삭제
    @Transactional
    public void deleteDailyLog(String accessToken, Long groupId, Long logId){
        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        DailyLog dailyLog =dailyLogRepository.findById(logId)
                .orElseThrow(()-> new BaseException(ErrorCode.DAILYLOG_NOT_FOUND));

        if (dailyLog.getAuthor().getId().equals(participant.getId()) || participant.getRole() == Participant.Role.LEADER) {
            // 작성자 본인이거나 혹은 방장일 경우에
            dailyLogRepository.deleteById(logId);
        } else {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }
    }
}
