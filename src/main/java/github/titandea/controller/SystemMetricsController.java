package github.titandea.controller;

import github.titandea.dto.SystemMetrics;
import github.titandea.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/metrics")
public class SystemMetricsController {
    private final MonitoringService monitoringService;

    @GetMapping
    public SystemMetrics getMetrics() {
        return monitoringService.collectMetrics();
    }
}
