package com.campusride.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.chat.dto.ChatMessageResponse;
import com.campusride.chat.dto.SendChatMessageRequest;
import com.campusride.common.exceptions.ChatAccessDeniedException;
import com.campusride.users.Role;
import com.campusride.users.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

  @Mock private ChatService chatService;
  @Mock private SimpMessagingTemplate messagingTemplate;

  @InjectMocks private ChatController chatController;

  @Test
  void getMessages_shouldReturnRequestedPage() {
    User driver = User.builder().id(1L).role(Role.STUDENT).build();
    ChatMessageResponse response = response();
    when(chatService.getMessages(10L, 1L, 50L, 20)).thenReturn(List.of(response));

    assertThat(chatController.getMessages(10L, 50L, 20, driver)).containsExactly(response);
  }

  @Test
  void getMessages_shouldRejectLimitOutsideAllowedRange() {
    User driver = User.builder().id(1L).role(Role.STUDENT).build();

    assertThatThrownBy(() -> chatController.getMessages(10L, null, 0, driver))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> chatController.getMessages(10L, null, 101, driver))
        .isInstanceOf(IllegalArgumentException.class);

    verify(chatService, never())
        .getMessages(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyInt());
  }

  @Test
  void sendMessage_shouldDeliverOnlyToCurrentRecipients() {
    SendChatMessageRequest request = new SendChatMessageRequest("Hello");
    ChatMessageResponse response = response();

    when(chatService.sendMessage(10L, "driver@test.com", request)).thenReturn(response);
    when(chatService.recipientEmails(10L))
        .thenReturn(Set.of("driver@test.com", "accepted@test.com"));

    chatController.sendMessage(10L, request, () -> "driver@test.com");

    verify(messagingTemplate)
        .convertAndSendToUser("driver@test.com", "/queue/rides/10/chat", response);
    verify(messagingTemplate)
        .convertAndSendToUser("accepted@test.com", "/queue/rides/10/chat", response);
    verify(chatService).sendMessage(10L, "driver@test.com", request);
  }

  @Test
  void sendMessage_shouldNotDeliverWhenPersistingFails() {
    SendChatMessageRequest request = new SendChatMessageRequest("Hello");
    when(chatService.sendMessage(10L, "driver@test.com", request))
        .thenThrow(new ChatAccessDeniedException());

    assertThatThrownBy(() -> chatController.sendMessage(10L, request, () -> "driver@test.com"))
        .isInstanceOf(ChatAccessDeniedException.class);

    verify(chatService, never()).recipientEmails(10L);
    verify(messagingTemplate, never())
        .convertAndSendToUser(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any());
  }

  private static ChatMessageResponse response() {
    return new ChatMessageResponse(
        5L, 10L, 1L, "Driver Test", "Hello", LocalDateTime.of(2026, 9, 26, 16, 0));
  }
}
