package sw.study.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.security.Key;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sw.study.user.util.RedisUtil;

@Slf4j
@Component
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24;            // 1일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일
    private static final long PASSWORD_RESET_TOKEN_EXPIRE_TIME = 1000 * 60 * 10;// 10분

    private final Key key;
    private final RedisUtil redisUtil;

    public TokenProvider(@Value("${jwt.secret}") String secretKey, RedisUtil redisUtil) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
    }

    public TokenDTO generateTokenDTO(Authentication authentication) {
        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        //System.out.println("Authentication Name: " + authentication.getName());
        //System.out.println("Authorities: " + authorities);

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())       // payload "sub": "name"
                .claim(AUTHORITIES_KEY, authorities)        // payload "auth": "ROLE_USER"
                .setExpiration(accessTokenExpiresIn)        // payload "exp": 1516239022 (예시)
                .signWith(key, SignatureAlgorithm.HS512)    // header "alg": "HS512"
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder().
                setSubject(authentication.getName())       // payload "sub": "name"
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .claim("isRefreshToken", true)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenDTO.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    public TokenDTO reissueAccessToken(String refreshToken) {
        // 리프레시 토큰에서 사용자 정보 추출 -> 클레임 확인
        Claims claims = parseClaims(refreshToken);

        // Refresh Token 검증 및 클레임에서 Refresh Token 여부 확인
        if (!validateToken(refreshToken) || claims.get("isRefreshToken") == null || !Boolean.TRUE.equals(claims.get("isRefreshToken"))) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        String email = claims.getSubject();
        String authorities = claims.get(AUTHORITIES_KEY).toString();

        String newAccessToken = generateAccessToken(email, authorities);
        String newRefreshToken = generateRefreshToken(email, authorities);

        String refreshTokenKey = "RT:" + email;
        String accessTokenKey = "AT:" + email;

        // 기존의 accessToken을 Redis에서 삭제
        redisUtil.delete(accessTokenKey);

        // 기존의 refreshToken 삭제
        redisUtil.delete(refreshTokenKey);

        redisUtil.setData(refreshTokenKey, newRefreshToken, REFRESH_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

        // Access Token도 Redis에 저장
        redisUtil.setData(accessTokenKey, newAccessToken, ACCESS_TOKEN_EXPIRE_TIME, TimeUnit.MILLISECONDS);

        return TokenDTO.builder()
                .grantType(BEARER_TYPE)
                .accessToken(newAccessToken)
                .accessTokenExpiresIn(new Date((new Date()).getTime() + ACCESS_TOKEN_EXPIRE_TIME).getTime())
                .refreshToken(newRefreshToken)
                .build();
    }

    private String generateAccessToken(String email, String authorities) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateRefreshToken(String email, String authorities) {
        long now = (new Date()).getTime();
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .claim("isRefreshToken", true) // refreshToken 임을 나타내는 클레임 추가
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        if (isTokenBlacklisted(accessToken)) {
            throw new RuntimeException("블랙리스트에 등록된 토큰입니다.");
        }

        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        // 권한 정보가 없을 경우 예외 처리
        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        String authoritiesString = claims.get(AUTHORITIES_KEY).toString();

        if (authoritiesString.isEmpty()) {
            throw new RuntimeException("권한 정보가 비어 있습니다.");
        }

        // 권한 문자열을 사용하여 GrantedAuthority 객체 생성
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(authoritiesString.split(","))
                        .filter(authority -> !authority.trim().isEmpty()) // 빈 문자열 필터링
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        if (authorities.isEmpty()) {
            throw new RuntimeException("권한 정보 비어 있습니다.");
        }

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    public boolean isTokenBlacklisted(String accessToken) {
        String blacklistKey = "BlackList_" + accessToken;
        return redisUtil.hasKeyBlackList(blacklistKey); // 블랙리스트에 존재하는지 체크
    }

    public String generatePasswordResetToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + PASSWORD_RESET_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(email) // 복호화에 사용한다.
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
