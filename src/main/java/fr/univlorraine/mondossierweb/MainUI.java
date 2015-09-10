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
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Page.UriFragmentChangedEvent;
import com.vaadin.server.Page.UriFragmentChangedListener;
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
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UiController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.dao.IDaoCodeLoginEtudiant;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.AccesBloqueView;
import fr.univlorraine.mondossierweb.views.AccesRefuseView;
import fr.univlorraine.mondossierweb.views.AdminView;
import fr.univlorraine.mondossierweb.views.AdressesView;
import fr.univlorraine.mondossierweb.views.AssistanceView;
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
import fr.univlorraine.mondossierweb.views.windows.LoadingIndicatorWindow;
import fr.univlorraine.tools.vaadin.GoogleAnalyticsTracker;
import fr.univlorraine.tools.vaadin.LogAnalyticsTracker;
import fr.univlorraine.tools.vaadin.PiwikAnalyticsTracker;
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
	@Resource
	private transient ListeInscritsController listeInscritsController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient ConfigController configController;




	@Resource
	private AdminView adminView;

	@Resource
	private AssistanceView assistanceView;

	@Resource
	private RechercheRapideView rechercheRapideView;

	@Resource
	private RechercheArborescenteView rechercheArborescenteView;

	@Resource
	private ListeInscritsView listeInscritsView;

	@Resource
	private FavorisView favorisView;

	private LoadingIndicatorWindow loadingIndicatorWindow = new LoadingIndicatorWindow();

	//rang de l'onglet contenant le dossier etudiant dans le conteneur principal
	private int rangTabDossierEtudiant;

	//tab Dossier Etudiant
	private Tab tabDossierEtu;

	//rang de l'onglet contenant la recherche dans le conteneur principal
	private int rangTabRecherche;

	//Le composant principal de la page (contient tabSheetGlobal ou layoutDossierEtudiant en fonction du type de l'utilisateur)
	private VerticalLayout mainVerticalLayout=new VerticalLayout();

	//Le menu de la partie "dossier étudiant"
	private CssLayout mainMenu = new CssLayout();

	//Le layout contenant le menu
	private CssLayout menuLayout = new CssLayout(mainMenu);

	//Contenu de la partie "dossier étudiant" contentLayout affiche la vue à afficher dans le dossier via le "navigator"
	private CssLayout contentLayout = new CssLayout();

	//Layout principal de la partie "dossier étudiant" : contient le menu et le contentlayout
	private HorizontalLayout layoutDossierEtudiant = new HorizontalLayout(menuLayout, contentLayout);

	//le tabSheet global affiché aux enseignants (contient les onglets Recherche et Dossier)
	private TabSheet tabSheetGlobal = new TabSheet();

	//Layout de l'onglet Recherche de tabSheetGlobal
	private VerticalLayout layoutOngletRecherche;

	//Le sous menu Recherche affiché aux enseignants (affiche les onglets recherche rapide, rechercher arbo, liste inscrits, favoris)
	private TabSheet tabSheetEnseignant= new TabSheet();

	// Gestionnaire de vues de la partie "Dossier étudiant"
	@Getter
	private DiscoveryNavigator navigator = new DiscoveryNavigator(this, contentLayout);

	// Noms des vues et boutons du menu associés 
	private Map<String, Button> viewButtons = new HashMap<>();

	// Noms des vues et index du tab associé 
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

		LOG.debug("init(); mainUI");

		//Gestion des erreurs
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
			// Traite les autres erreurs normalement 
			LOG.error(e.getThrowable().toString(), e.getThrowable());
			// Affiche de la vue d'erreur
			displayViewFullScreen(ErreurView.NAME);
			//DefaultErrorHandler.doDefault(e);
		});

		// Affiche le nom de l'application dans l'onglet du navigateur 
		getPage().setTitle(environment.getRequiredProperty("app.name"));

		//Gestion de l'acces a un dossier précis via url deepLinking (ne peut pas être fait dans navigator 
		//car le fragment ne correspond pas à une vue existante)
		getPage().addUriFragmentChangedListener(new UriFragmentChangedListener() {
			public void uriFragmentChanged(UriFragmentChangedEvent source) {

				//Si l'application est en maintenance on bloque l'accès
				if(!applicationActive() &&
						!source.getUriFragment().contains(AccesBloqueView.NAME) &&
						!(source.getUriFragment().contains(AdminView.NAME) && userController.isAdmin())){

					displayViewFullScreen(AccesBloqueView.NAME);
				}else{

					if(source.getUriFragment().contains("accesDossierEtudiant") 
							&& userController.isEnseignant()){
						rechercheController.accessToDossierEtudiantDeepLinking(source.getUriFragment());

					}/*else{
						if(source.getUriFragment().contains("accesNotesEtudiant") 
								&& userController.isEnseignant()){
							rechercheController.accessToDossierEtudiantDeepLinking(source.getUriFragment());
							navigator.navigateTo(NotesView.NAME);
						}
					}*/


				}
			}
		});

		/* Construit le gestionnaire de vues utilisé par la barre d'adresse et pour naviguer dans le dossier d'un étudiant */
		navigator.setErrorProvider(new SpringErrorViewProvider(ErreurView.class, navigator));
		navigator.addViewChangeListener(new ViewChangeListener() {
			private static final long serialVersionUID = 7905379446201794289L;

			private static final String SELECTED_ITEM = "selected";

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {

				//Avant de se rendre sur une vue, on supprime le style "selected" des objets du menu
				viewButtons.values().forEach(button -> button.removeStyleName(SELECTED_ITEM));

				//Si on tente d'accéder à la vue admin et que l'utilisateur est admin
				if(event.getViewName().equals(AdminView.NAME) && userController.isAdmin()){
					//Afficher la vue admin
					setContent(adminView);
					return true;
				}

				//Si l'application est en maintenance on bloque l'accès
				if(!applicationActive() && !event.getViewName().equals(AccesBloqueView.NAME)){
					displayViewFullScreen(AccesBloqueView.NAME);
					return false;
				}

				//On bloque l'accès aux vues mobile
				if(!Utils.isViewDesktop(event.getViewName())){
					return false;
				}
				//On bloque l'accès aux vues enseignants
				if(Utils.isViewEnseignant(event.getViewName())){
					//Si utilisateur n'est pas enseignant
					if(!userController.isEnseignant()){
						//acces bloque
						return false;
					}else{
						//Affichage de la vue enseignant demandée
						if(event.getViewName().equals(FavorisView.NAME)){
							navigateToFavoris();
							return true;
						}
						if(event.getViewName().equals(ListeInscritsView.NAME)){
							navigateToListeInscrits(null);
							return true;
						}
						if(event.getViewName().equals(RechercheRapideView.NAME)){
							navigateToRechercheRapide();
							return true;
						}
						if(event.getViewName().equals(RechercheArborescenteView.NAME)){
							navigateToRechercheArborescente(null);
							return true;
						}

						return false; //la vue enseignant demandée n'est pas géré (ex :vue mobile appelée depuis la version desktop)
					}
				}

				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {

				//On récupère l'élément du menu concerné par la vue à afficher
				Button button = viewButtons.get(event.getViewName());

				if (button instanceof Button) {
					//on applique le style "selected" sur l'objet du menu concerné par la vue affichée
					button.addStyleName(SELECTED_ITEM);
				}
			}
		});


		//init du tracker
		initAnalyticsTracker();




		//mainVerticalLayout est le contenu principal de la page
		setContent(mainVerticalLayout);

		//Si utilisateur enseignant ou étudiant
		if(userController.isEnseignant() || userController.isEtudiant()){

			if(!applicationActive()){
				displayViewFullScreen(AccesBloqueView.NAME);
			}else{
				//On récupère l'IP du client
				GenericUI.getCurrent().getIpClient();
				/* Parametre le layoutDossierEtudiant */
				menuLayout.setPrimaryStyleName(ValoTheme.MENU_ROOT);
				//Le contentLayout est scrollable si besoin
				contentLayout.addStyleName("v-scrollable");
				//contentLayout prend toute la place possible
				contentLayout.setSizeFull();
				//le contentLayout prend toute la place disponible dans le layoutDossierEtudiant
				layoutDossierEtudiant.setExpandRatio(contentLayout, 1);
				//layoutDossierEtudiant prend toute la place possible
				layoutDossierEtudiant.setSizeFull();


				//Si user enseignant
				if(userController.isEnseignant()){
					//On consultera les notes en vue enseignant
					vueEnseignantNotesEtResultats=true;

					//Construit le menu horizontal pour les enseignants
					tabSheetGlobal.setSizeFull();
					tabSheetGlobal.addStyleName(ValoTheme.TABSHEET_FRAMED);

					rangTabRecherche=0;
					rangTabDossierEtudiant = 1;

					//ajout de l'onglet principal 'recherche'
					layoutOngletRecherche = new VerticalLayout();
					ajoutOngletRecherche();
					layoutOngletRecherche.setSizeFull();
					tabSheetGlobal.addTab(layoutOngletRecherche, applicationContext.getMessage("mainUI.recherche.title", null, getLocale()), FontAwesome.SEARCH);

					//ajout de l'onglet principal 'assistance'
					tabSheetGlobal.addTab(assistanceView, applicationContext.getMessage(assistanceView.NAME + ".title", null, getLocale()), FontAwesome.SUPPORT);


					//ajout de l'onglet dossier étudiant
					addTabDossierEtudiant();

					//Ce tabSheet sera aligné à droite
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

					//On renseigne l'étudiant dont on consulte le dossier
					//Récupération du cod_etu
					etudiant = new Etudiant(daoCodeLoginEtudiant.getCodEtuFromLogin(userController.getCurrentUserName()));
					LOG.debug("MainUI etudiant : "+MainUI.getCurrent().getEtudiant().getCod_etu());
					//Récupération de l'état-civil (et les adresses)
					etudiantController.recupererEtatCivil();
					//On construit le menu affiché à l'étudiant
					buildMainMenuEtudiant();
				}


				/* Enregistre l'UI pour la réception de notifications */
				uiController.registerUI(this);



				boolean navigationComplete=false;
				String fragment = Page.getCurrent().getUriFragment();
				if (fragment != null && !fragment.isEmpty()) {
					//Cas de l'appel initial de l'application via l'url vers la vue admin (sinon le cas est gérer dans le listener du navigator
					if(fragment.contains("adminView") && userController.isAdmin()){
						//Afficher la vue admin
						navigator.navigateTo(AdminView.NAME);
						navigationComplete=true;
					}
					if(fragment.contains("accesDossierEtudiant") && userController.isEnseignant()){
						rechercheController.accessToDossierEtudiantDeepLinking(fragment);
						navigationComplete=true;
					}
					/*if(fragment.contains("accesNotesEtudiant") && userController.isEnseignant()){
						rechercheController.accessToDossierEtudiantDeepLinking(fragment);
						navigator.navigateTo(NotesView.NAME);
						navigationComplete=true;
					}*/
				}

				if(!navigationComplete){
					//PROBLEME DU F5 : on passe ici (init()) que quand on reinitialise l'UI ou en cas d'erreur. 
					//On ne peut donc pas rediriger vers des vues qui utilisent des variables non initialisées (ex : Main.getCurrent.getEtudiant)
					if(!applicationActive()){
						displayViewFullScreen(AccesBloqueView.NAME);
					}else{
						//Si utilisateur enseignant
						if(userController.isEnseignant()){
							//Récupération des favoris pour l'utilisateur
							List<Favoris> lfav = favorisController.getFavoris();
							if(lfav!=null && lfav.size()>0){
								//On affiche la vue des favoris
								navigator.navigateTo(FavorisView.NAME);
							}else{
								//On affiche la vue de recherche rapide
								navigator.navigateTo(RechercheRapideView.NAME);
							}
							//Affichage du message d'intro si besoin
							afficherMessageIntroEnseignants(false, true);
						}else{
							//Si utilisateur étudiant
							if(userController.isEtudiant()){
								//On affiche la vue de l'état-civil
								navigator.navigateTo(EtatCivilView.NAME);
								//Affichage du message d'intro si besoin
								afficherMessageIntroEtudiants();
							}else{
								//On affiche la vue d'erreur
								displayViewFullScreen(ErreurView.NAME);
							}
						}
					}
				}
			}
		}else{
			//Si utilisateur n'est ni enseignant, ni étudiant
			//On affiche la vue accès refusé
			displayViewFullScreen(AccesRefuseView.NAME);
		}
	}

	/**
	 * Affichage d'une vue en full-screen
	 * @param view
	 */
	private void displayViewFullScreen(String view){
		setContent(contentLayout);
		navigator.navigateTo(view);
	}

	/**
	 * Affichage du message d'intro aux étudiants
	 */
	public void afficherErreurView() {
		navigator.navigateTo(ErreurView.NAME);
		//displayViewFullScreen(ErreurView.NAME);
	}

	/**
	 * Affichage du message d'intro aux étudiants
	 */
	private void afficherMessageIntroEtudiants() {
		afficherMessageIntro(applicationContext.getMessage("helpWindow.text.etudiant", null, getLocale()),false, true);
	}

	/**
	 * Affichage du message d'intro aux enseignants
	 */
	public void afficherMessageIntroEnseignants(boolean displayForced, boolean displayCheckBox) {
		afficherMessageIntro(applicationContext.getMessage("helpWindow.text.enseignant", null, getLocale()),displayForced, displayCheckBox);

	}

	/**
	 * Affichage d'un message d'intro
	 * @param text
	 */
	private void afficherMessageIntro(String text, boolean displayForced, boolean displayCheckBox){

		//On Recupere dans la base si l'utilisateur a indiqué une préférence pour l'affichage du message d'introduction
		String val  = userController.getPreference(Utils.SHOW_MESSAGE_INTRO_PREFERENCE);

		//Par défaut, on affiche le message
		boolean afficherMessage = true;

		//Si on a une préférence indiquée par l'utilisateur en ce qui concerne l'affichage du message d'intro
		if(StringUtils.hasText(val)){
			//On récupère ce choix dans afficherMessage
			afficherMessage = Boolean.valueOf(val);
		}

		//Si on doit afficher le message
		if(displayForced || afficherMessage){
			//Création de la pop-pup contenant le message
			HelpWindow hbw = new HelpWindow(text,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),displayCheckBox);

			//Sur la fermeture de la fenêtre
			hbw.addCloseListener(g->{
				//On va enregistrer en base que l'utilisateur ne souhaite plus afficher le message si la checkbox proposée par la pop-up a été cochée
				boolean choix = hbw.getCheckBox().getValue();
				//Test si l'utilisateur a coché la case pour ne plus afficher le message
				if(choix){
					//mettre a jour dans la base de données
					userController.updatePreference(Utils.SHOW_MESSAGE_INTRO_PREFERENCE, "false");
				}
			});

			//Affichage de la pop_up
			UI.getCurrent().addWindow(hbw);
		}
	}

	/**
	 * Création des onglets pour l'enseignant
	 */
	private void ajoutOngletRecherche() {
		tabSheetEnseignant.setSizeFull();


		//Onglet recherche rapide
		tabSheetEnseignant.addTab(rechercheRapideView, applicationContext.getMessage("mainUI.rechercherapide.title", null, getLocale()), FontAwesome.SEARCH);
		viewEnseignantTab.put(rechercheRapideView.NAME, 0);

		//Onglet recherche arborescente
		tabSheetEnseignant.addTab(rechercheArborescenteView, applicationContext.getMessage("mainUI.recherchearbo.title", null, getLocale()), FontAwesome.SITEMAP);
		viewEnseignantTab.put(rechercheArborescenteView.NAME, 1);

		//Onglet favoris
		tabSheetEnseignant.addTab(favorisView, "Favoris", FontAwesome.STAR_O);
		viewEnseignantTab.put(favorisView.NAME, 2);


		//On gère le changement d'onglet effectué par l'utilisateur
		tabSheetEnseignant.addSelectedTabChangeListener( new TabSheet.SelectedTabChangeListener() {

			public void selectedTabChange(SelectedTabChangeEvent event){
				//On récupère le tabSheet
				TabSheet tabsheet = event.getTabSheet();

				//On récupère la vue à  afficher
				View vue = (View) tabsheet.getSelectedTab();
				//Si l'utilisateur veut afficher la vue des favoris
				if (vue instanceof FavorisView){
					//On initialise la vue
					favorisView.init();
				}
				//Si l'utilisateur veut afficher la vue de la liste des inscrits
				if (vue instanceof ListeInscritsView){
					//On refresh la vue
					listeInscritsView.refresh();
				}
				//Si l'utilisateur veut afficher la vue de la recherche arborescente
				if (vue instanceof RechercheArborescenteView){
					//On refresh la vue
					rechercheArborescenteView.refresh();
				}


			}
		});

		//Ajout de l'onglet contenant la liste des inscrits
		addTabListeInscrits();


		//Ajout du tabSheet dans le layout
		layoutOngletRecherche.addComponent(tabSheetEnseignant);
		//On passe le layout en sizeFull
		layoutOngletRecherche.setSizeFull();
		//Le tabSheet prend toute la place disponible dans le layout
		layoutOngletRecherche.setExpandRatio(tabSheetEnseignant, 1);

	}



	/**
	 * Ajout de l'onglet contenant la liste des inscrits
	 */
	private void addTabListeInscrits() {
		//Ajout de l'onglet au tabSheet
		tabSheetEnseignant.addTab(listeInscritsView, applicationContext.getMessage("mainUI.listeinscrits.title", null, getLocale()) + " ", FontAwesome.USERS);
		//test si c'est la premiere fois qu'on ajoute l'onglet
		if(!viewEnseignantTab.containsKey(listeInscritsView.NAME)){
			//On enregistre que l'onglet de la liste des inscrit est en 4em position (on compte à partir de 0)
			viewEnseignantTab.put(listeInscritsView.NAME, 3);
		}
		//On masque l'onglet par défaut
		tabSheetEnseignant.getTab(3).setVisible(false);
		//L'onglet possible une croix pour être fermé
		tabSheetEnseignant.getTab(3).setClosable(true);

	}

	/**
	 * Ajout de l'onglet principal "dossier" contenant le dossier de l'étudiant
	 */
	private void addTabDossierEtudiant() {
		//Ajout de l'onglet "Dossier"
		tabDossierEtu = tabSheetGlobal.addTab(layoutDossierEtudiant, applicationContext.getMessage("mainUI.dossier.title", null, getLocale()), FontAwesome.USER);
		tabSheetGlobal.setTabPosition(tabDossierEtu, rangTabDossierEtudiant);
		//On cache l'onglet par défaut
		tabSheetGlobal.getTab(rangTabDossierEtudiant).setVisible(false);
		//L'onglet possible une croix pour être fermé
		tabSheetGlobal.getTab(rangTabDossierEtudiant).setClosable(true);


	}

	/**
	 * Pour se rendre à la vue de rechercheArborescente
	 */
	/*public void navigateToRechercheArborescenteTab(){
		tabSheetEnseignant.setSelectedTab(2);
	}*/

	/**
	 * Construction du menu étudiant
	 */
	private void buildMainMenuEtudiant() {

		//Si l'étudiant dont on affiche le dossier est renseigné
		if(etudiant!=null){

			//Ajout du style au menu
			mainMenu.setPrimaryStyleName(ValoTheme.MENU_PART);
			//On fixe la largeur du menu
			mainMenu.setWidth("233px");

			//Si on a une url pour la photo de l'étudiant
			if(etudiant.getPhoto()!=null){
				//Layout contenant la photo
				HorizontalLayout photoLayout = new HorizontalLayout();

				//Ajout du style au layout
				photoLayout.addStyleName(ValoTheme.MENU_SUBTITLE);
				//On fixe la largeur du layout
				photoLayout.setWidth(213, Unit.PIXELS);
				//La layout a des marges
				photoLayout.setMargin(true);

				//Bouton qui indique, en fonction de l'icone, si l'étudiant est inscrit pour l'année en cours. Par défaut, icone indiquant que l'étudiant est inscrit
				Button etuInscritBtn = new Button("", FontAwesome.CHECK_CIRCLE);
				//Ajout du style au bouton
				etuInscritBtn.setPrimaryStyleName(ValoTheme.BUTTON_BORDERLESS);

				//Si l'étudiant est inscrit pour l'année en cours
				if(etudiant.isInscritPourAnneeEnCours()){
					//On fixe la description du bouton
					etuInscritBtn.setDescription("Inscrit pour l'année universitaire "+Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours(this)));
				}else{
					//On change l'icone du bouton pour indiquer que l'étudiant n'est pas inscrit
					etuInscritBtn.setIcon(FontAwesome.EXCLAMATION_CIRCLE);
					//On fixe la description du bouton
					etuInscritBtn.setDescription("Non Inscrit pour l'année universitaire "+Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours(this)));
				}

				//Ajout d'un élément vide dans le layout
				photoLayout.addComponent(new HorizontalLayout());


				//Création de l'image contenant la photo
				Image fotoEtudiant = new Image(null, new ExternalResource(etudiant.getPhoto()));
				fotoEtudiant.setWidth("120px");
				fotoEtudiant.setHeight("153px");
				//Ajout de la photo au layout
				photoLayout.addComponent(fotoEtudiant);
				//Alignement de la photo
				photoLayout.setComponentAlignment(fotoEtudiant, Alignment.MIDDLE_CENTER);
				//La photo prend toute la place disponible dans son layout
				photoLayout.setExpandRatio(fotoEtudiant, 1);


				//Ajout au layout du bouton, qui indique, en fonction de l'icone, si l'étudiant est inscrit pour l'année en cours
				photoLayout.addComponent(etuInscritBtn);

				//Ajout du layout de la photo au menu
				mainMenu.addComponent(photoLayout);
			}


			//Ajout du Prénom/Nom et codetu de l'étudiant dans le menu
			Label usernameLabel = new Label(etudiant.getNom()+"<br />"+etudiant.getCod_etu(), ContentMode.HTML);
			usernameLabel.addStyleName(ValoTheme.MENU_SUBTITLE);
			usernameLabel.setSizeUndefined();
			mainMenu.addComponent(usernameLabel);

			/* Etat Civil */
			addItemMenu("Etat-civil", EtatCivilView.NAME, FontAwesome.USER);

			//info annuelles visibles que si étudiant inscrit pour l'année en cours
			if(etudiant.isInscritPourAnneeEnCours()){
				addItemMenu("Informations annuelles", InformationsAnnuellesView.NAME, FontAwesome.INFO_CIRCLE);
			}

			/* Adresses */
			addItemMenu(applicationContext.getMessage(AdressesView.NAME + ".title", null, getLocale()), AdressesView.NAME, FontAwesome.HOME);

			/* Inscriptions */
			addItemMenu("Inscriptions", InscriptionsView.NAME, FontAwesome.FILE_TEXT);


			/* Calendrier */
			addItemMenu("Calendrier des épreuves", CalendrierView.NAME, FontAwesome.CALENDAR);


			/* Notes et Résultats */
			addItemMenu(applicationContext.getMessage(NotesView.NAME + ".title", null, getLocale()), NotesView.NAME, FontAwesome.LIST);

			/* Séparation avant Bouton "Aide" */
			CssLayout bottomMainMenu1 = new CssLayout();
			bottomMainMenu1.setStyleName(ValoTheme.MENU_SUBTITLE);
			bottomMainMenu1.setSizeUndefined();
			mainMenu.addComponent(bottomMainMenu1);

			/* Aide */
			Button helpBtn = new Button(applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()), FontAwesome.SUPPORT);
			helpBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			helpBtn.addClickListener(e -> {UI.getCurrent().addWindow(new HelpBasicWindow(applicationContext.getMessage("helpWindow.text.etudiant", null, getLocale()),applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),true));});
			mainMenu.addComponent(helpBtn);

			/* Deconnexion */
			//Voir si on peut accéder à l'appli hors ENT, le détecter, et afficher le bouton déconnexion
			/*	Button decoBtn = new Button("Déconnexion", FontAwesome.SIGN_OUT);
			decoBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			decoBtn.addClickListener(e -> getUI().getPage().setLocation("j_spring_security_logout"));
			mainMenu.addComponent(decoBtn);*/

			/* Séparation */
			CssLayout bottomMainMenu = new CssLayout();
			bottomMainMenu.setStyleName(ValoTheme.MENU_SUBTITLE);
			bottomMainMenu.setSizeUndefined();
			mainMenu.addComponent(bottomMainMenu);



		}
	}

	/**
	 * Ajout d'un item dans le menu étudiant
	 * @param caption
	 * @param viewName
	 * @param icon
	 */
	private void addItemMenu(String caption, String viewName, com.vaadin.server.Resource icon) {

		//Création du bouton
		Button itemBtn = new Button(caption, icon);
		itemBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);

		//Gestion du clic sur le bouton
		itemBtn.addClickListener(e -> navigator.navigateTo(viewName));
		viewButtons.put(viewName, itemBtn);

		//Ajout du bouton au menu
		mainMenu.addComponent(itemBtn);
	}

	/**
	 * @see com.vaadin.ui.UI#detach()
	 */
	@Override
	public void detach() {
		/* Se désinscrit de la réception de notifications (utile en cas d'utilisation du push) */
		uiController.unregisterUI(this);

		super.detach();
	}

	/**
	 * Affichage de la vue Recherche Arborescente
	 * @param parameterMap
	 */
	public void navigateToRechercheArborescente(Map<String, String> parameterMap) {
		LOG.debug("MainUI "+userController.getCurrentUserName()+" navigateToRechercheArborescente");
		//récupération de l'onglet qui affiche la vue RechercheArborescente
		int numtab = viewEnseignantTab.get(rechercheArborescenteView.NAME);
		//Si on a des paramètres renseignés
		if(parameterMap!=null){
			//initialisation de la vue avec les paramètres (on se place sur un élément précis de l'arborescence)
			rechercheArborescenteView.initFromParameters(parameterMap);
		}
		//On sélectionne l'onglet pour afficher la vue
		tabSheetEnseignant.setSelectedTab(numtab);
	}

	/**
	 * Affichage de la vue Liste Inscrits
	 * @param parameterMap
	 */
	public void navigateToListeInscrits(Map<String, String> parameterMap) {
		LOG.debug("MainUI "+userController.getCurrentUserName()+" navigateToListeInscrits");
		//récupération de l'onglet qui affiche la vue ListeInscrits
		int numtab = viewEnseignantTab.get(listeInscritsView.NAME);

		//Si on a des paramètres renseignés
		if(parameterMap!=null){

			//Récupéation de la liste des inscrits des inscrits en fonction des paramètres
			listeInscritsController.recupererLaListeDesInscrits(parameterMap, null, this);
			//initialisation de la vue avec la liste des inscrits
			listeInscritsView.initListe();
		}
		//Si l'onglet a été closed
		if(tabSheetEnseignant.getTab(numtab)==null){
			//On recréé l'onglet
			addTabListeInscrits();
		}

		//On affiche l'onglet
		tabSheetEnseignant.getTab(numtab).setVisible(true);

		//On se rend sur l'onglet pour afficher la vue ListeInscrits
		tabSheetEnseignant.setSelectedTab(numtab);
	}

	/**
	 * Affichage de la vue des favoris
	 */
	public void navigateToFavoris() {
		LOG.debug("MainUI "+userController.getCurrentUserName()+" navigateToFavoris");
		//récupération de l'onglet qui affiche la vue des favoris
		int numtab = viewEnseignantTab.get(favorisView.NAME);
		//On affiche l'onglet
		tabSheetEnseignant.getTab(numtab).setVisible(true);
		//On se rend sur l'onglet pour afficher la vue des favoris
		tabSheetEnseignant.setSelectedTab(numtab);
		//On se rend sur l'onglet Recherche dans le tabSheet principal au cas où on vienne du dossier d'un étudiant
		tabSheetGlobal.setSelectedTab(rangTabRecherche);
	}

	/**
	 * Affichage de la vue RechercheRapide
	 */
	public void navigateToRechercheRapide() {
		LOG.debug("MainUI "+userController.getCurrentUserName()+" navigateToRechercheRapide");
		//récupération de l'onglet qui affiche la vue RechercheRapide
		int numtab = viewEnseignantTab.get(rechercheRapideView.NAME);
		//On affiche l'onglet
		tabSheetEnseignant.getTab(numtab).setVisible(true);
		//On se rend sur l'onglet pour afficher la vue RechercheRapide
		tabSheetEnseignant.setSelectedTab(numtab);
		//On se rend sur l'onglet Recherche dans le tabSheet principal au cas où on vienne du dossier d'un étudiant
		tabSheetGlobal.setSelectedTab(rangTabRecherche);
	}

	/**
	 * Affichage du dossier d'un étudiant
	 * @param parameterMap
	 */
	public void navigateToDossierEtudiant(Map<String, String> parameterMap) {

		LOG.debug("MainUI "+userController.getCurrentUserName()+" navigateToDossierEtudiant : "+etudiant.getCod_etu());



		//Si l'onglet a été closed
		if(tabDossierEtu==null || tabSheetGlobal.getTabPosition(tabDossierEtu)<0){
			//On recréé l'onglet
			addTabDossierEtudiant();
		}
		//Si le menu a déjà été initialisé
		if(mainMenu!=null){
			//On supprime le contenu du menu 
			mainMenu.removeAllComponents();
		}
		//On reconstruit le menu pour l'étudiant concerné
		buildMainMenuEtudiant();

		//On rend visible l'onglet "Dossier" dans le tabSheet principal
		tabSheetGlobal.getTab(rangTabDossierEtudiant).setVisible(true);

		//On se rend sur l'onglet "Dossier" dans le tabSheet principal
		tabSheetGlobal.setSelectedTab(rangTabDossierEtudiant);

		//par défaut on affiche la vue état-civil
		navigator.navigateTo(EtatCivilView.NAME);
	}

	public void startBusyIndicator() {
		addWindow(loadingIndicatorWindow);
	}

	public void stopBusyIndicator() {
		loadingIndicatorWindow.close();
	}


	private boolean applicationActive(){
		return configController.isApplicationActive() && ((userController.isEtudiant() && configController.isPartieEtudiantActive()) 
				|| (userController.isEnseignant() && configController.isPartieEnseignantActive()));
	}

	/**
	 * Initialise le tracker d'activité.
	 */
	private void initAnalyticsTracker() {
		if (environment.getProperty("piwik.tracker.url") instanceof String && environment.getProperty("piwik.site.id") instanceof String) {
			analyticsTracker = new PiwikAnalyticsTracker(this, environment.getProperty("piwik.tracker.url"), environment.getProperty("piwik.site.id"));
		} else if (environment.getProperty("google.analytics.account") instanceof String) {
			analyticsTracker = new GoogleAnalyticsTracker(this, environment.getProperty("google.analytics.account"));
		} else {
			analyticsTracker = new LogAnalyticsTracker();
		}
		analyticsTracker.trackNavigator(navigator);
	}
}
