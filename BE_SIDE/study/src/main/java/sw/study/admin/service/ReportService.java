package sw.study.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.study.admin.domain.Report;
import sw.study.admin.repository.ReportRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    /**
     * 신고 생성
     */
    @Transactional
    public Long save(Report report) {
        return reportRepository.save(report).getId();
    }
}
