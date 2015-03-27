package fr.univlorraine.mondossierweb.views;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.Favoris;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.HelpMobileWindow;

/**
 * Favoris sur mobile
 */
@Component @Scope("prototype")
@VaadinView(FavorisMobileView.NAME)
@PreAuthorize("hasRole('teacher')")
public class FavorisMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "favorisMobileView";

	public static final String[] FAV_FIELDS_ORDER = {"id.idfav"};



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient RechercheController rechercheController;

	/** Thread pool  */
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	private Button infoButton;

	private List<String> liste_types_inscrits;

	private List<String> liste_type_arbo;

	private CssLayout contentLayout = new CssLayout();

	private BeanItemContainer<Favoris> bic;

	private HorizontalLayout labelAucunFavoriLayout ;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if(userController.isEnseignant() ){
			removeAllComponents();

			//((MdwTouchkitUI)MdwTouchkitUI.getCurrent()).checkMenuIsDisplayed();

			/* Style */
			setSizeFull();

			liste_types_inscrits= new LinkedList<String>();
			liste_types_inscrits.add("ELP");
			liste_types_inscrits.add("VET");

			liste_type_arbo= new LinkedList<String>();
			liste_type_arbo.add("CMP");
			liste_type_arbo.add("VET");

			List<Favoris> lfav = favorisController.getFavoris();


			//NAVBAR
			HorizontalLayout navbar=new HorizontalLayout();
			navbar.setSizeFull();
			navbar.setHeight("40px");
			navbar.setStyleName("navigation-bar");

			//Bouton retour
			infoButton = new Button();
			infoButton.setIcon(FontAwesome.INFO);
			infoButton.setStyleName("v-nav-button");
			infoButton.addClickListener(e->{
				//afficher message
				HelpMobileWindow hbw = new HelpMobileWindow(applicationContext.getMessage("helpWindowMobile.text.enseignant", null, getLocale()),applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),false);

				UI.getCurrent().addWindow(hbw);
			});
			navbar.addComponent(infoButton);
			navbar.setComponentAlignment(infoButton, Alignment.MIDDLE_LEFT);

			//Title
			Label labelFav = new Label(applicationContext.getMessage(NAME + ".title.label", null, getLocale()));
			labelFav.setStyleName("v-label-navbar");
			navbar.addComponent(labelFav);
			navbar.setComponentAlignment(labelFav, Alignment.MIDDLE_CENTER);

			//Bouton Search
			Button searchButton = new Button();
			searchButton.setIcon(FontAwesome.SEARCH);
			searchButton.setStyleName("v-nav-button");
			navbar.addComponent(searchButton);
			navbar.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
			searchButton.addClickListener(e->{
				((MdwTouchkitUI)MdwTouchkitUI.getCurrent()).navigateToRecherche();
			});
			navbar.setExpandRatio(labelFav, 1);
			addComponent(navbar);



			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setSizeFull();
			globalLayout.setSpacing(true);
			globalLayout.setMargin(true);


			FormLayout labelLayout = new FormLayout();
			labelLayout.setSizeFull();
			labelLayout.setMargin(false);
			labelLayout.setSpacing(false);


			Label infoLabel = new Label(applicationContext.getMessage(NAME + ".info.label", null, getLocale()));
			infoLabel.setStyleName(ValoTheme.LABEL_SMALL);
			infoLabel.setIcon(FontAwesome.INFO_CIRCLE);

			labelLayout.addComponent(infoLabel);
			globalLayout.addComponent(labelLayout);

			if(lfav!=null && lfav.size()>0){
				if(favorisContientVet(lfav)){

					Panel vetPanel = new Panel(applicationContext.getMessage(NAME + ".vetpanel.title", null, getLocale()));
					vetPanel.setStyleName("centertitle-panel");
					vetPanel.addStyleName("v-colored-panel-caption");
					vetPanel.setSizeFull();

					VerticalLayout vetLayout = new VerticalLayout();
					vetLayout.setSizeFull();
					int i=0;
					for(Favoris fav :  lfav){
						if(fav.getId().getTypfav().equals(Utils.VET)){
							i++;

							HorizontalLayout favVetLayout = new HorizontalLayout();
							favVetLayout.setMargin(true);
							favVetLayout.setSpacing(true);
							favVetLayout.setStyleName("v-layout-multiline");
							favVetLayout.setWidth("100%");
							favVetLayout.setHeight("100%");

							Button codeButton = new Button(fav.getId().getIdfav());
							codeButton.setStyleName("link"); 
							codeButton.addStyleName("v-link");
							codeButton.setWidth("90px");
							codeButton.addClickListener(e->{
								accessToDetail(fav.getId().getIdfav(),fav.getId().getTypfav());
							});


							Button libButton = new Button(favorisController.getLibObjFavori(fav.getId().getTypfav(),fav.getId().getIdfav()));
							libButton.setStyleName("v-button-multiline");
							libButton.addStyleName("link"); 
							libButton.addStyleName("v-link");
							libButton.setHeight("100%");
							libButton.setWidth("100%");
							libButton.addClickListener(e->{
								accessToDetail(fav.getId().getIdfav(),fav.getId().getTypfav());
							});

							favVetLayout.addComponent(codeButton);
							favVetLayout.setComponentAlignment(codeButton, Alignment.MIDDLE_CENTER);
							favVetLayout.addComponent(libButton);
							favVetLayout.setComponentAlignment(libButton, Alignment.MIDDLE_CENTER);
							favVetLayout.setExpandRatio(libButton, 1);
							vetLayout.addComponent(favVetLayout);
							if(i>1){
								favVetLayout.addStyleName("line-separator");
							}
						}
					}
					vetPanel.setContent(vetLayout);
					globalLayout.addComponent(vetPanel);

				}

				if(favorisContientElp(lfav)){
					Panel elpPanel = new Panel(applicationContext.getMessage(NAME + ".elppanel.title", null, getLocale()));
					elpPanel.setStyleName("centertitle-panel");
					elpPanel.addStyleName("v-colored-panel-caption");
					elpPanel.setSizeFull();

					VerticalLayout elpLayout = new VerticalLayout();
					elpLayout.setSizeFull();
					int i=0;
					for(Favoris fav :  lfav){
						if(fav.getId().getTypfav().equals(Utils.ELP)){
							i++;
							HorizontalLayout favElpLayout = new HorizontalLayout();
							favElpLayout.setMargin(true);
							favElpLayout.setSpacing(true);
							favElpLayout.setStyleName("v-layout-multiline");
							favElpLayout.setWidth("100%");
							favElpLayout.setHeight("100%");


							Button codeButton = new Button(fav.getId().getIdfav());
							codeButton.setStyleName("link"); 
							codeButton.addStyleName("v-link");
							codeButton.setWidth("90px");
							codeButton.addClickListener(e->{
								accessToDetail(fav.getId().getIdfav(),fav.getId().getTypfav());
							});

							Button libButton = new Button(favorisController.getLibObjFavori(fav.getId().getTypfav(),fav.getId().getIdfav()));
							libButton.setStyleName("v-button-multiline");
							libButton.addStyleName("link"); 
							libButton.addStyleName("v-link");
							libButton.setHeight("100%");
							libButton.setWidth("100%");
							libButton.addClickListener(e->{
								accessToDetail(fav.getId().getIdfav(),fav.getId().getTypfav());
							});

							favElpLayout.addComponent(codeButton);
							favElpLayout.setComponentAlignment(codeButton, Alignment.MIDDLE_CENTER);
							favElpLayout.addComponent(libButton);
							favElpLayout.setComponentAlignment(libButton, Alignment.MIDDLE_CENTER);
							favElpLayout.setExpandRatio(libButton, 1);
							elpLayout.addComponent(favElpLayout);
							if(i>1){
								favElpLayout.addStyleName("line-separator");
							}
						}
					}
					elpPanel.setContent(elpLayout);
					globalLayout.addComponent(elpPanel);
				}



			}

			labelAucunFavoriLayout = new HorizontalLayout();
			labelAucunFavoriLayout.setMargin(true);
			labelAucunFavoriLayout.setSizeFull();
			Label aucunFavoris = new Label(applicationContext.getMessage(NAME + ".favoris.aucun", null, getLocale()));
			aucunFavoris.setStyleName(ValoTheme.LABEL_COLORED);
			aucunFavoris.addStyleName(ValoTheme.LABEL_BOLD);
			labelAucunFavoriLayout.addComponent(aucunFavoris);
			labelAucunFavoriLayout.setVisible(false);
			globalLayout.addComponent(labelAucunFavoriLayout);

			if(lfav==null || lfav.size()==0){
				labelAucunFavoriLayout.setVisible(true);
			}

			//addComponent(globalLayout);
			contentLayout.setStyleName("v-scrollableelement");
			contentLayout.addComponent(globalLayout);
			addComponent(contentLayout);
			setExpandRatio(contentLayout, 1);

		}
	}



	private void accessToDetail(String id, String type) {
		
		//Si on doit afficher une fenêtre de loading pendant l'exécution
		if(PropertyUtils.isShowLoadingIndicator()){
			//affichage de la pop-up de loading
			MdwTouchkitUI.getCurrent().startBusyIndicator();
			
			//Execution de la méthode en parallèle dans un thread
			executorService.execute(new Runnable() {
				public void run() {
					MdwTouchkitUI.getCurrent().access(new Runnable() {
						@Override
						public void run() {
							rechercheController.accessToMobileDetail(id,type,false);
							//close de la pop-up de loading
							MdwTouchkitUI.getCurrent().stopBusyIndicator();
						}
					} );
				}
			});
		}else{
			//On ne doit pas afficher de fenêtre de loading, on exécute directement la méthode
			rechercheController.accessToMobileDetail(id,type,false);
		}
	}
	



	private boolean favorisContientVet(List<Favoris> lfav) {
		for(Favoris fav :  lfav){
			if(fav.getId().getTypfav().equals(Utils.VET)){
				return true;
			}
		}
		return false;
	}

	private boolean favorisContientElp(List<Favoris> lfav) {
		for(Favoris fav :  lfav){
			if(fav.getId().getTypfav().equals(Utils.ELP)){
				return true;
			}
		}
		return false;
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//LOG.debug("ENTER FAVORIS VIEW");
	}




}
