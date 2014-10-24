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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(CalendrierView.NAME)
public class CalendrierView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(CalendrierView.class);

	public static final String NAME = "calendrierView";

	public static final String[] CAL_FIELDS_ORDER = {"datedeb", "heure","duree","batiment","salle","place","epreuve"};

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		/* Style */
		setMargin(true);
		setSpacing(true);

		/* Titre */
		Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);


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
			calendrierTable.setVisibleColumns((Object[]) CAL_FIELDS_ORDER);
			for (String fieldName : CAL_FIELDS_ORDER) {
				calendrierTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
			}
			calendrierTable.setColumnCollapsingAllowed(true);
			calendrierTable.setColumnReorderingAllowed(true);
			calendrierTable.setSelectable(true);
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
