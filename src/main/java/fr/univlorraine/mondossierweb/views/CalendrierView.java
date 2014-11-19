package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
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
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.NoteController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(CalendrierView.NAME)
public class CalendrierView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(CalendrierView.class);

	public static final String NAME = "calendrierView";

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
		Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
		title.addStyleName(ValoTheme.LABEL_H1);
		titleLayout.addComponent(title);
		titleLayout.setComponentAlignment(title,Alignment.MIDDLE_LEFT);
		//Test si on a des diplomes ou des etapes
		if(MainUI.getCurrent().getEtudiant().getCalendrier()!=null && MainUI.getCurrent().getEtudiant().getCalendrier().size()>0){
			Button pdfButton = new Button();
			pdfButton.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
			pdfButton.setIcon(FontAwesome.FILE_PDF_O);
			pdfButton.setDescription(applicationContext.getMessage(NAME + ".btn.pdf.description", null, getLocale()));
			FileDownloader fd = new FileDownloader(calendrierController.exportPdf());
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
			
			String[] colonnes_to_display = CAL_FIELDS_ORDER;
			if(PropertyUtils.isAffNumPlaceExamen()){
				colonnes_to_display = CAL_FIELDS_ORDER_AVEC_PLACE;
			}
			
			calendrierTable.setVisibleColumns((Object[]) colonnes_to_display);
			for (String fieldName : colonnes_to_display) {
				calendrierTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
			}
			
			calendrierTable.setColumnCollapsingAllowed(true);
			calendrierTable.setColumnReorderingAllowed(true);
			calendrierTable.setSelectable(false);
			calendrierTable.setImmediate(true);
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

}
