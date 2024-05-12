package pmb.user.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import pmb.user.ServiceTestRunner;
import pmb.user.dto.JwtTokenDto;
import pmb.user.dto.PasswordDto;
import pmb.user.dto.UserDto;
import pmb.user.exception.AlreadyExistException;
import pmb.user.mapper.UserMapperImpl;
import pmb.user.model.User;
import pmb.user.repository.UserRepository;
import pmb.user.security.JwtTokenProvider;

@Import({ UserService.class, UserMapperImpl.class })
@ServiceTestRunner
class UserServiceTest {

  @MockBean
  private UserRepository userRepository;
  @MockBean
  private AuthenticationManager authenticationManager;
  @MockBean
  private JwtTokenProvider jwtTokenProvider;
  @MockBean
  private BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired
  private UserService userService;

  private final UserDto DUMMY_USER = new UserDto("test", "pwd");
  private final PasswordDto DUMMY_PASSWORD = new PasswordDto("password", "newPassword");

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
        userRepository, authenticationManager, jwtTokenProvider, bCryptPasswordEncoder);
  }

  @Nested
  class Save {

    @Test
    void already_exist() {
      when(userRepository.findById("test")).thenReturn(Optional.of(new User()));

      assertThrows(AlreadyExistException.class, () -> userService.save(DUMMY_USER));

      verify(userRepository).findById("test");
      verify(userRepository, never()).save(any());
      verify(bCryptPasswordEncoder, never()).encode(any());
      verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void success() {
      when(userRepository.findById("test")).thenReturn(Optional.empty());
      when(bCryptPasswordEncoder.encode("test")).thenAnswer(a -> a.getArgument(0));
      when(userRepository.save(any())).thenAnswer(a -> a.getArgument(0));

      UserDto saved = userService.save(DUMMY_USER);

      assertAll(
          () -> assertNotNull(saved),
          () -> assertEquals("test", saved.getUsername()),
          () -> assertNull(saved.getPassword()),
          () -> assertTrue(saved.isEnabled()),
          () -> assertTrue(saved.isAccountNonLocked()),
          () -> assertTrue(saved.isCredentialsNonExpired()),
          () -> assertTrue(saved.isAccountNonExpired()));

      verify(userRepository).findById("test");
      verify(userRepository).save(any());
      verify(bCryptPasswordEncoder).encode("pwd");
      verify(authenticationManager, never()).authenticate(any());
    }
  }

  @Nested
  class Login {

    @Test
    void bad_credentials() {
      ArgumentCaptor<UsernamePasswordAuthenticationToken> token = ArgumentCaptor
          .forClass(UsernamePasswordAuthenticationToken.class);

      when(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException.class);

      assertThrows(BadCredentialsException.class, () -> userService.login(DUMMY_USER));

      verify(authenticationManager).authenticate(token.capture());
      verify(userRepository, never()).findById(any());
      verify(bCryptPasswordEncoder, never()).encode(any());
      verify(userRepository, never()).save(any());
      verify(jwtTokenProvider, never()).create(any());

      UsernamePasswordAuthenticationToken captured = token.getValue();
      assertAll(
          () -> assertEquals("test", captured.getName()),
          () -> assertEquals("pwd", captured.getCredentials()),
          () -> assertFalse(captured.isAuthenticated()),
          () -> assertNull(SecurityContextHolder.getContext().getAuthentication()));
    }

    @Test
    void success() {
      ArgumentCaptor<UsernamePasswordAuthenticationToken> token = ArgumentCaptor
          .forClass(UsernamePasswordAuthenticationToken.class);

      when(authenticationManager.authenticate(any())).thenAnswer(a -> a.getArgument(0));
      when(jwtTokenProvider.create(any())).thenReturn("jwt");

      JwtTokenDto login = userService.login(DUMMY_USER);

      verify(authenticationManager).authenticate(token.capture());
      verify(jwtTokenProvider).create(any());
      verify(userRepository, never()).findById(any());
      verify(bCryptPasswordEncoder, never()).encode(any());
      verify(userRepository, never()).save(any());

      UsernamePasswordAuthenticationToken captured = token.getValue();
      assertAll(
          () -> assertEquals("jwt", login.token()),
          () -> assertEquals("test", captured.getName()),
          () -> assertEquals("pwd", captured.getCredentials()),
          () -> assertFalse(captured.isAuthenticated()),
          () -> assertEquals(
              "test", SecurityContextHolder.getContext().getAuthentication().getName()));
    }
  }

  @Nested
  class UpdatePassword {

    @Test
    @WithMockUser(username = "test")
    void ok() {
      User user = new User("test", "encryptedPassword");
      ArgumentCaptor<User> captured = ArgumentCaptor.forClass(User.class);

      when(userRepository.findById("test")).thenReturn(Optional.of(user));
      when(bCryptPasswordEncoder.matches("password", "encryptedPassword")).thenReturn(true);
      when(bCryptPasswordEncoder.encode("newPassword")).thenAnswer(a -> a.getArgument(0));
      when(userRepository.save(any())).thenAnswer(a -> a.getArgument(0));

      userService.updatePassword(DUMMY_PASSWORD);

      verify(userRepository).findById("test");
      verify(bCryptPasswordEncoder).matches("password", "encryptedPassword");
      verify(bCryptPasswordEncoder).encode("newPassword");
      verify(userRepository).save(captured.capture());

      User savedUser = captured.getValue();
      assertAll(
          () -> assertEquals("test", savedUser.getLogin()),
          () -> assertEquals("newPassword", savedUser.getPassword()));
    }

    @Test
    void not_loggued_then_not_found() {
      assertThrows(
          UsernameNotFoundException.class, () -> userService.updatePassword(DUMMY_PASSWORD));

      verify(userRepository, never()).findById("test");
      verify(bCryptPasswordEncoder, never()).matches("password", "encryptedPassword");
      verify(bCryptPasswordEncoder, never()).encode("newPassword");
      verify(userRepository, never()).save(any());
    }

    @Test
    @WithMockUser(username = "test")
    void not_found_in_db() {
      when(userRepository.findById("test")).thenReturn(Optional.empty());

      assertThrows(
          UsernameNotFoundException.class, () -> userService.updatePassword(DUMMY_PASSWORD));

      verify(userRepository).findById("test");
      verify(bCryptPasswordEncoder, never()).matches("password", "encryptedPassword");
      verify(bCryptPasswordEncoder, never()).encode("newPassword");
      verify(userRepository, never()).save(any());
    }

    @Test
    @WithMockUser(username = "test")
    void incorrect_password() {
      User user = new User("test", "encryptedPassword");

      when(userRepository.findById("test")).thenReturn(Optional.of(user));
      when(bCryptPasswordEncoder.matches("password", "encryptedPassword")).thenReturn(false);

      assertThrows(BadCredentialsException.class, () -> userService.updatePassword(DUMMY_PASSWORD));

      verify(userRepository).findById("test");
      verify(bCryptPasswordEncoder).matches("password", "encryptedPassword");
      verify(bCryptPasswordEncoder, never()).encode("newPassword");
      verify(userRepository, never()).save(any());
    }
  }
}
