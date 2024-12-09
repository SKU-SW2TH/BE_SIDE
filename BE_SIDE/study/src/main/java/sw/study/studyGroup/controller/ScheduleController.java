package sw.study.studyGroup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.apiDoc.ScheduleApiDocumentation;
import sw.study.studyGroup.dto.*;
import sw.study.studyGroup.service.ScheduleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studyGroup/{groupId}/schedule")
@RequiredArgsConstructor
@Tag(name = "StudyGroup_Schedule", description = "그룹 내 일정 관련")
public class ScheduleController implements ScheduleApiDocumentation {

    private final ScheduleService scheduleService;

    // 일정 생성
    @Override
    @PostMapping("/create")
    public ResponseEntity<?> createSchedule(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody ScheduleRequest requestDto) {

        scheduleService.createSchedule(
                accessToken,
                groupId,
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getStartDate(),
                requestDto.getEndDate());

        return ResponseEntity.status(HttpStatus.CREATED).body("일정이 성공적으로 등록되었습니다.");
    }
    
    // 1달 전체 일정 조회
    @Override
    @GetMapping("/list")
    public ResponseEntity<?> listOfSchedules(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month) {

        List<ScheduleListResponse> schedules = scheduleService.getScheduleList(accessToken, groupId, year, month);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "조회에 성공하였습니다.");
        response.put("data", schedules);

        return ResponseEntity.ok().body(response);
    }
    
    // 특정 일정 상세
    @Override
    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> scheduleDetail(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("scheduleId") Long scheduleId) {

        ScheduleDetailResponse schedule =scheduleService.getScheduleDetails(accessToken,groupId,scheduleId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "조회에 성공하였습니다.");
        response.put("data", schedule);

        return ResponseEntity.ok().body(response);
    }

    // 일정 수정
    @Override
    @PutMapping("/update/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody ScheduleRequest scheduleRequest) {

        scheduleService.updateSchedule(
                accessToken,
                scheduleId,
                scheduleRequest.getTitle(),
                scheduleRequest.getDescription(),
                scheduleRequest.getStartDate(),
                scheduleRequest.getEndDate());

        return ResponseEntity.status(HttpStatus.OK).body("일정이 성공적으로 수정되었습니다.");
    }

    // 일정 삭제
    @Override
    @DeleteMapping("/delete/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("scheduleId") Long scheduleId) {

        scheduleService.deleteSchedule(accessToken, scheduleId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("일정이 성공적으로 삭제되었습니다.");
    }
}
