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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.beans.BacEtatCivil;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.uicomponents.BasicErreurMessageLayout;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Page de l'état-civil
 */
@SuppressWarnings("serial")
@Route(value = EtatCivilView.NAME, layout = MainLayout.class)
@Secured(MdwUserDetailsService.CONSULT_DOSSIER_AUTORISE)
@Slf4j
public class EtatCivilView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	public static final String NAME = "etatCivilView";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient SessionController sessionController;

	private TextField fieldTelPortable;
	private TextField fieldMailPerso;
	private Button btnAnnulerModifCoordonneesPerso;
	private Button btnValidModifCoordonneesPerso;
	private Button btnModifCoordonneesPerso;
	private Div panelContactContent;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	/**
	 * Initialise la vue à chaque navigation (l'étudiant courant est renseigné par MainLayout).
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		removeAll();

		log.debug(userController.getCurrentUserName() + " EtatCivilView");

		//On vérifie le droit d'accéder à la vue
		if (userController.isEnseignant() || userController.isEtudiant()) {
			if (sessionController.getEtudiant() != null) {
				log.debug(userController.getCurrentUserName() + " init EtatCivilView " + SecurityContextHolder.getContext().getAuthentication().getName());
				buildView(sessionController.getEtudiant());
			} else {
				/* Erreur */
				add(new BasicErreurMessageLayout(applicationContext));
			}
		}
	}

	private void buildView(final Etudiant etudiant) {
		/* Style */
		setPadding(true);
		setSpacing(true);

		VerticalLayout globalLayout = new VerticalLayout();
		globalLayout.setWidthFull();
		globalLayout.setPadding(false);
		globalLayout.setSpacing(true);

		//Layout avec les infos etatcivil et bac (responsive : les blocs passent à la ligne)
		Div idLayout = new Div();
		idLayout.setWidthFull();
		idLayout.getStyle()
			.set("display", "flex")
			.set("flex-wrap", "wrap")
			.set("gap", "var(--lumo-space-m)");
		globalLayout.add(idLayout);

		/* Generalites */
		FormLayout formGeneralitesLayout = createFormLayout();

		addFormValue(formGeneralitesLayout, getTranslation(NAME + ".numdossier.title"), etudiant.getCod_etu());
		addFormValue(formGeneralitesLayout, getTranslation(NAME + ".nne.title"), etudiant.getCod_nne());
		addFormValue(formGeneralitesLayout, getTranslation(NAME + ".nom.title"), etudiant.getNom());

		/* Mail : lien mailto */
		String mail = etudiant.getEmail();
		if (StringUtils.hasText(mail)) {
			formGeneralitesLayout.addFormItem(new Anchor("mailto:" + mail, mail), getTranslation(NAME + ".mail.title"));
		} else {
			addFormValue(formGeneralitesLayout, getTranslation(NAME + ".mail.title"), null);
		}

		// Si on doit afficher les infos de naissance
		if (userController.isEtudiant()
				|| (userController.isEnseignant() && configController.isAffInfoNaissanceEnseignant())
				|| (userController.isGestionnaire() && configController.isAffInfoNaissanceGestionnaire())) {
			addFormValue(formGeneralitesLayout, getTranslation(NAME + ".nationalite.title"), etudiant.getNationalite());
			addFormValue(formGeneralitesLayout, getTranslation(NAME + ".naissance.title"), etudiant.getDatenaissance());
			addFormValue(formGeneralitesLayout, getTranslation(NAME + ".lieunaissance.title"), etudiant.getLieunaissance());
			addFormValue(formGeneralitesLayout, getTranslation(NAME + ".depnaissance.title"), etudiant.getDepartementnaissance());
		}

		idLayout.add(createPanel(getTranslation(NAME + ".generalites.title"), formGeneralitesLayout));

		/* Bac */
		String titreBac = getTranslation(NAME + ".bac.title");
		Component bacContent;
		List<BacEtatCivil> listeBac = etudiant.getListeBac();
		//Si plusieurs bac
		if (listeBac != null && listeBac.size() > 1) {
			titreBac = getTranslation(NAME + ".bacs.title");
			TabSheet bacTabSheet = new TabSheet();
			bacTabSheet.setWidthFull();
			for (BacEtatCivil bec : listeBac) {
				FormLayout tabBacLayout = createFormLayout();
				ajouterBacToView(tabBacLayout, bec);
				bacTabSheet.add(new Tab(VaadinIcon.ACADEMY_CAP.create(), new Span(bec.getCod_bac())), tabBacLayout);
			}
			bacContent = bacTabSheet;
		} else {
			//Un seul bac
			FormLayout formBacLayout = createFormLayout();
			if (listeBac != null && listeBac.size() == 1) {
				ajouterBacToView(formBacLayout, listeBac.get(0));
			}
			bacContent = formBacLayout;
		}
		idLayout.add(createPanel(titreBac, bacContent));

		/* Infos de contact */
		if (userController.isEtudiant()
				|| (userController.isEnseignant() && (configController.isAffContactTelEnseignant() || configController.isAffContactMailEnseignant()))
				|| (userController.isGestionnaire() && (configController.isAffContactTelGestionnaire() || configController.isAffContactMailGestionnaire()))) {
			panelContactContent = new Div();
			panelContactContent.setWidthFull();
			renseignerPanelContact();
			globalLayout.add(createPanel(getTranslation(NAME + ".contact.title"), panelContactContent));
		}
		add(globalLayout);
	}

	private void renseignerPanelContact() {
		final Etudiant etudiant = sessionController.getEtudiant();

		panelContactContent.removeAll();

		VerticalLayout contactLayout = new VerticalLayout();
		contactLayout.setPadding(false);
		contactLayout.setSpacing(true);

		/* Layout pour afficher les erreurs */
		VerticalLayout erreursLayout = new VerticalLayout();
		erreursLayout.setPadding(false);
		contactLayout.add(erreursLayout);
		erreursLayout.setVisible(false);

		/* Layout avec les champs 'Portable' et 'Email personnel' */
		FormLayout formContactLayout = createFormLayout();

		if (userController.isEtudiant()
				|| (userController.isGestionnaire() && configController.isAffContactTelGestionnaire())
				|| (userController.isEnseignant() && configController.isAffContactTelEnseignant())) {
			fieldTelPortable = new TextField();
			fieldTelPortable.setValue(etudiant.getTelPortable() != null ? etudiant.getTelPortable() : "");
			formatTextField(fieldTelPortable);
			fieldTelPortable.setMaxLength(15);
			formContactLayout.addFormItem(fieldTelPortable, getTranslation(NAME + ".portable.title"));
		}

		if (userController.isEtudiant()
				|| (userController.isGestionnaire() && configController.isAffContactMailGestionnaire())
				|| (userController.isEnseignant() && configController.isAffContactMailEnseignant())) {
			fieldMailPerso = new TextField();
			fieldMailPerso.setValue(etudiant.getEmailPerso() != null ? etudiant.getEmailPerso() : "");
			formatTextField(fieldMailPerso);
			fieldMailPerso.setMaxLength(200);
			formContactLayout.addFormItem(fieldMailPerso, getTranslation(NAME + ".mailperso.title"));
		}

		contactLayout.add(formContactLayout);

		/* Si user étudiant, modifications autorisées des coordonnées de contact
		 * et si l'étudiant possède une adresse annuelle, on affiche les boutons de modification */
		if (userController.isEtudiant() && configController.isModificationCoordonneesPersoAutorisee()
				&& etudiant.getAdresseAnnuelle() != null) {
			//Layout pour les boutons de modification
			HorizontalLayout btnLayout = new HorizontalLayout();
			btnLayout.setWidthFull();
			btnLayout.setSpacing(true);

			//Bouton pour valider la modification
			btnValidModifCoordonneesPerso = new Button(getTranslation(NAME + ".bouton.validercoordonnees"), VaadinIcon.CHECK.create());
			btnValidModifCoordonneesPerso.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			btnValidModifCoordonneesPerso.addClickListener(e -> {
				erreursLayout.removeAll();
				List<String> retour = etudiantController.updateContact(fieldTelPortable.getValue(), fieldMailPerso.getValue(), etudiant.getCod_etu());
				// si modif ok
				if (retour != null && retour.size() == 1 && retour.get(0).equals("OK")) {
					etudiantController.recupererEtatCivil();
					renseignerPanelContact();
				} else {
					//affichage erreurs
					if (retour != null && !retour.isEmpty()) {
						for (String erreur : retour) {
							Span labelErreur = new Span(erreur);
							labelErreur.addClassName(LumoUtility.TextColor.ERROR);
							erreursLayout.add(labelErreur);
						}
					}
					erreursLayout.setVisible(true);
				}
			});
			btnValidModifCoordonneesPerso.setVisible(false);
			btnLayout.add(btnValidModifCoordonneesPerso);

			//Bouton pour annuler la modification
			btnAnnulerModifCoordonneesPerso = new Button(getTranslation(NAME + ".bouton.annulercoordonnees"), VaadinIcon.CLOSE.create());
			btnAnnulerModifCoordonneesPerso.addThemeVariants(ButtonVariant.LUMO_ERROR);
			btnAnnulerModifCoordonneesPerso.addClickListener(e -> {
				erreursLayout.removeAll();
				erreursLayout.setVisible(false);
				fieldMailPerso.setValue(etudiant.getEmailPerso() != null ? etudiant.getEmailPerso() : "");
				fieldMailPerso.setReadOnly(true);
				fieldTelPortable.setValue(etudiant.getTelPortable() != null ? etudiant.getTelPortable() : "");
				fieldTelPortable.setReadOnly(true);
				btnValidModifCoordonneesPerso.setVisible(false);
				btnAnnulerModifCoordonneesPerso.setVisible(false);
				btnModifCoordonneesPerso.setVisible(true);
			});
			btnAnnulerModifCoordonneesPerso.setVisible(false);
			btnLayout.add(btnAnnulerModifCoordonneesPerso);

			//Bouton pour activer la modification des données
			btnModifCoordonneesPerso = new Button(getTranslation(NAME + ".bouton.modifiercoordonnees"), VaadinIcon.EDIT.create());
			btnModifCoordonneesPerso.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			btnModifCoordonneesPerso.addClickListener(e -> {
				fieldMailPerso.setReadOnly(false);
				fieldTelPortable.setReadOnly(false);
				btnValidModifCoordonneesPerso.setVisible(true);
				btnAnnulerModifCoordonneesPerso.setVisible(true);
				btnModifCoordonneesPerso.setVisible(false);
			});
			btnLayout.add(btnModifCoordonneesPerso);
			contactLayout.add(btnLayout);
		}

		panelContactContent.add(contactLayout);
	}

	private FormLayout createFormLayout() {
		FormLayout formLayout = new FormLayout();
		formLayout.setWidthFull();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		return formLayout;
	}

	private void formatTextField(TextField tf) {
		tf.setReadOnly(true);
		tf.setWidthFull();
	}

	/**
	 * Ajoute un couple libellé/valeur (valeur en gras) au formulaire.
	 */
	private void addFormValue(FormLayout formLayout, String caption, String value) {
		Span valueSpan = new Span(StringUtils.hasText(value) ? value : "");
		valueSpan.addClassName(LumoUtility.FontWeight.BOLD);
		formLayout.addFormItem(valueSpan, caption != null ? caption : "");
	}

	private void ajouterBacToView(FormLayout formBacLayout, BacEtatCivil bec) {
		addFormValue(formBacLayout, getTranslation(NAME + ".codebac.title"), bec.getLib_bac());

		if (bec.getLicSpeBacPre() != null) {
			addFormValue(formBacLayout, getTranslation(NAME + ".speprebac.title"), bec.getLicSpeBacPre());
		}
		if (bec.getLicSpe1Bac() != null) {
			addFormValue(formBacLayout, getTranslation(NAME + ".spe1bac.title"), bec.getLicSpe1Bac());
		}
		if (bec.getLicSpe2Bac() != null) {
			addFormValue(formBacLayout, getTranslation(NAME + ".spe2bac.title"), bec.getLicSpe2Bac());
		}
		if (bec.getLicOpt1Bac() != null) {
			addFormValue(formBacLayout, getTranslation(NAME + ".opt1bac.title"), bec.getLicOpt1Bac());
		}
		if (bec.getLicOpt2Bac() != null) {
			addFormValue(formBacLayout, getTranslation(NAME + ".opt2bac.title"), bec.getLicOpt2Bac());
		}
		if (bec.getLicOpt3Bac() != null) {
			addFormValue(formBacLayout, getTranslation(NAME + ".opt3bac.title"), bec.getLicOpt3Bac());
		}
		if (bec.getLicOpt4Bac() != null) {
			addFormValue(formBacLayout, getTranslation(NAME + ".opt4bac.title"), bec.getLicOpt4Bac());
		}

		addFormValue(formBacLayout, getTranslation(NAME + ".anneebac.title"), bec.getDaa_obt_bac_iba());
		addFormValue(formBacLayout, getTranslation(NAME + ".mentionbac.title"), bec.getCod_mnb());
		addFormValue(formBacLayout, getTranslation(NAME + ".typeetbbac.title"), bec.getCod_tpe());
		addFormValue(formBacLayout, getTranslation(NAME + ".etbbac.title"), bec.getCod_etb());
		addFormValue(formBacLayout, getTranslation(NAME + ".depbac.title"), bec.getCod_dep());
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
