package github.titandea.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.net.InetAddress;
import java.net.UnknownHostException;

@UtilityClass
@Slf4j
public class SystemUtils {

    private static String cachedHostname;

    public static String getHostname() {
        if (cachedHostname != null) {
            return cachedHostname;
        }

        try {
            String hostname = System.getenv("COMPUTERNAME");
            if (hostname == null || hostname.isEmpty()) {
                hostname = System.getenv("HOSTNAME");
            }
            if (hostname == null || hostname.isEmpty()) {
                hostname = System.getenv("HOST");
            }

            if (hostname == null || hostname.isEmpty()) {
                hostname = InetAddress.getLocalHost().getHostName();
            }

            if (hostname == null || hostname.isEmpty()) {
                hostname = System.getProperty("user.name", "unknown-host");
            }

            cachedHostname = hostname.trim().replaceAll("[^a-zA-Z0-9._-]", "");
            log.debug("Определено имя хоста: {}", cachedHostname);
            return cachedHostname;

        } catch (UnknownHostException e) {
            log.warn("Не удалось определить hostname через InetAddress: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Ошибка при получении имени хоста: {}", e.getMessage());
        }

        cachedHostname = "unknown-host";
        return cachedHostname;
    }

    public static Long getSystemUptimeMinutes(SystemInfo systemInfo) {
        try {
            OperatingSystem os = systemInfo.getOperatingSystem();
            long uptimeSeconds = os.getSystemUptime();
            return Math.round(uptimeSeconds / 60.0);
        } catch (Exception e) {
            log.debug("Не удалось получить uptime системы: {}", e.getMessage());
            return null;
        }
    }
}
