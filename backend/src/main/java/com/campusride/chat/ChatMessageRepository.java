package com.campusride.chat;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

  @Query(
      """
            select m from ChatMessage m
            join fetch m.sender
            where m.ride.id = :rideId
              and (:beforeId is null or m.id < :beforeId)
            order by m.id desc
            """)
  List<ChatMessage> findRecent(
      @Param("rideId") Long rideId, @Param("beforeId") Long beforeId, Pageable pageable);

  boolean existsByIdAndRideId(Long id, Long rideId);
}
