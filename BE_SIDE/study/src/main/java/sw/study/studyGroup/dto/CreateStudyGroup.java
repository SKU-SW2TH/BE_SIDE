package sw.study.studyGroup.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateStudyGroup {
    private String groupName;
    private String Description;
    private List<String> selectedNicknames;
    private String leaderNickname;
}
