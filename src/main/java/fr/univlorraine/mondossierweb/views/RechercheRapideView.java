package fr.univlorraine.mondossierweb.views;

import java.util.ArrayList;
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
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.services.apogee.ElasticSearchServiceImpl;
import fr.univlorraine.mondossierweb.uicomponents.AutoComplete;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(RechercheRapideView.NAME)
public class RechercheRapideView extends VerticalLayout implements View {

	private static final long serialVersionUID = 7147611659177952737L;



	public static final String NAME = "rechercheRapideView";



	public static final String[] FIELDS_ORDER = {"lib","type"};

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient RechercheArborescenteController rechercheArborescenteController;
	@Resource
	private transient EtudiantController etudiantController;




	/** {@link ElasticSearchServiceImpl} */
	@Resource
	private ElasticSearchServiceImpl ElasticSearchService;

	private VerticalLayout mainVerticalLayout;

	private HorizontalLayout champRechercheLayout;

	private Button btnRecherche;

	private AutoComplete champRecherche;

	//private SuggestField search1= new SuggestField();

	private HierarchicalContainer rrContainer;

	private TreeTable tableResultats;

	private String[] columnHeaders;

	private CheckBox casesAcocherComposantes;

	private CheckBox casesAcocherVet;

	private CheckBox casesAcocherElp;

	private CheckBox casesAcocherEtudiant;

	private Button resetButton;

	private List<ResultatDeRecherche> items = new ArrayList<ResultatDeRecherche>();

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if( userController.isEnseignant()){


			/* Style */
			setMargin(true);
			setSpacing(true);


			mainVerticalLayout = new VerticalLayout();
			champRechercheLayout = new HorizontalLayout();
			mainVerticalLayout.setImmediate(true);
			mainVerticalLayout.setSizeFull();

			//CHAMP NEW RECHERCHE
			/*AutocompleteField<String> search = new AutocompleteField<>();
		search.addStyleName("v-textfield v-widget");
		search.setWidth(450, Unit.PIXELS);
		search.setEnabled(true);
		search.setImmediate(true);
		search.focus();
		search.setQueryListener(new AutocompleteQueryListener<String>() {
			  @Override
			  public void handleUserQuery(AutocompleteField<String> field, String query) {
			    for (String page : quickSearch(query)) {
			      field.addSuggestion(page, page);
			    }
			  }
			});

			search.setSuggestionPickedListener(new AutocompleteSuggestionPickedListener<String>() {
			  @Override
			  public void onSuggestionPicked(String page) {
				 LOG.debug("handleSuggestionSelection "+page);
				  search(false, page);
			  }
			});

		mainVerticalLayout.addComponent(search);*/


			//ADD-ON Suggestfield
			/*	search1.setInputPrompt("Tapez votre recherche");
			search1.setEnabled(true);
			search1.setWidth(700, Unit.PIXELS);
			search1.setPopupWidth(700);
			setUpAutocomplete(search1);
			search1.addShortcutListener(new ShortcutListener("Enter Shortcut", ShortcutAction.KeyCode.ENTER, null) {
				@Override
				public void handleAction(Object sender, Object target) {
					if(target==search1){

						search(false);
					}
				}
			});*/


			//BOUTON DE RECHERCHE
			btnRecherche = new Button(applicationContext.getMessage("buttonChercher.label", null, Locale.getDefault()));
			btnRecherche.setIcon(FontAwesome.SEARCH);
			btnRecherche.setEnabled(true);
			btnRecherche.addClickListener(e -> search(false));


			//Init connexion à ES, pour gain perf au premiere lettre tapées
			if(ElasticSearchService.initConnexion(true)){

				//CHAMP DE RECHERCHE
				champRecherche = new AutoComplete();
				champRecherche.setWidth(700, Unit.PIXELS); //540
				champRecherche.setEnabled(true);
				champRecherche.setImmediate(true);
				champRecherche.focus();
				champRecherche.setTextChangeEventMode(TextChangeEventMode.EAGER);
				champRecherche.addTextChangeListener(new TextChangeListener() {
					@Override
					public void textChange(TextChangeEvent event) {
						if(event.getText()!=null){
							resetButton.setIcon(FontAwesome.TIMES);
						}
						champRecherche.showChoices(quickSearch(event.getText()), mainVerticalLayout,btnRecherche, false);

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

				//champRecherche.addBlurListener(e -> champRecherche.getChoicesPopup().setVisible(false));

				HorizontalLayout layoutBordure = new HorizontalLayout();
				layoutBordure.setWidth("100px");
				champRechercheLayout.addComponent(layoutBordure);
				champRechercheLayout.setComponentAlignment(layoutBordure, Alignment.MIDDLE_LEFT);

				/*champRechercheLayout.addComponent(search1);
			champRechercheLayout.setComponentAlignment(search1, Alignment.MIDDLE_LEFT);*/

				champRechercheLayout.addComponent(champRecherche);
				champRechercheLayout.setComponentAlignment(champRecherche, Alignment.MIDDLE_LEFT);

				//BOUTON RESET
				champRecherche.addStyleName("textfield-resetable");
				resetButton = new Button();
				resetButton.setIcon(FontAwesome.TIMES);
				resetButton.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				resetButton.addStyleName("btn-reset");
				resetButton.addClickListener(e->{
					champRecherche.setValue("");
					//search1.setValue("");
					resetButton.setIcon(FontAwesome.TIMES);
				});
				champRechercheLayout.addComponent(resetButton);
				champRechercheLayout.setComponentAlignment(resetButton, Alignment.MIDDLE_LEFT);

				//Ajout du bouton de recherche au layout
				champRechercheLayout.addComponent(btnRecherche);
				mainVerticalLayout.addComponent(champRechercheLayout);
				mainVerticalLayout.setComponentAlignment(champRechercheLayout, Alignment.MIDDLE_LEFT);
				champRechercheLayout.setMargin(true);



				casesAcocherComposantes= new CheckBox("Composantes");
				casesAcocherComposantes.setValue(true);
				casesAcocherComposantes.setStyleName(ValoTheme.CHECKBOX_SMALL);
				casesAcocherComposantes.addValueChangeListener(e -> tuneSearch());
				casesAcocherVet= new CheckBox("Etapes");
				casesAcocherVet.setValue(true);
				casesAcocherVet.setStyleName(ValoTheme.CHECKBOX_SMALL);
				casesAcocherVet.addValueChangeListener(e -> tuneSearch());
				casesAcocherElp= new CheckBox("Eléments pédagogiques");
				casesAcocherElp.setValue(true);
				casesAcocherElp.setStyleName(ValoTheme.CHECKBOX_SMALL);
				casesAcocherElp.addValueChangeListener(e -> tuneSearch());
				casesAcocherEtudiant= new CheckBox("Etudiants");
				casesAcocherEtudiant.setValue(true);
				casesAcocherEtudiant.setStyleName(ValoTheme.CHECKBOX_SMALL);
				casesAcocherEtudiant.addValueChangeListener(e -> tuneSearch());

				HorizontalLayout checkBoxLayout=new HorizontalLayout();
				checkBoxLayout.setMargin(true);
				checkBoxLayout.setSpacing(true);
				checkBoxLayout.addComponent(casesAcocherComposantes);
				checkBoxLayout.addComponent(casesAcocherVet);
				checkBoxLayout.addComponent(casesAcocherElp);
				checkBoxLayout.addComponent(casesAcocherEtudiant);


				mainVerticalLayout.addComponent(checkBoxLayout);

				//TABLE DE RESULTATS
				rrContainer = new HierarchicalContainer();
				rrContainer.addContainerProperty("lib", String.class, "");
				rrContainer.addContainerProperty("code", String.class, "");
				rrContainer.addContainerProperty("info", String.class, "");
				rrContainer.addContainerProperty("type", String.class, "");
				tableResultats = new TreeTable();
				tableResultats.setSizeFull();
				tableResultats.setSelectable(false);
				tableResultats.setMultiSelect(false);
				tableResultats.setImmediate(true);
				columnHeaders = new String[FIELDS_ORDER.length];
				for (int fieldIndex = 0; fieldIndex < FIELDS_ORDER.length; fieldIndex++){
					columnHeaders[fieldIndex] = applicationContext.getMessage("result.table." + FIELDS_ORDER[fieldIndex], null, Locale.getDefault());
				}
				tableResultats.addGeneratedColumn("lib", new DisplayNameColumnGenerator());
				tableResultats.addGeneratedColumn("type", new DisplayTypeColumnGenerator());
				tableResultats.setContainerDataSource(rrContainer);
				tableResultats.setVisibleColumns(FIELDS_ORDER);
				tableResultats.setColumnHeaders(columnHeaders);
				/*mainVerticalLayout.addComponent(searchBoxFilter);
		mainVerticalLayout.setComponentAlignment(searchBoxFilter, Alignment.MIDDLE_RIGHT);*/
				VerticalLayout tableVerticalLayout = new VerticalLayout();
				tableVerticalLayout.setMargin(new MarginInfo(false, true, true, true));
				tableVerticalLayout.setSizeFull();
				tableVerticalLayout.addComponent(tableResultats);
				mainVerticalLayout.addComponent(tableVerticalLayout);
				mainVerticalLayout.setExpandRatio(tableVerticalLayout, 1);
				tableResultats.setVisible(false);


				addComponent(mainVerticalLayout);
				setSizeFull();
			}else{

				//Message fonctionnalité indisponible
				addComponent(new Label(applicationContext.getMessage(NAME + ".indisponible.message", null, getLocale()), ContentMode.HTML));
			}
		}
	}



	private List<ResultatDeRecherche> quickSearch(String valueString){

		List<ResultatDeRecherche> listeReponses = new LinkedList<ResultatDeRecherche>();
		items.clear();

		String value = valueString;
		if(StringUtils.hasText(value) && value.length()>2){


			///////////////////////////////////////////////////////
			//appel elasticSearch
			///////////////////////////////////////////////////////
			//transformation de la chaine recherchée en fonction des besoins
			String valueselasticSearch = value;

	
			//valueselasticSearch = valueselasticSearch+"*";
			List<Map<String,Object>> lobjresult = ElasticSearchService.findObj(valueselasticSearch, Utils.NB_MAX_RESULT_QUICK_SEARCH * 5, true);

		
			
			//Liste des types autorisés
			LinkedList<String> listeTypeAutorise=new LinkedList();
			if(casesAcocherComposantes.getValue()){
				listeTypeAutorise.add(Utils.CMP);
			}
			if(casesAcocherVet.getValue()){
				listeTypeAutorise.add(Utils.VET);
			}
			if(casesAcocherElp.getValue()){
				listeTypeAutorise.add(Utils.ELP);
			}
			if(casesAcocherEtudiant.getValue()){
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
										ResultatDeRecherche r = (ResultatDeRecherche) listeReponses.get(rang);
										//if((r.lib.toUpperCase()).equals(((String)obj.get("LIB_DESC_OBJ")).toUpperCase())){
										if((r.lib.toUpperCase()).equals((new ResultatDeRecherche(obj)).lib.toUpperCase())){
											triOk=false;
										}
										rang++;
									}
									if(triOk){
										//En quickSearch on prend la description et non pas le libelle
										//listeReponses.add((String)obj.get("LIB_DESC_OBJ"));
										listeReponses.add(new ResultatDeRecherche(obj));
										items.add(new ResultatDeRecherche(obj));
									}
								}else{
									//En quickSearch on prend la description et non pas le libelle
									//listeReponses.add((String)obj.get("LIB_DESC_OBJ"));
									listeReponses.add(new ResultatDeRecherche(obj));
									items.add(new ResultatDeRecherche(obj));
								}
							}
						}
					}
				}
			}



		}

		//return listeReponses;
		//return new ArrayList<Object>(listeReponses);
		return listeReponses;

	}







	private void search(boolean rechercheSansUtiliserLaVue){


		//ChoicesPopup null si aucun choix propose
		if(champRecherche.getChoicesPopup()!=null){
			champRecherche.getChoicesPopup().setVisible(false);
			champRecherche.getChoicesPopup().setPopupVisible(false);
		}
		String value = String.valueOf(champRecherche.getValue());
		/*ResultatDeRecherche r = (ResultatDeRecherche)search1.getValue();
		String value = String.valueOf(r.getLib());*/

		if(StringUtils.hasText(value) && value.length()>1){


			boolean suggestionValidee = false;

			//On détecte si la recherche porte sur une suggestion proposée par la pop_up
			if(value.contains("[") && value.contains("]")){
				suggestionValidee = true;
			}


			///////////////////////////////////////////////////////
			//appel elasticSearch
			///////////////////////////////////////////////////////
			//transformation de la chaine recherchée en fonction des besoins
			String valueElasticSearch = value;
			List<Map<String,Object>> lobjresult = ElasticSearchService.findObj(valueElasticSearch, Utils.NB_MAX_RESULT_SEARCH, false);

			///////////////////////////////////////////////////////
			// recuperation des objets
			///////////////////////////////////////////////////////
			if(lobjresult!=null){

				rrContainer.removeAllItems();
				String code=null;
				String type=null;

				for(Map<String,Object> obj : lobjresult){
					if(obj != null){

						//GESTION DES TYPES D'OBJET AFFICHES
						ResultatDeRecherche rr=new ResultatDeRecherche(obj);
						Item i=rrContainer.addItem(rr);
						if(i!=null){
							//En search, on prend le libelle et non pas la description, à la différence du quickSearch
							code=rr.getCode();
							type=rr.type;
							i.getItemProperty("lib").setValue(rr.getLib());
							if(type.equals(Utils.TYPE_ETU) || type.equals(Utils.ETU)){
								i.getItemProperty("info").setValue(etudiantController.getFormationEnCours(code));	
							}
							i.getItemProperty("code").setValue(rr.getCode());
							i.getItemProperty("type").setValue(rr.type);
							
							rrContainer.setChildrenAllowed(rr, false);
						}


					}
				}

				tableResultats.setVisible(true);
				tuneSearch();
				//la recherche porte sur une suggestion proposée par la pop_up et on a bien un seul résultat
				if(suggestionValidee && lobjresult.size()==1 && rrContainer.size()==1 && code!=null && type!=null){
					//On accède directemet au détail
					rechercheController.accessToDetail(code,type, null);

				}
			}




		}else{
			if(StringUtils.hasText(value) && value.length()<=1){
				//afficher message erreur
				Notification.show("Merci d'indiquer au moins 2 lettres",Type.ERROR_MESSAGE);
			}
		}

	}


	class DisplayTypeColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			//On converti le type pour un affichage lisible
			String typeObj = (String) item.getItemProperty("type").getValue();
			String idObj = (String)item.getItemProperty("code").getValue();

			return rechercheArborescenteController.getTypeObj(typeObj, idObj);
		}
	}

	class DisplayNameColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			Button b = new Button(item.getItemProperty("lib").getValue().toString());
			b.setStyleName("link"); 
			b.addStyleName("v-link");

			b.addClickListener(e->rechercheController.accessToDetail(item.getItemProperty("code").getValue().toString(),item.getItemProperty("type").getValue().toString(), null));

			if(item.getItemProperty("info") !=null && item.getItemProperty("info").getValue()!=null  && 
					StringUtils.hasText(item.getItemProperty("info").getValue().toString())){
				
				HorizontalLayout libhl = new HorizontalLayout();
				libhl.setSizeFull();
				libhl.addComponent(b);
				libhl.setComponentAlignment(b, Alignment.MIDDLE_LEFT);
				
				Label formation = new Label(item.getItemProperty("info").getValue().toString());
				formation.setStyleName(ValoTheme.LABEL_SMALL);
				libhl.addComponent(formation);
				libhl.setComponentAlignment(formation, Alignment.MIDDLE_RIGHT);
				
				return libhl;
				
			}
			
			return b;
		}
	}




	private void tuneSearch() {

		if(rrContainer!=null){
			rrContainer.removeAllContainerFilters();

			Filter filterStringToSearch =  new SimpleStringFilter("type","TypeImpossible", true, false);
			SimpleStringFilter compFilter;
			SimpleStringFilter vetFilter;
			SimpleStringFilter elpFilter;
			SimpleStringFilter etuFilter;

			if(casesAcocherComposantes.getValue()){
				compFilter = new SimpleStringFilter("type",Utils.CMP, true, false);
				filterStringToSearch = compFilter;
			}
			if(casesAcocherVet.getValue()){
				vetFilter = new SimpleStringFilter("type",Utils.VET, true, false);
				filterStringToSearch = new Or(filterStringToSearch,vetFilter);
			}
			if(casesAcocherElp.getValue()){
				elpFilter = new SimpleStringFilter("type",Utils.ELP, true, false);
				filterStringToSearch = new Or(filterStringToSearch,elpFilter);
			}
			if(casesAcocherEtudiant.getValue()){
				etuFilter = new SimpleStringFilter("type",Utils.ETU, true, false);
				filterStringToSearch = new Or(filterStringToSearch,etuFilter);
			}

			rrContainer.addContainerFilter(filterStringToSearch);


		}

	}





	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}



	/*
	private void setUpAutocomplete(final SuggestField search) {


		search.setSuggestionHandler(new SuggestionHandler() {
			@Override
			public List<Object> searchItems(String query) {
				System.out.println("Query: " + query);
				return new ArrayList<Object>(quickSearch(query));
			}
		});

		search.setSuggestionConverter(new ResultatDeRechercheSuggestionConverter());

		search.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				System.out.println("SuugestField value changed");
				Notification.show("Selected " + search.getValue());
			}
		});
		search.setNewItemsAllowed(true);
		search.addFocusListener(new FocusListener() {
			@Override
			public void focus(FocusEvent event) {
				System.out.println("Focus event");
			}
		});
		search.addBlurListener(new BlurListener() {
			@Override
			public void blur(BlurEvent event) {
				System.out.println("Blur event");
			}
		});
	}


	protected void handleSuggestionSelection(Integer suggestion) {
		Notification.show("Selected " + suggestion);
	}


	private class ResultatDeRechercheSuggestionConverter extends BeanSuggestionConverter {

		public ResultatDeRechercheSuggestionConverter() {
			super(ResultatDeRecherche.class, "code", "lib","type");
		}

		@Override
		public Object toItem(SuggestFieldSuggestion suggestion) {
			System.out.println("toItem : "+suggestion.getId());
			ResultatDeRecherche result = null;
			for (ResultatDeRecherche bean : items) {
				if (bean.getCode().toString().equals(suggestion.getId())) {
					result = bean;
					break;
				}
			}
			assert result != null : "This should not be happening";
			return result;
		}
	}*/
}
