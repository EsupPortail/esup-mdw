package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.CalendrierController;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.utils.MyFileDownloader;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(CalendrierView.NAME)
public class CalendrierView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(CalendrierView.class);

	public static final String NAME = "calendrierView";
	
	public static final String[] CAL_FIELDS = {"datedeb", "heure","duree","epreuve"};
	
	public static final String[] CAL_FIELDS_AVEC_PLACE = {"datedeb", "heure","duree","place","epreuve"};

	public static final String[] CAL_FIELDS_ORDER = {"datedeb", "heure","duree","batiment","salle","epreuve"};
	
	public static final String[] CAL_FIELDS_ORDER_AVEC_PLACE = {"datedeb", "heure","duree","batiment","salle","place","epreuve"};

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
			pdfButton.setIcon(FontAwesome.FILE_PDF_O);
			pdfButton.setDescription(applicationContext.getMessage(NAME + ".btn.pdf.description", null, getLocale()));
			MyFileDownloader fd = new MyFileDownloader(calendrierController.exportPdf());
			fd.extend(pdfButton);
			titleLayout.addComponent(pdfButton);
			titleLayout.setComponentAlignment(pdfButton, Alignment.MIDDLE_RIGHT);
		}
		addComponent(titleLayout);


		VerticalLayout globalLayout = new VerticalLayout();
		globalLayout.setSizeFull();
		globalLayout.setSpacing(true);




		Panel panelCalendrier= new Panel(applicationContext.getMessage(NAME + ".calendrier.title", null, getLocale()));
		panelCalendrier.setSizeFull();

		if(MainUI.getCurrent().getEtudiant()!=null && MainUI.getCurrent().getEtudiant().getCalendrier()!=null && MainUI.getCurrent().getEtudiant().getCalendrier().size()>0){

			BeanItemContainer<Examen> bic= new BeanItemContainer<>(Examen.class, MainUI.getCurrent().getEtudiant().getCalendrier());
			LOG.info("bic : "+bic.size());
			Table calendrierTable = new Table(null, bic);
			calendrierTable.setWidth("100%");
			String[] colonnes_to_create = CAL_FIELDS;
			if(configController.isAffNumPlaceExamen()){
				colonnes_to_create = CAL_FIELDS_AVEC_PLACE;
			}
			for (String fieldName : colonnes_to_create) {
				calendrierTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
			}
			calendrierTable.addGeneratedColumn("batiment", new BatimentColumnGenerator());
			calendrierTable.addGeneratedColumn("salle", new SalleColumnGenerator());
			calendrierTable.setColumnHeader("batiment", applicationContext.getMessage(NAME+".table.batiment", null, getLocale()));
			calendrierTable.setColumnHeader("salle", applicationContext.getMessage(NAME+".table.salle", null, getLocale()));
			String[] colonnes_to_display = CAL_FIELDS_ORDER;
			if(configController.isAffNumPlaceExamen()){
				colonnes_to_display = CAL_FIELDS_ORDER_AVEC_PLACE;
			}
			calendrierTable.setVisibleColumns((Object[]) colonnes_to_display);
			calendrierTable.setColumnCollapsingAllowed(true);
			calendrierTable.setColumnReorderingAllowed(true);
			calendrierTable.setSelectable(false);
			calendrierTable.setImmediate(true);
			calendrierTable.setStyleName("noscrollabletable");
			calendrierTable.setPageLength(calendrierTable.getItemIds().size() );
			LOG.info("calendrierTable.getItemIds().size()  : "+calendrierTable.getItemIds().size());

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
