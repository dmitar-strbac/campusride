package com.campusride.config;

import com.campusride.auth.JwtService;
import com.campusride.chat.ChatService;
import com.campusride.users.User;
import com.campusride.users.UserRepository;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final Pattern SEND = Pattern.compile("^/app/rides/([1-9][0-9]*)/(?:chat|typing)$");
  private static final Pattern SUBSCRIBE =
      Pattern.compile("^/user/queue/rides/([1-9][0-9]*)/(?:chat|typing)$");

  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final ChatService chatService;

  @Value("${app.cors.allowed-origins}")
  private String allowedOrigins;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOrigins(
            Arrays.stream(allowedOrigins.split(",")).map(String::trim).toArray(String[]::new));
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
    registry.enableSimpleBroker("/queue");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(
        new ChannelInterceptor() {
          @Override
          public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor == null) {
              throw new AccessDeniedException("Invalid STOMP message");
            }

            StompCommand command = accessor.getCommand();
            if (command == null) {
              return message;
            }

            if (command == StompCommand.CONNECT) {
              String header = accessor.getFirstNativeHeader("Authorization");
              if (header == null || !header.startsWith("Bearer ")) {
                throw new AccessDeniedException("Missing bearer token");
              }

              try {
                String token = header.substring(7);
                String email = jwtService.extractUsername(token);
                User user =
                    userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new AccessDeniedException("Invalid token"));

                if (!jwtService.isTokenValid(token, user.getEmail())) {
                  throw new AccessDeniedException("Invalid token");
                }

                accessor.setUser(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
              } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
                throw new AccessDeniedException("Invalid token");
              }

              return message;
            }

            if (command == StompCommand.DISCONNECT) {
              return message;
            }

            if (accessor.getUser() == null) {
              throw new AccessDeniedException("Authentication required");
            }

            if (command == StompCommand.SUBSCRIBE
                && "/user/queue/chats/unread".equals(accessor.getDestination())) {
              return message;
            }

            if (command != StompCommand.SEND && command != StompCommand.SUBSCRIBE) {
              throw new AccessDeniedException("Unsupported STOMP command");
            }

            Pattern pattern = command == StompCommand.SEND ? SEND : SUBSCRIBE;
            Matcher matcher =
                pattern.matcher(accessor.getDestination() == null ? "" : accessor.getDestination());

            if (!matcher.matches()) {
              throw new AccessDeniedException("Invalid chat destination");
            }

            Long rideId;
            try {
              rideId = Long.valueOf(matcher.group(1));
            } catch (NumberFormatException ex) {
              throw new AccessDeniedException("Invalid ride ID");
            }

            User user =
                (User) ((UsernamePasswordAuthenticationToken) accessor.getUser()).getPrincipal();

            chatService.requireAccess(rideId, user.getId());
            return message;
          }
        });
  }
}
