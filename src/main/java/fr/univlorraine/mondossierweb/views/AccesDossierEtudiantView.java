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
package fr.univlorraine.mondossierweb.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

/**
 * Deep linking vers le dossier d'un étudiant : /accesDossierEtudiant/{codEtu}
 * (remplace l'ancien contrôleur DeepLinking + fragment #!accesDossierEtudiant).
 */
@SuppressWarnings("serial")
@Route(value = AccesDossierEtudiantView.NAME, layout = MainLayout.class)
@Secured({Utils.TEACHER_USER, Utils.GEST_USER, Utils.ADMIN_USER})
public class AccesDossierEtudiantView extends Div implements HasUrlParameter<String> {

	public static final String NAME = "accesDossierEtudiant";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient UserController userController;

	@Override
	public void setParameter(final BeforeEvent event, final String codEtu) {
		if (StringUtils.hasText(codEtu) && userController.isEnseignant()) {
			rechercheController.accessToDossierEtudiantDeepLinking(NAME + "/" + codEtu);
		} else {
			event.forwardTo(ErreurView.class);
		}
	}
}
