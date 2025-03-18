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
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.BeanItem;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.InscriptionController;
import fr.univlorraine.mondossierweb.controllers.SsoController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.MyFileDownloader;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.views.windows.DetailInscriptionWindow;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@SpringView(name = InscriptionsView.NAME)
@PreAuthorize("@userController.hasRoleInProperty('consultation_dossier')")
public class InscriptionsView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;
	
	private Logger LOG = LoggerFactory.getLogger(InscriptionsView.class);

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
	@Resource
	private transient SsoController ssoController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient ObjectFactory<DetailInscriptionWindow> detailInscriptionWindowFactory;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && (userController.isEnseignant() || userController.isEtudiant()) && MainUI.getCurrent()!=null && MainUI.getCurrent().getEtudiant()!=null){
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
				inscriptionsTable.setPageLength(inscriptionsTable.getItemIds().size());
				panelInscription.setContent(inscriptionsTable);
				globalLayout.addComponent(panelInscription);


				// Si on doit afficher les inscriptions dans d'autres cursus
				if (configController.isAffInscriptionsAutreCursus()) {
					Panel panelDAC = new Panel(applicationContext.getMessage(NAME + ".dac.title", null, getLocale()));

					if (MainUI.getCurrent().getEtudiant().getLinscdac() != null && MainUI.getCurrent().getEtudiant().getLinscdac().size() > 0) {
						Table inscriptionsDAC = new Table(null, new BeanItemContainer<>(Inscription.class, MainUI.getCurrent().getEtudiant().getLinscdac()));
						inscriptionsDAC.setWidth("100%");
						inscriptionsDAC.setVisibleColumns((Object[]) DAC_FIELDS_ORDER);
						for (String fieldName : DAC_FIELDS_ORDER) {
							inscriptionsDAC.setColumnHeader(fieldName, applicationContext.getMessage(NAME + ".tabledac." + fieldName, null, getLocale()));
						}
						inscriptionsDAC.setColumnCollapsingAllowed(true);
						inscriptionsDAC.setColumnReorderingAllowed(false);
						inscriptionsDAC.setSelectable(false);
						inscriptionsDAC.setImmediate(true);
						inscriptionsDAC.setStyleName("noscrollabletable");
						inscriptionsDAC.setPageLength(inscriptionsDAC.getItemIds().size());
						panelDAC.setContent(inscriptionsDAC);
					} else {
						HorizontalLayout labelDacLayout = new HorizontalLayout();
						labelDacLayout.setMargin(true);
						labelDacLayout.setSizeFull();
						Label aucuneDAC = new Label(applicationContext.getMessage(NAME + ".dac.aucune", null, getLocale()) + " " + MainUI.getCurrent().getEtudiant().getLibEtablissement());
						aucuneDAC.setStyleName(ValoTheme.LABEL_COLORED);
						aucuneDAC.addStyleName(ValoTheme.LABEL_BOLD);
						labelDacLayout.addComponent(aucuneDAC);
						panelDAC.setContent(labelDacLayout);
					}
					globalLayout.addComponent(panelDAC);
				}

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
	public void enter(ViewChangeListener.ViewChangeEvent event) {
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
			HorizontalLayout secondLineLayout = new HorizontalLayout();
			boolean secondLineEmpty = true;
			
			//ajout du libellé de l'étape concernée par l'inscription
			Label lib_label = new Label(inscription.getLib_etp());
			libelleLayout.addComponent(lib_label);
			libelleLayout.setComponentAlignment(lib_label, Alignment.MIDDLE_CENTER);
			libelleLayout.setHeight("2em");

			VerticalLayout twoLinesLayout = new VerticalLayout();
			twoLinesLayout.addComponent(libelleLayout);

			//Si c'est une inscription sur l'année en cours
			if(inscription.getCod_anu().substring(0, 4).equals(etudiantController.getAnneeUnivEnCours(MainUI.getCurrent()))){
				//On affiche le bouton pour voir de le détail de l'inscription
				Button bDetailInscription=new Button();
				bDetailInscription.setIcon(FontAwesome.SEARCH);
				bDetailInscription.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				bDetailInscription.setDescription(applicationContext.getMessage(NAME + ".inscriptionPedagogique.link", null, getLocale()));
				//Appel de la window contenant le détail de l'inscription
				Etape etape = new Etape();
				etape.setAnnee(inscription.getCod_anu());
				etape.setCode(inscription.getCod_etp());
				etape.setVersion(inscription.getCod_vrs_vet());
				etape.setLibelle(inscription.getLib_etp());
				bDetailInscription.addClickListener(e->{
					DetailInscriptionWindow dnw = detailInscriptionWindowFactory.getObject();
					dnw.init(etape); 
					UI.getCurrent().addWindow(dnw);
				});
				//ajout du bouton au layout
				libelleLayout.addComponent(bDetailInscription);
			}


			//Si on peut proposer le certificat de scolarité
			if(etudiantController.proposerCertificat(inscription, MainUI.getCurrent().getEtudiant(), false)){
				//On affiche le bouton pour éditer le certificat de scolarité
				Button bCertificatInscription=new Button();
				//bCertificatInscription.setIcon(FontAwesome.FILE_TEXT);
				bCertificatInscription.setIcon(FontAwesome.FILE_PDF_O);
				bCertificatInscription.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				bCertificatInscription.setDescription(applicationContext.getMessage(NAME + ".certificatScolarite.link", null, getLocale()));
				if(PropertyUtils.isPushEnabled()){
					MyFileDownloader fd = new MyFileDownloader(inscriptionController.exportPdf(inscription));
					fd.extend(bCertificatInscription);
				}else{
					FileDownloader fd = new FileDownloader(inscriptionController.exportPdf(inscription));
					fd.setOverrideContentType(false);
					fd.extend(bCertificatInscription);
				}
				//Si on n'affiche pas le bouton sur une nouvelle ligne
				if(!configController.isAffBtnCertifNouvelleLigne()){
					//ajout du bouton au layout du libellé
					libelleLayout.addComponent(bCertificatInscription);
				}else{
					//on adapte le style du bouton
					bCertificatInscription.setStyleName(ValoTheme.BUTTON_TINY);
					bCertificatInscription.setCaption(applicationContext.getMessage(NAME + ".certificatScolarite.btn.link", null, getLocale()));
					//ajout du bouton au layout secondLine
					secondLineLayout.addComponent(bCertificatInscription);
					secondLineEmpty=false;
				}
				if(configController.isAffBtnCertificatCouleur()) {
					bCertificatInscription.addStyleName("red-button-icon");
				}
			}		
			

			//Si on peut proposer l'attestation d'affiliation
			if(etudiantController.proposerAttestationAffiliationSSO(inscription, MainUI.getCurrent().getEtudiant())){
				//On affiche le bouton pour éditer l'attestation d'affiliation
				Button bAttestationAffiliationSso=new Button();
				bAttestationAffiliationSso.setIcon(FontAwesome.FILE_PDF_O);
				bAttestationAffiliationSso.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				bAttestationAffiliationSso.addStyleName("blue-button-icon");
				bAttestationAffiliationSso.setDescription(applicationContext.getMessage(NAME + ".affiliationSso.link", null, getLocale()));
				if(PropertyUtils.isPushEnabled()){
					MyFileDownloader fd = new MyFileDownloader(ssoController.exportAffiliationSsoPdf(MainUI.getCurrent().getEtudiant(),inscription));
					fd.extend(bAttestationAffiliationSso);
				}else{
					FileDownloader fd = new FileDownloader(ssoController.exportAffiliationSsoPdf(MainUI.getCurrent().getEtudiant(),inscription));
					fd.setOverrideContentType(false);
					fd.extend(bAttestationAffiliationSso);
				}
				//Si on n'affiche pas le bouton sur une nouvelle ligne
				if(!configController.isAffBtnAttestSsoNouvelleLigne()){
					//ajout du bouton au layout du libellé
					libelleLayout.addComponent(bAttestationAffiliationSso);
				}else{
					//on adapte le style du bouton
					bAttestationAffiliationSso.setStyleName(ValoTheme.BUTTON_TINY);
					bAttestationAffiliationSso.addStyleName("blue-button-icon");
					bAttestationAffiliationSso.setCaption(applicationContext.getMessage(NAME + ".affiliationSso.btn.link", null, getLocale()));
					//ajout du bouton au layout secondLine
					secondLineLayout.addComponent(bAttestationAffiliationSso);
					secondLineEmpty=false;
				}
			}
			
			//Si on peut proposer la quittance
			if(etudiantController.proposerQuittanceDroitsPayes(inscription, MainUI.getCurrent().getEtudiant())){
				//On affiche le bouton pour éditer la quittance
				Button bQuittanceSso=new Button();
				bQuittanceSso.setIcon(FontAwesome.FILE_PDF_O);
				bQuittanceSso.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				bQuittanceSso.setDescription(applicationContext.getMessage(NAME + ".quittance.link", null, getLocale()));
				if(PropertyUtils.isPushEnabled()){
					MyFileDownloader fd = new MyFileDownloader(ssoController.exportQuittancePdf(MainUI.getCurrent().getEtudiant(),inscription));
					fd.extend(bQuittanceSso);
				}else{
					FileDownloader fd = new FileDownloader(ssoController.exportQuittancePdf(MainUI.getCurrent().getEtudiant(),inscription));
					fd.setOverrideContentType(false);
					fd.extend(bQuittanceSso);
				}
				//Si on n'affiche pas le bouton sur une nouvelle ligne
				if(!configController.isAffBtnQuittanceDroitsPayesNouvelleLigne()){
					//ajout du bouton au layout du libellé
					libelleLayout.addComponent(bQuittanceSso);
				}else{
					//on adapte le style du bouton
					bQuittanceSso.setStyleName(ValoTheme.BUTTON_TINY);
					bQuittanceSso.setCaption(applicationContext.getMessage(NAME + ".quittance.btn.link", null, getLocale()));
					//ajout du bouton au layout secondLine
					secondLineLayout.addComponent(bQuittanceSso);
					secondLineEmpty=false;
				}
				if(configController.isAffBtnQuittanceCouleur()) {
					bQuittanceSso.addStyleName("green-button-icon");
				}
			}

			//Si le secondLineLayout contient des éléments
			if(!secondLineEmpty){
				secondLineLayout.setHeight("2em");
				//ajout du secondLineLayout au layout retourné
				twoLinesLayout.addComponent(secondLineLayout);
			}

			return twoLinesLayout;
		}
	}



}
