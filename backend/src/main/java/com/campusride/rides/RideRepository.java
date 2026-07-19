package com.campusride.rides;

import com.campusride.users.User;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RideRepository extends JpaRepository<Ride, Long> {

  List<Ride> findByDriverOrderByDepartureTimeDesc(User driver);

  @Query(
      """
            select r from Ride r
            where r.status = :status
              and r.departureTime > :now
              and (:origin is null or lower(r.origin) like concat('%', lower(cast(:origin as string)), '%'))
              and (:destination is null or lower(r.destination) like concat('%', lower(cast(:destination as string)), '%'))
            order by r.departureTime asc
            """)
  List<Ride> searchActiveRides(
      RideStatus status, LocalDateTime now, String origin, String destination);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from Ride r where r.id = :id")
  Optional<Ride> findByIdForUpdate(@Param("id") Long id);
}
