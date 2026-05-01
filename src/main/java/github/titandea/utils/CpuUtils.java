package github.titandea.utils;

import github.titandea.dto.CpuMetrics;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Sensors;


@UtilityClass
@Slf4j
public class CpuUtils {

    public static CpuMetrics collectCpuMetrics(
            CentralProcessor processor,
            Sensors sensors,
            long[] prevTicks,
            long[] currentTicks) {

        // Индексы массива тиков (согласно документации OSHI):
        // [0] User, [1] Nice, [2] System, [3] Idle, [4] IOWait, [5] IRQ, [6] SoftIRQ, [7] Steal
        long userDelta = currentTicks[0] - prevTicks[0];
        long niceDelta = currentTicks[1] - prevTicks[1];
        long systemDelta = currentTicks[2] - prevTicks[2];
        long idleDelta = currentTicks[3] - prevTicks[3];
        long iowaitDelta = currentTicks[4] - prevTicks[4];
        long irqDelta = currentTicks[5] - prevTicks[5];
        long softirqDelta = currentTicks[6] - prevTicks[6];
        long stealDelta = currentTicks[7] - prevTicks[7];

        long totalDelta = userDelta + niceDelta + systemDelta + idleDelta +
                iowaitDelta + irqDelta + softirqDelta + stealDelta;
        if (totalDelta == 0) totalDelta = 1; // Защита от деления на 0

        double user = (userDelta + niceDelta) * 100.0 / totalDelta;
        double system = (systemDelta + irqDelta + softirqDelta) * 100.0 / totalDelta;
        double iowait = iowaitDelta * 100.0 / totalDelta;
        double usage = 100.0 - (idleDelta * 100.0 / totalDelta);

        double[] loadAvg = processor.getSystemLoadAverage(3);

        return CpuMetrics.builder()
                .usagePercent(MathUtils.round2(usage))
                .userPercent(MathUtils.round2(user))
                .systemPercent(MathUtils.round2(system))
                .iowaitPercent(MathUtils.round2(iowait))
                .loadAverage1(loadAvg.length > 0 ? MathUtils.round2(loadAvg[0]) : -1)
                .loadAverage5(loadAvg.length > 1 ? MathUtils.round2(loadAvg[1]) : -1)
                .loadAverage15(loadAvg.length > 2 ? MathUtils.round2(loadAvg[2]) : -1)
                .temperature(MathUtils.round2(sensors.getCpuTemperature()))
                .physicalCores(processor.getPhysicalProcessorCount())
                .logicalProcessors(processor.getLogicalProcessorCount())
                .build();
    }
}