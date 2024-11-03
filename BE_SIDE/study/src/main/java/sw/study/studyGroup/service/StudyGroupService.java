package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sw.study.exception.*;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.domain.WaitingPeople;
import sw.study.studyGroup.dto.GroupParticipants;
import sw.study.studyGroup.dto.InvitedResponse;
import sw.study.studyGroup.dto.JoinedResponse;
import sw.study.studyGroup.repository.ParticipantRepository;
import sw.study.studyGroup.repository.StudyGroupRepository;
import sw.study.studyGroup.repository.WaitingPeopleRepository;
import sw.study.user.domain.Member;
import sw.study.user.repository.MemberRepository;

import javax.swing.*;
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
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 닉네임을 통한 사용자 검색 ( 그룹 생성 시 )
    public List<String> searchByNickname(String nickname, int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Member> members = memberRepository.findMembersByNicknameStartingWith(nickname, pageable);

        List<String> nicknames = new ArrayList<>();

        String loggedUserNickname = currentLogginedInfo().getNickname();

        if(members.isEmpty()){
            return Collections.emptyList();
            // 비어있는 리스트 반환
        }
        for(Member member : members){
            if(!loggedUserNickname.equals(member.getNickname()))
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

        // 초대 대상자들을 한 번에 조회
        List<Member> members = memberRepository.findByNicknameIn(selectedNicknames);

        // 대기 명단에 추가
        List<WaitingPeople> waitingPeopleList = new ArrayList<>();

        for (Member member : members) {
            WaitingPeople waitingPerson = WaitingPeople.createWaitingPerson(member, studyGroup);
            waitingPeopleList.add(waitingPerson);
        }

        studyGroup.whoEverInvited(waitingPeopleList.size());
        waitingPeopleRepository.saveAll(waitingPeopleList);
        return studyGroup;
    }

    // 초대를 받은 스터디그룹 확인하기
    public List <InvitedResponse> checkInvited(){

        Member user = currentLogginedInfo();

        // 대기 명단에서 로그인된 사용자의 정보만 따로 뺀 후에
        List<WaitingPeople> waitingPeopleList = waitingPeopleRepository.findByMemberId(user.getId());

        List<InvitedResponse> invitedResponses = new ArrayList<>();

        // waitingPeople 의 StudyGroup 으로 초대받은 그룹 탐색
        for(WaitingPeople waitingPerson : waitingPeopleList){

           StudyGroup studyGroup = waitingPerson.getStudyGroup(); // Exception 핸들링 불필요
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

        // participants 의 StudyGroup 으로 초대받은 그룹 탐색
        for (Participant participants : Participants) {
            StudyGroup studyGroup = participants.getStudyGroup(); // Exception 핸들링 불필요

            JoinedResponse groupInfo = JoinedResponse.createJoinedResponse(
                    studyGroup.getName(),
                    studyGroup.getDescription(),
                    studyGroup.getMemberCount()
            );

            joinedGroups.add(groupInfo);
        }
        return joinedGroups;
    }

    //초대 수락
    public void acceptInvitation(long groupId, String Nickname){

        Member member = currentLogginedInfo();
        waitingPeopleRepository.deleteByMemberId(member.getId());

        // 닉네임 중복확인
        if(participantRepository.findByNickname(Nickname)){
            throw new DuplicateNicknameException("해당 닉네임은 이미 사용중입니다.");
        }

        Optional<StudyGroup> studyGroup = studyGroupRepository.findById(groupId);

        // 스터디 그룹의 인원이 꽉 찼을 때
        studyGroup.ifPresent(group -> {
            if (group.getMemberCount() == 50) {
                throw new StudyGroupFullException("해당 스터디 그룹은 이미 가득 찬 상태입니다.");
            }
        });

        // 사용자가 이미 허용된 수 만큼의 그룹에 참가중이라면
        if(participantRepository.countByMemberId(member.getId())==20)
            throw new MaxStudyGroupException("더 이상 스터디그룹에 참가할 수 없습니다.");

        Participant newParticipant = Participant.createParticipant(Nickname, member, Participant.Role.MEMBER);
        studyGroup.ifPresent(group -> {
            group.whoEverAccepted(newParticipant);
            studyGroupRepository.save(group);
        });
    }

    //초대 거절
    public void rejectInvitation(long groupId){

        Member member = currentLogginedInfo();

        // findByMember로 조회하고 지우면 큰일남.. 특정 사용자가 받은 초대 다 지워버림;
        Optional<WaitingPeople> targetMemberOptional = waitingPeopleRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId);

        if (targetMemberOptional.isPresent()) {
            WaitingPeople targetMember = targetMemberOptional.get();

            Optional<StudyGroup> studyGroup = studyGroupRepository.findById(groupId);
            if (studyGroup.isPresent()) {
                studyGroup.get().whoEverRejected(targetMember);
                // 특정 그룹의 초대만 삭제해야됨.
                waitingPeopleRepository.delete(targetMember);
            }
        }
    }

    //참가자 전체 리스트 확인
    public List<GroupParticipants> listOfEveryone(long groupId){

        Member member = currentLogginedInfo();

        participantRepository.findByMemberIdAndGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("비정상적인 접근입니다."));

        List<Participant> participants = participantRepository.findAllByGroupId(groupId);

        List<GroupParticipants> result = new ArrayList<>();

        // 응답 DTO ( 닉네임, 신분 )
        for (Participant p : participants) {
            result.add(GroupParticipants.createGroupParticipants(
                    p.getNickname(), p.getRole()));
        }
        return result;
    }

    // 운영진 리스트 확인 (방장 포함)
    public List<GroupParticipants> listOfManagers(long groupId){

        Member member = currentLogginedInfo();

        participantRepository.findByMemberIdAndGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("비정상적인 접근입니다."));

        List<Participant> participants = participantRepository.findAllByGroupIdAndRole(groupId, Participant.Role.MANAGER);

        List<GroupParticipants> result = new ArrayList<>();
        for (Participant p : participants) {
            result.add(GroupParticipants.createGroupParticipants(p.getNickname(), p.getRole()));
        }

        return result;
    }

    // 팀원 리스트 확인
    public List<GroupParticipants> listOfMembers(long groupId){

        Member member = currentLogginedInfo();

        participantRepository.findByMemberIdAndGroupId(member.getId(), groupId)
                .orElseThrow(() -> new UnauthorizedException("비정상적인 접근입니다."));

        List<Participant> participants = participantRepository.findAllByGroupIdAndRole(groupId, Participant.Role.MEMBER);

        List<GroupParticipants> result = new ArrayList<>();
        for (Participant p : participants) {
            result.add(GroupParticipants.createGroupParticipants(p.getNickname(), p.getRole()));
        }

        return result;
    }
}
