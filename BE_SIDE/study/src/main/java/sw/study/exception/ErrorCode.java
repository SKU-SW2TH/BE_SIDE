package sw.study.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // ENUM 상수( 서버 응답 코드, 에러 이름, 메시지 )를 정의한다.
    // 각자 정의하신거 써주시면 됩니다. ( 스터디그룹 외에 다른 쪽에서 선언됬는데 같이 쓰고있는거 한 두개 적었습니다. )
    
    // 스터디그룹 관련 예외 - 온유
    STUDYGROUP_NOT_FOUND(HttpStatus.NOT_FOUND,"STUDYGROUP_NOT_FOUND", "해당하는 스터디그룹이 존재하지 않습니다."),
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND,"NOTICE_NOT_FOUND", "해당하는 공지사항이 존재하지 않습니다."),
    DAILYLOG_NOT_FOUND(HttpStatus.NOT_FOUND,"DAILYLOG_NOT_FOUND","해당하는 데일리로그가 존재하지 않습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "해당하는 일정이 존재하지 않습니다."),
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND, "WAITING_NOT_FOUND", "해당하는 그룹에 초대를 받은 상태가 아닙니다."),
    PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "WAITING_NOT_FOUND", "그룹 내 해당하는 참가자가 존재하지 않습니다."),

    MAX_STUDYGROUP(HttpStatus.FORBIDDEN,"MAX_STUDYGROUP","더 이상 스터디그룹에 참가할 수 없습니다."),
    UNAUTHORIZED(HttpStatus.FORBIDDEN,"UNAUTHORIZED","해당 스터디그룹에 참여중이지 않습니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN,"PERMISSION_DENIED","운영진 권한을 가지고 있지 않습니다."),
    LEADER_CANNOT_LEAVE(HttpStatus.FORBIDDEN,"LEADER_CANNOT_LEAVE","리더는 그룹 탈퇴를 하기 위해 권한 위임이 필요합니다."),
    STUDYGROUP_FULL(HttpStatus.BAD_REQUEST,"STUDYGROUP_FULL","해당 스터디그룹은 이미 가득 찬 상태입니다."),

    // 그 외
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 에러가 발생했습니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,"DUPLICATE_NICKNAME","이미 사용중인 닉네임입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND,"MEMBER_NOT_FOUND","해당 사용자가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
