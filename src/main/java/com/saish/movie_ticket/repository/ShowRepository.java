package com.saish.movie_ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saish.movie_ticket.dto.Show;

public interface ShowRepository extends JpaRepository<Show, Integer> {

}
