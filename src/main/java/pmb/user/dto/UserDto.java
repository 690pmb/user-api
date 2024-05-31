package pmb.user.dto;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * User data, used for authentication and registration.
 *
 * @see UserDetails
 */
public class UserDto implements UserDetails {

  @Serial
  private static final long serialVersionUID = 1L;

  @NotBlank
  @Size(min = 4, max = 30, groups = OnSignup.class)
  private String username;

  @NotBlank
  @Size(min = 6, max = 30, groups = OnSignup.class)
  private String password;

  private @NotEmpty(groups = OnSignup.class) List<@NotBlank(groups = OnSignup.class) String> apps;

  @Null
  private String role;

  public UserDto() {
    super();
  }

  public UserDto(String username, String password, List<String> apps, String role) {
    this.username = username;
    this.password = password;
    this.apps = apps;
    this.role = role;
  }

  @Override
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  @JsonIgnore
  public List<SimpleGrantedAuthority> getAuthorities() {
    return new ArrayList<>(
        Optional.ofNullable(role).filter(StringUtils::isNotBlank).map(SimpleGrantedAuthority::new).map(List::of)
            .orElse(Collections.emptyList()));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public List<String> getApps() {
    return apps;
  }

  public void setApps(List<String> apps) {
    this.apps = apps;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
