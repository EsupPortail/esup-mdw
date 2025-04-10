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
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationCategorie;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.views.windows.PreferencesApplicationWindow;
import fr.univlorraine.mondossierweb.views.windows.SwapUtilisateurWindow;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Page admin
 */
@Component @Scope("prototype")
@SpringView(name = AdminView.NAME)
public class AdminView extends VerticalLayout implements View {

	public static final String[] CONF_APP_FIELDS_ORDER = {"prefId", "prefDesc", "valeur"};

	public static final String[] SWAP_FIELDS_ORDER = {"loginSource", "loginCible", "datCre"};


	public static final String NAME = "adminView";


	/* Injections */
	@Resource
	private transient Environment environment;
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient ObjectFactory<SwapUtilisateurWindow> swapUtilisateurWindowFactory;
	@Resource
	private transient ObjectFactory<PreferencesApplicationWindow> preferencesApplicationWindowFactory;
	



	//le tabSheet global affiché
	private TabSheet tabSheetGlobal;

	private Button btnEditSwap;
	private Button btnAddSwap;
	private Table confSwapTable;
	private int tabSelectedPosition;

	private VerticalLayout layoutConfigApplication;
	private VerticalLayout layoutSwapUser;
	private HorizontalLayout topLayout;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		//On vérifie le droit d'accéder à la vue
		if(userController.userCanAccessAdminView()){
			removeAllComponents();
			/* Style */
			setMargin(true);
			setSpacing(true);

			/* En-tete menu large */
			topLayout = new HorizontalLayout();
			topLayout.addStyleName(ValoTheme.MENU_TITLE);
			topLayout.setWidth(100, Unit.PERCENTAGE);
			topLayout.setSpacing(true);

			Label apptitle = new Label(environment.getRequiredProperty("app.name"));
			apptitle.addStyleName(ValoTheme.LABEL_HUGE);
			apptitle.addStyleName(ValoTheme.LABEL_BOLD);

			Button homeButton = new Button(FontAwesome.HOME);
			homeButton.addStyleName("admin-home-button");
			homeButton.addClickListener(e -> getUI().getPage().setLocation(PropertyUtils.getAppUrl()));

			CssLayout titleLayout = new CssLayout();
			titleLayout.setWidthFull();
			titleLayout.addStyleName("admin-title-layout");
			titleLayout.addComponents(apptitle, homeButton);

			Label versionLabel = new Label("v" + environment.getRequiredProperty("app.version"));
			versionLabel.addStyleName(ValoTheme.LABEL_TINY);

			VerticalLayout appTitleLayout = new VerticalLayout(titleLayout, versionLabel);
			topLayout.addComponent(appTitleLayout);
			topLayout.setComponentAlignment(appTitleLayout, Alignment.MIDDLE_LEFT);
			topLayout.setExpandRatio(appTitleLayout, 1);

			addComponent(topLayout);

			tabSheetGlobal = new TabSheet();
			tabSheetGlobal.setSizeFull();
			tabSheetGlobal.addStyleName(ValoTheme.TABSHEET_FRAMED);

			/* Récupération des Categories de configuration */
			List<PreferencesApplicationCategorie> categories = configController.getCategoriesOrderByOrdre();

			int tabNumber = 0;
			for(PreferencesApplicationCategorie categorie : categories){
				layoutConfigApplication = new VerticalLayout();
				layoutConfigApplication.setSizeFull();
				ajoutGestionParametresApplicatifs(categorie,tabNumber);
				tabSheetGlobal.addTab(layoutConfigApplication, categorie.getCatDesc(), FontAwesome.COGS);
				tabNumber++;
			}

			//ajout de l'onglet 'swap'
			layoutSwapUser = new VerticalLayout();
			layoutSwapUser.setSizeFull();
			ajoutGestionSwap(tabNumber);
			tabSheetGlobal.addTab(layoutSwapUser, "Swap utilisateur", FontAwesome.GROUP);

			tabSheetGlobal.setSelectedTab(tabSelectedPosition);
			//Le menu horizontal pour les enseignants est définit comme étant le contenu de la page
			addComponent(tabSheetGlobal);
			setExpandRatio(tabSheetGlobal, 1);
		}
	}

	private void ajoutGestionParametresApplicatifs(PreferencesApplicationCategorie categorie,int tabNumber) {

		layoutConfigApplication.setMargin(true);
		layoutConfigApplication.setSpacing(true);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layoutConfigApplication.addComponent(buttonsLayout);
		Button btnEdit = new Button(applicationContext.getMessage(NAME+".btnEdit", null, getLocale()), FontAwesome.PENCIL);
		btnEdit.setEnabled(false);
		btnEdit.addStyleName("admin-button");
		buttonsLayout.addComponent(btnEdit);
		buttonsLayout.setComponentAlignment(btnEdit, Alignment.MIDDLE_RIGHT);

		/* Table des conf */
		Table confAppTable = new Table(null, new BeanItemContainer<>(PreferencesApplication.class, configController.getAppParametersForCatId(categorie.getCatId())));
		confAppTable.setSizeFull();
		confAppTable.setVisibleColumns((Object[]) CONF_APP_FIELDS_ORDER);
		for (String fieldName : CONF_APP_FIELDS_ORDER) {
			confAppTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".confAppTable." + fieldName, null, getLocale()));
		}
		confAppTable.setSortContainerPropertyId("prefId");
		confAppTable.setColumnCollapsingAllowed(true);
		confAppTable.setColumnReorderingAllowed(true);
		confAppTable.setSelectable(true);
		confAppTable.setImmediate(true);
		confAppTable.addItemSetChangeListener(e -> confAppTable.sanitizeSelection());
		confAppTable.addValueChangeListener(e -> {
			// Le bouton d'édition est actif seulement si un parametre est sélectionné. 
			boolean confIsSelected = confAppTable.getValue() instanceof PreferencesApplication;
			btnEdit.setEnabled(confIsSelected);

		});
		confAppTable.addItemClickListener(e -> {
			if (e.isDoubleClick()) {
				confAppTable.select(e.getItemId());
				btnEdit.click();
			}
		});
		
		btnEdit.addClickListener(e -> {
			if (confAppTable.getValue() instanceof PreferencesApplication) {
				//configController.editConfApp((PreferencesApplication) confAppTable.getValue());
				PreferencesApplicationWindow paw = preferencesApplicationWindowFactory.getObject();
				paw.init((PreferencesApplication) confAppTable.getValue());
				paw.addCloseListener(f->init());
				tabSelectedPosition=tabNumber;
				MainUI.getCurrent().addWindow(paw);
			}
		});
		
		layoutConfigApplication.addComponent(confAppTable);
		layoutConfigApplication.setExpandRatio(confAppTable, 1);


	}



	private void ajoutGestionSwap(int tabNumber) {

		layoutSwapUser.setMargin(true);
		layoutSwapUser.setSpacing(true);
		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layoutSwapUser.addComponent(buttonsLayout);

		btnEditSwap = new Button(applicationContext.getMessage(NAME+".btnEdit", null, getLocale()), FontAwesome.PENCIL);
		btnEditSwap.setEnabled(false);
		btnEditSwap.addClickListener(e -> {
			if (confSwapTable.getValue() instanceof UtilisateurSwap) {
				SwapUtilisateurWindow suw = swapUtilisateurWindowFactory.getObject();
				suw.init((UtilisateurSwap) confSwapTable.getValue(), false);
				suw.addCloseListener(f->init());
				tabSelectedPosition=tabNumber;
				MainUI.getCurrent().addWindow(suw);
			}
		});
		buttonsLayout.addComponent(btnEditSwap);
		btnEditSwap.addStyleName("admin-button");
		buttonsLayout.setComponentAlignment(btnEditSwap, Alignment.MIDDLE_LEFT);

		btnAddSwap = new Button(applicationContext.getMessage(NAME+".btnAdd", null, getLocale()), FontAwesome.PLUS);
		btnAddSwap.setEnabled(true);
		btnAddSwap.addStyleName("admin-button");
		btnAddSwap.addClickListener(e -> {
			SwapUtilisateurWindow suw = swapUtilisateurWindowFactory.getObject();
			suw.init(new UtilisateurSwap(), true);
			suw.addCloseListener(f->init());
			tabSelectedPosition=tabNumber;
			MainUI.getCurrent().addWindow(suw);

		});
		buttonsLayout.addComponent(btnAddSwap);
		buttonsLayout.setComponentAlignment(btnAddSwap, Alignment.MIDDLE_CENTER);

		// Deconnexion 
		Button decoBtn = new Button("Se Déconnecter", FontAwesome.SIGN_OUT);
		decoBtn.setEnabled(true);
		decoBtn.addStyleName("admin-button");
		decoBtn.addClickListener(e -> {
			userController.disconnectUser();
			getUI().getPage().setLocation("logout");
		});
		buttonsLayout.addComponent(decoBtn);
		buttonsLayout.setComponentAlignment(decoBtn, Alignment.MIDDLE_RIGHT);


		/* Table des conf */
		confSwapTable = new Table(null, new BeanItemContainer<>(UtilisateurSwap.class, configController.getSwapUtilisateurs()));
		confSwapTable.setSizeFull();
		confSwapTable.setVisibleColumns((Object[]) SWAP_FIELDS_ORDER);
		for (String fieldName : SWAP_FIELDS_ORDER) {
			confSwapTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".confSwapTable." + fieldName, null, getLocale()));
		}
		confSwapTable.setSortContainerPropertyId("loginSource");
		confSwapTable.setColumnCollapsingAllowed(true);
		confSwapTable.setColumnReorderingAllowed(true);
		confSwapTable.setSelectable(true);
		confSwapTable.setImmediate(true);
		confSwapTable.addItemSetChangeListener(e -> confSwapTable.sanitizeSelection());
		confSwapTable.addValueChangeListener(e -> {
			// Le bouton d'édition est actif seulement si un parametre est sélectionné. 
			boolean confIsSelected = confSwapTable.getValue() instanceof UtilisateurSwap;
			btnEditSwap.setEnabled(confIsSelected);

		});
		confSwapTable.addItemClickListener(e -> {
			if (e.isDoubleClick()) {
				confSwapTable.select(e.getItemId());
				btnEditSwap.click();
			}
		});
		layoutSwapUser.addComponent(confSwapTable);
		layoutSwapUser.setExpandRatio(confSwapTable, 1);
	}

	/**
	 * @see com.vaadin.navigator.View${symbol_pound}enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
	}

}
