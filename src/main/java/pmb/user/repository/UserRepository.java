package pmb.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pmb.user.model.User;

/**
 * {@link User} repository
 *
 * @see JpaRepository
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {}
