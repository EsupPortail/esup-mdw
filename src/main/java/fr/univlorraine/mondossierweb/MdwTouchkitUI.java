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
import com.vaadin.addon.touchkit.ui.TabBarView;
import com.vaadin.addon.touchkit.ui.TabBarView.SelectedTabChangeEvent;
import com.vaadin.addon.touchkit.ui.TabBarView.SelectedTabChangeListener;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.ui.UIState.NotificationTypeConfiguration;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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
import fr.univlorraine.mondossierweb.views.RechercheRapideView;
import fr.univlorraine.mondossierweb.views.windows.HelpMobileWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpWindow;
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
	
	



	@Getter
	private String trombinoscopeFromView;
	
	@Getter
	private String dossierEtuFromView;


	private NavigationManager noteNavigationManager;

	private TabBarView menuEtudiant;

	private Tab tabInfoAnnuelles;

	private Tab tabCalendrier;

	private Tab tabNotes;
	
	@Setter
	@Getter
	private Etape etapeDetailNotes;
	
	//vrai si on consulte les notes en vue enseignant
	@Setter
	@Getter
	private boolean vueEnseignantNotesEtResultats;

	// private VerticalLayout mainLayout= new VerticalLayout();
	//private CssLayout menuLayout = new CssLayout();
	private CssLayout contentLayout = new CssLayout();
	//private TabBarView menuEnseignant;

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
					//displayViewFullScreen(AccesRefuseView.NAME);
					navigator.navigateTo(AccesRefuseView.NAME);
					//navigator.navigateTo(AccesRefuseView.NAME);
					return;
				}
				cause = cause.getCause();
			}
			/* Traite les autres erreurs normalement */
			LOG.error(e.getThrowable().toString(), e.getThrowable());
			navigator.navigateTo(ErreurView.NAME);
		});

		setStyleName("v-noscrollableelement");

		contentLayout.setSizeFull();

		/* Affiche le nom de l'application dans l'onglet du navigateur */
		getPage().setTitle(environment.getRequiredProperty("app.name"));


		/* Device Detection */
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

		setContent(contentLayout);



		if(userController.isEnseignant() || userController.isEtudiant()){


			if(userController.isEnseignant()){

				//On consultera les notes en vue enseignant
				vueEnseignantNotesEtResultats=true;
				navigator.navigateTo(FavorisMobileView.NAME);
				afficherMessageIntroEnseignants();

			}else{
				//On consultera les notes en vue etudiant
				vueEnseignantNotesEtResultats=false;
				//User Etudiant
				etudiant = new Etudiant(daoCodeLoginEtudiant.getCodEtuFromLogin(userController.getCurrentUserName()));
				try{
				etudiantController.recupererEtatCivil();
				etudiantController.recupererCalendrierExamens();
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
	
			navigator.navigateTo(AccesRefuseView.NAME);
		}




	}


	private void afficherMessageIntroEnseignants() {
		afficherMessageIntro(applicationContext.getMessage("helpWindowMobile.text.enseignant", null, getLocale()));

	}
	
	private void afficherMessageIntro(String text){
		//Recuperer dans la base si l'utilisateur a demandé à ne plus afficher le message
		String val  = userController.getPreference(Utils.SHOW_MESSAGE_INTRO_MOBILE_PREFERENCE);
		boolean afficherMessage = true;
		if(StringUtils.hasText(val)){
			afficherMessage = Boolean.valueOf(val);
		}

		if(afficherMessage){
			HelpMobileWindow hbw = new HelpMobileWindow(text,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),true);
			hbw.addCloseListener(g->{
				boolean choix = hbw.getCheckBox().getValue();
				//Test si l'utilisateur a coché la case pour ne plus afficher le message
				if(choix){
					//mettre a jour dans la base de données
					userController.updatePreference(Utils.SHOW_MESSAGE_INTRO_MOBILE_PREFERENCE, "false");
				}
			});
			UI.getCurrent().addWindow(hbw);
		}
	}

	public void navigateToListeInscritsFromSearch(Map<String, String> parameterMap) {
		trombinoscopeFromView = RechercheMobileView.NAME;
		navigateToListeInscrits( parameterMap);
	}

	public void navigateToListeInscritsFromFavoris(Map<String, String> parameterMap) {
		trombinoscopeFromView = FavorisMobileView.NAME;
		navigateToListeInscrits( parameterMap);
	}

	public void navigateToDossierEtudiantFromListeInscrits() {
		dossierEtuFromView = ListeInscritsMobileView.NAME;
		navigateToDossierEtudiant();
	}
	
	public void navigateToDossierEtudiantFromSearch() {
		dossierEtuFromView = RechercheMobileView.NAME;
		navigateToDossierEtudiant();
	}
	
	public void navigateToDossierEtudiant() {

		informationsAnnuellesMobileView.refresh();
		calendrierMobileView.refresh();
		notesMobileView.refresh();

		
		if(menuEtudiant==null){
			initMenuEtudiant();
		}
		menuEtudiant.setSelectedTab(tabInfoAnnuelles);

		setContent(menuEtudiant);
	}

	private void initMenuEtudiant() {
		if(menuEtudiant==null){
			menuEtudiant = new TabBarView();

		}


		tabInfoAnnuelles = menuEtudiant.addTab(informationsAnnuellesMobileView, "Informations", FontAwesome.INFO);
		tabInfoAnnuelles.setId("tabInfoAnnuelles");


		tabCalendrier = menuEtudiant.addTab(calendrierMobileView, "Calendrier", FontAwesome.CALENDAR);
		tabCalendrier.setId("tabCalendrier");
		
		

		if(noteNavigationManager==null){
			noteNavigationManager= new NavigationManager();
		}
		noteNavigationManager.setCurrentComponent(notesMobileView);
		noteNavigationManager.setNextComponent(notesDetailMobileView);
		
	
		tabNotes = menuEtudiant.addTab(noteNavigationManager, "Résultats",  FontAwesome.LIST);
		tabNotes.setId("tabNotes");


	}

	public void navigateToListeInscrits(Map<String, String> parameterMap) {
		if(parameterMap!=null){
			listeInscritsController.recupererLaListeDesInscrits(parameterMap, null, this);
		}

		navigator.navigateTo(ListeInscritsMobileView.NAME);
	}

	public void navigateTofavoris() {
		navigator.navigateTo(FavorisMobileView.NAME);
	}
	
	public void navigateToRecherche() {
		setContent(contentLayout);
		navigator.navigateTo(RechercheMobileView.NAME);
	}

	public void navigateToListeInscrits() {
		setContent(contentLayout);
		navigator.navigateTo(ListeInscritsMobileView.NAME);
	}
	
	public void navigateToResumeNotes() {

		if(noteNavigationManager!=null){
			noteNavigationManager.navigateBack();
		}

			
	}
	
	public void navigateToDetailNotes(Etape etape) {

		if(noteNavigationManager!=null){
			notesDetailMobileView.refresh(etape, etudiant.getCod_etu());
			noteNavigationManager.navigateTo(notesDetailMobileView);
		}

			
	}

	


}
