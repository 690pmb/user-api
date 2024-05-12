package pmb.user.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pmb.user.dto.UserDto;
import pmb.user.mapper.UserMapper;
import pmb.user.repository.UserRepository;

/**
 * @see UserDetailsService
 * @see UserDetailsPasswordService
 */
@Service
public class MyUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public MyUserDetailsService(UserRepository userRepository, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Override
  public UserDetails loadUserByUsername(String login) {
    return userRepository.findById(login).map(userMapper::toDto)
        .orElseThrow(() -> new UsernameNotFoundException("user: " + login + " not found"));
  }

  @Override
  public UserDetails updatePassword(UserDetails user, String newPassword) {
    UserDto dto = (UserDto) loadUserByUsername(user.getUsername());
    dto.setPassword(newPassword);
    return userMapper.toDto(userRepository.save(userMapper.toEntity(dto)));
  }
}
