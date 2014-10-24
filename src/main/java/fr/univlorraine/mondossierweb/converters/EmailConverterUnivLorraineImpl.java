/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.services.apogee.AnnuMelLoginApogeeService;



/**
 * la classe qui permet d'obtenir l'e-mail d'un étudiant connaissant son login.
 * @author Charlie Dubois
 */
@Component(value="emailConverter")
public class EmailConverterUnivLorraineImpl implements EmailConverterInterface{

	
	@Resource
	private AnnuMelLoginApogeeService annuMelLoginApogeeService;
	
	
	/**
	 * le constructeur.
	 */
	public EmailConverterUnivLorraineImpl() {
		super();
	}
	
	/**
	 * @param login
	 * @return l'adresse mail.
	 */
	public String getMail(String login, String cod_etu) {
		//Gestion du cas ou le login est null ou vide
		if (cod_etu != null && !cod_etu.equals("") ) {
			String mail="";
			//aller chercher le mail dans annu_mel_login
			mail =  annuMelLoginApogeeService.findMailFromCodEtu(cod_etu);
			if(mail == null)
				mail = "";
			return mail;
		}
		return "";	
	}
	
	
	
}
