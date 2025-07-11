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
package fr.univlorraine.mondossierweb;


import com.vaadin.annotations.Push;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.ResultatController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.AccesBloqueView;
import fr.univlorraine.mondossierweb.views.AccesRefuseView;
import fr.univlorraine.mondossierweb.views.CalendrierMobileView;
import fr.univlorraine.mondossierweb.views.ErreurView;
import fr.univlorraine.mondossierweb.views.FavorisMobileView;
import fr.univlorraine.mondossierweb.views.InformationsAnnuellesMobileView;
import fr.univlorraine.mondossierweb.views.ListeInscritsMobileView;
import fr.univlorraine.mondossierweb.views.NavigationManagerView;
import fr.univlorraine.mondossierweb.views.NotesDetailMobileView;
import fr.univlorraine.mondossierweb.views.NotesMobileView;
import fr.univlorraine.mondossierweb.views.RechercheMobileView;
import fr.univlorraine.mondossierweb.views.windows.HelpMobileWindow;
import fr.univlorraine.mondossierweb.views.windows.LoadingIndicatorWindow;
import fr.univlorraine.tools.vaadin.GoogleAnalyticsTracker;
import fr.univlorraine.tools.vaadin.LogAnalyticsTracker;
import fr.univlorraine.tools.vaadin.PiwikAnalyticsTracker;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.Map;


@Scope("prototype")
@Component
@Theme("valo-ul")
@StyleSheet({"mobileView.css"})
@SuppressWarnings("serial")
@SpringUI(path = "m")
@Push(transport = Transport.WEBSOCKET_XHR)
@Viewport("user-scalable=no,initial-scale=1.0")
@Slf4j
public class MdwTouchkitUI extends GenericUI{

	/**
	 * Nombre maximum de tentatives de reconnexion lors d'une déconnexion.
	 */
	private static final int TENTATIVES_RECO = 3;

	/* Redirige java.util.logging vers SLF4j */
	static {
		SLF4JBridgeHandler.install();
	}
	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient ObjectFactory<HelpMobileWindow> helpMobileWindowFactory;
	@Resource
	private transient Environment environment;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource(name="${resultat.implementation}")
	private transient ResultatController resultatController;
	@Resource
	private transient ListeInscritsController listeInscritsController;
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

	//La vue par laquelle on est arrivé à la recherche
	@Getter
	@Setter
	private String rechercheFromView;

	//La vue par laquelle on est arrivé au dossier étudiant
	@Getter
	private String dossierEtuFromView;

	//le NavigationManager pour l'affichage des notes et du détail des notes
	//private NavigationManager noteNavigationManager;
	private NavigationManagerView noteNavigationManager;

	//La barre de menu affichée dans le dossier étudiant
	//private TabBarView menuEtudiant;
	private TabSheet menuEtudiant;

	//Onglet infoAnnuelles du dossier étudiant
	private TabSheet.Tab tabInfoAnnuelles;

	//Onglet Calendrier du dossier étudiant
	private TabSheet.Tab tabCalendrier;

	//Onglet notes du dossier étudiant
	private TabSheet.Tab tabNotes;

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

	/** The view provider. */
	@Resource
	private SpringViewProvider viewProvider;

	/** Gestionnaire de vues étudiant*/
	@Getter
	private final Navigator navigator = new Navigator(this, contentLayout);

	/**
	 * @see com.vaadin.ui.UI#getCurrent()
	 */
	public static MdwTouchkitUI getCurrent() {
		return (MdwTouchkitUI) UI.getCurrent();
	}

	@Override
	protected void init(VaadinRequest request) {
		log.debug("init(); MdwTouchkitUI");

		VaadinSession.getCurrent().setErrorHandler(e -> {

			/* Gérer les erreurs quand l'application est en maintenance */
			if(!applicationActive()){
				afficherMessageMaintenance();
				return;
			}

			Throwable cause = e.getThrowable();
			while (cause instanceof Throwable) {
				/* Gère les erreurs de fragment dans les urls */
				if (cause instanceof URISyntaxException) {
					log.info("Erreur de fragment ");
					// Retour à la racine
					Page.getCurrent().setLocation(PropertyUtils.getAppUrl()+"/m");
					return;
				}

				/* Gère les accès non autorisés */
				if (cause instanceof AccessDeniedException) {
					Notification.show(cause.getMessage(), Notification.Type.ERROR_MESSAGE);
					displayViewFullScreen(AccesRefuseView.NAME);
					return;
				}

				if(cause!=null && cause.getClass()!=null){
					String simpleName = cause.getClass().getSimpleName();

					/* Gérer les erreurs à ignorer */
					if (PropertyUtils.getListeErreursAIgnorer().contains(simpleName)) {
						Notification.show(cause.getMessage(), Notification.Type.ERROR_MESSAGE);
						navigator.navigateTo(ErreurView.NAME);
						return;
					}
				}
				cause = cause.getCause();
			}
			/* Traite les autres erreurs normalement */
			log.error(e.getThrowable().toString(), e.getThrowable());
			// Affiche de la vue d'erreur
			navigator.navigateTo(ErreurView.NAME);
		});

		//Le contenu principal n'est pas scrollable. Par contre les élément à l'intérieur pourront l'être
		setStyleName("v-noscrollableelement");

		//Le contentLayout prend toute la place disponible
		contentLayout.setSizeFull();

		/* Affiche le nom de l'application dans l'onglet du navigateur */
		getPage().setTitle(environment.getRequiredProperty("app.name"));

		reloadIfUriFragmentError(getPage().getUriFragment());

		/* Gestion de l'acces a un dossier précis via url deepLinking (ne peut pas être fait dans navigator 
				car le fragment ne correspond pas à une vue existante) */
		getPage().addUriFragmentChangedListener(new Page.UriFragmentChangedListener() {
			public void uriFragmentChanged(Page.UriFragmentChangedEvent source) {

				//On bloque l'accès aux vues desktop
				/*	if(!Utils.isViewMobile(source.getUriFragment())){
					afficherMessageAccesRefuse();
				}*/


				//Si l'application est en maintenance on bloque l'accès
				if(!applicationActive() && !source.getUriFragment().contains(AccesBloqueView.NAME)){
					afficherMessageMaintenance();
				}

				reloadIfUriFragmentError(source.getUriFragment());

			}
		});

		//Paramétrage du comportement en cas de perte de connexion
		configReconnectDialog();

		/* Construit le gestionnaire de vues */
		navigator.addProvider(viewProvider);
		navigator.setErrorProvider(new ViewProvider() {
			@Override
			public String getViewName(final String viewAndParameters) {
				return ErreurView.NAME;
			}

			@Override
			public View getView(final String viewName) {
				return viewProvider.getView(ErreurView.NAME);
			}
		});

		navigator.addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {


				//On bloque l'accès aux vues desktop
				if(!Utils.isViewMobile(event.getViewName())){
					return false;
				}

				//Si l'application est en maintenance on bloque l'accès
				if(!applicationActive() && !event.getViewName().equals(AccesBloqueView.NAME)){
					afficherMessageMaintenance();
					return false;
				}

				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				// TODO Auto-generated method stub

			}

		});

		//init du tracker
		initAnalyticsTracker();

		//contentLayout est le contenu principal de la page
		setContent(contentLayout);


		// Si l'utilisateur est enseignant ou étudiant
		if(userController.isEnseignant() || userController.isEtudiant()){

			if(!applicationActive()){
				afficherMessageMaintenance();
			}else{

				//On récupère l'IP du client
				GenericUI.getCurrent().getIpClient();

				// Si l'utilisateur est enseignant
				if(userController.isEnseignant()){

					//On consultera les notes en vue enseignant
					vueEnseignantNotesEtResultats=true;
					//On se rend par défaut à la vue des favoris
					navigator.navigateTo(FavorisMobileView.NAME);
					//On affiche le message d'intro
					afficherMessageIntro(true);

				}else{
					// Si l'utilisateur est étudiant
					//On consultera les notes en vue etudiant
					vueEnseignantNotesEtResultats=false;
					//On récupère le codetu de l'étudiant
					String codetu=userController.getCodetu();
					etudiant = new Etudiant(codetu);
					try{
						//On récupère l'état-civil et les adresses de l'étudiant
						etudiantController.recupererEtatCivil();
						//Si on a eu une erreur à la récupération de l'état-civil
						if(GenericUI.getCurrent().getEtudiant()==null){
							navigator.navigateTo(ErreurView.NAME);
						}else{
							if((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiant())  || 
								(userController.isEnseignant() && configController.isAffCalendrierEpreuvesEnseignant()) ||
								(userController.isGestionnaire() && configController.isAffCalendrierEpreuvesGestionnaire())){
								//On récupère le calendrier de l'étudiant
								etudiantController.recupererCalendrierExamens();
							}
							if((userController.isEtudiant() && configController.isAffNotesEtudiant()) || 
								(userController.isEnseignant() && configController.isAffNotesEnseignant()) ||
								(userController.isGestionnaire() && configController.isAffNotesGestionnaire())){
								//On récupère les notes de l'étudiant
								resultatController.recupererNotesEtResultatsEtudiant(etudiant);
							}
							//On affiche le dossier
							navigateToDossierEtudiant();
							//On affiche le message d'intro
							afficherMessageIntro(false);
						}
					} catch (Exception ex) {
						log.error("Probleme lors de la recherche de l'état-civil pour etudiant dont codetu est : "+codetu ,ex);
						navigator.navigateTo(ErreurView.NAME);
					}

				}
			}
		}else{
			//Utilisateur ni enseignant, ni étudiant, on le redirige vers la vue accès refusé
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

	private void afficherMessageMaintenance(){
		displayViewFullScreen(AccesBloqueView.NAME);
	}


	/**
	 * Affiche du message d'intro pour les enseignants
	 */
	private void afficherMessageIntro(boolean enseignant) {

		//On Recupere dans la base si l'utilisateur a indiqué une préférence pour l'affichage du message d'introduction
		String val  = userController.getPreference(Utils.SHOW_MESSAGE_INTRO_MOBILE_PREFERENCE);

		//Par défaut, on affiche le message
		boolean afficherMessage = true;

		//Si on a une préférence indiquée par l'utilisateur en ce qui concerne l'affichage du message d'intro
		if(StringUtils.hasText(val)){
			//On récupère ce choix dans afficherMessage
			afficherMessage = Boolean.valueOf(val);
		}

		//Si on doit afficher le message
		if(afficherMessage) {
			String message = enseignant ? applicationContext.getMessage("helpWindowMobile.text.enseignant", null, getLocale()) : applicationContext.getMessage("helpWindowMobile.text.etudiant", null, getLocale());
			if(StringUtils.hasText(message)) {
				HelpMobileWindow hbw = helpMobileWindowFactory.getObject();
				hbw.init(message, applicationContext.getMessage("messageIntroMobileWindow.title", null, getLocale()), true);
				hbw.addCloseListener(g -> {
					//On va enregistrer en base que l'utilisateur ne souhaite plus afficher le message si la checkbox proposée par la pop-up a été cochée
					boolean choix = hbw.getCheckBox().getValue();
					//Test si l'utilisateur a coché la case pour ne plus afficher le message
					if (choix) {
						//mettre a jour dans la base de données
						userController.updatePreference(Utils.SHOW_MESSAGE_INTRO_MOBILE_PREFERENCE, "false");
					}
				});
				UI.getCurrent().addWindow(hbw);
			}
		}
	}

	/*
	private void afficherMessageIntro(String text){

		Notification note = new Notification(text, "", Notification.TYPE_TRAY_NOTIFICATION, true);
		note.setPosition(Position.MIDDLE_CENTER);
		note.setDelayMsec(6000);
		note.show(this.getPage());

	}*/

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
		//On indique au navigator qu'on va aller sur la vue InformationsAnnuellesMobileView pour informer Piwik
		navigator.navigateTo(InformationsAnnuellesMobileView.NAME);
		//Refresh des vues du dossier étudiant avec les données de l'étudiant
		informationsAnnuellesMobileView.refresh();
		if((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiant())  || 
			(userController.isEtudiant() && configController.isAffCalendrierEpreuvesEnseignant()) ||
			(userController.isGestionnaire() && configController.isAffCalendrierEpreuvesGestionnaire())){
			calendrierMobileView.refresh();
		}
		if((userController.isEtudiant() && configController.isAffNotesEtudiant()) || 
			(userController.isEnseignant() && configController.isAffNotesEnseignant()) ||
			(userController.isGestionnaire() && configController.isAffNotesGestionnaire())){
			notesMobileView.refresh();
		}

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
			menuEtudiant = new TabSheet();
			menuEtudiant.setSizeFull();
			menuEtudiant.setStyleName("menu-mobile");
			//menuEtudiant.setTabCaptionsAsHtml(true);
		}

		//Création de l'onglet Informations
		tabInfoAnnuelles = menuEtudiant.addTab(informationsAnnuellesMobileView, applicationContext.getMessage("mobileUI.infoannuelles.title", null, getLocale()), FontAwesome.INFO);
		tabInfoAnnuelles.setId("tabInfoAnnuelles");
		//tabInfoAnnuelles.setCaption("<div class=\"valotabcaption\">"+tabInfoAnnuelles.getCaption()+"</div>");

		//Création de l'onglet Calendrier
		if((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiant())  || 
			(userController.isEnseignant() && configController.isAffCalendrierEpreuvesEnseignant()) ||
			(userController.isGestionnaire() && configController.isAffCalendrierEpreuvesGestionnaire())){
			tabCalendrier = menuEtudiant.addTab(calendrierMobileView, applicationContext.getMessage("mobileUI.calendrier.title", null, getLocale()), FontAwesome.CALENDAR);
			tabCalendrier.setId("tabCalendrier");
		}

		//Si le navigationManager des notes est null
		if(noteNavigationManager==null){
			//On créé le navigationManager
			noteNavigationManager= new NavigationManagerView();
		}
		if((userController.isEtudiant() && configController.isAffNotesEtudiant()) || 
			(userController.isEnseignant() && configController.isAffNotesEnseignant()) ||
			(userController.isGestionnaire() && configController.isAffNotesGestionnaire())){
			//le composant affiché dans le navigationManager est la vue des notes
			noteNavigationManager.setFirstComponent(notesMobileView);
			//le composant suivant à afficher dans le navigationManager est la vue du détail des notes
			noteNavigationManager.setNextComponent(notesDetailMobileView);
			//Création de l'onglet Résultats
			tabNotes = menuEtudiant.addTab(noteNavigationManager, applicationContext.getMessage("mobileUI.resultats.title", null, getLocale()),  FontAwesome.LIST);
			tabNotes.setId("tabNotes");
		}

		//Détection du retour sur la vue du détail des notes pour mettre à jour le JS
		menuEtudiant.addSelectedTabChangeListener(new TabSheet.SelectedTabChangeListener() {
			@Override
			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
				//test si on se rend sur la vue des notes
				if(menuEtudiant.getSelectedTab().equals(noteNavigationManager)){
					//test si on se rend sur le détail des notes
					if(noteNavigationManager.getCurrentView().equals(notesDetailMobileView)){
						//On met à jour le JS (qui est normalement perdu, sans explication)
						notesDetailMobileView.refreshJavascript();
					}
				}

				//test si on se rend sur la vue calendrier
				if(menuEtudiant.getSelectedTab().equals(calendrierMobileView)){
					/* Message d'info */
					if(applicationContext.getMessage(CalendrierMobileView.NAME+".message.info", null, getLocale()) != null){
						Notification note = new Notification(applicationContext.getMessage(CalendrierMobileView.NAME+".message.info", null, getLocale()),"", Notification.TYPE_TRAY_NOTIFICATION, true);
						note.setPosition(Position.MIDDLE_CENTER);
						note.setDelayMsec(6000);
						note.show(Page.getCurrent());
						//Notification.show("", applicationContext.getMessage(CalendrierMobileView.NAME+".message.info", null, getLocale()), Notification.TYPE_TRAY_NOTIFICATION);
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
	 * Affichage de la vue par laquelle on est arrivée à la recherche
	 */
	public void backFromSearch() {
		if(rechercheFromView==null || rechercheFromView.equals(FavorisMobileView.NAME)){
			navigator.navigateTo(FavorisMobileView.NAME);
		}else{
			if(rechercheFromView.equals(InformationsAnnuellesMobileView.NAME)){
				navigateToDossierEtudiant();
			}
		}
	}

	/**
	 * Affichage de la vue Search
	 */
	public void navigateToRecherche(String fromView) {
		if(fromView!=null){
			rechercheFromView = fromView;
		}
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
			noteNavigationManager.navigateToNextView();
		}


	}


	private boolean applicationActive(){
		return configController.isApplicationMobileActive() && ((userController.isEtudiant() && configController.isPartieEtudiantActive()) 
			|| (!userController.isEnseignant() && !userController.isEtudiant() && !userController.isGestionnaire()) 
			|| (userController.isEnseignant() && configController.isPartieEnseignantActive())
			|| (userController.isGestionnaire() && configController.isProfilGestionnaireActif()));
	}

	public void startBusyIndicator() {
		addWindow(loadingIndicatorWindow);
	}

	public void stopBusyIndicator() {
		loadingIndicatorWindow.close();
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

	private void reloadIfUriFragmentError(String uriFragment) {
		if(uriFragment != null && uriFragment.contains("#")) {
			log.warn("fragment erroné :"+uriFragment);
			// Retour à la racine
			Page.getCurrent().setLocation(PropertyUtils.getAppUrl()+"/m");
		}
	}

	/**
	 * Configure la reconnexion en cas de déconnexion.
	 */
	private void configReconnectDialog() {
		getReconnectDialogConfiguration().setDialogModal(true);
		getReconnectDialogConfiguration().setReconnectAttempts(TENTATIVES_RECO);
		getReconnectDialogConfiguration().setDialogText(applicationContext.getMessage("vaadin.reconnectDialog.text", null, getLocale()));
		getReconnectDialogConfiguration().setDialogTextGaveUp(applicationContext.getMessage("vaadin.reconnectDialog.textGaveUp", null, getLocale()));
	}

}
