package com.campusride;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.campusride.auth.JwtService;
import com.campusride.bookings.BookingRepository;
import com.campusride.bookings.BookingService;
import com.campusride.bookings.BookingStatus;
import com.campusride.bookings.dto.CreateBookingRequest;
import com.campusride.common.exceptions.InsufficientSeatsException;
import com.campusride.notifications.NotificationService;
import com.campusride.notifications.NotificationType;
import com.campusride.notifications.event.NotificationEvent;
import com.campusride.notifications.event.NotificationEventConsumer;
import com.campusride.notifications.event.NotificationEventPublisher;
import com.campusride.rides.Ride;
import com.campusride.rides.RideRepository;
import com.campusride.users.Role;
import com.campusride.users.User;
import com.campusride.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(
    properties = {
      "spring.config.import=",
      "spring.rabbitmq.dynamic=false",
      "spring.rabbitmq.listener.simple.auto-startup=false",
      "app.notifications.outbox-enabled=false",
      "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
      "spring.jpa.open-in-view=false"
    })
class BackendApplicationTests {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

  @Autowired JdbcTemplate jdbc;
  @Autowired UserRepository users;
  @Autowired RideRepository rides;
  @Autowired BookingRepository bookings;
  @Autowired BookingService bookingService;
  @Autowired NotificationService notifications;
  @Autowired NotificationEventPublisher publisher;
  @Autowired NotificationEventConsumer consumer;
  @Autowired ObjectMapper objectMapper;
  @Autowired PlatformTransactionManager transactionManager;
  @Autowired JwtService jwtService;
  @Autowired MockMvc mvc;

  private User driver;
  private User passenger;
  private Ride ride;

  @BeforeEach
  void prepareDatabase() {
    // Ovo je baza iz test containera, ne razvojna baza.
    jdbc.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");

    driver = users.save(user("driver@example.com"));
    passenger = users.save(user("passenger@example.com"));

    ride =
        rides.save(
            Ride.builder()
                .driver(driver)
                .origin("Novi Sad")
                .destination("Belgrade")
                .departureTime(LocalDateTime.now().plusDays(1))
                .availableSeats(1)
                .pricePerSeat(new BigDecimal("1000"))
                .build());
  }

  @Test
  void contextLoadsAndFlywayCreatesNotificationTables() {
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notifications", Long.class)).isZero();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notification_outbox", Long.class))
        .isZero();
  }

  @Test
  void rollsBackOutboxTogetherWithBusinessChange() {
    new TransactionTemplate(transactionManager)
        .executeWithoutResult(
            status -> {
              Ride locked = rides.findByIdForUpdate(ride.getId()).orElseThrow();
              locked.setAvailableSeats(0);

              publisher.publish(
                  passenger.getId(), NotificationType.BOOKING_ACCEPTED, locked, null, driver, 1);

              status.setRollbackOnly();
            });

    assertThat(rides.findById(ride.getId()).orElseThrow().getAvailableSeats()).isEqualTo(1);
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notification_outbox", Long.class))
        .isZero();
  }

  @Test
  void deduplicatesRepeatedEventAndPreservesOwnership() throws Exception {
    byte[] event = eventBody();

    consumer.consume(event);
    consumer.consume(event);

    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM notifications", Long.class)).isEqualTo(1L);
    assertThat(notifications.unreadCount(passenger.getId()).count()).isEqualTo(1);
    assertThat(notifications.unreadCount(driver.getId()).count()).isZero();

    Long id = jdbc.queryForObject("SELECT id FROM notifications", Long.class);

    mvc.perform(patch("/api/notifications/{id}/read", id).header("Authorization", bearer(driver)))
        .andExpect(status().isNotFound());

    mvc.perform(
            patch("/api/notifications/{id}/read", id).header("Authorization", bearer(passenger)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.readAt").isNotEmpty());

    assertThat(notifications.unreadCount(passenger.getId()).count()).isZero();
  }

  @Test
  void returns401WithoutTokenOrWithInvalidToken() throws Exception {
    mvc.perform(get("/api/notifications")).andExpect(status().isUnauthorized());

    mvc.perform(get("/api/notifications").header("Authorization", "Bearer invalid"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void returns400ForInvalidPaginationInsteadOf401() throws Exception {
    mvc.perform(
            get("/api/notifications").param("size", "0").header("Authorization", bearer(passenger)))
        .andExpect(status().isBadRequest());

    mvc.perform(
            get("/api/notifications")
                .param("page", "abc")
                .header("Authorization", bearer(passenger)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void acceptsOnlyOneBookingForLastSeat() throws Exception {
    User secondPassenger = users.save(user("second@example.com"));

    Long firstBooking =
        bookingService.requestBooking(ride.getId(), new CreateBookingRequest(1), passenger).id();
    Long secondBooking =
        bookingService
            .requestBooking(ride.getId(), new CreateBookingRequest(1), secondPassenger)
            .id();

    CountDownLatch start = new CountDownLatch(1);

    try (var executor = Executors.newFixedThreadPool(2)) {
      var first = executor.submit(acceptAfter(start, firstBooking));
      var second = executor.submit(acceptAfter(start, secondBooking));
      start.countDown();

      boolean firstAccepted = first.get(15, TimeUnit.SECONDS);
      boolean secondAccepted = second.get(15, TimeUnit.SECONDS);

      assertThat(firstAccepted ^ secondAccepted).isTrue();
    }

    assertThat(rides.findById(ride.getId()).orElseThrow().getAvailableSeats()).isZero();

    assertThat(
            bookings.findAll().stream()
                .filter(booking -> booking.getStatus() == BookingStatus.ACCEPTED)
                .count())
        .isEqualTo(1);
  }

  private Callable<Boolean> acceptAfter(CountDownLatch start, Long bookingId) {
    return () -> {
      start.await();
      try {
        bookingService.acceptBooking(bookingId, driver);
        return true;
      } catch (InsufficientSeatsException ex) {
        return false;
      }
    };
  }

  private byte[] eventBody() throws Exception {
    return objectMapper.writeValueAsBytes(
        new NotificationEvent(
            UUID.randomUUID(),
            passenger.getId(),
            NotificationType.RIDE_CANCELLED,
            ride.getId(),
            null,
            "Driver",
            "Novi Sad",
            "Belgrade",
            null,
            LocalDateTime.now()));
  }

  private String bearer(User user) {
    return "Bearer " + jwtService.generateToken(user.getEmail());
  }

  private User user(String email) {
    return User.builder()
        .firstName("Test")
        .lastName("User")
        .email(email)
        .password("unused-in-these-tests")
        .role(Role.STUDENT)
        .build();
  }
}
