package pmb.user.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import pmb.user.ServiceTestRunner;
import pmb.user.dto.UserDto;

@ServiceTestRunner
class JwtTokenProviderTest {

  private static final String SIGNING_KEY = "secretkey";
  private static final String DUMMY_TOKEN = Jwts.builder()
      .setSubject("test")
      .addClaims(Map.of("apps", "weather,cook", "roles", "admin"))
      .signWith(SignatureAlgorithm.HS512, SIGNING_KEY)
      .compact();

  private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("secretkey", 2592_000);

  @Test
  void create() {
    UserDto user = new UserDto("test", "pwd", List.of("weather", "cook"), "admin");
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, "pwd");

    String created = jwtTokenProvider.create(token);

    Claims body = Jwts.parser().setSigningKey(SIGNING_KEY).parseClaimsJws(created).getBody();

    assertAll(
        () -> assertEquals("test", body.getSubject()),
        () -> assertTrue(DateUtils.isSameDay(new Date(), body.getIssuedAt())),
        () -> assertEquals("weather,cook", body.get("apps")),
        () -> assertEquals("admin", body.get("roles")),
        () -> assertNotNull(body.getExpiration()),
        () -> assertNotNull(body.get(Claims.ID)));
  }

  @Test
  void getUserName() {
    assertEquals("test", jwtTokenProvider.getUserName(DUMMY_TOKEN));
  }

  @Test
  void getAuthentication() {
    UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) jwtTokenProvider
        .getAuthentication(DUMMY_TOKEN);

    UserDto user = (UserDto) authentication.getPrincipal();
    assertAll(
        () -> assertEquals("test", user.getUsername()),
        () -> assertNull(user.getPassword()),
        () -> assertEquals("ROLE_admin", user.getAuthorities().getFirst().toString()),
        () -> assertEquals("ROLE_admin", user.getRole().toString()),
        () -> assertEquals(2, user.getApps().size()),
        () -> user.getApps().forEach(app -> assertTrue(user.getApps().contains(app))),
        () -> assertEquals(DUMMY_TOKEN, authentication.getCredentials()));
  }

  @Nested
  class IsValid {

    @Test
    void ok() {
      assertTrue(jwtTokenProvider.isValid(DUMMY_TOKEN));
    }

    @Test
    void fail() {
      assertFalse(jwtTokenProvider.isValid("test"));
    }
  }

  @Nested
  class GetCurrentUserLogin {

    @Test
    void when_null() {
      SecurityContextHolder.getContext().setAuthentication(null);
      assertTrue(JwtTokenProvider.getCurrentUserLogin().isEmpty());
    }

    @Test
    void when_default() {
      UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(5L, "pwd");
      SecurityContextHolder.getContext().setAuthentication(auth);
      assertTrue(JwtTokenProvider.getCurrentUserLogin().isEmpty());
    }

    @Test
    void when_user() {
      SecurityContextHolder.getContext()
          .setAuthentication(
              new UsernamePasswordAuthenticationToken(
                  new UserDto(
                      "test", "pwd", List.of("weather"), "admin"),
                  "pwd"));

      Optional<String> result = JwtTokenProvider.getCurrentUserLogin();

      assertAll(() -> assertTrue(result.isPresent()), () -> assertEquals("test", result.get()));
    }

    @Test
    void when_string() {
      SecurityContextHolder.getContext()
          .setAuthentication(new UsernamePasswordAuthenticationToken("test", "pwd"));

      Optional<String> result = JwtTokenProvider.getCurrentUserLogin();

      assertAll(() -> assertTrue(result.isPresent()), () -> assertEquals("test", result.get()));
    }
  }
}
