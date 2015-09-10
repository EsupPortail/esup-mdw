/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;

import javax.annotation.Resource;

import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;



/**
 * la classe qui permet d'obtenir l'e-mail d'un �tudiant connaissant son login.
 * @author Charlie Dubois
 */
public class EmailConverterBasicImpl implements EmailConverterInterface{

	@Resource
	private transient ConfigController configController;
	
	/**
	 * le constructeur.
	 */
	public EmailConverterBasicImpl() {
		super();
	}
	
	/**
	 * @param login
	 * @return l'adresse mail.
	 */
	public String getMail( String login, String cod_etu) {
		// Gestion du cas ou le login est null ou vide
		if (login != null && !login.equals("") && configController.getExtensionMailEtudiant() != null && !configController.getExtensionMailEtudiant().equals("")) {
			return login + configController.getExtensionMailEtudiant();
		}
		return "";	
	}
	

}
