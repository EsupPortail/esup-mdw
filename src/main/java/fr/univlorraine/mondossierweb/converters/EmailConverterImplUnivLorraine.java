/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.services.apogee.AnnuMelLoginApogeeService;



/**
 * la classe qui permet d'obtenir l'e-mail d'un étudiant connaissant son codetu.
 * @author Charlie Dubois
 */
@Component(value="emailConverterUnivLorraineImpl")
public class EmailConverterImplUnivLorraine implements EmailConverterInterface{


	@Resource
	private AnnuMelLoginApogeeService annuMelLoginApogeeService;


	/**
	 * le constructeur.
	 */
	public EmailConverterImplUnivLorraine() {
		super();
	}

	/**
	 * @param codetu
	 * @return l'adresse mail.
	 */
	public String getMail(String cod_etu) {
		//Gestion du cas ou le login est null ou vide
		if (cod_etu != null && !cod_etu.equals("") ) {
			//aller chercher le mail dans annu_mel_login
			String mail =  annuMelLoginApogeeService.findMailFromCodEtu(cod_etu);
			if(mail != null){
				return mail;
			}
		}
		/*	//On n'a rien récupéré avec le codetu, on tente avec le login
		if (login != null && !login.equals("") ) {
			String mail="";
			//aller chercher le mail dans annu_mel_login
			mail =  annuMelLoginApogeeService.findMailFromLogin(login);
			if(mail == null)
				mail = "";
			return mail;
		}*/
		return "";	
	}



}
