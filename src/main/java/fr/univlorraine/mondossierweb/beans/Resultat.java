/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.beans;

import java.io.Serializable;

import lombok.Data;



/**
 * classe qui représente le résultat d'un étudiant.
 * @author Charlie Dubois
 */
@Data
public class Resultat implements Serializable{

	
	private static final long serialVersionUID = 4809562988462399839L;
		/**
		 * la session.
		 */
		private String session;
		/**
		 * la note.
		 */
		private String note;
		/**
		 * le bareme
		 */
		private int bareme;
		/**
		 * le résultat (admis ou pas).
		 */
		private String admission;
		/**
		 * le code de la mention
		 */
		private String codMention;
		/**
		 * le code de la mention
		 */
		private String libMention;
		
		

		
}
