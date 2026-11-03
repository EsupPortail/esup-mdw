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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.security.MdwUserDetails;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

/**
 * Page accès refusé
 */
@SuppressWarnings("serial")
@Route(value = AccesRefuseView.NAME, layout = MainLayout.class)
public class AccesRefuseView extends VerticalLayout implements HasDynamicTitle {

	public static final String NAME = "accesRefuseView";
	public static final String ROUTE = NAME;

	@Resource
	private transient UserController userController;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		/* Style */
		setPadding(true);
		setSpacing(true);

		/* Titre */
		add(new H1(getTranslation(NAME + ".title")));

		/* Texte : message dédié si l'accès a été refusé pour un blocage Apogée */
		add(new Html("<div>" + getTranslation(getMessageKey()) + "</div>"));
	}

	/**
	 * @return la clé du message à afficher selon le motif de refus de l'utilisateur courant
	 */
	private String getMessageKey() {
		final MdwUserDetails user = userController.getCurrentUser();
		return user != null && Utils.MOTIF_BLOCAGE.equals(user.getMotifRefus())
			? NAME + ".message.blocage"
			: NAME + ".message";
	}
}
