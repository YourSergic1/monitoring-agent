package github.titandea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NetworkMetrics {

    private String interfaceName;

    private long bytesSent;

    private long bytesRecv;

    private long packetsSent;

    private long packetsRecv;

    private long inErrors;

    private long outErrors;

    private long speed;
}