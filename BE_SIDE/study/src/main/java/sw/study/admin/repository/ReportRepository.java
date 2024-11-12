package sw.study.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sw.study.admin.domain.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

}
