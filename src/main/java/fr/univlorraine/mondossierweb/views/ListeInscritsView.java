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

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit.Vet;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.DetailGroupesWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpBasicWindow;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Liste des inscrits à une étape ou un ELP
 */
@SuppressWarnings("serial")
@Route(value = ListeInscritsView.NAME, layout = MainLayout.class)
@Secured({Utils.TEACHER_USER, Utils.GEST_USER, Utils.ADMIN_USER})
@Slf4j
public class ListeInscritsView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

	public static final String NAME = "listeInscritsView";
	public static final String ROUTE = NAME;

	public static final String TOUTES_LES_ETAPES_LABEL = "toutes";

	public static final String TOUS_LES_GROUPES_LABEL = "tous";

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient ListeInscritsController listeInscritsController;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient SessionController sessionController;
	@Resource
	private transient ObjectFactory<DetailGroupesWindow> detailGroupesWindowFactory;

	//la liste des inscrits
	private List<Inscrit> linscrits;

	private Button btnTrombi;
	private Button btnRetourListe;
	private Grid<Inscrit> inscritstable;
	private Grid.Column<Inscrit> colEtape;
	private Grid.Column<Inscrit> colNotes1;
	private Grid.Column<Inscrit> colNotes2;
	private Grid.Column<Inscrit> colGroupe;
	private FlexLayout trombiLayout;
	private Button btnAjoutFavori;
	private String code;
	private String typeFavori;
	private VerticalLayout dataLayout;
	private boolean afficherTrombinoscope;
	private ComboBox<String> listeAnnees;
	private ComboBox<String> listeEtapes;
	private ComboBox<String> listeGroupes;
	/** libellés des groupes (clé de groupe → libellé) */
	private Map<String, String> libellesGroupes;
	//liste contenant tous les codind à afficher (apres application du filtre)
	private List<String> listecodind;
	private Span infoNbInscrit;
	private String libelleObj;
	private Div panelFormInscrits;
	private HorizontalLayout panelLayout;
	private Span panelTitre;
	private HorizontalLayout leftResumeLayout;
	private HorizontalLayout middleResumeLayout;
	private Button btnDisplayFiltres;
	private Anchor exportTrombiAnchor;
	private Anchor exportExcelAnchor;
	private Checkbox collapseEtp;
	private Checkbox collapseResultatsS1;
	private Checkbox collapseResultatsS2;
	private Checkbox collapseGrp;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		log.info(NAME + ".beforeEnter");
		if (!configController.isApplicationActive() || !configController.isPartieEnseignantActive()) {
			event.forwardTo(AccesBloqueView.class);
			return;
		}

		/* Récupération de la liste des inscrits selon les paramètres d'URL
		   (ancien MainUI.goTo + navigateToListeInscrits) — permet aussi l'accès direct par URL */
		final Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
		final String codeParam = params.containsKey("code") ? params.get("code").get(0) : null;
		final String typeParam = params.containsKey("type") ? params.get("type").get(0) : null;
		final String anneeParam = params.containsKey("annee") ? params.get("annee").get(0) : null;
		if (StringUtils.hasText(codeParam) && StringUtils.hasText(typeParam) && userController.isEnseignant()) {
			final Map<String, String> parameterMap = new HashMap<>();
			parameterMap.put("code", codeParam);
			parameterMap.put("type", typeParam);
			if (anneeParam != null) {
				parameterMap.put("annee", anneeParam);
			}
			//Récupération de la liste des inscrits (gère VET et ELP) et alimentation de l'état de session.
			//Second argument null comme l'ancien MainUI : l'année d'URL est lue via la map (anneeParam),
			//l'argument non-null est réservé au changement d'année depuis la ComboBox (ELP déjà en session)
			listeInscritsController.recupererLaListeDesInscrits(parameterMap, null);
		}

		initListe();
	}

	public void initListe() {
		log.info(NAME + ".initListe");
		//On vérifie le droit d'accéder à la vue
		if (!userController.isEnseignant()) {
			return;
		}
		// initialisation de la vue
		removeAll();
		listeEtapes = null;
		listeGroupes = null;
		libellesGroupes = new LinkedHashMap<>();
		afficherTrombinoscope = false;

		/* Style */
		setPadding(true);
		setSpacing(true);
		setSizeFull();

		// Récupération de l'objet de la SE dont on doit afficher les inscrits
		code = sessionController.getCodeObjListInscrits();
		typeFavori = sessionController.getTypeObjListInscrits();
		libelleObj = "";
		if (typeIsVet() && sessionController.getEtapeListeInscrits() != null) {
			libelleObj = sessionController.getEtapeListeInscrits().getLibelle();
		}
		if (typeIsElp() && sessionController.getElpListeInscrits() != null) {
			libelleObj = sessionController.getElpListeInscrits().getLibelle();
		}

		// Si l'objet est renseigné
		if (code == null || typeFavori == null) {
			return;
		}

		//Panel contenant les filtres d'affichage et le bouton de mise en favori
		panelLayout = new HorizontalLayout();
		panelLayout.setWidthFull();
		panelLayout.addClassName("small-font-element");

		// Layout contenant les filtres
		final FormLayout formInscritLayout = new FormLayout();
		formInscritLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

		panelTitre = new Span(code + " " + libelleObj);
		panelTitre.addClassName("font-bold");
		panelFormInscrits = new Div();
		panelFormInscrits.setWidthFull();

		//Affichage d'une liste déroulante contenant la liste des années
		final List<String> lannees = sessionController.getListeAnneeInscrits();
		if (lannees != null && !lannees.isEmpty()) {
			listeAnnees = new ComboBox<>(getTranslation(NAME + ".annee"));
			listeAnnees.setAllowCustomValue(false);
			listeAnnees.setWidth("150px");
			listeAnnees.setItems(lannees);
			listeAnnees.setItemLabelGenerator(annee -> annee + "/" + (Integer.parseInt(annee) + 1));
			listeAnnees.setValue(sessionController.getAnneeInscrits());

			//Gestion de l'événement sur le changement d'année
			listeAnnees.addValueChangeListener(event -> {
				if (event.isFromClient()) {
					final String selectedValue = event.getValue();
					final Map<String, String> parameterMap = new HashMap<>();
					parameterMap.put("code", code);
					parameterMap.put("type", typeFavori);

					//récupération de la nouvelle liste
					if (typeIsVet()) {
						listeInscritsController.recupererLaListeDesInscrits(parameterMap, selectedValue);
					}
					if (typeIsElp()) {
						listeInscritsController.recupererLaListeDesInscritsELP(parameterMap, selectedValue);
					}

					//update de l'affichage
					initListe();
				}
			});
			formInscritLayout.add(listeAnnees);
		}

		//Si on affiche la liste des inscrits à un ELP,
		//on doit afficher l'étape d'appartenance et éventuellement les groupes
		if (typeIsElp()) {
			final List<VersionEtape> letapes = sessionController.getListeEtapesInscrits();
			if (letapes != null && !letapes.isEmpty()) {
				listeEtapes = new ComboBox<>(getTranslation(NAME + ".etapes"));
				listeEtapes.setAllowCustomValue(false);
				listeEtapes.setWidth("400px");
				final Map<String, String> libellesEtapes = new LinkedHashMap<>();
				libellesEtapes.put(TOUTES_LES_ETAPES_LABEL, TOUTES_LES_ETAPES_LABEL);
				for (final VersionEtape etape : letapes) {
					final String idEtape = etape.getId().getCod_etp() + "/" + etape.getId().getCod_vrs_vet();
					libellesEtapes.put(idEtape, "[" + idEtape + "] " + etape.getLib_web_vet());
				}
				listeEtapes.setItems(libellesEtapes.keySet());
				listeEtapes.setItemLabelGenerator(libellesEtapes::get);
				listeEtapes.setValue(sessionController.getEtapeInscrits() != null ? sessionController.getEtapeInscrits() : TOUTES_LES_ETAPES_LABEL);

				//Gestion de l'événement sur le changement d'étape
				listeEtapes.addValueChangeListener(event -> {
					if (event.isFromClient()) {
						String vetSelectionnee = event.getValue();
						if (TOUTES_LES_ETAPES_LABEL.equals(vetSelectionnee)) {
							vetSelectionnee = null;
						}
						sessionController.setEtapeInscrits(vetSelectionnee);
						//faire le changement
						filtrerInscrits(vetSelectionnee, getGroupeSelectionne());
					}
				});
				formInscritLayout.add(listeEtapes);
			}

			final List<ElpDeCollection> lgroupes = sessionController.getListeGroupesInscrits();
			if (lgroupes != null && !lgroupes.isEmpty()) {
				listeGroupes = new ComboBox<>();
				listeGroupes.setAllowCustomValue(false);
				listeGroupes.setWidth("348px");
				libellesGroupes.put(TOUS_LES_GROUPES_LABEL, TOUS_LES_GROUPES_LABEL);
				for (final ElpDeCollection edc : lgroupes) {
					for (final CollectionDeGroupes cdg : edc.getListeCollection()) {
						for (final Groupe groupe : cdg.getListeGroupes()) {
							libellesGroupes.put(groupe.getCleGroupe(), groupe.getLibGroupe());
						}
					}
				}
				listeGroupes.setItems(libellesGroupes.keySet());
				listeGroupes.setItemLabelGenerator(libellesGroupes::get);
				listeGroupes.setValue(sessionController.getGroupeInscrits() != null ? sessionController.getGroupeInscrits() : TOUS_LES_GROUPES_LABEL);

				//Gestion de l'événement sur le changement de groupe
				listeGroupes.addValueChangeListener(event -> {
					if (event.isFromClient()) {
						String grpSelectionnee = event.getValue();
						if (TOUS_LES_GROUPES_LABEL.equals(grpSelectionnee)) {
							grpSelectionnee = null;
						}
						sessionController.setGroupeInscrits(grpSelectionnee);
						//faire le changement
						filtrerInscrits(getEtapeSelectionnee(), grpSelectionnee);
					}
				});

				final Button btnDetailGpe = new Button(VaadinIcon.SEARCH.create());
				btnDetailGpe.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
				btnDetailGpe.setTooltipText(getTranslation(NAME + ".detail.groupes"));
				btnDetailGpe.addClickListener(e -> {
					String vet = null;
					if (listeEtapes != null && listeEtapes.getValue() != null && !listeEtapes.getValue().equals(TOUTES_LES_ETAPES_LABEL)) {
						vet = listeEtapes.getItemLabelGenerator().apply(listeEtapes.getValue());
					}
					final DetailGroupesWindow dgw = detailGroupesWindowFactory.getObject();
					dgw.init(lgroupes, panelTitre.getText(), vet, listeAnnees.getValue());
					dgw.open();
				});

				final HorizontalLayout gpLayout = new HorizontalLayout();
				gpLayout.setSpacing(false);
				gpLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
				gpLayout.add(listeGroupes, btnDetailGpe);
				formInscritLayout.addFormItem(gpLayout, getTranslation(NAME + ".groupes"));
			}
		}
		panelLayout.add(formInscritLayout);

		//Création du favori pour l'objet concerné par la liste des inscrits
		final List<Favoris> lfav = favorisController.getFavoris();
		final FavorisPK favpk = new FavorisPK();
		favpk.setLogin(userController.getCurrentUserName());
		favpk.setIdfav(code);
		favpk.setTypfav(typeFavori);
		final Favoris favori = new Favoris();
		favori.setId(favpk);
		//Création du bouton pour ajouter l'objet aux favoris
		final VerticalLayout favoriLayout = new VerticalLayout();
		favoriLayout.setPadding(false);
		favoriLayout.setAlignItems(FlexComponent.Alignment.END);
		btnAjoutFavori = new Button(getTranslation(NAME + ".btn.ajoutFavori"), VaadinIcon.STAR_O.create());
		btnAjoutFavori.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		btnAjoutFavori.addClickListener(e -> {
			//creation du favori en base sur le clic du bouton
			favorisController.saveFavori(favori);
			//On cache le bouton de mise en favori
			btnAjoutFavori.setVisible(false);
			//Affichage d'un message de confirmation
			Notification.show(getTranslation(NAME + ".message.favoriAjoute"));
		});
		favoriLayout.add(btnAjoutFavori);

		if (typeIsElp()) {
			final Button btnMasquerFiltre = new Button(getTranslation(NAME + ".btn.btnMasquerFiltre"), VaadinIcon.CHEVRON_CIRCLE_UP.create());
			btnMasquerFiltre.addClickListener(e -> {
				panelFormInscrits.removeAll();
				panelFormInscrits.add(panelTitre);
				if (btnDisplayFiltres != null) {
					btnDisplayFiltres.setVisible(true);
				}
			});
			favoriLayout.add(btnMasquerFiltre);
		}
		panelLayout.add(favoriLayout);

		//Si l'objet est déjà en favori, on n'affiche pas le bouton de mise en favori
		if (lfav != null && lfav.contains(favori)) {
			btnAjoutFavori.setVisible(false);
		}

		panelFormInscrits.add(panelTitre, panelLayout);
		add(panelFormInscrits);

		//Récupération de la liste des inscrits
		linscrits = sessionController.getListeInscrits();

		refreshListeCodind(linscrits);

		//Test si la liste contient des étudiants
		if (linscrits == null || linscrits.isEmpty() || listecodind == null || listecodind.isEmpty()) {
			final Span infoAucuninscrit = new Span(getTranslation(NAME + ".message.aucuninscrit"));
			add(infoAucuninscrit);
			setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, infoAucuninscrit);
			return;
		}

		final VerticalLayout infoLayout = new VerticalLayout();
		infoLayout.setPadding(false);
		infoLayout.setSizeFull();

		//Layout avec le nb d'inscrits, le bouton trombinoscope et le bouton d'export
		final HorizontalLayout resumeLayout = new HorizontalLayout();
		resumeLayout.setWidthFull();
		resumeLayout.setAlignItems(FlexComponent.Alignment.CENTER);

		//Label affichant le nb d'inscrits
		infoNbInscrit = new Span(getTranslation(NAME + ".message.nbinscrit") + " : " + linscrits.size());

		leftResumeLayout = new HorizontalLayout();
		leftResumeLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		leftResumeLayout.add(infoNbInscrit);

		final Button infoDescriptionButton = new Button(VaadinIcon.INFO_CIRCLE.create());
		infoDescriptionButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		infoDescriptionButton.setTooltipText(getTranslation(NAME + ".message.info.predescription"));
		infoDescriptionButton.addClickListener(e -> {
			String message = "";
			if (typeIsVet()) {
				message = getTranslation(NAME + ".message.info.vetdescription");
			}
			if (typeIsElp()) {
				message = getTranslation(NAME + ".message.info.elpdescription");
			}
			new HelpBasicWindow(message, getTranslation("helpWindow.defaultTitle")).open();
		});
		leftResumeLayout.add(infoDescriptionButton);

		//Bouton export trombinoscope (pdf)
		String nomFichier = getTranslation("pdf.trombinoscope.title") + "_" + panelTitre.getText() + ".pdf";
		nomFichier = nomFichier.replaceAll(" ", "_");
		final String nomFichierPdf = nomFichier;
		final Button btnExportTrombi = new Button(VaadinIcon.FILE_TEXT_O.create());
		btnExportTrombi.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		btnExportTrombi.setTooltipText(getTranslation(NAME + ".pdf.trombinoscope.link"));
		exportTrombiAnchor = new Anchor(DownloadHandler.fromInputStream(evt -> {
			/* Le handler s'exécute hors du verrou de session Vaadin : verrou requis pour lire
			   les composants et résoudre les beans de scope vaadin-session (SessionController) */
			final VaadinSession session = evt.getSession();
			session.lock();
			try {
				//recuperation de l'année sélectionnée et du libellé de l'ELP
				final String annee = listeAnnees.getValue();
				final String libObj = panelTitre.getText();
				//création du trombi en pdf
				return new DownloadResponse(listeInscritsController.getPdfStream(linscrits, listecodind, libObj, annee),
					nomFichierPdf, "application/pdf", -1);
			} finally {
				session.unlock();
			}
		}), "");
		exportTrombiAnchor.add(btnExportTrombi);
		exportTrombiAnchor.getElement().setAttribute("download", true);
		leftResumeLayout.add(exportTrombiAnchor);
		//On cache le bouton d'export pdf
		exportTrombiAnchor.setVisible(false);

		//Bouton export liste excel
		String nomFichierExcel = getTranslation("excel.listeinscrits.title") + "_" + panelTitre.getText() + ".xlsx";
		nomFichierExcel = nomFichierExcel.replaceAll(" ", "_");
		final String nomFichierXls = nomFichierExcel;
		final Button btnExportExcel = new Button(VaadinIcon.FILE_TABLE.create());
		btnExportExcel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		btnExportExcel.setTooltipText(getTranslation(NAME + ".excel.link"));
		exportExcelAnchor = new Anchor(DownloadHandler.fromInputStream(evt -> {
			/* Le handler s'exécute hors du verrou de session Vaadin : verrou requis (cf. export trombinoscope) */
			final VaadinSession session = evt.getSession();
			session.lock();
			try {
				//Recuperation de l'année sélectionnée et du libellé de l'ELP
				final String annee = listeAnnees.getValue();
				final String libObj = panelTitre.getText();
				// Récupération des paramètres d'affichage du tableau
				final boolean etp = collapseEtp != null && Boolean.TRUE.equals(collapseEtp.getValue());
				final boolean s1 = collapseResultatsS1 != null && Boolean.TRUE.equals(collapseResultatsS1.getValue());
				final boolean s2 = collapseResultatsS2 != null && Boolean.TRUE.equals(collapseResultatsS2.getValue());
				final boolean grp = collapseGrp != null && Boolean.TRUE.equals(collapseGrp.getValue());
				return new DownloadResponse(listeInscritsController.getXlsStream(linscrits, listecodind, libellesGroupes, libObj, annee, typeFavori, etp, s1, s2, grp, afficherInfoNaissance()),
					nomFichierXls, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", -1);
			} finally {
				session.unlock();
			}
		}), "");
		exportExcelAnchor.add(btnExportExcel);
		exportExcelAnchor.getElement().setAttribute("download", true);
		leftResumeLayout.add(exportExcelAnchor);

		resumeLayout.add(leftResumeLayout);

		//Middle layout avec les boutons de collapse des colonnes
		middleResumeLayout = new HorizontalLayout();
		middleResumeLayout.addClassName("small-font-element");
		middleResumeLayout.setSpacing(true);

		if (!typeIsVet()) {
			collapseEtp = new Checkbox(getTranslation(NAME + ".collapseEtp.title"));
			collapseEtp.setValue(true);
			collapseEtp.addValueChangeListener(e -> colEtape.setVisible(collapseEtp.getValue()));
			collapseEtp.setTooltipText(getTranslation(NAME + ".collapseEtp.description"));
			middleResumeLayout.add(collapseEtp);
		}
		collapseResultatsS1 = new Checkbox(getTranslation(NAME + ".collapseResultatsS1.title"));
		collapseResultatsS1.setValue(false);
		collapseResultatsS1.addValueChangeListener(e -> colNotes1.setVisible(collapseResultatsS1.getValue()));
		collapseResultatsS1.setTooltipText(getTranslation(NAME + ".collapseResultatsS1.description"));
		middleResumeLayout.add(collapseResultatsS1);

		collapseResultatsS2 = new Checkbox(getTranslation(NAME + ".collapseResultatsS2.title"));
		collapseResultatsS2.setValue(false);
		collapseResultatsS2.addValueChangeListener(e -> colNotes2.setVisible(collapseResultatsS2.getValue()));
		collapseResultatsS2.setTooltipText(getTranslation(NAME + ".collapseResultatsS2.description"));
		middleResumeLayout.add(collapseResultatsS2);

		if (listeGroupes != null) {
			collapseGrp = new Checkbox(getTranslation(NAME + ".collapseGrp.title"));
			collapseGrp.setValue(false);
			collapseGrp.addValueChangeListener(e -> colGroupe.setVisible(collapseGrp.getValue()));
			collapseGrp.setTooltipText(getTranslation(NAME + ".collapseGrp.description"));
			middleResumeLayout.add(collapseGrp);
		}

		resumeLayout.add(middleResumeLayout);

		final HorizontalLayout buttonResumeLayout = new HorizontalLayout();
		buttonResumeLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		//Bouton pour afficher les filtres
		btnDisplayFiltres = new Button(VaadinIcon.FILTER.create());
		btnDisplayFiltres.setTooltipText(getTranslation(NAME + ".btn.displayFilters"));
		btnDisplayFiltres.addClickListener(e -> {
			panelFormInscrits.removeAll();
			panelFormInscrits.add(panelTitre, panelLayout);
			btnDisplayFiltres.setVisible(false);
		});
		btnDisplayFiltres.setVisible(false);
		buttonResumeLayout.add(btnDisplayFiltres);

		//Bouton trombinoscope
		btnTrombi = new Button(getTranslation(NAME + ".message.trombinoscope"), VaadinIcon.GROUP.create());
		if (listeInscritsController.isPhotoProviderOperationnel()) {
			buttonResumeLayout.add(btnTrombi);

			//Gestion du clic sur le bouton trombinoscope
			btnTrombi.addClickListener(e -> {
				//Si on doit afficher une fenêtre de loading pendant l'exécution
				if (PropertyUtils.isShowLoadingIndicator()) {
					//affichage de la pop-up de loading
					MainLayout.getCurrent().ifPresent(MainLayout::startBusyIndicator);
					final UI ui = UI.getCurrent();
					//Execution de la méthode en parallèle dans un thread
					new Thread(() -> {
						ui.access(this::executeDisplayTrombinoscope);
						ui.access(() -> MainLayout.getCurrent().ifPresent(MainLayout::stopBusyIndicator));
					}).start();
				} else {
					//On exécute directement la méthode
					executeDisplayTrombinoscope();
				}
			});
		}

		//Bouton de retour à l'affichage de la liste
		btnRetourListe = new Button(getTranslation(NAME + ".message.retourliste"), VaadinIcon.MENU.create());
		buttonResumeLayout.add(btnRetourListe);
		btnRetourListe.setVisible(false);

		//Gestion du clic sur le bouton de retour à l'affichage de la liste
		btnRetourListe.addClickListener(e -> {
			afficherTrombinoscope = false;
			exportTrombiAnchor.setVisible(false);
			exportExcelAnchor.setVisible(true);
			btnTrombi.setVisible(true);
			btnRetourListe.setVisible(false);
			dataLayout.removeAll();
			dataLayout.add(inscritstable);
			middleResumeLayout.setVisible(true);
		});
		resumeLayout.add(buttonResumeLayout);
		infoLayout.add(resumeLayout);

		//Layout qui contient la liste des inscrits et le trombinoscope
		dataLayout = new VerticalLayout();
		dataLayout.setPadding(false);
		dataLayout.setSizeFull();

		//Grid contenant la liste des inscrits
		inscritstable = new Grid<>();
		inscritstable.addClassNames("table-without-column-selector", "scrollabletable");
		inscritstable.setSizeFull();

		inscritstable.addComponentColumn(i -> {
			final Button btnCodEtu = new Button(i.getCod_etu());
			btnCodEtu.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			btnCodEtu.addClassName("v-link");
			btnCodEtu.addClickListener(e -> rechercheController.accessToDetail(i.getCod_etu(), Utils.TYPE_ETU, null));
			return btnCodEtu;
		}).setHeader(getTranslation(NAME + ".table.cod_etu")).setFlexGrow(1).setResizable(true);

		inscritstable.addColumn(Inscrit::getNom).setHeader(getTranslation(NAME + ".table.nom")).setFlexGrow(2).setResizable(true);
		inscritstable.addColumn(Inscrit::getPrenom).setHeader(getTranslation(NAME + ".table.prenom")).setFlexGrow(2).setResizable(true);

		if (afficherInfoNaissance()) {
			inscritstable.addColumn(Inscrit::getDate_nai_ind).setHeader(getTranslation(NAME + ".table.date_nai_ind")).setFlexGrow(1).setResizable(true);
		}

		inscritstable.addComponentColumn(i -> {
			if (StringUtils.hasText(i.getEmail())) {
				return new Html("<a href=\"mailto:" + i.getEmail() + "\">" + i.getEmail() + "</a>");
			}
			return new Html("<span></span>");
		}).setHeader(getTranslation(NAME + ".table.email")).setFlexGrow(2).setResizable(true);

		if (typeIsVet()) {
			inscritstable.addColumn(Inscrit::getIae).setHeader(getTranslation(NAME + ".table.iae")).setFlexGrow(1).setResizable(true);
			inscritstable.addColumn(Inscrit::getIpe).setHeader(getTranslation(NAME + ".table.ipe")).setFlexGrow(1).setResizable(true);
		}

		//Si on est sur un ELP, on affiche l'étape de rattachement
		if (typeIsElp()) {
			colEtape = inscritstable.addComponentColumn(i -> {
				final Div vets = new Div();
				if (i.getListe_vet() != null) {
					//On parcourt la liste des vets
					for (final Vet ve : i.getListe_vet()) {
						if (StringUtils.hasText(ve.getLib_etp())) {
							final Div etapeDiv = new Div();
							etapeDiv.setText(ve.getLib_etp());
							vets.add(etapeDiv);
						}
					}
				}
				return vets;
			}).setHeader(getTranslation(NAME + ".table.etape")).setFlexGrow(2).setResizable(true);
		}

		colNotes1 = inscritstable.addColumn(i -> {
			final StringBuilder sb = new StringBuilder();
			if (StringUtils.hasText(i.getNotej())) {
				sb.append(i.getNotej());
			}
			if (StringUtils.hasText(i.getResj())) {
				sb.append(" ").append(i.getResj());
			}
			return sb.toString().trim();
		}).setHeader(getTranslation(NAME + ".table.notes1")).setFlexGrow(1).setResizable(true);

		colNotes2 = inscritstable.addColumn(i -> {
			final StringBuilder sb = new StringBuilder();
			if (StringUtils.hasText(i.getNotes())) {
				sb.append(i.getNotes());
			}
			if (StringUtils.hasText(i.getRess())) {
				sb.append(" ").append(i.getRess());
			}
			return sb.toString().trim();
		}).setHeader(getTranslation(NAME + ".table.notes2")).setFlexGrow(1).setResizable(true);

		colGroupe = inscritstable.addColumn(i -> {
			if (StringUtils.hasText(i.getCodes_groupes())) {
				return Utils.getLibellesFromCodes(i.getCodes_groupes(), libellesGroupes);
			}
			return "";
		}).setHeader(getTranslation(NAME + ".table.groupe")).setFlexGrow(1).setResizable(true);

		//On masque les colonnes de notes et groupe par défaut
		colNotes1.setVisible(false);
		colNotes2.setVisible(false);
		colGroupe.setVisible(false);

		dataLayout.add(inscritstable);

		//Layout contenant le trombinoscope
		trombiLayout = new FlexLayout();
		trombiLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		trombiLayout.setWidthFull();
		trombiLayout.addClassName("v-scrollablepanel");

		//Création du trombinoscope
		displayTrombinoscope();

		infoLayout.add(dataLayout);
		infoLayout.setFlexGrow(1, dataLayout);
		add(infoLayout);
		setFlexGrow(1, infoLayout);

		//refresh de la liste à afficher au départ
		filtrerInscrits(getEtapeSelectionnee(), getGroupeSelectionne());
	}

	private boolean afficherInfoNaissance() {
		return (userController.isEnseignant() && configController.isAffInfoNaissanceEnseignant())
			|| (userController.isGestionnaire() && configController.isAffInfoNaissanceGestionnaire());
	}

	//récupère l'étape sélectionnée
	private String getEtapeSelectionnee() {
		final String etapeSelectionnee = (listeEtapes != null && listeEtapes.getValue() != null) ? listeEtapes.getValue() : null;
		return TOUTES_LES_ETAPES_LABEL.equals(etapeSelectionnee) ? null : etapeSelectionnee;
	}

	//récupère le groupe sélectionné
	private String getGroupeSelectionne() {
		final String grpSelectionne = (listeGroupes != null && listeGroupes.getValue() != null) ? listeGroupes.getValue() : null;
		return TOUS_LES_GROUPES_LABEL.equals(grpSelectionne) ? null : grpSelectionne;
	}

	private void executeDisplayTrombinoscope() {
		//On refresh le ticket de photo et on met à jour le trombinoscope avec les urls photo mises à jour
		listeInscritsController.setUrlPhotos(sessionController.getListeInscrits());
		displayTrombinoscope();

		afficherTrombinoscope = true;
		btnTrombi.setVisible(false);
		exportTrombiAnchor.setVisible(true);
		exportExcelAnchor.setVisible(false);

		//Bouton retour à la liste devient visible
		btnRetourListe.setVisible(true);
		dataLayout.removeAll();
		dataLayout.add(trombiLayout);
		middleResumeLayout.setVisible(false);
	}

	private void displayTrombinoscope() {
		final List<Inscrit> inscrits = sessionController.getListeInscrits();
		trombiLayout.removeAll();

		if (inscrits == null) {
			return;
		}
		for (final Inscrit inscrit : inscrits) {
			if (listecodind.contains(inscrit.getCod_ind())) {
				final VerticalLayout photoLayout = new VerticalLayout();
				photoLayout.setPadding(true);
				photoLayout.setSpacing(false);
				photoLayout.setWidth("160px");
				photoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
				photoLayout.setId(inscrit.getCod_ind());
				if (inscrit.getUrlphoto() != null) {
					final Image fotoEtudiant = new Image(inscrit.getUrlphoto(), inscrit.getCod_etu());
					fotoEtudiant.setWidth("120px");
					fotoEtudiant.addClassName("photo-mdw");
					fotoEtudiant.getStyle().set("cursor", "pointer");
					fotoEtudiant.addClickListener(e -> rechercheController.accessToDetail(inscrit.getCod_etu(), Utils.TYPE_ETU, null));
					photoLayout.add(fotoEtudiant);
				}
				final Button btnNomEtudiant = new Button(inscrit.getPrenom() + " " + inscrit.getNom());
				btnNomEtudiant.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				btnNomEtudiant.addClassName("v-link");
				btnNomEtudiant.addClickListener(e -> rechercheController.accessToDetail(inscrit.getCod_etu(), Utils.TYPE_ETU, null));
				photoLayout.add(btnNomEtudiant);

				final Span codetuLabel = new Span(inscrit.getCod_etu());
				codetuLabel.addClassName("text-secondary");
				photoLayout.add(codetuLabel);

				trombiLayout.add(photoLayout);
			}
		}
	}

	private void filtrerInscrits(final String idEtape, final String idgroupe) {

		log.debug("filtre les inscrits pour idetape:" + idEtape + " et idgroupe :" + idgroupe);
		if (inscritstable != null && linscrits != null) {
			final List<Inscrit> filtres = linscrits.stream()
				.filter(i -> !StringUtils.hasText(idEtape)
					|| (i.getId_etp() != null && i.getId_etp().toLowerCase().contains(idEtape.toLowerCase())))
				.filter(i -> !StringUtils.hasText(idgroupe)
					|| (i.getCodes_groupes() != null && i.getCodes_groupes().toLowerCase().contains(idgroupe.toLowerCase())))
				.toList();

			inscritstable.setItems(filtres);

			//Maj de la liste contenant tous les codind à afficher apres application du filtre
			refreshListeCodind(filtres);

			//maj trombinoscope
			displayTrombinoscope();

			//maj de l'affichage du nombre d'étudiants
			infoNbInscrit.setText(getTranslation(NAME + ".message.nbinscrit") + " : " + listecodind.size());
		}
	}

	private boolean typeIsVet() {
		return typeFavori != null && typeFavori.equals(Utils.VET);
	}

	private boolean typeIsElp() {
		return typeFavori != null && typeFavori.equals(Utils.ELP);
	}

	private void refreshListeCodind(final List<Inscrit> inscrits) {
		if (listecodind != null) {
			listecodind.clear();
		} else {
			listecodind = new LinkedList<>();
		}
		if (inscrits != null) {
			for (final Inscrit inscrit : inscrits) {
				listecodind.add(inscrit.getCod_ind());
			}
		}
	}
}
