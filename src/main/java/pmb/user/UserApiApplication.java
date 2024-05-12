package pmb.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class UserApiApplication {

  public static void main(String[] args) {
    new SpringApplication(UserApiApplication.class).run(args);
  }
}
