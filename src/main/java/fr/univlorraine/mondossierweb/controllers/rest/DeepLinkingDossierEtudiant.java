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
package fr.univlorraine.mondossierweb.controllers.rest;

import javax.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.univlorraine.mondossierweb.utils.Utils;


/**
 * Contrôleur REST pour la gestion de l'acces a un dossier
 */
@Controller
@RequestMapping("/"+Utils.FRAGMENT_ACCES_DOSSIER_ETUDIANT)
public class DeepLinkingDossierEtudiant {

	@Resource
	private Environment environment;


	/**
	 * redirige vers la bonne url (apres être passé par le CAS)
	 */
	@RequestMapping(value="/{codEtu}", method=RequestMethod.GET)
	public String accesDossier(@PathVariable String codEtu) {

		String path = environment.getRequiredProperty("app.url")+"#!"+Utils.FRAGMENT_ACCES_DOSSIER_ETUDIANT+"/"+codEtu;
		
		return "redirect:"+path;
	}
}

