package pl.lukbol.ProjectSpring.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.lukbol.ProjectSpring.Configs.RabbitConfig;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class MailProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendResetPasswordMail(String to, String subject, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("recipient", to);
        message.put("subject", subject);
        message.put("content", content);

        rabbitTemplate.convertAndSend(RabbitConfig.MAIL_QUEUE, message);
    }

}