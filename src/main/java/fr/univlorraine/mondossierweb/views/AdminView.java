/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import fr.univlorraine.mondossierweb.views.windows.PreferencesApplicationWindow;
import fr.univlorraine.mondossierweb.views.windows.SwapUtilisateurWindow;

/**
 * Page admin
 */
@Component @Scope("prototype")
@SpringView(name = AdminView.NAME)
public class AdminView extends VerticalLayout implements View {

	private static final long serialVersionUID = -2605429366219007314L;

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



	//le tabSheet global affiché
	private TabSheet tabSheetGlobal;
	private Button btnEdit;
	private Button btnEditSwap;
	private Button btnAddSwap;
	private Table confAppTable;
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

			Label Apptitle = new Label(environment.getRequiredProperty("app.name"));
			Apptitle.addStyleName(ValoTheme.LABEL_HUGE);
			Apptitle.addStyleName(ValoTheme.LABEL_BOLD);

			Label versionLabel = new Label("v" + environment.getRequiredProperty("app.version"));
			versionLabel.addStyleName(ValoTheme.LABEL_TINY);

			VerticalLayout appTitleLayout = new VerticalLayout(Apptitle, versionLabel);
			topLayout.addComponent(appTitleLayout);
			topLayout.setComponentAlignment(appTitleLayout, Alignment.MIDDLE_LEFT);
			topLayout.setExpandRatio(appTitleLayout, 1);

			addComponent(topLayout);

			// Titre 
			/*Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
			title.addStyleName(ValoTheme.LABEL_H1);
			addComponent(title);*/

			// Texte 
			//addComponent(new Label(applicationContext.getMessage(NAME + ".message", null, getLocale()), ContentMode.HTML));

			tabSheetGlobal = new TabSheet();
			tabSheetGlobal.setSizeFull();
			tabSheetGlobal.addStyleName(ValoTheme.TABSHEET_FRAMED);

			//ajout de l'onglet principal 'parametres'
			layoutConfigApplication = new VerticalLayout();
			layoutConfigApplication.setSizeFull();
			ajoutGestionParametresApplicatifs();
			tabSheetGlobal.addTab(layoutConfigApplication, "Paramètres de l'application", FontAwesome.COGS);


			//ajout de l'onglet 'swap'
			layoutSwapUser = new VerticalLayout();
			layoutSwapUser.setSizeFull();
			ajoutGestionSwap();
			tabSheetGlobal.addTab(layoutSwapUser, "Swap utilisateur", FontAwesome.GROUP);


			tabSheetGlobal.setSelectedTab(tabSelectedPosition);
			//Ce tabSheet sera aligné à droite
			//tabSheetGlobal.addStyleName("right-aligned-tabs");

			//Le menu horizontal pour les enseignants est définit comme étant le contenu de la page
			addComponent(tabSheetGlobal);
			setExpandRatio(tabSheetGlobal, 1);
		}
	}

	private void ajoutGestionParametresApplicatifs() {


		layoutConfigApplication.setMargin(true);
		layoutConfigApplication.setSpacing(true);
		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layoutConfigApplication.addComponent(buttonsLayout);


		btnEdit = new Button(applicationContext.getMessage(NAME+".btnEdit", null, getLocale()), FontAwesome.PENCIL);
		btnEdit.setEnabled(false);
		btnEdit.addClickListener(e -> {
			if (confAppTable.getValue() instanceof PreferencesApplication) {
				//configController.editConfApp((PreferencesApplication) confAppTable.getValue());
				PreferencesApplicationWindow paw = new PreferencesApplicationWindow((PreferencesApplication) confAppTable.getValue());
				paw.addCloseListener(f->init());
				tabSelectedPosition=0;
				MainUI.getCurrent().addWindow(paw);
			}
		});
		buttonsLayout.addComponent(btnEdit);
		buttonsLayout.setComponentAlignment(btnEdit, Alignment.MIDDLE_CENTER);


		/* Table des conf */
		confAppTable = new Table(null, new BeanItemContainer<>(PreferencesApplication.class, configController.getAppParameters()));
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
		layoutConfigApplication.addComponent(confAppTable);
		layoutConfigApplication.setExpandRatio(confAppTable, 1);
	}



	private void ajoutGestionSwap() {


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
				//configController.editConfApp((PreferencesApplication) confAppTable.getValue());
				SwapUtilisateurWindow suw = new SwapUtilisateurWindow((UtilisateurSwap) confSwapTable.getValue(), false);
				suw.addCloseListener(f->init());
				tabSelectedPosition=1;
				MainUI.getCurrent().addWindow(suw);
			}
		});
		buttonsLayout.addComponent(btnEditSwap);
		buttonsLayout.setComponentAlignment(btnEditSwap, Alignment.MIDDLE_LEFT);

		btnAddSwap = new Button(applicationContext.getMessage(NAME+".btnAdd", null, getLocale()), FontAwesome.PLUS);
		btnAddSwap.setEnabled(true);
		btnAddSwap.addClickListener(e -> {

			SwapUtilisateurWindow suw = new SwapUtilisateurWindow(new UtilisateurSwap(), true);
			suw.addCloseListener(f->init());
			tabSelectedPosition=1;
			MainUI.getCurrent().addWindow(suw);

		});
		buttonsLayout.addComponent(btnAddSwap);
		buttonsLayout.setComponentAlignment(btnAddSwap, Alignment.MIDDLE_CENTER);
		
		// Deconnexion 
		Button decoBtn = new Button("Se Déconnecter", FontAwesome.SIGN_OUT);
		decoBtn.setEnabled(true);
		decoBtn.addClickListener(e -> {
			getUI().getPage().setLocation("j_spring_security_logout");
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
	public void enter(ViewChangeEvent event) {
	}

}
