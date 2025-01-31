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
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;



/**
 * Fenêtre de confirmation
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConfirmWindow extends Window {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;

	/* Composants */
	private Button btnOui = new Button();
	private Button btnNon = new Button();

	public void addBtnOuiListener(Button.ClickListener clickListener) {
		btnOui.addClickListener(clickListener);
	}

	public void removeBtnOuiListener(Button.ClickListener clickListener) {
		btnOui.removeClickListener(clickListener);
	}

	public void addBtnNonListener(Button.ClickListener clickListener) {
		btnNon.addClickListener(clickListener);
	}

	public void removeBtnNonListener(Button.ClickListener clickListener) {
		btnNon.removeClickListener(clickListener);
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

	/**
	 * Crée une fenêtre de confirmation
	 * @param message
	 * @param titre
	 */
	public void init(String message, String titre) {
		/* Style */
		setWidth(400, Unit.PIXELS);
		setModal(true);
		setResizable(false);
		setClosable(false);

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);

		/* Titre */
		if (titre == null) {
			titre = applicationContext.getMessage("confirmWindow.defaultTitle", null, getLocale());
		}
		setCaption(titre);

		/* Texte */
		if (message == null) {
			message = applicationContext.getMessage("confirmWindow.defaultQuestion", null, getLocale());
		}
		Label textLabel = new Label(message);
		layout.addComponent(textLabel);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);

		btnNon.setCaption(applicationContext.getMessage("confirmWindow.btnNon", null, getLocale()));
		btnNon.setIcon(FontAwesome.TIMES);
		btnNon.addClickListener(e -> close());
		buttonsLayout.addComponent(btnNon);
		buttonsLayout.setComponentAlignment(btnNon, Alignment.MIDDLE_LEFT);

		btnOui.setCaption(applicationContext.getMessage("confirmWindow.btnOui", null, getLocale()));
		btnOui.setIcon(FontAwesome.CHECK);
		btnOui.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnOui.addClickListener(e -> close());
		buttonsLayout.addComponent(btnOui);
		buttonsLayout.setComponentAlignment(btnOui, Alignment.MIDDLE_RIGHT);

		/* Centre la fenêtre */
		center();
	}

}
