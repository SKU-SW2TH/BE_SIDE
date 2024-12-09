package sw.study.studyGroup.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
