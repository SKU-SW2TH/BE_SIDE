package sw.study.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하는 Lombok 애노테이션
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization"; // HTTP 요청의 Authorization 헤더 이름
    public static final String BEARER_PREFIX = "Bearer "; // Bearer 토큰의 접두사

    private final TokenProvider tokenProvider; // JWT 토큰을 처리하는 TokenProvider 인스턴스

    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // 1. Request Header에서 토큰을 꺼냄
        String jwt = resolveToken(request);

        // 2. validateToken으로 토큰 유효성 검사
        // 정상 토큰이면 해당 토큰으로 Authentication을 가져와서 SecurityContext에 저장
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            Authentication authentication = tokenProvider.getAuthentication(jwt); // 토큰에서 인증 정보를 가져옴
            SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContext에 인증 정보를 저장
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청을 전달
    }

    // Request Header에서 토큰 정보를 꺼내오기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // Authorization 헤더에서 Bearer 토큰을 가져옴
        // Bearer 접두사가 있고, 토큰이 존재하면 접두사를 제외한 토큰 문자열을 반환
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7); // Bearer 접두사(7자) 이후의 문자열 반환
        }
        return null; // 토큰이 없거나 유효하지 않은 경우 null 반환
    }
}
