package pmb.user.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import pmb.user.model.User;

@DataJpaTest
class UserRepositoryTest {
  @Autowired
  UserRepository userRepository;

  @Test
  void save() {
    User user = new User("login", "pwd");
    User saved = userRepository.save(user);
    assertAll(
        () -> assertEquals("login", saved.getLogin()),
        () -> assertEquals("pwd", saved.getPassword()));
  }

  @Test
  @Sql(statements = "insert into user values ('login2', 'pwd2')")
  void findById() {
    Optional<User> user = userRepository.findById("login2");
    assertAll(
        () -> assertTrue(user.isPresent()),
        () -> assertEquals("login2", user.get().getLogin()),
        () -> assertEquals("pwd2", user.get().getPassword()));
  }
}
