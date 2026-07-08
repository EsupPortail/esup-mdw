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
import fr.univlorraine.mondossierweb.utils.DownloadFile;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.DetailNotesWindow;
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
		titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		H1 title = new H1(getTranslation(NAME + ".title"));
		titleLayout.add(title);
		titleLayout.expand(title);

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

			//Test si on a des diplomes ou des etapes
			if ((etudiant.getDiplomes() != null && !etudiant.getDiplomes().isEmpty())
					|| (etudiant.getEtapes() != null && !etudiant.getEtapes().isEmpty())) {
				DownloadFile df = noteController.exportPdfResume();
				if (df != null) {
					Button pdfButton = new Button(VaadinIcon.FILE_TEXT_O.create());
					pdfButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
					pdfButton.addClassName("button-big-icon");
					pdfButton.addClassName("red-button-icon");
					pdfButton.setTooltipText(getTranslation(NAME + ".btn.pdf.description"));
					Anchor pdfAnchor = new Anchor();
					pdfAnchor.setHref(DownloadHandler.fromInputStream(evt ->
						new DownloadResponse(df.getInputStream(), df.fileName(), df.mimeType(), df.getContentLength())));
					pdfAnchor.add(pdfButton);
					titleLayout.add(pdfAnchor);
				}
			}
			add(titleLayout);

			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setWidthFull();
			globalLayout.setPadding(false);
			globalLayout.setSpacing(true);

			// Ajouter bloc "Voir comme un étudiant" si nécessaire
			ajouterVoirCommeUnEtudiant(globalLayout);

			/* Les diplômes */
			Grid<Diplome> notesDiplomesGrid = new Grid<>();
			notesDiplomesGrid.setWidthFull();
			notesDiplomesGrid.setAllRowsVisible(true);
			notesDiplomesGrid.setSelectionMode(Grid.SelectionMode.NONE);
			notesDiplomesGrid.addColumn(Diplome::getAnnee)
				.setHeader(getTranslation(NAME + ".table.diplomes.annee")).setFlexGrow(1);
			notesDiplomesGrid.addColumn(Diplome::getCod_dip)
				.setHeader(getTranslation(NAME + ".table.diplomes.cod_dip")).setFlexGrow(1);
			notesDiplomesGrid.addColumn(Diplome::getLib_web_vdi)
				.setHeader(getTranslation(NAME + ".table.diplomes.lib_web_vdi")).setFlexGrow(3);
			notesDiplomesGrid.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), Resultat::getSession))
				.setHeader(getTranslation(NAME + ".table.diplomes.session")).setFlexGrow(1);
			notesDiplomesGrid.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), this::formatNote))
				.setHeader(getTranslation(NAME + ".table.diplomes.note")).setFlexGrow(1);
			notesDiplomesGrid.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), Resultat::getAdmission))
				.setHeader(getTranslation(NAME + ".table.diplomes.resultat")).setFlexGrow(1);
			if (configController.isAffMentionEtudiant()) {
				notesDiplomesGrid.addComponentColumn(diplome -> buildResultatsColumn(diplome.getResultats(), Resultat::getCodMention))
					.setHeader(getTranslation(NAME + ".table.diplomes.mention")).setFlexGrow(1);
			}
			if (etudiant.isAfficherRang()) {
				notesDiplomesGrid.addComponentColumn(diplome -> buildRangColumn(diplome.getRang(), diplome.isAfficherRang()))
					.setHeader(getTranslation(NAME + ".table.diplomes.rang")).setFlexGrow(1);
			}
			notesDiplomesGrid.setItems(etudiant.getDiplomes() != null ? etudiant.getDiplomes() : Collections.emptyList());
			globalLayout.add(createPanel(getTranslation(NAME + ".table.diplomes"), notesDiplomesGrid));

			/* Les étapes */
			Grid<Etape> notesEtapesGrid = new Grid<>();
			notesEtapesGrid.setWidthFull();
			notesEtapesGrid.setAllRowsVisible(true);
			notesEtapesGrid.setSelectionMode(Grid.SelectionMode.NONE);
			notesEtapesGrid.addColumn(Etape::getAnnee)
				.setHeader(getTranslation(NAME + ".table.etapes.annee")).setFlexGrow(1);
			notesEtapesGrid.addComponentColumn(this::buildCodeEtapeColumn)
				.setHeader(getTranslation(NAME + ".table.etapes.codevers")).setFlexGrow(1);
			notesEtapesGrid.addComponentColumn(this::buildLibelleEtapeColumn)
				.setHeader(getTranslation(NAME + ".table.etapes.libelle")).setFlexGrow(3);
			notesEtapesGrid.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), Resultat::getSession))
				.setHeader(getTranslation(NAME + ".table.etapes.session")).setFlexGrow(1);
			notesEtapesGrid.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), this::formatNote))
				.setHeader(getTranslation(NAME + ".table.etapes.note")).setFlexGrow(1);
			notesEtapesGrid.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), Resultat::getAdmission))
				.setHeader(getTranslation(NAME + ".table.etapes.resultat")).setFlexGrow(1);
			if (configController.isAffMentionEtudiant()) {
				notesEtapesGrid.addComponentColumn(etape -> buildResultatsColumn(etape.getResultats(), Resultat::getCodMention))
					.setHeader(getTranslation(NAME + ".table.etapes.mention")).setFlexGrow(1);
			}
			if (etudiant.isAfficherRang()) {
				notesEtapesGrid.addComponentColumn(etape -> buildRangColumn(etape.getRang(), etape.isAfficherRang()))
					.setHeader(getTranslation(NAME + ".table.etapes.rang")).setFlexGrow(1);
			}
			notesEtapesGrid.setItems(etudiant.getEtapes() != null ? etudiant.getEtapes() : Collections.emptyList());
			globalLayout.add(createPanel(getTranslation(NAME + ".table.etapes"), notesEtapesGrid));

			/* Signification des résultats */
			if (etudiant.isSignificationResultatsUtilisee()) {
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
				globalLayout.add(panelSignificationResultats);
			}
			add(globalLayout);
		} else {
			add(titleLayout);

			// Ajouter bloc "Voir comme un étudiant" si nécessaire
			ajouterVoirCommeUnEtudiant(this);

			//Si on est dans le cas d'un blocage apogée
			if (etudiant != null
					&& (userController.isEtudiant() || ((isUserEnseignantWithAccess() || isUserGestionnaireWithAccess()) && !voirCommeEnseignant()))
					&& etudiant.isNonAutoriseConsultationNotes()) {
				//message non autorisé.
				add(new Span(getTranslation(NAME + ".blocage.msg")));
			}
		}
	}

	private void ajouterVoirCommeUnEtudiant(com.vaadin.flow.component.HasComponents globalLayout) {
		//Test si user enseignant
		if (isUserGestionnaireWithAccess() || isUserEnseignantWithAccess()) {
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
				init();
			});

			Html vueLabel = new Html("<div>"
				+ getTranslation(voirCommeEnseignant() ? NAME + ".label.vueEnseignant" : NAME + ".label.vueEtudiant")
				+ "</div>");
			Div vueLabelWrapper = new Div(vueLabel);
			vueLabelWrapper.addClassName(LumoUtility.FontSize.SMALL);

			vueLayout.add(changerVueButton);
			vueLayout.add(vueLabelWrapper);
			vueLayout.expand(vueLabelWrapper);

			globalLayout.add(createPanel(null, vueLayout));
		}
	}

	private boolean voirCommeEnseignant() {
		return sessionController.isVueEnseignantNotesEtResultats();
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
		return buildEtapeLinkColumn(etape, etape.getCode() + "/" + etape.getVersion());
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
			//Appel de la window contenant le détail des notes
			prepareBoutonAppelDetailDesNotes(b, etape);
			return b;
		}
		return new Span(libelle);
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

	/**
	 * Fournit la valeur à afficher pour un résultat.
	 */
	@FunctionalInterface
	private interface ValueProviderResultat {
		String getValue(Resultat r);
	}

}
