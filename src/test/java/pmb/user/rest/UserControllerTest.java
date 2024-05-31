package pmb.user.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import pmb.user.TestUtils;
import pmb.user.dto.JwtTokenDto;
import pmb.user.dto.PasswordDto;
import pmb.user.dto.UserDto;
import pmb.user.exception.AlreadyExistException;
import pmb.user.security.JwtTokenProvider;
import pmb.user.security.MyUserDetailsService;
import pmb.user.security.SecurityConfig;
import pmb.user.service.UserService;

@ActiveProfiles("test")
@Import({ JwtTokenProvider.class, SecurityConfig.class })
@MockBean(MyUserDetailsService.class)
@WebMvcTest(controllers = UserController.class)
@DisplayNameGeneration(value = ReplaceUnderscores.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private UserService userService;
  private static final UserDto DUMMY_USER = new UserDto("test", "password", List.of("weather", "cook"), null);
  private static final PasswordDto DUMMY_PASSWORD = new PasswordDto("password", "password2");
  private static final JwtTokenDto DUMMY_TOKEN = new JwtTokenDto("jwtToken");

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(userService);
  }

  @Nested
  class Signin {

    @ParameterizedTest(name = "Given valid user #{index} when login then ok")
    @MethodSource("pmb.user.rest.UserControllerTest$Signin#validUser")
    void ok(UserDto user) throws Exception {
      ArgumentCaptor<UserDto> userCaptor = ArgumentCaptor.forClass(UserDto.class);

      when(userService.login(any())).thenReturn(DUMMY_TOKEN);

      assertEquals(
          DUMMY_TOKEN.token(),
          objectMapper
              .readValue(
                  TestUtils.readResponse.apply(
                      mockMvc
                          .perform(
                              post("/users/signin")
                                  .content(objectMapper.writeValueAsString(user))
                                  .contentType(MediaType.APPLICATION_JSON_VALUE))
                          .andExpect(status().isOk())),
                  JwtTokenDto.class)
              .token());

      verify(userService).login(userCaptor.capture());

      UserDto signin = userCaptor.getValue();
      assertAll(
          () -> assertEquals(user.getUsername(), signin.getUsername()),
          () -> assertEquals(user.getPassword(), signin.getPassword()),
          () -> assertTrue(signin.getAuthorities().isEmpty()));
    }

    @ParameterizedTest(name = "Given invalid user #{index} when login then bad request")
    @MethodSource("pmb.user.rest.UserControllerTest$Signin#invalidUser")
    void when_failed_validation_then_bad_request(UserDto user) throws Exception {
      mockMvc
          .perform(
              post("/users/signin")
                  .content(objectMapper.writeValueAsString(user))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$", containsString("Validation failed for argument [0]")))
          .andExpect(jsonPath("$", not(matchesPattern(".*with [2-9]+ errors.*"))));

      verify(userService, never()).login(any());
    }

    static Stream<Arguments> invalidUser() {
      return Stream.of(
          arguments(new UserDto(null, "password", List.of("weather"), null)),
          arguments(new UserDto("", "password", List.of("weather"), null)),
          arguments(new UserDto("name", null, null, null)),
          arguments(new UserDto("name", "", List.of("weather"), null)),
          arguments(new UserDto("name", "password", List.of(""), "admin")));
    }

    static Stream<Arguments> validUser() {
      return Stream.of(arguments(new UserDto("name", "password", List.of("weather"), null)),
          arguments(new UserDto("name", "pwd", List.of(""), null)),
          arguments(new UserDto("n", "password", null, null)));
    }

    @Test
    void when_incorrect_password_then_unauthorized() throws Exception {
      when(userService.login(any())).thenThrow(BadCredentialsException.class);

      mockMvc
          .perform(
              post("/users/signin")
                  .content(objectMapper.writeValueAsString(new UserDto("test", "password", null, null)))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isUnauthorized());

      verify(userService).login(any());
    }
  }

  @Nested
  class Signup {

    @Test
    void ok() throws Exception {
      ArgumentCaptor<UserDto> capture = ArgumentCaptor.forClass(UserDto.class);
      when(userService.save(any())).thenAnswer(a -> a.getArgument(0));

      assertThat(DUMMY_USER)
          .usingRecursiveComparison()
          .isEqualTo(
              objectMapper.readValue(
                  TestUtils.readResponse.apply(
                      mockMvc
                          .perform(
                              post("/users/signup")
                                  .content(objectMapper.writeValueAsString(DUMMY_USER))
                                  .contentType(MediaType.APPLICATION_JSON_VALUE))
                          .andExpect(status().isCreated())),
                  UserDto.class));

      verify(userService).save(capture.capture());
      assertThat(capture.getValue()).usingRecursiveComparison().isEqualTo(DUMMY_USER);
    }

    @Test
    void when_already_exist_then_conflict() throws Exception {
      when(userService.save(any())).thenThrow(AlreadyExistException.class);

      mockMvc
          .perform(
              post("/users/signup")
                  .content(objectMapper.writeValueAsString(DUMMY_USER))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isConflict());

      verify(userService).save(any());
    }

    @ParameterizedTest(name = "Given user with signup ''{0}'', password ''{1}'', apps ''{2}'' when signup then bad request")
    @MethodSource("pmb.user.rest.UserControllerTest$Signup#invalidUser")
    void when_invalid_then_bad_request(UserDto user)
        throws Exception {
      mockMvc
          .perform(
              post("/users/signup")
                  .content(objectMapper.writeValueAsString(user))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$", anyOf(containsString("Validation failed for argument [0]"), matchesPattern(
              "Field: '.*', Message: '.*'"))))
          .andExpect(jsonPath("$", not(matchesPattern(".*with [2-9]+ errors.*"))));

      verify(userService, never()).save(any());
    }

    static Stream<Arguments> invalidUser() {
      return Stream.of(
          arguments(new UserDto(null, "password", List.of("weather"), null)),
          arguments(new UserDto("", "password", List.of("weather"), null)),
          arguments(new UserDto("k", "password", List.of("weather"), null)),
          arguments(new UserDto("0123456789012345678901234567890", "password", List.of("weather"), null)),
          arguments(new UserDto("name", null, null, null)),
          arguments(new UserDto("name", "", List.of("weather"), null)),
          arguments(new UserDto("name", "ds", List.of("weather"), null)),
          arguments(new UserDto("name", "0123456789012345678901234567890", List.of("weather"), null)),
          arguments(new UserDto("name", "password", null, null)),
          arguments(new UserDto("name", "password", List.of(""), null)),
          arguments(new UserDto("name", "password", List.of("cook"), "admin")));
    }

    @Test
    void when_exception_then_internal_server_error() throws Exception {
      when(userService.save(any())).thenThrow(new ArithmeticException());

      mockMvc
          .perform(
              post("/users/signup")
                  .content(objectMapper.writeValueAsString(DUMMY_USER))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isInternalServerError());

      verify(userService).save(any());
    }
  }

  @Nested
  class UpdatePassword {

    @Test
    @WithMockUser
    void ok() throws Exception {
      ArgumentCaptor<PasswordDto> capture = ArgumentCaptor.forClass(PasswordDto.class);
      doNothing().when(userService).updatePassword(any());

      mockMvc
          .perform(
              put("/users/password")
                  .content(objectMapper.writeValueAsString(DUMMY_PASSWORD))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isNoContent());

      verify(userService).updatePassword(capture.capture());
      assertThat(capture.getValue()).usingRecursiveComparison().isEqualTo(DUMMY_PASSWORD);
    }

    @Test
    @WithMockUser
    void when_notFound_then() throws Exception {
      doThrow(new UsernameNotFoundException("User not found")).when(userService).updatePassword(any());

      mockMvc
          .perform(
              put("/users/password")
                  .content(objectMapper.writeValueAsString(DUMMY_PASSWORD))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isUnauthorized());

      verify(userService).updatePassword(any());
    }

    @WithMockUser
    @ParameterizedTest(name = "Given new password ''{0}'' and old password ''{1}'' when updates password then bad request")
    @CsvSource({
        ", password",
        "o, password",
        "01234567891011121314151617181920, password",
        "password,",
        "password, o",
        "password, 01234567891011121314151617181920",
    })
    void when_failed_validation_then_bad_request(String newPassword, String oldPassword)
        throws Exception {
      mockMvc
          .perform(
              put("/users/password")
                  .content(
                      objectMapper.writeValueAsString(new PasswordDto(oldPassword, newPassword)))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isBadRequest());

      verify(userService, never()).updatePassword(any());
    }

    @Test
    void not_authenticated_then_unauthorized() throws Exception {
      mockMvc
          .perform(
              put("/users/password")
                  .content(objectMapper.writeValueAsString(DUMMY_PASSWORD))
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isUnauthorized());

      verify(userService, never()).updatePassword(any());
    }
  }
}
