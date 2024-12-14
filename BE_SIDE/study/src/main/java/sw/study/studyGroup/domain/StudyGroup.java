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

    // 양방향 관계 설정을 위한 참가자 리스트
    @OneToMany(mappedBy = "studyGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    //초대를 했을 때
    public void whoEverInvited(int size){
        waitingCount += size;
    }

    // 초대를 수락했을 떄
    public void whoEverAccepted(Participant participant){
        participants.add(participant);
        this.memberCount++;
        this.waitingCount--;
    }

    // 초대를 거부했을 때
    public void whoEverRejected(WaitingPeople waitingPerson){
        this.waitingCount--;
    }

    // 그룹을 탈퇴했을 때
    public void whoEverQuit(){
        this.memberCount--;
    }

    public void whoEverKicked(){
        this.memberCount--;
    }

    public static StudyGroup createStudyGroup(String name, String description){
        StudyGroup group = new StudyGroup();
        group.name = name;
        group.description = description;
        group.memberCount = 1; // 우선 생성했을때는 기본적으로 방장 본인만 있는 상태
        group.waitingCount = 0;
        group.isDeleted = false;
        group.createdAt = LocalDateTime.now();
        group.updatedAt = null;
        return group;
    }
}
