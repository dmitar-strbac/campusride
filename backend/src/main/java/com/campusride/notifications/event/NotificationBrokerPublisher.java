package com.campusride.notifications.event;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationBrokerPublisher {

  private final RabbitTemplate rabbitTemplate;

  @Value("${app.notifications.confirm-timeout-ms:5000}")
  private long confirmTimeoutMs;

  public void send(UUID eventId, String payload) {
    MessageProperties properties = new MessageProperties();
    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
    properties.setMessageId(eventId.toString());

    Message message = new Message(payload.getBytes(StandardCharsets.UTF_8), properties);

    // Svaki publish pokušaj ima poseban correlation ID.
    CorrelationData correlation = new CorrelationData(UUID.randomUUID().toString());

    rabbitTemplate.send(
        NotificationRabbitConfig.EXCHANGE,
        NotificationRabbitConfig.ROUTING_KEY,
        message,
        correlation);

    try {
      CorrelationData.Confirm confirm =
          correlation.getFuture().get(confirmTimeoutMs, TimeUnit.MILLISECONDS);

      if (!confirm.isAck() || correlation.getReturned() != null) {
        throw new AmqpException("Notification was not confirmed and routed");
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new AmqpException("Notification publishing interrupted", ex);
    } catch (ExecutionException | TimeoutException ex) {
      throw new AmqpException("Notification confirmation failed", ex);
    }
  }
}
