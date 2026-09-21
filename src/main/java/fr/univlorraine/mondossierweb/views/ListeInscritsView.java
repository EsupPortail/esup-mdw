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
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
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
	/** Menu d'actions (trois points verticaux) affiché à côté du titre */
	private ContextMenu actionsMenu;
	/** Bouton d'ouverture du menu d'actions */
	private Button btnActionsMenu;
	/** Entrée du menu d'actions pour ajouter l'objet aux favoris */
	private MenuItem menuItemAjoutFavori;
	/** Entrée du menu d'actions pour réafficher les filtres */
	private MenuItem menuItemDisplayFiltres;
	/** Entrée du menu d'actions pour masquer les filtres (uniquement pour un ELP) */
	private MenuItem menuItemMasquerFiltres;
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
	/** Layout regroupant le titre et le bouton d'actions */
	private HorizontalLayout titreLayout;
	private HorizontalLayout leftResumeLayout;
	/** Colonnes affichées dans la liste, pilotées depuis le menu d'actions */
	private boolean afficherEtape = true;
	private boolean afficherSession1 = false;
	private boolean afficherSession2 = false;
	private boolean afficherGroupe = false;
	/** Bouton d'actions (trois points verticaux) affiché à côté du bouton d'information */
	private Button btnResumeMenu;
	/** Menu d'actions des exports et de l'affichage des colonnes */
	private ContextMenu resumeMenu;
	private MenuItem menuItemExportTrombi;
	private MenuItem menuItemExportExcel;
	private MenuItem menuItemCollapseEtp;
	private MenuItem menuItemCollapseS1;
	private MenuItem menuItemCollapseS2;
	private MenuItem menuItemCollapseGrp;

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

		//Panel contenant les filtres d'affichage
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

		//Création du favori pour l'objet concerné par la liste des inscrits
		final List<Favoris> lfav = favorisController.getFavoris();
		final FavorisPK favpk = new FavorisPK();
		favpk.setLogin(userController.getCurrentUserName());
		favpk.setIdfav(code);
		favpk.setTypfav(typeFavori);
		final Favoris favori = new Favoris();
		favori.setId(favpk);

		//Bouton d'actions (trois points verticaux) affiché à côté du titre
		btnActionsMenu = new Button(VaadinIcon.ELLIPSIS_V.create());
		btnActionsMenu.addClassName("btn-actions-menu");
		btnActionsMenu.setTooltipText(getTranslation(NAME + ".btn.actions"));

		//Titre et bouton d'actions alignés horizontalement
		titreLayout = new HorizontalLayout();
		titreLayout.addClassName("titre-liste-inscrits");
		titreLayout.setSpacing(true);
		titreLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		titreLayout.add(panelTitre, btnActionsMenu);

		//Menu d'actions ouvert au clic sur le bouton : titre rappelant l'objet puis actions
		actionsMenu = new ContextMenu(btnActionsMenu);
		actionsMenu.setOpenOnClick(true);
		final Span enteteMenu = new Span(panelTitre.getText());
		enteteMenu.addClassName("menu-actions-entete");
		//En-tête affiché uniquement sur mobile
		actionsMenu.addItem(enteteMenu).setClassName("menu-actions-entete-item");

		//Action de mise en favori de l'objet concerné par la liste des inscrits
		menuItemAjoutFavori = actionsMenu.addItem(
			actionMenuItem(VaadinIcon.STAR_O, getTranslation(NAME + ".btn.ajoutFavori")),
			e -> {
				//creation du favori en base sur le clic du menu
				favorisController.saveFavori(favori);
				//On cache l'action de mise en favori
				menuItemAjoutFavori.setVisible(false);
				//Affichage d'un message de confirmation
				Notification.show(getTranslation(NAME + ".message.favoriAjoute"));
				majVisibiliteBtnActionsMenu();
			});
		//Si l'objet est déjà en favori, on n'affiche pas l'action de mise en favori
		if (lfav != null && lfav.contains(favori)) {
			menuItemAjoutFavori.setVisible(false);
		}

		//Action de réaffichage des filtres
		menuItemDisplayFiltres = actionsMenu.addItem(
			actionMenuItem(VaadinIcon.FILTER, getTranslation(NAME + ".btn.displayFilters")),
			e -> afficherFiltres());
		menuItemDisplayFiltres.setVisible(false);

		//Action de masquage des filtres, uniquement pour un ELP (remplace l'ancien bouton btnMasquerFiltre)
		menuItemMasquerFiltres = actionsMenu.addItem(
				actionMenuItem(VaadinIcon.CHEVRON_CIRCLE_UP, getTranslation(NAME + ".btn.btnMasquerFiltre")),
				e -> masquerFiltres());

		//Le bouton d'actions n'est affiché que si au moins une action est disponible
		majVisibiliteBtnActionsMenu();

		//Affichage d'une liste déroulante contenant la liste des années
		final List<String> lannees = sessionController.getListeAnneeInscrits();
		if (lannees != null && !lannees.isEmpty()) {
			listeAnnees = new ComboBox<>(getTranslation(NAME + ".annee"));
			listeAnnees.setAllowCustomValue(false);
			listeAnnees.setWidth("150px");
			listeAnnees.setItems(lannees);
			listeAnnees.getStyle().set("padding", "0");
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

		panelFormInscrits.add(titreLayout, panelLayout);
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
		infoNbInscrit = new Span(getInfoNbInscrits(linscrits.size()));

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

		//Bouton d'actions (trois points verticaux) : exports et affichage des colonnes
		btnResumeMenu = new Button(VaadinIcon.ELLIPSIS_V.create());
		btnResumeMenu.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		btnResumeMenu.setTooltipText(getTranslation(NAME + ".btn.actions"));
		leftResumeLayout.add(btnResumeMenu);

		//Noms des fichiers exportés
		final String nomFichierPdf = (getTranslation("pdf.trombinoscope.title") + "_" + panelTitre.getText() + ".pdf").replaceAll(" ", "_");
		final String nomFichierXls = (getTranslation("excel.listeinscrits.title") + "_" + panelTitre.getText() + ".xlsx").replaceAll(" ", "_");

		//Ancres de téléchargement, masquées et utilisées uniquement pour déclencher les exports
		final Anchor ancreExportTrombi = creerAncreMasquee(DownloadHandler.fromInputStream(evt -> {
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
		}));
		final Anchor ancreExportExcel = creerAncreMasquee(DownloadHandler.fromInputStream(evt -> {
			/* Le handler s'exécute hors du verrou de session Vaadin : verrou requis (cf. export trombinoscope) */
			final VaadinSession session = evt.getSession();
			session.lock();
			try {
				//Recuperation de l'année sélectionnée et du libellé de l'ELP
				final String annee = listeAnnees.getValue();
				final String libObj = panelTitre.getText();
				// Récupération des paramètres d'affichage du tableau
				final boolean etp = !typeIsVet() && afficherEtape;
				final boolean s1 = afficherSession1;
				final boolean s2 = afficherSession2;
				final boolean grp = listeGroupes != null && afficherGroupe;
				return new DownloadResponse(listeInscritsController.getXlsStream(linscrits, listecodind, libellesGroupes, libObj, annee, typeFavori, etp, s1, s2, grp, afficherInfoNaissance()),
					nomFichierXls, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", -1);
			} finally {
				session.unlock();
			}
		}));

		resumeLayout.add(leftResumeLayout);

		//Menu d'actions ouvert au clic sur le bouton : exports puis affichage des colonnes
		resumeMenu = new ContextMenu(btnResumeMenu);
		resumeMenu.setOpenOnClick(true);

		//Export du trombinoscope (pdf), proposé uniquement lorsque le trombinoscope est affiché
		menuItemExportTrombi = resumeMenu.addItem(
			actionMenuItem(VaadinIcon.FILE_TEXT_O, getTranslation(NAME + ".pdf.trombinoscope.link")),
			e -> ancreExportTrombi.getElement().callJsFunction("click"));
		menuItemExportTrombi.setVisible(afficherTrombinoscope);

		//Export de la liste (excel), proposé uniquement lorsque la liste est affichée
		menuItemExportExcel = resumeMenu.addItem(
			actionMenuItem(VaadinIcon.FILE_TABLE, getTranslation(NAME + ".excel.link")),
			e -> ancreExportExcel.getElement().callJsFunction("click"));
		menuItemExportExcel.setVisible(!afficherTrombinoscope);

		//Actions d'affichage des colonnes, en cochant/décochant l'entrée correspondante
		if (!typeIsVet()) {
			menuItemCollapseEtp = resumeMenu.addItem(
				actionMenuItem(VaadinIcon.CHECK_SQUARE_O, getTranslation(NAME + ".collapseEtp.title")),
				e -> basculerAffichageColonne(menuItemCollapseEtp, true));
			initialiserItemColonne(menuItemCollapseEtp, afficherEtape);
		}
		menuItemCollapseS1 = resumeMenu.addItem(
			actionMenuItem(VaadinIcon.CHECK_SQUARE_O, getTranslation(NAME + ".collapseResultatsS1.title")),
			e -> basculerAffichageColonne(menuItemCollapseS1, false));
		initialiserItemColonne(menuItemCollapseS1, afficherSession1);

		menuItemCollapseS2 = resumeMenu.addItem(
			actionMenuItem(VaadinIcon.CHECK_SQUARE_O, getTranslation(NAME + ".collapseResultatsS2.title")),
			e -> basculerAffichageColonne(menuItemCollapseS2, false));
		initialiserItemColonne(menuItemCollapseS2, afficherSession2);

		if (listeGroupes != null) {
			menuItemCollapseGrp = resumeMenu.addItem(
				actionMenuItem(VaadinIcon.CHECK_SQUARE_O, getTranslation(NAME + ".collapseGrp.title")),
				e -> basculerAffichageColonne(menuItemCollapseGrp, false));
			initialiserItemColonne(menuItemCollapseGrp, afficherGroupe);
		}

		majVisibiliteResumeMenu();

		final HorizontalLayout buttonResumeLayout = new HorizontalLayout();
		buttonResumeLayout.setAlignItems(FlexComponent.Alignment.CENTER);

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
			btnTrombi.setVisible(true);
			btnRetourListe.setVisible(false);
			dataLayout.removeAll();
			dataLayout.add(inscritstable);
			majVisibiliteResumeMenu();
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

	private String getInfoNbInscrits(int size) {
		return size + " " + (size > 1 ? getTranslation(NAME + ".message.nbinscrits") : getTranslation(NAME + ".message.nbinscrit"));
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

	/** Masque les filtres : seul le titre (et le bouton d'actions) reste affiché. */
	private void masquerFiltres() {
		panelFormInscrits.removeAll();
		panelFormInscrits.add(titreLayout);
		if (menuItemDisplayFiltres != null) {
			menuItemDisplayFiltres.setVisible(true);
		}
		if (menuItemMasquerFiltres != null) {
			menuItemMasquerFiltres.setVisible(false);
		}
		majVisibiliteBtnActionsMenu();
	}

	/** Réaffiche les filtres sous le titre. */
	private void afficherFiltres() {
		panelFormInscrits.removeAll();
		panelFormInscrits.add(titreLayout, panelLayout);
		if (menuItemDisplayFiltres != null) {
			menuItemDisplayFiltres.setVisible(false);
		}
		if (menuItemMasquerFiltres != null) {
			menuItemMasquerFiltres.setVisible(true);
		}
		majVisibiliteBtnActionsMenu();
	}

	/** Affiche le bouton d'actions uniquement si au moins une action du menu est visible. */
	private void majVisibiliteBtnActionsMenu() {
		if (btnActionsMenu == null) {
			return;
		}
		final boolean actionVisible = (menuItemAjoutFavori != null && menuItemAjoutFavori.isVisible())
			|| (menuItemDisplayFiltres != null && menuItemDisplayFiltres.isVisible())
			|| (menuItemMasquerFiltres != null && menuItemMasquerFiltres.isVisible());
		btnActionsMenu.setVisible(actionVisible);
	}

	/** Crée une ancre masquée, rattachée à la vue, servant uniquement à déclencher un téléchargement. */
	private Anchor creerAncreMasquee(final DownloadHandler handler) {
		final Anchor anchor = new Anchor(handler, "");
		anchor.getElement().setAttribute("download", true);
		anchor.getStyle().set("display", "none");
		add(anchor);
		return anchor;
	}

	/** Initialise une entrée de menu cochable correspondant à l'affichage d'une colonne. */
	private void initialiserItemColonne(final MenuItem menuItem, final boolean affiche) {
		menuItem.setCheckable(true);
		menuItem.setKeepOpen(true);
		menuItem.setChecked(affiche);
	}

	/**
	 * Applique l'affichage de la colonne associée à une entrée de menu.
	 * L'état est basculé à partir de la valeur métier, puis resynchronisé sur le composant.
	 */
	private void basculerAffichageColonne(final MenuItem menuItem, final boolean estEtape) {
		if (estEtape) {
			afficherEtape = !afficherEtape;
			menuItem.setChecked(afficherEtape);
			if (colEtape != null) {
				colEtape.setVisible(afficherEtape);
			}
		} else if (menuItem == menuItemCollapseS1) {
			afficherSession1 = !afficherSession1;
			menuItem.setChecked(afficherSession1);
			if (colNotes1 != null) {
				colNotes1.setVisible(afficherSession1);
			}
		} else if (menuItem == menuItemCollapseS2) {
			afficherSession2 = !afficherSession2;
			menuItem.setChecked(afficherSession2);
			if (colNotes2 != null) {
				colNotes2.setVisible(afficherSession2);
			}
		} else if (menuItem == menuItemCollapseGrp) {
			afficherGroupe = !afficherGroupe;
			menuItem.setChecked(afficherGroupe);
			if (colGroupe != null) {
				colGroupe.setVisible(afficherGroupe);
			}
		}
	}

	/**
	 * Affiche les actions d'export et d'affichage des colonnes selon l'affichage courant
	 * (liste ou trombinoscope), puis masque le bouton d'actions s'il ne reste aucune action.
	 */
	private void majVisibiliteResumeMenu() {
		if (resumeMenu == null) {
			return;
		}
		//Le trombinoscope et la liste s'excluent : un seul export est proposé
		if (menuItemExportTrombi != null) {
			menuItemExportTrombi.setVisible(afficherTrombinoscope);
		}
		if (menuItemExportExcel != null) {
			menuItemExportExcel.setVisible(!afficherTrombinoscope);
		}
		//Les actions d'affichage des colonnes ne concernent que la liste
		if (menuItemCollapseEtp != null) {
			menuItemCollapseEtp.setVisible(!afficherTrombinoscope);
		}
		if (menuItemCollapseS1 != null) {
			menuItemCollapseS1.setVisible(!afficherTrombinoscope);
		}
		if (menuItemCollapseS2 != null) {
			menuItemCollapseS2.setVisible(!afficherTrombinoscope);
		}
		if (menuItemCollapseGrp != null) {
			menuItemCollapseGrp.setVisible(!afficherTrombinoscope);
		}
		final boolean actionVisible = (menuItemExportTrombi != null && menuItemExportTrombi.isVisible())
			|| (menuItemExportExcel != null && menuItemExportExcel.isVisible())
			|| (menuItemCollapseEtp != null && menuItemCollapseEtp.isVisible())
			|| (menuItemCollapseS1 != null && menuItemCollapseS1.isVisible())
			|| (menuItemCollapseS2 != null && menuItemCollapseS2.isVisible())
			|| (menuItemCollapseGrp != null && menuItemCollapseGrp.isVisible());
		btnResumeMenu.setVisible(actionVisible);
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

	private void executeDisplayTrombinoscope() {
		//On refresh le ticket de photo et on met à jour le trombinoscope avec les urls photo mises à jour
		listeInscritsController.setUrlPhotos(sessionController.getListeInscrits());
		displayTrombinoscope();

		afficherTrombinoscope = true;
		btnTrombi.setVisible(false);

		//Bouton retour à la liste devient visible
		btnRetourListe.setVisible(true);
		dataLayout.removeAll();
		dataLayout.add(trombiLayout);
		majVisibiliteResumeMenu();
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
			infoNbInscrit.setText(getInfoNbInscrits(listecodind.size()));
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
