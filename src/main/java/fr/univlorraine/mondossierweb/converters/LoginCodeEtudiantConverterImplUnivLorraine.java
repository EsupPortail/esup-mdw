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
