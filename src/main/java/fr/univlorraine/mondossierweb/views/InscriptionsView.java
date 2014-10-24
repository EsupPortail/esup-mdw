package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(InscriptionsView.NAME)
@StyleSheet("inscriptionsView.css")
public class InscriptionsView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "inscriptionsView";

	public static final String[] IA_FIELDS_ORDER = {"cod_anu", "cod_comp","cod_etp","cod_vrs_vet","lib_etp"};

	public static final String[] DAC_FIELDS_ORDER = {"cod_anu", "cod_dac","lib_cmt_dac","lib_etb","res"};


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

		Panel panelInscription= new Panel(MainUI.getCurrent().getEtudiant().getLibEtablissement());
		

		Table inscriptionsTable = new Table(null, new BeanItemContainer<>(Inscription.class, MainUI.getCurrent().getEtudiant().getLinsciae()));
		inscriptionsTable.setWidth("100%");
		inscriptionsTable.setVisibleColumns((Object[]) IA_FIELDS_ORDER);
		for (String fieldName : IA_FIELDS_ORDER) {
			inscriptionsTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
		}
		//inscriptionsTable.setSortContainerPropertyId("cod_anu");
		inscriptionsTable.setColumnCollapsingAllowed(true);
		inscriptionsTable.setColumnReorderingAllowed(true);
		inscriptionsTable.setSelectable(true);
		inscriptionsTable.setImmediate(true);
		inscriptionsTable.setPageLength(inscriptionsTable.getItemIds().size() );
		panelInscription.setContent(inscriptionsTable);
		globalLayout.addComponent(panelInscription);


		//DAC
		Panel panelDAC= new Panel(applicationContext.getMessage(NAME + ".dac.title", null, getLocale()));

		if(MainUI.getCurrent().getEtudiant().getLinscdac()!=null && MainUI.getCurrent().getEtudiant().getLinscdac().size()>0){
			Table inscriptionsDAC = new Table(null, new BeanItemContainer<>(Inscription.class, MainUI.getCurrent().getEtudiant().getLinscdac()));
			inscriptionsDAC.setWidth("100%");
			inscriptionsDAC.setVisibleColumns((Object[]) DAC_FIELDS_ORDER);
			for (String fieldName : DAC_FIELDS_ORDER) {
				inscriptionsDAC.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".tabledac." + fieldName, null, getLocale()));
			}
			inscriptionsDAC.setColumnCollapsingAllowed(true);
			inscriptionsDAC.setColumnReorderingAllowed(true);
			inscriptionsDAC.setSelectable(true);
			inscriptionsDAC.setImmediate(true);
			inscriptionsDAC.setPageLength(inscriptionsDAC.getItemIds().size() );
			panelDAC.setContent(inscriptionsDAC);
		}else{
			HorizontalLayout labelDacLayout = new HorizontalLayout();
			labelDacLayout.setMargin(true);
			labelDacLayout.setSizeFull();
			Label aucuneDAC = new Label(applicationContext.getMessage(NAME + ".dac.aucune", null, getLocale())+ " "+MainUI.getCurrent().getEtudiant().getLibEtablissement());
			aucuneDAC.setStyleName(ValoTheme.LABEL_COLORED);
			aucuneDAC.addStyleName(ValoTheme.LABEL_BOLD);
			labelDacLayout.addComponent(aucuneDAC);
			panelDAC.setContent(labelDacLayout);
		}
		globalLayout.addComponent(panelDAC);
		
		
		Panel panelPremInscription= new Panel(applicationContext.getMessage(NAME + ".premiereinsc.title", null, getLocale()));
		FormLayout formPremInscription = new FormLayout();
		formPremInscription.setSpacing(true);
		formPremInscription.setMargin(true);
		
		String captionAnneePremInscription = applicationContext.getMessage(NAME+".premiereinsc.annee", null, getLocale());
		TextField fieldAnneePremInscription = new TextField(captionAnneePremInscription, MainUI.getCurrent().getEtudiant().getAnneePremiereInscrip());
		formatTextField(fieldAnneePremInscription);
		formPremInscription.addComponent(fieldAnneePremInscription);
		
		String captionEtbPremInscription = applicationContext.getMessage(NAME+".premiereinsc.etb", null, getLocale());
		TextField fieldEtbPremInscription = new TextField(captionEtbPremInscription, MainUI.getCurrent().getEtudiant().getEtbPremiereInscrip());
		formatTextField(fieldEtbPremInscription);
		formPremInscription.addComponent(fieldEtbPremInscription);
		
		panelPremInscription.setContent(formPremInscription);
		globalLayout.addComponent(panelPremInscription);
		
		addComponent(globalLayout);


	}

	private void formatTextField(TextField tf){
		tf.setEnabled(false);
		tf.setSizeFull();
		tf.setNullRepresentation("");
		tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
	}

	
	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
