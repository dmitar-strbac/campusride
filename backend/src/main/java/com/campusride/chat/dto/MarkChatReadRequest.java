package com.campusride.chat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MarkChatReadRequest(@NotNull @Positive Long lastReadMessageId) {}
