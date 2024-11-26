package sw.study.studyGroup.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.exception.studyGroup.NoticeNotFoundException;
import sw.study.exception.studyGroup.UnauthorizedException;
import sw.study.studyGroup.apiDoc.NoticeApiDocumentation;
import sw.study.studyGroup.dto.NoticeRequestDto;
import sw.study.studyGroup.dto.NoticeResponseDto;
import sw.study.studyGroup.service.NoticeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/studyGroup/{groupId}/notice")
@RequiredArgsConstructor
@Tag(name = "StudyGroup_Notice", description = "그룹 내 공지사항 관련")
public class NoticeController implements NoticeApiDocumentation {

    private final NoticeService noticeService;

    // 공지사항 작성
    @Override
    @PostMapping("/create")
    public ResponseEntity<?> createNotice(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestBody NoticeRequestDto requestDto) {

        noticeService.createNotice(accessToken, groupId, requestDto.getTitle(), requestDto.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body("공지사항이 성공적으로 작성되었습니다.");
    }

    // 공지사항 목록 조회
    @Override
    @GetMapping("/list")
    public ResponseEntity<?> listOfNotices(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size) {

        List<NoticeResponseDto> notices = noticeService.listOfNotice(accessToken, groupId, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "조회에 성공하였습니다.");
        response.put("data", notices);

        return ResponseEntity.ok().body(response);
    }

    // 특정 게시글 상세 조회
    @Override
    @GetMapping("/{noticeId}")
    public ResponseEntity<?> noticeDetail(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("noticeId") Long noticeId) {

        NoticeResponseDto notice = noticeService.noticeDetail(accessToken, groupId, noticeId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "조회에 성공하였습니다.");
        response.put("data", notice);

        return ResponseEntity.ok().body(response);
    }

    // 공지사항 수정
    @Override
    @PutMapping("/update/{noticeId}")
    public ResponseEntity<?> updateNotice(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("noticeId") Long noticeId,
            @RequestBody NoticeRequestDto noticeRequestDto) {

        noticeService.updateNotice(accessToken,groupId, noticeId, noticeRequestDto.getTitle(),noticeRequestDto.getContent());
        return ResponseEntity.status(HttpStatus.OK).body("공지사항이 성공적으로 수정되었습니다.");
    }

    // 공지사항 삭제
    @Override
    @DeleteMapping("/delete/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable("groupId") Long groupId,
            @PathVariable("noticeId") Long noticeId) {

        noticeService.deleteNotice(accessToken, groupId, noticeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("공지사항이 성공적으로 삭제되었습니다.");
    }
}
