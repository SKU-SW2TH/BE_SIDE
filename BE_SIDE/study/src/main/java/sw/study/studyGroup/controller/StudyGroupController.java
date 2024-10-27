package sw.study.studyGroup.controller;

import lombok.RequiredArgsConstructor;
import org.hibernate.internal.util.collections.JoinedList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.dto.CreateStudyGroup;
import sw.study.studyGroup.dto.InvitedResponse;
import sw.study.studyGroup.dto.JoinedResponse;
import sw.study.studyGroup.service.StudyGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/studyGroup")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping("/searchMembers") // 사용자 검색
    public ResponseEntity<List<String>> searchMembers(
            @RequestParam String nickname,
            @RequestParam int page,
            @RequestParam int size) {
        List<String> results = studyGroupService.searchByNickname(nickname,page,size);

        // 검색되는거 없어도 -> 404 리턴하지 말고 204 리턴 ( 성공 - noContent )
        if(results.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(results);
        // 예외 처리 해줘야함 ( 본인은 검색되지 않아야 함. )
        // 프론트에서도 방지가 가능하나, 백엔드에서도 별도로 처리를 해줘야 할듯함.
    }

    @PostMapping("/create") // 스터디 그룹 생성
    public ResponseEntity<StudyGroup> createStudyGroup(
            @RequestBody CreateStudyGroup requestDto) {

        StudyGroup createdGroup = studyGroupService.createStudyGroup(
                requestDto.getGroupName(),
                requestDto.getDescription(),
                requestDto.getSelectedNicknames(),
                requestDto.getLeaderNickname()
        );

        // 예외처리에 대한 부분 추가 필요.
        // E.G ) selected Nickname 에 본인이 포함되면 안됨?
        // 본인은 바로 추가해야되잖아. 조금 더 생각해보기로. 머리 안돌아감
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
    }

    @GetMapping("/invitedList")
    public ResponseEntity<List<InvitedResponse>> checkInvitedList(){
        List<InvitedResponse> invitedResponses = studyGroupService.checkInvited();
        return ResponseEntity.ok(invitedResponses);
        // 리스트가 비어있을 경우 예외처리 필요.
    }

    @GetMapping("/joinedList")
    public ResponseEntity<List<JoinedResponse>> checckJoinedList(){
        List<JoinedResponse> joinedResponses = studyGroupService.checkJoined();
        return ResponseEntity.ok(joinedResponses);
        // 초대받은 리스트가 없는 경우 -> 별도의 exception 처리해줘야
        // 빈 배열을 return 해도 프론트에서 핸들링 자체는 가능할듯 보임.
    }

}
