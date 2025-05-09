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

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.FieldEvents;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TreeTable;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.tools.elasticsearch.ElasticSearchApogeeService;
import fr.univlorraine.mondossierweb.uicomponents.AutoComplete;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@SpringView(name = RechercheRapideView.NAME)
public class RechercheRapideView extends VerticalLayout implements View {
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
	@Resource
	private transient ConfigController configController;

	@Resource
	private ElasticSearchApogeeService ElasticSearchService;
	private VerticalLayout mainVerticalLayout;
	private HorizontalLayout champRechercheLayout;
	private Button btnRecherche;
	private AutoComplete champRecherche;
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
		if(configController.isApplicationActive() && userController.isEnseignant()){
			/* Style */
			setMargin(true);
			setSpacing(true);

			mainVerticalLayout = new VerticalLayout();
			champRechercheLayout = new HorizontalLayout();
			// mainVerticalLayout.setImmediate(true);
			mainVerticalLayout.setSizeFull();

			//BOUTON DE RECHERCHE
			btnRecherche = new Button(applicationContext.getMessage("buttonChercher.label", null, Locale.getDefault()));
			btnRecherche.addStyleName("right-input-cmp");
			btnRecherche.setIcon(FontAwesome.SEARCH);
			btnRecherche.setEnabled(true);
			btnRecherche.addClickListener(e -> search(false));

			//Init connexion à ES, pour gain perf au premiere lettre tapées
			if(ElasticSearchService.initConnexion()){
				//CHAMP DE RECHERCHE
				champRecherche = new AutoComplete();
				champRecherche.setWidth(700, Unit.PIXELS); //540
				champRecherche.setEnabled(true);
				champRecherche.setImmediate(true);
				champRecherche.setMaxLength(100);
				champRecherche.focus();
				champRecherche.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
				champRecherche.addTextChangeListener(new FieldEvents.TextChangeListener() {
					@Override
					public void textChange(FieldEvents.TextChangeEvent event) {
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
							if(champRecherche!=null && champRecherche.getChoices()!=null &&
									champRecherche.getChoices().getItemIds()!=null){
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
							if(champRecherche!=null &&  champRecherche.getChoices()!=null && champRecherche.getChoices().getItemIds()!=null){
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

				// Maj style css du champ de recherche
				champRecherche.updateStyle();

				HorizontalLayout layoutBordure = new HorizontalLayout();
				layoutBordure.setWidth("100px");
				champRechercheLayout.addComponent(layoutBordure);
				champRechercheLayout.setComponentAlignment(layoutBordure, Alignment.MIDDLE_LEFT);

				champRechercheLayout.addComponent(champRecherche);
				champRechercheLayout.setComponentAlignment(champRecherche, Alignment.TOP_LEFT);

				//BOUTON RESET
				resetButton = new Button();
				resetButton.setIcon(FontAwesome.TIMES);
				resetButton.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				resetButton.addStyleName("btn-reset");
				resetButton.addClickListener(e->{
					champRecherche.setValue("");
					//search1.setValue("");
					resetButton.setIcon(FontAwesome.TIMES);
					champRecherche.focus();
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
			if(PropertyUtils.isEnableRechercheAutoElp() && casesAcocherElp.getValue()){
				listeTypeAutorise.add(Utils.ELP);
			}
			if(casesAcocherEtudiant.getValue()){
				listeTypeAutorise.add(Utils.ETU);
			}

			///////////////////////////////////////////////////////
			// recuperation des obj ElasticSearch
			///////////////////////////////////////////////////////
			if(lobjresult!=null && !listeTypeAutorise.isEmpty()){
				for(Map<String,Object> obj : lobjresult){
					if(listeReponses.size()<Utils.NB_MAX_RESULT_QUICK_SEARCH){
						if(obj != null){
							if(listeTypeAutorise.contains((String)obj.get(Utils.ES_TYPE))){
								if(!listeReponses.isEmpty()){
									boolean triOk=true;
									int rang = 0;
									//On evite des doublons
									while(triOk && rang<listeReponses.size()){
										//En quickSearch on prend la description et non pas le libelle
										ResultatDeRecherche r = (ResultatDeRecherche) listeReponses.get(rang);
										
										if((r.lib.toUpperCase()).equals((new ResultatDeRecherche(obj)).lib.toUpperCase())){
											triOk=false;
										}
										rang++;
									}
									if(triOk){
										//En quickSearch on prend la description et non pas le libelle
										listeReponses.add(new ResultatDeRecherche(obj));
										items.add(new ResultatDeRecherche(obj));
									}
								}else{
									//En quickSearch on prend la description et non pas le libelle
									listeReponses.add(new ResultatDeRecherche(obj));
									items.add(new ResultatDeRecherche(obj));
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
				Notification.show("Merci d'indiquer au moins 2 lettres", Notification.Type.ERROR_MESSAGE);
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

			Container.Filter filterStringToSearch =  new SimpleStringFilter("type","TypeImpossible", true, false);
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
	public void enter(ViewChangeListener.ViewChangeEvent event) {
	}


}
