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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Fenêtre d'édition d'un swap utilisateur
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SwapUtilisateurWindow extends Dialog {

	public static final String NAME = "swapUtilisateurWindow";

	@Resource
	private transient ConfigController configController;

	/* Composants */
	private final Binder<UtilisateurSwap> binder = new Binder<>(UtilisateurSwap.class);
	private final TextField loginSourceField = new TextField();
	private final TextField loginCibleField = new TextField();
	private final DateTimePicker datCreField = new DateTimePicker();

	/**
	 * Crée une fenêtre d'édition du swap utilisateur
	 * @param swap utilisateur à éditer
	 * @param ajout true en cas de création
	 */
	public void init(final UtilisateurSwap swap, final boolean ajout) {
		/* Style */
		setResizable(false);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);
		setWidth("50%");

		/* Titre */
		setHeaderTitle(getTranslation(ajout ? NAME + ".title.ajout" : NAME + ".title"));

		/* Layout */
		final VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(true);
		layout.setWidthFull();
		add(layout);

		/* Formulaire */
		final FormLayout formLayout = new FormLayout();
		formLayout.setWidthFull();

		loginSourceField.setWidthFull();
		formLayout.addFormItem(loginSourceField, getTranslation(NAME + ".swapUserTable.loginSource"));
		loginCibleField.setWidthFull();
		formLayout.addFormItem(loginCibleField, getTranslation(NAME + ".swapUserTable.loginCible"));
		formLayout.addFormItem(datCreField, getTranslation(NAME + ".swapUserTable.datCre"));

		binder.forField(loginSourceField).bind(UtilisateurSwap::getLoginSource, UtilisateurSwap::setLoginSource);
		binder.forField(loginCibleField).bind(UtilisateurSwap::getLoginCible, UtilisateurSwap::setLoginCible);
		binder.forField(datCreField)
			.withConverter(
				ldt -> ldt == null ? null : Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()),
				date -> date == null ? null : LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()))
			.bind(UtilisateurSwap::getDatCre, UtilisateurSwap::setDatCre);
		binder.readBean(swap);

		/* À l'ajout, la date d'activation est préremplie à maintenant (sinon DAT_CRE null = swap invalide) */
		if (ajout && swap.getDatCre() == null) {
			datCreField.setValue(LocalDateTime.now());
		}

		loginSourceField.setReadOnly(swap.getLoginSource() != null);
		layout.add(formLayout);

		/* Ajoute les boutons */
		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidthFull();
		buttonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		layout.add(buttonsLayout);

		final Button btnAnnuler = new Button(getTranslation(NAME + ".btnAnnuler"), VaadinIcon.CLOSE.create());
		btnAnnuler.addClickListener(e -> close());
		btnAnnuler.addClassName("admin-button");
		buttonsLayout.add(btnAnnuler);

		final Button btnEnregistrer = new Button(getTranslation(NAME + ".btnSave"), VaadinIcon.CHECK.create());
		btnEnregistrer.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btnEnregistrer.addClassName("admin-button");
		btnEnregistrer.addClickListener(e -> {
			try {
				/* Valide la saisie */
				binder.writeBean(swap);
				boolean commitok = true;
				if (ajout) {
					//On regarde si le login source n'est pas déjà dans la table
					final String loginSource = swap.getLoginSource();
					if (configController.getSwapUtilisateur(loginSource) != null) {
						commitok = false;
						//Afficher message d'erreur
						Notification notification = Notification.show(getTranslation(NAME + ".error.loginexistant"));
						notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
					}
				}
				if (commitok) {
					/* Enregistre le swap saisie */
					configController.saveSwap(swap);

					/* Ferme la fenêtre */
					close();
				}
			} catch (final ValidationException ce) {
			}
		});
		buttonsLayout.add(btnEnregistrer);
	}
}
