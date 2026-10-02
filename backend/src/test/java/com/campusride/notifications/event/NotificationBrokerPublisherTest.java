package com.campusride.notifications.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationBrokerPublisherTest {

  @Mock RabbitTemplate rabbitTemplate;
  @InjectMocks NotificationBrokerPublisher publisher;

  @BeforeEach
  void configureTimeout() {
    ReflectionTestUtils.setField(publisher, "confirmTimeoutMs", 10L);
  }

  @Test
  void publishesPersistentJsonAndWaitsForConfirmation() {
    UUID eventId = UUID.randomUUID();

    doAnswer(
            invocation -> {
              Message message = invocation.getArgument(2);
              assertThat(message.getMessageProperties().getMessageId())
                  .isEqualTo(eventId.toString());
              assertThat(message.getMessageProperties().getContentType())
                  .isEqualTo("application/json");
              assertThat(message.getMessageProperties().getDeliveryMode())
                  .isEqualTo(org.springframework.amqp.core.MessageDeliveryMode.PERSISTENT);

              CorrelationData correlation = invocation.getArgument(3);
              correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
              return null;
            })
        .when(rabbitTemplate)
        .send(
            eq(NotificationRabbitConfig.EXCHANGE),
            eq(NotificationRabbitConfig.ROUTING_KEY),
            any(Message.class),
            any(CorrelationData.class));

    publisher.send(eventId, "{}");
  }

  @Test
  void rejectsBrokerNack() {
    completeConfirmation(false, false, false);

    assertThatThrownBy(() -> publisher.send(UUID.randomUUID(), "{}"))
        .isInstanceOf(AmqpException.class);
  }

  @Test
  void rejectsUnroutableMessageEvenWhenBrokerAcknowledges() {
    completeConfirmation(true, true, false);

    assertThatThrownBy(() -> publisher.send(UUID.randomUUID(), "{}"))
        .isInstanceOf(AmqpException.class);
  }

  @Test
  void rejectsFailedConfirmationFuture() {
    completeConfirmation(false, false, true);

    assertThatThrownBy(() -> publisher.send(UUID.randomUUID(), "{}"))
        .isInstanceOf(AmqpException.class);
  }

  @Test
  void rejectsConfirmationTimeout() {
    assertThatThrownBy(() -> publisher.send(UUID.randomUUID(), "{}"))
        .isInstanceOf(AmqpException.class);
  }

  @Test
  void preservesInterruptFlag() {
    Thread.currentThread().interrupt();
    try {
      assertThatThrownBy(() -> publisher.send(UUID.randomUUID(), "{}"))
          .isInstanceOf(AmqpException.class);
      assertThat(Thread.currentThread().isInterrupted()).isTrue();
    } finally {
      Thread.interrupted();
    }
  }

  private void completeConfirmation(boolean ack, boolean returned, boolean exceptional) {

    doAnswer(
            invocation -> {
              CorrelationData correlation = invocation.getArgument(3);

              if (returned) {
                correlation.setReturned(
                    new ReturnedMessage(
                        invocation.getArgument(2),
                        312,
                        "NO_ROUTE",
                        NotificationRabbitConfig.EXCHANGE,
                        NotificationRabbitConfig.ROUTING_KEY));
              }

              if (exceptional) {
                correlation
                    .getFuture()
                    .completeExceptionally(new IllegalStateException("Disconnected"));
              } else {
                correlation.getFuture().complete(new CorrelationData.Confirm(ack, null));
              }

              return null;
            })
        .when(rabbitTemplate)
        .send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
  }
}
