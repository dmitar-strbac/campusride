package com.campusride.chat;

import com.campusride.chat.dto.MarkChatReadRequest;
import com.campusride.chat.dto.UnreadCountResponse;
import com.campusride.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ride Chat", description = "Ride conversations, message history, and unread counts")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatReadController {

  private final ChatReadService chatReadService;
  private final SimpMessagingTemplate messagingTemplate;

  @Operation(summary = "Get my unread message counts")
  @GetMapping("/chats/unread")
  public List<UnreadCountResponse> getMyUnreadCounts(@AuthenticationPrincipal User user) {
    return chatReadService.getMyUnreadCounts(user);
  }

  @Operation(summary = "Mark ride messages as read")
  @PatchMapping("/rides/{rideId}/messages/read")
  public UnreadCountResponse markRead(
      @PathVariable Long rideId,
      @Valid @RequestBody MarkChatReadRequest request,
      @AuthenticationPrincipal User user) {

    UnreadCountResponse result =
        chatReadService.markRead(rideId, user.getId(), request.lastReadMessageId());

    messagingTemplate.convertAndSendToUser(user.getEmail(), "/queue/chats/unread", result);

    return result;
  }
}
