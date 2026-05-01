package github.titandea.utils;

import github.titandea.dto.NetworkMetrics;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class NetworkUtils {

    private static final Set<String> EXCLUDED_INTERFACES = Set.of(
            "lo", "loopback", "docker", "veth", "br-", "virbr", "tun", "tap",
            "vmnet", "VirtualBox", "Hyper-V", "vEthernet"
    );

    public static List<NetworkMetrics> collectNetworkMetrics(HardwareAbstractionLayer hal) {
        try {
            return hal.getNetworkIFs().stream()
                    .filter(NetworkUtils::isPhysicalInterface)
                    .filter(NetworkUtils::hasValidIpv4)
                    .map(NetworkUtils::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Не удалось собрать сетевые метрики: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private static boolean isPhysicalInterface(NetworkIF net) {
        if (net.isKnownVmMacAddr()) {
            return false;
        }
        String name = net.getName().toLowerCase().trim();
        return EXCLUDED_INTERFACES.stream()
                .noneMatch(excluded -> name.contains(excluded));
    }

    private static boolean hasValidIpv4(NetworkIF net) {
        String[] ipv4addrs = net.getIPv4addr();
        if (ipv4addrs == null || ipv4addrs.length == 0) {
            return false;
        }
        String ip = ipv4addrs[0].trim();
        return !ip.startsWith("127.") && !ip.startsWith("0.") && !ip.isEmpty();
    }

    private static NetworkMetrics mapToDto(NetworkIF net) {
        return NetworkMetrics.builder()
                .interfaceName(net.getName().trim())
                .bytesSent(net.getBytesSent())
                .bytesRecv(net.getBytesRecv())
                .packetsSent(net.getPacketsSent())
                .packetsRecv(net.getPacketsRecv())
                .inErrors(net.getInErrors())
                .outErrors(net.getOutErrors())
                .speed(net.getSpeed())
                .build();
    }
}