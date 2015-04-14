package fr.univlorraine.mondossierweb;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.DiscoveryNavigator;

import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent.Direction;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationListener;
import com.vaadin.addon.touchkit.ui.TabBarView;
import com.vaadin.addon.touchkit.ui.TabBarView.SelectedTabChangeEvent;
import com.vaadin.addon.touchkit.ui.TabBarView.SelectedTabChangeListener;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.UiController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.dao.IDaoCodeLoginEtudiant;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.AccesRefuseView;
import fr.univlorraine.mondossierweb.views.CalendrierMobileView;
import fr.univlorraine.mondossierweb.views.ErreurView;
import fr.univlorraine.mondossierweb.views.FavorisMobileView;
import fr.univlorraine.mondossierweb.views.InformationsAnnuellesMobileView;
import fr.univlorraine.mondossierweb.views.ListeInscritsMobileView;
import fr.univlorraine.mondossierweb.views.NotesDetailMobileView;
import fr.univlorraine.mondossierweb.views.NotesMobileView;
import fr.univlorraine.mondossierweb.views.RechercheMobileView;
import fr.univlorraine.mondossierweb.views.windows.HelpMobileWindow;
import fr.univlorraine.mondossierweb.views.windows.LoadingIndicatorWindow;
import fr.univlorraine.tools.vaadin.GoogleAnalyticsTracker;
import fr.univlorraine.tools.vaadin.SpringErrorViewProvider;
import gouv.education.apogee.commun.transverse.exception.WebBaseException;

@Component @Scope("prototype")
@Theme("valo-ul")
@StyleSheet("mobileView.css")
public class MdwTouchkitUI extends GenericUI{

	private static final long serialVersionUID = 1440138826041756551L;

	private Logger LOG = LoggerFactory.getLogger(MdwTouchkitUI.class);

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
	private InformationsAnnuellesMobileView informationsAnnuellesMobileView;
	@Resource
	private CalendrierMobileView calendrierMobileView;
	@Resource
	private NotesMobileView notesMobileView;
	@Resource
	private NotesDetailMobileView notesDetailMobileView;
	
	//Indicateur de chargement
	private LoadingIndicatorWindow loadingIndicatorWindow = new LoadingIndicatorWindow();


	//La vue par laquelle on est arrivé au trombinoscope
	@Getter
	private String trombinoscopeFromView;

	//La vue par laquelle on est arrivé au dossier étudiant
	@Getter
	private String dossierEtuFromView;

	//le NavigationManager pour l'affichage des notes et du détail des notes
	private NavigationManager noteNavigationManager;

	//La barre de menu affichée dans le dossier étudiant
	private TabBarView menuEtudiant;

	//Onglet infoAnnuelles du dossier étudiant
	private Tab tabInfoAnnuelles;

	//Onglet Calendrier du dossier étudiant
	private Tab tabCalendrier;

	//Onglet notes du dossier étudiant
	private Tab tabNotes;

	//Etape concernées par le détail des notes
	@Setter
	@Getter
	private Etape etapeDetailNotes;

	//vrai si on consulte les notes en vue enseignant
	@Setter
	@Getter
	private boolean vueEnseignantNotesEtResultats;

	//Le contenu principal de la page
	private CssLayout contentLayout = new CssLayout();


	/** Tracker Google Analytics */
	@Getter
	private GoogleAnalyticsTracker googleAnalyticsTracker = new GoogleAnalyticsTracker(this);

	/** Gestionnaire de vues étudiant*/
	@Getter
	private DiscoveryNavigator navigator = new DiscoveryNavigator(this, contentLayout);

	/**
	 * @see com.vaadin.ui.UI#getCurrent()
	 */
	public static MdwTouchkitUI getCurrent() {
		return (MdwTouchkitUI) UI.getCurrent();
	}

	@Override
	protected void init(VaadinRequest request) {


		VaadinSession.getCurrent().setErrorHandler(e -> {
			Throwable cause = e.getThrowable();
			while (cause instanceof Throwable) {
				/* Gère les accès non autorisés */
				if (cause instanceof AccessDeniedException) {
					Notification.show(cause.getMessage(), Type.ERROR_MESSAGE);
					navigator.navigateTo(AccesRefuseView.NAME);
					return;
				}
				cause = cause.getCause();
			}
			/* Traite les autres erreurs normalement */
			LOG.error(e.getThrowable().toString(), e.getThrowable());
			// Affiche de la vue d'erreur
			navigator.navigateTo(ErreurView.NAME);
		});

		//Le contenu principal n'est pas scrollable. Par contre les élément à l'intérieur pourront l'être
		setStyleName("v-noscrollableelement");

		//Le contentLayout prend toute la place disponible
		contentLayout.setSizeFull();

		/* Affiche le nom de l'application dans l'onglet du navigateur */
		getPage().setTitle(environment.getRequiredProperty("app.name"));


		/* Ex de Device Detection */
		Device currentDevice = DeviceUtils.getCurrentDevice((HttpServletRequest) request);
		if(currentDevice.isMobile())
			LOG.debug("device : mobile");
		if(currentDevice.isTablet())
			LOG.debug("device : tablet");
		if(currentDevice.isNormal())
			LOG.debug("device : normal");

		/* Construit le gestionnaire de vues */
		navigator.setErrorProvider(new SpringErrorViewProvider(ErreurView.class, navigator));



		/* Initialise Google Analytics */
		googleAnalyticsTracker.setAccount(environment.getProperty("analytics.account"));
		/* Suis les changements de vue du navigator */
		googleAnalyticsTracker.trackNavigator(navigator);

		//contentLayout est le contenu principal de la page
		setContent(contentLayout);


		// Si l'utilisateur est enseignant ou étudiant
		if(userController.isEnseignant() || userController.isEtudiant()){

			//On récupère l'IP du client
			GenericUI.getCurrent().getIpClient();
			
			// Si l'utilisateur est enseignant
			if(userController.isEnseignant()){

				//On consultera les notes en vue enseignant
				vueEnseignantNotesEtResultats=true;
				//On se rend par défaut à la vue des favoris
				navigator.navigateTo(FavorisMobileView.NAME);
				//On affiche le message d'intro
				afficherMessageIntroEnseignants();

			}else{
				// Si l'utilisateur est étudiant
				//On consultera les notes en vue etudiant
				vueEnseignantNotesEtResultats=false;
				//On récupère le codetu de l'étudiant
				etudiant = new Etudiant(daoCodeLoginEtudiant.getCodEtuFromLogin(userController.getCurrentUserName()));
				try{
					//On récupère l'état-civil et les adresses de l'étudiant
					etudiantController.recupererEtatCivil();
					//On récupère le calendrier de l'étudiant
					etudiantController.recupererCalendrierExamens();
					//On récupère les notes de l'étudiant
					etudiantController.recupererNotesEtResultats(etudiant);
					//On affiche le dossier
					navigateToDossierEtudiant();
				} catch (WebBaseException ex) {
					LOG.error("Probleme avec le WS lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
					navigator.navigateTo(ErreurView.NAME);
				} catch (Exception ex) {
					LOG.error("Probleme lors de la recherche de l'état-civil pour etudiant dont codetu est : " + GenericUI.getCurrent().getEtudiant().getCod_etu(),ex);
					navigator.navigateTo(ErreurView.NAME);
				}

			}

		}else{
			//Utilisateur ni enseignant, ni étudiant, on le redirige vers la vue accès refusé
			navigator.navigateTo(AccesRefuseView.NAME);
		}




	}


	/**
	 * Affiche du message d'intro pour les enseignants
	 */
	private void afficherMessageIntroEnseignants() {
		afficherMessageIntro(applicationContext.getMessage("helpWindowMobile.text.enseignant", null, getLocale()));

	}

	/**
	 * Affiche du message d'intro
	 */
	private void afficherMessageIntro(String text){
		//Recuperer dans la base si l'utilisateur a une préférence pour l'affichage le message
		String val  = userController.getPreference(Utils.SHOW_MESSAGE_INTRO_MOBILE_PREFERENCE);
		
		//Par défaut on affiche le message
		boolean afficherMessage = true;
		
		//Si on a une préférence pour l'utilisateur en ce qui concerne l'affichage du message d'accueil mobile
		if(StringUtils.hasText(val)){
			//On récupère ce choix dans afficherMessage
			afficherMessage = Boolean.valueOf(val);
		}

		//Si on doit afficher le message
		if(afficherMessage){
			//Création de la pop-pup contenant le message
			HelpMobileWindow hbw = new HelpMobileWindow(text,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),true);
			
			//Sur la fermeture de la fenêtre
			hbw.addCloseListener(g->{
				//On va enregistrer en base que l'utilisateur ne souhaite plus afficher le message si la checkbox proposée par la pop-up a été cochée
				boolean choix = hbw.getCheckBox().getValue();
				//Test si l'utilisateur a coché la case pour ne plus afficher le message
				if(choix){
					//mettre a jour dans la base de données
					userController.updatePreference(Utils.SHOW_MESSAGE_INTRO_MOBILE_PREFERENCE, "false");
				}
			});
			
			//Affichage de la pop_up
			UI.getCurrent().addWindow(hbw);
		}
	}

	/**
	 * Affichage de la vue Liste Inscrits depuis la vue Search
	 * @param parameterMap
	 */
	public void navigateToListeInscritsFromSearch(Map<String, String> parameterMap) {
		trombinoscopeFromView = RechercheMobileView.NAME;
		navigateToListeInscrits(parameterMap);
	}

	/**
	 * Affichage de la vue Liste Inscrits depuis la vue Favoris
	 * @param parameterMap
	 */
	public void navigateToListeInscritsFromFavoris(Map<String, String> parameterMap) {
		trombinoscopeFromView = FavorisMobileView.NAME;
		navigateToListeInscrits( parameterMap);
	}

	/**
	 * Affichage du dossier d'un étudiant depuis la vue listeInscrits
	 */
	public void navigateToDossierEtudiantFromListeInscrits() {
		dossierEtuFromView = ListeInscritsMobileView.NAME;
		navigateToDossierEtudiant();
	}

	/**
	 * Affichage du dossier d'un étudiant depuis la vue Search
	 */
	public void navigateToDossierEtudiantFromSearch() {
		dossierEtuFromView = RechercheMobileView.NAME;
		navigateToDossierEtudiant();
	}

	/**
	 * Affichage du dossier d'un étudiant
	 */
	public void navigateToDossierEtudiant() {
		//Refresh des vues du dossier étudiant avec les données de l'étudiant
		informationsAnnuellesMobileView.refresh();
		calendrierMobileView.refresh();
		notesMobileView.refresh();

		//Si le menu étudiant n'a jamais été initialisé
		if(menuEtudiant==null){
			//création du menu étudiant
			initMenuEtudiant();
		}
		//On affiche la vue infoAnnuelles par défaut
		menuEtudiant.setSelectedTab(tabInfoAnnuelles);
		
		//Le contenu principal de la page devient le tabBarView représentant le dossier étudiant
		setContent(menuEtudiant);
	}

	/**
	 * Création du menu étudiant
	 */
	private void initMenuEtudiant() {
		
		//Si le menuEtudiant n'a jamais été initialisé
		if(menuEtudiant==null){
			//On créé le menuEtudiant
			menuEtudiant = new TabBarView();
		}

		//Création de l'onglet Informations
		tabInfoAnnuelles = menuEtudiant.addTab(informationsAnnuellesMobileView, applicationContext.getMessage("mobileUI.infoannuelles.title", null, getLocale()), FontAwesome.INFO);
		tabInfoAnnuelles.setId("tabInfoAnnuelles");

		//Création de l'onglet Calendrier
		tabCalendrier = menuEtudiant.addTab(calendrierMobileView, applicationContext.getMessage("mobileUI.calendrier.title", null, getLocale()), FontAwesome.CALENDAR);
		tabCalendrier.setId("tabCalendrier");


		//Si le navigationManager des notes est null
		if(noteNavigationManager==null){
			//On créé le navigationManager
			noteNavigationManager= new NavigationManager();
		}
		//le composant affiché dans le navigationManager est la vue des notes
		noteNavigationManager.setCurrentComponent(notesMobileView);
		//le composant suivant à afficher dans le navigationManager est la vue du détail des notes
		noteNavigationManager.setNextComponent(notesDetailMobileView);
		//Création de l'onglet Résultats
		tabNotes = menuEtudiant.addTab(noteNavigationManager, applicationContext.getMessage("mobileUI.resultats.title", null, getLocale()),  FontAwesome.LIST);
		tabNotes.setId("tabNotes");
		
		//Détection du retour sur la vue du détail des notes pour mettre à jour le JS
		menuEtudiant.addListener(new SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(SelectedTabChangeEvent event) {
			 //test si on se rend sur la vue des notes
			 if(menuEtudiant.getSelelectedTab().equals(tabNotes)){
				 //test si on se rend sur le détail des notes
				 if(noteNavigationManager.getCurrentComponent().equals(notesDetailMobileView)){
					 //On met à jour le JS (qui est normalement perdu, sans explication)
					 notesDetailMobileView.refreshJavascript();
				 }
			 }
			}
		});

	}

	/**
	 * Affichage de la liste des inscrits à partir des infos en paramètre
	 * @param parameterMap
	 */
	public void navigateToListeInscrits(Map<String, String> parameterMap) {
		//Si on a des paramètres renseignés
		if(parameterMap!=null){
			//On récupère a liste des inscrits
			listeInscritsController.recupererLaListeDesInscrits(parameterMap, null, this);
		}
		//On affiche la vue ListeInscrits
		navigator.navigateTo(ListeInscritsMobileView.NAME);
	}

	/**
	 * Affichage de la vue Favoris
	 */
	public void navigateTofavoris() {
		navigator.navigateTo(FavorisMobileView.NAME);
	}

	/**
	 * Affichage de la vue Search
	 */
	public void navigateToRecherche() {
		setContent(contentLayout);
		navigator.navigateTo(RechercheMobileView.NAME);
	}

	/**
	 * Affichage de la vue listeInscrits
	 */
	public void navigateToListeInscrits() {
		setContent(contentLayout);
		navigator.navigateTo(ListeInscritsMobileView.NAME);
	}

	/**
	 * Affichage de la vue contenant le résumé des notes (cad on vient du détail des notes)
	 */
	public void navigateToResumeNotes() {
		//Si le navigationManager n'est pas null
		if(noteNavigationManager!=null){
			//On réaffiche la vue précédente du navigationManager
			noteNavigationManager.navigateBack();
		}


	}

	/**
	 * Affichage de la vue contenant le détail des notes à partir de l'étape en paramètre
	 * @param etape
	 */
	public void navigateToDetailNotes(Etape etape) {
		//Si le navigationManager n'est pas null
		if(noteNavigationManager!=null){
			//On refresh la vue du détail des notes
			notesDetailMobileView.refresh(etape, etudiant.getCod_etu());
			//On navigue sur la vue du détail des notes
			noteNavigationManager.navigateTo(notesDetailMobileView);
		}


	}


	public void startBusyIndicator() {
		addWindow(loadingIndicatorWindow);
	}

	public void stopBusyIndicator() {
		loadingIndicatorWindow.close();
	}



}
