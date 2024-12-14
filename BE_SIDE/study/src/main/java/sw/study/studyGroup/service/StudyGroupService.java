package sw.study.studyGroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.config.jwt.JWTService;
import sw.study.exception.*;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.Participant.Role;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.domain.WaitingPeople;
import sw.study.studyGroup.dto.NicknameRequest;
import sw.study.studyGroup.dto.ParticipantsResponse;
import sw.study.studyGroup.dto.StudyGroupResponse;
import sw.study.studyGroup.repository.*;
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
    private final JWTService jwtService;

    // 토큰에서 사용자 이메일 정보 얻어서 Member 객체 가져오기
    private Member currentLogginedInfo(String accessToken) {
        String token = jwtService.extractToken(accessToken);
        String email = jwtService.extractEmail(token);

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 조회할 수 없습니다."));
    }

    // 닉네임을 통한 사용자 검색 ( 그룹 생성 시 / 생성 이후 신규 초대 모두 핸들링 )
    public List<String> searchByNickname(String accessToken, String nickname, int page, int size, Long groupId) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Member> members = memberRepository.findMembersByNicknameStartingWith(nickname, pageable);
        List<String> nicknames = new ArrayList<>();

        Member loggedUser = currentLogginedInfo(accessToken);
        String loggedUserNickname = loggedUser.getNickname();

        List<String> participants = new ArrayList<>(); // 방 생성 이후 (groupId 존재)

        if (groupId != null) {
            participants = participantRepository.findAllByStudyGroupId(groupId, pageable)
                    .stream()
                    .map(participant -> participant.getMember().getNickname())
                    .toList();
        }

        if (members.isEmpty()) {
            return Collections.emptyList();
            // 비어있는 리스트 반환 ( 검색된 결과가 없을 경우에 )
        }
        for (Member member : members) {
            // 로그인된 사용자  / 기존 참가자는 제외
            if (!loggedUserNickname.equals(member.getNickname())
                    && !participants.contains(member.getNickname()))
                nicknames.add(member.getNickname());
        }
        return nicknames;
    }

    // 스터디 그룹 생성 ( + 사용자 초대 )
    @Transactional
    public StudyGroup createStudyGroup(
            String accessToken, String groupName, String description, List<String> selectedNicknames, String leaderNickname) {

        // 스터디 그룹 생성 
        StudyGroup studyGroup = StudyGroup.createStudyGroup(groupName, description);
        studyGroupRepository.save(studyGroup);

        // 로그인 되어있는 사용자 정보를 가져오기
        Member leader = currentLogginedInfo(accessToken);

        // 방장은 바로 Participant에 추가해준다.
        Participant leaderParticipant = Participant.createParticipant(leaderNickname, leader, Role.LEADER, studyGroup);
        participantRepository.save(leaderParticipant);

        // 초대 대상자들을 한 번에 조회
        List<Member> members = memberRepository.findByNicknameIn(selectedNicknames);

        // 대기 명단에 추가
        List<WaitingPeople> waitingPeople = new ArrayList<>();

        for (Member member : members) {
            WaitingPeople waitingPerson = WaitingPeople.createWaitingPerson(member, studyGroup);
            waitingPeople.add(waitingPerson);
        }

        studyGroup.whoEverInvited(waitingPeople.size());
        waitingPeopleRepository.saveAll(waitingPeople);
        return studyGroup;
    }

    // 초대를 받은 스터디그룹 확인하기
    public List<StudyGroupResponse> checkInvited(String accessToken) {

        Member user = currentLogginedInfo(accessToken);

        // 대기 명단에서 로그인된 사용자의 정보만 따로 뺀 후에
        List<WaitingPeople> waitingPeople = waitingPeopleRepository.findByMemberId(user.getId());

        List<StudyGroupResponse> studyGroups = new ArrayList<>();

        // waitingPeople 의 StudyGroup 으로 초대받은 그룹 탐색
        for (WaitingPeople waitingPerson : waitingPeople) {

            StudyGroup studyGroup = waitingPerson.getStudyGroup(); // Exception 핸들링 불필요
            StudyGroupResponse groupInfo = StudyGroupResponse.createStudyGroupResponse(
                    studyGroup.getId(),
                    studyGroup.getName(),
                    studyGroup.getDescription(),
                    studyGroup.getMemberCount()
            );
            studyGroups.add(groupInfo);
        }
        return studyGroups;
    }

    // 참여중인 스터디 그룹 확인
    public List<StudyGroupResponse> checkJoined(String accessToken) {

        Member user = currentLogginedInfo(accessToken);

        // 얻은 user 객체로 Participant 테이블 확인
        List<Participant> Participants = participantRepository.findByMemberId(user.getId());

        List<StudyGroupResponse> studyGroups = new ArrayList<>();

        // participants 의 StudyGroup 으로 초대받은 그룹 탐색
        for (Participant participants : Participants) {
            StudyGroup studyGroup = participants.getStudyGroup(); // Exception 핸들링 불필요

            StudyGroupResponse groupInfo = StudyGroupResponse.createStudyGroupResponse(
                    studyGroup.getId(),
                    studyGroup.getName(),
                    studyGroup.getDescription(),
                    studyGroup.getMemberCount()
            );

            studyGroups.add(groupInfo);
        }
        return studyGroups;
    }

    //초대 수락
    @Transactional
    public void acceptInvitation(String accessToken, Long groupId, String nickname) {

        Member member = currentLogginedInfo(accessToken);
        waitingPeopleRepository.deleteByMemberId(member.getId());

        // 중복 확인
        if (participantRepository.findByNickname(nickname).isPresent()) {
            throw new BaseException(ErrorCode.DUPLICATE_NICKNAME);
        }

        // 그룹 존재 여부 확인
        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        // 스터디 그룹의 인원이 꽉 찬 경우
        if (studyGroup.getMemberCount() == 50) {
            throw new BaseException(ErrorCode.STUDYGROUP_FULL);
        }

        // 사용자가 이미 허용된 수 만큼의 그룹에 참가중이라면
        if (participantRepository.countByMemberId(member.getId()) == 20) {
            throw new BaseException(ErrorCode.MAX_STUDYGROUP);
        }

        Participant participant = Participant.createParticipant(nickname, member, Role.MEMBER, studyGroup);
        studyGroup.whoEverAccepted(participant);
        studyGroupRepository.save(studyGroup);
    }

    //초대 거절
    @Transactional
    public void rejectInvitation(String accessToken, Long groupId) {

        Member member = currentLogginedInfo(accessToken);

        WaitingPeople targetMember = waitingPeopleRepository.findByMemberIdAndStudyGroup_Id(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.WAITING_NOT_FOUND));

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        studyGroup.whoEverRejected(targetMember);
        waitingPeopleRepository.delete(targetMember);
    }

    //참가자 전체 리스트 확인
    public List<ParticipantsResponse> listOfEveryone(String accessToken, Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Page<Participant> participants = participantRepository.findAllByStudyGroupId(groupId, pageable);

        List<ParticipantsResponse> result = new ArrayList<>();

        // 응답 DTO ( 닉네임, 신분 )
        for (Participant p : participants) {
            result.add(ParticipantsResponse.createGroupParticipants(
                    p.getNickname(), p.getRole()));
        }
        return result;
    }

    // 운영진 리스트 확인 (방장 포함)
    public List<ParticipantsResponse> listOfManagers(String accessToken, Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Page<Participant> participants = participantRepository.findAllByStudyGroupIdAndRole(groupId, Role.MANAGER, pageable);

        List<ParticipantsResponse> result = new ArrayList<>();
        for (Participant p : participants) {
            result.add(ParticipantsResponse.createGroupParticipants(p.getNickname(), p.getRole()));
        }

        return result;
    }

    // 팀원 리스트 확인
    public List<ParticipantsResponse> listOfMembers(String accessToken, Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Page<Participant> participants = participantRepository.findAllByStudyGroupIdAndRole(groupId, Role.MEMBER, pageable);

        List<ParticipantsResponse> result = new ArrayList<>();
        for (Participant p : participants) {
            result.add(ParticipantsResponse.createGroupParticipants(p.getNickname(), p.getRole()));
        }

        return result;
    }

    // 그룹 내에서 초대된 리스트 확인
    public List<String> listOfWaiting(String accessToken, Long groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Role role = participant.getRole();

        if (role == Role.MEMBER) {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        Page<WaitingPeople> waitingList = waitingPeopleRepository.findByStudyGroup_Id(groupId, pageable);
        List<String> result = new ArrayList<>();

        for (WaitingPeople target : waitingList) {
            result.add(target.getMember().getNickname());
        }

        return result;
    }

    // 특정 사용자 초대 취소
    @Transactional
    public boolean cancelInvitation(String accessToken, Long groupId, String nickname) {

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Role role = participant.getRole();

        if (role == Role.MEMBER) {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        Member target = memberRepository.findByNickname(nickname)
                .orElseThrow(()-> new BaseException(ErrorCode.MEMBER_NOT_FOUND));

        WaitingPeople waitingPerson = waitingPeopleRepository.findByMemberIdAndStudyGroup_Id(target.getId(),groupId)
                .orElseThrow(()-> new BaseException(ErrorCode.WAITING_NOT_FOUND));

        waitingPeopleRepository.delete(waitingPerson);
        return true;
    }

    // 그룹 내 권한 변경
    @Transactional
    public void changeRole(String accessToken, Long groupId, String nickname) {

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Role role = participant.getRole();

        if (role == Role.MEMBER) {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        Participant target = participantRepository.findParticipantByNickname(nickname);

        if (target.getRole() == Role.MEMBER) {
            target.promote();
        } else if (target.getRole() == Role.MANAGER) {
            target.demote();
        }
    }

    // 그룹 내 닉네임 변경
    @Transactional
    public void changeParticipantNickname(String accessToken, Long groupId, String nickname) {

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        participantRepository.findByStudyGroupIdAndNickname(groupId, nickname)
                .ifPresent(isTaken-> {
                    throw new BaseException(ErrorCode.DUPLICATE_NICKNAME);
                });
        participant.updateNickname(nickname);
    }

    // 그룹 내 신규 초대
    @Transactional
    public void inviteNewMember(String accessToken, Long groupId, List<NicknameRequest> nicknameRequest) {

        Member member = currentLogginedInfo(accessToken);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        if (participant.getRole() == Role.MEMBER) {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        List<String> selectedNicknames = nicknameRequest.stream()
                .map(NicknameRequest::getNickname)
                .toList();

        List<Member> members = memberRepository.findByNicknameIn(selectedNicknames);

        // 대기 명단에 추가
        List<WaitingPeople> waitingPeopleList = new ArrayList<>();

        for (Member newbie : members) {
            WaitingPeople waitingPerson = WaitingPeople.createWaitingPerson(newbie, studyGroup);
            waitingPeopleList.add(waitingPerson);
        }

        studyGroup.whoEverInvited(waitingPeopleList.size());
        waitingPeopleRepository.saveAll(waitingPeopleList);
    }

    // 그룹 탈퇴
    @Transactional
    public void quitGroup(String accessToken, Long groupId) {

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        if (participant.getRole() == Role.LEADER) {
            throw new BaseException(ErrorCode.LEADER_CANNOT_LEAVE);
        }

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                        .orElseThrow(()->new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        studyGroup.getParticipants().remove(participant);
        studyGroup.whoEverQuit();
    }


    // 그룹 내 특정 사용자 추방
    @Transactional
    public void userKick(String accessToken, Long groupId, String nickname) {

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        if (participant.getRole() != Role.LEADER) {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(()->new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        Participant target = participantRepository.findByStudyGroupIdAndNickname(groupId, nickname)
                        .orElseThrow(()->new BaseException(ErrorCode.PARTICIPANT_NOT_FOUND));

        studyGroup.getParticipants().remove(target);
        studyGroup.whoEverKicked();
    }
}