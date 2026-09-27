package com.campusride.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.chat.dto.UnreadCountResponse;
import com.campusride.common.exceptions.ChatAccessDeniedException;
import com.campusride.users.User;
import java.sql.ResultSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class ChatReadServiceTest {

  @Mock private JdbcTemplate jdbc;
  @Mock private ChatService chatService;
  @Mock private ChatMessageRepository messageRepository;

  @InjectMocks private ChatReadService chatReadService;

  @Test
  void getMyUnreadCounts_shouldMapRideCountsForCurrentUser() throws Exception {
    User user = User.builder().id(2L).build();
    ResultSet resultSet = mock(ResultSet.class);

    when(resultSet.getLong("ride_id")).thenReturn(10L);
    when(resultSet.getLong("unread_count")).thenReturn(3L);

    when(jdbc.query(
            anyString(),
            org.mockito.ArgumentMatchers.<RowMapper<UnreadCountResponse>>any(),
            eq(2L),
            eq(2L),
            eq(2L),
            eq(2L)))
        .thenAnswer(
            invocation -> {
              RowMapper<UnreadCountResponse> mapper = invocation.getArgument(1);
              return List.of(mapper.mapRow(resultSet, 0));
            });

    assertThat(chatReadService.getMyUnreadCounts(user))
        .containsExactly(new UnreadCountResponse(10L, 3L));
  }

  @Test
  void getUnreadCount_shouldReturnNumberOfUnreadMessages() {
    when(jdbc.queryForObject(anyString(), eq(Long.class), eq(10L), eq(2L), eq(10L), eq(2L)))
        .thenReturn(3L);

    assertThat(chatReadService.getUnreadCount(10L, 2L)).isEqualTo(new UnreadCountResponse(10L, 3L));

    verify(chatService).requireAccess(10L, 2L);
  }

  @Test
  void getUnreadCount_shouldTreatNullCountAsZero() {
    when(jdbc.queryForObject(anyString(), eq(Long.class), eq(10L), eq(2L), eq(10L), eq(2L)))
        .thenReturn(null);

    assertThat(chatReadService.getUnreadCount(10L, 2L)).isEqualTo(new UnreadCountResponse(10L, 0L));
  }

  @Test
  void getUnreadCount_shouldCheckAccessBeforeQuery() {
    org.mockito.Mockito.doThrow(new ChatAccessDeniedException())
        .when(chatService)
        .requireAccess(10L, 2L);

    assertThatThrownBy(() -> chatReadService.getUnreadCount(10L, 2L))
        .isInstanceOf(ChatAccessDeniedException.class);

    verify(jdbc, never()).queryForObject(anyString(), eq(Long.class), any(), any(), any(), any());
  }

  @Test
  void markRead_shouldRejectMessageFromAnotherRide() {
    when(messageRepository.existsByIdAndRideId(50L, 10L)).thenReturn(false);

    assertThatThrownBy(() -> chatReadService.markRead(10L, 2L, 50L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Message does not belong to this ride");

    verify(chatService).requireAccess(10L, 2L);
    verify(jdbc, never()).update(anyString(), any(), any(), any());
  }

  @Test
  void markRead_shouldSaveProgressAndReturnUpdatedCount() {
    when(messageRepository.existsByIdAndRideId(50L, 10L)).thenReturn(true);
    when(jdbc.queryForObject(anyString(), eq(Long.class), eq(10L), eq(2L), eq(10L), eq(2L)))
        .thenReturn(1L);

    assertThat(chatReadService.markRead(10L, 2L, 50L)).isEqualTo(new UnreadCountResponse(10L, 1L));

    verify(jdbc).update(anyString(), eq(10L), eq(2L), eq(50L));
    verify(chatService, org.mockito.Mockito.times(2)).requireAccess(10L, 2L);
  }
}
