package com.campusride.notifications.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxDispatcherTest {

  @Mock JdbcTemplate jdbc;
  @Mock NotificationBrokerPublisher brokerPublisher;
  @InjectMocks NotificationOutboxDispatcher dispatcher;

  @Test
  void doesNothingWhenOutboxIsEmpty() {
    when(jdbc.query(anyString(), any(RowMapper.class))).thenReturn(List.of());

    dispatcher.dispatch();

    verifyNoInteractions(brokerPublisher);
    verify(jdbc, never()).update(anyString(), any(Object[].class));
  }

  @Test
  void marksPublishedOnlyAfterSuccessfulDelivery() throws Exception {
    UUID eventId = stubPendingEvent();

    dispatcher.dispatch();

    var order = inOrder(brokerPublisher, jdbc);
    order.verify(brokerPublisher).send(eventId, "{}");
    order.verify(jdbc).update(anyString(), eq(eventId));
  }

  @Test
  void doesNotMarkFailedDeliveryPublished() throws Exception {
    UUID eventId = stubPendingEvent();
    doThrow(new AmqpException("Offline")).when(brokerPublisher).send(eventId, "{}");

    assertThatThrownBy(dispatcher::dispatch).isInstanceOf(AmqpException.class);

    verify(jdbc, never()).update(anyString(), any(Object[].class));
  }

  @Test
  void removesOnlyOldPublishedEvents() {
    dispatcher.cleanupPublished();

    verify(jdbc)
        .update(
            """
        DELETE FROM notification_outbox
        WHERE published_at < CURRENT_TIMESTAMP - INTERVAL '30 days'
        """);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private UUID stubPendingEvent() throws Exception {
    UUID eventId = UUID.randomUUID();
    ResultSet resultSet = mock(ResultSet.class);
    when(resultSet.getObject("id", UUID.class)).thenReturn(eventId);
    when(resultSet.getString("payload")).thenReturn("{}");

    when(jdbc.query(anyString(), any(RowMapper.class)))
        .thenAnswer(
            invocation -> {
              RowMapper mapper = invocation.getArgument(1);
              return List.of(mapper.mapRow(resultSet, 0));
            });

    return eventId;
  }
}
