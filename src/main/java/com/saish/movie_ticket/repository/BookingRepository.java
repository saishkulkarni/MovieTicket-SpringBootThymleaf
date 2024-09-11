package com.saish.movie_ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saish.movie_ticket.dto.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

}
