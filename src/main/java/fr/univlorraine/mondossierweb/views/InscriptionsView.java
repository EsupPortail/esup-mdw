package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.WordUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.InscriptionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.views.windows.DetailInscriptionWindow;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(InscriptionsView.NAME)
public class InscriptionsView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "inscriptionsView";

	public static final String[] IA_FIELDS_ORDER = {"cod_anu", "cod_comp","cod_etp","cod_vrs_vet"};

	public static final String[] IA_FIELDS_ORDER_ETU = {"cod_anu", "lib_comp"};

	public static final String[] DAC_FIELDS_ORDER = {"cod_anu", "cod_dac","lib_cmt_dac","lib_etb","res"};



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient InscriptionController inscriptionController;



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

		//Si les informations sur les inscriptions n'ont pas déjà été récupérées, on les récupère
		if(MainUI.getCurrent().getEtudiant().getLibEtablissement()==null){
			etudiantController.recupererInscriptions();
		}

		//Test si la récupération des inscriptions via le WS s'est bien passée
		if(MainUI.getCurrent().isRecuperationWsInscriptionsOk()){
			
			//Tout c'est bien passé lors de la récupération des infos via le WS
			
			Panel panelInscription= new Panel(MainUI.getCurrent().getEtudiant().getLibEtablissement());


			Table inscriptionsTable = new Table(null, new BeanItemContainer<>(Inscription.class, MainUI.getCurrent().getEtudiant().getLinsciae()));
			inscriptionsTable.setWidth("100%");
			String[] colonnes = IA_FIELDS_ORDER;
			if(userController.isEtudiant()){
				colonnes = IA_FIELDS_ORDER_ETU;
			}
			inscriptionsTable.setVisibleColumns((Object[]) colonnes);
			for (String fieldName : colonnes) {
				inscriptionsTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
			}
			inscriptionsTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.lib_etp", null, getLocale()), new LibelleInscriptionColumnGenerator());

			//inscriptionsTable.setSortContainerPropertyId("cod_anu");
			inscriptionsTable.setColumnCollapsingAllowed(true);
			inscriptionsTable.setColumnReorderingAllowed(false);
			inscriptionsTable.setSelectable(false);
			inscriptionsTable.setImmediate(true);
			inscriptionsTable.setStyleName("noscrollabletable");
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
				inscriptionsDAC.setColumnReorderingAllowed(false);
				inscriptionsDAC.setSelectable(false);
				inscriptionsDAC.setImmediate(true);
				inscriptionsDAC.setStyleName("noscrollabletable");
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
		}else{
			//Il y a eu un soucis lors de la récupération des infos via le WS
			Panel panelErreurInscription= new Panel();
			Label labelMsgErreur = new Label(applicationContext.getMessage("AllView.erreur.message", null, getLocale()));
			panelErreurInscription.setContent(labelMsgErreur);
			addComponent(panelErreurInscription);
			
		}

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


	/** Formats the position in a column containing Date objects. */
	class LibelleInscriptionColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Inscription> bid = (BeanItem<Inscription>) item;
			Inscription inscription = (Inscription) bid.getBean();
			HorizontalLayout libelleLayout = new HorizontalLayout();

			//ajout du libellé de l'étape concernée par l'inscription
			Label lib_label = new Label(inscription.getLib_etp());
			libelleLayout.addComponent(lib_label);
			libelleLayout.setComponentAlignment(lib_label, Alignment.MIDDLE_CENTER);


			//Si c'est une inscription sur l'année en cours
			if(inscription.getCod_anu().substring(0, 4).equals(etudiantController.getAnneeUnivEnCours(MainUI.getCurrent()))){
				//On affiche le bouton pour voir de le détail de l'inscription
				Button bDetailInscription=new Button();
				bDetailInscription.setIcon(FontAwesome.SEARCH);
				bDetailInscription.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				bDetailInscription.setDescription(applicationContext.getMessage(NAME + ".inscriptionPedagogique.link", null, getLocale()));
				//Appel de la window contenant le détail des notes
				Etape etape = new Etape();
				etape.setAnnee(inscription.getCod_anu());
				etape.setCode(inscription.getCod_etp());
				etape.setVersion(inscription.getCod_vrs_vet());
				etape.setLibelle(inscription.getLib_etp());
				bDetailInscription.addClickListener(e->{
					DetailInscriptionWindow dnw = new DetailInscriptionWindow(etape); 
					UI.getCurrent().addWindow(dnw);
				});
				libelleLayout.addComponent(bDetailInscription);
			}


			//Si on peut proposer le certificat de scolarité
			if(etudiantController.proposerCertificat(inscription, MainUI.getCurrent().getEtudiant())){
				//On affiche le bouton pour éditer le certificat de scolarité
				Button bCertificatInscription=new Button();
				bCertificatInscription.setIcon(FontAwesome.FILE_TEXT);
				bCertificatInscription.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				bCertificatInscription.setDescription(applicationContext.getMessage(NAME + ".certificatScolarite.link", null, getLocale()));
				FileDownloader fd = new FileDownloader(inscriptionController.exportPdf(inscription));
				fd.extend(bCertificatInscription);
				libelleLayout.addComponent(bCertificatInscription);
			}




			return libelleLayout;
		}
	}



}
