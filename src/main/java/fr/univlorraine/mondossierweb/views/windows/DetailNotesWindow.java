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
import com.vaadin.flow.component.grid.Grid;
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

		final Etudiant etudiant = sessionController.getEtudiant();

		/* Titre */
		setHeaderTitle(getTranslation(NAME + ".title"));

		/* Bouton de fermeture */
		if (!headerInitialized) {
			headerInitialized = true;
			Button btnFermer = new Button(VaadinIcon.CLOSE.create());
			btnFermer.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
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

			if (lelp != null && !lelp.isEmpty() && configController.isPdfNotesActive()) {
				DownloadFile df = noteController.exportPdfDetail(etape);
				if (df != null) {
					Button pdfButton = new Button(VaadinIcon.FILE_TEXT_O.create());
					pdfButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
					pdfButton.addClassName("button-icon");
					pdfButton.addClassName("red-button-icon");
					pdfButton.setTooltipText(getTranslation(NAME + ".btn.pdf.description"));
					Anchor pdfAnchor = new Anchor();
					pdfAnchor.setHref(DownloadHandler.fromInputStream(evt ->
						new DownloadResponse(df.getInputStream(), df.fileName(), df.mimeType(), df.getContentLength())));
					pdfAnchor.add(pdfButton);
					titleLayout.add(pdfAnchor);
				}
			}
			layout.add(titleLayout);

			ajouterPanelVoirCommeUnEtudiant(layout);

			Component detailContent;
			if (lelp != null && !lelp.isEmpty()) {
				Grid<ElementPedagogique> detailNotesGrid = new Grid<>();
				detailNotesGrid.setSizeFull();
				detailNotesGrid.setSelectionMode(Grid.SelectionMode.NONE);
				if (contientElpObtenusPrecedemment(lelp)) {
					detailNotesGrid.addComponentColumn(this::buildAnneeColumn)
						.setHeader(getTranslation(NAME + ".table.elp.annee")).setFlexGrow(1).setResizable(true);
				}
				detailNotesGrid.addComponentColumn(this::buildCodeElpColumn)
					.setHeader(getTranslation(NAME + ".table.elp.code")).setFlexGrow(1).setResizable(true);
				detailNotesGrid.addComponentColumn(this::buildLibelleElpColumn)
					.setHeader(getTranslation(NAME + ".table.elp.libelle")).setFlexGrow(4).setResizable(true);
				detailNotesGrid.addComponentColumn(el -> buildNoteColumn(el, el.getNote1(), el.getBareme1()))
					.setHeader(noteController.renommerSession1(lelp)
						? getTranslation(NAME + ".table.elp.notesession1bis")
						: getTranslation(NAME + ".table.elp.notesession1"))
					.setFlexGrow(1).setResizable(true);
				detailNotesGrid.addComponentColumn(el -> buildResultatColumn(el, el.getRes1()))
					.setHeader(getTranslation(NAME + ".table.elp.resultatsession1")).setFlexGrow(1).setResizable(true);

				if (noteController.afficherSession2(lelp)) {
					detailNotesGrid.addComponentColumn(el -> buildNoteColumn(el, el.getNote2(), el.getBareme2()))
						.setHeader(getTranslation(NAME + ".table.elp.notesession2")).setFlexGrow(1).setResizable(true);
					detailNotesGrid.addComponentColumn(el -> buildResultatColumn(el, el.getRes2()))
						.setHeader(getTranslation(NAME + ".table.elp.resultatsession2")).setFlexGrow(1).setResizable(true);
				}
				if (configController.isAffRangEtudiant() || resultatController.isAfficherRangElpEpr()) {
					detailNotesGrid.addComponentColumn(this::buildRangColumn)
						.setHeader(getTranslation(NAME + ".table.elp.rang")).setFlexGrow(1).setResizable(true);
				}
				if (configController.isAffECTSEtudiant()) {
					detailNotesGrid.addComponentColumn(this::buildEctsColumn)
						.setHeader(getTranslation(NAME + ".table.elp.ects"))
						.setFlexGrow(1)
						.setResizable(true)
						.setVisible(!configController.isMasqueECTSEtudiant());
				}
				if (configController.isAffMentionElpEtudiant()) {
					detailNotesGrid.addComponentColumn(this::buildMentionColumn)
						.setHeader(getTranslation(NAME + ".table.elp.mention")).setFlexGrow(1).setResizable(true);
				}
				detailNotesGrid.setItems(lelp);
				detailContent = detailNotesGrid;
			} else {
				setHeight("30%");
				Span labelAucunResultat = new Span(getTranslation(NAME + ".message.aucuneresultat"));
				labelAucunResultat.addClassName(LumoUtility.FontWeight.BOLD);
				detailContent = labelAucunResultat;
			}
			Div panelDetailNotes = createPanel(etape.getLibelle() + " - " + getTranslation(NAME + ".label.anneeuniv") + " " + etape.getAnnee(), detailContent);
			panelDetailNotes.setSizeFull();
			layout.add(panelDetailNotes);

			if (lelp != null && !lelp.isEmpty() && etudiant.isSignificationResultatsUtilisee()) {
				StringBuilder grilleSignficationResultats = new StringBuilder();
				@SuppressWarnings("unchecked")
				Map<String, String> significations = etudiant.getSignificationResultats();
				Set<String> ss = significations.keySet();
				for (String k : ss) {
					if (k != null && !k.equals("") && !k.equals(" ")) {
						grilleSignficationResultats.append("<b>").append(k).append("</b>&#160;:&#160;").append(significations.get(k));
						grilleSignficationResultats.append("&#160;&#160;&#160;");
					}
				}
				Div significationLayout = new Div(new Html("<div>" + grilleSignficationResultats + "</div>"));
				significationLayout.addClassName(LumoUtility.FontSize.SMALL);
				Div panelSignificationResultats = createPanel(getTranslation(NAME + ".info.significations.resultats"), significationLayout);
				panelSignificationResultats.addClassName("significationpanel");
				layout.add(panelSignificationResultats);
			}

			layout.setFlexGrow(1, panelDetailNotes);
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
			vueLayout.setSpacing(true);
			vueLayout.setAlignItems(FlexComponent.Alignment.CENTER);

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
			vueLayout.expand(vueLabelWrapper);

			panelVue = createPanel(null, vueLayout);
			layout.add(panelVue);
			panelVue.setVisible(false);
		}
	}

	private boolean voirCommeEnseignant() {
		return sessionController.isVueEnseignantNotesEtResultats();
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
	 */
	private Component buildLibelleElpColumn(ElementPedagogique el) {
		Span libLabel = new Span();
		if (StringUtils.hasText(el.getLibelle())) {
			libLabel.setText(el.getLibelle());
			applyElpStyle(libLabel, el);
			//indentation des libelles dans la liste
			if (el.getLevel() > 1) {
				libLabel.getStyle().set("padding-inline-start", ((el.getLevel() - 1) * 1.5) + "em");
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
		if (!(content instanceof Grid)) {
			body.getStyle().set("padding", "var(--lumo-space-s)");
		}
		panel.add(body);
		return panel;
	}

}
