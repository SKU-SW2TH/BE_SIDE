package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.Participant;

@Data
public class ParticipantsResponse {

    private String nickname;
    private Participant.Role role;
    private String profileImg;

    private ParticipantsResponse(String nickname, Participant.Role role, String profileImg){
        this.nickname = nickname;
        this.role = role;
        this.profileImg = profileImg;
    }

    public static ParticipantsResponse createGroupParticipants(String nickname, Participant.Role role, String profileImg){
        return new ParticipantsResponse(nickname,role, profileImg);
    }
}
