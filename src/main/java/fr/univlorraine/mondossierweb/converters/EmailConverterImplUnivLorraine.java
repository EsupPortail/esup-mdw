/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.converters;

import jakarta.annotation.Resource;

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
