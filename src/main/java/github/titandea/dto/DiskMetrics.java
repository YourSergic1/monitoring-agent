package github.titandea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiskMetrics {

    private String mountPoint;

    private String type;

    private long totalBytes;

    private long usedBytes;

    private long freeBytes;

    private double usagePercent;
}
