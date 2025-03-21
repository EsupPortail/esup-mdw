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


import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.utils.CssUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.HelpMobileWindow;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Favoris sur mobile
 */
@Component @Scope("prototype")
@SpringView(name = FavorisMobileView.NAME)
@PreAuthorize("@userController.hasRoleInProperty('teacher') || @userController.hasRoleInProperty('gestionnaire')")
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
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient ObjectFactory<HelpMobileWindow> helpMobileWindowFactory;

	/** Thread pool  */
	ExecutorService executorService = Executors.newSingleThreadExecutor();
	private MenuBar menuBar;

	private List<String> liste_types_inscrits;

	private List<String> liste_type_arbo;

	private VerticalLayout contentLayout = new VerticalLayout();

	private BeanItemContainer<Favoris> bic;

	private HorizontalLayout labelAucunFavoriLayout ;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI && userController.isEnseignant() ){
			removeAllComponents();

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
			navbar.setHeight(CssUtils.NAVBAR_HEIGHT);
			navbar.setStyleName("navigation-bar");

			Utils.ajoutLogoBandeau(configController.getLogoUniversiteMobile(), navbar);

			//Title
			Label labelFav = new Label(applicationContext.getMessage(NAME + ".title.label", null, getLocale()));
			labelFav.setStyleName("v-label-navbar");
			navbar.addComponent(labelFav);
			navbar.setComponentAlignment(labelFav, Alignment.MIDDLE_CENTER);

			navbar.setExpandRatio(labelFav, 1);
			addComponent(navbar);

			menuBar = new MenuBar();
			menuBar.setStyleName("v-menubar-mobile");
			MenuBar.MenuItem ellipsisItem = menuBar.addItem("", FontAwesome.ELLIPSIS_V, null);
			ellipsisItem.setStyleName("ellipsis-icon");

			MenuBar.MenuItem rechercheItem = ellipsisItem.addItem(applicationContext.getMessage(NAME + ".menu.recherche", null, getLocale()),FontAwesome.SEARCH, new MenuBar.Command() {
				@Override
				public void menuSelected(MenuBar.MenuItem selectedItem) {
					((MdwTouchkitUI)MdwTouchkitUI.getCurrent()).navigateToRecherche(NAME);
				}
			});

			MenuBar.MenuItem informationItem = ellipsisItem.addItem(applicationContext.getMessage(NAME + ".menu.information", null, getLocale()), FontAwesome.INFO, new MenuBar.Command() {
				@Override
				public void menuSelected(MenuBar.MenuItem selectedItem) {
					String message = applicationContext.getMessage("helpWindowMobile.text.enseignant", null, getLocale());
					HelpMobileWindow hbw = helpMobileWindowFactory.getObject();
					hbw.init(message,applicationContext.getMessage("messageIntroMobileWindow.title", null, getLocale()),false);
					UI.getCurrent().addWindow(hbw);
				}
			});

			navbar.addComponent(menuBar);
			navbar.setComponentAlignment(menuBar, Alignment.MIDDLE_RIGHT);

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
			infoLabel.setWidth("100%");

			labelLayout.addComponent(infoLabel);
			globalLayout.addComponent(labelLayout);

			if(lfav != null && lfav.size()>0){
				ajouteTypeFav(globalLayout, lfav, Utils.VET, applicationContext.getMessage(NAME + ".vetpanel.title", null, getLocale()));
				ajouteTypeFav(globalLayout, lfav, Utils.ELP, applicationContext.getMessage(NAME + ".elppanel.title", null, getLocale()));
			}

			labelAucunFavoriLayout = new HorizontalLayout();
			labelAucunFavoriLayout.setMargin(true);
			labelAucunFavoriLayout.setSizeFull();
			Button aucunFavoris = new Button(applicationContext.getMessage(NAME + ".favoris.aucun", null, getLocale()));
			aucunFavoris.setStyleName("v-nav-button");
			aucunFavoris.addStyleName(ValoTheme.BUTTON_LINK);
			aucunFavoris.addClickListener(e->{
				((MdwTouchkitUI)MdwTouchkitUI.getCurrent()).navigateToRecherche(NAME);
			});
			
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

	private void ajouteTypeFav(VerticalLayout globalLayout, List<Favoris> lfav, String typeFav, String titre) {
		if(favorisContientTypeFav(lfav, typeFav)){
			Panel favPanel = new Panel(titre);
			favPanel.setStyleName("lefttitle-panel");
			favPanel.addStyleName("v-medium-panel-caption");
			favPanel.setSizeFull();
			favPanel.setContent(getFavLayout(lfav,typeFav));
			globalLayout.addComponent(favPanel);
		}
	}

	private com.vaadin.ui.Component getFavLayout(List<Favoris> lfav, String typeFav) {

		VerticalLayout favLayout = new VerticalLayout();
		favLayout.setSizeFull();
		favLayout.setHeight(null);
		for(Favoris fav :  lfav) {
			if (fav.getId().getTypfav().equals(typeFav)) {

				HorizontalLayout favVetLayout = new HorizontalLayout();
				favVetLayout.setSizeFull();
				favVetLayout.setMargin(true);
				favVetLayout.setSpacing(true);
				favVetLayout.setStyleName("v-layout-multiline");
				favVetLayout.setWidth("100%");
				favVetLayout.setHeight("100%");

				VerticalLayout liblayout = new VerticalLayout();
				liblayout.addComponent(new Label(fav.getId().getIdfav()));
				Label libelle = new Label(favorisController.getLibObjFavori(fav.getId().getTypfav(), fav.getId().getIdfav()));
				libelle.setStyleName(ValoTheme.LABEL_SMALL);
				liblayout.addComponent(libelle);

				HorizontalLayout blayout = new HorizontalLayout();
				Button actionButton = new Button();
				actionButton.setIcon(FontAwesome.USERS);
				actionButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
				actionButton.addClickListener(e -> {
					accessToDetail(fav.getId().getIdfav(), fav.getId().getTypfav());
				});
				blayout.addComponent(actionButton);
				//blayout.setComponentAlignment(actionButton, Alignment.MIDDLE_RIGHT);

				favVetLayout.addComponent(liblayout);
				favVetLayout.addComponent(blayout);
				favVetLayout.setExpandRatio(liblayout, 1);

				favLayout.addComponent(favVetLayout);
			}
		}
		return favLayout;
	}


	private void accessToDetail(String id, String type) {
			//On ne doit pas afficher de fenêtre de loading, on exécute directement la méthode
			rechercheController.accessToMobileDetail(id,type,false);
	}
	

	private boolean favorisContientTypeFav(List<Favoris> lfav, String typeFacv) {
		for(Favoris fav :  lfav){
			if(fav.getId().getTypfav().equals(typeFacv)){
				return true;
			}
		}
		return false;
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
		//LOG.debug("ENTER FAVORIS VIEW");
	}




}
