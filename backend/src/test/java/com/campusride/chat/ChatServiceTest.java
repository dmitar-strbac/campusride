package com.campusride.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusride.bookings.BookingRepository;
import com.campusride.bookings.BookingStatus;
import com.campusride.chat.dto.ChatMessageResponse;
import com.campusride.chat.dto.SendChatMessageRequest;
import com.campusride.common.exceptions.ChatAccessDeniedException;
import com.campusride.common.exceptions.RideNotFoundException;
import com.campusride.rides.Ride;
import com.campusride.rides.RideRepository;
import com.campusride.users.Role;
import com.campusride.users.User;
import com.campusride.users.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock private RideRepository rideRepository;
  @Mock private BookingRepository bookingRepository;
  @Mock private ChatMessageRepository messageRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private ChatService chatService;

  @Test
  void requireAccess_shouldAllowDriver() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));

    chatService.requireAccess(10L, 1L);

    verify(bookingRepository, never()).existsByRideIdAndPassengerIdAndStatus(any(), any(), any());
  }

  @Test
  void requireAccess_shouldAllowAcceptedPassenger() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));
    when(bookingRepository.existsByRideIdAndPassengerIdAndStatus(10L, 2L, BookingStatus.ACCEPTED))
        .thenReturn(true);

    chatService.requireAccess(10L, 2L);
  }

  @Test
  void requireAccess_shouldRejectUserWithoutAcceptedBooking() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));

    assertThatThrownBy(() -> chatService.requireAccess(10L, 2L))
        .isInstanceOf(ChatAccessDeniedException.class);
  }

  @Test
  void requireAccess_shouldRejectUnknownRide() {
    when(rideRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> chatService.requireAccess(99L, 1L))
        .isInstanceOf(RideNotFoundException.class);
  }

  @Test
  void getMessages_shouldReturnRecentMessagesWithCursor() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));
    ChatMessage message = message();
    when(messageRepository.findRecent(10L, 50L, PageRequest.of(0, 20)))
        .thenReturn(List.of(message));

    List<ChatMessageResponse> result = chatService.getMessages(10L, 1L, 50L, 20);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).content()).isEqualTo("Hello");
    assertThat(result.get(0).senderName()).isEqualTo("Passenger Test");
    verify(messageRepository).findRecent(10L, 50L, PageRequest.of(0, 20));
  }

  @Test
  void getMessages_shouldRejectInvalidCursor() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));

    assertThatThrownBy(() -> chatService.getMessages(10L, 1L, 0L, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("beforeId must be positive");

    verify(messageRepository, never()).findRecent(any(), any(), any());
  }

  @Test
  void getMessages_shouldRejectUnauthorizedReaderBeforeQueryingMessages() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));

    assertThatThrownBy(() -> chatService.getMessages(10L, 2L, null, 20))
        .isInstanceOf(ChatAccessDeniedException.class);

    verify(messageRepository, never()).findRecent(any(), any(), any());
  }

  @Test
  void sendMessage_shouldTrimAndPersistWithAuthenticatedSender() {
    User passenger = passenger();
    when(userRepository.findByEmail(passenger.getEmail())).thenReturn(Optional.of(passenger));
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));
    when(bookingRepository.existsByRideIdAndPassengerIdAndStatus(10L, 2L, BookingStatus.ACCEPTED))
        .thenReturn(true);
    when(rideRepository.getReferenceById(10L)).thenReturn(ride());
    when(messageRepository.saveAndFlush(any(ChatMessage.class)))
        .thenAnswer(
            invocation -> {
              ChatMessage saved = invocation.getArgument(0);
              saved.setId(5L);
              saved.setCreatedAt(LocalDateTime.of(2026, 9, 26, 16, 0));
              return saved;
            });

    ChatMessageResponse result =
        chatService.sendMessage(10L, passenger.getEmail(), new SendChatMessageRequest("  Hello  "));

    assertThat(result.senderId()).isEqualTo(2L);
    assertThat(result.rideId()).isEqualTo(10L);
    assertThat(result.content()).isEqualTo("Hello");
    verify(messageRepository)
        .saveAndFlush(
            org.mockito.ArgumentMatchers.argThat(
                saved ->
                    saved.getSender().getId().equals(2L) && saved.getContent().equals("Hello")));
  }

  @Test
  void sendMessage_shouldRejectUnknownSender() {
    when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                chatService.sendMessage(
                    10L, "unknown@test.com", new SendChatMessageRequest("Hello")))
        .isInstanceOf(ChatAccessDeniedException.class);

    verify(messageRepository, never()).saveAndFlush(any());
  }

  @Test
  void sendMessage_shouldRejectUnauthorizedSender() {
    when(userRepository.findByEmail(passenger().getEmail())).thenReturn(Optional.of(passenger()));
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));

    assertThatThrownBy(
            () ->
                chatService.sendMessage(
                    10L, passenger().getEmail(), new SendChatMessageRequest("Hello")))
        .isInstanceOf(ChatAccessDeniedException.class);

    verify(messageRepository, never()).saveAndFlush(any());
  }

  @Test
  void sendMessage_shouldRejectWhitespaceOnlyContent() {
    when(userRepository.findByEmail(driver().getEmail())).thenReturn(Optional.of(driver()));
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));

    assertThatThrownBy(
            () ->
                chatService.sendMessage(
                    10L, driver().getEmail(), new SendChatMessageRequest("   ")))
        .isInstanceOf(IllegalArgumentException.class);

    verify(messageRepository, never()).saveAndFlush(any());
  }

  @Test
  void sendMessage_shouldRejectContentOverLimit() {
    when(userRepository.findByEmail(driver().getEmail())).thenReturn(Optional.of(driver()));
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));

    assertThatThrownBy(
            () ->
                chatService.sendMessage(
                    10L, driver().getEmail(), new SendChatMessageRequest("x".repeat(1001))))
        .isInstanceOf(IllegalArgumentException.class);

    verify(messageRepository, never()).saveAndFlush(any());
  }

  @Test
  void recipientEmails_shouldIncludeDriverAndAcceptedPassengers() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));
    when(bookingRepository.findPassengerEmailsByRideIdAndStatus(10L, BookingStatus.ACCEPTED))
        .thenReturn(Set.of("passenger@test.com"));

    assertThat(chatService.recipientEmails(10L))
        .containsExactlyInAnyOrder("driver@test.com", "passenger@test.com");
  }

  @Test
  void recipientEmails_shouldIncludeDriverWhenThereAreNoPassengers() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));
    when(bookingRepository.findPassengerEmailsByRideIdAndStatus(10L, BookingStatus.ACCEPTED))
        .thenReturn(Set.of());

    assertThat(chatService.recipientEmails(10L)).containsExactly("driver@test.com");
  }

  @Test
  void getMessages_shouldReturnFirstPageWithoutCursor() {
    when(rideRepository.findById(10L)).thenReturn(Optional.of(ride()));
    when(messageRepository.findRecent(10L, null, PageRequest.of(0, 30)))
        .thenReturn(List.of(message()));

    List<ChatMessageResponse> result = chatService.getMessages(10L, 1L, null, 30);

    assertThat(result).hasSize(1);
    verify(messageRepository).findRecent(10L, null, PageRequest.of(0, 30));
  }

  private static User driver() {
    return User.builder()
        .id(1L)
        .firstName("Driver")
        .lastName("Test")
        .email("driver@test.com")
        .role(Role.STUDENT)
        .build();
  }

  private static User passenger() {
    return User.builder()
        .id(2L)
        .firstName("Passenger")
        .lastName("Test")
        .email("passenger@test.com")
        .role(Role.STUDENT)
        .build();
  }

  private static Ride ride() {
    return Ride.builder().id(10L).driver(driver()).build();
  }

  private static ChatMessage message() {
    ChatMessage message = new ChatMessage();
    message.setId(5L);
    message.setRide(ride());
    message.setSender(passenger());
    message.setContent("Hello");
    message.setCreatedAt(LocalDateTime.of(2026, 9, 26, 16, 0));
    return message;
  }
}
