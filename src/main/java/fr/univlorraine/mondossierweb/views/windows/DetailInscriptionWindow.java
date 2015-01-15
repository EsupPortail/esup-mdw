package fr.univlorraine.mondossierweb.views.windows;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Diplome;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.views.windows.DetailNotesWindow.ECTSColumnGenerator;

/**
 * Fenêtre du détail de l'inscription
 */
@Configurable(preConstruction=true)
public class DetailInscriptionWindow extends Window {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "inscriptionWindow";



	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient ConfigController configController;

	private Etape etape;

	/**
	 * Crée une fenêtre
	 */
	public DetailInscriptionWindow(Etape et) {
		super();
		etape = et;
		init();
	}

	private void init() {

		/* Style */
		setWidth(80, Unit.PERCENTAGE);
		setHeight(95, Unit.PERCENTAGE);
		setModal(true);
		setResizable(false);


		//Test si user enseignant et en vue Enseignant
		if(userController.isEnseignant() && MainUI.getCurrent().isVueEnseignantNotesEtResultats()){
			//On recupere les notes pour un enseignant
			etudiantController.renseigneDetailNotesEtResultatsEnseignant(etape);
		}else{
			//On récupère les notes pour un étudiant
			etudiantController.renseigneDetailNotesEtResultats(etape);
		}

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);

		/* Titre */
		setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));


		//Sous titre avec l'année
		HorizontalLayout titleLayout = new HorizontalLayout();
		titleLayout.setSizeFull();
		titleLayout.setHeight("20px");
		//Label labelSousMenu = new Label(applicationContext.getMessage(NAME+".sousmenu", null, getLocale()));
		//titleLayout.addComponent(labelSousMenu);
		Label labelAnneeUniv = new Label(applicationContext.getMessage(NAME+".label.anneeuniv", null, getLocale())+" <b>"+ etape.getAnnee()+"</b>");
		labelAnneeUniv.setContentMode(ContentMode.HTML);
		titleLayout.addComponent(labelAnneeUniv);
		titleLayout.setComponentAlignment(labelAnneeUniv, Alignment.MIDDLE_CENTER);
		layout.addComponent(titleLayout);



		Panel panelDetailInscription= new Panel(etape.getLibelle());
		panelDetailInscription.setSizeFull();

		List<ElementPedagogique> lelp = MainUI.getCurrent().getEtudiant().getElementsPedagogiques();
		if(lelp!=null && lelp.size()>0){
			Table detailInscriptionTable = new Table(null, new BeanItemContainer<>(ElementPedagogique.class, lelp));
			detailInscriptionTable.setSizeFull();
			detailInscriptionTable.setVisibleColumns(new String[0]);
			detailInscriptionTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.code", null, getLocale()), new CodeElpColumnGenerator());
			detailInscriptionTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.libelle", null, getLocale()), new LibelleElpColumnGenerator());
			if(configController.isAffECTSEtudiant()){
				detailInscriptionTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.ects", null, getLocale()), new ECTSColumnGenerator());
			}
			detailInscriptionTable.setColumnCollapsingAllowed(true);
			detailInscriptionTable.setColumnReorderingAllowed(false);
			detailInscriptionTable.setSelectable(false);
			detailInscriptionTable.setImmediate(true);
			detailInscriptionTable.addStyleName("scrollabletable");
			panelDetailInscription.setContent(detailInscriptionTable);
		}else{
			setHeight(30, Unit.PERCENTAGE);
			HorizontalLayout messageLayout=new HorizontalLayout();
			messageLayout.setSpacing(true);
			messageLayout.setMargin(true);
			Label labelAucuneIp=new Label(applicationContext.getMessage(NAME+".message.aucuneip", null, getLocale()));
			labelAucuneIp.setStyleName(ValoTheme.LABEL_BOLD);
			messageLayout.addComponent(labelAucuneIp);
			panelDetailInscription.setContent(messageLayout);
		}


		layout.addComponent(panelDetailInscription);



		layout.setExpandRatio(panelDetailInscription, 1);


		setContent(layout);


		/* Centre la fenêtre */
		center();


	}


	/** Formats the position in a column containing Date objects. */
	class CodeElpColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<ElementPedagogique> bid = (BeanItem<ElementPedagogique>) item;
			ElementPedagogique el = (ElementPedagogique) bid.getBean();
			Label libLabel = new Label();

			if(StringUtils.hasText(el.getLibelle())){

				//indentation des libelles dans la liste:
				String code = el.getCode();
				if(el.getLevel()==1 && !el.isEpreuve()){
					code="<b>"+code+"</b>";
				}
				if(el.isEpreuve()){
					code="<i>"+code+"</i>";
				}
				libLabel.setValue(code);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class LibelleElpColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<ElementPedagogique> bid = (BeanItem<ElementPedagogique>) item;
			ElementPedagogique el = (ElementPedagogique) bid.getBean();
			Label libLabel = new Label();

			if(StringUtils.hasText(el.getLibelle())){

				//indentation des libelles dans la liste:
				int rg = new Integer(el.getLevel());
				String libelp = el.getLibelle();
				String lib = "";
				for (int j = 2; j <= rg; j++) {
					lib= lib + "&#160;&#160;&#160;&#160;&#160;";
				}
				if(el.getLevel()==1 && !el.isEpreuve()){
					libelp="<b>"+libelp+"</b>";
				}
				if(el.isEpreuve()){
					libelp="<i>"+libelp+"</i>";
				}
				libLabel.setValue(lib+libelp);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}


	/** Formats the position in a column containing Date objects. */
	class ECTSColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<ElementPedagogique> bid = (BeanItem<ElementPedagogique>) item;
			ElementPedagogique el = (ElementPedagogique) bid.getBean();
			Label libLabel = new Label();

			if(StringUtils.hasText(el.getEcts())){
				libLabel.setValue(el.getEcts());
			}
			return libLabel;
		}
	}




}
