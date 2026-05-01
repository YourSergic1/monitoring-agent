package github.titandea.sheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.titandea.dto.SystemMetrics;
import github.titandea.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsScheduler {

    private final MonitoringService monitoringService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${agent.kafka-topic}")
    private String kafkaTopic;

    @Scheduled(fixedDelayString = "${agent.collection-interval-ms}")
    public void collectAndSendMetrics() {
        log.debug("Запуск цикла сбора метрик...");

        try {
            SystemMetrics metrics = monitoringService.collectMetrics();

            if (metrics == null) {
                log.warn("Сбор метрик завершился неудачей (возвращён null). Отправка пропущена.");
                return;
            }

            String payload = objectMapper.writeValueAsString(metrics);
            String key = metrics.getLocalIp();

            kafkaTemplate.send(kafkaTopic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Ошибка отправки метрик в Kafka: {}", ex.getMessage());
                        } else {
                            log.info("Метрики отправлены. Агент: {}, Тема: {}", key, kafkaTopic);
                        }
                    });

        } catch (Exception e) {
            log.error("Критическая ошибка в планировщике метрик: {}", e.getMessage(), e);
        }
    }
}
