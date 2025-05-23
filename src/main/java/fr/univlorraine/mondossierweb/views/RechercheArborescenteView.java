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
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Tree;
import com.vaadin.v7.ui.TreeTable;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.VersionDiplome;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.entities.vaadin.ObjetBase;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteService;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteServiceImpl;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.utils.miscellaneous.ReferencedButton;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Recherche arborescente
 */
@Component @Scope("prototype")
@SpringView(name = RechercheArborescenteView.NAME)
public class RechercheArborescenteView extends VerticalLayout implements View {
	public static final String NAME = "rechercheArborescenteView";
	private static final String ID_PROPERTY = "id";
	private static final String TRUE_ID_PROPERTY = "trueObjectId";
	private static final String LIBELLE_PROPERTY = "libelle";
	private static final String DEPLIE_PROPERTY = "deplie";
	private static final String TYPE_PROPERTY ="type";
	/* les champs de la table */
	public static final String[] DETAIL_FIELDS_ORDER = {"libelle", "trueObjectId","type"};
	/* les champs de la table */
	public static final String[] DETAIL_FIELDS_ORDER_ON_REFRESH = {"libelle", "trueObjectId","type","actions"};

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheArborescenteController rechercheArborescenteController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient FavorisController favorisController;
	/** {@link ComposanteServiceImpl} */
	@Resource
	private ComposanteService composanteService;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient ListeInscritsController listeInscritsController;
	@Resource
	private transient ConfigController configController;

	private RechercheArboThread rat;
	private HierarchicalContainer hc;
	private TreeTable table;
	private List<String> markedRows;
	private List<String> liste_types_favoris;
	private List<String> liste_types_inscrits;
	private List<String> liste_types_deplier;
	private List<ReferencedButton> listeBoutonFavoris;
	private Label ligneSelectionneeLabel;
	private Label vetElpSelectionneLabel;
	private Label labelLigneSelectionneeLabel;
	private FormLayout elpLayout;
	private String annee;
	private ComboBox comboBoxAnneeUniv;
	private String code;
	private String type;
	private boolean initEffectue;
	private Button reinitButton;

	/**
	 * liste des composantes actives
	 */
	private List<Composante> lcomp;


	public void refresh() {
		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && userController.isEnseignant()){
			//Actualiser de l'affiche du bouton de mise en favori
			if(table!=null && hc!=null){
				recuperationDesfavoris();
				if(listeBoutonFavoris!=null){
					for(ReferencedButton btnfav : listeBoutonFavoris){
						if(markedRows.contains(btnfav.getIdObj())){	
							btnfav.getButton().setIcon(FontAwesome.TRASH_O);
							btnfav.getButton().setStyleName(ValoTheme.BUTTON_DANGER);
							btnfav.getButton().addStyleName("deletefavbutton");
							btnfav.getButton().setDescription(applicationContext.getMessage(NAME+".supprimerfavori", null, getLocale()));
						}else{
							btnfav.getButton().setIcon(FontAwesome.STAR_O);
							btnfav.getButton().setStyleName(ValoTheme.BUTTON_PRIMARY);
							btnfav.getButton().setDescription(applicationContext.getMessage(NAME+".ajouterfavori", null, getLocale()));
						}
						if(btnfav.isSingleAction()) {
							btnfav.getButton().addStyleName("single-action-button");
						} else {
							btnfav.getButton().addStyleName("left-action-button");
						}
					}
				}
			}
		}
	}

	private void recuperationDesfavoris() {
		//Recuperation des favoris
		List<Favoris> lfav = favorisController.getFavorisFromLogin(userController.getCurrentUserName());
		markedRows = new LinkedList<String>();
		for(Favoris fav : lfav){
			String idFav = fav.getId().getTypfav()+":"+fav.getId().getIdfav();
			markedRows.add(idFav);
		}
	}


	/**
	 * reinitialise la vue pour pointer sur les données en paramètres
	 */
	public void initFromScratch(){
		removeAllComponents();
		initEffectue=false;
		annee=null;
		code = null;
		type = null;
		init();
	}

	/**
	 * reinitialise la vue pour pointer sur les données en paramètres
	 * @param parameterMap
	 */
	public void initFromParameters(Map<String, String> parameterMap){
		removeAllComponents();
		initEffectue=false;
		annee=null;
		code = parameterMap.get("code");
		type = parameterMap.get("type");
		init();
	}
	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		//On vérifie le droit d'accéder à la vue
		if(configController.isApplicationActive() && userController.isEnseignant() ){
			/* Style */
			setMargin(false);
			setSpacing(false);
			setSizeFull();

			if(listeBoutonFavoris!=null){
				listeBoutonFavoris.clear();
			}else{
				listeBoutonFavoris = new LinkedList<ReferencedButton>();
			}

			liste_types_favoris = new LinkedList<String>();
			liste_types_favoris.add(Utils.CMP);
			liste_types_favoris.add(Utils.ELP);
			liste_types_favoris.add(Utils.VET);

			liste_types_inscrits= new LinkedList<String>();
			liste_types_inscrits.add(Utils.ELP);
			liste_types_inscrits.add(Utils.VET);

			liste_types_deplier= new LinkedList<String>();
			liste_types_deplier.add(Utils.ELP);
			liste_types_deplier.add(Utils.VET);

			recuperationDesfavoris();

			HorizontalLayout btnLayout = new HorizontalLayout();
			btnLayout.setMargin(false);
			btnLayout.setSpacing(false);
			btnLayout.setWidth("100%");

			comboBoxAnneeUniv = new ComboBox(applicationContext.getMessage(NAME+".anneeuniv", null, getLocale()));
			comboBoxAnneeUniv.setPageLength(5);
			comboBoxAnneeUniv.setTextInputAllowed(false);
			comboBoxAnneeUniv.setNullSelectionAllowed(false);
			//Initialisation de la liste des années
			List<String> lanneeUniv = rechercheArborescenteController.recupererLesDernieresAnneeUniversitaire();
			if(lanneeUniv!=null && !lanneeUniv.isEmpty()){
				for(String anneeUniv : lanneeUniv){
					comboBoxAnneeUniv.addItem(anneeUniv);
					int anneenplusun = Integer.parseInt(anneeUniv) + 1;
					comboBoxAnneeUniv.setItemCaption(anneeUniv,anneeUniv+"/"+anneenplusun);
				}
				if(annee==null){
					annee=etudiantController.getAnneeUnivEnCours(MainUI.getCurrent());
					//annee = lanneeUniv.get(0);
				}
			}
			comboBoxAnneeUniv.setValue(annee);
			comboBoxAnneeUniv.setStyleName(ValoTheme.COMBOBOX_SMALL);
			comboBoxAnneeUniv.addValueChangeListener(e -> changerAnnee((String)comboBoxAnneeUniv.getValue()));

			reinitButton = new Button();
			reinitButton.setDescription(applicationContext.getMessage(NAME+".reinitbutton.description", null, getLocale()));
			reinitButton.addClickListener(e->{
				initFromScratch();
			});
			reinitButton.setStyleName(ValoTheme.BUTTON_DANGER);
			reinitButton.addStyleName("single-action-button");
			reinitButton.setIcon(FontAwesome.TIMES);
			if(!StringUtils.hasText(code)){
				reinitButton.setVisible(false);
			}
			labelLigneSelectionneeLabel=new Label();
			labelLigneSelectionneeLabel.setValue(applicationContext.getMessage(NAME+".ligneselectionnee", null, getLocale()));
			labelLigneSelectionneeLabel.addStyleName("label-align-right");
			labelLigneSelectionneeLabel.addStyleName("font-small");
			labelLigneSelectionneeLabel.setVisible(false);


			HorizontalLayout btnLeftLayout= new HorizontalLayout();
			btnLeftLayout.setWidth("100%");
			btnLeftLayout.setMargin(true);
			btnLeftLayout.addComponent(comboBoxAnneeUniv);
			btnLeftLayout.setComponentAlignment(comboBoxAnneeUniv, Alignment.MIDDLE_LEFT);
			/*btnLeftLayout.addComponent(reinitButton);
			btnLeftLayout.setComponentAlignment(reinitButton, Alignment.BOTTOM_RIGHT);*/
			btnLeftLayout.addComponent(labelLigneSelectionneeLabel);
			btnLeftLayout.setComponentAlignment(labelLigneSelectionneeLabel, Alignment.MIDDLE_CENTER);
			btnLayout.addComponent(btnLeftLayout);


			ligneSelectionneeLabel =new Label();
			//ligneSelectionneeLabel.setCaption(applicationContext.getMessage(NAME+".ligneselectionnee", null, getLocale()));
			ligneSelectionneeLabel.setVisible(false);
			elpLayout = new FormLayout();
			elpLayout.setMargin(false);
			vetElpSelectionneLabel = new Label();
			vetElpSelectionneLabel.setVisible(false);
			elpLayout.addComponent(vetElpSelectionneLabel);
			elpLayout.setVisible(false);
			VerticalLayout ligneLayout = new VerticalLayout();
			ligneLayout.addStyleName("font-small");
			ligneLayout.addComponent(ligneSelectionneeLabel);
			ligneLayout.addComponent(elpLayout);

			HorizontalLayout rightLayout = new HorizontalLayout();
			rightLayout.setSizeFull();
			rightLayout.setSpacing(true);
			rightLayout.setMargin(true);
			rightLayout.addComponent(ligneLayout);
			rightLayout.setComponentAlignment(ligneLayout, Alignment.MIDDLE_LEFT);
			rightLayout.addComponent(reinitButton);
			rightLayout.setComponentAlignment(reinitButton, Alignment.MIDDLE_RIGHT);
			rightLayout.setExpandRatio(ligneLayout, 1);
			btnLayout.addComponent(rightLayout);
			btnLayout.setComponentAlignment(rightLayout, Alignment.MIDDLE_LEFT);

			addComponent(btnLayout);

			if(code!=null && type!=null){
				Label elementRecherche = new Label(code +" "+type);
				elementRecherche.addStyleName(ValoTheme.LABEL_H1);
				//addComponent(elementRecherche);

			}

			table = new TreeTable();
			table.setSizeFull();
			table.setStyleName("scrollabletable");
			table.setSelectable(true);

			initComposantes();

			//gestion du style pour les lignes en favori
			table.setCellStyleGenerator(new Table.CellStyleGenerator() {
				@Override
				public String getStyle(final Table source, final Object itemId,final Object propertyId) {
					String style = null;
					if (propertyId == null && markedRows.contains(itemId)) {
						style = "marked";
					}
					return style;
				}
			});

			table.addItemClickListener(new ItemClickEvent.ItemClickListener() {

				@Override
				public void itemClick(ItemClickEvent event) {
					selectionnerLigne(event.getItemId());
				}

			});

			//gestion du clic sur la fleche pour déplier une entrée
			table.addExpandListener(new Tree.ExpandListener() {
				@Override
				public void nodeExpand(Tree.ExpandEvent event) {
					if(event!=null && event.getItemId()!=null && hc!=null && hc.getItem(event.getItemId())!=null
							&& hc.getItem(event.getItemId()).getItemProperty(TYPE_PROPERTY)!=null){
						selectionnerLigne(event.getItemId());
						deplierNoeud((String)event.getItemId(), true);
					}
				}
			});

			VerticalLayout tableVerticalLayout = new VerticalLayout();
			tableVerticalLayout.setMargin(new MarginInfo(false, true, true, true));
			tableVerticalLayout.setSizeFull();
			tableVerticalLayout.addComponent(table);
			tableVerticalLayout.setExpandRatio(table, 1);
			addComponent(tableVerticalLayout);
			setExpandRatio(tableVerticalLayout, 1);
		}
	}

	private void initComposantes() {
		if(hc!=null && hc.size()>0){
			hc.removeAllItems();
		}
		hc = new HierarchicalContainer();
		hc.addContainerProperty(ID_PROPERTY, String.class, "");
		hc.addContainerProperty(TRUE_ID_PROPERTY, String.class, "");
		hc.addContainerProperty(LIBELLE_PROPERTY, String.class, "");
		hc.addContainerProperty(TYPE_PROPERTY, String.class, "");
		hc.addContainerProperty(DEPLIE_PROPERTY, String.class, "");

		List<ObjetBase> lobj = new LinkedList<ObjetBase>();
		String rootItemId = null;
		//Si aucun code ni type spécifié on récupère toutes les composantes
		if(!StringUtils.hasText(code) || !StringUtils.hasText(type)){
			if(lcomp==null || lcomp.isEmpty()){
				lcomp = composanteService.findComposantesEnService();
			}

			for(Composante comp : lcomp){
				ObjetBase obj = new ObjetBase();
				rechercheArborescenteController.renseigneObjFromCmp(obj, comp);
				lobj.add(obj);
				Item i = hc.addItem(obj.getId());
				renseignerItem(i,obj);
			}

		}else{
			//On ajoute l'objet racine et on déplira ci-dessous le premier niveau
			ObjetBase obj = rechercheArborescenteController.getObj(code,type);
			Item i = hc.addItem(obj.getId());
			renseignerItem(i,obj);
			rootItemId = (String)i.getItemProperty(ID_PROPERTY).getValue();
		}

		table.setContainerDataSource(hc);
		if(!initEffectue){
			table.addContainerProperty(TRUE_ID_PROPERTY, String.class, "");
			table.addContainerProperty(LIBELLE_PROPERTY, String.class, "");
			table.setVisibleColumns(DETAIL_FIELDS_ORDER);
			table.setColumnHeader(LIBELLE_PROPERTY, applicationContext.getMessage(NAME+".table.libelle", null, getLocale()));
			table.setColumnHeader(TRUE_ID_PROPERTY, applicationContext.getMessage(NAME+".table.trueObjectId", null, getLocale()));
			table.addGeneratedColumn("type", new DisplayTypeColumnGenerator());
			table.setColumnHeader("type", applicationContext.getMessage(NAME+".table.type", null, getLocale()));
			table.addGeneratedColumn("actions", new ActionsColumnGenerator(this));
			table.setColumnHeader("actions", applicationContext.getMessage(NAME+".table.actions", null, getLocale()));
			initEffectue=true;
		}else{
			table.addContainerProperty(TRUE_ID_PROPERTY, String.class, "");
			table.addContainerProperty(LIBELLE_PROPERTY, String.class, "");
			table.setVisibleColumns(DETAIL_FIELDS_ORDER_ON_REFRESH);
		}
		//On déplie l'élément racine (quand la racine de la table n'est pas la totalité des composantes)
		if(StringUtils.hasText(rootItemId)){
			selectionnerLigne(rootItemId);
			deplierNoeud(rootItemId, true);
			table.setCollapsed(rootItemId, false);
		}

	}
	private void renseignerItem(Item i, ObjetBase obj) {
		i.getItemProperty(LIBELLE_PROPERTY).setValue(obj.getLibelle());
		i.getItemProperty(ID_PROPERTY).setValue(obj.getId());
		i.getItemProperty(TRUE_ID_PROPERTY).setValue(obj.getTrueObjectId());
		i.getItemProperty(TYPE_PROPERTY).setValue(obj.getType());
		i.getItemProperty(DEPLIE_PROPERTY).setValue(obj.getDeplie());

	}

	private void changerAnnee(String value) {
		annee=value;
		initComposantes();
	}
	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
		//log.debug("enter");
	}

	class DisplayTypeColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			String typeObj = (String)item.getItemProperty(TYPE_PROPERTY).getValue();
			String idObj = (String)item.getItemProperty(TRUE_ID_PROPERTY).getValue();

			return rechercheArborescenteController.getTypeObj(typeObj,idObj);
		}
	}

	/** Formats the position in a column containing Date objects. */
	class ActionsColumnGenerator implements Table.ColumnGenerator {

		RechercheArborescenteView rechercheArborescenteView;

		public ActionsColumnGenerator(RechercheArborescenteView rechercheArborescenteView) {
			super();
			this.rechercheArborescenteView = rechercheArborescenteView;
		}

		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			String typeObj = (String)item.getItemProperty(TYPE_PROPERTY).getValue();
			String idObj = (String)item.getItemProperty(TRUE_ID_PROPERTY).getValue();

			String idFav = typeObj+":"+idObj;

			HorizontalLayout boutonActionLayout = new HorizontalLayout();

			Button btnFav = new Button();
			ReferencedButton rb = new ReferencedButton();
			//Si c'est un objet qui peut être mis en favori
			if(typeObj != null && liste_types_favoris != null && liste_types_favoris.contains(typeObj)){
				if(markedRows.contains(idFav)){	
					btnFav.setIcon(FontAwesome.TRASH_O);
					btnFav.setStyleName(ValoTheme.BUTTON_DANGER);
					btnFav.addStyleName("deletefavbutton");
					btnFav.setDescription(applicationContext.getMessage(NAME+".supprimerfavori", null, getLocale()));
				}else{
					btnFav.setIcon(FontAwesome.STAR_O);
					btnFav.setStyleName(ValoTheme.BUTTON_PRIMARY);
					btnFav.setDescription(applicationContext.getMessage(NAME+".ajouterfavori", null, getLocale()));
				}
				btnFav.addStyleName("single-action-button");
				//Gestion du clic sur le bouton favori
				btnFav.addClickListener(new Button.ClickListener() {
					@Override
					public void buttonClick(Button.ClickEvent event) {
						//if(markedRows.contains((String)itemId)){
						if(markedRows.contains(idFav)){
							//creation du favori
							FavorisPK fpk = new FavorisPK();
							fpk.setLogin(userController.getCurrentUserName());
							fpk.setTypfav(typeObj);
							fpk.setIdfav(idObj);
							//suppression du favori en base
							favorisController.removeFavori(fpk);
							//mise à jour de la liste des favoris de la vue
							//markedRows.remove((String)itemId);
							markedRows.remove(idFav);
							btnFav.setIcon(FontAwesome.STAR_O);
							btnFav.setStyleName(ValoTheme.BUTTON_PRIMARY);
							event.getButton().setIcon(FontAwesome.STAR_O);
						}else{
							//creation du favori
							FavorisPK fpk = new FavorisPK();
							fpk.setLogin(userController.getCurrentUserName());
							fpk.setTypfav(typeObj);
							fpk.setIdfav(idObj);
							Favoris favori = new Favoris();
							favori.setId(fpk);
							//aout du favori en base
							favorisController.saveFavori(favori);
							//mise à jour de la liste des favoris de la vue
							//markedRows.add((String)itemId);
							markedRows.add(idFav);
							btnFav.setIcon(FontAwesome.TRASH_O);
							btnFav.setStyleName(ValoTheme.BUTTON_DANGER);
							btnFav.addStyleName("deletefavbutton");
							event.getButton().setIcon(FontAwesome.TRASH_O);
						}
						table.markAsDirtyRecursive();
						//btonFavori.markAsDirty();
					}
				});
				rb.setButton(btnFav);
				rb.setIdObj(idFav);
				listeBoutonFavoris.add(rb);
				boutonActionLayout.addComponent(btnFav);
			}
			Button btnDeplier = null;
			if(typeObj!=null && liste_types_deplier!=null && liste_types_deplier.contains(typeObj)){
				btnDeplier=new Button();
				btnDeplier.addStyleName("middle-action-button");
				btnDeplier.setIcon(FontAwesome.SITEMAP);
				btnDeplier.setDescription(applicationContext.getMessage(NAME+".deplierarbo", null, getLocale()));
				btnDeplier.addClickListener(e->{

					if(PropertyUtils.isPushEnabled() &&  PropertyUtils.isShowLoadingIndicator()){
						//affichage de la pop-up de loading
						MainUI.getCurrent().startBusyIndicator();
						MainUI.getCurrent().push();

						//Execution de la méthode en parallèle dans un thread
						rat = new RechercheArboThread(MainUI.getCurrent(), rechercheArborescenteView, (String) itemId);
						rat.start();
					}else{
						deplierNoeudComplet((String)itemId);
						selectionnerLigne((String) itemId);
						table.setCurrentPageFirstItemId((String)itemId);
					}
				});
				boutonActionLayout.addComponent(btnDeplier);
			}

			Button btnListeInscrits = null;
			if(typeObj!=null && liste_types_inscrits!=null && liste_types_inscrits.contains(typeObj)){
				btnListeInscrits=new Button();
				btnListeInscrits.addStyleName("right-action-button");
				btnListeInscrits.setIcon(FontAwesome.USERS);
				btnListeInscrits.addStyleName(ValoTheme.BUTTON_FRIENDLY);
				btnListeInscrits.setDescription(applicationContext.getMessage(NAME+".acceslisteinscrits", null, getLocale()));
				btnListeInscrits.addClickListener(e->{
					rechercheController.accessToDetail(idObj,typeObj,(String)comboBoxAnneeUniv.getValue());
				});
				boutonActionLayout.addComponent(btnListeInscrits);
			}
			if (btnDeplier == null && btnListeInscrits == null) {
				rb.setSingleAction(true);
				btnFav.addStyleName("single-action-button");
			} else {
				rb.setSingleAction(false);
				btnFav.addStyleName("left-action-button");
			}

			return boutonActionLayout;
		}
	}


	private void selectionnerLigne(Object itemId) {

		table.setValue(itemId);
		String typeItemSelected=(String) hc.getItem(itemId).getItemProperty(TYPE_PROPERTY).getValue();
		ligneSelectionneeLabel.setValue((String) hc.getItem(itemId).getItemProperty(LIBELLE_PROPERTY).getValue()+ " ("+typeItemSelected+")");
		//Si c'est un ELP qui est selectionne
		if(typeItemSelected.equals(Utils.ELP) || typeItemSelected.equals(Utils.COL) || typeItemSelected.equals(Utils.GRP)){
			//On va chercher le premier element pere non ELP pour l'afficher en rappel
			String idItemSelected=(String) hc.getItem(itemId).getItemProperty(ID_PROPERTY).getValue();
			//On recupere l'id complet de l'ELP et on le split pour en parcourir l'arborescence
			String[] elps = idItemSelected.split("_");
			boolean vetTrouvee = false;
			String idsElp = "";
			String idVet = "";
			for(int i=0;i<elps.length;i++){
				if(!vetTrouvee){
					//Si le type est different de ELP , GRP et COL
					String typeItem = elps[i].substring(0,3);
					if(!typeItem.equals(Utils.ELP) && !typeItem.equals(Utils.COL) && !typeItem.equals(Utils.GRP)){
						vetTrouvee=true;
						idVet = idItemSelected.replaceAll(idsElp+"_", "");
					}else{
						//tant qu'on n'a pas trouve la VET on reconstitue la partie gauche de l'id
						if(StringUtils.hasText(idsElp)){
							idsElp = idsElp+"_";
						}
						idsElp = idsElp+elps[i];
					}
				}
			}
			if(vetTrouvee && StringUtils.hasText(idVet) && hc.getItem(idVet)!=null){
				//afficher le lib de la VET pour rappel
				String typeVet=(String) hc.getItem(idVet).getItemProperty(TYPE_PROPERTY).getValue();
				//On passe l'ELP dans le label du bas
				vetElpSelectionneLabel.setCaption(ligneSelectionneeLabel.getValue());
				vetElpSelectionneLabel.setIcon(FontAwesome.ARROW_RIGHT);
				vetElpSelectionneLabel.addStyleName("font-small");
				vetElpSelectionneLabel.setVisible(true);
				elpLayout.setVisible(true);
				vetElpSelectionneLabel.setHeight("20px");
				//La VET est affichée dans le label du haut
				ligneSelectionneeLabel.setValue((String) hc.getItem(idVet).getItemProperty(LIBELLE_PROPERTY).getValue()+ " ("+typeVet+")");

			}
		}else{
			vetElpSelectionneLabel.setVisible(false);
			elpLayout.setVisible(false);
		}
		ligneSelectionneeLabel.setVisible(true);
		labelLigneSelectionneeLabel.setVisible(true);
	}


	private void deplierNoeudComplet(String itemId){
		deplierNoeud(itemId, false);
		table.setCollapsed(itemId, false);
		//parcourir les fils, pour chaque fils faire deplierNoeudComplet
		Collection<String> lfils = (Collection<String>)hc.getChildren(itemId);
		if(lfils!=null && !lfils.isEmpty()){
			for(String fils : lfils){
				deplierNoeudComplet(fils);
			}
		}
	}

	private void deplierNoeud(String itemId, boolean afficherMessage){
		if(!table.hasChildren(itemId)){

			String type =(String) hc.getItem(itemId).getItemProperty(TYPE_PROPERTY).getValue();
			String deplie =(String) hc.getItem(itemId).getItemProperty(DEPLIE_PROPERTY).getValue();
			String trueObjectId =(String) hc.getItem(itemId).getItemProperty(TRUE_ID_PROPERTY).getValue();
			String objectId = (String) hc.getItem(itemId).getItemProperty(ID_PROPERTY).getValue();

			//Si on n'a pas déjà déplié ou tenté de déplier cette élément
			if(deplie.equals("false")){
				if(type.equals(Utils.CMP)){
					//recuperation des vdi

					List<VersionDiplome> lvdi = composanteService.findVdiFromComposante(annee, trueObjectId);
					List<ObjetBase> lobj = new LinkedList<ObjetBase>();
					if(lvdi!=null && !lvdi.isEmpty()){
						for(VersionDiplome vdi : lvdi){

							ObjetBase obj = new ObjetBase();
							obj.setType(Utils.VDI);
							obj.setId(Utils.VDI+":"+vdi.getId().getCod_dip()+"/"+vdi.getId().getCod_vrs_vdi()+"_"+itemId);
							obj.setTrueObjectId(vdi.getId().getCod_dip()+"/"+vdi.getId().getCod_vrs_vdi());
							obj.setLibelle(vdi.getLib_web_vdi());
							obj.setDeplie("false");
							lobj.add(obj);

							Item i = hc.addItem(obj.getId());
							if(i!=null){
								renseignerItem(i,obj);

								table.setParent(obj.getId(), itemId);
							}else{
								//log.debug("attention : element non créé !");
							}

						} 
					}else{
						table.setChildrenAllowed(itemId, false);
					}
					//maj du Deplie pour la composante
					hc.getItem(itemId).getItemProperty(DEPLIE_PROPERTY).setValue("true");

				}else{
					if(type.equals(Utils.VDI)){

						//recuperation des vet
						String[] tabs=trueObjectId.split("/");
						String codDip=tabs[0];
						String vrsDip=tabs[1];

						String[] tabs2=objectId.split("CMP:");
						String codCmp=tabs2[1];


						List<VersionEtape> lvet = composanteService.findVetFromVdiAndCmp(annee, codDip, vrsDip, codCmp);
						List<ObjetBase> lobj = new LinkedList<ObjetBase>();
						if(lvet!=null && !lvet.isEmpty()){
							for(VersionEtape vet : lvet){

								ObjetBase obj = new ObjetBase();
								rechercheArborescenteController.renseigneObjFromVet(obj,vet, itemId);
								lobj.add(obj);

								Item i = hc.addItem(obj.getId());
								if(i!=null){
									renseignerItem(i,obj);

									table.setParent(obj.getId(), itemId);
								}


							}
						}else{
							table.setChildrenAllowed(itemId, false);
						}

						//maj du Deplie pour la VDI
						hc.getItem(itemId).getItemProperty(DEPLIE_PROPERTY).setValue("true");
					}else{
						if(type.equals(Utils.VET) || type.equals(Utils.ELP)){

							List<ElementPedagogique> lelp = new LinkedList<ElementPedagogique>();
							if(type.equals(Utils.VET)){
								//recuperation des elp
								String[] tabs=trueObjectId.split("/");
								String codEtp=tabs[0];
								String vrsEtp=tabs[1];
								lelp = composanteService.findElpFromVet( codEtp, vrsEtp);
							}
							//true si on est sur un ELP pour lequel on récupère des groupes, false sinon
							boolean elpAvecGroupe=false;
							if(type.equals(Utils.ELP)){
								//recuperation des elp
								lelp = composanteService.findElpFromElp( trueObjectId);

								//Si ELP n'a pas de fils , on cherche les groupes
								if(lelp==null || lelp.isEmpty()){
									//On tente de récupèrer les groupes de l'ELP
									List<ElpDeCollection> lgroupes =  listeInscritsController.recupererGroupes(annee, trueObjectId);
									//Si on a récupéré des groupes
									if(lgroupes != null && !lgroupes.isEmpty()){
										//Vrai si on a plusieurs collection (dans ce cas on affiche les collections dans l'arbo)
										boolean plsrsCollection = false;
										//On parcourt l'ELP (un seul ELP dans la liste en vérité)
										for(ElpDeCollection edc : lgroupes){
											//Si plusieurs collection
											if(edc.getListeCollection().size()>1){
												plsrsCollection = true;
											}
											for(CollectionDeGroupes cdg : edc.getListeCollection()){
												String itemIdPourGroupe=itemId;
												//Si plusieurs collection on ajoute la collection dans la table
												if(plsrsCollection){
													//On créé un nouvel objet basique à inséré dans le tableau
													ObjetBase obj = new ObjetBase();
													//On value l'objet obj à partir de l'ELP en cours
													rechercheArborescenteController.renseigneObjFromCollection(obj, cdg, itemId);
													//On ajout l'objet au tableau
													Item i = hc.addItem(obj.getId());
													if(i!=null){
														//On value l'item du tableau à partir de l'objet
														renseignerItem(i,obj);
														//On branche correctement l'item avec son père
														table.setParent(obj.getId(), itemId);
														//On déplie la collection automatiquement
														table.setCollapsed(obj.getId(), false);
														//On remet le focus sur l'ELP
														selectionnerLigne(itemId);
														//Le pere des groupes sera la collection et non l'elp
														itemIdPourGroupe=obj.getId();
													}
												}
												//Pour chaque groupe
												for(Groupe groupe : cdg.getListeGroupes()){
													elpAvecGroupe=true;
													//On ajoute le groupe
													//On créé un nouvel objet basique à inséré dans le tableau
													ObjetBase obj = new ObjetBase();
													//On value l'objet obj à partir de l'ELP en cours
													rechercheArborescenteController.renseigneObjFromGroupe(obj, groupe, itemIdPourGroupe);
													//On ajout l'objet au tableau
													Item i = hc.addItem(obj.getId());
													if(i!=null){
														//On value l'item du tableau à partir de l'objet
														renseignerItem(i,obj);
														//On branche correctement l'item avec son père
														table.setParent(obj.getId(), itemIdPourGroupe);
														//On fait disparaitre la fleche à gauche de l'élément sans fils
														table.setChildrenAllowed(obj.getId(), false);
													}

												}
											}
										}
									}
								}
							}

							//Si on a récupéré des ELP
							if(lelp!=null && !lelp.isEmpty()){
								//On parcourt les ELP
								for(ElementPedagogique elp : lelp){
									//On créé un nouvel objet basique à inséré dans le tableau
									ObjetBase obj = new ObjetBase();
									//On value l'objet obj à partir de l'ELP en cours
									rechercheArborescenteController.renseigneObjFromElp(obj, elp, itemId);
									//On ajoute l'objet à la liste
									//lobj.add(obj);
									//On ajout l'objet au tableau
									Item i = hc.addItem(obj.getId());
									if(i!=null){
										//On value l'item du tableau à partir de l'objet
										renseignerItem(i,obj);
										//On branche correcetement l'item avec son père
										table.setParent(obj.getId(), itemId);
									}
								}
							}else{
								//Si on n'est pas sur un ELP avec des groupes
								if(!elpAvecGroupe){
									//Si aucun elp récupéré et qu'on doit afficher le message
									if(afficherMessage){
										//On affiche le message de notification
										Notification.show("Aucun sous élément pour cet élément");
									}
									//On fait disparaitre la fleche à gauche de l'élément sans fils
									table.setChildrenAllowed(itemId, false);
								}
							}

							//maj du Deplie pour l'element ou la vet
							hc.getItem(itemId).getItemProperty(DEPLIE_PROPERTY).setValue("true");
						}
					}
				}
			}

		}
	}

	public void deplierTout(String itemId) {
		deplierNoeudComplet((String)itemId);
		selectionnerLigne((String)itemId);
		table.setCurrentPageFirstItemId((String)itemId);
	}

	private static class RechercheArboThread extends Thread {
		private final MainUI mainUI;
		private final RechercheArborescenteView rechercheArborescenteView;
		private String itemId;

		public RechercheArboThread(MainUI mainUI, RechercheArborescenteView rechercheArborescenteView, String itemId) {
			this.mainUI = mainUI;
			this.rechercheArborescenteView = rechercheArborescenteView;
			this.itemId = itemId;
		}
		@Override
		public void run() {
			mainUI.access(() -> rechercheArborescenteView.deplierTout(itemId));
			//close de la pop-up de loading
			mainUI.access(() -> mainUI.stopBusyIndicator());
		}
	}
}
