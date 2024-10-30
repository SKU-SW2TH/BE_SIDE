package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.domain.WaitingPeople;
import sw.study.studyGroup.dto.InvitedResponse;
import sw.study.studyGroup.dto.JoinedResponse;
import sw.study.studyGroup.repository.ParticipantRepository;
import sw.study.studyGroup.repository.StudyGroupRepository;
import sw.study.studyGroup.repository.WaitingPeopleRepository;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyGroupService {

    private final MemberRepository memberRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ParticipantRepository participantRepository;
    private final WaitingPeopleRepository waitingPeopleRepository;

    // 토큰에서 사용자 이메일 정보 얻어서 Member 객체 가져오기
    private Member currentLogginedInfo() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserEmail = userDetails.getUsername(); // 현재 사용자의 이메일

        return memberRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found")); // 해당 부분은 커스텀 예외 처리 필요
    }

    // 닉네임을 통한 사용자 검색 ( 그룹 생성 시 )
    public List<String> searchByNickname(String nickname, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Member> members = memberRepository.findMembersByNicknameStartingWith(nickname, pageable);

        List<String> nicknames = new ArrayList<>();

        String logginedUserNickname = currentLogginedInfo().getNickname();

        if(members.isEmpty()){
            return Collections.emptyList();
            // 비어있는 리스트 반환
        }
        for(Member member : members){
            if(!logginedUserNickname.equals(member.getNickname()))
                nicknames.add(member.getNickname());
        }
        return nicknames;
    }

    // 스터디 그룹 생성 ( + 사용자 초대 )
    public StudyGroup createStudyGroup(
            String groupName, String description, List<String> selectedNicknames, String leaderNickname){

        // 스터디 그룹 생성 
        StudyGroup studyGroup = StudyGroup.createStudyGroup(groupName, description);
        studyGroupRepository.save(studyGroup);

        // 로그인 되어있는 사용자 정보를 가져오기
        Member leader = currentLogginedInfo();

        // 방장은 바로 Participant에 추가해준다.
        Participant leaderParticipant = Participant.createParticipant(leaderNickname, leader, Participant.Role.LEADER);
        participantRepository.save(leaderParticipant);

        // 이후 초대 대상자들은 대기명단에 추가
        for(String nickname : selectedNicknames){
            
            Optional<Member> memberOptional = memberRepository.findByNickname(nickname);
            // 해당하는 닉네임을 가진 Member 객체를 찾아서
            if(memberOptional.isPresent()){
                Member member = memberOptional.get();
                WaitingPeople waitingPerson = WaitingPeople.createWaitingPerson(member,studyGroup);
                waitingPeopleRepository.save(waitingPerson);
                // 대기 명단에 추가
            }
        }
        return studyGroup;
    }

    // 초대를 받은 스터디그룹 확인하기
    public List <InvitedResponse> checkInvited(){

        Member user = currentLogginedInfo();

        // 대기 명단에서 로그인된 사용자의 정보만 따로 뺀 후에
        List<WaitingPeople> invitedGroups = waitingPeopleRepository.findByMemberId(user.getId());

        List<InvitedResponse> invitedResponses = new ArrayList<>();

        // waitingPeople 의 StudyGroup 으로 초대받은 그룹 탐색
        for(WaitingPeople waitingPerson : invitedGroups){
            Long groupId = waitingPerson.getStudyGroup().getId();
            StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("스터디그룹이 존재하지 않습니다.")); //별도 처리 필요

            InvitedResponse groupInfo = InvitedResponse.createInvitedResponse(
                    studyGroup.getName(),
                    studyGroup.getDescription(),
                    studyGroup.getMemberCount()
            );
            invitedResponses.add(groupInfo);
        }
        return invitedResponses;
    }

    // 참여중인 스터디 그룹 확인
    public List <JoinedResponse> checkJoined() {

        Member user = currentLogginedInfo();

        // 얻은 user 객체로 Participant 테이블 확인
        List<Participant> Participants = participantRepository.findByMemberId(user.getId());

        List<JoinedResponse> joinedGroups = new ArrayList<>();

        // waitingPeople 의 StudyGroup 으로 초대받은 그룹 탐색
        for (Participant participants : Participants) {
            Long groupId = participants.getStudyGroup().getId();
            StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("스터디그룹이 존재하지 않습니다.")); //별도 처리 필요
            JoinedResponse groupInfo = JoinedResponse.createJoinedResponse(
                    studyGroup.getName(),
                    studyGroup.getDescription(),
                    studyGroup.getMemberCount()
            );
            joinedGroups.add(groupInfo);
        }
        return joinedGroups;
    }
}
