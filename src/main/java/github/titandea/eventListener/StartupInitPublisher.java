package github.titandea.eventListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.titandea.dto.InitInfo;
import github.titandea.service.InitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupInitPublisher {
    private final InitService initService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.init-topic}")
    private String kafkaInitTopic;

    @EventListener(ApplicationReadyEvent.class)
    public void collectAndSendInitInfo() {
        log.debug("Запуск цикла сбора информации.");

        try {
            InitInfo initInfo = initService.collectInitInfo();

            if (initInfo == null) {
                log.warn("Сбор информации завершился неудачей (возвращён null). Отправка пропущена.");
                return;
            }

            String payload = objectMapper.writeValueAsString(initInfo);
            String key = initInfo.getLocalIp();

            kafkaTemplate.send(kafkaInitTopic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Ошибка отправки информации в Kafka: {}", ex.getMessage());
                        } else {
                            log.info("Информация отправлена. Агент: {}, Тема: {}", key, kafkaInitTopic);
                        }
                    });

        } catch (Exception e) {
            log.error("Критическая ошибка в сборке информации: {}", e.getMessage(), e);
        }
    }
}
