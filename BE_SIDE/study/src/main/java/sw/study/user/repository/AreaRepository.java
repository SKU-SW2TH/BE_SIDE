package sw.study.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sw.study.user.domain.Area;
import java.util.List;
import java.util.Optional;

public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findAll();

    Optional<Area> findByAreaName(String areaName);

}
