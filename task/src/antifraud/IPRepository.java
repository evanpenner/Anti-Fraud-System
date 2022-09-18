package antifraud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface IPRepository extends JpaRepository<BlacklistedIP, Long> {
    boolean existsByIp(String ip);

    @Transactional
    void deleteByIp(String ip);
}
