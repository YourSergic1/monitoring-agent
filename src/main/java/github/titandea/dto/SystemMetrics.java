package github.titandea.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemMetrics {

    private String hostname;

    private String localIp;

    private String publicIp;

    private LocalDateTime dateTime;

    private Long uptimeMinutes;

    private CpuMetrics cpu;

    private MemoryMetrics memory;

    private List<DiskMetrics> disk;

    private List<NetworkMetrics> network;
}