package com.campusride.notifications.event;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.notifications.outbox-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class NotificationOutboxDispatcher {

  private final JdbcTemplate jdbc;
  private final NotificationBrokerPublisher brokerPublisher;

  private record PendingEvent(UUID id, String payload) {}

  @Scheduled(fixedDelayString = "${app.notifications.outbox-delay-ms:2000}")
  @Transactional
  public void dispatch() {
    List<PendingEvent> pending =
        jdbc.query(
            """
        SELECT id, payload
        FROM notification_outbox
        WHERE published_at IS NULL
        ORDER BY created_at, id
        LIMIT 10
        FOR UPDATE SKIP LOCKED
        """,
            (rs, row) -> new PendingEvent(rs.getObject("id", UUID.class), rs.getString("payload")));

    for (PendingEvent event : pending) {
      brokerPublisher.send(event.id(), event.payload());

      jdbc.update(
          """
          UPDATE notification_outbox
          SET published_at = CURRENT_TIMESTAMP
          WHERE id = ?
          """,
          event.id());
    }
  }

  @Scheduled(cron = "${app.notifications.outbox-cleanup-cron:0 0 3 * * *}")
  @Transactional
  public void cleanupPublished() {
    jdbc.update(
        """
        DELETE FROM notification_outbox
        WHERE published_at < CURRENT_TIMESTAMP - INTERVAL '30 days'
        """);
  }
}
