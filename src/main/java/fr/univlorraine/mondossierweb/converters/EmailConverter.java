/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;



/**
 * la classe qui permet d'obtenir l'e-mail d'un �tudiant connaissant son login.
 * @author Charlie Dubois
 */
public class EmailConverter implements EmailConverterInterface{

	
	/**
	 * le constructeur.
	 */
	public EmailConverter() {
		super();
	}
	
	/**
	 * @param login
	 * @return l'adresse mail.
	 */
	public String getMail( String login, String cod_etu) {
		// Gestion du cas ou le login est null ou vide
		if (login != null && !login.equals("") && PropertyUtils.getExtensionMailEtudiant() != null && !PropertyUtils.getExtensionMailEtudiant().equals("")) {
			return login + PropertyUtils.getExtensionMailEtudiant();
		}
		return "";	
	}
	

}
