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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.InscriptionController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.SsoController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.utils.DownloadFile;
import fr.univlorraine.mondossierweb.views.windows.DetailInscriptionWindow;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Page des inscriptions
 */
@SuppressWarnings("serial")
@Route(value = InscriptionsView.NAME, layout = MainLayout.class)
@Secured(MdwUserDetailsService.CONSULT_DOSSIER_AUTORISE)
public class InscriptionsView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	public static final String NAME = "inscriptionsView";
	public static final String ROUTE = NAME;

	public static final String[] IA_FIELDS_ORDER = {"cod_anu", "cod_comp", "cod_etp", "cod_vrs_vet"};

	public static final String[] IA_FIELDS_ORDER_ETU = {"cod_anu", "lib_comp"};

	public static final String[] DAC_FIELDS_ORDER = {"cod_anu", "cod_dac", "lib_cmt_dac", "lib_etb", "res"};

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient InscriptionController inscriptionController;
	@Resource
	private transient SsoController ssoController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient SessionController sessionController;
	@Resource
	private transient ObjectFactory<DetailInscriptionWindow> detailInscriptionWindowFactory;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	/**
	 * Initialise la vue à chaque navigation.
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		removeAll();

		final Etudiant etudiant = sessionController.getEtudiant();

		//On vérifie le droit d'accéder à la vue
		if ((userController.isEnseignant() || userController.isEtudiant()) && etudiant != null) {
			/* Style */
			setPadding(true);
			setSpacing(true);

			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setWidthFull();
			globalLayout.setPadding(false);
			globalLayout.setSpacing(true);

			//Si les informations sur les inscriptions n'ont pas déjà été récupérées, on les récupère
			if (etudiant.getLibEtablissement() == null) {
				etudiantController.recupererInscriptions();
			}

			//Test si la récupération des inscriptions via le WS s'est bien passée
			if (sessionController.isRecuperationWsInscriptionsOk()) {

				//Tout s'est bien passé lors de la récupération des infos via le WS
				Grid<Inscription> inscriptionsGrid = new Grid<>();
				inscriptionsGrid.setWidthFull();
				inscriptionsGrid.setAllRowsVisible(true);
				inscriptionsGrid.setSelectionMode(Grid.SelectionMode.NONE);
				String[] colonnes = IA_FIELDS_ORDER;
				if (userController.isEtudiant()) {
					colonnes = IA_FIELDS_ORDER_ETU;
				}
				for (String fieldName : colonnes) {
					inscriptionsGrid.addColumn(inscription -> getInscriptionFieldValue(inscription, fieldName))
						.setHeader(getTranslation(NAME + ".table." + fieldName))
						.setFlexGrow(1);
				}
				inscriptionsGrid.addComponentColumn(this::buildLibelleInscriptionColumn)
					.setHeader(getTranslation(NAME + ".table.lib_etp"))
					.setFlexGrow(3);
				inscriptionsGrid.addComponentColumn(this::buildActionsColumn)
					.setHeader(getTranslation(NAME + ".table.documents"))
					.setFlexGrow(0);
				inscriptionsGrid.setItems(etudiant.getLinsciae() != null ? etudiant.getLinsciae() : Collections.emptyList());

				globalLayout.add(createPanel(etudiant.getLibEtablissement(), inscriptionsGrid));

				// Si on doit afficher les inscriptions dans d'autres cursus
				if (configController.isAffInscriptionsAutreCursus()) {
					Component dacContent;
					if (etudiant.getLinscdac() != null && !etudiant.getLinscdac().isEmpty()) {
						Grid<Inscription> inscriptionsDAC = new Grid<>();
						inscriptionsDAC.setWidthFull();
						inscriptionsDAC.setAllRowsVisible(true);
						inscriptionsDAC.setSelectionMode(Grid.SelectionMode.NONE);
						for (String fieldName : DAC_FIELDS_ORDER) {
							inscriptionsDAC.addColumn(inscription -> getInscriptionFieldValue(inscription, fieldName))
								.setHeader(getTranslation(NAME + ".tabledac." + fieldName))
								.setFlexGrow(1);
						}
						inscriptionsDAC.setItems(etudiant.getLinscdac());
						dacContent = inscriptionsDAC;
					} else {
						Span aucuneDAC = new Span(getTranslation(NAME + ".dac.aucune") + " " + etudiant.getLibEtablissement());
						aucuneDAC.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.BOLD);
						dacContent = aucuneDAC;
					}
					globalLayout.add(createPanel(getTranslation(NAME + ".dac.title"), dacContent));
				}

				FormLayout formPremInscription = new FormLayout();
				formPremInscription.setWidthFull();
				formPremInscription.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
				addFormValue(formPremInscription, getTranslation(NAME + ".premiereinsc.annee"), etudiant.getAnneePremiereInscrip());
				addFormValue(formPremInscription, getTranslation(NAME + ".premiereinsc.etb"), etudiant.getEtbPremiereInscrip());
				globalLayout.add(createPanel(getTranslation(NAME + ".premiereinsc.title"), formPremInscription));

				add(globalLayout);
			} else {
				//Il y a eu un souci lors de la récupération des infos via le WS
				add(createPanel(null, new Span(getTranslation("AllView.erreur.message"))));
			}
		}
	}

	private String getInscriptionFieldValue(Inscription inscription, String fieldName) {
		switch (fieldName) {
			case "cod_anu":
				return inscription.getCod_anu();
			case "cod_comp":
				return inscription.getCod_comp();
			case "lib_comp":
				return inscription.getLib_comp();
			case "cod_etp":
				return inscription.getCod_etp();
			case "cod_vrs_vet":
				return inscription.getCod_vrs_vet();
			case "cod_dac":
				return inscription.getCod_dac();
			case "lib_cmt_dac":
				return inscription.getLib_cmt_dac();
			case "lib_etb":
				return inscription.getLib_etb();
			case "res":
				return inscription.getRes();
			default:
				return "";
		}
	}

	/**
	 * Colonne libellé de l'inscription : libellé de l'étape + bouton de détail.
	 */
	private Component buildLibelleInscriptionColumn(Inscription inscription) {
		HorizontalLayout libelleLayout = new HorizontalLayout();
		libelleLayout.setSpacing(true);
		libelleLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		//ajout du libellé de l'étape concernée par l'inscription
		libelleLayout.add(new Span(inscription.getLib_etp()));

		//Si c'est une inscription sur l'année en cours
		if (inscription.getCod_anu() != null && inscription.getCod_anu().length() >= 4
				&& inscription.getCod_anu().substring(0, 4).equals(etudiantController.getAnneeUnivEnCours())) {
			//On affiche le bouton pour voir le détail de l'inscription
			Button bDetailInscription = new Button(VaadinIcon.SEARCH.create());
			bDetailInscription.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			bDetailInscription.setTooltipText(getTranslation(NAME + ".inscriptionPedagogique.link"));
			//Appel de la window contenant le détail de l'inscription
			Etape etape = new Etape();
			etape.setAnnee(inscription.getCod_anu());
			etape.setCode(inscription.getCod_etp());
			etape.setVersion(inscription.getCod_vrs_vet());
			etape.setLibelle(inscription.getLib_etp());
			bDetailInscription.addClickListener(e -> {
				DetailInscriptionWindow dnw = detailInscriptionWindowFactory.getObject();
				dnw.init(etape);
				dnw.open();
			});
			//ajout du bouton au layout
			libelleLayout.add(bDetailInscription);
		}

		return libelleLayout;
	}

	/**
	 * Colonne documents de l'inscription : boutons (certificat,
	 * attestation d'affiliation, quittance).
	 */
	private Component buildActionsColumn(Inscription inscription) {
		final Etudiant etudiant = sessionController.getEtudiant();

		HorizontalLayout actionsLayout = new HorizontalLayout();
		actionsLayout.setSpacing(true);
		actionsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		//Si on peut proposer le certificat de scolarité
		if (etudiantController.proposerCertificat(inscription, etudiant)) {
			DownloadFile df = inscriptionController.exportPdf(inscription);
			if (df != null) {
				//On affiche le bouton pour éditer le certificat de scolarité
				Button bCertificatInscription = new Button(VaadinIcon.FILE_TEXT_O.create());
				//Si on n'affiche pas le bouton sous forme de libellé
				if (!configController.isAffBtnCertifNouvelleLigne()) {
					bCertificatInscription.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					bCertificatInscription.setTooltipText(getTranslation(NAME + ".certificatScolarite.link"));
					if (configController.isAffBtnCertificatCouleur()) {
						bCertificatInscription.addClassName("red-button-icon");
					}
				} else {
					bCertificatInscription.addThemeVariants(ButtonVariant.LUMO_SMALL);
					bCertificatInscription.setText(getTranslation(NAME + ".certificatScolarite.btn.link"));
				}
				actionsLayout.add(createDownloadAnchor(df, bCertificatInscription));
			}
		}

		//Si on peut proposer l'attestation d'affiliation
		if (etudiantController.proposerAttestationAffiliationSSO(inscription, etudiant)) {
			DownloadFile df = ssoController.exportAffiliationSsoPdf(etudiant, inscription);
			if (df != null) {
				//On affiche le bouton pour éditer l'attestation d'affiliation
				Button bAttestationAffiliationSso = new Button(VaadinIcon.FILE_TEXT_O.create());
				//Si on n'affiche pas le bouton sous forme de libellé
				if (!configController.isAffBtnAttestSsoNouvelleLigne()) {
					bAttestationAffiliationSso.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					bAttestationAffiliationSso.setTooltipText(getTranslation(NAME + ".affiliationSso.link"));
					bAttestationAffiliationSso.addClassName("blue-button-icon");
				} else {
					bAttestationAffiliationSso.addThemeVariants(ButtonVariant.LUMO_SMALL);
					bAttestationAffiliationSso.setText(getTranslation(NAME + ".affiliationSso.btn.link"));
				}
				actionsLayout.add(createDownloadAnchor(df, bAttestationAffiliationSso));
			}
		}

		//Si on peut proposer la quittance
		if (etudiantController.proposerQuittanceDroitsPayes(inscription, etudiant)) {
			DownloadFile df = ssoController.exportQuittancePdf(etudiant, inscription);
			if (df != null) {
				//On affiche le bouton pour éditer la quittance
				Button bQuittanceSso = new Button(VaadinIcon.FILE_TEXT_O.create());
				//Si on n'affiche pas le bouton sous forme de libellé
				if (!configController.isAffBtnQuittanceDroitsPayesNouvelleLigne()) {
					bQuittanceSso.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
					bQuittanceSso.setTooltipText(getTranslation(NAME + ".quittance.link"));
					if (configController.isAffBtnQuittanceCouleur()) {
						bQuittanceSso.addClassName("green-button-icon");
					}
				} else {
					bQuittanceSso.addThemeVariants(ButtonVariant.LUMO_SMALL);
					bQuittanceSso.setText(getTranslation(NAME + ".quittance.btn.link"));
				}
				actionsLayout.add(createDownloadAnchor(df, bQuittanceSso));
			}
		}

		return actionsLayout;
	}

	/**
	 * Encapsule un bouton dans une ancre de téléchargement du fichier.
	 */
	private Anchor createDownloadAnchor(DownloadFile df, Button button) {
		Anchor anchor = new Anchor();
		anchor.setHref(DownloadHandler.fromInputStream(evt ->
			new DownloadResponse(df.getInputStream(), df.fileName(), df.mimeType(), df.getContentLength())));
		anchor.add(button);
		return anchor;
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
		panel.setWidthFull();
		panel.getStyle()
			.set("border", "1px solid var(--lumo-contrast-20pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("overflow", "hidden");
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
		body.getStyle().set("padding", "var(--lumo-space-s)");
		panel.add(body);
		return panel;
	}

}
