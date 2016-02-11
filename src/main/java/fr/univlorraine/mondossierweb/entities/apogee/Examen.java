/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.entities.apogee;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Data;

/**
 * classe qui représente un examen du calendrier.
 * @author Charlie Dubois
 */
@Entity
@Data
public class Examen {

	@Id
	@Column(name = "ID")
	private int id;

	/**
	 * date de l'événement.
	 */
	@Column(name = "datedeb")
	private String datedeb;
	/**
	 * heure de l'événement.
	 */
	@Column(name = "heure")
	private String heure;
	/**
	 * duree de l'événement.
	 */
	@Column(name = "duree")
	private String duree;
	/**
	 * salle ou se déroule l'événement.
	 */
	@Column(name = "salle")
	private String salle;
	/**
	 * libellé de la salle ou se déroule l'événement.
	 */
	@Column(name = "libsalle")
	private String libsalle;
	/**
	 * place dans la salle.
	 */
	@Column(name = "place")
	private String place;
	/**
	 * batiment ou se déroule l'événement.
	 */
	@Column(name = "batiment")
	private String batiment;
	/**
	 * localisation du batiment ou se déroule l'événement.
	 */
	@Column(name = "localisation")
	private String localisation;
	/**
	 * épreuve concernéé par l'événement.
	 */
	@Column(name = "epreuve")
	private String epreuve;
	/**
	 * centre incompatibilité concernév par l'événement.
	 */
	@Column(name = "codcin")
	private String codcin;

	
}
