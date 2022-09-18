package antifraud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByUsernameIgnoreCase(String username);

    @Transactional
    void deleteByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
