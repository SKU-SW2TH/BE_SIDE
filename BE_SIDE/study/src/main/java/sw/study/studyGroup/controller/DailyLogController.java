package sw.study.studyGroup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.studyGroup.DailyLogNotFoundException;
import sw.study.exception.studyGroup.StudyGroupNotFoundException;
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.studyGroup.dto.DailyLogRequestDto;
import sw.study.studyGroup.dto.DailyLogResponseDto;
import sw.study.studyGroup.service.DailyLogService;
import sw.study.studyGroup.service.StudyGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/studyGroup/{groupId}/dailyLog")
@RequiredArgsConstructor
@Tag(name = "DailyLog", description = "그룹 내 데일리 로그 관련 API")
public class DailyLogController {

    private final StudyGroupService studyGroupService;
    private final DailyLogService dailyLogService;

    // 데일리 로그 작성
    @PostMapping("/create")
    public ResponseEntity<?> createDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @RequestBody DailyLogRequestDto requestDto) {
        try {
            dailyLogService.createDailyLog(accessToken, groupId, requestDto.getTitle(), requestDto.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body("데일리 로그가 성공적으로 작성되었습니다.");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (StudyGroupNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 데일리 로그 조회
    @GetMapping("/list")
    public ResponseEntity<?> listOfDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId) {
        try {
            List<DailyLogResponseDto> logs = dailyLogService.listOfDailyLog(accessToken, groupId);
            return ResponseEntity.ok().body(logs); // 리스트 반환
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 데일리 로그 수정
    @PutMapping("/{logId}/update")
    public ResponseEntity<?> updateDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable long logId,
            @RequestBody DailyLogRequestDto requestDto) {
        try {
            dailyLogService.updateDailyLog(accessToken, groupId, logId, requestDto.getTitle(), requestDto.getContent());
            return ResponseEntity.status(HttpStatus.OK).body("데일리 로그가 성공적으로 수정되었습니다.");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (DailyLogNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }

    // 데일리 로그 삭제
    @DeleteMapping("/{logId}/delete")
    public ResponseEntity<?> deleteDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable long groupId,
            @PathVariable long logId) {
        try {
            dailyLogService.deleteDailyLog(accessToken,groupId, logId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("데일리 로그가 성공적으로 삭제되었습니다.");
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (DailyLogNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
        }
    }
}
