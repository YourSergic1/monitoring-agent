package github.titandea.utils;

import github.titandea.dto.MemoryMetrics;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.VirtualMemory;

@UtilityClass
@Slf4j
public class MemoryUtils {

    public static MemoryMetrics collectMemoryMetrics(SystemInfo systemInfo) {
        try {
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            VirtualMemory vmem = memory.getVirtualMemory();

            long total = memory.getTotal();
            long available = memory.getAvailable();
            long used = total - available;

            long swapTotal = vmem.getSwapTotal();
            long swapUsed = vmem.getSwapUsed();
            double swapPercent = swapTotal > 0 ? (double) swapUsed / swapTotal * 100.0 : 0.0;

            return MemoryMetrics.builder()
                    .totalBytes(total)
                    .availableBytes(available)
                    .usedBytes(used)
                    .swapTotalBytes(swapTotal)
                    .swapUsedBytes(swapUsed)
                    .swapUsagePercent(MathUtils.round2(swapPercent))
                    .build();
        } catch (Exception e) {
            log.warn("Не удалось собрать метрики памяти: {}", e.getMessage());
            return null;
        }
    }
}
