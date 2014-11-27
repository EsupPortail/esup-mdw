package fr.univlorraine.mondossierweb.views;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.event.Action;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;
import com.vaadin.ui.Tree.ExpandEvent;
import com.vaadin.ui.Tree.ExpandListener;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.Favoris;
import fr.univlorraine.mondossierweb.entities.FavorisPK;
import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.VersionDiplome;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.vaadin.ObjetBase;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteService;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteServiceImpl;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Recherche arborescente
 */
@Component @Scope("prototype")
@VaadinView(RechercheArborescenteView.NAME)
public class RechercheArborescenteView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "rechercheArborescenteView";

	private static final String ID_PROPERTY = "id";

	private static final String TRUE_ID_PROPERTY = "trueObjectId";

	private static final String LIBELLE_PROPERTY = "libelle";

	private static final String DEPLIE_PROPERTY = "deplie";

	private static final String TYPE_PROPERTY ="type";

	private static final Action TOUT_DEPLIER_ACTION=new Action("Tout déplier");

	private static final Action METTRE_EN_FAVORI_ACTION=new Action("Mettre en favori");

	private static final Action SUPPRIMER_FAVORI_ACTION=new Action("Supprimer des favoris");

	private static final Action LISTE_INSCRIT_ACTION=new Action("Accéder à la liste d'inscrits");

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


	private HierarchicalContainer hc;
	
	private TreeTable table;

	private List<String> markedRows;
	
	private List<String> liste_types_favoris;
	
	private List<String> liste_types_inscrits;
	
	private List<String> liste_types_deplier;

	private String annee;

	private ComboBox comboBoxAnneeUniv;
	
	private String code;
	
	private String type;

	private boolean initEffectue;

	/**
	 * liste des composantes actives
	 */
	private List<Composante> lcomp;

	/**
	 * reinitialise la vue pour pointer sur les données en paramètres
	 * @param parameterMap
	 */
	public void initFromParameters(Map<String, String> parameterMap){
		removeAllComponents();
		code = parameterMap.get("code");
		type = parameterMap.get("type");
		init();
	}
	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		/* Style */
		setMargin(true);
		setSpacing(true);

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
		
		List<String> lanneeUniv = rechercheArborescenteController.recupererLesCinqDernieresAnneeUniversitaire();
		
		//Recuperation des favoris
		List<Favoris> lfav = favorisController.getFavorisFromLogin(userController.getCurrentUserName());
		markedRows = new LinkedList<String>();
		for(Favoris fav : lfav){
			String idFav = fav.getId().getTypfav()+":"+fav.getId().getIdfav();
			markedRows.add(idFav);
		}
		
		

		comboBoxAnneeUniv = new ComboBox("Année universitaire");
		for(String anneeUniv : lanneeUniv){
			comboBoxAnneeUniv.addItem(anneeUniv);
		}
		comboBoxAnneeUniv.setTextInputAllowed(false);
		comboBoxAnneeUniv.setNullSelectionAllowed(false);

		if(annee==null){
			annee=etudiantController.getAnneeUnivEnCours();
		}
		comboBoxAnneeUniv.setValue(annee);

		comboBoxAnneeUniv.addValueChangeListener(e -> changerAnnee((String)comboBoxAnneeUniv.getValue()));

		addComponent(comboBoxAnneeUniv);

		if(code!=null && type!=null){
			Label elementRecherche = new Label(code +" "+type);
			elementRecherche.addStyleName(ValoTheme.LABEL_H1);
			addComponent(elementRecherche);
		}

		table = new TreeTable();
		table.setSizeFull();
		table.setStyleName("scrollabletable");
		table.setSelectable(true);

		initComposantes();

	


		//gestion du style pour les lignes en favori
		table.setCellStyleGenerator(new CellStyleGenerator() {
			@Override
			public String getStyle(final Table source, final Object itemId,final Object propertyId) {
				String style = null;
				if (propertyId == null && markedRows.contains(itemId)) {
					style = "marked";
				}
				return style;
			}
		});


		//gestion du clic sur la fleche pour déplier une entrée
		table.addExpandListener(new ExpandListener() {

			private static final long serialVersionUID = 8532342540008245348L;

			@Override
			public void nodeExpand(ExpandEvent event) {
				//System.out.println("node expand "+event.getItemId());
				if(!table.hasChildren(event.getItemId())){

					String type =(String) hc.getItem(event.getItemId()).getItemProperty(TYPE_PROPERTY).getValue();
					String deplie =(String) hc.getItem(event.getItemId()).getItemProperty("deplie").getValue();
					String trueObjectId =(String) hc.getItem(event.getItemId()).getItemProperty("trueObjectId").getValue();
					//System.out.println("searchChild of : "+type + " "+trueObjectId+" "+deplie);

					//Si on n'a pas déjà déplié ou tenté de déplier cette élément
					if(deplie.equals("false")){
						if(type.equals(Utils.CMP)){
							//recuperation des vdi

							List<VersionDiplome> lvdi = composanteService.findVdiFromComposante(annee, trueObjectId);
							//System.out.println("lvdi "+lvdi!=null?lvdi.size():"vide");
							List<ObjetBase> lobj = new LinkedList<ObjetBase>();
							for(VersionDiplome vdi : lvdi){

								ObjetBase obj = new ObjetBase();
								obj.setType(Utils.VDI);
								obj.setId(Utils.VDI+":"+vdi.getId().getCod_dip()+"/"+vdi.getId().getCod_vrs_vdi()+"_"+event.getItemId());
								obj.setTrueObjectId(vdi.getId().getCod_dip()+"/"+vdi.getId().getCod_vrs_vdi());
								obj.setLibelle(vdi.getLib_web_vdi());
								obj.setDeplie("false");
								lobj.add(obj);

								//System.out.println("vdi : "+obj.getTrueObjectId()+" : "+obj.getLibelle() +" pour pere : "+event.getItemId());
								Item i = hc.addItem(obj.getId());
								if(i!=null){
									i.getItemProperty("libelle").setValue(obj.getLibelle());
									i.getItemProperty("id").setValue(obj.getId());
									i.getItemProperty("trueObjectId").setValue(obj.getTrueObjectId());
									i.getItemProperty(TYPE_PROPERTY).setValue(obj.getType());
									i.getItemProperty("deplie").setValue(obj.getDeplie());

									table.setParent(obj.getId(), event.getItemId());
								}else{
									//System.out.println("attention : element non créé !");
								}

							} 
							//maj du Deplie pour la composante
							hc.getItem(event.getItemId()).getItemProperty("deplie").setValue("true");

						}else{
							if(type.equals(Utils.VDI)){

								//recuperation des vet
								String[] tabs=trueObjectId.split("/");
								String codDip=tabs[0];
								String vrsDip=tabs[1];
								List<VersionEtape> lvet = composanteService.findVetFromVdi(annee, codDip, vrsDip);
								List<ObjetBase> lobj = new LinkedList<ObjetBase>();
								for(VersionEtape vet : lvet){

									ObjetBase obj = new ObjetBase();
									obj.setType(Utils.VET);
									obj.setId(Utils.VET+":"+vet.getId().getCod_etp()+"/"+vet.getId().getCod_vrs_vet()+"_"+event.getItemId());
									obj.setTrueObjectId(vet.getId().getCod_etp()+"/"+vet.getId().getCod_vrs_vet());
									obj.setLibelle(vet.getLib_web_vet());
									obj.setDeplie("false");
									lobj.add(obj);

									//System.out.println("vdi : "+obj.getTrueObjectId()+" : "+obj.getLibelle() +" pour pere : "+event.getItemId());
									Item i = hc.addItem(obj.getId());
									if(i!=null){
										i.getItemProperty("libelle").setValue(obj.getLibelle());
										i.getItemProperty("id").setValue(obj.getId());
										i.getItemProperty("trueObjectId").setValue(obj.getTrueObjectId());
										i.getItemProperty(TYPE_PROPERTY).setValue(obj.getType());
										i.getItemProperty("deplie").setValue(obj.getDeplie());

										table.setParent(obj.getId(), event.getItemId());
									}else{
										//System.out.println("attention : element non créé !");
									}


								}

								//maj du Deplie pour la VDI
								hc.getItem(event.getItemId()).getItemProperty("deplie").setValue("true");
							}else{
								if(type.equals(Utils.VET) || type.equals(Utils.ELP)){

									List<ElementPedagogique> lelp = new LinkedList<ElementPedagogique>();
									if(type.equals(Utils.VET)){
										//recuperation des elp
										String[] tabs=trueObjectId.split("/");
										String codEtp=tabs[0];
										String vrsEtp=tabs[1];
										//System.out.println("appel native query avec : "+codEtp+" - "+vrsEtp);
										lelp = composanteService.findElpFromVet( codEtp, vrsEtp);
									}
									if(type.equals(Utils.ELP)){
										//recuperation des elp
										lelp = composanteService.findElpFromElp( trueObjectId);
									}

									List<ObjetBase> lobj = new LinkedList<ObjetBase>();
									if(lelp!=null && lelp.size()>0){
										for(ElementPedagogique elp : lelp){

											ObjetBase obj = new ObjetBase();
											obj.setType(Utils.ELP);
											obj.setId(Utils.ELP+":"+elp.getCod_elp()+"_"+event.getItemId());
											obj.setTrueObjectId(elp.getCod_elp());
											obj.setLibelle(elp.getLib_elp());
											obj.setDeplie("false");
											lobj.add(obj);

											//System.out.println("vdi : "+obj.getTrueObjectId()+" : "+obj.getLibelle() +" pour pere : "+event.getItemId());
											Item i = hc.addItem(obj.getId());
											if(i!=null){
												i.getItemProperty("libelle").setValue(obj.getLibelle());
												i.getItemProperty("id").setValue(obj.getId());
												i.getItemProperty("trueObjectId").setValue(obj.getTrueObjectId());
												i.getItemProperty(TYPE_PROPERTY).setValue(obj.getType());
												i.getItemProperty("deplie").setValue(obj.getDeplie());

												table.setParent(obj.getId(), event.getItemId());
											}else{
												System.out.println("attention : element non créé !");
											}


										}
									}else{
										//System.out.println("Aucun sous elp pour "+event.getItemId());
										Notification.show("Aucun sous élément pour cet élément");
										table.setChildrenAllowed(event.getItemId(), false);
									}

									//maj du Deplie pour l'element ou la vet
									hc.getItem(event.getItemId()).getItemProperty("deplie").setValue("true");
								}
							}
						}
					}
					/*List<Livre> llivre = diplomeController.getDipFromCompByAnnee(event.getItemId().toString(), annee);
					for(Livre livre : llivre){
						Item i = hc.addItem("livre"+livre.getId().toString());
						if(i!=null){
							i.getItemProperty("libelle").setValue(livre.getTitre());
							i.getItemProperty("id").setValue(livre.getId().toString());
							table.setParent("livre"+livre.getId().toString(), event.getItemId());
						}
					}*/
				}
			}


		});




		VerticalLayout tableVerticalLayout = new VerticalLayout();
		tableVerticalLayout.setMargin(true);
		tableVerticalLayout.setSizeFull();
		tableVerticalLayout.addComponent(table);
		tableVerticalLayout.setExpandRatio(table, 1);
		addComponent(tableVerticalLayout);
		setExpandRatio(tableVerticalLayout, 1);
	



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
		if(lcomp==null || lcomp.size()==0){
			lcomp = composanteService.findComposantesEnService();
		}
		List<ObjetBase> lobj = new LinkedList<ObjetBase>();
		for(Composante comp : lcomp){
			ObjetBase obj = new ObjetBase();
			obj.setType(Utils.CMP);
			obj.setId(Utils.CMP+":"+comp.getCodCmp());
			obj.setTrueObjectId(comp.getCodCmp());
			obj.setLibelle(comp.getLibCmp());
			obj.setDeplie("false");
			lobj.add(obj);
			Item i = hc.addItem(obj.getId());

			i.getItemProperty("libelle").setValue(obj.getLibelle());
			i.getItemProperty("id").setValue(obj.getId());
			i.getItemProperty("trueObjectId").setValue(obj.getTrueObjectId());
			i.getItemProperty(TYPE_PROPERTY).setValue(obj.getType());
			i.getItemProperty("deplie").setValue(obj.getDeplie());

		}
		//Vrai si c'est la premiere initialisation de la table
		if(initEffectue){
			table.removeAllItems();
		}
		table.setContainerDataSource(hc);
		if(!initEffectue){
			//System.out.println("set visible colonnes for init");
			table.addContainerProperty(TRUE_ID_PROPERTY, String.class, "");
			table.addContainerProperty(LIBELLE_PROPERTY, String.class, "");
			table.setVisibleColumns(DETAIL_FIELDS_ORDER);
			table.setColumnHeader("libelle", applicationContext.getMessage(NAME+".table.libelle", null, getLocale()));
			table.setColumnHeader("trueObjectId", applicationContext.getMessage(NAME+".table.trueObjectId", null, getLocale()));
			table.setColumnHeader("type", applicationContext.getMessage(NAME+".table.type", null, getLocale()));
			table.addGeneratedColumn("actions", new MyColumnGenerator());
			table.setColumnHeader("actions", applicationContext.getMessage(NAME+".table.actions", null, getLocale()));
			initEffectue=true;
		}else{
			//System.out.println("set visible colonnes for refresh");
			table.addContainerProperty(TRUE_ID_PROPERTY, String.class, "");
			table.addContainerProperty(LIBELLE_PROPERTY, String.class, "");
			table.setVisibleColumns(DETAIL_FIELDS_ORDER_ON_REFRESH);
		}

	}
	private void changerAnnee(String value) {
		annee=value;
		//System.out.println("changment annee : "+annee);
		initComposantes();
	}
	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//System.out.println("enter");
	}

	/** Formats the position in a column containing Date objects. */
	class MyColumnGenerator implements Table.ColumnGenerator {
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

			//Si c'est un objet qui peut être mis en favori
			if(typeObj!=null && liste_types_favoris!=null && liste_types_favoris.contains(typeObj)){
				Button btnfav=new Button();
				//System.out.println("itemId : "+itemId);
				//if(markedRows.contains((String)itemId)){
				if(markedRows.contains(idFav)){	
					btnfav.setIcon(FontAwesome.BOOKMARK);
					btnfav.addStyleName(ValoTheme.BUTTON_PRIMARY);
					btnfav.setDescription("Supprimer des favoris");
				}else{
					btnfav.setIcon(FontAwesome.BOOKMARK_O);
					btnfav.addStyleName(ValoTheme.BUTTON_PRIMARY);
					btnfav.setDescription("Mettre en favori");
				}

				//Gestion du clic sur le bouton favori
				btnfav.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
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
							btnfav.setIcon(FontAwesome.BOOKMARK_O);
							event.getButton().setIcon(FontAwesome.BOOKMARK_O);
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
							btnfav.setIcon(FontAwesome.BOOKMARK);
							event.getButton().setIcon(FontAwesome.BOOKMARK);
						}
						table.markAsDirtyRecursive();
						//btonFavori.markAsDirty();
					}
				});
				boutonActionLayout.addComponent(btnfav);
			}
			
			if(typeObj!=null && liste_types_deplier!=null && liste_types_deplier.contains(typeObj)){
			Button btnDeplier=new Button();
			btnDeplier.setIcon(FontAwesome.SITEMAP);
			btnDeplier.setDescription("Déplier l'arborescence");
			boutonActionLayout.addComponent(btnDeplier);
			}
			if(typeObj!=null && liste_types_inscrits!=null && liste_types_inscrits.contains(typeObj)){
				Button btnListeInscrits=new Button();
				btnListeInscrits.setIcon(FontAwesome.USERS);
				btnListeInscrits.addStyleName(ValoTheme.BUTTON_FRIENDLY);
				btnListeInscrits.setDescription("Accèder à la liste des inscrits");
				boutonActionLayout.addComponent(btnListeInscrits);
			}
			

			return boutonActionLayout;
		}
	}

}
