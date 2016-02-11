/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import lombok.Data;

/**
 * Représente un groupe d'étudiants
 * @author chdubois
 *
 */
@Data
public class Groupe {

	private String cleGroupe;
	/**
	 * le code du groupe
	 */
	private String codGroupe;
	/**
	 * le libelle du groupe
	 */
	private String libGroupe;
	/**
	 * la capacite max du groupe
	 */
	private int capMaxGpe;
	/**
	 * la capacite intermédiaire du groupe
	 */
	private int capIntGpe;
	/**
	 * nombre d'inscrits dans le groupe
	 */
	private int nbInscrits;
	/**
	 * vrai si capInt superieure a zero, donc on l'affiche
	 */
	private boolean affCapIntGpe;
	
	public Groupe(String codGroupe,String libGroupe, int capMaxGpe, int capIntGpe) {
		super();
		this.capIntGpe = capIntGpe;
		this.capMaxGpe = capMaxGpe;
		this.codGroupe = codGroupe;
		this.libGroupe = libGroupe;
	}

	
	public Groupe(String codGroupe) {
		super();
		this.codGroupe = codGroupe;
	}


	
}
