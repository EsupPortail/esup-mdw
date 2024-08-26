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
package fr.univlorraine.mondossierweb.views.windows;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.controllers.*;
import fr.univlorraine.mondossierweb.utils.MyFileDownloader;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Fenêtre du détail des notes
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DetailNotesWindow extends Window {

	public static final String NAME = "notesWindow";



	@Resource
	private transient ApplicationContext applicationContext;
	
	@Resource
	private transient UserController userController;
	
	@Resource
	private transient EtudiantController etudiantController;
	
	@Resource(name="${resultat.implementation}")
	private transient ResultatController resultatController;
	
	@Resource
	private transient NoteController noteController;
	
	@Resource
	private transient ConfigController configController;

	private Etape etape;
	
	private Button btnDisplayFiltres;
	
	private Panel panelVue;

	public void init(Etape et) {
		etape = et;

		/* Titre */
		setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && MainUI.getCurrent() != null && MainUI.getCurrent().getEtudiant() != null &&
			(((userController.isEtudiant() || ((isUserEnseignantWithAccess() || isUserGestionnaireWithAccess()) && !voirCommeEnseignant()))
					&& configController.isAffNotesEtudiant() && !MainUI.getCurrent().getEtudiant().isNonAutoriseConsultationNotes()) ||
					(isUserEnseignantWithAccess() && voirCommeEnseignant()) ||
					(isUserGestionnaireWithAccess() && voirCommeEnseignant()))  ){

			/* Style */
			setWidth(80, Unit.PERCENTAGE);
			setHeight(95, Unit.PERCENTAGE);
			setModal(true);
			setResizable(false);


			//Test si user enseignant et en vue Enseignant
			if(userController.isEnseignant() && voirCommeEnseignant()){
				//On recupere les notes pour un enseignant
				resultatController.renseigneDetailNotesEtResultats(etape, userController.isGestionnaire());
			}else{
				//On récupère les notes pour un étudiant
				resultatController.renseigneDetailNotesEtResultatsEtudiant(etape);
			}

			/* Layout */
			VerticalLayout layout = new VerticalLayout();
			layout.setSizeFull();
			layout.setMargin(true);
			layout.setSpacing(true);

			List<ElementPedagogique> lelp = MainUI.getCurrent().getEtudiant().getElementsPedagogiques();

			//Sous titre avec l'année
			HorizontalLayout titleLayout = new HorizontalLayout();
			titleLayout.setSizeFull();
			titleLayout.setHeight("20px");
			Label messageLabel = new Label(applicationContext.getMessage(NAME+".label.messageinfo", null, getLocale()));
			messageLabel.setContentMode(ContentMode.HTML);
			messageLabel.setStyleName(ValoTheme.LABEL_SMALL);
			titleLayout.addComponent(messageLabel);
			titleLayout.setExpandRatio(messageLabel, 1);
			titleLayout.setComponentAlignment(messageLabel, Alignment.MIDDLE_LEFT);

			ajouterVoirCommeUnEtudiant(titleLayout);

			
			if(lelp!=null && lelp.size()>0 && configController.isPdfNotesActive()){
				Button pdfButton = new Button();
				pdfButton.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				pdfButton.addStyleName("button-icon");
				pdfButton.addStyleName("red-button-icon");
				pdfButton.setIcon(FontAwesome.FILE_PDF_O);
				pdfButton.setDescription(applicationContext.getMessage(NAME + ".btn.pdf.description", null, getLocale()));
				
				
				if(PropertyUtils.isPushEnabled()){
					MyFileDownloader fd = new MyFileDownloader(noteController.exportPdfDetail(etape));
					fd.extend(pdfButton);
				}else{
					FileDownloader fd = new FileDownloader(noteController.exportPdfDetail(etape));
					fd.extend(pdfButton);
				}
				
				titleLayout.addComponent(pdfButton);
				titleLayout.setComponentAlignment(pdfButton, Alignment.MIDDLE_RIGHT);
			}
			layout.addComponent(titleLayout);

			ajouterPanelVoirCommeUnEtudiant(layout);

			Panel panelDetailNotes= new Panel(etape.getLibelle()+" - "+applicationContext.getMessage(NAME+".label.anneeuniv", null, getLocale())+" "+ etape.getAnnee());
			panelDetailNotes.addStyleName("small-font-element");
			panelDetailNotes.setSizeFull();


			if(lelp!=null && lelp.size()>0){
				Table detailNotesTable = new Table(null, new BeanItemContainer<>(ElementPedagogique.class, lelp));
				detailNotesTable.setSizeFull();
				detailNotesTable.setVisibleColumns(new String[0]);
				detailNotesTable.setColumnCollapsingAllowed(true);
				if(contientElpObtenusPrecedemment(lelp)){
				detailNotesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.annee", null, getLocale()), new AnneeColumnGenerator());
				}
				detailNotesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.code", null, getLocale()), new CodeElpColumnGenerator());
				detailNotesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.libelle", null, getLocale()), new LibelleElpColumnGenerator());
				detailNotesTable.addGeneratedColumn(noteController.renommerSession1(lelp) ? 
						applicationContext.getMessage(NAME+".table.elp.notesession1bis", null, getLocale()):
						applicationContext.getMessage(NAME+".table.elp.notesession1", null, getLocale())
						, new Session1ColumnGenerator());

				detailNotesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.resultatsession1", null, getLocale()), new ResultatSession1ColumnGenerator());
				
				if(noteController.afficherSession2(lelp)) {
					detailNotesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.notesession2", null, getLocale()), new Session2ColumnGenerator());
					detailNotesTable.addGeneratedColumn("resultatsession2", new ResultatSession2ColumnGenerator());
					detailNotesTable.setColumnHeader("resultatsession2", applicationContext.getMessage(NAME+".table.elp.resultatsession2", null, getLocale()));
				}
				if(configController.isAffRangEtudiant() || resultatController.isAfficherRangElpEpr()){
					detailNotesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.rang", null, getLocale()), new RangColumnGenerator());
				}
				if(configController.isAffECTSEtudiant()){
					detailNotesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.elp.ects", null, getLocale()), new ECTSColumnGenerator());
					if(configController.isMasqueECTSEtudiant()){
						detailNotesTable.setColumnCollapsed(applicationContext.getMessage(NAME+".table.elp.ects", null, getLocale()), true);
					}
				}
				detailNotesTable.setColumnReorderingAllowed(false);
				detailNotesTable.setSelectable(false);
				detailNotesTable.setImmediate(true);
				detailNotesTable.addStyleName("scrollabletable");
				panelDetailNotes.setContent(detailNotesTable);
			}else{
				setHeight(30, Unit.PERCENTAGE);
				HorizontalLayout messageLayout=new HorizontalLayout();
				messageLayout.setSpacing(true);
				messageLayout.setMargin(true);
				Label labelAucunResultat = new Label(applicationContext.getMessage(NAME+".message.aucuneresultat", null, getLocale()));
				labelAucunResultat.setStyleName(ValoTheme.LABEL_BOLD);
				messageLayout.addComponent(labelAucunResultat);
				panelDetailNotes.setContent(messageLayout);

			}
			layout.addComponent(panelDetailNotes);



			if(lelp!=null && lelp.size()>0 && MainUI.getCurrent().getEtudiant().isSignificationResultatsUtilisee()){
				Panel panelSignificationResultats= new Panel(applicationContext.getMessage(NAME+".info.significations.resultats", null, getLocale()));

				panelSignificationResultats.addStyleName("significationpanel");
				panelSignificationResultats.addStyleName("small-font-element");
				panelSignificationResultats.setIcon(FontAwesome.INFO_CIRCLE);

				VerticalLayout significationLayout = new VerticalLayout();
				significationLayout.setMargin(true);
				significationLayout.setSpacing(true);

				String grilleSignficationResultats = "";
				//grilleSignficationResultats = significationResultats.toString().substring(1,significationResultats.toString().length()-1);
				Set<String> ss = MainUI.getCurrent().getEtudiant().getSignificationResultats().keySet();
				for(String k : ss){
					if(k != null && !k.equals("") && !k.equals(" ")){
						grilleSignficationResultats = grilleSignficationResultats + "<b>"+k+"</b>&#160;:&#160;"+ MainUI.getCurrent().getEtudiant().getSignificationResultats().get(k);
						grilleSignficationResultats = grilleSignficationResultats + "&#160;&#160;&#160;";
					}
				}
				Label mapSignificationLabel=new Label(grilleSignficationResultats);
				mapSignificationLabel.setStyleName(ValoTheme.LABEL_SMALL);
				mapSignificationLabel.setContentMode(ContentMode.HTML);
				significationLayout.addComponent(mapSignificationLabel);

				panelSignificationResultats.setContent(significationLayout);
				layout.addComponent(panelSignificationResultats);

			}



			layout.setExpandRatio(panelDetailNotes, 1);


			setContent(layout);


			/* Centre la fenêtre */
			center();

		} else {
			// Acces non autorise
			VerticalLayout layout = new VerticalLayout();

			HorizontalLayout titleLayout = new HorizontalLayout();
			// Ajouter bloc "Voir comme un étudiant" si nécessaire
			ajouterVoirCommeUnEtudiant(titleLayout);
			ajouterPanelVoirCommeUnEtudiant(layout);
			layout.addComponent(titleLayout);

			//Si on est dans le cas d'un blocage apogée
			if(UI.getCurrent() instanceof MainUI
					&& MainUI.getCurrent()!=null && MainUI.getCurrent().getEtudiant()!=null
					&& (userController.isEtudiant() || ((isUserEnseignantWithAccess() || isUserGestionnaireWithAccess()) && !voirCommeEnseignant()))
					&& MainUI.getCurrent().getEtudiant().isNonAutoriseConsultationNotes()){
				//message non autorisé.
				HorizontalLayout refusLayout = new HorizontalLayout();
				refusLayout.setWidth("100%");
				Label msg = new Label(applicationContext.getMessage(NAME + ".blocage.msg", null, getLocale()));
				refusLayout.addComponent(msg);
				refusLayout.setComponentAlignment(msg,Alignment.MIDDLE_LEFT);
				layout.addComponent(refusLayout);

			}
			setContent(layout);
		}

	}

	private void ajouterPanelVoirCommeUnEtudiant(VerticalLayout layout) {
		//Test si user enseignant
		if(userController.isEnseignant()){
			panelVue= new Panel();

			HorizontalLayout vueLayout = new HorizontalLayout();
			vueLayout.setMargin(true);
			vueLayout.setSpacing(true);
			vueLayout.setSizeFull();

			Button changerVueButton = new Button(applicationContext.getMessage(NAME+".button.vueEnseignant", null, getLocale()));
			changerVueButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
			if(voirCommeEnseignant()){
				changerVueButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
				changerVueButton.setCaption(applicationContext.getMessage(NAME+".button.vueEtudiant", null, getLocale()));
			}
			//On change la variable vueEnseignantNotesEtResultats et on recréé la vue en cours
			changerVueButton.addClickListener(e -> {resultatController.changerVueNotesEtResultats();init(etape);});

			Label vueLabel=new Label(applicationContext.getMessage(NAME+".label.vueEtudiant", null, getLocale()));
			if(voirCommeEnseignant()){
				vueLabel.setValue(applicationContext.getMessage(NAME+".label.vueEnseignant", null, getLocale()));
			}
			vueLabel.setContentMode(ContentMode.HTML);
			vueLabel.setStyleName(ValoTheme.LABEL_SMALL);

			vueLayout.addComponent(changerVueButton);
			vueLayout.setComponentAlignment(changerVueButton, Alignment.MIDDLE_CENTER);
			vueLayout.addComponent(vueLabel);
			vueLayout.setExpandRatio(vueLabel, 1);

			panelVue.setContent(vueLayout);
			layout.addComponent(panelVue);
			panelVue.setVisible(false);
		}
	}

	private boolean voirCommeEnseignant() {
		return MainUI.getCurrent().isVueEnseignantNotesEtResultats();
	}

	private void ajouterVoirCommeUnEtudiant(HorizontalLayout titleLayout) {
		//Test si user enseignant
		if(userController.isEnseignant()){
			//Bouton pour afficher les filtres
			btnDisplayFiltres=new Button();
			btnDisplayFiltres.setWidth("52px");
			btnDisplayFiltres.setHeight("32px");
			btnDisplayFiltres.setStyleName(ValoTheme.BUTTON_PRIMARY);
			if(voirCommeEnseignant()){
				btnDisplayFiltres.setStyleName(ValoTheme.BUTTON_FRIENDLY);
			}
			btnDisplayFiltres.setIcon(FontAwesome.FILTER);
			btnDisplayFiltres.setDescription(applicationContext.getMessage(NAME+".btn.displayFilters", null, getLocale()));
			btnDisplayFiltres.addClickListener(e->{
				btnDisplayFiltres.setVisible(false);
				panelVue.setVisible(true);
			});
			titleLayout.addComponent(btnDisplayFiltres);
			titleLayout.setComponentAlignment(btnDisplayFiltres, Alignment.MIDDLE_RIGHT);
			//titleLayout.setExpandRatio(btnDisplayFiltres, 1);
			btnDisplayFiltres.setVisible(true);
		}
	}

	private boolean isUserGestionnaireWithAccess() {
		return userController.isGestionnaire() && configController.isAffNotesGestionnaire();
	}

	private boolean isUserEnseignantWithAccess() {
		return userController.isEnseignant() && configController.isAffNotesEnseignant();
	}

	private boolean contientElpObtenusPrecedemment(List<ElementPedagogique> lelp) {
		for(ElementPedagogique el : lelp){
			if(el.getLevel()>1 && !el.isEpreuve() && StringUtils.hasText(el.getAnnee())){
				return true;
			}
		}
		return false;
	}

	/** Formats the position in a column containing Date objects. */
	class AnneeColumnGenerator implements Table.ColumnGenerator {
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

			if((el.getLevel()==1 || !el.isEpreuve()) && StringUtils.hasText(el.getAnnee())){

				
				//indentation des libelles dans la liste:
				String annee = Utils.convertAnneeUnivToDisplay(el.getAnnee());

				if(el.getLevel()==1 && !el.isEpreuve()){
					annee="<b>"+annee+"</b>";
				}
				if(el.isEpreuve()){
					annee="<i>"+annee+"</i>";
				}
				libLabel.setValue(annee);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
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
	class Session1ColumnGenerator implements Table.ColumnGenerator {
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
				String note = el.getNote1();
				if(el.getBareme1()!=0 && (configController.isToujoursAfficherBareme() || el.getBareme1()!=20)){
					note += "/"+el.getBareme1();
				}
				if(el.getLevel()==1 && !el.isEpreuve()){
					note="<b>"+note+"</b>";
				}
				if(el.isEpreuve()){
					note="<i>"+note+"</i>";
				}
				libLabel.setValue(note);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class Session2ColumnGenerator implements Table.ColumnGenerator {
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
				String note = el.getNote2();
				if(el.getBareme2()!=0 && (configController.isToujoursAfficherBareme() || el.getBareme2()!=20)){
					note += "/"+el.getBareme2();
				}
				if(el.getLevel()==1 && !el.isEpreuve()){
					note="<b>"+note+"</b>";
				}
				if(el.isEpreuve()){
					note="<i>"+note+"</i>";
				}
				libLabel.setValue(note);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}


	/** Formats the position in a column containing Date objects. */
	class ResultatSession1ColumnGenerator implements Table.ColumnGenerator {
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

			if(StringUtils.hasText(el.getLibelle()) && el.getRes1()!=null){

				//indentation des libelles dans la liste:
				String res = el.getRes1();
				if(el.getLevel()==1 && !el.isEpreuve()){
					res="<b>"+res+"</b>";
				}
				if(el.isEpreuve()){
					res="<i>"+res+"</i>";
				}
				libLabel.setValue(res);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class ResultatSession2ColumnGenerator implements Table.ColumnGenerator {
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

			if(StringUtils.hasText(el.getLibelle()) && el.getRes2()!=null){

				//indentation des libelles dans la liste:
				String res = el.getRes2();
				if(el.getLevel()==1 && !el.isEpreuve()){
					res="<b>"+res+"</b>";
				}
				if(el.isEpreuve()){
					res="<i>"+res+"</i>";
				}
				libLabel.setValue(res);
			}
			libLabel.setContentMode(ContentMode.HTML);
			return libLabel;
		}
	}


	/** Formats the position in a column containing Date objects. */
	class RangColumnGenerator implements Table.ColumnGenerator {
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

			if(StringUtils.hasText(el.getRang())){
				libLabel.setValue(el.getRang());
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
