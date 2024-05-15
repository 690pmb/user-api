package pmb.user.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pmb.user.model.App;

@DataJpaTest
class AppRepositoryTest {
  @Autowired AppRepository appRepository;

  @Test
  void save() {
    App app = new App("test");
    App saved = appRepository.save(app);
    assertAll(() -> assertEquals("test", saved.getName()));
  }

  @Test
  void findById() {
    Optional<App> app = appRepository.findById("weather");
    assertAll(
        () -> assertTrue(app.isPresent()), () -> assertEquals("weather", app.get().getName()));
  }

  @Test
  void delete() {
    appRepository.deleteById("weather");
    Optional<App> app = appRepository.findById("weather");
    assertAll(() -> assertTrue(app.isEmpty()));
  }
}
