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
import sw.study.exception.studyGroup.*;
import sw.study.studyGroup.domain.Participant;
import sw.study.studyGroup.domain.Participant.Role;
import sw.study.studyGroup.domain.StudyGroup;
import sw.study.studyGroup.domain.WaitingPeople;
import sw.study.studyGroup.dto.GroupParticipants;
import sw.study.studyGroup.dto.InvitedResponse;
import sw.study.studyGroup.dto.JoinedResponse;
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
        // accessToken 유효성 검사
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("[ERROR] 유효하지 않는 토큰 형식입니다.");
        }

        // "Bearer " 부분 제거 후 실제 토큰만 추출
        String token = accessToken.substring(7);

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

        List<String> existingParticipants = new ArrayList<>(); // 방 생성 이후 (groupId 존재)

        if (groupId != null) {
            existingParticipants = participantRepository.findAllByStudyGroupId(groupId)
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
                    && !existingParticipants.contains(member.getNickname()))
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
    public List<InvitedResponse> checkInvited(String accessToken) {

        Member user = currentLogginedInfo(accessToken);

        // 대기 명단에서 로그인된 사용자의 정보만 따로 뺀 후에
        List<WaitingPeople> waitingPeopleList = waitingPeopleRepository.findByMemberId(user.getId());

        List<InvitedResponse> invitedResponses = new ArrayList<>();

        // waitingPeople 의 StudyGroup 으로 초대받은 그룹 탐색
        for (WaitingPeople waitingPerson : waitingPeopleList) {

            StudyGroup studyGroup = waitingPerson.getStudyGroup(); // Exception 핸들링 불필요
            InvitedResponse groupInfo = InvitedResponse.createInvitedResponse(
                    studyGroup.getId(),
                    studyGroup.getName(),
                    studyGroup.getDescription(),
                    studyGroup.getMemberCount()
            );
            invitedResponses.add(groupInfo);
        }
        return invitedResponses;
    }

    // 참여중인 스터디 그룹 확인
    public List<JoinedResponse> checkJoined(String accessToken) {

        Member user = currentLogginedInfo(accessToken);

        // 얻은 user 객체로 Participant 테이블 확인
        List<Participant> Participants = participantRepository.findByMemberId(user.getId());

        List<JoinedResponse> joinedGroups = new ArrayList<>();

        // participants 의 StudyGroup 으로 초대받은 그룹 탐색
        for (Participant participants : Participants) {
            StudyGroup studyGroup = participants.getStudyGroup(); // Exception 핸들링 불필요

            JoinedResponse groupInfo = JoinedResponse.createJoinedResponse(
                    studyGroup.getId(),
                    studyGroup.getName(),
                    studyGroup.getDescription(),
                    studyGroup.getMemberCount()
            );

            joinedGroups.add(groupInfo);
        }
        return joinedGroups;
    }

    //초대 수락
    @Transactional
    public void acceptInvitation(String accessToken, Long groupId, String nickname) {
        Logger logger = LoggerFactory.getLogger(getClass());
        try {
            Member member = currentLogginedInfo(accessToken);
            waitingPeopleRepository.deleteByMemberId(member.getId());

            // 닉네임 중복확인
            Optional<Participant> participant = participantRepository.findByNickname(nickname);
            if (participant.isPresent()) {  // 닉네임이 존재하는 경우
                throw new BaseException(ErrorCode.DUPLICATE_NICKNAME);
            }

            // Optional을 사용하여 StudyGroup을 가져오고, 그룹이 없으면 예외 처리
            StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                    .orElseThrow(() -> new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

            // 스터디 그룹의 인원이 꽉 찼을 때
            if (studyGroup.getMemberCount() == 50) {
                throw new BaseException(ErrorCode.STUDYGROUP_FULL);
            }

            // 사용자가 이미 허용된 수 만큼의 그룹에 참가중이라면
            if (participantRepository.countByMemberId(member.getId()) == 20) {
                throw new BaseException(ErrorCode.MAX_STUDYGROUP);
            }

            Participant newParticipant = Participant.createParticipant(nickname, member, Role.MEMBER, studyGroup);
            studyGroup.whoEverAccepted(newParticipant);
            studyGroupRepository.save(studyGroup);
        } catch (Exception e) {
            // 예외 발생 시 로깅
            logger.error("초대 수락 중 오류 발생: ", e);
            throw e;  // 예외를 다시 던져서 위의 Controller에서 처리하도록 합니다.
        }
    }

    //초대 거절
    @Transactional
    public void rejectInvitation(String accessToken, Long groupId) {

        Member member = currentLogginedInfo(accessToken);

        // findByMember로 조회하고 지우면 큰일남.. 특정 사용자가 받은 초대 다 지워버림;
        Optional<WaitingPeople> targetMemberOptional = waitingPeopleRepository.findByMemberIdAndStudyGroup_Id(member.getId(), groupId);

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
    public List<GroupParticipants> listOfEveryone(String accessToken, Long groupId) {

        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        List<Participant> participants = participantRepository.findAllByStudyGroupId(groupId);

        List<GroupParticipants> result = new ArrayList<>();

        // 응답 DTO ( 닉네임, 신분 )
        for (Participant p : participants) {
            result.add(GroupParticipants.createGroupParticipants(
                    p.getNickname(), p.getRole()));
        }
        return result;
    }

    // 운영진 리스트 확인 (방장 포함)
    public List<GroupParticipants> listOfManagers(String accessToken, Long groupId) {

        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        List<Participant> participants = participantRepository.findAllByStudyGroupIdAndRole(groupId, Role.MANAGER);

        List<GroupParticipants> result = new ArrayList<>();
        for (Participant p : participants) {
            result.add(GroupParticipants.createGroupParticipants(p.getNickname(), p.getRole()));
        }

        return result;
    }

    // 팀원 리스트 확인
    public List<GroupParticipants> listOfMembers(String accessToken, Long groupId) {

        Member member = currentLogginedInfo(accessToken);

        participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        List<Participant> participants = participantRepository.findAllByStudyGroupIdAndRole(groupId, Role.MEMBER);

        List<GroupParticipants> result = new ArrayList<>();
        for (Participant p : participants) {
            result.add(GroupParticipants.createGroupParticipants(p.getNickname(), p.getRole()));
        }

        return result;
    }

    // 그룹 내에서 초대된 리스트 확인
    public List<String> listOfWaiting(String accessToken, Long groupId) {

        Member member = currentLogginedInfo(accessToken);

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        Role role = participant.getRole();

        if (role == Role.MEMBER) {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

        List<WaitingPeople> waitingList = waitingPeopleRepository.findByStudyGroup_Id(groupId);
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

        Optional<Member> target = memberRepository.findByNickname(nickname);

        if (target.isPresent()) {
            Optional<WaitingPeople> waitingPeopleOpt = waitingPeopleRepository.findByMemberIdAndStudyGroup_Id(target.get().getId(), groupId);

            if (waitingPeopleOpt.isPresent()) {
                waitingPeopleRepository.delete(waitingPeopleOpt.get());
                return true; // 초대 취소 처리
            }
        } else {
            throw new UserNotFoundException("해당 닉네임을 가진 사용자가 존재하지 않습니다.");
        }
        return false;
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

        Optional<Participant> isTaken = participantRepository.findByStudyGroupIdAndNickname(groupId, nickname);

        if (isTaken.isPresent()) throw new BaseException(ErrorCode.DUPLICATE_NICKNAME);

        participant.changedNickname(nickname);
    }

    // 그룹 내 신규 초대
    @Transactional
    public void inviteNewMember(String accessToken, Long groupId, List<String> selectedNicknames) {

        Member member = currentLogginedInfo(accessToken);

        StudyGroup studyGroup = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.STUDYGROUP_NOT_FOUND));

        Participant participant = participantRepository.findByMemberIdAndStudyGroupId(member.getId(), groupId)
                .orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED));

        if (participant.getRole() == Role.MEMBER) {
            throw new BaseException(ErrorCode.PERMISSION_DENIED);
        }

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

        studyGroupRepository.findById(groupId).ifPresent(studyGroup -> {
            studyGroup.getParticipants().remove(participant);
            //participantRepository.delete(participant);
            //양방향 리스트 : list.remove() 에 의해 고아가 되고 자동으로 삭제됨 (orphanRemoval)
            studyGroup.whoEverQuit();
        });
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

        studyGroupRepository.findById(groupId).ifPresent(
                studyGroup -> {
                    Optional<Participant> target = participantRepository.findByStudyGroupIdAndNickname(groupId, nickname);
                    target.ifPresent(value -> studyGroup.getParticipants().remove(value));
                    // 여러 닉네임을 여러 그룹에서 사용하는 경우 -> 수정 필요
                    // 이 또한 orphanRemoval 덕분에 repo.delete 할 필요 없다.
                    studyGroup.whoEverKicked();
                }
        );
    }
}