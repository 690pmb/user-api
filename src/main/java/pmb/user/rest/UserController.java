package pmb.user.rest;

import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pmb.user.dto.JwtTokenDto;
import pmb.user.dto.OnSignup;
import pmb.user.dto.PasswordDto;
import pmb.user.dto.UserDto;
import pmb.user.service.UserService;

/** User rest controller. */
@Validated
@RestController
@RequestMapping(path = "/users", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/signin")
  public JwtTokenDto signin(@RequestBody @Valid UserDto user) {
    LOGGER.debug("Login: {}", user.getUsername());
    return userService.login(user);
  }

  @PostMapping("/signup")
  @Validated(OnSignup.class)
  @ResponseStatus(code = HttpStatus.CREATED)
  public UserDto signup(@RequestBody @Valid UserDto user) {
    LOGGER.debug("Register: {}", user.getUsername());
    return userService.save(user);
  }

  @PutMapping("/password")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void updatePassword(@RequestBody @Valid PasswordDto password) {
    LOGGER.debug("Update password");
    userService.updatePassword(password);
  }
}
