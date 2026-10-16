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
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.theme.lumo.LumoUtility;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.NoteController;
import fr.univlorraine.mondossierweb.controllers.ResultatController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList;
import fr.univlorraine.mondossierweb.utils.DownloadFile;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fenêtre du détail des notes
 */
@SuppressWarnings("serial")
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetailNotesWindow extends Dialog {

	public static final String NAME = "notesWindow";

	/** Indentation (en em) par niveau du libellé dans la grid desktop. */
	private static final double INDENT_LIBELLE_GRID = 1.5;
	/** Indentation (en em) par niveau du libellé dans la carte mobile. */
	private static final double INDENT_LIBELLE_CARD = 0.5;

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

	private Etape etape;
	private Button btnDisplayFiltres;
	private Div panelVue;
	private boolean headerInitialized;

	public void init(Etape et) {
		etape = et;
		buildContent();
	}

	private void buildContent() {
		removeAll();
		addClassName("detail-notes-window");

		final Etudiant etudiant = sessionController.getEtudiant();

		/* Titre */
		setHeaderTitle(getTranslation(NAME + ".title"));

		/* Bouton de fermeture */
		if (!headerInitialized) {
			headerInitialized = true;
			Button btnFermer = new Button(VaadinIcon.CLOSE.create());
			btnFermer.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			btnFermer.addClassName("btn-fermer-header");
			btnFermer.getElement().setAttribute("aria-label", getTranslation(NAME + ".btnFermer"));
			btnFermer.addClickListener(e -> close());
			getHeader().add(btnFermer);
		}

		//On vérifie le droit d'accéder à la vue
		if (etudiant != null
				&& (((userController.isEtudiant() || ((isUserEnseignantWithAccess() || isUserGestionnaireWithAccess()) && !voirCommeEnseignant()))
						&& configController.isAffNotesEtudiant() && !etudiant.isNonAutoriseConsultationNotes())
					|| (isUserEnseignantWithAccess() && voirCommeEnseignant())
					|| (isUserGestionnaireWithAccess() && voirCommeEnseignant()))) {

			/* Style */
			setWidth("80%");
			setHeight("95%");
			setResizable(false);
			setCloseOnOutsideClick(true);

			//Test si user enseignant et en vue Enseignant
			if (userController.isEnseignant() && voirCommeEnseignant()) {
				//On recupere les notes pour un enseignant
				resultatController.renseigneDetailNotesEtResultats(etape, userController.isGestionnaire());
			} else {
				//On récupère les notes pour un étudiant
				resultatController.renseigneDetailNotesEtResultatsEtudiant(etape);
			}

			/* Layout */
			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setPadding(false);
			layout.setSpacing(true);

			List<ElementPedagogique> lelp = etudiant.getElementsPedagogiques();

			//Sous titre avec le message d'information
			HorizontalLayout titleLayout = new HorizontalLayout();
			titleLayout.setWidthFull();
			titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
			Html messageLabel = new Html("<div>" + getTranslation(NAME + ".label.messageinfo") + "</div>");
			Div messageWrapper = new Div(messageLabel);
			messageWrapper.addClassName(LumoUtility.FontSize.SMALL);
			titleLayout.add(messageWrapper);
			titleLayout.expand(messageWrapper);

			ajouterVoirCommeUnEtudiant(titleLayout);

			// L'encart « Voir comme un étudiant » est affiché pour les enseignants
			// (même condition que son ajout par ajouterPanelVoirCommeUnEtudiant) :
			// dans ce cas les boutons PDF et informations sont empilés verticalement
			// en icône seule, sinon ils sont côte à côte avec leur libellé texte.
			final boolean encartVueAffiche = userController.isEnseignant();

			Anchor pdfAnchor = null;
			if (lelp != null && !lelp.isEmpty() && configController.isPdfNotesActive()) {
				DownloadFile df = noteController.exportPdfDetail(etape);
				if (df != null) {
					Button pdfButton = new Button(VaadinIcon.FILE_TEXT_O.create());
					pdfButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
					pdfButton.addClassName("button-icon");
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
			if (lelp != null && !lelp.isEmpty() && etudiant.isSignificationResultatsUtilisee()) {
				infoButton = new Button(VaadinIcon.INFO_CIRCLE.create());
				infoButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				infoButton.addClassName("button-icon");
				infoButton.setTooltipText(getTranslation(NAME + ".info.significations.resultats"));
				infoButton.getElement().setAttribute("aria-label", getTranslation(NAME + ".info.significations.resultats"));
				infoButton.addClickListener(e -> new HelpBasicWindow(buildSignificationResultatsHtml(etudiant),
						getTranslation(NAME + ".info.significations.resultats")).open());
				if (!encartVueAffiche) {
					// Pas d'encart : le bouton affiche son libellé texte à côté de l'icône
					infoButton.setText(getTranslation(NAME + ".btn.infos.label"));
				}
			}

			// Les boutons PDF et informations sont regroupés dans un même conteneur,
			// au même niveau que l'encart « Voir comme un enseignant ».
			if (pdfAnchor != null || infoButton != null) {
				titleLayout.add(buildConteneurBoutons(encartVueAffiche, pdfAnchor, infoButton));
			}
			layout.add(titleLayout);

			ajouterPanelVoirCommeUnEtudiant(layout);

			Component detailContent;
			if (lelp != null && !lelp.isEmpty()) {
				final String headerSession1 = noteController.renommerSession1(lelp)
					? getTranslation(NAME + ".table.elp.notesession1bis")
					: getTranslation(NAME + ".table.elp.notesession1");
				final String headerSession2 = getTranslation(NAME + ".table.elp.notesession2");
				final boolean afficherSession2 = noteController.afficherSession2(lelp);

				ResponsiveCardList<ElementPedagogique> detailNotesList = new ResponsiveCardList<>();
				detailNotesList.setSizeFull();
				if (contientElpObtenusPrecedemment(lelp)) {
					detailNotesList.addComponentColumn(this::buildAnneeColumn,
							getTranslation(NAME + ".table.elp.annee")).setFlexGrow(1).setResizable(true);
				}
				detailNotesList.addComponentColumn(this::buildCodeElpColumn,
						getTranslation(NAME + ".table.elp.code")).setFlexGrow(1).setResizable(true);
				detailNotesList.addComponentColumn(el -> buildLibelleElpColumn(el, INDENT_LIBELLE_GRID),
						getTranslation(NAME + ".table.elp.libelle")).setFlexGrow(4).setResizable(true);
				detailNotesList.addComponentColumn(el -> buildNoteColumn(el, el.getNote1(), el.getBareme1()),
						headerSession1).setFlexGrow(1).setResizable(true);
				detailNotesList.addComponentColumn(el -> buildResultatColumn(el, el.getRes1()),
						getTranslation(NAME + ".table.elp.resultatsession1")).setFlexGrow(1).setResizable(true);

				if (afficherSession2) {
					detailNotesList.addComponentColumn(el -> buildNoteColumn(el, el.getNote2(), el.getBareme2()),
							headerSession2).setFlexGrow(1).setResizable(true);
					detailNotesList.addComponentColumn(el -> buildResultatColumn(el, el.getRes2()),
							getTranslation(NAME + ".table.elp.resultatsession2")).setFlexGrow(1).setResizable(true);
				}
				if (configController.isAffRangEtudiant() || resultatController.isAfficherRangElpEpr()) {
					detailNotesList.addComponentColumn(this::buildRangColumn,
							getTranslation(NAME + ".table.elp.rang")).setFlexGrow(1).setResizable(true);
				}
				if (configController.isAffECTSEtudiant()) {
					detailNotesList.addComponentColumn(this::buildEctsColumn,
							getTranslation(NAME + ".table.elp.ects"))
						.setFlexGrow(1)
						.setResizable(true)
						.setVisible(!configController.isMasqueECTSEtudiant());
				}
				if (configController.isAffMentionElpEtudiant()) {
					detailNotesList.addComponentColumn(this::buildMentionColumn,
							getTranslation(NAME + ".table.elp.mention")).setFlexGrow(1).setResizable(true);
				}
				/* Cartes affichées sur petit écran */
				detailNotesList.setCardRenderer(el -> createElpCard(el, headerSession1, headerSession2, afficherSession2));
				detailNotesList.setItems(lelp);
				detailContent = detailNotesList;
			} else {
				setHeight("30%");
				Span labelAucunResultat = new Span(getTranslation(NAME + ".message.aucuneresultat"));
				labelAucunResultat.addClassName(LumoUtility.FontWeight.BOLD);
				detailContent = labelAucunResultat;
			}
			Div panelDetailNotes = createPanel(etape.getLibelle() + " - " + getTranslation(NAME + ".label.anneeuniv") + " " + etape.getAnnee(), detailContent);
			panelDetailNotes.setSizeFull();
			layout.add(panelDetailNotes);

			layout.setFlexGrow(1, panelDetailNotes);

			/* Bouton de fermeture, aligné en bas (affiché sur mobile) */
			final Button btnFermerMobile = new Button(getTranslation(NAME + ".btnFermer"), e -> close());
			btnFermerMobile.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			btnFermerMobile.addClassName("btn-fermer-mobile");
			final HorizontalLayout boutonsLayout = new HorizontalLayout(btnFermerMobile);
			boutonsLayout.setWidthFull();
			boutonsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			layout.add(boutonsLayout);

			add(layout);

		} else {
			// Acces non autorise
			VerticalLayout layout = new VerticalLayout();
			layout.setPadding(false);

			HorizontalLayout titleLayout = new HorizontalLayout();
			titleLayout.setWidthFull();
			// Ajouter bloc "Voir comme un étudiant" si nécessaire
			ajouterVoirCommeUnEtudiant(titleLayout);
			ajouterPanelVoirCommeUnEtudiant(layout);
			layout.add(titleLayout);

			//Si on est dans le cas d'un blocage apogée
			if (etudiant != null
					&& (userController.isEtudiant() || ((isUserEnseignantWithAccess() || isUserGestionnaireWithAccess()) && !voirCommeEnseignant()))
					&& etudiant.isNonAutoriseConsultationNotes()) {
				//message non autorisé.
				layout.add(new Span(getTranslation(NAME + ".blocage.msg")));
			}
			add(layout);
		}
	}

	private void ajouterPanelVoirCommeUnEtudiant(VerticalLayout layout) {
		//Test si user enseignant
		if (userController.isEnseignant()) {
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
				buildContent();
			});

			Html vueLabel = new Html("<div>"
				+ getTranslation(voirCommeEnseignant() ? NAME + ".label.vueEnseignant" : NAME + ".label.vueEtudiant")
				+ "</div>");
			Div vueLabelWrapper = new Div(vueLabel);
			vueLabelWrapper.addClassName(LumoUtility.FontSize.SMALL);

			vueLayout.add(changerVueButton);
			vueLayout.add(vueLabelWrapper);

			panelVue = createPanel(null, vueLayout);
			layout.add(panelVue);
			panelVue.setVisible(false);
		}
	}

	/**
	 * Regroupe les boutons PDF et informations dans un conteneur placé à droite de
	 * l'entête. Si l'encart « Voir comme un étudiant » est affiché, les boutons sont
	 * empilés verticalement en icône seule ; sinon ils sont côte à côte avec leur
	 * libellé texte.
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

	private void ajouterVoirCommeUnEtudiant(HorizontalLayout titleLayout) {
		//Test si user enseignant
		if (userController.isEnseignant()) {
			//Bouton pour afficher les filtres
			btnDisplayFiltres = new Button(VaadinIcon.FILTER.create());
			btnDisplayFiltres.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			if (voirCommeEnseignant()) {
				btnDisplayFiltres.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
				btnDisplayFiltres.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			}
			btnDisplayFiltres.setTooltipText(getTranslation(NAME + ".btn.displayFilters"));
			btnDisplayFiltres.addClickListener(e -> {
				btnDisplayFiltres.setVisible(false);
				panelVue.setVisible(true);
			});
			titleLayout.add(btnDisplayFiltres);
			btnDisplayFiltres.setVisible(true);
		}
	}

	private boolean isUserGestionnaireWithAccess() {
		return userController.isGestionnaire() && configController.isAffNotesGestionnaire();
	}

	private boolean isUserEnseignantWithAccess() {
		return userController.isEnseignant() && configController.isAffNotesEnseignant();
	}

	private boolean contientElpObtenusPrecedemment(List<ElementPedagogique> lelp) {
		for (ElementPedagogique el : lelp) {
			if (el.getLevel() > 1 && !el.isEpreuve() && StringUtils.hasText(el.getAnnee())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Colonne année (ancienne AnneeColumnGenerator).
	 */
	private Component buildAnneeColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if ((el.getLevel() == 1 || !el.isEpreuve()) && StringUtils.hasText(el.getAnnee())) {
			libLabel.setText(Utils.convertAnneeUnivToDisplay(el.getAnnee()));
			applyElpStyle(libLabel, el);
		}
		return libLabel;
	}

	/**
	 * Colonne code de l'élément pédagogique (ancienne CodeElpColumnGenerator).
	 */
	private Component buildCodeElpColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getLibelle())) {
			libLabel.setText(el.getCode());
			applyElpStyle(libLabel, el);
		}
		return libLabel;
	}

	/**
	 * Colonne libellé de l'élément pédagogique, indenté selon le niveau
	 * (ancienne LibelleElpColumnGenerator).
	 *
	 * @param indentEm indentation en em par niveau (différente entre la grid
	 *                 desktop et la carte mobile)
	 */
	private Component buildLibelleElpColumn(ElementPedagogique el, double indentEm) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getLibelle())) {
			libLabel.setText(el.getLibelle());
			applyElpStyle(libLabel, el);
			//indentation des libelles selon le niveau de l'élément
			if (el.getLevel() > 1) {
				libLabel.getStyle().set("padding-inline-start", ((el.getLevel() - 1) * indentEm) + "em");
			}
		}
		return libLabel;
	}

	/**
	 * Colonne note de session avec barème éventuel
	 * (anciennes Session1ColumnGenerator/Session2ColumnGenerator).
	 */
	private Component buildNoteColumn(ElementPedagogique el, String note, int bareme) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getLibelle())) {
			String value = note;
			if (bareme != 0 && (configController.isToujoursAfficherBareme() || bareme != 20)) {
				value += "/" + bareme;
			}
			libLabel.setText(value != null ? value : "");
			applyElpStyle(libLabel, el);
		}
		return libLabel;
	}

	/**
	 * Colonne résultat de session
	 * (anciennes ResultatSession1ColumnGenerator/ResultatSession2ColumnGenerator).
	 */
	private Component buildResultatColumn(ElementPedagogique el, String res) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getLibelle()) && res != null) {
			libLabel.setText(res);
			applyElpStyle(libLabel, el);
		}
		return libLabel;
	}

	/**
	 * Colonne du rang (ancienne RangColumnGenerator).
	 */
	private Component buildRangColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getRang())) {
			libLabel.setText(el.getRang());
		}
		return libLabel;
	}

	/**
	 * Colonne ECTS (ancienne ECTSColumnGenerator).
	 */
	private Component buildEctsColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getEcts())) {
			libLabel.setText(el.getEcts());
		}
		return libLabel;
	}

	/**
	 * Colonne mention (ancienne MentionColumnGenerator).
	 */
	private Component buildMentionColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getCodMention())) {
			libLabel.setText(el.getCodMention());
		}
		return libLabel;
	}

	/**
	 * Style du contenu d'une cellule : gras pour les éléments de niveau 1, italique pour les épreuves.
	 */
	private void applyElpStyle(Span span, ElementPedagogique el) {
		if (el.getLevel() == 1 && !el.isEpreuve()) {
			span.addClassName(LumoUtility.FontWeight.BOLD);
		}
		if (el.isEpreuve()) {
			span.getStyle().set("font-style", "italic");
		}
	}

	/**
	 * Construit la carte d'un élément pédagogique, affichée sur petit écran :
	 * à gauche le libellé ; à droite une colonne par session (libellé, note,
	 * résultat) puis, en pied, le rang, l'ECTS et la mention.
	 */
	private Div createElpCard(final ElementPedagogique el, final String headerSession1,
			final String headerSession2, final boolean afficherSession2) {
		final Div card = new Div();
		card.addClassName("card");

		final HorizontalLayout split = new HorizontalLayout();
		split.setWidthFull();
		split.setSpacing(true);
		split.setPadding(false);
		split.setAlignItems(FlexComponent.Alignment.START);
		split.addClassName("card-split");

		/* Colonne gauche : libellé */
		final VerticalLayout gauche = new VerticalLayout();
		gauche.setPadding(false);
		gauche.setSpacing(false);
		gauche.setAlignItems(FlexComponent.Alignment.STRETCH);
		gauche.addClassName("card-split-left");
		gauche.add(buildLibelleElpColumn(el, INDENT_LIBELLE_CARD));

		/* Colonne droite : sessions puis pied (rang, ECTS, mention) */
		final VerticalLayout droite = new VerticalLayout();
		droite.setPadding(false);
		droite.setSpacing(false);
		droite.addClassName("card-split-right");

		/* Aucun résultat : ni note ni résultat pour toutes les sessions affichées */
		final boolean aucunResultat = !StringUtils.hasText(el.getNote1()) && !StringUtils.hasText(el.getRes1())
			&& (!afficherSession2 || (!StringUtils.hasText(el.getNote2()) && !StringUtils.hasText(el.getRes2())));

		if (aucunResultat) {
			/* Aucun résultat : message en italique à la place des sessions */
			final Span labelAucunResultat = new Span(getTranslation(NAME + ".message.aucuneresultat"));
			labelAucunResultat.getStyle().set("font-style", "italic").set("text-align", "center").set("width", "100%");
			droite.add(labelAucunResultat);
		} else {
			final HorizontalLayout sessions = new HorizontalLayout();
			sessions.setPadding(false);
			sessions.setSpacing(true);
			sessions.setAlignItems(FlexComponent.Alignment.START);
			/* Session 1 : affichée seulement si elle porte une note ou un résultat */
			if (StringUtils.hasText(el.getNote1()) || StringUtils.hasText(el.getRes1())) {
				sessions.add(buildSessionCarte(headerSession1, el.getNote1(), el.getBareme1(), el.getRes1()));
			}
			/* Session 2 : affichée seulement si activée et porteuse d'une note ou d'un résultat */
			if (afficherSession2 && (StringUtils.hasText(el.getNote2()) || StringUtils.hasText(el.getRes2()))) {
				sessions.add(buildSessionCarte(headerSession2, el.getNote2(), el.getBareme2(), el.getRes2()));
			}
			/* Une seule session affichée : on la centre horizontalement */
			if (sessions.getComponentCount() == 1) {
				sessions.setWidthFull();
				sessions.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
			}
			droite.add(sessions);
		}

		/* Pied : rang, ECTS puis mention, l'un en dessous de l'autre */
		final VerticalLayout footer = new VerticalLayout();
		footer.setPadding(false);
		footer.setSpacing(false);
		footer.addClassName("card-footer");
		if ((configController.isAffRangEtudiant() || resultatController.isAfficherRangElpEpr()) && StringUtils.hasText(el.getRang())) {
			footer.add(buildFooterLigne(getTranslation(NAME + ".table.elp.rang"), el.getRang()));
		}
		if (configController.isAffECTSEtudiant() && !configController.isMasqueECTSEtudiant() && StringUtils.hasText(el.getEcts())) {
			footer.add(buildFooterLigne(getTranslation(NAME + ".table.elp.ects"), el.getEcts()));
		}
		if (configController.isAffMentionElpEtudiant() && StringUtils.hasText(el.getCodMention())) {
			footer.add(buildFooterLigne(getTranslation(NAME + ".table.elp.mention"), el.getCodMention()));
		}
		if (footer.getComponentCount() > 0) {
			droite.add(footer);
		}

		split.add(gauche, droite);
		split.expand(droite);
		card.add(split);
		return card;
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
	 * Construit la colonne d'une session dans une carte mobile : libellé de la
	 * session, note (avec barème éventuel) et résultat.
	 */
	private VerticalLayout buildSessionCarte(final String header, final String note, final int bareme, final String resultat) {
		final VerticalLayout session = new VerticalLayout();
		session.setPadding(false);
		session.setSpacing(false);
		session.addClassName("card-session");
		/* Largeur au contenu : les sessions restent l'une à côté de l'autre */
		session.getStyle().set("width", "auto");
		final Span sessionLabel = new Span(header != null ? header : "");
		sessionLabel.addClassName("card-session-header");
		session.add(sessionLabel);
		String valeurNote = note;
		if (StringUtils.hasText(note) && bareme != 0 && (configController.isToujoursAfficherBareme() || bareme != 20)) {
			valeurNote += "/" + bareme;
		}
		session.add(new Span(valeurNote != null ? valeurNote : ""));
		session.add(new Span(resultat != null ? resultat : ""));
		return session;
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
			.set("overflow", "hidden")
			.set("display", "flex")
			.set("flex-direction", "column");
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
		body.getStyle()
			.set("flex", "1 1 auto")
			.set("overflow", "auto");
		if (!(content instanceof ResponsiveCardList<?>)) {
			body.getStyle().set("padding", "var(--lumo-space-s)");
		}
		panel.add(body);
		return panel;
	}

}
