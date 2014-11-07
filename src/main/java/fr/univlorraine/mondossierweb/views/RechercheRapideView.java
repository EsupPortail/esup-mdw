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

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.themes.ValoTheme;
import com.zybnet.autocomplete.server.AutocompleteField;
import com.zybnet.autocomplete.server.AutocompleteQueryListener;
import com.zybnet.autocomplete.server.AutocompleteSuggestionPickedListener;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.solr.ObjSolr;
import fr.univlorraine.mondossierweb.services.apogee.SolrServiceImpl;
import fr.univlorraine.mondossierweb.uicomponents.AutoComplete;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(RechercheRapideView.NAME)
@StyleSheet("rechercheRapideView.css")
public class RechercheRapideView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "rechercheRapideView";

	private static final int NB_MAX_RESULT_QUICK_SEARCH=5;

	public static final String[] ETU_FIELDS_ORDER = {"lib","type","code"};

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheController rechercheController;




	/** {@link SolrServiceImpl} */
	@Resource
	private SolrServiceImpl SolrService;

	private VerticalLayout mainVerticalLayout;

	private HorizontalLayout champRechercheLayout;

	private Button btnRecherche;

	private AutoComplete champRecherche;

	//private TextField searchBoxFilter;

	private HierarchicalContainer rrContainer;

	private TreeTable tableResultats;

	private String[] etuColumnHeaders;

	private CheckBox casesAcocherComposantes;

	private CheckBox casesAcocherVet;

	private CheckBox casesAcocherElp;

	private CheckBox casesAcocherEtudiant;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
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
				  System.out.println("handleSuggestionSelection "+page);
				  search(false, page);
			  }
			});*/

		//mainVerticalLayout.addComponent(search);


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
				champRecherche.showChoices(quickSearch(event.getText()), mainVerticalLayout);

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
						//System.out.println("selectionner ligne table suivante");
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
						//System.out.println("selectionner ligne table precedente");
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
		champRechercheLayout.addComponent(champRecherche);
		champRechercheLayout.setComponentAlignment(champRecherche, Alignment.MIDDLE_LEFT);

		/*champRechercheLayout.addComponent(search);
		champRechercheLayout.setComponentAlignment(search, Alignment.MIDDLE_CENTER);*/

		//BOUTON DE RECHERCHE
		btnRecherche = new Button(applicationContext.getMessage("buttonChercher.label", null, Locale.getDefault()));
		btnRecherche.setIcon(FontAwesome.SEARCH);
		btnRecherche.setEnabled(true);
		//btnRecherche.addClickListener(e -> search(false, search.getText()));
		btnRecherche.addClickListener(e -> search(false));
		champRechercheLayout.addComponent(btnRecherche);
		mainVerticalLayout.addComponent(champRechercheLayout);
		mainVerticalLayout.setComponentAlignment(champRechercheLayout, Alignment.MIDDLE_LEFT);
		champRechercheLayout.setMargin(true);




		//FILTRE DE RECHERCHE
		/*searchBoxFilter = new TextField();
		searchBoxFilter.setCaption("Filtrer les résultats");
		searchBoxFilter.addShortcutListener(new ShortcutListener("Enter Shortcut", ShortcutAction.KeyCode.ENTER, null) {
			@Override
			public void handleAction(Object sender, Object target) {
				if(target==searchBoxFilter)
					performSearch();
				if(target==champRecherche){
					search(false);
				}
			}
		});
		searchBoxFilter.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				searchBoxFilter.setValue(event.getText());
				performSearch();
			}
		});
		searchBoxFilter.setVisible(false);*/



		casesAcocherComposantes= new CheckBox("Composantes");
		casesAcocherComposantes.setValue(true);
		casesAcocherComposantes.setStyleName(ValoTheme.CHECKBOX_SMALL);
		casesAcocherComposantes.addValueChangeListener(e -> tuneSearch());
		casesAcocherVet= new CheckBox("Versions d'étapes");
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
		rrContainer.addContainerProperty("type", String.class, "");
		tableResultats = new TreeTable();
		tableResultats.setSizeFull();
		tableResultats.setSelectable(true);
		tableResultats.setMultiSelect(true);
		tableResultats.setImmediate(true);
		etuColumnHeaders = new String[ETU_FIELDS_ORDER.length];
		for (int fieldIndex = 0; fieldIndex < ETU_FIELDS_ORDER.length; fieldIndex++){
			etuColumnHeaders[fieldIndex] = applicationContext.getMessage("result.table." + ETU_FIELDS_ORDER[fieldIndex], null, Locale.getDefault());
		}
		tableResultats.addGeneratedColumn("lib", new DisplayNameColumnGenerator());
		tableResultats.setContainerDataSource(rrContainer);
		tableResultats.setVisibleColumns(ETU_FIELDS_ORDER);
		tableResultats.setColumnHeaders(etuColumnHeaders);
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
		setSizeFull();

	}



	private List<ResultatDeRecherche> quickSearch(String valueString){

		List<ResultatDeRecherche> listeReponses = new LinkedList<ResultatDeRecherche>();
		/**
		 * ESSAYER D'AFFICHER LE MENU DOSSIER ETUDIANT AU CLIC
		 * OU DEPUIS LE CONTROLEUR QU'ON VA APPELER
		 */
		String value = valueString;
		if(StringUtils.hasText(value) && value.length()>2){


			///////////////////////////////////////////////////////
			//appel solr
			///////////////////////////////////////////////////////
			//transformation de la chaine recherchée en fonction des besoins
			String valuesolr = value;
			//if(valuesolr.length()<3)

			valuesolr = valuesolr+"*";
			//valuesolr= "\""+valuesolr+"\"";//NE PAS FAIRE SI ON AJOUTE *
			//System.out.println("value valuesolr : "+valuesolr);
			//System.out.println("execution avec la requete solr...");
			long startTime = System.currentTimeMillis();
			List<Map<String,Object>> lobjresult = SolrService.findObj(valuesolr, NB_MAX_RESULT_QUICK_SEARCH * 5, true);
			long stopTime = System.currentTimeMillis();
			long durationTimeSolr = stopTime - startTime;
			//System.out.println("elapsedTime : "+durationTimeSolr+" ms");


			//Liste des types autorisés
			LinkedList<String> listeTypeAutorise=new LinkedList();
			if(casesAcocherComposantes.getValue()){
				listeTypeAutorise.add("CMP");
			}
			if(casesAcocherVet.getValue()){
				listeTypeAutorise.add("VET");
			}
			if(casesAcocherElp.getValue()){
				listeTypeAutorise.add("ELP");
			}
			if(casesAcocherEtudiant.getValue()){
				listeTypeAutorise.add("ETU");
			}


			///////////////////////////////////////////////////////
			// recuperation des obj SOLR
			///////////////////////////////////////////////////////
			if(lobjresult!=null && listeTypeAutorise.size()>0){
				for(Map<String,Object> obj : lobjresult){
					if(listeReponses.size()<NB_MAX_RESULT_QUICK_SEARCH){
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
		/**
		 * ESSAYER D'AFFICHER LE MENU DOSSIER ETUDIANT AU CLIC
		 * OU DEPUIS LE CONTROLEUR QU'ON VA APPELER
		 */

		//ChoicesPopup null si aucun choix propose
		if(champRecherche.getChoicesPopup()!=null){
			champRecherche.getChoicesPopup().setVisible(false);
			champRecherche.getChoicesPopup().setPopupVisible(false);
		}
		String value = String.valueOf(champRecherche.getValue());


		//System.out.println("Search : "+value);

		//searchBoxFilter.setValue("");
		if(StringUtils.hasText(value) && value.length()>1){


			///////////////////////////////////////////////////////
			//appel solr
			///////////////////////////////////////////////////////
			//transformation de la chaine recherchée en fonction des besoins
			String valuesolr = value;
			//	valuesolr = valuesolr+" OR "+valuesolr+"*";
			long startTime = System.currentTimeMillis();
			List<Map<String,Object>> lobjresult = SolrService.findObj(valuesolr, 0, false);
			long stopTime = System.currentTimeMillis();
			long durationTimeSolr = stopTime - startTime;

			///////////////////////////////////////////////////////
			// recuperation des obj SOLR
			///////////////////////////////////////////////////////
			if(lobjresult!=null){
				LinkedList<String> typeObjAffiches=new LinkedList<String>();
				LinkedList<ResultatDeRecherche> parentsAffiches=new LinkedList<ResultatDeRecherche>();
				String rubriqueEnCours="";


				rrContainer.removeAllItems();


				ResultatDeRecherche rdr=null;
				for(Map<String,Object> obj : lobjresult){
					if(obj != null){
						//System.out.println("=>" +obj.getId().getCodObj()+"/"+obj.getId().getCodVrsObj()+ " - "+obj.getLibObj()+" ["+obj.getId().getTypObj()+"]");
						String rubrique=(String)obj.get("TYP_OBJ");
						Item rubriqueCourante = null;
						//GESTION DES TYPES D'OBJET AFFICHES

						/*
						if(!typeObjAffiches.contains(rubrique)){

							//compter le nombre d'objet correspondant
							int nbObjRubrique=0;
							for(ObjSolr o : lobjsolr){
								if(o!=null && o.getType()!=null && o.getType().equals(rubrique))
									nbObjRubrique++;
							}
							rubriqueEnCours=rubrique+"("+nbObjRubrique+")";

							rdr = new ResultatDeRecherche(); 
							rdr.setLib(rubriqueEnCours);

							typeObjAffiches.add(rubrique);
							parentsAffiches.add(rdr);
						}else{
							for(ResultatDeRecherche r : parentsAffiches){
								if(r.getLib().substring(0, 3).equals(rubrique)){
									rdr = r;
								}
							}
						}
						 */

						ResultatDeRecherche rr=new ResultatDeRecherche(obj);
						Item i=rrContainer.addItem(rr);
						if(i!=null){
							//En search, on prend le libelle et non pas la description, à la différence du quickSearch
							i.getItemProperty("lib").setValue(rr.getLib());
							i.getItemProperty("code").setValue(rr.getCode());
							if(rr.type.equals("ELP"))
								i.getItemProperty("type").setValue(Utils.TYPE_ELP);
							if(rr.type.equals("CMP"))
								i.getItemProperty("type").setValue(Utils.TYPE_CMP);
							if(rr.type.equals("VDI"))
								i.getItemProperty("type").setValue(Utils.TYPE_VDI);
							if(rr.type.equals("VET"))
								i.getItemProperty("type").setValue(Utils.TYPE_VET);
							if(rr.type.equals("ETU"))
								i.getItemProperty("type").setValue(Utils.TYPE_ETU);


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



	class DisplayNameColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			/*Link  b=new Link();
			b.setCaption(item.getItemProperty("lib").getValue().toString());*/
			Button b = new Button(item.getItemProperty("lib").getValue().toString());
			b.setStyleName("link"); 
			b.addStyleName("v-link");

			b.addClickListener(e->rechercheController.accessToDetail(item.getItemProperty("code").getValue().toString(),item.getItemProperty("type").getValue().toString()));


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
				compFilter = new SimpleStringFilter("type",Utils.TYPE_CMP, true, false);
				filterStringToSearch = compFilter;
			}
			if(casesAcocherVet.getValue()){
				vetFilter = new SimpleStringFilter("type",Utils.TYPE_VET, true, false);
				filterStringToSearch = new Or(filterStringToSearch,vetFilter);
			}
			if(casesAcocherElp.getValue()){
				elpFilter = new SimpleStringFilter("type",Utils.TYPE_ELP, true, false);
				filterStringToSearch = new Or(filterStringToSearch,elpFilter);
			}
			if(casesAcocherEtudiant.getValue()){
				etuFilter = new SimpleStringFilter("type",Utils.TYPE_ETU, true, false);
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

}
