package com.campusride.chat;

import com.campusride.chat.dto.UnreadCountResponse;
import com.campusride.users.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatReadService {

  private final JdbcTemplate jdbc;
  private final ChatService chatService;
  private final ChatMessageRepository messageRepository;

  @Transactional(readOnly = true)
  public List<UnreadCountResponse> getMyUnreadCounts(User user) {
    return jdbc.query(
        """
                        SELECT m.ride_id, COUNT(*) AS unread_count
                        FROM chat_messages m
                        JOIN rides r ON r.id = m.ride_id
                        WHERE m.sender_id <> ?
                          AND m.id > COALESCE(
                            (SELECT s.last_read_message_id
                             FROM chat_read_state s
                             WHERE s.ride_id = m.ride_id AND s.user_id = ?),
                            0
                          )
                          AND (
                            r.driver_id = ?
                            OR EXISTS (
                              SELECT 1 FROM bookings b
                              WHERE b.ride_id = m.ride_id
                                AND b.passenger_id = ?
                                AND b.status = 'ACCEPTED'
                            )
                          )
                        GROUP BY m.ride_id
                        """,
        (rs, rowNum) -> new UnreadCountResponse(rs.getLong("ride_id"), rs.getLong("unread_count")),
        user.getId(),
        user.getId(),
        user.getId(),
        user.getId());
  }

  @Transactional(readOnly = true)
  public UnreadCountResponse getUnreadCount(Long rideId, Long userId) {
    chatService.requireAccess(rideId, userId);

    Long count =
        jdbc.queryForObject(
            """
                        SELECT COUNT(*)
                        FROM chat_messages m
                        WHERE m.ride_id = ?
                          AND m.sender_id <> ?
                          AND m.id > COALESCE(
                            (SELECT s.last_read_message_id
                             FROM chat_read_state s
                             WHERE s.ride_id = ? AND s.user_id = ?),
                            0
                          )
                        """,
            Long.class,
            rideId,
            userId,
            rideId,
            userId);

    return new UnreadCountResponse(rideId, count == null ? 0 : count);
  }

  @Transactional
  public UnreadCountResponse markRead(Long rideId, Long userId, Long lastReadMessageId) {

    chatService.requireAccess(rideId, userId);

    if (!messageRepository.existsByIdAndRideId(lastReadMessageId, rideId)) {
      throw new IllegalArgumentException("Message does not belong to this ride");
    }

    jdbc.update(
        """
                        INSERT INTO chat_read_state
                          (ride_id, user_id, last_read_message_id)
                        VALUES (?, ?, ?)
                        ON CONFLICT (ride_id, user_id)
                        DO UPDATE SET last_read_message_id =
                          GREATEST(
                            chat_read_state.last_read_message_id,
                            EXCLUDED.last_read_message_id
                          )
                        """,
        rideId,
        userId,
        lastReadMessageId);

    return getUnreadCount(rideId, userId);
  }
}
