package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.InterestArea;
import java.util.List;
import java.util.Optional;

public interface InterestAreaRepository extends JpaRepository<InterestArea, Long> {
    List<InterestArea> findAll();

    Optional<InterestArea> findByAreaName(String areaName);

}
