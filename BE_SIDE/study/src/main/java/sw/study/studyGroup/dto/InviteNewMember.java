package sw.study.studyGroup.dto;

import lombok.Data;
import sw.study.studyGroup.domain.Participant;

import java.util.List;

@Data
public class InviteNewMember {
    private List<String> selectedNicknames;
}
