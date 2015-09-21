/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import lombok.Data;


/**
 * classe qui représente les adresses de l'étudiant
 * @author Charlie Dubois
 */
@Data
public class Adresse {

	/**
	 * annee pour une adresse annuelle
	 */
	private String annee;
	
	private String adresse1;
	
	private String adresse2;
	
	private String adresse3;
	
	private String adresseetranger;
	
	private String codePostal;
	
	private String ville;
	
	private String pays;
	
	private String codPays;
	
	private String numerotel;
	
	private String type;

	public Adresse() {
		super();
		
	}
	

	


}
