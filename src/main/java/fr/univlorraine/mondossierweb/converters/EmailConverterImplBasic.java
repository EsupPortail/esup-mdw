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
