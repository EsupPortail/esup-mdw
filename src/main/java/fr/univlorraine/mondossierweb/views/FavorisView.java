package fr.univlorraine.mondossierweb.views;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.GenericUI;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Favoris
 */
@Component @Scope("prototype")
@VaadinView(FavorisView.NAME)
public class FavorisView extends VerticalLayout implements View {

	private static final long serialVersionUID = 6309734175451108885L;


	public static final String NAME = "favorisView";


	public static final String[] FAV_FIELDS_ORDER = {"Type","id", "Libelle", "Actions"};



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheArborescenteController rechercheArborescenteController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient RechercheController rechercheController;


	/** Thread pool  */
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	private List<String> liste_types_inscrits;

	private List<String> liste_type_arbo;

	private Table favorisTable;

	private List<Favoris> lfav;

	private BeanItemContainer<Favoris> bic;

	private HorizontalLayout labelAucunFavoriLayout ;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {


		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && userController.isEnseignant() ){


			removeAllComponents();
			/* Style */
			setMargin(true);
			setSpacing(true);

			liste_types_inscrits= new LinkedList<String>();
			liste_types_inscrits.add(Utils.ELP);
			liste_types_inscrits.add(Utils.VET);

			liste_type_arbo= new LinkedList<String>();
			liste_type_arbo.add(Utils.CMP);
			liste_type_arbo.add(Utils.VET);

			lfav = favorisController.getFavoris();

			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setSizeFull();
			globalLayout.setSpacing(true);



			if(lfav!=null && lfav.size()>0){
				bic = new BeanItemContainer<>(Favoris.class,lfav);
				bic.addNestedContainerProperty("id.typfav");
				bic.addNestedContainerProperty("id.idfav");
				favorisTable = new Table(null, bic);
				favorisTable.setWidth("100%");

				favorisTable.addGeneratedColumn("Type", new DisplayTypeColumnGenerator());
				favorisTable.setColumnHeader("Type", applicationContext.getMessage(NAME+".table.id.typfav", null, getLocale()));

				favorisTable.addGeneratedColumn("id", new DisplayIdColumnGenerator());
				favorisTable.setColumnHeader("id", applicationContext.getMessage(NAME+".table.id.idfav", null, getLocale()));

				favorisTable.addGeneratedColumn("Libelle", new MyLibelleColumnGenerator());
				favorisTable.addGeneratedColumn("Actions", new MyActionsColumnGenerator());

				favorisTable.setVisibleColumns((Object[]) FAV_FIELDS_ORDER);
				favorisTable.setColumnCollapsingAllowed(true);
				favorisTable.setColumnReorderingAllowed(true);
				favorisTable.setSelectable(true);
				favorisTable.setImmediate(true);
				favorisTable.addStyleName("noscrollabletable");
				favorisTable.setPageLength(favorisTable.getItemIds().size() );
				globalLayout.addComponent(favorisTable);

			}

			labelAucunFavoriLayout = new HorizontalLayout();
			labelAucunFavoriLayout.setMargin(true);
			labelAucunFavoriLayout.setSizeFull();
			Label aucunFavoris = new Label(applicationContext.getMessage(NAME + ".favoris.aucun", null, getLocale()));
			aucunFavoris.setStyleName(ValoTheme.LABEL_COLORED);
			aucunFavoris.addStyleName(ValoTheme.LABEL_BOLD);
			labelAucunFavoriLayout.addComponent(aucunFavoris);
			labelAucunFavoriLayout.setVisible(false);
			globalLayout.addComponent(labelAucunFavoriLayout);

			if(lfav==null || lfav.size()==0){
				labelAucunFavoriLayout.setVisible(true);
			}


			addComponent(globalLayout);

		}
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//LOG.debug("ENTER FAVORIS VIEW");
	}

	class DisplayIdColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			String idObj = (String)item.getItemProperty("id.idfav").getValue();
			//On converti le type pour un affichage lisible
			return idObj;
		}
	}

	class DisplayTypeColumnGenerator implements Table.ColumnGenerator {

		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			String typeObj = (String)item.getItemProperty("id.typfav").getValue();
			String idObj = (String)item.getItemProperty("id.idfav").getValue();
			return rechercheArborescenteController.getTypeObj(typeObj,idObj);
		}
	}

	/** Formats the position in a column containing Date objects. */
	class MyActionsColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			String typeObj = (String)item.getItemProperty("id.typfav").getValue();
			String idObj = (String)item.getItemProperty("id.idfav").getValue();

			HorizontalLayout boutonActionLayout = new HorizontalLayout();

			Button btnfav=new Button();
			btnfav.setIcon(FontAwesome.TRASH_O);
			btnfav.setStyleName(ValoTheme.BUTTON_DANGER);
			btnfav.addStyleName("deletefavbutton");
			btnfav.setDescription("Supprimer des favoris");
			//Gestion du clic sur le bouton favori
			btnfav.addClickListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					FavorisPK fpk = new FavorisPK();
					fpk.setIdfav(idObj);
					fpk.setLogin(userController.getCurrentUserName());
					fpk.setTypfav(typeObj);
					favorisController.removeFavori(fpk);
					//favorisTable.removeItem(itemId)
					bic.removeItem(itemId);
					favorisTable.sanitizeSelection();
					favorisTable.setPageLength(favorisTable.getItemIds().size() );
					if(favorisTable.getItemIds().size()<1){
						favorisTable.setVisible(false);
						labelAucunFavoriLayout.setVisible(true);
					}


				}
			});
			boutonActionLayout.addComponent(btnfav);


			if(typeObj!=null && liste_type_arbo!=null && liste_type_arbo.contains(typeObj)){
				Button btnArbo=new Button();
				btnArbo.setIcon(FontAwesome.SITEMAP);
				btnArbo.setDescription(applicationContext.getMessage(NAME+".accesarborescence", null, getLocale()));
				btnArbo.addClickListener(e->{
					rechercheController.accessToRechercheArborescente(idObj,typeObj);
				});
				boutonActionLayout.addComponent(btnArbo);
			}
			if(typeObj!=null && liste_types_inscrits!=null && liste_types_inscrits.contains(typeObj)){
				Button btnListeInscrits=new Button();
				btnListeInscrits.setIcon(FontAwesome.USERS);
				btnListeInscrits.addStyleName(ValoTheme.BUTTON_FRIENDLY);
				btnListeInscrits.setDescription(applicationContext.getMessage(NAME+".acceslisteinscrits", null, getLocale()));
				btnListeInscrits.addClickListener(e->{

					//Si on doit afficher une fenêtre de loading pendant l'exécution
					if(PropertyUtils.isShowLoadingIndicator()){
						//affichage de la pop-up de loading
						MainUI.getCurrent().startBusyIndicator();

						//Execution de la méthode en parallèle dans un thread
						executorService.execute(new Runnable() {
							public void run() {
								MainUI.getCurrent().access(new Runnable() {
									@Override
									public void run() {
										rechercheController.accessToDetail(idObj,typeObj,null);
										//close de la pop-up de loading
										MainUI.getCurrent().stopBusyIndicator();
									}
								} );
							}
						});
					}else{
						//On ne doit pas afficher de fenêtre de loading, on exécute directement la méthode
						rechercheController.accessToDetail(idObj,typeObj,null);
					}

				});
				boutonActionLayout.addComponent(btnListeInscrits);
			}


			return boutonActionLayout;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class MyLibelleColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);
			String typeObj = (String)item.getItemProperty("id.typfav").getValue();
			String idObj = (String)item.getItemProperty("id.idfav").getValue();

			HorizontalLayout boutonActionLayout = new HorizontalLayout();

			if(typeObj!=null){
				Label lib = new Label(favorisController.getLibObjFavori(typeObj,idObj));
				boutonActionLayout.addComponent(lib);
			}

			//Recuperer le libelle de l'objet dans Apogée

			return boutonActionLayout;
		}
	}
}
