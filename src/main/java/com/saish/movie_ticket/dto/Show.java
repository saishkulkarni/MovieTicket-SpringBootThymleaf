package com.saish.movie_ticket.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "MovieShow")
public class Show {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private int timing;
	private boolean available;
	private int ticketPrice;

	@ManyToOne
	private Movie movie;

	@ManyToOne
	private Screen screen;

}
