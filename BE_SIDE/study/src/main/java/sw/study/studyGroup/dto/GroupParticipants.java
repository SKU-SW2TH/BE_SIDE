package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.Participant;

@Data
public class GroupParticipants {

    // 프로필 사진 이후 추가 필요
    private String nickname;
    private Participant.Role role;

    private GroupParticipants(String nickname, Participant.Role role){
        this.nickname = nickname;
        this.role = role;
    }

    public static GroupParticipants createGroupParticipants(String nickname, Participant.Role role){
        return new GroupParticipants(nickname,role);
    }
}
