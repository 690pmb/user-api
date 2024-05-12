package pmb.user.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import pmb.user.ServiceTestRunner;
import pmb.user.dto.AppDto;
import pmb.user.exception.AlreadyExistException;
import pmb.user.mapper.AppMapperImpl;
import pmb.user.model.App;
import pmb.user.repository.AppRepository;

@Import({ AppService.class, AppMapperImpl.class })
@ServiceTestRunner
class AppServiceTest {

  @MockBean
  private AppRepository appRepository;
  @Autowired
  private AppService appService;

  private final String DUMMY_APP = "test";

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
        appRepository);
  }

  @Nested
  class Save {

    @Test
    void already_exist() {
      when(appRepository.findByName(DUMMY_APP)).thenReturn(Optional.of(new App()));

      assertThrows(AlreadyExistException.class, () -> appService.save(DUMMY_APP));

      verify(appRepository).findByName(DUMMY_APP);
      verify(appRepository, never()).save(any());
    }

    @Test
    void success() {
      when(appRepository.findByName(DUMMY_APP)).thenReturn(Optional.empty());
      when(appRepository.save(any())).thenAnswer(a -> a.getArgument(0));

      AppDto saved = appService.save(DUMMY_APP);

      assertAll(
          () -> assertNotNull(saved),
          () -> assertEquals(DUMMY_APP, saved.name()));

      verify(appRepository).findByName(DUMMY_APP);
      verify(appRepository).save(any());
    }
  }

  @Nested
  class Delete {

    @Test
    void ok() {
      doNothing().when(appRepository).deleteById(8L);

      appService.delete(8L);

      verify(appRepository).deleteById(8L);
    }
  }
}
