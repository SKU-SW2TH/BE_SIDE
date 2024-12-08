package sw.study.studyGroup.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="schedule_id")
    private Long id; //PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="group_id", nullable = false)
    private StudyGroup studyGroup; //FK

    @Column(nullable = false)
    private String title; // 제목

    @Column(nullable = false)
    private String description; // 본문

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // 시작일

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // 종료일

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성 일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 수정 일시

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 삭제 일시

    @Column(name = "is_deleted")
    boolean isDeleted; // 삭제 여부

    public static Schedule createSchedule(
            StudyGroup studyGroup, String title, String description, LocalDate startDate, LocalDate endDate){
        Schedule schedule = new Schedule();
        schedule.studyGroup = studyGroup;
        schedule.title = title;
        schedule.description = description;
        schedule.startDate = startDate;
        schedule.endDate = endDate;
        schedule.createdAt = LocalDateTime.now();
        schedule.updatedAt = null;
        schedule.deletedAt = null;
        schedule.isDeleted = false;
        return schedule;
    }

    public void updateSchedule(String title, String description, LocalDate startDate, LocalDate endDate){
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }
}
