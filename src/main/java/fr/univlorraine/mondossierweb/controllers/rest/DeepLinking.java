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
import fr.univlorraine.mondossierweb.views.AdminView;


/**
 * Contrôleur REST pour la gestion de l'acces a un dossier et à l'adminView
 */
@Controller
public class DeepLinking {

	private static final String VAADIN_FRAGMENT_PREFIX = "#!";
	
	@Resource
	private Environment environment;


	/**
	 * redirige vers la bonne url (apres être passé par le CAS)
	 */
	@RequestMapping(value= "/" + Utils.FRAGMENT_ACCES_DOSSIER_ETUDIANT + "/{codEtu}", method=RequestMethod.GET)
	public String accesDossier(@PathVariable String codEtu) {

		String path = environment.getRequiredProperty("app.url") + VAADIN_FRAGMENT_PREFIX + Utils.FRAGMENT_ACCES_DOSSIER_ETUDIANT + "/" + codEtu;
		
		return "redirect:" + path;
	}
	
	@RequestMapping(value="/" + AdminView.NAME, method=RequestMethod.GET)
	public String accesAdminView() {

		String path = environment.getRequiredProperty("app.url") + VAADIN_FRAGMENT_PREFIX + AdminView.NAME;
		
		return "redirect:" + path;
	}
}

