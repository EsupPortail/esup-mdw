/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;

/**
 * interface listant les méthodes devant être implémentées par la classe 
 * qui récupérera le login depuis le code étudiant.
 * @author Charlie Dubois
 *
 */
public interface LoginCodeEtudiantConverterInterface {

	/**
	 * @param codetu
	 * @return le login d'un étudiant à partir de son codetu
	 */
	String getLoginFromCodEtu(String codetu);
	
	
}
