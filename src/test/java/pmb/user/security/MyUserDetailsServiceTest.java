package pmb.user.security;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import pmb.user.ServiceTestRunner;
import pmb.user.dto.UserDto;
import pmb.user.mapper.UserMapperImpl;
import pmb.user.model.App;
import pmb.user.model.User;
import pmb.user.repository.UserRepository;

@ServiceTestRunner
@Import({ MyUserDetailsService.class, UserMapperImpl.class })
class MyUserDetailsServiceTest {

  @MockBean
  private UserRepository userRepository;
  @Autowired
  private MyUserDetailsService myUserDetailsService;

  private static final User DUMMY_USER = new User("test", "pwd", List.of(new App("weather"), new App("cook")), "admin");
  private static final UserDto DUMMY_USER_DTO = new UserDto("test", "pwd", List.of("weather", "cook"), "admin");

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(userRepository);
  }

  @Nested
  class LoadUserByUsername {

    @Test
    void ok() {
      when(userRepository.findById("test")).thenReturn(Optional.of(DUMMY_USER));

      UserDto actual = (UserDto) myUserDetailsService.loadUserByUsername("test");

      assertAll(
          () -> assertEquals("test", actual.getUsername()),
          () -> assertEquals("pwd", actual.getPassword()),
          () -> assertEquals("admin", actual.getRole().toString()),
          () -> assertEquals(2, actual.getApps().size()),
          () -> actual
              .getApps()
              .forEach(
                  app -> assertTrue(
                      DUMMY_USER.getApps().stream()
                          .map(App::getName)
                          .anyMatch(a -> a.equals(app)))));

      verify(userRepository).findById("test");
    }

    @Test
    void not_found() {
      when(userRepository.findById("test")).thenReturn(Optional.empty());

      assertThrows(
          UsernameNotFoundException.class, () -> myUserDetailsService.loadUserByUsername("test"));

      verify(userRepository).findById("test");
    }
  }

  @Nested
  class UpdatePassword {

    @Test
    void ok() {
      when(userRepository.findById("test")).thenReturn(Optional.of(DUMMY_USER));
      when(userRepository.save(any())).thenAnswer(a -> a.getArgument(0));

      UserDto actual = (UserDto) myUserDetailsService.updatePassword(DUMMY_USER_DTO, "password");

      assertAll(
          () -> assertEquals("test", actual.getUsername()),
          () -> assertEquals("password", actual.getPassword()),
          () -> assertEquals("admin", actual.getRole().toString()),
          () -> assertEquals(2, actual.getApps().size()),
          () -> actual
              .getApps()
              .forEach(
                  app -> assertTrue(
                      DUMMY_USER.getApps().stream()
                          .map(App::getName)
                          .anyMatch(a -> a.equals(app)))));

      verify(userRepository).findById("test");
      verify(userRepository).save(any());
    }

    @Test
    void not_found() {
      when(userRepository.findById("test")).thenReturn(Optional.empty());

      assertThrows(
          UsernameNotFoundException.class,
          () -> myUserDetailsService.updatePassword(DUMMY_USER_DTO, "password"));

      verify(userRepository).findById("test");
      verify(userRepository, never()).save(any());
    }
  }

  @Nested
  class save {
    @Test
    void ok() {
      ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
      when(userRepository.save(any())).thenAnswer(a -> a.getArgument(0));

      myUserDetailsService.save(DUMMY_USER_DTO);

      verify(userRepository).save(captor.capture());

      User saved = captor.getValue();
      assertAll(
          () -> assertEquals("test", saved.getLogin()),
          () -> assertEquals("pwd", saved.getPassword()),
          () -> assertEquals("admin", saved.getRole()),
          () -> assertEquals(2, saved.getApps().size()),
          () -> saved
              .getApps()
              .forEach(
                  app -> assertTrue(
                      DUMMY_USER.getApps().stream()
                          .map(App::getName)
                          .anyMatch(a -> a.equals(app.getName())))));
    }
  }
}
