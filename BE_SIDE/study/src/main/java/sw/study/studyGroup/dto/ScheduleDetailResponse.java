package sw.study.studyGroup.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ScheduleDetailResponse {
    private Long scheduleId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    public ScheduleDetailResponse(Long scheduleId, String title, String description, LocalDate startDate, LocalDate endDate) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}