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

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Fenêtre d'aide
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HelpMobileWindow extends Window {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;

	/* Composants */
	private Button btnFermer = new Button();
	@Getter
	private CheckBox checkBox=new CheckBox();

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
		init(null, null,false);
	}

	/**
	 * Crée une fenêtre de confirmation avec un titre par défaut
	 * @param message
	 */
	public void init(String message) {
		init(message, null,false);
	}

	/**
	 * Crée une fenêtre de confirmation
	 * @param message
	 * @param titre
	 */
	public void init(String message, String titre, boolean displayCheckBox) {
		// Style 
		setWidth("90%");
		setModal(true);
		setResizable(false);
		setClosable(false);

		// Layout 
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);

		// Titre 
		if (titre == null) {
			titre = applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale());
		}
		setCaption(titre);

		// Texte 
		Label textLabel = new Label(message,ContentMode.HTML);
		layout.addComponent(textLabel);

		// Boutons
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);

		if(displayCheckBox) {
			// Checkbox "ne plus afficher ce message"
			checkBox.setCaption(applicationContext.getMessage("helpWindow.checkBox.mobile.message", null, getLocale()));
			buttonsLayout.addComponent(checkBox);
			buttonsLayout.setComponentAlignment(checkBox, Alignment.MIDDLE_RIGHT);
		}
		// Bouton "Fermer"
		btnFermer.setIcon(FontAwesome.CHECK);
		btnFermer.setStyleName(ValoTheme.BUTTON_PRIMARY);
		btnFermer.addStyleName("v-popover-window-button");
		btnFermer.addClickListener(e -> close());
		buttonsLayout.addComponent(btnFermer);
		buttonsLayout.setComponentAlignment(btnFermer, Alignment.MIDDLE_RIGHT);
		if(displayCheckBox) {
			buttonsLayout.setExpandRatio(checkBox, 1);
		}
		// Centre la fenêtre 
		center();
	}
}
