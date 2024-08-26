package com.saish.movie_ticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saish.movie_ticket.dto.Movie;
import com.saish.movie_ticket.dto.Screen;
import com.saish.movie_ticket.dto.Show;

public interface ShowRepository extends JpaRepository<Show, Integer> {
    List<Show> findByScreenIn(List<Screen> screens);

    boolean existsByScreenAndTimingAndAvailableTrueAndMovieIn(Screen screen, int timing, List<Movie> movies);

    List<Show> findByMovieAndAvailableTrue(Movie movie);
}
