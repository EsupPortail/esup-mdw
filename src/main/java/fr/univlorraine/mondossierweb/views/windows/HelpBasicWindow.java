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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.Registration;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import org.springframework.util.StringUtils;

/**
 * Fenêtre d'aide basique
 */
@SuppressWarnings("serial")
public class HelpBasicWindow extends Dialog {

	public static final String NAME = "helpBasicWindow";

	/* Composants */
	private final Button btnFermer = new Button();

	/**
	 * Crée une fenêtre d'aide avec un titre
	 */
	public HelpBasicWindow(final String message, final String titre) {
		this(message, titre, false);
	}

	/**
	 * Crée une fenêtre d'aide
	 * @param message contenu HTML
	 * @param titre titre de la fenêtre
	 * @param displayLienContact affiche le lien mailto vers le contact assistance
	 */
	public HelpBasicWindow(final String message, final String titre, final boolean displayLienContact) {
		/* Style */
		setWidth("900px");
		setMaxWidth("100%");
		setResizable(false);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(true);

		/* Titre */
		setHeaderTitle(titre);

		/* Layout */
		final VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(false);
		add(layout);

		/* Lien de contact */
		if (displayLienContact) {
			final ConfigController configController = VaadinService.getCurrent().getInstantiator().getOrCreate(ConfigController.class);
			final String mailContact = configController.getAssistanceContactMail();
			if (StringUtils.hasText(mailContact)) {
				final Button contactBtn = new Button(getTranslation(NAME + ".btnContact"), VaadinIcon.ENVELOPE.create());
				contactBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				final Anchor contactAnchor = new Anchor("mailto:" + mailContact, contactBtn);
				layout.add(contactAnchor);
				layout.setAlignSelf(FlexComponent.Alignment.END, contactAnchor);
			}
		}

		/* Texte */
		layout.add(new Html("<div>" + message + "</div>"));

		/* Boutons */
		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidthFull();
		buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		layout.add(buttonsLayout);

		btnFermer.setText(getTranslation("helpWindow.btnFermer"));
		btnFermer.setIcon(VaadinIcon.CLOSE.create());
		btnFermer.addClickListener(e -> close());
		buttonsLayout.add(btnFermer);
	}

	public Registration addBtnNonListener(final ComponentEventListener<ClickEvent<Button>> clickListener) {
		return btnFermer.addClickListener(clickListener);
	}
}
