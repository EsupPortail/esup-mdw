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
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Favoris
 */
@Component @Scope("prototype")
@SpringView(name = FavorisView.NAME)
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
	@Resource
	private transient ConfigController configController;
	private RechercheControllerThread rct;

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
		if(configController.isApplicationActive() && UI.getCurrent() instanceof MainUI && userController.isEnseignant() ){

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

			if(lfav!=null && !lfav.isEmpty()){
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

			if(lfav==null || lfav.isEmpty()){
				labelAucunFavoriLayout.setVisible(true);
			}

			addComponent(globalLayout);
		}
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
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
			btnfav.addStyleName("left-action-button");
			btnfav.setDescription("Supprimer des favoris");
			//Gestion du clic sur le bouton de suppression du favori
			btnfav.addClickListener(new Button.ClickListener() {
				@Override
				public void buttonClick(Button.ClickEvent event) {
					FavorisPK fpk = new FavorisPK();
					fpk.setIdfav(idObj);
					fpk.setLogin(userController.getCurrentUserName());
					fpk.setTypfav(typeObj);
					favorisController.removeFavori(fpk);
					//favorisTable.removeItem(itemId)
					bic.removeItem(itemId);
					favorisTable.sanitizeSelection();
					favorisTable.setPageLength(favorisTable.getItemIds().size() );
					if(favorisTable.getItemIds().isEmpty()){
						favorisTable.setVisible(false);
						labelAucunFavoriLayout.setVisible(true);
					}
				}
			});
			boutonActionLayout.addComponent(btnfav);

			Button btnArbo = null;
			// Si on peut accéder à l'arborescence depuis le favori
			if(typeObj!=null && liste_type_arbo!=null && liste_type_arbo.contains(typeObj)){
				btnArbo = new Button();
				btnArbo.setIcon(FontAwesome.SITEMAP);
				btnArbo.setDescription(applicationContext.getMessage(NAME+".accesarborescence", null, getLocale()));
				btnArbo.addClickListener(e->{
					rechercheController.accessToRechercheArborescente(idObj,typeObj);
				});
				boutonActionLayout.addComponent(btnArbo);
			}
			// Si on peut accéder à la liste des inscrits depuis le favori
			if(typeObj!=null && liste_types_inscrits!=null && liste_types_inscrits.contains(typeObj)){
				if (btnArbo != null) {
					btnArbo.addStyleName("middle-action-button");
				}
				Button btnListeInscrits=new Button();
				btnListeInscrits.setIcon(FontAwesome.USERS);
				btnListeInscrits.addStyleName(ValoTheme.BUTTON_FRIENDLY);
				btnListeInscrits.addStyleName("right-action-button");
				btnListeInscrits.setDescription(applicationContext.getMessage(NAME+".acceslisteinscrits", null, getLocale()));
				btnListeInscrits.addClickListener(e->{

					//Si on doit afficher une fenêtre de loading pendant l'exécution
					if(PropertyUtils.isPushEnabled() && PropertyUtils.isShowLoadingIndicator()){
						//affichage de la pop-up de loading
						MainUI.getCurrent().startBusyIndicator();
						MainUI.getCurrent().push();

						//Execution de la méthode en parallèle dans un thread
						rct = new RechercheControllerThread(MainUI.getCurrent(),rechercheController,idObj,typeObj);
						rct.start();

					}else{
						//On ne doit pas afficher de fenêtre de loading, on exécute directement la méthode
						rechercheController.accessToDetail(idObj,typeObj,null);
					}

				});
				boutonActionLayout.addComponent(btnListeInscrits);
			} else {
				btnArbo.addStyleName("right-action-button");
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

			return boutonActionLayout;
		}
	}

	private static class RechercheControllerThread extends Thread {
		private final MainUI mainUI;
		private final RechercheController rechercheController;
		private final String typeObj;
		private final String idObj;

		public RechercheControllerThread(MainUI mainUI, RechercheController rechercheController, String idObj, String typeObj) {
			this.mainUI = mainUI;
			this.rechercheController = rechercheController;
			this.idObj = idObj;
			this.typeObj = typeObj;
		}
		@Override
		public void run() {
			mainUI.access(() -> rechercheController.accessToDetail(idObj,typeObj,null));
			mainUI.access(() -> mainUI.stopBusyIndicator());
		}
	}
}
