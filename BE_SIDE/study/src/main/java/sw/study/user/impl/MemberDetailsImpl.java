package sw.study.user.impl;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import sw.study.user.domain.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MemberDetailsImpl implements UserDetails {

    private final Member member;

    public MemberDetailsImpl(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    @Override // 권한 반환
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !member.isSuspended();
    } // 정지된 계정은 잠금된 것으로 처리

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !member.isDeleted();
    } // 삭제된 계정은 비활성화

}
