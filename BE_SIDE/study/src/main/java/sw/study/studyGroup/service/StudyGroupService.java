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

    // 닉네임을 통한 사용자 검색 ( 그룹 생성 시 )
    // 탐색의 대상은 커뮤니티 사이드에서 사용하는 닉네임 ( default )
    public List<String> searchByNickname(String nickname, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Member> members = memberRepository.findMembersByNickname(nickname, pageable);
        List<String> nicknames = new ArrayList<>();

        if(members.isEmpty()){
            return Collections.emptyList();
            // 비어있는 리스트 반환
        }
        for(Member member : members){
            nicknames.add(member.getNickname());
        }
        return nicknames;
    }

    // 스터디 그룹 생성 ( 사용자 초대 )
    public StudyGroup createStudyGroup(
            String groupName, String description, List<String> selectedNicknames, String leaderNickname){

        // 스터디 그룹 생성 
        StudyGroup studyGroup = StudyGroup.createStudyGroup(groupName, description);
        studyGroupRepository.save(studyGroup);

        // 로그인 되어있는 사용자 정보를 가져오기
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserEmail = userDetails.getUsername(); // 현재 사용자의 이메일
        Member leader = memberRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found")); // 해당 부분은 커스텀 예외 처리 필요

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
            else{
                // 여기서는 어떻게 처리해줘야할지 모르겠음..
                // 비어있는 방을 우선적으로 만들고 이후에 별도 초대를 할 수도 있음.
            }
        }
        return studyGroup;
    }

}
