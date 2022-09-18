package antifraud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    boolean existsByNumber(String number);

    @Transactional
    void deleteByNumber(String number);
}
