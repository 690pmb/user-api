package pmb.user.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pmb.user.dto.JwtTokenDto;
import pmb.user.dto.PasswordDto;
import pmb.user.dto.UserDto;
import pmb.user.exception.AlreadyExistException;
import pmb.user.mapper.UserMapper;
import pmb.user.model.App;
import pmb.user.model.User;
import pmb.user.repository.UserRepository;
import pmb.user.security.JwtTokenProvider;

/** {@link User} service. */
@Service
public class UserService {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final UserMapper userMapper;

  public UserService(
      UserRepository userRepository,
      AuthenticationManager authenticationManager,
      JwtTokenProvider jwtTokenProvider,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.userMapper = userMapper;
  }

  /**
   * Checks unity by username and save it to database (with its password encoded).
   *
   * @param user to save
   * @return saved user
   */
  public UserDto save(UserDto user) {
    userRepository
        .findById(user.getUsername())
        .ifPresent(
            u -> {
              throw new AlreadyExistException(
                  "User with name '" + user.getUsername() + "' already exist");
            });
    User saved = userRepository.save(
        new User(
            user.getUsername(),
            bCryptPasswordEncoder.encode(user.getPassword()),
            user.getApps().stream().map(App::new).toList(),
            userMapper.authoritiesToRole(user.getAuthorities())));
    return userMapper.toDtoWithoutPassword(saved);
  }

  /**
   * Checks given credentials, authenticates and creates a jwt token.
   *
   * @param user credentials
   * @return a jwt token
   */
  public JwtTokenDto login(UserDto user) {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
        user.getUsername(), user.getPassword(), user.getAuthorities());
    Authentication authentication = authenticationManager.authenticate(token);
    JwtTokenDto jwtToken = new JwtTokenDto(jwtTokenProvider.create(authentication));
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    return jwtToken;
  }

  /**
   * Checks if old password is correct and then updates user with new password.
   *
   * @param password holding new & old passwords
   */
  public void updatePassword(PasswordDto password) {
    User user = getCurrentUser();
    if (!bCryptPasswordEncoder.matches(password.oldPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid credentials");
    }
    user.setPassword(bCryptPasswordEncoder.encode(password.newPassword()));
    userRepository.save(user);
  }

  /**
   * Recovers current logged user.
   *
   * @return {@link User} authenticated
   */
  private User getCurrentUser() {
    return JwtTokenProvider.getCurrentUserLogin()
        .flatMap(userRepository::findById)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }
}
