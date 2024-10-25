package sw.study.studyGroup.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id; // 그룹 ID

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "leader_id") // 방장 ID
    //private Member leader;  // FK (회원 테이블 참조)

    @Column(name = "room_name", nullable = false)
    private String name; // 방 이름

    private String description; // 설명

    @Column(name = "member_count")
    private int memberCount; // 멤버수

    @Column(name = "waiting_count")
    private int waitingCount; // 대기 수

    @Column(name = "is_deleted")
    private boolean isDeleted; // 삭제

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 생성날짜

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    // 해당 엔티티 내부에서의 수정 사항 -> 방 이름 / 설명 / 멤버수, 대기수 변환과 관련

    /*
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // 삭제날짜 -> 수정에 포함되서 지우기
    */

    // 양방향 관계 설정을 위한 참가자 리스트
    @OneToMany(mappedBy = "studyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    public void whoEverInvited(){
        this.waitingCount++; // 초대했을때
    }
    
    public void whoEverAccepted(){
        this.memberCount++; // 초대를 수락했을시
    }

    public static StudyGroup createStudyGroup(String name, String description){
        StudyGroup group = new StudyGroup();
        group.name = name;
        group.description = description;
        group.memberCount = 1; // 우선 생성했을때는 기본적으로 방장 본인만 있는 상태
        group.waitingCount = 0;
        group.isDeleted = false;
        group.createdAt = LocalDateTime.now();
        group.updatedAt = LocalDateTime.now();
        return group;
    }
}
