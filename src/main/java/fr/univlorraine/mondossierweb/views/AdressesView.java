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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.beans.Adresse;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.views.windows.ModificationAdressesWindow;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

/**
 * Page des adresses
 */
@SuppressWarnings("serial")
@Route(value = AdressesView.NAME, layout = MainLayout.class)
@Secured(MdwUserDetailsService.CONSULT_DOSSIER_AUTORISE)
public class AdressesView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	public static final String NAME = "adressesView";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient SessionController sessionController;
	@Resource
	private transient ObjectFactory<ModificationAdressesWindow> modificationAdressesWindowFactory;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	/**
	 * Initialise la vue à chaque navigation.
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		init();
	}

	private void init() {
		final Etudiant etudiant = sessionController.getEtudiant();

		//On vérifie le droit d'accéder à la vue
		if (etudiant != null
				&& (userController.isEtudiant()
					|| (userController.isEnseignant() && configController.isAffAdressesEnseignant())
					|| (userController.isGestionnaire() && configController.isAffAdressesGestionnaire()))) {
			removeAll();

			/* Style */
			setPadding(true);
			setSpacing(true);

			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setWidthFull();
			globalLayout.setPadding(false);
			globalLayout.setSpacing(true);

			//Layout avec les adresses (responsive : les blocs passent à la ligne)
			Div idLayout = new Div();
			idLayout.setWidthFull();
			idLayout.getStyle()
				.set("display", "flex")
				.set("flex-wrap", "wrap")
				.set("gap", "var(--lumo-space-m)");
			globalLayout.add(idLayout);

			if (etudiant.getAdresseAnnuelle() != null) {
				Adresse adresseAnnuelle = etudiant.getAdresseAnnuelle();

				FormLayout formAdresseAnnuelleLayout = createFormLayout();

				addFormValue(formAdresseAnnuelleLayout, getTranslation(NAME + ".adresse.title"), adresseAnnuelle.getAdresse1());

				String annuelle2 = valuateTextFieldFromMultipleValues(adresseAnnuelle.getAdresse2(), adresseAnnuelle.getAdresse3());
				if (annuelle2 != null) {
					addFormValue(formAdresseAnnuelleLayout, null, annuelle2);
				}

				addFormValue(formAdresseAnnuelleLayout, getTranslation(NAME + ".ville.title"),
					valuateTextFieldFromMultipleValues(adresseAnnuelle.getAdresseetranger(), adresseAnnuelle.getCodePostal(), adresseAnnuelle.getVille()));
				addFormValue(formAdresseAnnuelleLayout, getTranslation(NAME + ".pays.title"), adresseAnnuelle.getPays());
				addFormValue(formAdresseAnnuelleLayout, getTranslation(NAME + ".telephone.title"), adresseAnnuelle.getNumerotel());

				idLayout.add(createPanel(getTranslation(NAME + ".adresseannuelle.title") + " " + adresseAnnuelle.getAnnee(), formAdresseAnnuelleLayout));
			}

			if (etudiant.getAdresseFixe() != null) {
				Adresse adresseFixe = etudiant.getAdresseFixe();

				FormLayout formAdresseFixeLayout = createFormLayout();

				addFormValue(formAdresseFixeLayout, getTranslation(NAME + ".adresse.title"), adresseFixe.getAdresse1());

				String adfixe2 = valuateTextFieldFromMultipleValues(adresseFixe.getAdresse2(), adresseFixe.getAdresse3());
				if (adfixe2 != null) {
					addFormValue(formAdresseFixeLayout, null, adfixe2);
				}

				addFormValue(formAdresseFixeLayout, getTranslation(NAME + ".ville.title"),
					valuateTextFieldFromMultipleValues(adresseFixe.getAdresseetranger(), adresseFixe.getCodePostal(), adresseFixe.getVille()));
				addFormValue(formAdresseFixeLayout, getTranslation(NAME + ".pays.title"), adresseFixe.getPays());
				addFormValue(formAdresseFixeLayout, getTranslation(NAME + ".telephone.title"), adresseFixe.getNumerotel());

				idLayout.add(createPanel(getTranslation(NAME + ".adressefixe.title"), formAdresseFixeLayout));
			}

			add(globalLayout);

			if (userController.isEtudiant() && configController.isModificationAdressesAutorisee() && etudiant.getAdresseAnnuelle() != null) {
				HorizontalLayout btnLayout = new HorizontalLayout();
				btnLayout.setWidthFull();
				btnLayout.setSpacing(true);

				String captionBtnModif = getTranslation(NAME + ".bouton.modifieradresses");
				// Si on ne peut pas modifier l'adresse annuelle
				if (!configController.isModificationAdresseAnnuelleAutorisee()) {
					// Changement du caption du bouton en conséquence
					captionBtnModif = getTranslation(NAME + ".bouton.modifieradressefixe");
				}
				Button btnModifAdresses = new Button(captionBtnModif, VaadinIcon.EDIT.create());
				btnModifAdresses.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
				btnModifAdresses.addClickListener(e -> {
					ModificationAdressesWindow maw = modificationAdressesWindowFactory.getObject();
					maw.init(sessionController.getEtudiant(), configController.isModificationTelephoneAutorisee(), configController.isModificationAdresseAnnuelleAutorisee());
					maw.addOpenedChangeListener(f -> {
						if (!f.isOpened()) {
							init();
						}
					});
					maw.open();
				});
				btnLayout.add(btnModifAdresses);
				add(btnLayout);
			}
		}
	}

	private String valuateTextFieldFromMultipleValues(String string1, String string2) {
		if (string1 != null && string2 != null) {
			return string1 + " " + string2;
		}
		if (string1 != null && string2 == null) {
			return string1;
		}
		if (string1 == null && string2 != null) {
			return string2;
		}
		return null;
	}

	private String valuateTextFieldFromMultipleValues(String string1, String string2, String string3) {
		return valuateTextFieldFromMultipleValues(valuateTextFieldFromMultipleValues(string1, string2), string3);
	}

	private FormLayout createFormLayout() {
		FormLayout formLayout = new FormLayout();
		formLayout.setWidthFull();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		return formLayout;
	}

	/**
	 * Ajoute un couple libellé/valeur (valeur en gras) au formulaire.
	 */
	private void addFormValue(FormLayout formLayout, String caption, String value) {
		Span valueSpan = new Span(StringUtils.hasText(value) ? value : "");
		valueSpan.addClassName(LumoUtility.FontWeight.BOLD);
		formLayout.addFormItem(valueSpan, caption != null ? caption : "");
	}

	/**
	 * Panel (ancien com.vaadin.ui.Panel) : bloc avec un titre et une bordure.
	 */
	private Div createPanel(String titre, Component content) {
		Div panel = new Div();
		panel.getStyle()
			.set("border", "1px solid var(--lumo-contrast-20pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("overflow", "hidden")
			.set("flex", "1 1 20rem");
		if (StringUtils.hasText(titre)) {
			Div header = new Div(new Span(titre));
			header.getStyle()
				.set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
				.set("border-bottom", "1px solid var(--lumo-contrast-20pct)")
				.set("background-color", "var(--lumo-contrast-5pct)")
				.set("font-weight", "600");
			panel.add(header);
		}
		Div body = new Div(content);
		body.getStyle().set("padding", "var(--lumo-space-m)");
		panel.add(body);
		return panel;
	}

}
