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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@SpringView(name = AssistanceView.NAME)
public class AssistanceView extends VerticalLayout implements View {
	private static final long serialVersionUID = 7356887304797399383L;

	public static final String NAME = "assistanceView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient ConfigController configController;
	
	@Resource
	private transient UserController userController;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if(configController.isApplicationActive() && userController.isEnseignant()){
			// Style 
			setMargin(true);
			setSpacing(true);

			// Récupération de la configuration pour cette vue
			String docUrl= configController.getAssistanceDocUrl();
			String helpdeskUrl= configController.getAssistanceHelpdeskUrl();
			String mailContact = configController.getAssistanceContactMail();


			// Titre 
			Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
			title.addStyleName(ValoTheme.LABEL_H1);
			addComponent(title);


			// Texte
			if(StringUtils.hasText(applicationContext.getMessage(NAME + ".text", null, getLocale()))){
				addComponent(new Label(applicationContext.getMessage(NAME + ".text", null, getLocale()), ContentMode.HTML));
			}
			
			// Afficher la pop-up de démarrage 
			Button popupBtn = new Button(applicationContext.getMessage(NAME + ".btnPopUp", null, getLocale()), FontAwesome.INFO);
			popupBtn.addStyleName(ValoTheme.BUTTON_LINK);
			popupBtn.addClickListener(e-> MainUI.getCurrent().afficherMessageIntroEnseignants(true, false));
			addComponent(popupBtn);


			// Accès à la documentation 
			if(StringUtils.hasText(docUrl)){
				Button docBtn = new Button(applicationContext.getMessage(NAME + ".btnDoc", null, getLocale()), FontAwesome.FILE_TEXT);
				docBtn.addStyleName(ValoTheme.BUTTON_LINK);
				BrowserWindowOpener docBwo = new BrowserWindowOpener(docUrl);
				docBwo.extend(docBtn);
				addComponent(docBtn);
			}

			// Envoyer un ticket 
			if(StringUtils.hasText(helpdeskUrl)){
				Button helpDeskBtn = new Button(applicationContext.getMessage(NAME + ".btnHelpdesk", null, getLocale()), FontAwesome.AMBULANCE);
				helpDeskBtn.addStyleName(ValoTheme.BUTTON_LINK);
				BrowserWindowOpener helpDeskBwo = new BrowserWindowOpener(helpdeskUrl);
				helpDeskBwo.extend(helpDeskBtn);
				addComponent(helpDeskBtn);
			}

			// Envoyer un mail 
			if(StringUtils.hasText(mailContact)){
				Button contactBtn = new Button(applicationContext.getMessage(NAME + ".btnContact", new Object[] {mailContact}, getLocale()), FontAwesome.ENVELOPE);
				contactBtn.addStyleName(ValoTheme.BUTTON_LINK);
				BrowserWindowOpener contactBwo = new BrowserWindowOpener("mailto:" + mailContact);
				contactBwo.extend(contactBtn);
				addComponent(contactBtn);
			}
			
			/* Deconnexion */
			//Voir si on peut accéder à l'appli hors ENT, le détecter, et afficher le bouton déconnexion
			if(configController.isLogoutCasPropose()){
				Button decoBtn = new Button("Déconnexion", FontAwesome.SIGN_OUT);
				decoBtn.addStyleName(ValoTheme.BUTTON_LINK);
				decoBtn.addClickListener(e -> {
					userController.disconnectUser();
					getUI().getPage().setLocation("logout");
				});
				addComponent(decoBtn);
			}
		}
	}

	/**
	 * @see com.vaadin.navigator.View${symbol_pound}enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
