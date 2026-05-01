package github.titandea.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CpuMetrics {

    private double usagePercent;

    private double userPercent;

    private double systemPercent;

    private double iowaitPercent;

    private double loadAverage1;

    private double loadAverage5;

    private double loadAverage15;

    private double temperature;

    private int physicalCores;

    private int logicalProcessors;
}
