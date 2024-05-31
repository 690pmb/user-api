package pmb.user.service;

import org.springframework.stereotype.Service;
import pmb.user.dto.AppDto;
import pmb.user.exception.AlreadyExistException;
import pmb.user.model.App;
import pmb.user.repository.AppRepository;

/** {@link App} service. */
@Service
public class AppService {

  private final AppRepository appRepository;

  public AppService(AppRepository appRepository) {
    this.appRepository = appRepository;
  }

  /**
   * Checks unity by name and save it to database.
   *
   * @param name new app name
   * @return saved app
   */
  public AppDto save(String name) {
    appRepository
        .findById(name)
        .ifPresent(
            u -> {
              throw new AlreadyExistException("Application with name '" + name + "' already exist");
            });
    return new AppDto(appRepository.save(new App(name)).getName());
  }

  /**
   * Deletes, say nothing if not exist.
   *
   * @param name app's name to delete
   */
  public void delete(String name) {
    appRepository.findById(name).ifPresent(appRepository::delete);
  }
}
