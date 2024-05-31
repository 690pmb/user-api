package pmb.user.rest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pmb.user.dto.AppDto;
import pmb.user.service.AppService;

/** App rest controller. */
@Validated
@RestController
@RequestMapping(
    path = "/apps",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
public class AppController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);

  private final AppService appService;

  public AppController(AppService appService) {
    this.appService = appService;
  }

  @PostMapping("/{name}")
  @ResponseStatus(HttpStatus.CREATED)
  public AppDto create(@NotBlank @Size(max = 30) @PathVariable String name) {
    LOGGER.debug("Create app with name: '{}'", name);
    return appService.save(name);
  }

  @DeleteMapping("/{name}")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  public void updatePassword(@PathVariable String name) {
    LOGGER.debug("Delete app with name '{}'", name);
    appService.delete(name);
  }
}
