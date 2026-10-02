package com.campusride.notifications.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitConfig {

  public static final String EXCHANGE = "campusride.notifications";
  public static final String QUEUE = "campusride.notifications.v1";
  public static final String ROUTING_KEY = "notification.created";
  public static final String DEAD_LETTER_EXCHANGE = "campusride.notifications.dlx";
  public static final String DEAD_LETTER_QUEUE = "campusride.notifications.dead.v1";

  @Bean
  DirectExchange notificationExchange() {
    return new DirectExchange(EXCHANGE, true, false);
  }

  @Bean
  DirectExchange notificationDeadLetterExchange() {
    return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
  }

  @Bean
  Queue notificationQueue() {
    return QueueBuilder.durable(QUEUE)
        .deadLetterExchange(DEAD_LETTER_EXCHANGE)
        .deadLetterRoutingKey(ROUTING_KEY)
        .build();
  }

  @Bean
  Queue notificationDeadLetterQueue() {
    return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
  }

  @Bean
  Binding notificationBinding() {
    return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with(ROUTING_KEY);
  }

  @Bean
  Binding notificationDeadLetterBinding() {
    return BindingBuilder.bind(notificationDeadLetterQueue())
        .to(notificationDeadLetterExchange())
        .with(ROUTING_KEY);
  }
}
