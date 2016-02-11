/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;



/**
 * la classe qui permet d'obtenir l'e-mail d'un etudiant connaissant son codetu.
 * @author Charlie Dubois
 */
@Component(value="emailConverterBasicImpl")
public class EmailConverterImplBasic implements EmailConverterInterface{

	@Resource
	private transient ConfigController configController;
	
	@Resource(name="${loginFromCodetu.implementation}")
	private LoginCodeEtudiantConverterInterface loginCodeEtudiantConverter;
	
	/**
	 * le constructeur.
	 */
	public EmailConverterImplBasic() {
		super();
	}
	
	/**
	 * @param codetu
	 * @return l'adresse mail.
	 */
	public String getMail( String cod_etu) {

		String login = loginCodeEtudiantConverter.getLoginFromCodEtu(cod_etu);
		// Gestion du cas ou le login est null ou vide
		if (login != null && !login.equals("") && configController.getExtensionMailEtudiant() != null && !configController.getExtensionMailEtudiant().equals("")) {
			return login + configController.getExtensionMailEtudiant();
		}
		return "";	
	}
	

}
