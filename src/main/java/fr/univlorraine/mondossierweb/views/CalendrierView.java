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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.CalendrierController;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.converters.DateToStringConverter;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.utils.MyFileDownloader;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@SpringView(name = CalendrierView.NAME)
@PreAuthorize("@userController.hasRoleInProperty('consultation_dossier')")
public class CalendrierView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(CalendrierView.class);

	public static final String NAME = "calendrierView";

	public static final String[] CAL_FIELDS = {"datedeb", "heure","duree","epreuve"};
	
	public static final String[] CAL_FIELDS_DETAIL = {"datedeb", "heure","duree","epreuve", "codeepreuve", "libsession", "codeetape", "versionetape"};

	public static final String[] CAL_FIELDS_AVEC_PLACE = {"datedeb", "heure","duree","place","epreuve"};
	
	public static final String[] CAL_FIELDS_AVEC_PLACE_DETAIL = {"datedeb", "heure","duree","place","epreuve", "codeepreuve", "libsession", "codeetape", "versionetape"};

	public static final String[] CAL_FIELDS_ORDER = {"datedeb", "heure","duree","batiment","salle","epreuve"};

	public static final String[] CAL_FIELDS_ORDER_AVEC_PLACE = {"datedeb", "heure","duree","batiment","salle","place","epreuve"};

	public static final String[] CAL_FIELDS_ORDER_DETAIL = {"datedeb", "heure","duree","batiment","salle","epreuve", "codeepreuve", "libsession", "codeetape", "versionetape"};

	public static final String[] CAL_FIELDS_ORDER_AVEC_PLACE_DETAIL = {"datedeb", "heure","duree","batiment","salle","place","epreuve", "codeepreuve", "libsession", "codeetape", "versionetape"};

	
	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient CalendrierController calendrierController;
	@Resource
	private transient ConfigController configController;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && MainUI.getCurrent()!=null && MainUI.getCurrent().getEtudiant()!=null &&
			((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiant()) || 
				(userController.isEnseignant() && configController.isAffCalendrierEpreuvesEnseignant()) ||
				(userController.isGestionnaire() && configController.isAffCalendrierEpreuvesGestionnaire())) ) {
			
			/* Style */
			setMargin(true);
			setSpacing(true);

			//Si on n'a pas déjà essayer de récupérer le calendrier
			if(!MainUI.getCurrent().getEtudiant().isCalendrierRecupere()){
				etudiantController.recupererCalendrierExamens();
			}


			/* Titre */
			HorizontalLayout titleLayout = new HorizontalLayout();
			titleLayout.setWidth("100%");
			Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
			title.addStyleName(ValoTheme.LABEL_H1);
			titleLayout.addComponent(title);
			titleLayout.setComponentAlignment(title,Alignment.MIDDLE_LEFT);
			//Test si on a des diplomes ou des etapes
			if(MainUI.getCurrent().getEtudiant().getCalendrier()!=null && MainUI.getCurrent().getEtudiant().getCalendrier().size()>0){
				Button pdfButton = new Button();
				pdfButton.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				pdfButton.addStyleName("button-big-icon");
				pdfButton.addStyleName("red-button-icon");
				pdfButton.setIcon(FontAwesome.FILE_PDF_O);
				pdfButton.setDescription(applicationContext.getMessage(NAME + ".btn.pdf.description", null, getLocale()));
				if(PropertyUtils.isPushEnabled()){
					MyFileDownloader fd = new MyFileDownloader(calendrierController.exportPdf());
					fd.extend(pdfButton);
				}else{
					FileDownloader fd = new FileDownloader(calendrierController.exportPdf());
					fd.setOverrideContentType(false);
					fd.extend(pdfButton);
				}
				titleLayout.addComponent(pdfButton);
				titleLayout.setComponentAlignment(pdfButton, Alignment.MIDDLE_RIGHT);
			}
			addComponent(titleLayout);


			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setSizeFull();
			globalLayout.setSpacing(true);


			/* Message d'info */
			if(applicationContext.getMessage(NAME+".message.info", null, getLocale()) != null){
				Panel panelVue= new Panel();

				HorizontalLayout vueLayout = new HorizontalLayout();
				vueLayout.setMargin(true);
				vueLayout.setSpacing(true);
				vueLayout.setSizeFull();

				Label vueLabel=new Label(applicationContext.getMessage(NAME+".message.info", null, getLocale()));
				vueLabel.setContentMode(ContentMode.HTML); 
				vueLabel.setStyleName(ValoTheme.LABEL_SMALL);
				vueLayout.addComponent(vueLabel);
				vueLayout.setExpandRatio(vueLabel, 1);

				panelVue.setContent(vueLayout);
				globalLayout.addComponent(panelVue);
			}



			/* Le Calendrier */
			Panel panelCalendrier= new Panel(applicationContext.getMessage(NAME + ".calendrier.title", null, getLocale()));
			panelCalendrier.setSizeFull();

			if(MainUI.getCurrent().getEtudiant()!=null && MainUI.getCurrent().getEtudiant().getCalendrier()!=null && MainUI.getCurrent().getEtudiant().getCalendrier().size()>0){

				BeanItemContainer<Examen> bic= new BeanItemContainer<>(Examen.class, MainUI.getCurrent().getEtudiant().getCalendrier());
				Table calendrierTable = new Table(null, bic);
				calendrierTable.setWidth("100%");
				String[] colonnes_to_create = CAL_FIELDS;
				if(configController.isAffDetailExamen()) {
					colonnes_to_create = CAL_FIELDS_DETAIL;
				}
				if(configController.isAffNumPlaceExamen()){
					colonnes_to_create = CAL_FIELDS_AVEC_PLACE;
					if(configController.isAffDetailExamen()) {
              	       colonnes_to_create = CAL_FIELDS_AVEC_PLACE_DETAIL;
					}
				}
				for (String fieldName : colonnes_to_create) {
					calendrierTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
				}
				calendrierTable.addGeneratedColumn("batiment", new BatimentColumnGenerator());
				calendrierTable.addGeneratedColumn("salle", new SalleColumnGenerator());
				calendrierTable.setColumnHeader("batiment", applicationContext.getMessage(NAME+".table.batiment", null, getLocale()));
				calendrierTable.setColumnHeader("salle", applicationContext.getMessage(NAME+".table.salle", null, getLocale()));
				String[] colonnes_to_display = CAL_FIELDS_ORDER;
				if(configController.isAffDetailExamen()) {
                    colonnes_to_display = CAL_FIELDS_ORDER_DETAIL;
				}
				if(configController.isAffNumPlaceExamen()){
					colonnes_to_display = CAL_FIELDS_ORDER_AVEC_PLACE;
					if(configController.isAffDetailExamen()) {
                        colonnes_to_display = CAL_FIELDS_ORDER_AVEC_PLACE_DETAIL;
                	}
				}
				calendrierTable.setVisibleColumns((Object[]) colonnes_to_display);
				calendrierTable.setColumnCollapsingAllowed(true);
				calendrierTable.setColumnReorderingAllowed(true);
				calendrierTable.setSelectable(false);
				calendrierTable.setImmediate(true);
				calendrierTable.setConverter("datedeb", new DateToStringConverter());
				calendrierTable.setStyleName("noscrollabletable");
				calendrierTable.setPageLength(calendrierTable.getItemIds().size() );

				panelCalendrier.setContent(calendrierTable);
			}else{
				HorizontalLayout labelExamenLayout = new HorizontalLayout();
				labelExamenLayout.setMargin(true);
				labelExamenLayout.setSizeFull();
				Label aucunExamen = new Label(applicationContext.getMessage(NAME + ".examen.aucun", null, getLocale()));
				aucunExamen.setStyleName(ValoTheme.LABEL_COLORED);
				aucunExamen.addStyleName(ValoTheme.LABEL_BOLD);
				labelExamenLayout.addComponent(aucunExamen);
				panelCalendrier.setContent(labelExamenLayout);
			}
			globalLayout.addComponent(panelCalendrier);

			addComponent(globalLayout);
		}
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}



	/** Formats the position in a column containing Date objects. */
	class BatimentColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Examen> bid = (BeanItem<Examen>) item;
			Examen ex = (Examen) bid.getBean();
			Label libLabel = new Label();
			libLabel.setValue(ex.getBatiment());
			libLabel.setDescription(ex.getLocalisation());
			return libLabel;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class SalleColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Examen> bid = (BeanItem<Examen>) item;
			Examen ex = (Examen) bid.getBean();
			Label libLabel = new Label();
			libLabel.setValue(ex.getSalle());
			libLabel.setDescription(ex.getLibsalle());
			return libLabel;
		}
	}

}
