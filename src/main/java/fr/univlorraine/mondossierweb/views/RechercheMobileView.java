package fr.univlorraine.mondossierweb.views;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.services.apogee.ElasticSearchServiceImpl;
import fr.univlorraine.mondossierweb.uicomponents.AutoComplete;
import fr.univlorraine.mondossierweb.utils.Utils;


/**
 * Recherche sur mobile
 */
@Component @Scope("prototype")
@VaadinView(RechercheMobileView.NAME)
public class RechercheMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "rechercheMobileView";

	public static final String[] FIELDS_ORDER = {"type","lib"};


	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheController rechercheController;
	/** {@link ElasticSearchServiceImpl} */
	@Resource
	private ElasticSearchServiceImpl ElasticSearchService;


	private Button returnButton;

	private AutoComplete champRecherche;

	private Button btnRecherche;

	private HorizontalLayout champRechercheLayout;

	private VerticalLayout mainVerticalLayout;
	
	private HierarchicalContainer rrContainer;
	
	private String[] columnHeaders;

	private TreeTable tableResultats;

	private boolean casesAcocherVet=true;

	private boolean casesAcocherElp=true;

	private boolean casesAcocherEtudiant=true;
	
	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		// On réinitialise la vue
		removeAllComponents();

		// Style
		setSizeFull();
		addStyleName("v-noscrollableelement");




		//NAVBAR
		HorizontalLayout navbar=new HorizontalLayout();
		navbar.setSizeFull();
		navbar.setHeight("40px");
		navbar.setStyleName("navigation-bar");

		//Bouton retour
		returnButton = new Button();
		returnButton.setIcon(FontAwesome.ARROW_LEFT);
		returnButton.setStyleName("v-nav-button");
		returnButton.addClickListener(e->{
				MdwTouchkitUI.getCurrent().navigateTofavoris();
		});
		navbar.addComponent(returnButton);
		navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);

		//Title
		Label labelTrombi = new Label(applicationContext.getMessage(NAME + ".title.label", null, getLocale()));
		labelTrombi.setStyleName("v-label-navbar");
		navbar.addComponent(labelTrombi);
		navbar.setComponentAlignment(labelTrombi, Alignment.MIDDLE_CENTER);

		navbar.setExpandRatio(labelTrombi, 1);
		addComponent(navbar);




		//CHAMP DE RECHERCHE
		champRechercheLayout = new HorizontalLayout();
		champRechercheLayout.setWidth("100%");
		mainVerticalLayout = new VerticalLayout();
		mainVerticalLayout.setImmediate(true);
		mainVerticalLayout.setSizeFull();

		champRecherche = new AutoComplete();
		champRecherche.setWidth(100, Unit.PERCENTAGE); 
		champRecherche.setEnabled(true);
		champRecherche.setImmediate(true);
		champRecherche.focus();
		champRecherche.setTextChangeEventMode(TextChangeEventMode.EAGER);
		champRecherche.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				/*if(event.getText()!=null){
					resetButton.setIcon(FontAwesome.TIMES);
				}*/
				champRecherche.showChoices(quickSearch(event.getText()), mainVerticalLayout, true);

			}
		});
		champRecherche.setImmediate(true);
		champRecherche.addShortcutListener(new ShortcutListener("Enter Shortcut", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				if(target==champRecherche){
					//Si on était sur une ligne proposée sous le champ de recherche
					if(champRecherche.getSelectedItem()>0){
						//On remplie d'abord le champ avec la ligne sélectionnée
						champRecherche.setValue(champRecherche.getChoices().getItem(champRecherche.getSelectedItem()).getItemProperty("lib").getValue().toString());
					}
					search(false);
				}
			}
		});

		champRecherche.addShortcutListener(new ShortcutListener("Bottom Arrow", ShortcutAction.KeyCode.ARROW_DOWN, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				if(target==champRecherche){
					if(champRecherche.getChoices().getItemIds()!=null){
						champRecherche.getChoicesPopup().setVisible(true);
						champRecherche.getChoices().setValue(champRecherche.getNextItem());


					}
				}
			}
		});

		champRecherche.addShortcutListener(new ShortcutListener("Top Arrow", ShortcutAction.KeyCode.ARROW_UP, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				if(target==champRecherche){
					if(champRecherche.getChoices().getItemIds()!=null){
						champRecherche.getChoicesPopup().setVisible(true);
						Integer champSelectionne = champRecherche.getPreviousItem();
						if(champSelectionne>0){
							champRecherche.getChoices().setValue(champSelectionne);
						}else{
							champRecherche.getChoices().setValue(null);
						}

					}
				}
			}
		});


		/*HorizontalLayout layoutBordure = new HorizontalLayout();
		layoutBordure.setWidth("10px");
		champRechercheLayout.addComponent(layoutBordure);*/
		//champRechercheLayout.addStyleName("v-layout-with-margin");
		//champRechercheLayout.setComponentAlignment(layoutBordure, Alignment.MIDDLE_LEFT);
		champRechercheLayout.addComponent(champRecherche);
		champRechercheLayout.setComponentAlignment(champRecherche, Alignment.MIDDLE_LEFT);
		



		//BOUTON DE RECHERCHE
		btnRecherche = new Button();
		btnRecherche.setIcon(FontAwesome.SEARCH);
		btnRecherche.setStyleName(ValoTheme.BUTTON_PRIMARY);
		btnRecherche.addStyleName("v-popover-button");
		btnRecherche.addStyleName("v-button-without-padding");
		btnRecherche.setEnabled(true);
		btnRecherche.addClickListener(e -> search(false));
		champRechercheLayout.addComponent(btnRecherche);
		mainVerticalLayout.addComponent(champRechercheLayout);
		mainVerticalLayout.setComponentAlignment(champRechercheLayout, Alignment.MIDDLE_LEFT);
		champRechercheLayout.setMargin(true);
		champRechercheLayout.setExpandRatio(champRecherche, 1);

		
		HorizontalLayout checkBoxVetLayout = new HorizontalLayout();
		Label etapeLabel=new Label("Etapes");
		etapeLabel.setStyleName(ValoTheme.LABEL_SMALL);
		checkBoxVetLayout.addComponent(etapeLabel);

		HorizontalLayout checkBoxElpLayout = new HorizontalLayout();
		Label elpLabel=new Label("Elements pédagogiques");
		elpLabel.setStyleName(ValoTheme.LABEL_SMALL);
		checkBoxElpLayout.addComponent(elpLabel);
		
		HorizontalLayout checkBoxEtuLayout = new HorizontalLayout();
		Label etuLabel=new Label("Etudiants");
		etuLabel.setStyleName(ValoTheme.LABEL_SMALL);
		checkBoxEtuLayout.addComponent(etuLabel);
		
		checkBoxVetLayout.setSizeFull();
		checkBoxVetLayout.setWidth("80px");
		
		checkBoxElpLayout.setSizeFull();
		
		checkBoxEtuLayout.setSizeFull();
		checkBoxEtuLayout.setWidth("80px");
		
		
		if(casesAcocherVet){
			checkBoxVetLayout.setStyleName("layout-checkbox-checked");
		}else{
			checkBoxVetLayout.setStyleName("layout-checkbox-unchecked");
		}
		
		if(casesAcocherElp){
			checkBoxElpLayout.setStyleName("layout-checkbox-checked");
		}else{
			checkBoxElpLayout.setStyleName("layout-checkbox-unchecked");
		}
		
		if(casesAcocherEtudiant){
			checkBoxEtuLayout.setStyleName("layout-checkbox-checked");
		}else{
			checkBoxEtuLayout.setStyleName("layout-checkbox-unchecked");
		}
		
		checkBoxVetLayout.addListener(new LayoutClickListener() {
			public void layoutClick(LayoutClickEvent event) {
				if(casesAcocherVet){
					casesAcocherVet=false;
					checkBoxVetLayout.setStyleName("layout-checkbox-unchecked");
				}else{
					casesAcocherVet=true;
					checkBoxVetLayout.setStyleName("layout-checkbox-checked");
				}
				tuneSearch();
			}
		});
		
		checkBoxElpLayout.addListener(new LayoutClickListener() {
			public void layoutClick(LayoutClickEvent event) {
				if(casesAcocherElp){
					casesAcocherElp=false;
					checkBoxElpLayout.setStyleName("layout-checkbox-unchecked");
				}else{
					casesAcocherElp=true;
					checkBoxElpLayout.setStyleName("layout-checkbox-checked");
				}
				tuneSearch();
			}
		});
		
		checkBoxEtuLayout.addListener(new LayoutClickListener() {
			public void layoutClick(LayoutClickEvent event) {
				if(casesAcocherEtudiant){
					casesAcocherEtudiant=false;
					checkBoxEtuLayout.setStyleName("layout-checkbox-unchecked");
				}else{
					casesAcocherEtudiant=true;
					checkBoxEtuLayout.setStyleName("layout-checkbox-checked");
				}
				tuneSearch();
			}
		});

		

		HorizontalLayout checkBoxLayout=new HorizontalLayout();
		checkBoxLayout.setWidth("100%");
		checkBoxLayout.setHeight("50px");
		checkBoxLayout.setMargin(true);
		checkBoxLayout.setSpacing(true);
		checkBoxLayout.addComponent(checkBoxVetLayout);
		checkBoxLayout.addComponent(checkBoxElpLayout);
		checkBoxLayout.addComponent(checkBoxEtuLayout);
		checkBoxLayout.setExpandRatio(checkBoxElpLayout, 1);


		mainVerticalLayout.addComponent(checkBoxLayout);

		//TABLE DE RESULTATS
		rrContainer = new HierarchicalContainer();
		rrContainer.addContainerProperty("lib", String.class, "");
		rrContainer.addContainerProperty("code", String.class, "");
		rrContainer.addContainerProperty("type", String.class, "");
		tableResultats = new TreeTable();
		tableResultats.setWidth("100%");
		tableResultats.setSelectable(false);
		tableResultats.setMultiSelect(false);
		tableResultats.setImmediate(true);
		columnHeaders = new String[FIELDS_ORDER.length];
		for (int fieldIndex = 0; fieldIndex < FIELDS_ORDER.length; fieldIndex++){
			columnHeaders[fieldIndex] = applicationContext.getMessage("result.table." + FIELDS_ORDER[fieldIndex], null, Locale.getDefault());
		}
		
		tableResultats.addGeneratedColumn("type", new DisplayTypeColumnGenerator());
		tableResultats.addGeneratedColumn("lib", new DisplayNameColumnGenerator());
		tableResultats.setContainerDataSource(rrContainer);
		tableResultats.setVisibleColumns(FIELDS_ORDER);
		tableResultats.setStyleName("nohscrollabletable");
		tableResultats.setColumnHeaders(columnHeaders);
		tableResultats.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
		tableResultats.setColumnWidth("type", 90);
		
		/*mainVerticalLayout.addComponent(searchBoxFilter);
		mainVerticalLayout.setComponentAlignment(searchBoxFilter, Alignment.MIDDLE_RIGHT);*/
		VerticalLayout tableVerticalLayout = new VerticalLayout();
		tableVerticalLayout.setMargin(true);
		tableVerticalLayout.setSizeFull();
		tableVerticalLayout.addComponent(tableResultats);
		mainVerticalLayout.addComponent(tableVerticalLayout);
		mainVerticalLayout.setExpandRatio(tableVerticalLayout, 1);
		tableResultats.setVisible(false);


		addComponent(mainVerticalLayout);
		setExpandRatio(mainVerticalLayout, 1);


	}




	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//LOG.debug("enter listeInscritsMobileView");
	}


	private List<ResultatDeRecherche> quickSearch(String valueString){

		List<ResultatDeRecherche> listeReponses = new LinkedList<ResultatDeRecherche>();

		String value = valueString;
		if(StringUtils.hasText(value) && value.length()>2){


			///////////////////////////////////////////////////////
			//appel elasticSearch
			///////////////////////////////////////////////////////
			//transformation de la chaine recherchée en fonction des besoins
			String valueselasticSearch = value;
			
			valueselasticSearch = valueselasticSearch+"*";
			List<Map<String,Object>> lobjresult = ElasticSearchService.findObj(valueselasticSearch, Utils.NB_MAX_RESULT_QUICK_SEARCH * 5, true);

			//Liste des types autorisés
			LinkedList<String> listeTypeAutorise=new LinkedList();
			if(casesAcocherVet){
				listeTypeAutorise.add(Utils.VET);
			}
			if(casesAcocherElp){
				listeTypeAutorise.add(Utils.ELP);
			}
			if(casesAcocherEtudiant){
				listeTypeAutorise.add(Utils.ETU);
			}


			///////////////////////////////////////////////////////
			// recuperation des obj ElasticSearch
			///////////////////////////////////////////////////////
			if(lobjresult!=null && listeTypeAutorise.size()>0){
				for(Map<String,Object> obj : lobjresult){
					if(listeReponses.size()<Utils.NB_MAX_RESULT_QUICK_SEARCH){
						if(obj != null){
							if(listeTypeAutorise.contains((String)obj.get("TYP_OBJ"))){
								if(listeReponses.size()>0){
									boolean triOk=true;
									int rang = 0;
									//On evite des doublons
									while(triOk && rang<listeReponses.size()){
										//En quickSearch on prend la description et non pas le libelle
										if((listeReponses.get(rang).lib.toUpperCase()).equals(((String)obj.get("LIB_DESC_OBJ")).toUpperCase())){
											triOk=false;
										}
										rang++;
									}
									if(triOk){
										//En quickSearch on prend la description et non pas le libelle
										//listeReponses.add((String)obj.get("LIB_DESC_OBJ"));
										listeReponses.add(new ResultatDeRecherche(obj));
									}
								}else{
									//En quickSearch on prend la description et non pas le libelle
									//listeReponses.add((String)obj.get("LIB_DESC_OBJ"));
									listeReponses.add(new ResultatDeRecherche(obj));
								}
							}
						}
					}
				}
			}



		}

		return listeReponses;

	}







	private void search(boolean rechercheSansUtiliserLaVue){


		//ChoicesPopup null si aucun choix propose
		if(champRecherche.getChoicesPopup()!=null){
			champRecherche.getChoicesPopup().setVisible(false);
			champRecherche.getChoicesPopup().setPopupVisible(false);
		}
		String value = String.valueOf(champRecherche.getValue());

		if(StringUtils.hasText(value) && value.length()>1){


			///////////////////////////////////////////////////////
			//appel elasticSearch
			///////////////////////////////////////////////////////
			//transformation de la chaine recherchée en fonction des besoins
			String valueElasticSearch = value;
			List<Map<String,Object>> lobjresult = ElasticSearchService.findObj(valueElasticSearch, 0, false);

			///////////////////////////////////////////////////////
			// recuperation des obj SOLR
			///////////////////////////////////////////////////////
			if(lobjresult!=null){

				rrContainer.removeAllItems();

				for(Map<String,Object> obj : lobjresult){
					if(obj != null){
						
						//GESTION DES TYPES D'OBJET AFFICHES
						ResultatDeRecherche rr=new ResultatDeRecherche(obj);
						Item i=rrContainer.addItem(rr);
						if(i!=null){
							//En search, on prend le libelle et non pas la description, à la différence du quickSearch
							i.getItemProperty("lib").setValue(rr.getLib());
							i.getItemProperty("code").setValue(rr.getCode());
							i.getItemProperty("type").setValue(rr.type);
							rrContainer.setChildrenAllowed(rr, false);
						}


					}
				}

				tableResultats.setVisible(true);
				tuneSearch();
			}




		}else{
			if(StringUtils.hasText(value) && value.length()<=1){
				//afficher message erreur
				Notification.show("Merci d'indiquer au moins 2 lettres",Type.ERROR_MESSAGE);
			}
		}

	}


	private void tuneSearch() {

		if(rrContainer!=null){
			rrContainer.removeAllContainerFilters();

			Filter filterStringToSearch =  new SimpleStringFilter("type","TypeImpossible", true, false);
			SimpleStringFilter vetFilter;
			SimpleStringFilter elpFilter;
			SimpleStringFilter etuFilter;


			if(casesAcocherVet){
				vetFilter = new SimpleStringFilter("type",Utils.VET, true, false);
				filterStringToSearch = new Or(filterStringToSearch,vetFilter);
			}
			if(casesAcocherElp){
				elpFilter = new SimpleStringFilter("type",Utils.ELP, true, false);
				filterStringToSearch = new Or(filterStringToSearch,elpFilter);
			}
			if(casesAcocherEtudiant){
				etuFilter = new SimpleStringFilter("type",Utils.ETU, true, false);
				filterStringToSearch = new Or(filterStringToSearch,etuFilter);
			}

			rrContainer.addContainerFilter(filterStringToSearch);


		}

	}
	

	class DisplayNameColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			String lib = item.getItemProperty("lib").getValue().toString();
			if(lib.startsWith("[")){
				String tab[]=lib.split("]");
				lib=tab[1].trim();
			}
			Button b = new Button(lib);
			b.setStyleName("v-button-multiline");
			b.addStyleName("link"); 
			b.addStyleName("v-link");
			b.setHeight("100%");
			b.setWidth("100%");
			b.addClickListener(e->{
				rechercheController.accessToMobileDetail(item.getItemProperty("code").getValue().toString(),item.getItemProperty("type").getValue().toString());
			});

			return b;
		}
	}


	class DisplayTypeColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			
			String code = item.getItemProperty("lib").getValue().toString();
			if(code.startsWith("[")){
				String tab[]=code.split("]");
				code=tab[0].replaceFirst("\\[", "").trim();
			}
			
			Label labelType = new Label(Utils.convertTypeToDisplay(item.getItemProperty("type").getValue().toString()));
			labelType.setWidth("100%");
			labelType.setStyleName(ValoTheme.LABEL_SMALL);
			labelType.addStyleName("label-centre-bold");
			
			Label labelCode = new Label(code);
			labelCode.setWidth("100%");
			labelCode.setStyleName(ValoTheme.LABEL_SMALL);
			labelCode.addStyleName("label-centre");
			
			VerticalLayout vl =new VerticalLayout();
			vl.addComponent(labelType);
			vl.addComponent(labelCode);
			//On converti le type pour un affichage lisible
			return vl;
		}
	}

}
