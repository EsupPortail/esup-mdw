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
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.CalendrierController;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.security.MdwUserDetailsService;
import fr.univlorraine.mondossierweb.utils.DownloadFile;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

/**
 * Page du calendrier des épreuves
 */
@SuppressWarnings("serial")
@Route(value = CalendrierView.NAME, layout = MainLayout.class)
@Secured(MdwUserDetailsService.CONSULT_DOSSIER_AUTORISE)
public class CalendrierView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	public static final String NAME = "calendrierView";
	public static final String ROUTE = NAME;

	public static final String[] CAL_FIELDS_ORDER = {"datedeb", "heure", "duree", "batiment", "salle", "epreuve"};

	public static final String[] CAL_FIELDS_ORDER_AVEC_PLACE = {"datedeb", "heure", "duree", "batiment", "salle", "place", "epreuve"};

	public static final String[] CAL_FIELDS_ORDER_DETAIL = {"datedeb", "heure", "duree", "batiment", "salle", "epreuve", "codeepreuve", "libsession", "codeetape", "versionetape"};

	public static final String[] CAL_FIELDS_ORDER_AVEC_PLACE_DETAIL = {"datedeb", "heure", "duree", "batiment", "salle", "place", "epreuve", "codeepreuve", "libsession", "codeetape", "versionetape"};

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient CalendrierController calendrierController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient SessionController sessionController;

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
		if (etudiant != null
				&& ((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiant())
					|| (userController.isEnseignant() && configController.isAffCalendrierEpreuvesEnseignant())
					|| (userController.isGestionnaire() && configController.isAffCalendrierEpreuvesGestionnaire()))) {

			/* Style */
			setPadding(true);
			setSpacing(true);

			//Si on n'a pas déjà essayé de récupérer le calendrier
			if (!etudiant.isCalendrierRecupere()) {
				etudiantController.recupererCalendrierExamens();
			}

			/* Titre */
			HorizontalLayout titleLayout = new HorizontalLayout();
			titleLayout.setWidthFull();
			titleLayout.setAlignItems(FlexComponent.Alignment.CENTER);
			//Test si on a des épreuves
			if (etudiant.getCalendrier() != null && !etudiant.getCalendrier().isEmpty()) {
				DownloadFile df = calendrierController.exportPdf();
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

			/* Message d'info */
			String messageInfo = getTranslation(NAME + ".message.info");
			if (StringUtils.hasText(messageInfo) && !messageInfo.equals(NAME + ".message.info")) {
				Html infoLabel = new Html("<div>" + messageInfo + "</div>");
				Div infoWrapper = new Div(infoLabel);
				infoWrapper.addClassName(LumoUtility.FontSize.SMALL);
				globalLayout.add(createPanel(null, infoWrapper));
			}

			/* Le Calendrier */
			Component calendrierContent;
			// Si on a des données à afficher
			if (etudiant.getCalendrier() != null && !etudiant.getCalendrier().isEmpty()) {

				Grid<Examen> calendrierGrid = new Grid<>();
				calendrierGrid.setWidthFull();
				calendrierGrid.setAllRowsVisible(true);
				calendrierGrid.setSelectionMode(Grid.SelectionMode.NONE);
				calendrierGrid.setColumnReorderingAllowed(true);

				String[] colonnesToDisplay = CAL_FIELDS_ORDER;
				if (configController.isAffDetailExamen()) {
					colonnesToDisplay = CAL_FIELDS_ORDER_DETAIL;
				}
				if (configController.isAffNumPlaceExamen()) {
					colonnesToDisplay = CAL_FIELDS_ORDER_AVEC_PLACE;
					if (configController.isAffDetailExamen()) {
						colonnesToDisplay = CAL_FIELDS_ORDER_AVEC_PLACE_DETAIL;
					}
				}
				for (String fieldName : colonnesToDisplay) {
					if ("batiment".equals(fieldName)) {
						calendrierGrid.addComponentColumn(this::buildBatimentColumn)
							.setHeader(getTranslation(NAME + ".table.batiment"))
							.setFlexGrow(1).setResizable(true);
					} else if ("salle".equals(fieldName)) {
						calendrierGrid.addComponentColumn(this::buildSalleColumn)
							.setHeader(getTranslation(NAME + ".table.salle"))
							.setFlexGrow(1).setResizable(true);
					} else {
						calendrierGrid.addColumn(examen -> getExamenFieldValue(examen, fieldName))
							.setHeader(getTranslation(NAME + ".table." + fieldName))
							.setFlexGrow("epreuve".equals(fieldName) ? 3 : 1).setResizable(true);
					}
				}
				calendrierGrid.setItems(etudiant.getCalendrier());
				calendrierContent = calendrierGrid;
			} else {
				Span aucunExamen = new Span(getTranslation(NAME + ".examen.aucun"));
				aucunExamen.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.FontWeight.BOLD);
				calendrierContent = aucunExamen;
			}
			globalLayout.add(createPanel(getTranslation(NAME + ".calendrier.title"), calendrierContent));

			add(globalLayout);
		}
	}

	private String getExamenFieldValue(Examen examen, String fieldName) {
		switch (fieldName) {
			case "datedeb":
				return examen.getDatedeb() != null ? Utils.formatDateToString(examen.getDatedeb()) : "";
			case "heure":
				return examen.getHeure();
			case "duree":
				return examen.getDuree();
			case "place":
				return examen.getPlace();
			case "epreuve":
				return examen.getEpreuve();
			case "codeepreuve":
				return examen.getCodeepreuve();
			case "libsession":
				return examen.getLibsession();
			case "codeetape":
				return examen.getCodeetape();
			case "versionetape":
				return examen.getVersionetape();
			default:
				return "";
		}
	}

	/**
	 * Colonne bâtiment avec la localisation en info-bulle (ancienne BatimentColumnGenerator).
	 */
	private Component buildBatimentColumn(Examen ex) {
		Span libLabel = new Span(ex.getBatiment() != null ? ex.getBatiment() : "");
		if (StringUtils.hasText(ex.getLocalisation())) {
			libLabel.getElement().setAttribute("title", ex.getLocalisation());
		}
		return libLabel;
	}

	/**
	 * Colonne salle avec le libellé de la salle en info-bulle (ancienne SalleColumnGenerator).
	 */
	private Component buildSalleColumn(Examen ex) {
		Span libLabel = new Span(ex.getSalle() != null ? ex.getSalle() : "");
		if (StringUtils.hasText(ex.getLibsalle())) {
			libLabel.getElement().setAttribute("title", ex.getLibsalle());
		}
		return libLabel;
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
