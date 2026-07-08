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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.shared.Registration;

/**
 * Fenêtre de confirmation
 */
@SuppressWarnings("serial")
public class ConfirmWindow extends Dialog {

	/* Composants */
	private final Button btnOui = new Button();
	private final Button btnNon = new Button();

	/**
	 * Crée une fenêtre de confirmation avec un message et un titre par défaut
	 */
	public ConfirmWindow() {
		this(null, null);
	}

	/**
	 * Crée une fenêtre de confirmation avec un titre par défaut
	 */
	public ConfirmWindow(final String message) {
		this(message, null);
	}

	/**
	 * Crée une fenêtre de confirmation
	 */
	public ConfirmWindow(final String message, final String titre) {
		/* Style */
		setWidth("400px");
		setResizable(false);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);

		/* Titre */
		setHeaderTitle(titre != null ? titre : getTranslation("confirmWindow.defaultTitle"));

		/* Layout */
		final VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(true);
		add(layout);

		/* Texte */
		layout.add(new Paragraph(message != null ? message : getTranslation("confirmWindow.defaultQuestion")));

		/* Boutons */
		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidthFull();
		buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		layout.add(buttonsLayout);

		btnNon.setText(getTranslation("confirmWindow.btnNon"));
		btnNon.setIcon(VaadinIcon.CLOSE.create());
		btnNon.addClickListener(e -> close());
		buttonsLayout.add(btnNon);

		btnOui.setText(getTranslation("confirmWindow.btnOui"));
		btnOui.setIcon(VaadinIcon.CHECK.create());
		btnOui.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btnOui.addClickListener(e -> close());
		buttonsLayout.add(btnOui);
	}

	public Registration addBtnOuiListener(final ComponentEventListener<ClickEvent<Button>> clickListener) {
		return btnOui.addClickListener(clickListener);
	}

	public Registration addBtnNonListener(final ComponentEventListener<ClickEvent<Button>> clickListener) {
		return btnNon.addClickListener(clickListener);
	}
}
