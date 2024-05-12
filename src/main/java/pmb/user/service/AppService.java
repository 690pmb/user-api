package pmb.user.service;

import org.springframework.stereotype.Service;

import pmb.user.dto.AppDto;
import pmb.user.exception.AlreadyExistException;
import pmb.user.mapper.AppMapper;
import pmb.user.model.App;
import pmb.user.repository.AppRepository;

/** {@link App} service. */
@Service
public class AppService {

  private final AppRepository appRepository;
  private final AppMapper appMapper;

  public AppService(
      AppRepository appRepository,
      AppMapper appMapper) {
    this.appRepository = appRepository;
    this.appMapper = appMapper;
  }

  /**
   * Checks unity by name and save it to database.
   *
   * @param name new app name
   * @return saved app
   */
  public AppDto save(String name) {
    appRepository
        .findByName(name)
        .ifPresent(
            u -> {
              throw new AlreadyExistException(
                  "Application with name '" + name + "' already exist");
            });
    return appMapper.toDto(appRepository.save(new App(name)));
  }

  /**
   * Deletes, say nothing if not exist.
   *
   * @param app app's id to delete
   */
  public void delete(Long id) {
    appRepository.deleteById(id);
  }
}
