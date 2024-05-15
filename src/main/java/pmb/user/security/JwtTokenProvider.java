package pmb.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import pmb.user.dto.UserDto;

/** Jwt token utils class. */
@Component
public class JwtTokenProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

  private static final String JWT_APPS_KEY = "apps";
  private static final String JWT_ROLE_KEY = "roles";
  private static final String SPRING_ROLE_PREFIX = "ROLE_";
  private final String secretKey;
  private final Integer tokenDuration;

  public JwtTokenProvider(
      @Value("${jwt.secretkey}") String secretKey,
      @Value("${jwt.duration}") Integer tokenDuration) {
    this.secretKey = secretKey;
    this.tokenDuration = tokenDuration;
  }

  /**
   * Creates a token with authenticated user data.
   *
   * @param authentication user data
   * @return a token
   */
  public String create(Authentication authentication) {
    Date issuedAt = new Date();
    UserDto user = (UserDto) authentication.getPrincipal();
    return Jwts.builder()
        .setSubject(user.getUsername())
        .claim(JWT_APPS_KEY, StringUtils.join(user.getApps(), ","))
        .claim(
            JWT_ROLE_KEY,
            user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")))
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(issuedAt)
        .setExpiration(DateUtils.addSeconds(issuedAt, tokenDuration))
        .compact();
  }

  /**
   * Extracts user's name from given token
   *
   * @param token a jwt token
   * @return its username
   */
  public String getUserName(String token) {
    return parseToken(token).getBody().getSubject();
  }

  /**
   * Validates a jwt token.
   *
   * @param token to validate
   * @return {@code true} token valid, {@code false} otherwise
   */
  public boolean isValid(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      LOGGER.debug("Invalid token", e);
      SecurityContextHolder.clearContext();
      return false;
    }
  }

  /**
   * Return an Authentication object of the given token.
   *
   * @param token containing valid credential user
   * @return a {@link UsernamePasswordAuthenticationToken} authenticated
   */
  public Authentication getAuthentication(String token) {
    Claims claims = parseToken(token).getBody();
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
        SPRING_ROLE_PREFIX + claims.get(JWT_ROLE_KEY, String.class));
    return new UsernamePasswordAuthenticationToken(
        new UserDto(
            claims.getSubject(),
            null,
            List.of(claims.get(JWT_APPS_KEY, String.class).split(",")),
            authority.toString()),
        token,
        List.of(authority));
  }

  /**
   * Extracts from Spring Security session the authenticated user's name.
   *
   * @return a username
   */
  public static Optional<String> getCurrentUserLogin() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Authentication authentication = securityContext.getAuthentication();
    if (null == authentication) {
      return Optional.empty();
    }
    if (authentication.getPrincipal() instanceof UserDetails p) {
      return Optional.of(p.getUsername());
    }
    if (authentication.getPrincipal() instanceof String p) {
      return Optional.of(p);
    }
    return Optional.empty();
  }

  private Jws<Claims> parseToken(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
  }
}
