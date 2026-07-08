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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationValeurs;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Fenêtre d'édition des parametre de l'application
 *
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PreferencesApplicationWindow extends Dialog {

	public static final String NAME = "preferencesApplicationWindow";

	@Resource
	private transient ConfigController configController;

	/* Composants */
	private final Binder<PreferencesApplication> binder = new Binder<>(PreferencesApplication.class);
	private Grid<PreferencesApplicationValeurs> pavGrid;
	private Button btnEnregistrer;
	private Button btnAnnuler;

	/**
	 * Crée une fenêtre d'édition des preferences applicative
	 * @param prefApp à éditer
	 */
	public void init(PreferencesApplication prefApp) {
		/* Style */
		setResizable(false);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);
		setWidth("50%");

		/* Titre */
		setHeaderTitle(getTranslation(NAME + ".title"));

		/* Formulaire */
		FormLayout formLayout = new FormLayout();
		formLayout.setWidthFull();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

		TextField prefIdField = new TextField(getTranslation(NAME + ".confAppTable.prefId"));
		prefIdField.setWidthFull();
		prefIdField.setReadOnly(prefApp.getPrefId() != null);
		binder.forField(prefIdField)
			.withNullRepresentation("")
			.bind(PreferencesApplication::getPrefId, PreferencesApplication::setPrefId);
		formLayout.add(prefIdField);

		TextField prefDescField = new TextField(getTranslation(NAME + ".confAppTable.prefDesc"));
		prefDescField.setWidthFull();
		prefDescField.setReadOnly(prefApp.getPrefDesc() != null);
		binder.forField(prefDescField)
			.withNullRepresentation("")
			.bind(PreferencesApplication::getPrefDesc, PreferencesApplication::setPrefDesc);
		formLayout.add(prefDescField);

		//Si on est sur un parametre booleen
		if (Utils.PARAM_TYP_BOOLEAN.equals(prefApp.getType())) {
			Select<String> valeurField = new Select<>();
			valeurField.setLabel(getTranslation(NAME + ".confAppTable.valeur"));
			valeurField.setItems("true", "false");
			valeurField.setEmptySelectionAllowed(false);
			valeurField.setWidthFull();
			binder.forField(valeurField)
				.bind(PreferencesApplication::getValeur, PreferencesApplication::setValeur);
			formLayout.add(valeurField);
		} else if (Utils.PARAM_TYP_LIST.equals(prefApp.getType())) {
			//Si on est sur un parametre List : on forme le modificateur de liste
			formLayout.add(buildListEditor(prefApp));
		} else {
			TextField valeurField = new TextField(getTranslation(NAME + ".confAppTable.valeur"));
			valeurField.setWidthFull();
			binder.forField(valeurField)
				.withNullRepresentation("")
				.bind(PreferencesApplication::getValeur, PreferencesApplication::setValeur);
			formLayout.add(valeurField);
		}

		binder.readBean(prefApp);

		add(formLayout);

		/* Ajoute les boutons */
		btnAnnuler = new Button(getTranslation(NAME + ".btnAnnuler"), VaadinIcon.CLOSE.create());
		btnAnnuler.addClassName("admin-button");
		btnAnnuler.addClickListener(e -> close());
		getFooter().add(btnAnnuler);

		btnEnregistrer = new Button(getTranslation(NAME + ".btnSave"), VaadinIcon.CHECK.create());
		btnEnregistrer.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btnEnregistrer.addClassName("admin-button");
		btnEnregistrer.addClickListener(e -> {
			try {
				/* Valide la saisie */
				binder.writeBean(prefApp);
				/* Enregistre la conf saisie */
				configController.saveAppParameter(prefApp);
				/* Ferme la fenêtre */
				close();
			} catch (ValidationException ve) {
			}
		});
		getFooter().add(btnEnregistrer);
	}

	/**
	 * @param prefApp
	 * @return l'éditeur de liste de valeurs d'un parametre de type LIST
	 */
	private VerticalLayout buildListEditor(PreferencesApplication prefApp) {
		VerticalLayout listlayout = new VerticalLayout();
		listlayout.setWidthFull();
		listlayout.setPadding(false);
		listlayout.setSpacing(true);

		HorizontalLayout nvl = new HorizontalLayout();
		nvl.setWidthFull();
		//champ de saisie
		final TextField listInputField = new TextField();
		listInputField.setWidthFull();
		nvl.add(listInputField);
		//Bouton add
		Button btnAddInput = new Button(getTranslation(NAME + ".list.addbtn"), VaadinIcon.PLUS.create());
		btnAddInput.addClickListener(e -> addListValueToPrefApp(prefApp, listInputField));
		nvl.add(btnAddInput);
		nvl.setFlexGrow(1, listInputField);
		listlayout.add(nvl);

		//bouton supprimer
		Button boutonSupprimerPav = new Button(getTranslation(NAME + ".btnPavDelete"), VaadinIcon.MINUS.create());
		boutonSupprimerPav.setEnabled(false);

		//affichage des pav
		pavGrid = new Grid<>();
		pavGrid.setWidthFull();
		pavGrid.setHeight("200px");
		pavGrid.addColumn(PreferencesApplicationValeurs::getValeur)
			.setHeader(getTranslation(NAME + ".confAppTable.valeur"));
		pavGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
		pavGrid.addSelectionListener(e -> boutonSupprimerPav.setEnabled(e.getFirstSelectedItem().isPresent()));
		refreshPavGrid(prefApp);

		boutonSupprimerPav.addClickListener(e ->
			pavGrid.getSelectedItems().stream().findFirst()
				.ifPresent(pavChoisie -> supprimerPav(prefApp, pavChoisie)));

		listlayout.add(pavGrid);
		listlayout.add(boutonSupprimerPav);
		return listlayout;
	}

	private void refreshPavGrid(PreferencesApplication prefApp) {
		if (prefApp.getPreferencesApplicationValeurs() != null && !prefApp.getPreferencesApplicationValeurs().isEmpty()) {
			pavGrid.setItems(prefApp.getPreferencesApplicationValeurs());
		} else {
			pavGrid.setItems(Collections.emptyList());
		}
	}

	/* ajout d'un element */
	private void addListValueToPrefApp(PreferencesApplication prefApp, TextField tf) {
		boolean ajouter = true;
		String value = tf.getValue();
		if (prefApp.getPreferencesApplicationValeurs() == null) {
			prefApp.setPreferencesApplicationValeurs(new LinkedList<PreferencesApplicationValeurs>());
		} else {
			for (PreferencesApplicationValeurs p : prefApp.getPreferencesApplicationValeurs()) {
				if (p.getValeur() != null && p.getValeur().equals(value)) {
					ajouter = false;
				}
			}
		}
		tf.clear();
		if (ajouter) {
			PreferencesApplicationValeurs pav = new PreferencesApplicationValeurs();
			pav.setValeur(value);
			pav.setPreferencesApplication(prefApp);
			prefApp.getPreferencesApplicationValeurs().add(pav);
			refreshPavGrid(prefApp);
		} else {
			Notification.show(getTranslation(NAME + ".notification.valeur.presente"));
		}
	}

	private void supprimerPav(PreferencesApplication prefApp, PreferencesApplicationValeurs pavChoisie) {
		if (prefApp != null && prefApp.getPreferencesApplicationValeurs() != null && pavChoisie != null && StringUtils.hasText(pavChoisie.getValeur())) {
			if (prefApp.getPreferencesApplicationValeurs().size() == 1) {
				//On vérifie que c'est bien l'élément à supprimer
				PreferencesApplicationValeurs p = prefApp.getPreferencesApplicationValeurs().get(0);
				if (p.getValeur() != null && p.getValeur().equals(pavChoisie.getValeur())) {
					prefApp.setPreferencesApplicationValeurs(null);
				}
			} else {
				int i = 0;
				int rangToDelete = 0;
				boolean pavToDeleted = false;
				for (PreferencesApplicationValeurs p : prefApp.getPreferencesApplicationValeurs()) {
					//On test sur la valur car il se peut que l'objet n'ai pas encore d'id
					if (!pavToDeleted && p.getValeur() != null && p.getValeur().equals(pavChoisie.getValeur())) {
						rangToDelete = i;
						pavToDeleted = true;
					}
					i++;
				}
				if (pavToDeleted) {
					prefApp.getPreferencesApplicationValeurs().remove(rangToDelete);
				}
			}
		}
		refreshPavGrid(prefApp);
	}
}
