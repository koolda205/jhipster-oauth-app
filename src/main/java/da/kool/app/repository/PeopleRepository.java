package da.kool.app.repository;

import da.kool.app.domain.People;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the People entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PeopleRepository extends JpaRepository<People, Long> {}
