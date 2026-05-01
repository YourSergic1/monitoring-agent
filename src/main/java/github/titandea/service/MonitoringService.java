package github.titandea.service;

import github.titandea.dto.*;
import github.titandea.utils.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Sensors;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {
    private final SystemInfo systemInfo = new SystemInfo();
    private long[] previousCpuTicks = new long[8];

    @Value("${agent.organization}")
    private String agentOrganization;

    @PostConstruct
    public void init() {
        try {
            previousCpuTicks = systemInfo.getHardware().getProcessor().getSystemCpuLoadTicks();
            log.info("MonitoringService инициализирован. Организация: {}",
                    agentOrganization);
        } catch (Exception e) {
            log.error("Ошибка при инициализации MonitoringService: {}", e.getMessage(), e);
        }
    }

    public SystemMetrics collectMetrics() {
        try {
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            Sensors sensors = systemInfo.getHardware().getSensors();

            CpuMetrics cpu = buildCpuMetrics(processor, sensors, previousCpuTicks);
            MemoryMetrics memory = buildMemoryMetrics();
            List<DiskMetrics> diskMetrics = buildDiskMetrics();
            List<NetworkMetrics> networkMetrics = buildNetworkMetrics();

            return SystemMetrics.builder()
                    .agentOrganization(agentOrganization)
                    .hostname(SystemUtils.getHostname())
                    .localIp(IpUtils.getLocalIp(systemInfo))
                    .publicIp(IpUtils.getPublicIp())
                    .dateTime(LocalDateTime.now())
                    .uptimeMinutes(SystemUtils.getSystemUptimeMinutes(systemInfo))
                    .cpu(cpu)
                    .memory(memory)
                    .disk(diskMetrics)
                    .network(networkMetrics)
                    .build();

        } catch (Exception e) {
            log.error("Ошибка сбора метрик: {}", e.getMessage(), e);
            return null;
        }
    }

    private CpuMetrics buildCpuMetrics(CentralProcessor processor, Sensors sensors, long[] prevTicks) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        long[] currentTicks = processor.getSystemCpuLoadTicks();
        return CpuUtils.collectCpuMetrics(processor, sensors, prevTicks, currentTicks);
    }

    private MemoryMetrics buildMemoryMetrics() {
        return MemoryUtils.collectMemoryMetrics(systemInfo);
    }

    private List<DiskMetrics> buildDiskMetrics() {
        return DiskUtils.collectDiskMetrics(systemInfo);
    }

    private List<NetworkMetrics> buildNetworkMetrics() {
        return NetworkUtils.collectNetworkMetrics(systemInfo.getHardware());
    }
}
