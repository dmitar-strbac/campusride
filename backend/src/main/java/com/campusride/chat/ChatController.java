package com.campusride.chat;

import com.campusride.chat.dto.ChatMessageResponse;
import com.campusride.chat.dto.SendChatMessageRequest;
import com.campusride.chat.dto.TypingEvent;
import com.campusride.chat.dto.TypingRequest;
import com.campusride.common.exceptions.ChatAccessDeniedException;
import com.campusride.users.User;
import com.campusride.users.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Ride Chat", description = "Ride conversations, message history, and unread counts")
public class ChatController {

  private final ChatService chatService;
  private final SimpMessagingTemplate messagingTemplate;
  private final UserRepository userRepository;
  private final ChatReadService chatReadService;

  @Operation(summary = "Get ride chat history")
  @GetMapping("/{rideId}/messages")
  @ResponseBody
  public List<ChatMessageResponse> getMessages(
      @PathVariable Long rideId,
      @RequestParam(required = false) Long beforeId,
      @RequestParam(defaultValue = "30") int limit,
      @AuthenticationPrincipal User user) {

    if (limit < 1 || limit > 100) {
      throw new IllegalArgumentException("limit must be between 1 and 100");
    }

    return chatService.getMessages(rideId, user.getId(), beforeId, limit);
  }

  @MessageMapping("/rides/{rideId}/chat")
  public void sendMessage(
      @DestinationVariable Long rideId,
      @Valid SendChatMessageRequest request,
      Principal principal) {

    ChatMessageResponse saved = chatService.sendMessage(rideId, principal.getName(), request);

    for (String email : chatService.recipientEmails(rideId)) {
      messagingTemplate.convertAndSendToUser(email, "/queue/rides/" + rideId + "/chat", saved);
      User recipient =
          userRepository.findByEmail(email).orElseThrow(ChatAccessDeniedException::new);

      messagingTemplate.convertAndSendToUser(
          email, "/queue/chats/unread", chatReadService.getUnreadCount(rideId, recipient.getId()));
    }
  }

  @MessageMapping("/rides/{rideId}/typing")
  public void typing(@DestinationVariable Long rideId, TypingRequest request, Principal principal) {

    User sender =
        userRepository.findByEmail(principal.getName()).orElseThrow(ChatAccessDeniedException::new);

    chatService.requireAccess(rideId, sender.getId());

    TypingEvent event =
        new TypingEvent(
            rideId,
            sender.getId(),
            sender.getFirstName() + " " + sender.getLastName(),
            request.typing());

    for (String email : chatService.recipientEmails(rideId)) {
      if (!email.equals(sender.getEmail())) {
        messagingTemplate.convertAndSendToUser(email, "/queue/rides/" + rideId + "/typing", event);
      }
    }
  }
}
