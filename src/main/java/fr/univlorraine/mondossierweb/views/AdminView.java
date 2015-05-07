package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.views.windows.PreferencesApplicationWindow;

/**
 * Page admin
 */
@Component @Scope("prototype")
@VaadinView(AdminView.NAME)
public class AdminView extends VerticalLayout implements View {

	private static final long serialVersionUID = -2605429366219007314L;

	public static final String[] CONF_APP_FIELDS_ORDER = {"prefId", "prefDesc", "valeur"};


	public static final String NAME = "adminView";


	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient ConfigController configController;

	

	//le tabSheet global affiché
	private TabSheet tabSheetGlobal;
	private Button btnEdit;
	private Table confAppTable;
	
	private VerticalLayout layoutConfigApplication;
	
	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		//On vérifie le droit d'accéder à la vue
		if(userController.isAdmin()){
			removeAllComponents();
			/* Style */
			setMargin(true);
			setSpacing(true);

			/* Titre */
			Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
			title.addStyleName(ValoTheme.LABEL_H1);
			addComponent(title);

			/* Texte */
			//addComponent(new Label(applicationContext.getMessage(NAME + ".message", null, getLocale()), ContentMode.HTML));
			
			tabSheetGlobal = new TabSheet();
			tabSheetGlobal.setSizeFull();
			tabSheetGlobal.addStyleName(ValoTheme.TABSHEET_FRAMED);
			
			//ajout de l'onglet principal 'recherche'
			layoutConfigApplication = new VerticalLayout();
			layoutConfigApplication.setSizeFull();
			ajoutGestionParametresApplicatifs();
			tabSheetGlobal.addTab(layoutConfigApplication, "Paramètres de l'application", FontAwesome.COGS);
			
			
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

		HorizontalLayout leftButtonsLayout = new HorizontalLayout();
		leftButtonsLayout.setSpacing(true);
		buttonsLayout.addComponent(leftButtonsLayout);
		buttonsLayout.setComponentAlignment(leftButtonsLayout, Alignment.MIDDLE_LEFT);

		btnEdit = new Button(applicationContext.getMessage(NAME+".btnEdit", null, getLocale()), FontAwesome.PENCIL);
		btnEdit.setEnabled(false);
		btnEdit.addClickListener(e -> {
			if (confAppTable.getValue() instanceof PreferencesApplication) {
				//configController.editConfApp((PreferencesApplication) confAppTable.getValue());
				PreferencesApplicationWindow paw = new PreferencesApplicationWindow((PreferencesApplication) confAppTable.getValue());
				paw.addCloseListener(f->init());
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
			boolean structureIsSelected = confAppTable.getValue() instanceof PreferencesApplication;
			btnEdit.setEnabled(structureIsSelected);
			
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

	/**
	 * @see com.vaadin.navigator.View${symbol_pound}enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
