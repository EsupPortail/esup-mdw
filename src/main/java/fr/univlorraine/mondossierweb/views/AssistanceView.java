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
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;

/**
 * Page d'assistance
 */
@SuppressWarnings("serial")
@Route(value = AssistanceView.NAME, layout = MainLayout.class)
public class AssistanceView extends VerticalLayout implements HasDynamicTitle {

	public static final String NAME = "assistanceView";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient ConfigController configController;

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

		//On vérifie le droit d'accéder à la vue
		if (configController.isApplicationActive() && userController.isEnseignant()) {
			/* Style */
			setPadding(true);
			setSpacing(true);

			// Récupération de la configuration pour cette vue
			String docUrl = configController.getAssistanceDocUrl();
			String helpdeskUrl = configController.getAssistanceHelpdeskUrl();
			String mailContact = configController.getAssistanceContactMail();

			/* Texte */
			if (StringUtils.hasText(getTranslation(NAME + ".text"))) {
				add(new Html("<div>" + getTranslation(NAME + ".text") + "</div>"));
			}

			// Afficher la pop-up de démarrage
			Button popupBtn = new Button(getTranslation(NAME + ".btnPopUp"), VaadinIcon.INFO_CIRCLE.create());
			popupBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			popupBtn.addClickListener(e -> MainLayout.getCurrent()
				.ifPresent(layout -> layout.afficherMessageIntroEnseignants(true, false)));
			add(popupBtn);

			// Accès à la documentation
			if (StringUtils.hasText(docUrl)) {
				add(createLinkButton(getTranslation(NAME + ".btnDoc"), VaadinIcon.FILE_TEXT_O.create(), docUrl));
			}

			// Envoyer un ticket
			if (StringUtils.hasText(helpdeskUrl)) {
				add(createLinkButton(getTranslation(NAME + ".btnHelpdesk"), VaadinIcon.AMBULANCE.create(), helpdeskUrl));
			}

			// Envoyer un mail
			if (StringUtils.hasText(mailContact)) {
				Button contactBtn = new Button(getTranslation(NAME + ".btnContact", mailContact), VaadinIcon.ENVELOPE_O.create());
				contactBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				Anchor contactAnchor = new Anchor("mailto:" + mailContact, contactBtn);
				add(contactAnchor);
			}

			// Deconnexion
			if (configController.isLogoutCasPropose()) {
				Button decoBtn = new Button(getTranslation("buttonDeconnexion.label"), VaadinIcon.SIGN_OUT.create());
				decoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				decoBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().setLocation("logout")));
				add(decoBtn);
			}
		}
	}

	private Anchor createLinkButton(final String label, final com.vaadin.flow.component.icon.Icon icon, final String url) {
		Button btn = new Button(label, icon);
		btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		Anchor anchor = new Anchor(url, btn);
		anchor.setTarget("_blank");
		return anchor;
	}

}
