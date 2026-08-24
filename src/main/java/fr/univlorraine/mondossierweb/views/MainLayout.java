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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.shared.Registration;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.SessionController;
import fr.univlorraine.mondossierweb.controllers.UiController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.HelpBasicWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpWindow;
import fr.univlorraine.mondossierweb.views.windows.LoadingIndicatorWindow;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Layout principal de l'application (remplace l'ancienne MainUI Vaadin 7) :
 * AppLayout avec navbar (titre de la vue affichée, utilisateur connecté,
 * déconnexion) et drawer contenant en tête le logo et le nom de
 * l'application, puis le menu construit selon le profil de l'utilisateur.
 */
@SuppressWarnings("serial")
@Slf4j
public class MainLayout extends AppLayout implements BeforeEnterObserver, AfterNavigationObserver {

	/* Routes des vues (mêmes noms que les anciennes @SpringView, cf. guide de migration) */
	public static final String ROUTE_ETAT_CIVIL = "etatCivilView";
	public static final String ROUTE_INFORMATIONS_ANNUELLES = "informationsAnnuellesView";
	public static final String ROUTE_ADRESSES = "adressesView";
	public static final String ROUTE_INSCRIPTIONS = "inscriptionsView";
	public static final String ROUTE_CALENDRIER = "calendrierView";
	public static final String ROUTE_NOTES = "notesView";
	public static final String ROUTE_ASSISTANCE = "assistanceView";
	public static final String ROUTE_RECHERCHE_RAPIDE = "rechercheRapideView";
	public static final String ROUTE_RECHERCHE_ARBORESCENTE = "rechercheArborescenteView";
	public static final String ROUTE_FAVORIS = "favorisView";
	public static final String ROUTE_ADMIN = "adminView";

	/** Routes des vues du dossier étudiant */
	private static final Set<String> ROUTES_DOSSIER = Set.of(ROUTE_ETAT_CIVIL, ROUTE_INFORMATIONS_ANNUELLES,
		ROUTE_ADRESSES, ROUTE_INSCRIPTIONS, ROUTE_CALENDRIER, ROUTE_NOTES);

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient SessionController sessionController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient UiController uiController;

	/* Navbar */
	private final DrawerToggle drawerToggle = new DrawerToggle();
	private final H1 viewTitle = new H1();
	private final Span usernameSpan = new Span();
	private final Button logoutBtn = new Button();

	/* Drawer */
	private final HorizontalLayout drawerHeader = new HorizontalLayout();
	private final VerticalLayout drawerContent = new VerticalLayout();
	private final Tabs menuPrincipal = new Tabs();
	private final Tabs menuDossier = new Tabs();
	private final Div dossierSection = new Div();
	private final Div dossierHeader = new Div();

	/* Fenêtre d'attente (équivalent startBusyIndicator/stopBusyIndicator de l'ancienne MainUI) */
	private final LoadingIndicatorWindow loadingIndicatorWindow = new LoadingIndicatorWindow();

	/** cod_etu du dossier actuellement représenté dans le menu (null si aucun) */
	private String codEtuMenu;

	/** Dernière vue consultée hors du dossier étudiant (cible du retour à la fermeture du dossier) */
	private Location derniereLocationHorsDossier;

	/**
	 * @return le MainLayout de l'UI courante, s'il est présent
	 */
	public static Optional<MainLayout> getCurrent() {
		return Optional.ofNullable(UI.getCurrent())
			.flatMap(ui -> ui.getChildren()
				.filter(MainLayout.class::isInstance)
				.map(MainLayout.class::cast)
				.findFirst());
	}

	public MainLayout() {
		setPrimarySection(Section.DRAWER);
		configureMenus();
	}

	@PostConstruct
	public void init() {
		buildDrawerHeader();
		addToNavbar(true, buildHeader());
		addToDrawer(new Scroller(drawerContent));
	}

	/* ====================== Navbar ====================== */

	private HorizontalLayout buildHeader() {
		final HorizontalLayout header = new HorizontalLayout();
		header.setId("header");
		header.setWidthFull();
		header.setPadding(false);
		header.setSpacing(false);
		header.setAlignItems(FlexComponent.Alignment.CENTER);

		drawerToggle.getElement().setAttribute("title", getTranslation("mainLayout.drawerToggle.title"));
		drawerToggle.getElement().setAttribute("aria-label", getTranslation("mainLayout.drawerToggle.title"));
		header.add(drawerToggle);

		/* Titre de la vue affichée (mis à jour à chaque navigation) */
		viewTitle.getStyle()
			.set("font-size", "var(--lumo-font-size-l)")
			.set("margin", "0 var(--lumo-space-m)")
			.set("white-space", "nowrap")
			.set("overflow", "hidden")
			.set("color", "#fff")
			.set("text-overflow", "ellipsis");
		header.add(viewTitle);

		/* Espace extensible */
		final Div spacer = new Div();
		header.add(spacer);
		header.expand(spacer);

		/* Utilisateur connecté */
		if (userController.getCurrentAuthentication() != null) {
			usernameSpan.setText(userController.getCurrentAuthentication().getName());
		}
		usernameSpan.getStyle().set("margin-inline-end", "var(--lumo-space-m)");
		//header.add(usernameSpan);

		/* Déconnexion */
		logoutBtn.setText(getTranslation("buttonDeconnexion.label"));
		logoutBtn.setIcon(VaadinIcon.SIGN_OUT.create());
		logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		logoutBtn.getStyle().set("color", "inherit").set("margin-inline-end", "var(--lumo-space-m)");
		logoutBtn.addClickListener(e -> {
			userController.disconnectUser();
			UI.getCurrent().getPage().setLocation("logout");
		});
		logoutBtn.setVisible(configController.isLogoutCasPropose());
		//header.add(logoutBtn);

		return header;
	}

	/* ====================== En-tête du drawer ====================== */

	/**
	 * Construit l'en-tête affiché tout en haut du drawer :
	 * logo de l'université et nom de l'application.
	 */
	private void buildDrawerHeader() {
		drawerHeader.setId("drawer-header");
		drawerHeader.setWidthFull();
		drawerHeader.setPadding(true);
		drawerHeader.setSpacing(true);
		drawerHeader.setAlignItems(FlexComponent.Alignment.CENTER);

		/* Logo de l'université */
		final Image logo = buildLogoImage(getTranslation("mainUI.app.title"));
		logo.setHeight("2.1rem");
		drawerHeader.add(logo);

		/* Titre de l'application */
		final H1 appTitle = new H1(getTranslation("mainUI.app.title"));
		appTitle.getStyle()
			.set("font-size", "var(--lumo-font-size-l)")
			.set("margin", "0")
			.set("white-space", "nowrap");
		drawerHeader.add(appTitle);
	}

	/* ====================== Logo ====================== */

	/** Logo par défaut servi depuis {@code META-INF/resources/images/}. */
	private static final String DEFAULT_LOGO_SRC = "/images/logo-ul-blanc.png";

	/**
	 * Construit l'image du logo à partir du chemin/URL configuré en base
	 * (paramètre {@code logoUniversiteEns}/{@code logoUniversiteEtu}).
	 * <p>
	 * Reste compatible avec les valeurs héritées de l'ancienne application Vaadin 7,
	 * qui chargeait un {@code FileResource} depuis un chemin filesystem :
	 * <ul>
	 *   <li>URL http(s) ou protocole-relatif ({@code //...}) → utilisée telle quelle ;</li>
	 *   <li>chemin web absolu ({@code /images/...}) → servi depuis {@code META-INF/resources} ;</li>
	 *   <li>chemin filesystem → servi via un {@link StreamResource} ;</li>
	 *   <li>chemin classpath (ex: {@code images/logo-ul-blanc.png}) → servi via un {@link StreamResource} ;</li>
	 *   <li>vide ou introuvable → logo par défaut {@link #DEFAULT_LOGO_SRC}.</li>
	 * </ul>
	 */
	private Image buildLogoImage(final String alt) {
		final String configured = safeIsEtudiant()
			? configController.getLogoUniversiteEtu()
			: configController.getLogoUniversiteEns();

		if (StringUtils.hasText(configured)) {
			final String trimmed = configured.trim();
			final String lower = trimmed.toLowerCase();

			// 1. URL externe ou chemin web absolu : utilisable tel quel par le navigateur
			if (lower.startsWith("http://") || lower.startsWith("https://")
				|| lower.startsWith("//") || trimmed.startsWith("/")) {
				return new Image(trimmed, alt);
			}

			// 2. Chemin filesystem : on sert le fichier via un StreamResource
			final Image fileImage = tryFileImage(trimmed, alt);
			if (fileImage != null) {
				return fileImage;
			}

			// 3. Chemin classpath : servi via un StreamResource
			final Image cpImage = tryClasspathImage(trimmed, alt);
			if (cpImage != null) {
				return cpImage;
			}

			log.warn("Logo configuré '{}' introuvable (ni URL, ni fichier, ni ressource classpath) ; utilisation du logo par défaut.", trimmed);
		}

		// 4. Fallback : logo par défaut servi depuis META-INF/resources
		return new Image(DEFAULT_LOGO_SRC, alt);
	}

	/** Tente de servir le logo depuis un fichier filesystem. */
	private Image tryFileImage(final String path, final String alt) {
		try {
			final File file = new File(path);
			if (file.isFile()) {
				final String name = file.getName();
				final StreamResource res = new StreamResource(name, () -> {
					try {
						return new FileInputStream(file);
					} catch (FileNotFoundException e) {
						throw new UncheckedIOException(e);
					}
				});
				return new Image(res, alt);
			}
		} catch (Exception e) {
			log.debug("Impossible de charger le logo depuis le fichier {}", path, e);
		}
		return null;
	}

	/** Tente de servir le logo depuis le classpath. */
	private Image tryClasspathImage(final String path, final String alt) {
		final String cp = path.startsWith("/") ? path.substring(1) : path;
		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(cp)) {
			if (in == null) {
				return null;
			}
			final byte[] bytes = in.readAllBytes();
			final String name = path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
			final StreamResource res = new StreamResource(name, () -> new ByteArrayInputStream(bytes));
			return new Image(res, alt);
		} catch (Exception e) {
			log.debug("Impossible de charger le logo depuis le classpath {}", path, e);
		}
		return null;
	}

	/* ====================== Drawer / menu ====================== */

	private void configureMenus() {
		drawerContent.setPadding(false);
		drawerContent.setSpacing(false);
		drawerContent.setSizeFull();

		menuPrincipal.setId("menu-tabs");
		configureMenu(menuPrincipal);
		configureMenu(menuDossier);

		menuPrincipal.addSelectedChangeListener(e -> {
			if (e.isFromClient() && e.getSelectedTab() instanceof MenuTab tab) {
				menuDossier.setSelectedTab(null);
				UI.getCurrent().navigate(tab.getRoute());
			}
		});
		menuDossier.addSelectedChangeListener(e -> {
			if (e.isFromClient() && e.getSelectedTab() instanceof MenuTab tab) {
				menuPrincipal.setSelectedTab(null);
				UI.getCurrent().navigate(tab.getRoute());
			}
		});

		dossierHeader.getStyle()
			.set("padding", "var(--lumo-space-m)")
			.set("text-align", "center");
		dossierSection.setWidthFull();
		dossierSection.add(dossierHeader, menuDossier);
	}

	private void configureMenu(final Tabs tabs) {
		tabs.setOrientation(Tabs.Orientation.VERTICAL);
		tabs.setAutoselect(false);
		tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
		tabs.setWidthFull();
		tabs.addClassName("menu-tabs");
	}

	/**
	 * (Re)construit le menu du drawer en fonction du profil de l'utilisateur
	 * et du dossier étudiant éventuellement consulté.
	 */
	public void updateMenu() {
		final Etudiant etudiant = sessionController.getEtudiant();
		final boolean etudiantConnecte = safeIsEtudiant();
		final boolean enseignant = safeIsEnseignant();

		menuPrincipal.removeAll();
		menuDossier.removeAll();
		dossierHeader.removeAll();
		drawerContent.removeAll();

		/* Logo et nom de l'application tout en haut du drawer */
		drawerContent.add(drawerHeader);

		/* Entrées enseignant/gestionnaire (ancienne barre d'onglets Recherche) */
		if (enseignant) {
			menuPrincipal.add(new MenuTab(VaadinIcon.SEARCH, getTranslation("mainUI.rechercherapide.title"), ROUTE_RECHERCHE_RAPIDE));
			menuPrincipal.add(new MenuTab(VaadinIcon.SITEMAP, getTranslation("mainUI.recherchearbo.title"), ROUTE_RECHERCHE_ARBORESCENTE));
			menuPrincipal.add(new MenuTab(VaadinIcon.STAR_O, getTranslation("mainLayout.menu.favoris"), ROUTE_FAVORIS));
		}

		/* Assistance : accessible à tous les profils autorisés */
		menuPrincipal.add(new MenuTab(VaadinIcon.LIFEBUOY, getTranslation("assistanceView.title"), ROUTE_ASSISTANCE));

		/* Administration */
		if (userController.userCanAccessAdminView()) {
			menuPrincipal.add(new MenuTab(VaadinIcon.COG, getTranslation("adminView.title"), ROUTE_ADMIN));
		}

		/* Dossier étudiant : pour l'étudiant connecté ou pour l'enseignant qui consulte un dossier
		 * (équivalent de l'ancien layoutDossierEtudiant) */
		final boolean dossierVisible = etudiant != null && (etudiantConnecte || enseignant);
		if (dossierVisible) {
			buildDossierHeader(etudiant, !etudiantConnecte);
			buildMenuDossier(etudiant, etudiantConnecte);
			/* Animation d'entrée depuis la gauche (rejouée à chaque insertion dans le DOM) */
			dossierSection.removeClassName("dossier-anim-out");
			dossierSection.addClassName("dossier-anim-in");
		}

		/* Ordre d'affichage : le dossier en premier pour un étudiant, en dernier pour un enseignant */
		if (etudiantConnecte) {
			if (dossierVisible) {
				drawerContent.add(dossierSection);
			}
			drawerContent.add(menuPrincipal);
		} else {
			drawerContent.add(menuPrincipal);
			if (dossierVisible) {
				drawerContent.add(dossierSection);
			}
		}

		codEtuMenu = dossierVisible ? etudiant.getCod_etu() : null;
	}

	/**
	 * En-tête du dossier étudiant dans le drawer : photo, statut d'inscription, nom et n° étudiant.
	 */
	private void buildDossierHeader(final Etudiant etudiant, final boolean afficherTitreDossier) {
		if (afficherTitreDossier) {
			/* Séparateur visuel pour démarquer le menu du dossier du menu principal
			 * (marges négatives pour s'étendre sur toute la largeur malgré le padding du conteneur) */
			final Hr separateur = new Hr();
			separateur.getStyle().set("margin", "0 calc(-1 * var(--lumo-space-m)) var(--lumo-space-s)");
			dossierHeader.add(separateur);

			/* Croix de fermeture du dossier, alignée à droite sous le séparateur */
			final Button fermerBtn = new Button(VaadinIcon.CLOSE.create());
			fermerBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			final String fermerLabel = getTranslation("mainLayout.dossier.fermer");
			fermerBtn.getElement().setAttribute("title", fermerLabel);
			fermerBtn.getElement().setAttribute("aria-label", fermerLabel);
			fermerBtn.addClickListener(e -> {
				/* Evite un double déclenchement pendant l'animation de sortie */
				fermerBtn.setEnabled(false);
				fermerDossier();
			});
			final HorizontalLayout fermerBar = new HorizontalLayout(fermerBtn);
			fermerBar.setWidthFull();
			fermerBar.setPadding(false);
			fermerBar.setSpacing(false);
			fermerBar.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
			dossierHeader.add(fermerBar);
		}

		/* Photo de l'étudiant + indicateur d'inscription pour l'année en cours */
		if (StringUtils.hasText(etudiant.getPhoto())) {
			final Image photo = new Image(etudiant.getPhoto(), etudiant.getNom());
			photo.setWidth("120px");
			photo.addClassName("photo-mdw");
			dossierHeader.add(photo);

			String annee = "";
			try {
				annee = Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours());
			} catch (final Exception e) {
				log.debug("Impossible de récupérer l'année universitaire en cours", e);
			}
			final Icon inscritIcon = (etudiant.isInscritPourAnneeEnCours()
				? VaadinIcon.CHECK_CIRCLE
				: VaadinIcon.EXCLAMATION_CIRCLE).create();
			inscritIcon.setSize("var(--lumo-icon-size-s)");
			final String description = getTranslation(etudiant.isInscritPourAnneeEnCours()
				? "mainLayout.etudiant.inscrit"
				: "mainLayout.etudiant.noninscrit", annee);
			inscritIcon.getElement().setAttribute("title", description);
			inscritIcon.getElement().setAttribute("aria-label", description);
			dossierHeader.add(inscritIcon);
		}

		final Span nom = new Span(etudiant.getNom());
		nom.addClassName("font-bold");
		nom.getStyle().set("display", "block");
		final Span codetu = new Span(etudiant.getCod_etu());
		codetu.addClassName("text-secondary");
		codetu.getStyle().set("display", "block");
		dossierHeader.add(nom, codetu);
	}

	/**
	 * Entrées du dossier étudiant (ancien buildMainMenuEtudiant).
	 */
	private void buildMenuDossier(final Etudiant etudiant, final boolean etudiantConnecte) {
		final boolean gestionnaire = userController.isGestionnaire();

		/* Etat-civil */
		menuDossier.add(new MenuTab(VaadinIcon.USER, getTranslation("etatCivilView.title"), ROUTE_ETAT_CIVIL));

		/* Informations annuelles : visibles seulement si l'étudiant en a */
		final boolean affInfosAnnuelles = etudiantConnecte
			|| (gestionnaire ? configController.isAffInfosAnnuellesGestionnaire() : configController.isAffInfosAnnuellesEnseignant());
		if (affInfosAnnuelles && etudiant.getInfosAnnuelles() != null && !etudiant.getInfosAnnuelles().isEmpty()) {
			menuDossier.add(new MenuTab(VaadinIcon.INFO_CIRCLE, getTranslation("informationsAnnuellesView.title"), ROUTE_INFORMATIONS_ANNUELLES));
		}

		/* Adresses */
		final boolean affAdresses = etudiantConnecte
			|| (gestionnaire ? configController.isAffAdressesGestionnaire() : configController.isAffAdressesEnseignant());
		if (affAdresses) {
			menuDossier.add(new MenuTab(VaadinIcon.HOME, getTranslation("adressesView.title"), ROUTE_ADRESSES));
		}

		/* Inscriptions */
		menuDossier.add(new MenuTab(VaadinIcon.FILE_TEXT, getTranslation("inscriptionsView.title"), ROUTE_INSCRIPTIONS));

		/* Calendrier des épreuves */
		final boolean affCalendrier = etudiantConnecte
			? configController.isAffCalendrierEpreuvesEtudiant()
			: (gestionnaire ? configController.isAffCalendrierEpreuvesGestionnaire() : configController.isAffCalendrierEpreuvesEnseignant());
		if (affCalendrier) {
			menuDossier.add(new MenuTab(VaadinIcon.CALENDAR, getTranslation("calendrierView.title"), ROUTE_CALENDRIER));
		}

		/* Notes et résultats */
		final boolean affNotes = etudiantConnecte
			? configController.isAffNotesEtudiant()
			: (gestionnaire ? configController.isAffNotesGestionnaire() : configController.isAffNotesEnseignant());
		if (affNotes) {
			menuDossier.add(new MenuTab(VaadinIcon.LIST, getTranslation("notesView.title"), ROUTE_NOTES));
		}

		MenuTab menuAide = new MenuTab(VaadinIcon.QUESTION_CIRCLE_O, getTranslation("helpWindow.defaultTitle"), null);
		menuAide.getElement().addEventListener("click", event -> afficherAide());
		menuDossier.add(menuAide);

	}

	/* ====================== Cycle de vie / navigation ====================== */

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);

		/* Enregistre l'UI pour la réception de notifications */
		uiController.registerUI(attachEvent.getUI());

		updateMenu();

		/* Affichage du message d'intro si besoin (préférence utilisateur) */
		if (safeIsEnseignant()) {
			afficherMessageIntroEnseignants(false, true);
		} else if (safeIsEtudiant()) {
			afficherMessageIntro(false, true);
		}
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		/* Se désinscrit de la réception de notifications */
		uiController.unregisterUI(detachEvent.getUI());
		super.onDetach(detachEvent);
	}

	/**
	 * Avant chaque navigation : initialise le dossier de l'étudiant connecté
	 * et bloque l'accès si l'application est en maintenance.
	 */
	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		/* On renseigne l'étudiant dont on consulte le dossier (profil étudiant) */
		if (safeIsEtudiant() && sessionController.getEtudiant() == null) {
			sessionController.setEtudiant(new Etudiant(userController.getCodetu()));
			etudiantController.recupererEtatCivil();
		}

		/* Si l'application est en maintenance, on bloque l'accès
		 * (sauf vue admin pour les administrateurs) */
		final String route = event.getLocation().getFirstSegment();
		if (!applicationActive()
			&& !AccesBloqueView.NAME.equals(route)
			&& !(ROUTE_ADMIN.equals(route) && userController.userCanAccessAdminView())) {
			event.forwardTo(AccesBloqueView.NAME);
		}
	}

	/**
	 * Après chaque navigation : met à jour le menu si le dossier consulté a changé,
	 * affiche le titre de la vue dans la navbar et surligne l'entrée de menu
	 * correspondant à la vue affichée.
	 */
	@Override
	public void afterNavigation(final AfterNavigationEvent event) {
		final String codEtuCourant = sessionController.getEtudiant() != null
			? sessionController.getEtudiant().getCod_etu()
			: null;
		if (!Objects.equals(codEtuCourant, codEtuMenu)) {
			updateMenu();
		}

		/* Titre de la vue affichée */
		viewTitle.setText(resolveViewTitle(event));

		final String route = event.getLocation().getFirstSegment();

		/* Mémorise la dernière vue consultée hors du dossier étudiant, pour y
		 * rediriger l'utilisateur à la fermeture du dossier (croix du drawer) */
		if (!ROUTES_DOSSIER.contains(route)) {
			derniereLocationHorsDossier = event.getLocation();
		}

		selectTab(menuPrincipal, route);
		selectTab(menuDossier, route);
	}

	/**
	 * Ferme le dossier étudiant consulté : joue l'animation de sortie vers la
	 * gauche puis, à la fin de l'animation, vide la session et redirige vers la
	 * dernière vue consultée avant l'ouverture du dossier (à défaut l'accueil).
	 * La navigation déclenche {@link #afterNavigation} qui reconstruit le menu
	 * sans la section dossier.
	 */
	private void fermerDossier() {
		dossierSection.removeClassName("dossier-anim-in");
		dossierSection.addClassName("dossier-anim-out");

		/* La fermeture effective attend la fin de l'animation CSS */
		final Registration[] holder = new Registration[1];
		holder[0] = dossierSection.getElement().addEventListener("animationend", e -> {
			holder[0].remove();
			dossierSection.removeClassName("dossier-anim-out");
			sessionController.setEtudiant(null);
			if (derniereLocationHorsDossier != null) {
				UI.getCurrent().navigate(derniereLocationHorsDossier.getPath(), derniereLocationHorsDossier.getQueryParameters());
			} else {
				UI.getCurrent().navigate("");
			}
		});
	}

	/**
	 * Titre de la vue affichée : le {@code getPageTitle()} de la vue si elle
	 * implémente {@link HasDynamicTitle}, le libellé d'un MenuTab pointant sur
	 * la route courante sinon, la clé {@code <route>.title} en dernier recours.
	 */
	private String resolveViewTitle(final AfterNavigationEvent event) {
		/* Cas nominal : la vue expose son titre (getPageTitle) */
		if (event.getActiveChain().size() > 1
			&& event.getActiveChain().get(1) instanceof HasDynamicTitle view) {
			return view.getPageTitle();
		}
		final String route = event.getLocation().getFirstSegment();
		/* Dernier recours : clé <route>.title (vues d'erreur/d'accès, HomeView) */
		final String title = getTranslation(route + ".title");
		return title.startsWith(route) ? "" : title;
	}

	private void selectTab(final Tabs tabs, final String route) {
		final Optional<MenuTab> tab = tabsStream(tabs)
			.filter(t -> Objects.equals(t.getRoute(), route))
			.findFirst();
		tabs.setSelectedTab(tab.orElse(null));
	}

	private Stream<MenuTab> tabsStream(final Tabs tabs) {
		return tabs.getChildren()
			.filter(MenuTab.class::isInstance)
			.map(MenuTab.class::cast);
	}

	/* ====================== Services aux vues ====================== */

	/**
	 * Affiche la fenêtre d'attente (ancien MainUI.startBusyIndicator).
	 */
	public void startBusyIndicator() {
		loadingIndicatorWindow.open();
	}

	/**
	 * Ferme la fenêtre d'attente (ancien MainUI.stopBusyIndicator).
	 */
	public void stopBusyIndicator() {
		loadingIndicatorWindow.close();
	}

	/**
	 * Affichage du message d'intro aux enseignants (utilisé par AssistanceView).
	 * @param displayForced affiche le message même si l'utilisateur a demandé à ne plus le voir
	 * @param displayCheckBox propose la case "Ne plus afficher ce message"
	 */
	public void afficherMessageIntroEnseignants(final boolean displayForced, final boolean displayCheckBox) {
		afficherMessageIntro(getTranslation("helpWindow.text.enseignant"), displayForced, displayCheckBox);
	}

	/**
	 * Affichage du message d'intro adapté au profil de l'utilisateur courant.
	 */
	public void afficherMessageIntro(final boolean displayForced, final boolean displayCheckBox) {
		afficherMessageIntro(getTranslation(safeIsEtudiant() ? "helpWindow.text.etudiant" : "helpWindow.text.enseignant"),
			displayForced, displayCheckBox);
	}

	private void afficherMessageIntro(final String text, final boolean displayForced, final boolean displayCheckBox) {
		/* Préférence utilisateur pour l'affichage du message d'introduction */
		final String val = userController.getPreference(Utils.SHOW_MESSAGE_INTRO_PREFERENCE);
		final boolean afficherMessage = !StringUtils.hasText(val) || Boolean.parseBoolean(val);

		if (displayForced || afficherMessage) {
			final HelpWindow hw = new HelpWindow(text, getTranslation("helpWindow.defaultTitle"), displayCheckBox);
			hw.addOpenedChangeListener(e -> {
				/* À la fermeture : mémorise le choix de ne plus afficher le message */
				if (!e.isOpened() && displayCheckBox && Boolean.TRUE.equals(hw.getCheckBox().getValue())) {
					userController.updatePreference(Utils.SHOW_MESSAGE_INTRO_PREFERENCE, "false");
				}
			});
			hw.open();
		}
	}

	/**
	 * Affiche la fenêtre d'aide (ancien bouton "Aide" du menu étudiant).
	 */
	private void afficherAide() {
		final String text = getTranslation(safeIsEtudiant() ? "helpWindow.text.etudiant" : "helpWindow.text.enseignant");
		new HelpBasicWindow(text, getTranslation("helpWindow.defaultTitle"), true).open();
	}

	/* ====================== Helpers ====================== */

	private boolean safeIsEtudiant() {
		try {
			return userController.getCurrentUser() != null && userController.isEtudiant();
		} catch (final Exception e) {
			return false;
		}
	}

	private boolean safeIsEnseignant() {
		try {
			return userController.getCurrentUser() != null && userController.isEnseignant();
		} catch (final Exception e) {
			return false;
		}
	}

	/**
	 * Vérifie que l'application n'est pas en maintenance pour le profil courant
	 * (reprise de l'ancien MainUI.applicationActive()).
	 */
	private boolean applicationActive() {
		final boolean etudiant = safeIsEtudiant();
		final boolean enseignant = safeIsEnseignant();
		final boolean gestionnaire = userController.getCurrentUser() != null && userController.isGestionnaire();
		return configController.isApplicationActive()
			&& ((etudiant && configController.isPartieEtudiantActive())
				|| (!enseignant && !etudiant && !gestionnaire)
				|| (enseignant && configController.isPartieEnseignantActive())
				|| (gestionnaire && configController.isProfilGestionnaireActif()));
	}

	/**
	 * Entrée du menu : un onglet portant la route de la vue cible.
	 */
	private static class MenuTab extends Tab {

		@Getter
		private final String route;

		MenuTab(final VaadinIcon icon, final String label, final String route) {
			this.route = route;
			//setLabel(label);
			final Icon i = icon.create();
			i.setSize("var(--lumo-icon-size-s)");
			i.getStyle().set("margin-inline-end", "var(--lumo-space-s)");
			add(i, new Span(label));
		}
	}
}
