package sw.study.studyGroup.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleListResponse {
    private Long scheduleId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    public ScheduleListResponse(Long scheduleId, String title, LocalDate startDate, LocalDate endDate) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
