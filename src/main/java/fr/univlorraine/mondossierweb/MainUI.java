package fr.univlorraine.mondossierweb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.DiscoveryNavigator;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ClassResource;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.UiController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.dao.IDaoCodeLoginEtudiant;
import fr.univlorraine.mondossierweb.entities.Favoris;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.AccesBloqueView;
import fr.univlorraine.mondossierweb.views.AccesRefuseView;
import fr.univlorraine.mondossierweb.views.AdminView;
import fr.univlorraine.mondossierweb.views.AdressesView;
import fr.univlorraine.mondossierweb.views.CalendrierView;
import fr.univlorraine.mondossierweb.views.ErreurView;
import fr.univlorraine.mondossierweb.views.EtatCivilView;
import fr.univlorraine.mondossierweb.views.FavorisView;
import fr.univlorraine.mondossierweb.views.InformationsAnnuellesView;
import fr.univlorraine.mondossierweb.views.InscriptionsView;
import fr.univlorraine.mondossierweb.views.ListeInscritsView;
import fr.univlorraine.mondossierweb.views.NotesView;
import fr.univlorraine.mondossierweb.views.RechercheArborescenteView;
import fr.univlorraine.mondossierweb.views.RechercheRapideView;
import fr.univlorraine.mondossierweb.views.windows.HelpBasicWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpWindow;
import fr.univlorraine.tools.vaadin.SpringErrorViewProvider;

/**
 * Application Vaadin
 * 
 * 
 */
@Component @Scope("prototype")
@Theme("valo-ul")
@StyleSheet("mainView.css")
public class MainUI extends GenericUI {
	private static final long serialVersionUID = -4633936971448921781L;

	private Logger LOG = LoggerFactory.getLogger(MainUI.class);

	/* Redirige java.util.logging vers SLF4j */
	static {
		SLF4JBridgeHandler.install();
	}

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserController userController;
	@Resource
	private transient UiController uiController;
	@Resource
	private transient RechercheArborescenteController rechercheArborescenteController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource(name="codetuFromLoginDao")
	private transient IDaoCodeLoginEtudiant daoCodeLoginEtudiant;
	@Resource
	private transient ListeInscritsController listeInscritsController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient ConfigController configController;



	@Resource
	private AdminView adminView;

	@Resource
	private RechercheRapideView rechercheRapideView;

	@Resource
	private RechercheArborescenteView rechercheArborescenteView;

	@Resource
	private ListeInscritsView listeInscritsView;

	@Resource
	private FavorisView favorisView;

	//rang de l'onglet contenant le dossier etudiant dans le conteneur principal
	private int rangTabDossierEtudiant;

	//rang de l'onglet contenant la recherche dans le conteneur principal
	private int rangTabRecherche;



	/* Composants */
	private VerticalLayout mainVerticalLayout=new VerticalLayout();
	private CssLayout mainMenu = new CssLayout();
	private CssLayout menuLayout = new CssLayout(mainMenu);
	private CssLayout contentLayout = new CssLayout();
	private HorizontalLayout layoutDossierEtudiant = new HorizontalLayout(menuLayout, contentLayout);
	private TabSheet tabSheetGlobal = new TabSheet();
	private VerticalLayout layoutOngletRecherche;
	private TabSheet tabSheetEnseignant= new TabSheet();
	//private CssLayout contentTabSheetEnseignantLayout = new CssLayout();



	/** Gestionnaire de vues étudiant*/
	@Getter
	private DiscoveryNavigator navigator = new DiscoveryNavigator(this, contentLayout);



	/** Gestionnaire de vues */
	/*@Getter
	private DiscoveryNavigator navigatorEnseignant = new DiscoveryNavigator(this, contentTabSheetEnseignantLayout);*/





	/** Noms des vues et boutons du menu associés */
	private Map<String, Button> viewButtons = new HashMap<>();

	/** Noms des vues et index du tab associé */
	private Map<String, Integer> viewEnseignantTab = new HashMap<>();

	/**
	 * @see com.vaadin.ui.UI#getCurrent()
	 */
	public static MainUI getCurrent() {
		return (MainUI) UI.getCurrent();
	}

	/**
	 * @see com.vaadin.ui.UI#init(com.vaadin.server.VaadinRequest)
	 */
	@Override
	protected void init(VaadinRequest request) {
		VaadinSession.getCurrent().setErrorHandler(e -> {
			Throwable cause = e.getThrowable();
			while (cause instanceof Throwable) {
				/* Gère les accès non autorisés */
				if (cause instanceof AccessDeniedException) {
					Notification.show(cause.getMessage(), Type.ERROR_MESSAGE);
					displayViewFullScreen(AccesRefuseView.NAME);
					return;
				}
				cause = cause.getCause();
			}
			/* Traite les autres erreurs normalement */
			displayViewFullScreen(ErreurView.NAME);
			//DefaultErrorHandler.doDefault(e);
		});

		/* Affiche le nom de l'application dans l'onglet du navigateur */
		getPage().setTitle(environment.getRequiredProperty("app.name"));




		/* Construit le gestionnaire de vues */
		navigator.setErrorProvider(new SpringErrorViewProvider(ErreurView.class, navigator));
		navigator.addViewChangeListener(new ViewChangeListener() {
			private static final long serialVersionUID = 7905379446201794289L;

			private static final String SELECTED_ITEM = "selected";

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				viewButtons.values().forEach(button -> button.removeStyleName(SELECTED_ITEM));
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				Button button = viewButtons.get(event.getViewName());
				if (button instanceof Button) {
					button.addStyleName(SELECTED_ITEM);
				}
			}
		});


		/* Initialise Google Analytics */
		googleAnalyticsTracker.setAccount(environment.getProperty("analytics.account"));
		/* Suis les changements de vue du navigator */
		googleAnalyticsTracker.trackNavigator(navigator);

		setContent(mainVerticalLayout);

		if(userController.isEnseignant() || userController.isEtudiant()){



			/* Parametre le layoutDossierEtudiant */
			menuLayout.setPrimaryStyleName(ValoTheme.MENU_ROOT);
			contentLayout.addStyleName("v-scrollable");
			contentLayout.setSizeFull();
			layoutDossierEtudiant.setExpandRatio(contentLayout, 1);
			layoutDossierEtudiant.setSizeFull();


			//Si user enseignant
			if(userController.isEnseignant()){
				//On consultera les notes en vue enseignant
				vueEnseignantNotesEtResultats=true;

				/* Construit le menu horizontal pour les enseignants */
				tabSheetGlobal.setSizeFull();
				tabSheetGlobal.addStyleName(ValoTheme.TABSHEET_FRAMED);

				//ajout de l'onglet Admin
				rangTabRecherche=0;
				rangTabDossierEtudiant = 1;
				/*if(userController.isAdmin()){
					tabSheetGlobal.addTab(adminView, "Admin", FontAwesome.WRENCH);
					rangTabRecherche = 1;
					rangTabDossierEtudiant = 2;
				}*/
				//ajout de l'onglet recherche
				layoutOngletRecherche = new VerticalLayout();
				ajoutOngletRecherche();
				layoutOngletRecherche.setSizeFull();
				tabSheetGlobal.addTab(layoutOngletRecherche, "Recherche", FontAwesome.SEARCH);

				//ajout de l'onglet dossier étudiant
				addTabDossierEtudiant();
				tabSheetGlobal.addStyleName("right-aligned-tabs");

				//Le menu horizontal pour les enseignants est définit comme étant le contenu de la page
				mainVerticalLayout.addComponent(tabSheetGlobal);
				mainVerticalLayout.setSizeFull();
				mainVerticalLayout.setExpandRatio(tabSheetGlobal, 1);
			}else{
				//On consultera les notes en vue etudiant
				vueEnseignantNotesEtResultats=false;
				//User Etudiant
				//Le Dossier est définit comme étant le contenu de la page
				mainVerticalLayout.addComponent(layoutDossierEtudiant);
				mainVerticalLayout.setSizeFull();
				mainVerticalLayout.setExpandRatio(layoutDossierEtudiant, 1);

				etudiant = new Etudiant(daoCodeLoginEtudiant.getCodEtuFromLogin(userController.getCurrentUserName()));
				LOG.debug("MainUI etudiant : "+MainUI.getCurrent().getEtudiant().getCod_etu());
				etudiantController.recupererEtatCivil();
				/*etudiantController.recupererInscriptions();
				etudiantController.recupererCalendrierExamens();*/
				buildMainMenuEtudiant();
			}

			//setContent(layout);




			/* Contruit le menu */
			//buildMainMenuEtudiant();



			/* Enregistre l'UI pour la réception de notifications */
			uiController.registerUI(this);

			/* Résout la vue à afficher */
			String fragment = Page.getCurrent().getUriFragment();
			//PROBLEME DU F5 : on passe ici que quand on reinitialise l'UI. 
			//On ne peut donc pas rediriger vers des vu qui utilise des variables non initialisées (ex : Main.getCurrent.getEtudiant)
			//if (fragment == null || fragment.isEmpty()) {
			if(!configController.isApplicationActive()){
				displayViewFullScreen(AccesBloqueView.NAME);
			}else{

				if(userController.isEnseignant()){
					List<Favoris> lfav = favorisController.getFavoris();
					if(lfav!=null && lfav.size()>0){
						navigator.navigateTo(FavorisView.NAME);
						navigateToFavoris();
						afficherMessageIntroEnseignants();

					}else{
						navigator.navigateTo(RechercheRapideView.NAME);
						navigateToRechercheRapide();
						afficherMessageIntroEnseignants();
					}
				}else{
					if(userController.isEtudiant()){
						navigator.navigateTo(EtatCivilView.NAME);
						afficherMessageIntroEtudiants();
					}else{
						displayViewFullScreen(ErreurView.NAME);
					}
				}
			}
		}else{
			displayViewFullScreen(AccesRefuseView.NAME);
		}
	}

	private void displayViewFullScreen(String view){
		setContent(contentLayout);
		navigator.navigateTo(view);
	}

	private void afficherMessageIntroEtudiants() {
		afficherMessageIntro(applicationContext.getMessage("helpWindow.text.etudiant", null, getLocale()));

	}

	private void afficherMessageIntroEnseignants() {
		afficherMessageIntro(applicationContext.getMessage("helpWindow.text.enseignant", null, getLocale()));

	}

	private void afficherMessageIntro(String text){
		//Recuperer dans la base si l'utilisateur a demandé à ne plus afficher le message
		String val  = userController.getPreference(Utils.SHOW_MESSAGE_INTRO_PREFERENCE);
		boolean afficherMessage = true;
		if(StringUtils.hasText(val)){
			afficherMessage = Boolean.valueOf(val);
		}

		if(afficherMessage){
			HelpWindow hbw = new HelpWindow(text,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),true);
			hbw.addCloseListener(g->{
				boolean choix = hbw.getCheckBox().getValue();
				//Test si l'utilisateur a coché la case pour ne plus afficher le message
				if(choix){
					//mettre a jour dans la base de données
					userController.updatePreference(Utils.SHOW_MESSAGE_INTRO_PREFERENCE, "false");
				}
			});
			UI.getCurrent().addWindow(hbw);
		}
	}

	private void ajoutOngletRecherche() {
		tabSheetEnseignant.setSizeFull();


		tabSheetEnseignant.addTab(rechercheRapideView, "Recherche Rapide", FontAwesome.SEARCH);
		//tabSheetEnseignant.addTab(contentTabSheetEnseignantLayout, "Recherche Rapide", FontAwesome.SEARCH);
		viewEnseignantTab.put(rechercheRapideView.NAME, 0);

		tabSheetEnseignant.addTab(rechercheArborescenteView, "Recherche Arborescente", FontAwesome.SITEMAP);
		//tabSheetEnseignant.addTab(contentTabSheetEnseignantLayout, "Recherche Arborescente", FontAwesome.SITEMAP);
		viewEnseignantTab.put(rechercheArborescenteView.NAME, 1);

		tabSheetEnseignant.addTab(favorisView, "Favoris", FontAwesome.STAR_O);
		viewEnseignantTab.put(favorisView.NAME, 2);
		tabSheetEnseignant.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {

			public void selectedTabChange(SelectedTabChangeEvent event){
				// Find the tabsheet
				TabSheet tabsheet = event.getTabSheet();

				// Find the tab (here we know it's a layout)
				View vue = (View) tabsheet.getSelectedTab();
				if (vue instanceof FavorisView){
					favorisView.init();
				}
				if (vue instanceof ListeInscritsView){
					listeInscritsView.refresh();
				}
				if (vue instanceof RechercheArborescenteView){
					rechercheArborescenteView.refresh();
				}


			}
		});
		//tabSheetEnseignant.addTab(contentTabSheetEnseignantLayout, "Favoris", FontAwesome.STAR_O);

		addTabListeInscrits();


		tabSheetEnseignant.addStyleName("left-aligned-tabs");
		layoutOngletRecherche.addComponent(tabSheetEnseignant);
		layoutOngletRecherche.setSizeFull();
		layoutOngletRecherche.setExpandRatio(tabSheetEnseignant, 1);

	}




	private void addTabListeInscrits() {
		tabSheetEnseignant.addTab(listeInscritsView, "Liste d'inscrits ", FontAwesome.USERS);
		//test si c'est la premiere fois qu'on ajoute l'onglet
		if(!viewEnseignantTab.containsKey(listeInscritsView.NAME)){
			viewEnseignantTab.put(listeInscritsView.NAME, 3);
		}
		tabSheetEnseignant.getTab(3).setVisible(false);
		tabSheetEnseignant.getTab(3).setClosable(true);

	}

	private void addTabDossierEtudiant() {
		tabSheetGlobal.addTab(layoutDossierEtudiant, "Dossier", FontAwesome.USER);
		tabSheetGlobal.getTab(rangTabDossierEtudiant).setVisible(false);
		tabSheetGlobal.getTab(rangTabDossierEtudiant).setClosable(true);

	}

	public void navigateToRechercheArborescenteTab(){
		tabSheetEnseignant.setSelectedTab(2);
	}

	private void buildMainMenuEtudiant() {

		if(etudiant!=null){

			mainMenu.setPrimaryStyleName(ValoTheme.MENU_PART);
			mainMenu.setWidth("233px");


			HorizontalLayout fotoLayout = new HorizontalLayout();

			fotoLayout.addStyleName(ValoTheme.MENU_SUBTITLE);
			fotoLayout.setWidth(213, Unit.PIXELS);
			fotoLayout.setMargin(true);




			Button etuInscritBtn = new Button("", FontAwesome.CHECK_CIRCLE);
			etuInscritBtn.setPrimaryStyleName(ValoTheme.BUTTON_BORDERLESS);
			if(etudiant.isInscritPourAnneeEnCours()){
				etuInscritBtn.setDescription("Inscrit pour l'année universitaire "+Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours(this)));
			}else{
				etuInscritBtn.setIcon(FontAwesome.EXCLAMATION_CIRCLE);
				etuInscritBtn.setDescription("Non Inscrit pour l'année universitaire "+Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours(this)));
			}

			fotoLayout.addComponent(new HorizontalLayout());

			/* Photo étudiant */
			if(etudiant.getPhoto()!=null){
				Image logo = new Image(null, new ClassResource("/images/UL.png"));
				Image fotoEtudiant = new Image(null, new ExternalResource(etudiant.getPhoto()));
				fotoEtudiant.setWidth("120px");
				fotoEtudiant.setHeight("153px");
				fotoLayout.addComponent(fotoEtudiant);
				fotoLayout.setComponentAlignment(fotoEtudiant, Alignment.MIDDLE_CENTER);
				fotoLayout.setExpandRatio(fotoEtudiant, 1);
			}
			fotoLayout.addComponent(etuInscritBtn);


			mainMenu.addComponent(fotoLayout);



			/* Titre: Username */
			//Label usernameLabel = new Label(userController.getCurrentUserName());
			Label usernameLabel = new Label(etudiant.getNom()+"<br />"+etudiant.getCod_etu(), ContentMode.HTML);
			usernameLabel.addStyleName(ValoTheme.MENU_SUBTITLE);
			usernameLabel.setSizeUndefined();
			mainMenu.addComponent(usernameLabel);

			/* Etat Civil */
			addItemMenu("Etat-civil", EtatCivilView.NAME, FontAwesome.USER);

			//info annuelles visibles que si étudiant inscrit pour l'année en cours
			if(etudiant.isInscritPourAnneeEnCours()){
				/* Informations Annuelles */
				addItemMenu("Informations annuelles", InformationsAnnuellesView.NAME, FontAwesome.INFO_CIRCLE);
			}

			/* Adresses */
			addItemMenu(applicationContext.getMessage(AdressesView.NAME + ".title", null, getLocale()), AdressesView.NAME, FontAwesome.HOME);

			/* Inscriptions */
			addItemMenu("Inscriptions", InscriptionsView.NAME, FontAwesome.FILE_TEXT);


			/* Calendrier */
			addItemMenu("Calendrier des épreuves", CalendrierView.NAME, FontAwesome.CALENDAR);


			/* Bouton vers la vue Admin */
			if (userController.canCurrentUserAccessView(AdminView.class)) {
				//	addItemMenu(applicationContext.getMessage(AdminView.NAME + ".title", null, getLocale()), AdminView.NAME, FontAwesome.WRENCH);
				/* Activation des profils: décommenter cette zone */
				/*
			addItemMenu(null, applicationContext.getMessage("admin.tabProfils", null, getLocale()), SecurityView.NAME, FontAwesome.LOCK);
				 */
			}

			/* Bouton permettant de rétablir l'utilisateur ayant changé de rôle */
			if (userController.isUserSwitched()) {
				Button btnSwitchUserBack = new Button(applicationContext.getMessage("admin.switchUser.btnSwitchUserBack", null, getLocale()), FontAwesome.UNDO);
				btnSwitchUserBack.setPrimaryStyleName(ValoTheme.MENU_ITEM);
				btnSwitchUserBack.addClickListener(e -> userController.switchBackToPreviousUser());
				mainMenu.addComponent(btnSwitchUserBack);
			}





			/* Notes et Résultats */
			addItemMenu(applicationContext.getMessage(NotesView.NAME + ".title", null, getLocale()), NotesView.NAME, FontAwesome.LIST);

			CssLayout bottomMainMenu1 = new CssLayout();
			bottomMainMenu1.setStyleName(ValoTheme.MENU_SUBTITLE);
			bottomMainMenu1.setSizeUndefined();
			mainMenu.addComponent(bottomMainMenu1);



			/* Assistance */
			Button helpBtn = new Button("Assistance", FontAwesome.SUPPORT);
			helpBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			helpBtn.addClickListener(e -> {UI.getCurrent().addWindow(new HelpBasicWindow(applicationContext.getMessage("helpWindow.text.etudiant", null, getLocale()),applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale())));});
			mainMenu.addComponent(helpBtn);

			/* Deconnexion */
			/*	Button decoBtn = new Button("Déconnexion", FontAwesome.SIGN_OUT);
			decoBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			decoBtn.addClickListener(e -> getUI().getPage().setLocation("j_spring_security_logout"));
			mainMenu.addComponent(decoBtn);*/

			CssLayout bottomMainMenu = new CssLayout();
			bottomMainMenu.setStyleName(ValoTheme.MENU_SUBTITLE);
			bottomMainMenu.setSizeUndefined();
			mainMenu.addComponent(bottomMainMenu);



		}
	}

	private void addItemMenu(String caption, String viewName, com.vaadin.server.Resource icon) {
		Button itemBtn = new Button(caption, icon);
		itemBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);
		itemBtn.addClickListener(e -> navigator.navigateTo(viewName));
		viewButtons.put(viewName, itemBtn);
		mainMenu.addComponent(itemBtn);
	}

	/**
	 * @see com.vaadin.ui.UI#detach()
	 */
	@Override
	public void detach() {
		/* Se désinscrit de la réception de notifications */
		uiController.unregisterUI(this);

		super.detach();
	}

	public void navigateToRechercheArborescente(Map<String, String> parameterMap) {
		int numtab = viewEnseignantTab.get(rechercheArborescenteView.NAME);
		if(parameterMap!=null){
			rechercheArborescenteView.initFromParameters(parameterMap);
		}
		tabSheetEnseignant.setSelectedTab(numtab);
	}

	public void navigateToListeInscrits(Map<String, String> parameterMap) {
		int numtab = viewEnseignantTab.get(listeInscritsView.NAME);
		if(parameterMap!=null){
			listeInscritsController.recupererLaListeDesInscrits(parameterMap, null, this);
			listeInscritsView.initListe();
		}
		//Si l'onglet a été closed
		if(tabSheetEnseignant.getTab(numtab)==null){
			addTabListeInscrits();
		}

		tabSheetEnseignant.getTab(numtab).setVisible(true);

		tabSheetEnseignant.setSelectedTab(numtab);
	}

	public void navigateToFavoris() {
		int numtab = viewEnseignantTab.get(favorisView.NAME);
		tabSheetEnseignant.getTab(numtab).setVisible(true);
		tabSheetEnseignant.setSelectedTab(numtab);
		tabSheetGlobal.setSelectedTab(rangTabRecherche);
	}

	public void navigateToRechercheRapide() {
		int numtab = viewEnseignantTab.get(rechercheRapideView.NAME);
		tabSheetEnseignant.getTab(numtab).setVisible(true);
		tabSheetEnseignant.setSelectedTab(numtab);
		tabSheetGlobal.setSelectedTab(rangTabRecherche);
	}

	public void navigateToDossierEtudiant(Map<String, String> parameterMap) {
		if(parameterMap!=null){
			//listeInscritsView.initFromParameters(parameterMap);
		}
		//Si l'onglet a été closed
		if(tabSheetGlobal.getTab(rangTabDossierEtudiant)==null){
			addTabDossierEtudiant();
		}
		if(mainMenu!=null){
			mainMenu.removeAllComponents();
		}
		buildMainMenuEtudiant();
		tabSheetGlobal.getTab(rangTabDossierEtudiant).setVisible(true);

		tabSheetGlobal.setSelectedTab(rangTabDossierEtudiant);

		navigator.navigateTo(EtatCivilView.NAME);
	}





}
