package pmb.user.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/** User database entity. */
@Entity
@Table(name = "user")
public class User {

  @Id private String login;

  private String password;

  @ManyToMany
  @JoinTable(
      name = "user_app",
      joinColumns = @JoinColumn(name = "login"),
      inverseJoinColumns = @JoinColumn(name = "app_name"))
  private List<App> apps = new ArrayList<>();

  private String role;

  public User() {}

  public User(String login, String password, List<App> apps, String role) {
    this.login = login;
    this.password = password;
    this.apps = apps;
    this.role = role;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<App> getApps() {
    return apps;
  }

  public void setApps(List<App> apps) {
    this.apps = apps;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
