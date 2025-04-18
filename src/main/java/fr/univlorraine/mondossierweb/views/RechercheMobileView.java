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
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.FieldEvents;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TreeTable;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.tools.elasticsearch.ElasticSearchApogeeService;
import fr.univlorraine.mondossierweb.uicomponents.AutoComplete;
import fr.univlorraine.mondossierweb.utils.CssUtils;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Recherche sur mobile
 */ 
@Component @Scope("session")
@SpringView(name = RechercheMobileView.NAME)
@PreAuthorize("@userController.hasRoleInProperty('teacher') || @userController.hasRoleInProperty('gestionnaire')")
public class RechercheMobileView extends VerticalLayout implements View {

	public static final String NAME = "rechercheMobileView";
	public static final String[] FIELDS_ORDER = {"type","lib"};

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
	private ElasticSearchApogeeService ElasticSearchService;

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
	private Button resetButton;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI && userController.isEnseignant()){
			// On réinitialise la vue
			removeAllComponents();

			// Style
			setSizeFull();
			addStyleName("v-noscrollableelement");

			//NAVBAR
			HorizontalLayout navbar=new HorizontalLayout();
			navbar.setSizeFull();
			navbar.setHeight(CssUtils.NAVBAR_HEIGHT);
			navbar.setStyleName("navigation-bar");

			//Bouton retour
			returnButton = new Button();
			returnButton.setIcon(FontAwesome.ARROW_LEFT);
			returnButton.setStyleName("v-menu-nav-button");
			returnButton.addClickListener(e->{
				MdwTouchkitUI.getCurrent().backFromSearch();
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

			//BOUTON DE RECHERCHE
			btnRecherche = new Button();
			btnRecherche.setIcon(FontAwesome.SEARCH);
			btnRecherche.setStyleName(ValoTheme.BUTTON_PRIMARY);
			btnRecherche.addStyleName("v-popover-button");
			btnRecherche.addStyleName("v-button-without-padding");
			btnRecherche.addStyleName("right-input-cmp");
			btnRecherche.setEnabled(true);
			btnRecherche.addClickListener(e -> search(false));

			//CHAMP DE RECHERCHE
			champRechercheLayout = new HorizontalLayout();
			champRechercheLayout.setWidth("100%");
			mainVerticalLayout = new VerticalLayout();
			// mainVerticalLayout.setImmediate(true);
			mainVerticalLayout.setSizeFull();

			//Init connexion à ES, pour gain perf au premiere lettre tapées
			if(ElasticSearchService.initConnexion()){
				//Création du champ autoComplete
				champRecherche = new AutoComplete();
				champRecherche.setWidth(100, Unit.PERCENTAGE); 
				champRecherche.setEnabled(true);
				// champRecherche.setImmediate(true);
				champRecherche.setMaxLength(100);
				champRecherche.focus();
				champRecherche.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);
				champRecherche.setImmediate(true);
				champRecherche.addTextChangeListener(new FieldEvents.TextChangeListener() {
					@Override
					public void textChange(FieldEvents.TextChangeEvent event) {
						champRecherche.showChoices(quickSearch(event.getText()), mainVerticalLayout, btnRecherche,true);
					}
				});

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

				// Maj style css du champ de recherche
				champRecherche.updateStyle();
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

				champRechercheLayout.addComponent(champRecherche);
				champRechercheLayout.setComponentAlignment(champRecherche, Alignment.MIDDLE_LEFT);

				//BOUTON RESET
				resetButton = new Button();
				resetButton.setIcon(FontAwesome.TIMES);
				resetButton.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				resetButton.addStyleName("v-popover-button");
				resetButton.addStyleName("v-button-without-padding");
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
				champRechercheLayout.setExpandRatio(champRecherche, 1);

				HorizontalLayout checkBoxVetLayout = new HorizontalLayout();
				Label etapeLabel=new Label(applicationContext.getMessage(NAME+".etapes.checkbox", null, getLocale()));
				etapeLabel.setStyleName(ValoTheme.LABEL_SMALL);
				checkBoxVetLayout.addComponent(etapeLabel);

				HorizontalLayout checkBoxElpLayout = new HorizontalLayout();
				Label elpLabel=new Label(applicationContext.getMessage(NAME+".elps.checkbox", null, getLocale()));
				elpLabel.setStyleName(ValoTheme.LABEL_SMALL);
				checkBoxElpLayout.addComponent(elpLabel);

				HorizontalLayout checkBoxEtuLayout = new HorizontalLayout();
				Label etuLabel=new Label(applicationContext.getMessage(NAME+".etudiants.checkbox", null, getLocale()));
				etuLabel.setStyleName(ValoTheme.LABEL_SMALL);
				checkBoxEtuLayout.addComponent(etuLabel);

				checkBoxVetLayout.setSizeFull();
				checkBoxElpLayout.setSizeFull();
				checkBoxEtuLayout.setSizeFull();

				if(casesAcocherVet){
					checkBoxVetLayout.setStyleName("layout-checkbox-checked");
					etapeLabel.setStyleName(ValoTheme.LABEL_SMALL);
				}else{
					checkBoxVetLayout.setStyleName("layout-checkbox-unchecked");
					etapeLabel.addStyleName("label-line-through");
				}

				if(casesAcocherElp){
					checkBoxElpLayout.setStyleName("layout-checkbox-checked");
					elpLabel.setStyleName(ValoTheme.LABEL_SMALL);
				}else{
					checkBoxElpLayout.setStyleName("layout-checkbox-unchecked");
					elpLabel.addStyleName("label-line-through");
				}

				if(casesAcocherEtudiant){
					checkBoxEtuLayout.setStyleName("layout-checkbox-checked");
					etuLabel.setStyleName(ValoTheme.LABEL_SMALL);
				}else{
					checkBoxEtuLayout.setStyleName("layout-checkbox-unchecked");
					etuLabel.addStyleName("label-line-through");
				}

				HorizontalLayout checkBoxLayout=new HorizontalLayout();
				checkBoxLayout.setWidth("100%");
				checkBoxLayout.setHeight("50px");
				checkBoxLayout.setMargin(true);
				checkBoxLayout.setSpacing(true);
				checkBoxLayout.addComponent(checkBoxVetLayout);
				checkBoxLayout.addComponent(checkBoxElpLayout);
				checkBoxLayout.addComponent(checkBoxEtuLayout);

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
				tableResultats.setColumnWidth("type", 100);

				VerticalLayout tableVerticalLayout = new VerticalLayout();
				tableVerticalLayout.setMargin(true);
				tableVerticalLayout.setSizeFull();
				tableVerticalLayout.addComponent(tableResultats);
				mainVerticalLayout.addComponent(tableVerticalLayout);
				mainVerticalLayout.setExpandRatio(tableVerticalLayout, 1);
				tableResultats.setVisible(false);

				addComponent(mainVerticalLayout);
				setExpandRatio(mainVerticalLayout, 1);
			}else{
				//Message fonctionnalité indisponible
				addComponent(new Label(applicationContext.getMessage(NAME + ".indisponible.message", null, getLocale()), ContentMode.HTML));
			}
		}
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
		//log.debug("enter listeInscritsMobileView");
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

			List<Map<String,Object>> lobjresult = ElasticSearchService.findObj(valueselasticSearch, Utils.NB_MAX_RESULT_QUICK_SEARCH * 5, true);

			//Liste des types autorisés
			LinkedList<String> listeTypeAutorise=new LinkedList();
			if(casesAcocherVet){
				listeTypeAutorise.add(Utils.VET);
			}
			if(PropertyUtils.isEnableRechercheAutoElp() && casesAcocherElp){
				listeTypeAutorise.add(Utils.ELP);
			}
			if(casesAcocherEtudiant){
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
										if((listeReponses.get(rang).lib.toUpperCase()).equals((new ResultatDeRecherche(obj)).lib.toUpperCase())){
											triOk=false;
										}
										rang++;
									}
									if(triOk){
										//En quickSearch on prend la description et non pas le libelle
										listeReponses.add(new ResultatDeRecherche(obj));
									}
								}else{
									//En quickSearch on prend la description et non pas le libelle
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
							i.getItemProperty("lib").setValue(rr.getLib());
							i.getItemProperty("code").setValue(rr.getCode());
							i.getItemProperty("type").setValue(rr.type);
							code=rr.getCode();
							type=rr.type;
							rrContainer.setChildrenAllowed(rr, false);
						}
					}
				}

				tableResultats.setVisible(true);
				tuneSearch();
				//la recherche porte sur une suggestion proposée par la pop_up et on a bien un seul résultat
				if(suggestionValidee && lobjresult.size()==1 && rrContainer.size()==1 && code!=null && type!=null){
					//On accède directemet au détail
					rechercheController.accessToMobileDetail(code,type,true);

				}
			}
		}else{
			if(StringUtils.hasText(value) && value.length()<=1){
				//afficher message erreur
				Notification.show("Merci d'indiquer au moins 2 lettres", Notification.Type.ERROR_MESSAGE);
			}
		}

	}


	private void tuneSearch() {

		if(rrContainer!=null){
			rrContainer.removeAllContainerFilters();

			Container.Filter filterStringToSearch =  new SimpleStringFilter("type","TypeImpossible", true, false);
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
			Utils.setButtonStyle(b);
			b.setHeight("100%");
			b.setWidth("100%");
			b.addClickListener(e->{
				rechercheController.accessToMobileDetail(item.getItemProperty("code").getValue().toString(),item.getItemProperty("type").getValue().toString(),true);
			});

			VerticalLayout vl = new VerticalLayout();
			vl.setSizeFull();
			vl.addComponent(b);
			return vl;
		}
	}

	

	class DisplayTypeColumnGenerator implements Table.ColumnGenerator {
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			String code = (String)item.getItemProperty("code").getValue();
			String typeObj = item.getItemProperty("type").getValue().toString();

			typeObj = rechercheArborescenteController.getTypeObj(typeObj, code);

			Label labelType = new Label(typeObj);
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
			//On convertit le type pour un affichage lisible
			return vl;
		}
	}

}
