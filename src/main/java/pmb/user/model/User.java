package pmb.user.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** User database entity. */
@Entity
@Table(name = "user")
public class User {

    @Id
    private String login;

    private String password;

    public User() {
    }

    /**
     * {@link User} constructor.
     *
     * @param login    user's name
     * @param password his password
     */
    public User(String login, String password) {
        this.login = login;
        this.password = password;
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
}
