package github.titandea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoryMetrics {

    private long totalBytes;

    private long availableBytes;

    private long usedBytes;

    private long swapTotalBytes;

    private long swapUsedBytes;

    private double swapUsagePercent;
}