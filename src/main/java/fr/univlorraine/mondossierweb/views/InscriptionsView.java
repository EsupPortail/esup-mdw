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
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.HttpStatusCode;
import com.vaadin.flow.server.VaadinSession;
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
import fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList;
import fr.univlorraine.mondossierweb.utils.DownloadFile;
import fr.univlorraine.mondossierweb.views.windows.DetailInscriptionWindow;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

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
				ResponsiveCardList<Inscription> inscriptionsList = new ResponsiveCardList<>();
				inscriptionsList.setWidthFull();
				inscriptionsList.setAllRowsVisible(true);
				String[] colonnes = IA_FIELDS_ORDER;
				if (userController.isEtudiant()) {
					colonnes = IA_FIELDS_ORDER_ETU;
				}
				for (String fieldName : colonnes) {
					inscriptionsList.addColumn(inscription -> getInscriptionFieldValue(inscription, fieldName),
							getTranslation(NAME + ".table." + fieldName))
						.setResizable(true)
						.setFlexGrow(fieldName.equals("cod_anu") || fieldName.equals("cod_comp")? 1 : 0);
				}
				inscriptionsList.addComponentColumn(this::buildLibelleInscriptionColumn,
						getTranslation(NAME + ".table.lib_etp"))
					.setResizable(true)
					.setFlexGrow(5);
				inscriptionsList.addComponentColumn(this::buildActionsColumn,
						getTranslation(NAME + ".table.documents"))
					.setResizable(true)
					.setFlexGrow(3);
				/* Cartes affichées sur petit écran */
				inscriptionsList.setCardRenderer(this::createInscriptionCard);
				inscriptionsList.setItems(etudiant.getLinsciae() != null ? etudiant.getLinsciae() : Collections.emptyList());

				globalLayout.add(createPanel(etudiant.getLibEtablissement(), inscriptionsList));

				// Si on doit afficher les inscriptions dans d'autres cursus
				if (configController.isAffInscriptionsAutreCursus()) {
					Component dacContent;
					if (etudiant.getLinscdac() != null && !etudiant.getLinscdac().isEmpty()) {
						ResponsiveCardList<Inscription> inscriptionsDacList = new ResponsiveCardList<>();
						inscriptionsDacList.setWidthFull();
						inscriptionsDacList.setAllRowsVisible(true);
						for (String fieldName : DAC_FIELDS_ORDER) {
							inscriptionsDacList.addColumn(inscription -> getInscriptionFieldValue(inscription, fieldName),
									getTranslation(NAME + ".tabledac." + fieldName))
								.setResizable(true)
								.setFlexGrow(fieldName.equals(DAC_FIELDS_ORDER[2]) || fieldName.equals(DAC_FIELDS_ORDER[3])? 3 : (fieldName.equals(DAC_FIELDS_ORDER[1])? 2 : 1));
						}
						/* Cartes affichées sur petit écran */
						inscriptionsDacList.setCardRenderer(this::createDacCard);
						inscriptionsDacList.setItems(etudiant.getLinscdac());
						dacContent = inscriptionsDacList;
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
		if (estInscriptionAnneeEnCours(inscription)) {
			//On affiche le bouton pour voir le détail de l'inscription
			Button bDetailInscription = new Button(VaadinIcon.SEARCH.create());
			bDetailInscription.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			bDetailInscription.setTooltipText(getTranslation(NAME + ".inscriptionPedagogique.link"));
			bDetailInscription.addClickListener(e -> ouvrirDetailInscription(inscription));
			//ajout du bouton au layout
			libelleLayout.add(bDetailInscription);
		}

		return libelleLayout;
	}

	/** Vrai si l'inscription porte sur l'année universitaire en cours. */
	private boolean estInscriptionAnneeEnCours(final Inscription inscription) {
		return inscription.getCod_anu() != null && inscription.getCod_anu().length() >= 4
				&& inscription.getCod_anu().substring(0, 4).equals(etudiantController.getAnneeUnivEnCours());
	}

	/** Ouvre la fenêtre de détail (inscription pédagogique) d'une inscription. */
	private void ouvrirDetailInscription(final Inscription inscription) {
		Etape etape = new Etape();
		etape.setAnnee(inscription.getCod_anu());
		etape.setCode(inscription.getCod_etp());
		etape.setVersion(inscription.getCod_vrs_vet());
		etape.setLibelle(inscription.getLib_etp());
		DetailInscriptionWindow dnw = detailInscriptionWindowFactory.getObject();
		dnw.init(etape);
		dnw.open();
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
			actionsLayout.add(createDownloadAnchor(() -> inscriptionController.exportPdf(inscription), bCertificatInscription));
		}

		//Si on peut proposer l'attestation d'affiliation
		if (etudiantController.proposerAttestationAffiliationSSO(inscription, etudiant)) {
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
			actionsLayout.add(createDownloadAnchor(() -> ssoController.exportAffiliationSsoPdf(etudiant, inscription), bAttestationAffiliationSso));
		}

		//Si on peut proposer la quittance
		if (etudiantController.proposerQuittanceDroitsPayes(inscription, etudiant)) {
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
			actionsLayout.add(createDownloadAnchor(() -> ssoController.exportQuittancePdf(etudiant, inscription), bQuittanceSso));
		}

		return actionsLayout;
	}

	/**
	 * Construit la carte d'une inscription, affichée sur petit écran :
	 * badge de l'année et libellé cliquable ouvrant le menu des actions
	 * (détail et documents). Les informations des colonnes Composante, Code
	 * et Version ne sont pas reprises dans la carte.
	 */
	private Div createInscriptionCard(final Inscription inscription) {
		final Div card = new Div();
		card.addClassName("card");

		/* Ligne 1 : badge de l'année */
		final Span badgeAnnee = new Span(inscription.getCod_anu());
		badgeAnnee.addClassName("card-badge");

		/* Ligne 2 : libellé cliquable ouvrant le menu des actions */
		final Span libelle = new Span(inscription.getLib_etp());
		libelle.addClassName(LumoUtility.FontWeight.BOLD);
		libelle.addClassName("libelle-actions-menu");

		final ContextMenu menu = new ContextMenu(libelle);
		menu.setOpenOnClick(true);
		/* En-tête rappelant l'inscription cliquée : libellé puis année */
		final Span enteteLibelle = new Span(inscription.getLib_etp());
		enteteLibelle.addClassName("menu-actions-entete");
		final Span enteteAnnee = new Span(inscription.getCod_anu());
		enteteAnnee.addClassName("menu-actions-entete-id");
		final VerticalLayout entete = new VerticalLayout(enteteLibelle, enteteAnnee);
		entete.setPadding(false);
		entete.setSpacing(false);
		menu.addItem(entete);
		if (!remplirMenuActions(menu, inscription)) {
			/* Aucune action : on l'indique dans le menu plutôt que de ne rien afficher */
			final Span aucuneAction = new Span(getTranslation(NAME + ".aucuneaction"));
			aucuneAction.addClassName("menu-actions-vide");
			menu.addItem(aucuneAction);
		}

		card.add(badgeAnnee, libelle);
		return card;
	}

	/**
	 * Remplit le menu d'actions d'une inscription (affiché sur petit écran au clic
	 * sur le libellé) : détail de l'inscription et documents téléchargeables
	 * disponibles.
	 *
	 * @return vrai si au moins une action a été ajoutée
	 */
	private boolean remplirMenuActions(final ContextMenu menu, final Inscription inscription) {
		final Etudiant etudiant = sessionController.getEtudiant();
		boolean auMoinsUneAction = false;

		//Détail de l'inscription (uniquement sur l'année en cours)
		if (estInscriptionAnneeEnCours(inscription)) {
			menu.addItem(
				actionMenuItem(VaadinIcon.SEARCH, getTranslation(NAME + ".inscriptionPedagogique.link")),
				e -> ouvrirDetailInscription(inscription));
			auMoinsUneAction = true;
		}

		//Certificat de scolarité
		if (etudiantController.proposerCertificat(inscription, etudiant)) {
			menu.addItem(createDownloadMenuItem(() -> inscriptionController.exportPdf(inscription),
				VaadinIcon.FILE_TEXT_O, getTranslation(NAME + ".certificatScolarite.link")));
			auMoinsUneAction = true;
		}

		//Attestation d'affiliation
		if (etudiantController.proposerAttestationAffiliationSSO(inscription, etudiant)) {
			menu.addItem(createDownloadMenuItem(() -> ssoController.exportAffiliationSsoPdf(etudiant, inscription),
				VaadinIcon.FILE_TEXT_O, getTranslation(NAME + ".affiliationSso.link")));
			auMoinsUneAction = true;
		}

		//Quittance
		if (etudiantController.proposerQuittanceDroitsPayes(inscription, etudiant)) {
			menu.addItem(createDownloadMenuItem(() -> ssoController.exportQuittancePdf(etudiant, inscription),
				VaadinIcon.FILE_TEXT_O, getTranslation(NAME + ".quittance.link")));
			auMoinsUneAction = true;
		}

		return auMoinsUneAction;
	}

	/** Construit l'icône et le libellé textuel d'une entrée du menu d'actions. */
	private HorizontalLayout actionMenuItem(final VaadinIcon icone, final String libelle) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		final Icon icon = icone.create();
		icon.setSize("var(--lumo-icon-size-s)");
		layout.add(icon, new Span(libelle));
		return layout;
	}

	/**
	 * Construit une entrée de menu téléchargeable : une ancre (avec génération
	 * différée du document) contenant l'icône et le libellé de l'action.
	 */
	private Anchor createDownloadMenuItem(final Supplier<DownloadFile> dfSupplier, final VaadinIcon icone, final String libelle) {
		return createDownloadAnchor(dfSupplier, actionMenuItem(icone, libelle));
	}

	/**
	 * Construit la carte d'une inscription hors établissement (DAC), affichée sur petit écran :
	 * badge de l'année + type de cursus, spécialité, établissement et résultat.
	 */
	private Div createDacCard(final Inscription inscription) {
		final Div card = new Div();
		card.addClassName("card");

		/* Ligne 1 : badge de l'année + type de cursus */
		final HorizontalLayout ligneEntete = new HorizontalLayout();
		ligneEntete.setSpacing(true);
		ligneEntete.setAlignItems(FlexComponent.Alignment.CENTER);
		final Span badgeAnnee = new Span(inscription.getCod_anu());
		badgeAnnee.addClassName("card-badge");
		ligneEntete.add(badgeAnnee);
		if (StringUtils.hasText(inscription.getCod_dac())) {
			ligneEntete.add(new Span(inscription.getCod_dac()));
		}

		/* Ligne 2 : spécialité en gras */
		final Span specialite = new Span(inscription.getLib_cmt_dac());
		specialite.addClassName(LumoUtility.FontWeight.BOLD);

		card.add(ligneEntete, specialite, new Span(inscription.getLib_etb()), new Span(inscription.getRes()));
		return card;
	}

	/**
	 * Encapsule un bouton dans une ancre de téléchargement du fichier.
	 * La génération du document est différée à la demande de téléchargement :
	 * elle n'a donc pas lieu pour les vues masquées (cartes sur grand écran,
	 * grille sur petit écran) et n'est jamais produite deux fois.
	 *
	 * @param dfSupplier fournit le document à télécharger, généré au clic
	 * @param content composant cliquable (bouton ou entrée de menu) encapsulé dans l'ancre
	 */
	private Anchor createDownloadAnchor(Supplier<DownloadFile> dfSupplier, Component content) {
		Anchor anchor = new Anchor();
		anchor.setHref(DownloadHandler.fromInputStream(evt -> {
			/* Le handler s'exécute hors du verrou de session Vaadin : verrou requis pour
			   résoudre les beans de scope vaadin-session (SessionController) et générer le document */
			final VaadinSession session = evt.getSession();
			session.lock();
			try {
				final DownloadFile df = dfSupplier.get();
				if (df == null) {
					//La génération a échoué : on renvoie une erreur plutôt qu'une page blanche
					return DownloadResponse.error(HttpStatusCode.INTERNAL_SERVER_ERROR);
				}
				return new DownloadResponse(df.getInputStream(), df.fileName(), df.mimeType(), df.getContentLength());
			} finally {
				session.unlock();
			}
		}));
		anchor.add(content);
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
		if (!(content instanceof ResponsiveCardList<?>)) {
			body.getStyle().set("padding", "var(--lumo-space-s)");
		}
		panel.add(body);
		return panel;
	}

}
