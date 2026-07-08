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
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import lombok.Getter;

/**
 * Fenêtre d'aide
 */
@SuppressWarnings("serial")
public class HelpWindow extends Dialog {

	/* Composants */
	private final Button btnFermer = new Button();

	@Getter
	private final Checkbox checkBox = new Checkbox();

	/**
	 * Crée une fenêtre d'aide
	 * @param message contenu HTML de l'aide
	 * @param titre titre de la fenêtre (défaut si null)
	 * @param displayCheckBox propose la case « ne plus afficher ce message »
	 */
	public HelpWindow(final String message, final String titre, final boolean displayCheckBox) {
		/* Style */
		setWidth("900px");
		setMaxWidth("100%");
		setResizable(false);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);

		/* Titre */
		setHeaderTitle(titre != null ? titre : getTranslation("helpWindow.defaultTitle"));

		/* Layout */
		final VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(true);
		add(layout);

		/* Texte */
		layout.add(new Html("<div>" + message + "</div>"));

		/* Boutons */
		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidthFull();
		buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.add(buttonsLayout);

		if (displayCheckBox) {
			checkBox.setLabel(getTranslation("helpWindow.checkBox.message"));
			buttonsLayout.add(checkBox);
		}

		btnFermer.setText(getTranslation("helpWindow.btnFermer"));
		btnFermer.setIcon(VaadinIcon.CLOSE.create());
		btnFermer.addClickListener(e -> close());
		buttonsLayout.add(btnFermer);
	}

	public Registration addBtnNonListener(final ComponentEventListener<ClickEvent<Button>> clickListener) {
		return btnFermer.addClickListener(clickListener);
	}
}
