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
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fenêtre de saisie
 */
@SuppressWarnings("serial")
public class InputWindow extends Dialog {

	/* Composants */
	private final TextField inputTextField = new TextField();
	private final Button btnOk = new Button();
	private final Button btnCancel = new Button();

	/** Listeners */
	@Getter
	private final Set<BtnOkListener> btnOkListeners = new LinkedHashSet<>();

	/**
	 * Crée une fenêtre de saisie avec un message et un titre par défaut
	 */
	public InputWindow() {
		this(null, null);
	}

	/**
	 * Crée une fenêtre de saisie avec un titre par défaut
	 */
	public InputWindow(final String message) {
		this(message, null);
	}

	/**
	 * Crée une fenêtre de saisie
	 */
	public InputWindow(final String message, final String titre) {
		/* Style */
		setWidth("400px");
		setResizable(false);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);

		/* Titre */
		setHeaderTitle(titre != null ? titre : getTranslation("inputWindow.defaultTitle"));

		/* Layout */
		final VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(true);
		add(layout);

		/* Texte */
		layout.add(new Paragraph(message != null ? message : getTranslation("inputWindow.defaultMessage")));

		/* Champ de saisie */
		inputTextField.setWidthFull();
		layout.add(inputTextField);

		/* Boutons */
		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidthFull();
		buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		layout.add(buttonsLayout);

		btnCancel.setText(getTranslation("inputWindow.btnCancel"));
		btnCancel.addClickListener(e -> close());
		buttonsLayout.add(btnCancel);

		btnOk.setText(getTranslation("inputWindow.btnOk"));
		btnOk.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btnOk.addClickShortcut(Key.ENTER);
		btnOk.addClickListener(e -> {
			btnOkListeners.forEach(l -> l.btnOkClick(inputTextField.getValue()));
			close();
		});
		buttonsLayout.add(btnOk);

		/* Place le focus sur le champ de saisie */
		inputTextField.focus();
	}

	public void addBtnOkListener(final BtnOkListener btnOkListener) {
		btnOkListeners.add(btnOkListener);
	}

	public void removeBtnOkListener(final BtnOkListener btnOkListener) {
		btnOkListeners.remove(btnOkListener);
	}

	public Registration addBtnCancelListener(final ComponentEventListener<ClickEvent<Button>> clickListener) {
		return btnCancel.addClickListener(clickListener);
	}

	/**
	 * Interface pour les listeners du bouton ok.
	 */
	public interface BtnOkListener extends Serializable {
		/**
		 * Appelé lorsque Ok est cliqué.
		 */
		void btnOkClick(String text);
	}
}
