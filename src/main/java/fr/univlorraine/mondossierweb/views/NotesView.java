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
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import fr.univlorraine.mondossierweb.beans.Diplome;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.beans.Resultat;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.NoteController;
import fr.univlorraine.mondossierweb.controllers.ResultatController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList;
import fr.univlorraine.mondossierweb.utils.DownloadFile;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.DetailNotesWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpBasicWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpWindow;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Page des notes
 */
@SuppressWarnings("serial")
@Route(value = NotesView.NAME, layout = MainLayout.class)
@Secured(MdwUserDetailsService.CONSULT_DOSSIER_AUTORISE)
@Slf4j
public class NotesView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	public static final String NAME = "notesView";
	public static final String ROUTE = NAME;

	private boolean vueEnseignant;

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource(name = "${resultat.implementation}")
	private transient ResultatController resultatController;
	@Resource
	private transient NoteController noteController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient SessionController sessionController;
	@Resource
	private transient ObjectFactory<DetailNotesWindow> detailNotesWindowFactory;

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
		removeAll();

		final Etudiant etudiant = sessionController.getEtudiant();

		/* Titre */
		HorizontalLayout titleLayout = new HorizontalLayout();
		titleLayout.setWidthFull();
		titleLayout.setSpacing(true);
		titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		titleLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

		//On vérifie le droit d'accéder à la vue
		if (etudiant != null
				&& (((userController.isEtudiant() || ((isUserEnseignantWithAccess() || isUserGestionnaireWithAccess()) && !voirCommeEnseignant()))
						&& configController.isAffNotesEtudiant() && !etudiant.isNonAutoriseConsultationNotes())
					|| (isUserEnseignantWithAccess() && voirCommeEnseignant())
					|| (isUserGestionnaireWithAccess() && voirCommeEnseignant()))) {

			log.debug(userController.getCurrentUserName() + " NotesView");

			/* Style */
			setPadding(true);
			setSpacing(true);

			//Test si user enseignant et en vue Enseignant
			if (userController.isEnseignant() && voirCommeEnseignant()) {
				//On recupere les notes pour un enseignant
				resultatController.renseigneNotesEtResultats(etudiant, userController.isGestionnaire());
			} else {
				//On récupère les notes pour un étudiant
				resultatController.renseigneNotesEtResultatsEtudiant(etudiant);
			}

			// Ajouter le bloc "Voir comme un étudiant" (enseignants/gestionnaires), sur la même ligne que les boutons PDF et informations
			ajouterVoirCommeUnEtudiant(titleLayout);

			// L'encart « Voir comme un enseignant » est affiché dans les mêmes conditions que
			// son ajout par ajouterVoirCommeUnEtudiant (gestionnaire ou enseignant avec accès).
			final boolean encartVueAffiche = isUserGestionnaireWithAccess() || isUserEnseignantWithAccess();

			//Test si on a des diplomes ou des etapes
			Anchor pdfAnchor = null;
			if ((etudiant.getDiplomes() != null && !etudiant.getDiplomes().isEmpty())
					|| (etudiant.getEtapes() != null && !etudiant.getEtapes().isEmpty())) {
				DownloadFile df = noteController.exportPdfResume();
				if (df != null) {
					Button pdfButton = new Button(VaadinIcon.FILE_TEXT_O.create());
					pdfButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
					pdfButton.addClassName("button-big-icon");
					pdfButton.addClassName("red-button-icon");
					pdfButton.setTooltipText(getTranslation(NAME + ".btn.pdf.description"));
					if (!encartVueAffiche) {
						// Pas d'encart : le bouton affiche son libellé texte à côté de l'icône
						pdfButton.setText(getTranslation(NAME + ".btn.pdf.label"));
					}
					pdfAnchor = new Anchor();
					pdfAnchor.setHref(DownloadHandler.fromInputStream(evt ->
						new DownloadResponse(df.getInputStream(), df.fileName(), df.mimeType(), df.getContentLength())));
					pdfAnchor.add(pdfButton);
				}
			}

			// Bouton d'information sur la signification des résultats
			Button infoButton = null;
			if (etudiant.isSignificationResultatsUtilisee()) {
				infoButton = new Button(VaadinIcon.INFO_CIRCLE.create());
				infoButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				infoButton.addClassName("button-big-icon");
				infoButton.setTooltipText(getTranslation(NAME + ".info.significations.resultats"));
				infoButton.getElement().setAttribute("aria-label", getTranslation(NAME + ".info.significations.resultats"));
				infoButton.addClickListener(e -> new HelpBasicWindow(buildSignificationResultatsHtml(etudiant),
						getTranslation(NAME + ".info.significations.resultats")).open());
				if (!encartVueAffiche) {
					// Pas d'encart : le bouton affiche son libellé texte à côté de l'icône
					infoButton.setText(getTranslation(NAME + ".btn.infos.label"));
				}
			}

			// Les boutons PDF et informations sont regroupés dans un même conteneur, au même
			// niveau que l'encart « Voir comme un enseignant » (à droite de l'entête).
			if (pdfAnchor != null || infoButton != null) {
				titleLayout.add(buildConteneurBoutons(encartVueAffiche, pdfAnchor, infoButton));
			}
			add(titleLayout);

			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setWidthFull();
			globalLayout.setPadding(false);
			globalLayout.setSpacing(false);

			/* Les diplômes */
			ResponsiveCardList<Diplome> notesDiplomesList = new ResponsiveCardList<>();
			notesDiplomesList.setWidthFull();
			notesDiplomesList.setAllRowsVisible(true);
			notesDiplomesList.addColumn(Diplome::getAnnee,
					getTranslation(NAME + ".table.diplomes.annee")).setFlexGrow(1).setResizable(true);
			notesDiplomesList.addColumn(Diplome::getCod_dip,
					getTranslation(NAME + ".table.diplomes.cod_dip")).setFlexGrow(1).setResizable(true);
			notesDiplomesList.addComponentColumn(diplome -> buildLibelleWrappable(diplome.getLib_web_vdi()),
					getTranslation(NAME + ".table.diplomes.lib_web_vdi")).setFlexGrow(3).setResizable(true);
			notesDiplomesList.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), Resultat::getSession),
					getTranslation(NAME + ".table.diplomes.session")).setFlexGrow(1).setResizable(true);
			notesDiplomesList.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), this::formatNote),
					getTranslation(NAME + ".table.diplomes.note")).setFlexGrow(1).setResizable(true);
			notesDiplomesList.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), Resultat::getAdmission),
					getTranslation(NAME + ".table.diplomes.resultat")).setFlexGrow(1).setResizable(true);
			if (configController.isAffMentionEtudiant()) {
				notesDiplomesList.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), Resultat::getCodMention),
						getTranslation(NAME + ".table.diplomes.mention")).setFlexGrow(1).setResizable(true);
			}
			if (etudiant.isAfficherRang()) {
				notesDiplomesList.addComponentColumn(diplome -> buildRangColumn(diplome.getRang(), diplome.isAfficherRang()),
						getTranslation(NAME + ".table.diplomes.rang")).setFlexGrow(1).setResizable(true);
			}
			/* Cartes affichées sur petit écran */
			notesDiplomesList.setCardRenderer(this::createDiplomeCard);
			notesDiplomesList.setItems(etudiant.getDiplomes() != null ? etudiant.getDiplomes() : Collections.emptyList());
			final Component panelDiplomes = createPanel(getTranslation(NAME + ".table.diplomes"), notesDiplomesList);
			panelDiplomes.setId("notes-section-diplomes");
			panelDiplomes.addClassNames("notes-section", "notes-section-diplomes");

			/* Les étapes */
			ResponsiveCardList<Etape> notesEtapesList = new ResponsiveCardList<>();
			notesEtapesList.setWidthFull();
			notesEtapesList.setAllRowsVisible(true);
			notesEtapesList.addColumn(Etape::getAnnee,
					getTranslation(NAME + ".table.etapes.annee")).setFlexGrow(1).setResizable(true);
			notesEtapesList.addComponentColumn(this::buildCodeEtapeColumn,
					getTranslation(NAME + ".table.etapes.codevers")).setFlexGrow(1).setResizable(true);
			notesEtapesList.addComponentColumn(this::buildLibelleEtapeColumn,
					getTranslation(NAME + ".table.etapes.libelle")).setFlexGrow(3).setResizable(true);
			notesEtapesList.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), Resultat::getSession),
					getTranslation(NAME + ".table.etapes.session")).setFlexGrow(1).setResizable(true);
			notesEtapesList.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), this::formatNote),
					getTranslation(NAME + ".table.etapes.note")).setFlexGrow(1).setResizable(true);
			notesEtapesList.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), Resultat::getAdmission),
					getTranslation(NAME + ".table.etapes.resultat")).setFlexGrow(1).setResizable(true);
			if (configController.isAffMentionEtudiant()) {
				notesEtapesList.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), Resultat::getCodMention),
						getTranslation(NAME + ".table.etapes.mention")).setFlexGrow(1).setResizable(true);
			}
			if (etudiant.isAfficherRang()) {
				notesEtapesList.addComponentColumn(etape -> buildRangColumn(etape.getRang(), etape.isAfficherRang()),
						getTranslation(NAME + ".table.etapes.rang")).setFlexGrow(1).setResizable(true);
			}
			/* Cartes affichées sur petit écran */
			notesEtapesList.setCardRenderer(this::createEtapeCard);
			notesEtapesList.setItems(etudiant.getEtapes() != null ? etudiant.getEtapes() : Collections.emptyList());
			final Component panelEtapes = createPanel(getTranslation(NAME + ".table.etapes"), notesEtapesList);
			panelEtapes.setId("notes-section-etapes");
			panelEtapes.addClassNames("notes-section", "notes-section-etapes");

			/* Sur mobile, un bouton par section permet d'afficher l'une ou l'autre ;
			   sur desktop, les boutons sont masqués et les deux sections restent empilées (voir styles.css). */
			final VerticalLayout sectionsLayout = new VerticalLayout(panelDiplomes, panelEtapes);
			sectionsLayout.setWidthFull();
			sectionsLayout.setPadding(false);
			sectionsLayout.setSpacing(true);
			sectionsLayout.addClassName("notes-sections");

			globalLayout.add(buildSectionsSwitch(sectionsLayout), sectionsLayout);

			add(globalLayout);
		} else {
			// Ajouter bloc "Voir comme un étudiant" si nécessaire
			ajouterVoirCommeUnEtudiant(titleLayout);
			add(titleLayout);

			//Si on est dans le cas d'un blocage apogée
			if (etudiant != null
					&& (userController.isEtudiant() || ((isUserEnseignantWithAccess() || isUserGestionnaireWithAccess()) && !voirCommeEnseignant()))
					&& etudiant.isNonAutoriseConsultationNotes()) {
				//message non autorisé.
				add(new Span(getTranslation(NAME + ".blocage.msg")));
			}
		}
	}

	private void ajouterVoirCommeUnEtudiant(HorizontalLayout titleLayout) {
		//Test si user enseignant
		if (isUserGestionnaireWithAccess() || isUserEnseignantWithAccess()) {
			HorizontalLayout vueLayout = new HorizontalLayout();
			vueLayout.setWidthFull();
			vueLayout.setPadding(false);
			/* Sur écran large, le texte est à côté du bouton ; sur mobile, il passe en dessous (voir styles.css) */
			vueLayout.addClassName("vue-enseignant-layout");

			Button changerVueButton = new Button(getTranslation(NAME + ".button.vueEnseignant"));
			changerVueButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			if (voirCommeEnseignant()) {
				changerVueButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
				changerVueButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
				changerVueButton.setText(getTranslation(NAME + ".button.vueEtudiant"));
			}
			//On change la variable vueEnseignantNotesEtResultats et on recréé la vue en cours
			changerVueButton.addClickListener(e -> {
				resultatController.changerVueNotesEtResultats();
				init();
			});

			Html vueLabel = new Html("<div>"
				+ getTranslation(voirCommeEnseignant() ? NAME + ".label.vueEnseignant" : NAME + ".label.vueEtudiant")
				+ "</div>");
			Div vueLabelWrapper = new Div(vueLabel);
			vueLabelWrapper.addClassName(LumoUtility.FontSize.SMALL);

			vueLayout.add(changerVueButton);
			vueLayout.add(vueLabelWrapper);

			/* Le bloc est placé dans l'entête, à gauche du bouton PDF */
			Div vuePanel = createPanel(null, vueLayout);
			titleLayout.add(vuePanel);
			titleLayout.expand(vuePanel);
		}
	}

	/**
	 * Construit les boutons de bascule entre les sections « Diplômes » et
	 * « Étapes ». Ils ne sont visibles que sur mobile (voir styles.css) ; sur
	 * desktop, les deux sections restent affichées l'une sous l'autre.
	 */
	private HorizontalLayout buildSectionsSwitch(final Component sectionsLayout) {
		final HorizontalLayout switchLayout = new HorizontalLayout();
		switchLayout.addClassName("notes-sections-switch");
		switchLayout.setWidthFull();
		switchLayout.setSpacing(false);

		final Button btnDiplomes = new Button(getTranslation(NAME + ".table.diplomes"));
		btnDiplomes.addClassName("notes-section-button");
		btnDiplomes.getElement().setAttribute("aria-controls", "notes-section-diplomes");

		final Button btnEtapes = new Button(getTranslation(NAME + ".button.etapes"));
		btnEtapes.addClassName("notes-section-button");
		btnEtapes.getElement().setAttribute("aria-controls", "notes-section-etapes");

		btnDiplomes.addClickListener(e -> afficherSection(sectionsLayout, btnDiplomes, btnEtapes, true));
		btnEtapes.addClickListener(e -> afficherSection(sectionsLayout, btnDiplomes, btnEtapes, false));

		switchLayout.add(btnDiplomes, btnEtapes);
		//La section des diplômes est affichée par défaut
		afficherSection(sectionsLayout, btnDiplomes, btnEtapes, true);
		return switchLayout;
	}

	/**
	 * Affiche la section demandée sur mobile et met à jour l'état des boutons.
	 * La classe posée sur le conteneur n'a d'effet qu'en dessous du seuil mobile.
	 */
	private void afficherSection(final Component sectionsLayout, final Button btnDiplomes,
			final Button btnEtapes, final boolean diplomes) {
		sectionsLayout.removeClassName("afficher-diplomes");
		sectionsLayout.removeClassName("afficher-etapes");
		sectionsLayout.addClassName(diplomes ? "afficher-diplomes" : "afficher-etapes");
		majBoutonSection(btnDiplomes, diplomes);
		majBoutonSection(btnEtapes, !diplomes);
	}

	/**
	 * Met à jour l'état visuel et accessible d'un bouton de bascule de section.
	 * Le bouton de la section masquée est souligné pour montrer qu'il est cliquable.
	 */
	private void majBoutonSection(final Button bouton, final boolean actif) {
		bouton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
		if (actif) {
			bouton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			bouton.removeClassName("notes-section-button-inactive");
		} else {
			bouton.addClassName("notes-section-button-inactive");
		}
		bouton.getElement().setAttribute("aria-pressed", String.valueOf(actif));
	}

	/**
	 * Regroupe les boutons PDF et informations dans un conteneur placé à droite de
	 * l'entête. Si l'encart « Voir comme un enseignant » est affiché, les boutons
	 * sont empilés verticalement en icône seule ; sinon ils sont côte à côte avec
	 * leur libellé texte.
	 */
	private Component buildConteneurBoutons(final boolean encartVueAffiche, final Anchor pdfAnchor, final Button infoButton) {
		if (encartVueAffiche) {
			VerticalLayout boutonsLayout = new VerticalLayout();
			/* Largeur au contenu : le conteneur se place à droite de l'entête */
			boutonsLayout.setWidth("auto");
			boutonsLayout.setPadding(false);
			boutonsLayout.setSpacing(false);
			boutonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
			if (pdfAnchor != null) {
				boutonsLayout.add(pdfAnchor);
			}
			if (infoButton != null) {
				boutonsLayout.add(infoButton);
			}
			return boutonsLayout;
		}
		HorizontalLayout boutonsLayout = new HorizontalLayout();
		/* Largeur au contenu : le conteneur se place à droite de l'entête */
		boutonsLayout.setWidth("auto");
		boutonsLayout.setPadding(false);
		boutonsLayout.setSpacing(true);
		boutonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		if (pdfAnchor != null) {
			boutonsLayout.add(pdfAnchor);
		}
		if (infoButton != null) {
			boutonsLayout.add(infoButton);
		}
		return boutonsLayout;
	}

	private boolean voirCommeEnseignant() {
		return sessionController.isVueEnseignantNotesEtResultats();
	}

	/**
	 * Construit le contenu HTML de la signification des résultats, affiché dans la pop-up d'information.
	 */
	private String buildSignificationResultatsHtml(Etudiant etudiant) {
		StringBuilder grilleSignificationResultats = new StringBuilder();
		@SuppressWarnings("unchecked")
		Map<String, String> significations = etudiant.getSignificationResultats();
		Set<String> ss = significations.keySet();
		for (String k : ss) {
			if (k != null && !k.equals("") && !k.equals(" ")) {
				grilleSignificationResultats.append("<span class=\"signification-item\"><b>")
					.append(k).append("</b>&#160;:&#160;")
					.append(significations.get(k)).append("</span>");
			}
		}
		return grilleSignificationResultats.toString();
	}

	private boolean isUserGestionnaireWithAccess() {
		return userController.isGestionnaire() && configController.isAffNotesGestionnaire();
	}

	private boolean isUserEnseignantWithAccess() {
		return userController.isEnseignant() && configController.isAffNotesEnseignant();
	}

	/**
	 * Note avec barème éventuel (ancienne NoteColumnGenerator).
	 */
	private String formatNote(Resultat r) {
		String res = r.getNote();
		//ajout du bareme si différent de 0 et de 20
		if (r.getBareme() != 0 && (configController.isToujoursAfficherBareme() || r.getBareme() != 20)) {
			res = res + "/" + r.getBareme();
		}
		return res;
	}

	/**
	 * Colonne affichant une valeur par résultat (anciennes Session/Note/Resultat/MentionColumnGenerator).
	 */
	private Component buildResultatsColumn(List<Resultat> resultats, ValueProviderResultat valueProvider) {
		VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(false);
		if (resultats != null) {
			for (Resultat r : resultats) {
				String value = valueProvider.getValue(r);
				layout.add(new Span(value != null ? value : ""));
			}
		}
		return layout;
	}

	/**
	 * Colonne du rang (ancienne RangColumnGenerator).
	 */
	private Component buildRangColumn(String rang, boolean afficherRang) {
		if (afficherRang && StringUtils.hasText(rang)) {
			return new Span(rang);
		}
		return new Span();
	}

	/**
	 * Colonne code/version de l'étape (ancienne CodeEtapeColumnGenerator).
	 */
	private Component buildCodeEtapeColumn(Etape etape) {
		/* Le code n'est plus cliquable : simple Span pouvant passer sur plusieurs lignes */
		return buildLibelleWrappable(etape.getCode() + "/" + etape.getVersion());
	}

	/**
	 * Colonne libellé de l'étape (ancienne LibelleEtapeColumnGenerator).
	 */
	private Component buildLibelleEtapeColumn(Etape etape) {
		return buildEtapeLinkColumn(etape, etape.getLibelle());
	}

	private Component buildEtapeLinkColumn(Etape etape, String libelle) {
		if (etape.getRang() == null || !etape.getRang().equals("NC")) {
			Button b = new Button(libelle);
			b.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			/* Libellé cliquable souligné (voir styles.css, ::part(label)) */
			b.addClassName("lien-souligne");
			//Appel de la window contenant le détail des notes
			prepareBoutonAppelDetailDesNotes(b, etape);
			return b;
		}
		/* Cas non cliquable : le libellé doit aussi pouvoir passer sur 2 lignes */
		return buildLibelleWrappable(libelle);
	}

	/**
	 * Construit un Span dont le texte peut passer sur plusieurs lignes.
	 */
	private Span buildLibelleWrappable(String texte) {
		Span span = new Span(texte);
		span.getStyle().set("white-space", "normal");
		span.getStyle().set("overflow-wrap", "anywhere");
		return span;
	}

	private void prepareBoutonAppelDetailDesNotes(Button b, Etape etape) {
		//Appel de la window contenant le détail des notes
		b.addClickListener(e -> {
			DetailNotesWindow dnw = detailNotesWindowFactory.getObject();
			dnw.init(etape);
			vueEnseignant = voirCommeEnseignant();
			dnw.addOpenedChangeListener(f -> {
				//Si la vue a changé, on repasse par l'init
				if (!f.isOpened() && vueEnseignant != voirCommeEnseignant()) {
					init();
				}
			});
			dnw.open();

			//Recuperer dans la base si l'utilisateur a demandé à ne plus afficher le message
			boolean afficherMessage = true;
			//Si on a paramétré l'application pour ne pas afficher le message
			if (!configController.isAffMessageNotesInformatives()) {
				//On bloque l'affichage
				afficherMessage = false;
			} else {
				//Si l'utilisateur n'est pas un étudiant
				if (!userController.isEtudiant()) {
					//On vérifie s'il a demandé à ne plus afficher le message
					String val = userController.getPreference(Utils.SHOW_MESSAGE_NOTES_PREFERENCE);
					if (StringUtils.hasText(val)) {
						afficherMessage = Boolean.valueOf(val);
					}
				}
			}

			if (afficherMessage) {
				String message = getTranslation(NAME + ".window.message.info");
				HelpWindow hbw = new HelpWindow(message, getTranslation("helpWindow.defaultTitle"), !userController.isEtudiant());
				hbw.addOpenedChangeListener(g -> {
					if (!g.isOpened() && !userController.isEtudiant()) {
						//Test si l'utilisateur a coché la case pour ne plus afficher le message
						if (Boolean.TRUE.equals(hbw.getCheckBox().getValue())) {
							//mettre a jour dans la base de données
							userController.updatePreference(Utils.SHOW_MESSAGE_NOTES_PREFERENCE, "false");
						}
					}
				});
				hbw.open();
			}
		});
	}

	/**
	 * Construit la carte d'un diplôme, affichée sur petit écran : à gauche
	 * l'année et le libellé ; à droite les sessions (libellé, note, résultat)
	 * puis, en pied, le rang et la mention.
	 */
	private Div createDiplomeCard(final Diplome diplome) {
		final Div card = new Div();
		card.addClassName("card");

		final HorizontalLayout split = new HorizontalLayout();
		split.setWidthFull();
		split.setSpacing(true);
		split.setPadding(false);
		split.setAlignItems(FlexComponent.Alignment.START);
		split.addClassName("card-split");

		/* Colonne gauche : année puis libellé (une ligne par info) */
		final VerticalLayout gauche = new VerticalLayout();
		gauche.setPadding(false);
		gauche.setSpacing(false);
		gauche.setAlignItems(FlexComponent.Alignment.STRETCH);
		gauche.addClassName("card-split-left");
		final Span anneeDiplome = new Span(diplome.getAnnee() != null ? diplome.getAnnee() : "");
		anneeDiplome.addClassName("card-badge");
		/* Le fond du badge ne doit englober que le texte : on ne l'étire pas sur toute la largeur */
		anneeDiplome.getStyle().set("align-self", "flex-start");
		gauche.add(anneeDiplome);
		gauche.add(new Span(diplome.getLib_web_vdi() != null ? diplome.getLib_web_vdi() : ""));

		/* Colonne droite : sessions puis pied (rang, mention) */
		final VerticalLayout droite = buildResultatsCarte(diplome.getResultats(), diplome.getRang(), diplome.isAfficherRang(),
				getTranslation(NAME + ".table.diplomes.rang"), getTranslation(NAME + ".table.diplomes.mention"));

		split.add(gauche, droite);
		split.expand(droite);
		card.add(split);
		return card;
	}

	/**
	 * Construit la carte d'une étape, affichée sur petit écran : à gauche
	 * l'année et le libellé (cliquable) ; à droite les sessions (libellé, note,
	 * résultat) puis, en pied, le rang et la mention.
	 */
	private Div createEtapeCard(final Etape etape) {
		final Div card = new Div();
		card.addClassName("card");

		final HorizontalLayout split = new HorizontalLayout();
		split.setWidthFull();
		split.setSpacing(true);
		split.setPadding(false);
		split.setAlignItems(FlexComponent.Alignment.START);
		split.addClassName("card-split");

		/* Colonne gauche : année puis libellé (cliquable) */
		final VerticalLayout gauche = new VerticalLayout();
		gauche.setPadding(false);
		gauche.setSpacing(false);
		gauche.setAlignItems(FlexComponent.Alignment.STRETCH);
		gauche.addClassName("card-split-left");
		final Span anneeEtape = new Span(etape.getAnnee() != null ? etape.getAnnee() : "");
		anneeEtape.addClassName("card-badge");
		/* Le fond du badge ne doit englober que le texte : on ne l'étire pas sur toute la largeur */
		anneeEtape.getStyle().set("align-self", "flex-start");
		gauche.add(anneeEtape);
		/* Le libellé de la filière peut passer sur plusieurs lignes si la place manque */
		final Component libelleEtape = buildLibelleEtapeColumn(etape);
		libelleEtape.getElement().getClassList().add("card-libelle-wrap");
		gauche.add(libelleEtape);

		/* Colonne droite : sessions puis pied (rang, mention) */
		final VerticalLayout droite = buildResultatsCarte(etape.getResultats(), etape.getRang(), etape.isAfficherRang(),
				getTranslation(NAME + ".table.etapes.rang"), getTranslation(NAME + ".table.etapes.mention"));

		split.add(gauche, droite);
		split.expand(droite);
		card.add(split);
		return card;
	}

	/**
	 * Construit la partie droite d'une carte : une colonne par session
	 * (libellé, note, résultat), puis un pied avec le rang et la mention.
	 */
	private VerticalLayout buildResultatsCarte(final List<Resultat> resultats, final String rang, final boolean afficherRang,
			final String rangLabel, final String mentionLabel) {
		final Etudiant etudiant = sessionController.getEtudiant();

		final VerticalLayout droite = new VerticalLayout();
		droite.setPadding(false);
		droite.setSpacing(false);
		droite.addClassName("card-split-right");

		if (resultats == null || resultats.isEmpty()) {
			/* Aucun résultat à afficher : message en italique à la place des sessions */
			final Span aucunResultat = new Span(getTranslation(NAME + ".message.aucuneresultat"));
			aucunResultat.getStyle().set("font-style", "italic").set("text-align", "center").set("width", "100%");
			droite.add(aucunResultat);
		} else {
			final HorizontalLayout sessions = new HorizontalLayout();
			sessions.setPadding(false);
			sessions.setSpacing(true);
			sessions.setAlignItems(FlexComponent.Alignment.START);
			for (final Resultat r : resultats) {
				final VerticalLayout session = new VerticalLayout();
				session.setPadding(false);
				session.setSpacing(false);
				session.addClassName("card-session");
				/* Largeur au contenu : les sessions restent l'une à côté de l'autre */
				session.getStyle().set("width", "auto");
				final Span sessionLabel = new Span(r.getSession() != null ? r.getSession() : "");
				sessionLabel.addClassName("card-session-header");
				session.add(sessionLabel);
				session.add(new Span(formatNote(r)));
				session.add(new Span(r.getAdmission() != null ? r.getAdmission() : ""));
				sessions.add(session);
			}
			/* Une seule session : on la centre horizontalement dans la carte */
			if (resultats.size() == 1) {
				sessions.setWidthFull();
				sessions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			}
			droite.add(sessions);
		}

		/* Pied : rang puis mention, l'un en dessous de l'autre */
		final VerticalLayout footer = new VerticalLayout();
		footer.setPadding(false);
		footer.setSpacing(false);
		footer.addClassName("card-footer");
		if (etudiant != null && etudiant.isAfficherRang() && afficherRang && StringUtils.hasText(rang)) {
			footer.add(buildFooterLigne(rangLabel, rang));
		}
		if (configController.isAffMentionEtudiant() && resultats != null) {
			for (final Resultat r : resultats) {
				if (StringUtils.hasText(r.getCodMention())) {
					footer.add(buildFooterLigne(mentionLabel, r.getCodMention()));
				}
			}
		}
		if (footer.getComponentCount() > 0) {
			droite.add(footer);
		}
		return droite;
	}

	/**
	 * Construit une ligne du pied de carte, centrée horizontalement : le libellé
	 * occupe la zone gauche (aligné à droite) et la valeur la zone droite (alignée
	 * à gauche), de largeur égale, de sorte que la paire soit centrée.
	 */
	private HorizontalLayout buildFooterLigne(final String label, final String valeur) {
		final HorizontalLayout ligne = new HorizontalLayout();
		ligne.setWidthFull();
		ligne.setPadding(false);
		ligne.setSpacing(false);

		final Span labelSpan = new Span(label);
		labelSpan.getStyle().set("flex", "1 1 0").set("text-align", "right").set("margin-right", "0.3em");

		final Span valeurSpan = new Span(valeur);
		valeurSpan.getStyle().set("flex", "1 1 0").set("text-align", "left");

		ligne.add(labelSpan, valeurSpan);
		return ligne;
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

	/**
	 * Fournit la valeur à afficher pour un résultat.
	 */
	@FunctionalInterface
	private interface ValueProviderResultat {
		String getValue(Resultat r);
	}

}
