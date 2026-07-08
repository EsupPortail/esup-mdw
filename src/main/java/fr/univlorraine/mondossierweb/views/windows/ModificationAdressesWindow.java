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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.beans.Adresse;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.AdresseController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import gouv.education.apogee.commun.client.ws.EtudiantMetier.TypeHebergementDTO;
import gouv.education.apogee.commun.client.ws.GeographieMetier.CommuneDTO2;
import gouv.education.apogee.commun.client.ws.GeographieMetier.PaysDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Fenêtre de modification des adresses
 */
@SuppressWarnings("serial")
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ModificationAdressesWindow extends Dialog {

	public static final String NAME = "modificationAdressesWindow";
	private static final String COD_HEBERG_DOMICILE_PARENTAL = "4";
	private static final String COD_PAY_FRANCE = "100";

	/* Injections */
	@Resource
	private transient AdresseController adresseController;
	@Resource
	private transient EtudiantController etudiantController;

	/* Composants */
	private Button btnAnnuler = new Button();
	private Button btnValider = new Button();
	private Select<String> lhebergement;
	private TextField fieldAnnu1;
	private TextField fieldAnnu2;
	private TextField fieldAnnu3;
	private Select<String> lpays1;
	private TextField fieldTelephone1;
	private TextField fieldCodePostal1;
	private String codePostalVillesAnnu;
	private Select<String> lville1;
	private TextField fieldVilleEtranger1;
	private Span labelChoixHebergement;
	private TextField fieldFixe1;
	private TextField fieldFixe2;
	private TextField fieldFixe3;
	private Select<String> lpays2;
	private TextField fieldTelephone2;
	private TextField fieldCodePostal2;
	private String codePostalVillesFixe;
	private Select<String> lville2;
	private TextField fieldVilleEtranger2;

	/**
	 * Crée la fenêtre de modification des adresses
	 * @param etudiant
	 * @param modificationTelephoneAutorisee
	 * @param modificationAdresseAnnuelleAutorisee
	 */
	public void init(Etudiant etudiant, boolean modificationTelephoneAutorisee, boolean modificationAdresseAnnuelleAutorisee) {
		/* Style */
		setResizable(false);
		setCloseOnEsc(false);
		setCloseOnOutsideClick(false);
		setMaxWidth("100%");

		/* Titre */
		setHeaderTitle(getTranslation(NAME + ".title"));

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(true);
		add(layout);

		/* Layout pour afficher les erreurs */
		VerticalLayout erreursLayout = new VerticalLayout();
		erreursLayout.setPadding(false);
		layout.add(erreursLayout);
		erreursLayout.setVisible(false);

		/* Les 2 panneaux d'adresses côte à côte (à la ligne si écran étroit) */
		Div panelslayout = new Div();
		panelslayout.getStyle()
			.set("display", "flex")
			.set("flex-wrap", "wrap")
			.set("gap", "var(--lumo-space-m)");
		layout.add(panelslayout);

		/* Panel adresse annuelle */
		FormLayout formAdresseAnnuelleLayout = createFormLayout();

		//TypeHebergement
		List<TypeHebergementDTO> hebergements = adresseController.getTypesHebergement();
		Map<String, String> libellesHebergement = new HashMap<>();
		List<String> codesHebergement = new LinkedList<>();
		for (TypeHebergementDTO h : hebergements) {
			codesHebergement.add(h.getCodTypeHebergement());
			libellesHebergement.put(h.getCodTypeHebergement(), h.getLibWebTypeHebergement());
		}
		lhebergement = new Select<>();
		lhebergement.setLabel(getTranslation(NAME + ".typehebergement"));
		lhebergement.setItems(codesHebergement);
		lhebergement.setItemLabelGenerator(code -> libelleNonNull(libellesHebergement.get(code), code));
		lhebergement.setEmptySelectionAllowed(false);
		lhebergement.setRequiredIndicatorVisible(true);
		lhebergement.setWidthFull();
		lhebergement.setValue(etudiant.getAdresseAnnuelle().getType());
		lhebergement.addValueChangeListener(event -> {
			String selectedValue = event.getValue();
			//Si un hébergement autre que le domicile parental a été choisi
			if (selectedValue != null && !selectedValue.equals(COD_HEBERG_DOMICILE_PARENTAL)) {
				activeFormulaireAdresseAnnuelle();
			} else {
				desactiveFormulaireAdresseAnnuelle();
			}
		});
		formAdresseAnnuelleLayout.add(lhebergement);

		//labelChoixHebergement
		labelChoixHebergement = new Span(getTranslation(NAME + ".labelhebergement"));
		formAdresseAnnuelleLayout.add(labelChoixHebergement);

		//AdresseAnnuelle1
		fieldAnnu1 = createTextField(getTranslation(NAME + ".annu1"), etudiant.getAdresseAnnuelle().getAdresse1(), 32);
		fieldAnnu1.setRequiredIndicatorVisible(true);
		formAdresseAnnuelleLayout.add(fieldAnnu1);

		//AdresseAnnuelle2
		fieldAnnu2 = createTextField(getTranslation(NAME + ".annu2"), etudiant.getAdresseAnnuelle().getAdresse2(), 32);
		formAdresseAnnuelleLayout.add(fieldAnnu2);

		//AdresseAnnuelle3
		fieldAnnu3 = createTextField(getTranslation(NAME + ".annu3"), etudiant.getAdresseAnnuelle().getAdresse3(), 32);
		formAdresseAnnuelleLayout.add(fieldAnnu3);

		//Liste des Pays
		List<PaysDTO> pays = adresseController.getPays();
		Map<String, String> libellesPays = new HashMap<>();
		List<String> codesPays = new LinkedList<>();
		for (PaysDTO p : pays) {
			codesPays.add(p.getCodePay());
			libellesPays.put(p.getCodePay(), p.getLibPay());
		}
		lpays1 = new Select<>();
		lpays1.setLabel(getTranslation(NAME + ".pays1"));
		lpays1.setItems(codesPays);
		lpays1.setItemLabelGenerator(code -> libelleNonNull(libellesPays.get(code), code));
		lpays1.setEmptySelectionAllowed(false);
		lpays1.setRequiredIndicatorVisible(true);
		lpays1.setWidthFull();
		lpays1.setValue(etudiant.getAdresseAnnuelle().getCodPays());
		lpays1.addValueChangeListener(event -> {
			String selectedValue = event.getValue();
			//Si un pays autre que France a été choisi
			if (selectedValue != null && !selectedValue.equals(COD_PAY_FRANCE)) {
				activerChampPourAdresseAnnuelleEtranger();
			} else {
				activerChampPourAdresseAnnuelleEnFrance();
			}
		});
		formAdresseAnnuelleLayout.add(lpays1);

		//Ville pour adresse à l'étranger
		fieldVilleEtranger1 = createTextField(getTranslation(NAME + ".villeetranger1"), etudiant.getAdresseAnnuelle().getAdresseetranger(), 32);
		fieldVilleEtranger1.setRequiredIndicatorVisible(true);
		formAdresseAnnuelleLayout.add(fieldVilleEtranger1);

		//codePostal1 pour adresses en france
		fieldCodePostal1 = createTextField(getTranslation(NAME + ".codepostal1"), etudiant.getAdresseAnnuelle().getCodePostal(), 5);
		fieldCodePostal1.setRequiredIndicatorVisible(true);
		fieldCodePostal1.setValueChangeMode(ValueChangeMode.EAGER);
		fieldCodePostal1.addValueChangeListener(event -> {
			if (event.isFromClient()) {
				updateListeVillesAnnuelle(event.getValue());
			}
		});
		formAdresseAnnuelleLayout.add(fieldCodePostal1);

		//Ville pour adresse en france
		lville1 = new Select<>();
		lville1.setLabel(getTranslation(NAME + ".ville1"));
		lville1.setEmptySelectionAllowed(false);
		lville1.setRequiredIndicatorVisible(true);
		lville1.setWidthFull();
		List<CommuneDTO2> villes1 = adresseController.getVilles(etudiant.getAdresseAnnuelle().getCodePostal());
		lville1.setItems(getLibellesCommunes(villes1));
		codePostalVillesAnnu = etudiant.getAdresseAnnuelle().getCodePostal();
		if (etudiant.getAdresseAnnuelle().getVille() != null) {
			lville1.setValue(etudiant.getAdresseAnnuelle().getVille());
		}
		lville1.addValueChangeListener(event -> updateCodePostalVilleAnnuelle());
		formAdresseAnnuelleLayout.add(lville1);

		//activation des champs utiles en fonction de l'adresse de l'étudiant avant la modification
		if (etudiant.getAdresseAnnuelle().getCodPays().equals(COD_PAY_FRANCE)) {
			activerChampPourAdresseAnnuelleEnFrance();
		} else {
			activerChampPourAdresseAnnuelleEtranger();
		}

		//Téléphone1
		fieldTelephone1 = createTextField(getTranslation(NAME + ".tel1"), etudiant.getAdresseAnnuelle().getNumerotel(), 15);
		if (modificationTelephoneAutorisee) {
			formAdresseAnnuelleLayout.add(fieldTelephone1);
		}

		// Si modification adresse annuelle autorisée
		if (modificationAdresseAnnuelleAutorisee) {
			// Ajout panel de l'adresse annuelle dans la vue
			panelslayout.add(createPanel(getTranslation(NAME + ".panel.adresseannuelle.title") + " " + etudiant.getAdresseAnnuelle().getAnnee(),
				formAdresseAnnuelleLayout));
			//Si un hébergement autre que le domicile parental a été choisi
			if (!etudiant.getAdresseAnnuelle().getType().equals(COD_HEBERG_DOMICILE_PARENTAL)) {
				activeFormulaireAdresseAnnuelle();
			} else {
				desactiveFormulaireAdresseAnnuelle();
			}
		}

		/* Panel adresse fixe */
		FormLayout formAdresseFixeLayout = createFormLayout();

		//AdresseFixe1
		fieldFixe1 = createTextField(getTranslation(NAME + ".fixe1"), etudiant.getAdresseFixe().getAdresse1(), 32);
		fieldFixe1.setRequiredIndicatorVisible(true);
		formAdresseFixeLayout.add(fieldFixe1);

		//AdresseFixe2
		fieldFixe2 = createTextField(getTranslation(NAME + ".fixe2"), etudiant.getAdresseFixe().getAdresse2(), 32);
		formAdresseFixeLayout.add(fieldFixe2);

		//AdresseFixe3
		fieldFixe3 = createTextField(getTranslation(NAME + ".fixe3"), etudiant.getAdresseFixe().getAdresse3(), 32);
		formAdresseFixeLayout.add(fieldFixe3);

		//Liste des Pays
		lpays2 = new Select<>();
		lpays2.setLabel(getTranslation(NAME + ".pays1"));
		lpays2.setItems(codesPays);
		lpays2.setItemLabelGenerator(code -> libelleNonNull(libellesPays.get(code), code));
		lpays2.setEmptySelectionAllowed(false);
		lpays2.setRequiredIndicatorVisible(true);
		lpays2.setWidthFull();
		lpays2.setValue(etudiant.getAdresseFixe().getCodPays());
		lpays2.addValueChangeListener(event -> {
			String selectedValue = event.getValue();
			//Si un pays autre que France a été choisi
			if (selectedValue != null && !selectedValue.equals(COD_PAY_FRANCE)) {
				activerChampPourAdresseFixeEtranger();
			} else {
				activerChampPourAdresseFixeEnFrance();
			}
		});
		formAdresseFixeLayout.add(lpays2);

		//Ville pour adresse à l'étranger
		fieldVilleEtranger2 = createTextField(getTranslation(NAME + ".villeetranger2"), etudiant.getAdresseFixe().getAdresseetranger(), 32);
		fieldVilleEtranger2.setRequiredIndicatorVisible(true);
		formAdresseFixeLayout.add(fieldVilleEtranger2);

		//codePostal2 pour adresses en france
		fieldCodePostal2 = createTextField(getTranslation(NAME + ".codepostal2"), etudiant.getAdresseFixe().getCodePostal(), 5);
		fieldCodePostal2.setRequiredIndicatorVisible(true);
		fieldCodePostal2.setValueChangeMode(ValueChangeMode.EAGER);
		fieldCodePostal2.addValueChangeListener(event -> {
			if (event.isFromClient()) {
				updateListeVillesFixe(event.getValue());
			}
		});
		formAdresseFixeLayout.add(fieldCodePostal2);

		//Ville pour adresse en france
		lville2 = new Select<>();
		lville2.setLabel(getTranslation(NAME + ".ville2"));
		lville2.setEmptySelectionAllowed(false);
		lville2.setRequiredIndicatorVisible(true);
		lville2.setWidthFull();
		List<CommuneDTO2> villes2 = adresseController.getVilles(etudiant.getAdresseFixe().getCodePostal());
		lville2.setItems(getLibellesCommunes(villes2));
		codePostalVillesFixe = etudiant.getAdresseFixe().getCodePostal();
		if (etudiant.getAdresseFixe().getVille() != null) {
			lville2.setValue(etudiant.getAdresseFixe().getVille());
		}
		lville2.addValueChangeListener(event -> updateCodePostalVilleFixe());
		formAdresseFixeLayout.add(lville2);

		//Activation des champs utiles en fonction de l'adresse de l'étudiant avant la modification
		if (etudiant.getAdresseFixe().getCodPays().equals(COD_PAY_FRANCE)) {
			activerChampPourAdresseFixeEnFrance();
		} else {
			activerChampPourAdresseFixeEtranger();
		}

		//Téléphone2
		fieldTelephone2 = createTextField(getTranslation(NAME + ".tel2"), etudiant.getAdresseFixe().getNumerotel(), 15);
		if (modificationTelephoneAutorisee) {
			formAdresseFixeLayout.add(fieldTelephone2);
		}

		//ajout du panel adresse fixe
		panelslayout.add(createPanel(getTranslation(NAME + ".panel.adressefixe.title"), formAdresseFixeLayout));

		// Boutons
		btnValider.setText(getTranslation(NAME + ".btnValider"));
		btnValider.setIcon(VaadinIcon.CHECK.create());
		btnValider.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btnValider.addClickListener(e -> {
			Adresse adresseAnnuelle = new Adresse();
			adresseAnnuelle.setType(lhebergement.getValue());
			adresseAnnuelle.setAdresse1(fieldAnnu1.getValue());
			adresseAnnuelle.setAdresse2(fieldAnnu2.getValue());
			adresseAnnuelle.setAdresse3(fieldAnnu3.getValue());
			adresseAnnuelle.setCodPays(lpays1.getValue());
			adresseAnnuelle.setCodePostal(fieldCodePostal1.getValue());
			adresseAnnuelle.setVille(lville1.getValue());
			adresseAnnuelle.setAdresseetranger(fieldVilleEtranger1.getValue());
			adresseAnnuelle.setNumerotel(fieldTelephone1.getValue());

			Adresse adresseFixe = new Adresse();
			adresseFixe.setAdresse1(fieldFixe1.getValue());
			adresseFixe.setAdresse2(fieldFixe2.getValue());
			adresseFixe.setAdresse3(fieldFixe3.getValue());
			adresseFixe.setCodPays(lpays2.getValue());
			adresseFixe.setCodePostal(fieldCodePostal2.getValue());
			adresseFixe.setVille(lville2.getValue());
			adresseFixe.setAdresseetranger(fieldVilleEtranger2.getValue());
			adresseFixe.setNumerotel(fieldTelephone2.getValue());

			erreursLayout.removeAll();
			List<String> retour = adresseController.majAdresses(adresseAnnuelle, adresseFixe, modificationTelephoneAutorisee);
			if (retour != null && retour.size() == 1 && retour.get(0).equals("OK")) {
				//ajout maj vue adresse
				etudiantController.recupererAdresses();
				close();
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
		getFooter().add(btnValider);

		btnAnnuler.setText(getTranslation(NAME + ".btnAnnuler"));
		btnAnnuler.setIcon(VaadinIcon.CLOSE.create());
		btnAnnuler.addThemeVariants(ButtonVariant.LUMO_ERROR);
		btnAnnuler.addClickListener(e -> close());
		getFooter().add(btnAnnuler);
	}

	private List<String> getLibellesCommunes(List<CommuneDTO2> villes) {
		List<String> libelles = new LinkedList<>();
		if (villes != null) {
			for (CommuneDTO2 v : villes) {
				//pas de libellé null : le Select Flow refuse un label null
				if (v != null && v.getLibCommune() != null) {
					libelles.add(v.getLibCommune());
				}
			}
		}
		return libelles;
	}

	/* Le Select Flow jette IllegalArgumentException si le générateur de libellé retourne null */
	private String libelleNonNull(String libelle, String code) {
		if (libelle != null) {
			return libelle;
		}
		return code != null ? code : "";
	}

	private FormLayout createFormLayout() {
		FormLayout formLayout = new FormLayout();
		formLayout.setWidthFull();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		return formLayout;
	}

	private TextField createTextField(String caption, String value, int maxLength) {
		TextField tf = new TextField(caption);
		tf.setValue(value != null ? value : "");
		tf.setWidthFull();
		tf.setMaxLength(maxLength);
		return tf;
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

	private void activerChampPourAdresseAnnuelleEnFrance() {
		fieldVilleEtranger1.setVisible(false);
		fieldCodePostal1.setVisible(true);
		lville1.setVisible(true);
	}

	private void activerChampPourAdresseAnnuelleEtranger() {
		fieldVilleEtranger1.setVisible(true);
		fieldCodePostal1.setVisible(false);
		lville1.setVisible(false);
	}

	private void updateCodePostalVilleAnnuelle() {
		fieldCodePostal1.setValue(codePostalVillesAnnu != null ? codePostalVillesAnnu : "");
	}

	private void updateListeVillesAnnuelle(String text) {
		List<CommuneDTO2> villes1 = adresseController.getVilles(text);
		if (villes1 != null && !villes1.isEmpty()) {
			codePostalVillesAnnu = text;
			if (lville1 != null) {
				lville1.setItems(getLibellesCommunes(villes1));
				lville1.setValue(villes1.get(0).getLibCommune());
			}
		}
	}

	private void activerChampPourAdresseFixeEnFrance() {
		fieldVilleEtranger2.setVisible(false);
		fieldCodePostal2.setVisible(true);
		lville2.setVisible(true);
	}

	private void activerChampPourAdresseFixeEtranger() {
		fieldVilleEtranger2.setVisible(true);
		fieldCodePostal2.setVisible(false);
		lville2.setVisible(false);
	}

	private void updateCodePostalVilleFixe() {
		fieldCodePostal2.setValue(codePostalVillesFixe != null ? codePostalVillesFixe : "");
	}

	private void updateListeVillesFixe(String text) {
		List<CommuneDTO2> villes2 = adresseController.getVilles(text);
		if (villes2 != null && !villes2.isEmpty()) {
			codePostalVillesFixe = text;
			if (lville2 != null) {
				lville2.setItems(getLibellesCommunes(villes2));
				lville2.setValue(villes2.get(0).getLibCommune());
			}
		}
	}

	private void activeFormulaireAdresseAnnuelle() {
		//active les champs adresse annuelle
		fieldAnnu1.setVisible(true);
		fieldAnnu2.setVisible(true);
		fieldAnnu3.setVisible(true);
		lpays1.setVisible(true);
		fieldTelephone1.setVisible(true);
		fieldCodePostal1.setVisible(COD_PAY_FRANCE.equals(lpays1.getValue()));
		lville1.setVisible(COD_PAY_FRANCE.equals(lpays1.getValue()));
		fieldVilleEtranger1.setVisible(!COD_PAY_FRANCE.equals(lpays1.getValue()));
		labelChoixHebergement.setVisible(false);
	}

	private void desactiveFormulaireAdresseAnnuelle() {
		//desactive les champs adresse annuelle
		fieldAnnu1.setVisible(false);
		fieldAnnu2.setVisible(false);
		fieldAnnu3.setVisible(false);
		lpays1.setVisible(false);
		fieldTelephone1.setVisible(false);
		fieldCodePostal1.setVisible(false);
		lville1.setVisible(false);
		fieldVilleEtranger1.setVisible(false);
		labelChoixHebergement.setVisible(true);
	}
}
