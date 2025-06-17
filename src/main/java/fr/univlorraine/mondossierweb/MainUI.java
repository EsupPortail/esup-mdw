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
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UiController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
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
import jakarta.annotation.Resource;
import lombok.Getter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Application Vaadin
 * 
 * 
 */
@Scope("prototype")
@Component
@Theme("valo-ul")
@StyleSheet("mainView.css")
@SuppressWarnings("serial")
@SpringUI(path = "")
@Slf4j
@Push(transport = Transport.WEBSOCKET_XHR)
public class MainUI extends GenericUI {

	/**
	 * Nombre maximum de tentatives de reconnexion lors d'une déconnexion.
	 */
	private static final int TENTATIVES_RECO = 3;

	private static final String FRAGMENT_PREFIX = "!";

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
	private transient ObjectFactory<HelpBasicWindow> helpBasicWindowFactory;


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

	@Resource
	private transient ObjectFactory<HelpWindow> helpWindowFactory;


	private LoadingIndicatorWindow loadingIndicatorWindow = new LoadingIndicatorWindow();

	//rang de l'onglet contenant le dossier etudiant dans le conteneur principal
	private int rangTabDossierEtudiant;

	//tab Dossier Etudiant
	private TabSheet.Tab tabDossierEtu;

	//rang de l'onglet contenant la recherche dans le conteneur principal
	private int rangTabRecherche;

	// Variable permettant de stocker les paramètres lors du passage d'une vue à l'autre
	private Map<String, String> urlParameterMapRechArb;

	// Variable permettant de stocker les paramètres lors du passage d'une vue à l'autre
	private Map<String, String> urlParameterMapListeInscrits;

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

	/** The view provider. */
	@Resource
	private SpringViewProvider viewProvider;

	/** Gestionnaire de vues étudiant*/
	@Getter
	private final Navigator navigator = new Navigator(this, contentLayout);

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

		log.debug("init(); mainUI");

		//Gestion des erreurs
		VaadinSession.getCurrent().setErrorHandler(e -> {

			/* Gérer les erreurs quand l'application est en maintenance */
			if(!applicationActive()){
				displayViewFullScreen(AccesBloqueView.NAME);
				return;
			}

			Throwable cause = e.getThrowable();

			while (cause != null) {
				/* Gère les erreurs de fragment dans les urls */
				if (cause instanceof URISyntaxException) {
					log.debug("Erreur de fragment ");
					// Retour à la racine
					Page.getCurrent().setLocation(PropertyUtils.getAppUrl());
					return;
				}
				/* Gère les accès non autorisés */
				if (cause instanceof AccessDeniedException) {
					Notification.show(cause.getMessage(), Notification.Type.ERROR_MESSAGE);
					displayViewFullScreen(AccesRefuseView.NAME);
					return;
				}

                String simpleName = cause.getClass().getSimpleName();
                /* Gére les erreurs à ignorer */
                if (PropertyUtils.getListeErreursAIgnorer().contains(simpleName)) {
                    Notification.show(cause.getMessage(), Notification.Type.ERROR_MESSAGE);
                    displayViewFullScreen(ErreurView.NAME);
                    return;
                }
                cause = cause.getCause();
			}
			// Traite les autres erreurs normalement 
			log.error(e.getThrowable().toString(), e.getThrowable());
			// Affiche de la vue d'erreur
			displayViewFullScreen(ErreurView.NAME);
			//DefaultErrorHandler.doDefault(e);
		});


		// Affiche le nom de l'application dans l'onglet du navigateur 
		getPage().setTitle(environment.getRequiredProperty("app.name"));

		reloadIfUriFragmentError(Page.getCurrent().getUriFragment());

		//Gestion de l'acces a un dossier précis via url deepLinking (ne peut pas être fait dans navigator 
		//car le fragment ne correspond pas à une vue existante)
		getPage().addUriFragmentChangedListener(new Page.UriFragmentChangedListener() {
			public void uriFragmentChanged(Page.UriFragmentChangedEvent source) {

				//Si l'application est en maintenance on bloque l'accès
				if(!applicationActive() && (source.getUriFragment() == null ||
						(!source.getUriFragment().contains(AccesBloqueView.NAME) &&
						!(source.getUriFragment().contains(AdminView.NAME) && userController.isAdmin())))){
					displayViewFullScreen(AccesBloqueView.NAME);
				}else{
					reloadIfUriFragmentError(source.getUriFragment());
					if(source.getUriFragment() != null && source.getUriFragment().contains(Utils.FRAGMENT_ACCES_DOSSIER_ETUDIANT) && userController.isEnseignant()){
						rechercheController.accessToDossierEtudiantDeepLinking(source.getUriFragment());
					}
				}
			}


		});

		//Paramétrage du comportement en cas de perte de connexion
		configReconnectDialog();

		/* Construit le gestionnaire de vues utilisé par la barre d'adresse et pour naviguer dans le dossier d'un étudiant */
		navigator.addProvider(viewProvider);
		navigator.setErrorProvider(new ViewProvider() {
			@Override
			public String getViewName(final String viewAndParameters) {
				log.warn("navigator ErrorProvider getViewName : " + viewAndParameters);
				return ErreurView.NAME;
			}

			@Override
			public View getView(final String viewName) {
				log.warn("navigator ErrorProvider - getView " + viewName);
				return viewProvider.getView(ErreurView.NAME);
			}
		});


		navigator.addViewChangeListener(new ViewChangeListener() {
			private static final String SELECTED_ITEM = "selected";

			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {

				log.debug("beforeViewChange " + event.getViewName());

				//Avant de se rendre sur une vue, on supprime le style "selected" des objets du menu
				viewButtons.values().forEach(button -> button.removeStyleName(SELECTED_ITEM));

				//Si on tente d'accéder à la vue admin et que l'utilisateur est admin
				if(event.getViewName().equals(AdminView.NAME) && userController.userCanAccessAdminView()){
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
				// Si la vue demandée est une vue dédiée aux enseignants
				if(Utils.isViewEnseignant(event.getViewName())){
					//Si utilisateur n'est pas enseignant ou gestionnaire
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
							navigateToListeInscrits();
							return true;
						}
						if(event.getViewName().equals(RechercheRapideView.NAME)){
							navigateToRechercheRapide();
							return true;
						}
						if(event.getViewName().equals(RechercheArborescenteView.NAME)){
							navigateToRechercheArborescente();
							return true;
						}

						return false; //la vue enseignant demandée n'est pas gérée (ex :vue mobile appelée depuis la version desktop)
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
			String fragment = Page.getCurrent().getUriFragment();

			// Si dans le cas d'un admin voulant accéder à l'adminView
			if(fragmentVersView(fragment, adminView.NAME) && userController.userCanAccessAdminView()){
				displayViewFullScreen(AdminView.NAME);
			} else {

				// Si application non active 
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

						//Ce tabSheet sera decalé vers le haut
						// tabSheetGlobal.addStyleName("top-shift-tabs");

						//Le menu horizontal pour les enseignants est définit comme étant le contenu de la page
						Utils.ajoutLogoBandeauEnseignant(configController.getLogoUniversiteEns(), mainVerticalLayout, applicationContext.getMessage("mainUI.app.title",null, UI.getCurrent().getLocale()));

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
						etudiant = new Etudiant(userController.getCodetu());
						log.debug("MainUI etudiant : "+etudiant.getCod_etu()+"-"+MainUI.getCurrent().getEtudiant().getCod_etu());
						//Récupération de l'état-civil (et les adresses)
						etudiantController.recupererEtatCivil();
						//On construit le menu affiché à l'étudiant
						buildMainMenuEtudiant();
					}


					/* Enregistre l'UI pour la réception de notifications */
					uiController.registerUI(this);

					boolean navigationComplete=false;


					if (fragment != null && !fragment.isEmpty()) {
						//Cas de l'appel initial de l'application via l'url vers la vue admin (sinon le cas est géré dans le listener du navigator
						if(fragmentVersView(fragment, adminView.NAME) && userController.userCanAccessAdminView()){
							//Afficher la vue admin
							navigator.navigateTo(AdminView.NAME);
							navigationComplete=true;
						}
						if(fragment.contains(Utils.FRAGMENT_ACCES_DOSSIER_ETUDIANT) && userController.isEnseignant()){
							rechercheController.accessToDossierEtudiantDeepLinking(fragment);
							navigationComplete=true;
						}
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
								if(lfav!=null && !lfav.isEmpty()){
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
									//Si on demande à accéder directement à la vue notesView
									if(fragment!=null && fragment.contains(NotesView.NAME)){
										//On affiche la vue notes
										navigator.navigateTo(NotesView.NAME);
									}else{
										//Si on demande à accéder directement à la vue calendrier et que la vue est activée
										if(fragment!=null && fragment.contains(CalendrierView.NAME) && configController.isAffCalendrierEpreuvesEtudiant()){
											//On affiche la vue Calendrier
											navigator.navigateTo(CalendrierView.NAME);
										}else{
											//On affiche la vue de l'état-civil
											navigator.navigateTo(EtatCivilView.NAME);
											//Affichage du message d'intro si besoin
											afficherMessageIntroEtudiants();
										}
									}
								}else{
									//On affiche la vue d'erreur
									displayViewFullScreen(ErreurView.NAME);
								}
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

	private boolean fragmentVersView(String fragment, String viewName) {
		return fragment != null && fragment.equals(FRAGMENT_PREFIX + viewName);
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
	 * Affichage message d'erreur
	 */
	public void afficherErreurView() {
		log.debug("afficherErreurView");
		navigator.navigateTo(ErreurView.NAME);
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
			HelpWindow hbw = helpWindowFactory.getObject();
			hbw.init(text,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),displayCheckBox);

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

			public void selectedTabChange(TabSheet.SelectedTabChangeEvent event){
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
		log.debug("Création du l'onglet du dossier de l'étudiant");
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
		if(etudiant != null){

			//Ajout du style au menu
			mainMenu.setPrimaryStyleName(ValoTheme.MENU_PART);
			//On fixe la largeur du menu
			mainMenu.setWidth("233px");

			// On affiche che le bandeau que si le user est étudiant (sinon le bandeau est déjà présent dans l'interface)
			if (userController.isEtudiant()) {
				//Ajout du logo de l'université et du titre de l'application
				Utils.ajoutLogoBandeauMenu(configController.getLogoUniversiteEtu(), mainMenu, applicationContext.getMessage("mainUI.app.title", null, UI.getCurrent().getLocale()));
			}

			//Si on a une url pour la photo de l'étudiant
			if(etudiant.getPhoto() != null){
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
				fotoEtudiant.addStyleName("photo-mdw");
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
			usernameLabel.addStyleName("retourALaLigneAutomatique");
			usernameLabel.setSizeUndefined();
			mainMenu.addComponent(usernameLabel);

			/* Etat Civil */
			addItemMenu(applicationContext.getMessage(EtatCivilView.NAME + ".title", null, getLocale()), EtatCivilView.NAME, FontAwesome.USER);

			//info annuelles 
			if(userController.isEtudiant() || 
					(userController.isEnseignant() && configController.isAffInfosAnnuellesEnseignant()) || 
					(userController.isGestionnaire() && configController.isAffInfosAnnuellesGestionnaire())){
				//visibles que si l'étudiant a des infos annuelles à afficher
				if(!etudiant.getInfosAnnuelles().isEmpty()){
					addItemMenu(applicationContext.getMessage(InformationsAnnuellesView.NAME + ".title", null, getLocale()), InformationsAnnuellesView.NAME, FontAwesome.INFO_CIRCLE);
				}
			}

			/* Adresses */
			if(userController.isEtudiant() || 
					(userController.isEnseignant() && configController.isAffAdressesEnseignant()) || 
					(userController.isGestionnaire() && configController.isAffAdressesGestionnaire())){
				addItemMenu(applicationContext.getMessage(AdressesView.NAME + ".title", null, getLocale()), AdressesView.NAME, FontAwesome.HOME);
			}


			/* Inscriptions */
			addItemMenu(applicationContext.getMessage(InscriptionsView.NAME + ".title", null, getLocale()), InscriptionsView.NAME, FontAwesome.FILE_TEXT);

			/* Calendrier */
			if((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiant())  || 
					(userController.isEnseignant() && configController.isAffCalendrierEpreuvesEnseignant()) || 
					(userController.isGestionnaire() && configController.isAffCalendrierEpreuvesGestionnaire())){
				addItemMenu(applicationContext.getMessage(CalendrierView.NAME + ".title", null, getLocale()), CalendrierView.NAME, FontAwesome.CALENDAR);
			}

			/* Notes et Résultats */
			if((userController.isEtudiant() && configController.isAffNotesEtudiant()) || 
					(userController.isEnseignant() && configController.isAffNotesEnseignant()) ||
					(userController.isGestionnaire() && configController.isAffNotesGestionnaire())){
				addItemMenu(applicationContext.getMessage(NotesView.NAME + ".title", null, getLocale()), NotesView.NAME, FontAwesome.LIST);
			}

			/* Séparation avant Bouton "Aide" */
			CssLayout bottomMainMenu1 = new CssLayout();
			bottomMainMenu1.setStyleName(ValoTheme.MENU_SUBTITLE);
			bottomMainMenu1.setSizeUndefined();
			mainMenu.addComponent(bottomMainMenu1);

			/* Aide */
			Button helpBtn = new Button(applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()), FontAwesome.SUPPORT);
			helpBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);
			HelpBasicWindow hbw = helpBasicWindowFactory.getObject();
			hbw.init(applicationContext.getMessage("helpWindow.text.etudiant", null, getLocale()),applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),true);
			helpBtn.addClickListener(e -> {
				UI.getCurrent().addWindow(hbw);});
			mainMenu.addComponent(helpBtn);

			/* Deconnexion */
			//Voir si on peut accéder à l'appli hors ENT, le détecter, et afficher le bouton déconnexion
			if(configController.isLogoutCasPropose() && userController.isEtudiant()){
				Button decoBtn = new Button(applicationContext.getMessage("buttonDeconnexion.label", null, getLocale()), FontAwesome.SIGN_OUT);
				decoBtn.setPrimaryStyleName(ValoTheme.MENU_ITEM);
				decoBtn.addClickListener(e -> {
					userController.disconnectUser();
					getUI().getPage().setLocation("logout");
				});
				mainMenu.addComponent(decoBtn);
			}
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
	 */
	private void navigateToRechercheArborescente() {
		log.debug("MainUI "+userController.getCurrentUserName()+" navigateToRechercheArborescente");
		//récupération de l'onglet qui affiche la vue RechercheArborescente
		int numtab = viewEnseignantTab.get(rechercheArborescenteView.NAME);
		//Si on a des paramètres renseignés
		if(urlParameterMapRechArb!=null){
			//initialisation de la vue avec les paramètres (on se place sur un élément précis de l'arborescence)
			rechercheArborescenteView.initFromParameters(urlParameterMapRechArb);
		}
		//On sélectionne l'onglet pour afficher la vue
		tabSheetEnseignant.setSelectedTab(numtab);
	}

	/**
	 * Affichage de la vue Liste Inscrits
	 */
	private void navigateToListeInscrits() {
		log.debug("MainUI "+userController.getCurrentUserName()+" navigateToListeInscrits");
		//récupération de l'onglet qui affiche la vue ListeInscrits
		int numtab = viewEnseignantTab.get(listeInscritsView.NAME);

		//Si on a des paramètres renseignés
		if(urlParameterMapListeInscrits != null){
			//Récupération de la liste des inscrits en fonction des paramètres
			listeInscritsController.recupererLaListeDesInscrits(urlParameterMapListeInscrits, null, this);
			//initialisation de la vue avec la liste des inscrits
			listeInscritsView.initListe();
		}
		//Si l'onglet a été closed
		if(tabSheetEnseignant.getTab(numtab)==null){
			//On recrée l'onglet
			addTabListeInscrits();
		}

		// Au cas où on soit sur l'onglet "Dossier"
		tabSheetGlobal.setSelectedTab(rangTabRecherche);

		// On affiche l'onglet
		tabSheetEnseignant.getTab(numtab).setVisible(true);

		// On se rend sur l'onglet pour afficher la vue ListeInscrits
		tabSheetEnseignant.setSelectedTab(numtab);
	}

	public void goTo(String view, Map<String, String> parameterMap) {
		if(view != null && view.equals(RechercheArborescenteView.NAME)) {
			urlParameterMapRechArb = parameterMap;
		}
		if(view != null && view.equals(ListeInscritsView.NAME)) {
			urlParameterMapListeInscrits = parameterMap;
		}
		navigator.navigateTo(view);
	}

	/**
	 * Affichage de la vue des favoris
	 */
	private void navigateToFavoris() {
		log.debug("MainUI "+userController.getCurrentUserName()+" navigateToFavoris");
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
	private void navigateToRechercheRapide() {
		log.debug("MainUI "+userController.getCurrentUserName()+" navigateToRechercheRapide");
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

		log.debug("MainUI "+userController.getCurrentUserName()+" navigateToDossierEtudiant : "+etudiant.getCod_etu());

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

		//par défaut on affiche la vue état-civil
		navigator.navigateTo(EtatCivilView.NAME);

		//On se rend sur l'onglet "Dossier" dans le tabSheet principal
		tabSheetGlobal.setSelectedTab(rangTabDossierEtudiant);

	}

	public void startBusyIndicator() {
		addWindow(loadingIndicatorWindow);
	}

	public void stopBusyIndicator() {
		try {
			//Ajout d'un sleep pour palier au cas ou le busy indicator n'a pas encore été affiché.
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		loadingIndicatorWindow.close();

	}


	private boolean applicationActive(){
		return configController.isApplicationActive() && ((userController.isEtudiant() && configController.isPartieEtudiantActive()) 
				|| (!userController.isEnseignant() && !userController.isEtudiant() && !userController.isGestionnaire()) 
				|| (userController.isEnseignant() && configController.isPartieEnseignantActive())
				|| (userController.isGestionnaire() && configController.isProfilGestionnaireActif()));
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
			Page.getCurrent().setLocation(PropertyUtils.getAppUrl());
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
