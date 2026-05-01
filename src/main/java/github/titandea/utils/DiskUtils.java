package github.titandea.utils;

import github.titandea.dto.DiskMetrics;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class DiskUtils {

    public static List<DiskMetrics> collectDiskMetrics(SystemInfo systemInfo) {
        try {
            Set<String> excludedTypes = Set.of("tmpfs", "devtmpfs", "overlay", "squashfs", "cdrom", "iso9660", "fusectl");

            return systemInfo.getOperatingSystem().getFileSystem().getFileStores().stream()
                    .filter(fs -> fs.getTotalSpace() > 0) // Игнорируем нулевые разделы
                    .filter(fs -> !excludedTypes.contains(fs.getType().toLowerCase())) // Исключаем виртуальные ФС
                    .map(fs -> {
                        long total = fs.getTotalSpace();
                        long free = fs.getUsableSpace(); // Учтено резервное место для root/admin
                        long used = total - free;
                        double usagePercent = total > 0 ? (double) used / total * 100.0 : 0.0;

                        return DiskMetrics.builder()
                                .mountPoint(fs.getMount())
                                .type(fs.getType())
                                .totalBytes(total)
                                .usedBytes(used)
                                .freeBytes(free)
                                .usagePercent(MathUtils.round2(usagePercent))
                                .build();
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Не удалось собрать метрики дисков: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
