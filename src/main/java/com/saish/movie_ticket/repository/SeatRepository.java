package com.saish.movie_ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saish.movie_ticket.dto.Seat;

public interface SeatRepository extends JpaRepository<Seat, Integer> {

    List<Seat> findBySeatNumberIn(List<String> selectedSeats);

}
