package pmb.user.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** Application database entity. */
@Entity
@Table(name = "app")
public class App {

  @Id private String name;

  public App() {}

  public App(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
