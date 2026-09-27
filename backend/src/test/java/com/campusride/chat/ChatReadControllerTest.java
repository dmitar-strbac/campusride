package com.campusride.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.chat.dto.MarkChatReadRequest;
import com.campusride.chat.dto.UnreadCountResponse;
import com.campusride.users.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatReadControllerTest {

  @Mock private ChatReadService chatReadService;
  @Mock private SimpMessagingTemplate messagingTemplate;

  @InjectMocks private ChatReadController chatReadController;

  @Test
  void getMyUnreadCounts_shouldReturnCurrentUsersCounts() {
    User user = User.builder().id(2L).email("student@test.com").build();
    List<UnreadCountResponse> counts = List.of(new UnreadCountResponse(10L, 3L));

    when(chatReadService.getMyUnreadCounts(user)).thenReturn(counts);

    assertThat(chatReadController.getMyUnreadCounts(user)).containsExactlyElementsOf(counts);
  }

  @Test
  void markRead_shouldPublishUpdatedCountToCurrentUser() {
    User user = User.builder().id(2L).email("student@test.com").build();
    MarkChatReadRequest request = new MarkChatReadRequest(50L);
    UnreadCountResponse result = new UnreadCountResponse(10L, 1L);

    when(chatReadService.markRead(10L, 2L, 50L)).thenReturn(result);

    assertThat(chatReadController.markRead(10L, request, user)).isEqualTo(result);

    verify(messagingTemplate)
        .convertAndSendToUser("student@test.com", "/queue/chats/unread", result);
  }
}
