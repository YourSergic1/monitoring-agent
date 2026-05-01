package github.titandea.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;

@UtilityClass
@Slf4j
public class IpUtils {

    private static final String[] IP_SERVICES = {
            "https://api.ipify.org",
            "https://ifconfig.me/ip",
            "https://ident.me",
            "https://ipinfo.io/ip"
    };

    private static final int TIMEOUT_MS = 3000;
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private static String cachedPublicIp;
    private static Instant cacheTimestamp;

    public static String getLocalIp(SystemInfo systemInfo) {
        return systemInfo.getHardware().getNetworkIFs().stream()
                .filter(net -> net.isKnownVmMacAddr() == false)  // исключаем виртуальные
                .filter(net -> net.getIPv4addr().length > 0)
                .map(net -> net.getIPv4addr()[0])
                .filter(ip -> !ip.startsWith("127.") && !ip.startsWith("0."))
                .findFirst()
                .orElse("127.0.0.1");
    }

    public static String getPublicIp() {
        if (cachedPublicIp != null && cacheTimestamp != null) {
            if (Duration.between(cacheTimestamp, Instant.now()).compareTo(CACHE_TTL) < 0) {
                return cachedPublicIp;
            }
        }

        for (String serviceUrl : IP_SERVICES) {
            try {
                String ip = fetchIpFromService(serviceUrl);
                if (ip != null && !ip.isEmpty() && isValidIp(ip)) {
                    cachedPublicIp = ip;
                    cacheTimestamp = Instant.now();
                    log.debug("Получен публичный IP: {} от {}", ip, serviceUrl);
                    return ip;
                }
            } catch (Exception e) {
                log.debug("Не удалось получить IP от {}: {}", serviceUrl, e.getMessage());
            }
        }

        log.warn("Не удалось получить публичный IP ни от одного сервиса");
        return null;
    }

    private static String fetchIpFromService(String serviceUrl) throws Exception {
        URL url = new URL(serviceUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "MonitoringAgent/1.0");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            conn.disconnect();
            return null;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            String ip = reader.readLine();
            conn.disconnect();
            return ip != null ? ip.trim() : null;
        }
    }

    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$") ||
                ip.matches("^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$");
    }
}
