package pl.lukbol.ProjectSpring.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.lukbol.ProjectSpring.Configs.RabbitConfig;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActionLogProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendActionLog(String username, String action) {
        Map<String, String> log = new HashMap<>();
        log.put("userName", username);
        log.put("action", action);

        rabbitTemplate.convertAndSend(RabbitConfig.ACTION_LOG_QUEUE, log);
    }
}
