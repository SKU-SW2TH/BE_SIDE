package sw.study.studyGroup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.studyGroup.DailyLogNotFoundException;
import sw.study.exception.studyGroup.StudyGroupNotFoundException;
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.studyGroup.apiDoc.DailyLogApiDocumentation;
import sw.study.studyGroup.dto.DailyLogRequestDto;
import sw.study.studyGroup.dto.DailyLogResponseDto;
import sw.study.studyGroup.service.DailyLogService;

import java.util.List;

@RestController
@RequestMapping("/api/studyGroup/{groupId}/dailyLog")
@RequiredArgsConstructor
@Tag(name = "StudyGroup_DailyLog", description = "그룹 내 데일리 로그")
public class DailyLogController implements DailyLogApiDocumentation {

    private final DailyLogService dailyLogService;

    // 데일리 로그 작성
    @Override
    @PostMapping("/create")
    public ResponseEntity<?> createDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody DailyLogRequestDto requestDto) {

        dailyLogService.createDailyLog(accessToken, groupId, requestDto.getTitle(), requestDto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body("데일리 로그가 성공적으로 작성되었습니다.");
    }

    // 데일리 로그 조회
    @Override
    @GetMapping("/list")
    public ResponseEntity<?> listOfDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size,
            @PathVariable("groupId") Long groupId,
            @RequestParam(name = "date") String date) {

        List<DailyLogResponseDto> logs = dailyLogService.listOfDailyLog(accessToken, page, size, groupId, date);
        return ResponseEntity.ok().body(logs); // 리스트 반환
    }

    // 데일리 로그 수정
    @Override
    @PutMapping("/update/{logId}")
    public ResponseEntity<?> updateDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("logId") Long logId,
            @RequestBody DailyLogRequestDto requestDto) {

        dailyLogService.updateDailyLog(accessToken, groupId, logId, requestDto.getTitle(), requestDto.getContent());
        return ResponseEntity.status(HttpStatus.OK).body("데일리 로그가 성공적으로 수정되었습니다.");

    }

    // 데일리 로그 삭제
    @Override
    @DeleteMapping("/delete/{logId}")
    public ResponseEntity<?> deleteDailyLog(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("logId") Long logId) {

        dailyLogService.deleteDailyLog(accessToken,groupId, logId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("데일리 로그가 성공적으로 삭제되었습니다.");
    }
}
