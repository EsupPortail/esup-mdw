/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2007 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.converters;




import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fr.univlorraine.mondossierweb.services.apogee.AnnuMelLoginApogeeService;




/**
 * Classe qui sait récupérer le login depuis le code étudiant.
 * @author Charlie Dubois
 *
 */
@Component(value="loginFromCodetuUnivLorraineImpl")
public class LoginCodeEtudiantConverterImplUnivLorraine implements LoginCodeEtudiantConverterInterface {

	private Logger LOG = LoggerFactory.getLogger(LoginCodeEtudiantConverterImplUnivLorraine.class);

	@Resource
	private AnnuMelLoginApogeeService annuMelLoginApogeeService;


	public LoginCodeEtudiantConverterImplUnivLorraine() {
		super();
	}


	public String getLoginFromCodEtu(final String codetu) {


		//Gestion du cas ou le login est null ou vide
		if (codetu != null && !codetu.equals("") ) {
			//aller chercher le mail dans annu_mel_login
			String login =  annuMelLoginApogeeService.findLoginFromCodEtu(codetu);
			if(login != null){
				return login;
			}
		}
		return null;
	}
}
