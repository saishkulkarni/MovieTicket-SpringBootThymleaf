package com.saish.movie_ticket.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
public class Screen {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	@Transient
	private int row;
	@Transient
	private int column;

	@ManyToOne
	private Theatre theatre;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<Seat> seats = new ArrayList<>();
}
