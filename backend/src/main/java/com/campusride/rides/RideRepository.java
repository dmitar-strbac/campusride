package com.campusride.rides;

import com.campusride.users.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RideRepository extends JpaRepository<Ride, Long> {

  List<Ride> findByDriverOrderByDepartureTimeDesc(User driver);

  @Query(
      """
            select r from Ride r
            where r.status = :status
              and r.departureTime > :now
              and (:origin is null or lower(r.origin) like lower(concat('%', :origin, '%')))
              and (:destination is null or lower(r.destination) like lower(concat('%', :destination, '%')))
            order by r.departureTime asc
            """)
  List<Ride> searchActiveRides(
      RideStatus status, LocalDateTime now, String origin, String destination);
}
