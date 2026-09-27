package com.campusride.chat;

import com.campusride.bookings.BookingRepository;
import com.campusride.bookings.BookingStatus;
import com.campusride.chat.dto.ChatMessageResponse;
import com.campusride.chat.dto.SendChatMessageRequest;
import com.campusride.common.exceptions.ChatAccessDeniedException;
import com.campusride.common.exceptions.RideNotFoundException;
import com.campusride.rides.Ride;
import com.campusride.rides.RideRepository;
import com.campusride.users.User;
import com.campusride.users.UserRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final RideRepository rideRepository;
  private final BookingRepository bookingRepository;
  private final ChatMessageRepository messageRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public void requireAccess(Long rideId, Long userId) {
    Ride ride = rideRepository.findById(rideId).orElseThrow(RideNotFoundException::new);

    if (!ride.getDriver().getId().equals(userId)
        && !bookingRepository.existsByRideIdAndPassengerIdAndStatus(
            rideId, userId, BookingStatus.ACCEPTED)) {
      throw new ChatAccessDeniedException();
    }
  }

  @Transactional(readOnly = true)
  public List<ChatMessageResponse> getMessages(Long rideId, Long userId, Long beforeId, int limit) {

    requireAccess(rideId, userId);

    if (beforeId != null && beforeId < 1) {
      throw new IllegalArgumentException("beforeId must be positive");
    }

    return messageRepository.findRecent(rideId, beforeId, PageRequest.of(0, limit)).stream()
        .map(ChatMessageResponse::from)
        .toList();
  }

  @Transactional
  public ChatMessageResponse sendMessage(
      Long rideId, String email, SendChatMessageRequest request) {

    User sender = userRepository.findByEmail(email).orElseThrow(ChatAccessDeniedException::new);

    requireAccess(rideId, sender.getId());

    String content = request.content().trim();
    if (content.isEmpty() || content.length() > 1000) {
      throw new IllegalArgumentException("Message must contain 1 to 1000 characters");
    }

    Ride ride = rideRepository.getReferenceById(rideId);
    ChatMessage message = new ChatMessage();
    message.setRide(ride);
    message.setSender(sender);
    message.setContent(content);

    return ChatMessageResponse.from(messageRepository.saveAndFlush(message));
  }

  @Transactional(readOnly = true)
  public Set<String> recipientEmails(Long rideId) {
    Ride ride = rideRepository.findById(rideId).orElseThrow(RideNotFoundException::new);
    Set<String> emails =
        new java.util.HashSet<>(
            bookingRepository.findPassengerEmailsByRideIdAndStatus(rideId, BookingStatus.ACCEPTED));
    emails.add(ride.getDriver().getEmail());
    return emails;
  }
}
