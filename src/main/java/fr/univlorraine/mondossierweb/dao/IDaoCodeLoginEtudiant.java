/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.dao;

/**
 * interface listant les méthodes devant être implémentées par la classe 
 * qui récupérera le cod_etu depuis le login Etudiant.
 * @author Charlie Dubois
 *
 */
public interface IDaoCodeLoginEtudiant {

	/**
	 * @param login
	 * @return le codetu d'un étudiant à partir de son login
	 */
	String getCodEtuFromLogin(String login);
	
	
}
