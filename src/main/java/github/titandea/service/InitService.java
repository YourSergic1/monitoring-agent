package github.titandea.service;

import github.titandea.dto.InitInfo;
import github.titandea.utils.IpUtils;
import github.titandea.utils.TimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InitService {
    @Value("${agent.organization}")
    private String organization;

    @Value("${agent.time.start}")
    private String agentTimeStart;

    @Value("${agent.time.continuous}")
    private Boolean continuous;

    @Value("${agent.time.end}")
    private String agentTimeEnd;

    private final SystemInfo systemInfo = new SystemInfo();

    public InitInfo collectInitInfo() {
        InitInfo initInfo = InitInfo.builder()
                .localIp(IpUtils.getLocalIp(systemInfo))
                .organization(UUID.fromString(organization))
                .build();
        if (continuous) {
            initInfo.setContinuous(continuous);
        } else {
            initInfo.setContinuous(continuous);
            initInfo.setStartTime(TimeFormatter.normalizeTime(agentTimeStart));
            initInfo.setEndTime(TimeFormatter.normalizeTime(agentTimeEnd));
        }
        return initInfo;
    }
}
