package com.campusride.chat.dto;

import com.campusride.chat.ChatMessage;
import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id,
    Long rideId,
    Long senderId,
    String senderName,
    String content,
    LocalDateTime createdAt) {

  public static ChatMessageResponse from(ChatMessage message) {
    return new ChatMessageResponse(
        message.getId(),
        message.getRide().getId(),
        message.getSender().getId(),
        message.getSender().getFirstName() + " " + message.getSender().getLastName(),
        message.getContent(),
        message.getCreatedAt());
  }
}
