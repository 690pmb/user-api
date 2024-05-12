package pmb.user.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import pmb.user.TestUtils;
import pmb.user.dto.AppDto;
import pmb.user.exception.AlreadyExistException;
import pmb.user.security.JwtTokenProvider;
import pmb.user.security.MyUserDetailsService;
import pmb.user.service.AppService;

@ActiveProfiles("test")
@WebMvcTest(controllers = AppController.class)
@MockBean(MyUserDetailsService.class)
@Import({ JwtTokenProvider.class })
@DisplayNameGeneration(value = ReplaceUnderscores.class)
class AppControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private AppService appService;

  private static final String DUMMY_APP = "test";

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(appService);
  }

  @Nested
  class Create {

    @Test
    void ok() throws Exception {
      when(appService.save(DUMMY_APP)).thenAnswer(a -> new AppDto(8L, a.getArgument(0)));

      assertThat(DUMMY_APP)
          .isEqualTo(
              objectMapper.readValue(
                  TestUtils.readResponse.apply(
                      mockMvc
                          .perform(
                              post("/apps")
                                  .param("name", DUMMY_APP)
                                  .contentType(MediaType.APPLICATION_JSON_VALUE))
                          .andExpect(status().isCreated())),
                  AppDto.class).name());

      verify(appService).save(DUMMY_APP);
    }

    @Test
    void when_already_exist_then_conflict() throws Exception {
      when(appService.save(any())).thenThrow(AlreadyExistException.class);

      mockMvc
          .perform(
              post("/apps")
                  .param("name", DUMMY_APP)
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isConflict());

      verify(appService).save(any());
    }

    @ParameterizedTest(name = "Given app with name ''{0}'' when create then bad request")
    @ValueSource(strings = {
        "",
        "   ",
        "0123456789012345678901234567890123456789"
    })
    void when_invalid_then_bad_request(String name)
        throws Exception {
      mockMvc
          .perform(
              post("/apps")
                  .param("name", name)
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isBadRequest());

      verify(appService, never()).save(any());
    }
  }

  @Nested
  class Delete {

    @Test
    void ok() throws Exception {
      doNothing().when(appService).delete(8L);

      mockMvc
          .perform(
              delete("/apps/{id}", 8L)
                  .contentType(MediaType.APPLICATION_JSON_VALUE))
          .andExpect(status().isNoContent());

      verify(appService).delete(8L);
    }
  }
}
