package pmb.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pmb.user.model.App;

/**
 * {@link App} repository
 *
 * @see JpaRepository
 */
@Repository
public interface AppRepository extends JpaRepository<App, String> {}
