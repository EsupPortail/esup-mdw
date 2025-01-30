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
package fr.univlorraine.mondossierweb.views.windows;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * Fenêtre d'aide basique
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HelpBasicWindow extends Window {
	
	public static final String NAME = "helpBasicWindow";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient ConfigController configController;

	/* Composants */
	private Button btnFermer = new Button();



	public void addBtnNonListener(Button.ClickListener clickListener) {
		btnFermer.addClickListener(clickListener);
	}

	public void removeBtnNonListener(Button.ClickListener clickListener) {
		btnFermer.removeClickListener(clickListener);
	}

	/**
	 * Crée une fenêtre de confirmation avec un message et un titre par défaut
	 */
	public void init() {
		init(null, null);
	}

	/**
	 * Crée une fenêtre de confirmation avec un titre par défaut
	 * @param message
	 */
	public void init(String message) {
		init(message, null);
	}

	public void init(String message, String titre) {
		init(message, titre, false);
	}
	/**
	 * Crée une fenêtre de confirmation
	 * @param message
	 * @param titre
	 */
	public void init(String message, String titre,boolean displayLienContact) {
		/* Style */
		setWidth(900, Unit.PIXELS);
		setModal(true);
		setResizable(false);
		setClosable(false);

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(false);
		setContent(layout);

		/* Titre */
		setCaption(titre);

		// Lien de contact
		if(displayLienContact){
			String mailContact = configController.getAssistanceContactMail();
			if(StringUtils.hasText(mailContact)){
				Button contactBtn = new Button(applicationContext.getMessage(NAME + ".btnContact", null, getLocale()), FontAwesome.ENVELOPE);
				contactBtn.addStyleName(ValoTheme.BUTTON_LINK);
				BrowserWindowOpener contactBwo = new BrowserWindowOpener("mailto:" + mailContact);
				contactBwo.extend(contactBtn);
				layout.addComponent(contactBtn);
				layout.setComponentAlignment(contactBtn, Alignment.TOP_RIGHT);
			}
		}

		/* Texte */
		Label textLabel = new Label(message,ContentMode.HTML);
		layout.addComponent(textLabel);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);

		btnFermer.setCaption(applicationContext.getMessage("helpWindow.btnFermer", null, getLocale()));
		btnFermer.setIcon(FontAwesome.TIMES);
		btnFermer.addClickListener(e -> close());
		buttonsLayout.addComponent(btnFermer);
		buttonsLayout.setComponentAlignment(btnFermer, Alignment.MIDDLE_RIGHT);


		/* Centre la fenêtre */
		center();
	}

}
