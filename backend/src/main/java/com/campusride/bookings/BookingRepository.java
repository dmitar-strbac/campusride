package com.campusride.bookings;

import com.campusride.rides.Ride;
import com.campusride.users.User;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  boolean existsByRideAndPassengerAndStatusIn(
      Ride ride, User passenger, Collection<BookingStatus> statuses);

  List<Booking> findByPassengerOrderByRideDepartureTimeDesc(User passenger);

  List<Booking> findByRideOrderByCreatedAtDesc(Ride ride);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Booking b where b.id = :id")
  Optional<Booking> findByIdForUpdate(@Param("id") Long id);
}
