package com.campusride.chat.dto;

public record TypingEvent(Long rideId, Long userId, String userName, boolean typing) {}
