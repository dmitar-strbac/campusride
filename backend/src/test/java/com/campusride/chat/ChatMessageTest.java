package com.campusride.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatMessageTest {

  @Test
  void onCreate_shouldSetCreatedAt() {
    ChatMessage message = new ChatMessage();

    message.onCreate();

    assertThat(message.getCreatedAt()).isNotNull();
  }
}
