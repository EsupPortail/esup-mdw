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
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.InfosAnnuelles;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.InscriptionController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.utils.CssUtils;
import fr.univlorraine.mondossierweb.utils.MyFileDownloader;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Page d'accueil mobile de l'étudiant
 */
@Component @Scope("prototype")
@SpringView(name = InformationsAnnuellesMobileView.NAME)
@Slf4j
public class InformationsAnnuellesMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "informationsAnnuellesMobileView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient InscriptionController inscriptionController;



	private Button returnButton;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
	}

	public void refresh(){

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI && (userController.isEnseignant() || userController.isEtudiant()) && MdwTouchkitUI.getCurrent() !=null && MdwTouchkitUI.getCurrent().getEtudiant()!=null){
			removeAllComponents();

			/* Style */
			setMargin(false);
			setSpacing(false);
			setSizeFull();

			//NAVBAR
			HorizontalLayout navbar=new HorizontalLayout();
			navbar.setSizeFull();
			navbar.setHeight(CssUtils.NAVBAR_HEIGHT);
			navbar.setStyleName("navigation-bar");

			//Bouton retour
			if(userController.isEnseignant()){
				returnButton = new Button();
				returnButton.setIcon(FontAwesome.ARROW_LEFT);
				//returnButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
				returnButton.setStyleName("v-menu-nav-button");
				returnButton.addClickListener(e->{
					if(MdwTouchkitUI.getCurrent().getDossierEtuFromView()!=null &&
						MdwTouchkitUI.getCurrent().getDossierEtuFromView().equals(ListeInscritsMobileView.NAME)){
						MdwTouchkitUI.getCurrent().navigateToListeInscrits();
					}else{

						if(MdwTouchkitUI.getCurrent().getDossierEtuFromView()!=null &&
							MdwTouchkitUI.getCurrent().getDossierEtuFromView().equals(RechercheMobileView.NAME)){
							MdwTouchkitUI.getCurrent().navigateToRecherche(null);
						}
					}
				});
				navbar.addComponent(returnButton);
				navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);
			}

			//Title
			Label labelTrombi = new Label(MdwTouchkitUI.getCurrent().getEtudiant().getNom());
			labelTrombi.setStyleName("v-label-navbar");
			navbar.addComponent(labelTrombi);
			navbar.setComponentAlignment(labelTrombi, Alignment.MIDDLE_CENTER);

			if(userController.isEnseignant()){
				//Si on ne peut pas déjà revenir sur la recherche via le bouton 'retour'
				if(MdwTouchkitUI.getCurrent().getDossierEtuFromView()==null ||
					!MdwTouchkitUI.getCurrent().getDossierEtuFromView().equals(RechercheMobileView.NAME)){
					//Bouton Search
					Button searchButton = new Button();
					searchButton.setIcon(FontAwesome.SEARCH);
					searchButton.setStyleName("v-menu-nav-button");
					navbar.addComponent(searchButton);
					navbar.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
					searchButton.addClickListener(e->{
						((MdwTouchkitUI)MdwTouchkitUI.getCurrent()).navigateToRecherche(NAME);
					});
				}
			}

			navbar.setExpandRatio(labelTrombi, 1);
			addComponent(navbar);

			VerticalLayout globalLayout = new VerticalLayout();
			//globalLayout.setSizeFull();
			globalLayout.setSpacing(true);
			globalLayout.setMargin(true);
			globalLayout.setStyleName("v-scrollableelement");


			VerticalLayout slimLayout = new VerticalLayout();
			slimLayout.setSpacing(false);
			slimLayout.setMargin(false);
			//slimLayout.setStyleName("v-scrollableelement");

			String mail = MdwTouchkitUI.getCurrent().getEtudiant().getEmail();
			if(StringUtils.hasText(mail)){
				Panel mailPanel = new Panel();
				mailPanel.setStyleName("panel-without-bottom-line-separator");
				HorizontalLayout mailLayout = new HorizontalLayout();
				mailLayout.setSizeFull();
				mailLayout.setHeight("35px");
				Label mailLabel = new Label();

				mail = "<a href=\"mailto:"+mail+"\">"+mail+"</a>";
				mailLabel.setValue(mail);
				mailLabel.setContentMode(ContentMode.HTML);

				mailLabel.setSizeFull();
				mailLabel.addStyleName("label-centre");
				mailLayout.addComponent(mailLabel);
				mailLayout.setComponentAlignment(mailLabel, Alignment.MIDDLE_CENTER);
				mailPanel.setContent(mailLayout);
				slimLayout.addComponent(mailPanel);
				slimLayout.setComponentAlignment(mailPanel, Alignment.MIDDLE_CENTER);
			}

			Panel etuPanel = new Panel();
			HorizontalLayout photoLayout = new HorizontalLayout();
			photoLayout.setId(MdwTouchkitUI.getCurrent().getEtudiant().getCod_ind());
			photoLayout.setSizeFull();
			if(MdwTouchkitUI.getCurrent().getEtudiant().getPhoto()!=null){
				Image fotoEtudiant = new Image(null, new ExternalResource(MdwTouchkitUI.getCurrent().getEtudiant().getPhoto()));
				fotoEtudiant.setWidth("120px");
				fotoEtudiant.setStyleName(ValoTheme.BUTTON_LINK);
				photoLayout.addComponent(fotoEtudiant);


			}
			VerticalLayout nomCodeLayout = new VerticalLayout();
			//nomCodeLayout.setSizeFull();
			nomCodeLayout.setSpacing(false);

			Label labelNomEtudiant = new Label(MdwTouchkitUI.getCurrent().getEtudiant().getNom());
			labelNomEtudiant.setSizeFull();
			labelNomEtudiant.setStyleName(ValoTheme.LABEL_BOLD);
			labelNomEtudiant.addStyleName("label-centre");
			nomCodeLayout.addComponent(labelNomEtudiant);
			nomCodeLayout.setComponentAlignment(labelNomEtudiant, Alignment.MIDDLE_CENTER);
			//nomCodeLayout.setExpandRatio(labelNomEtudiant, 1);

			Label codetuLabel = new Label(MdwTouchkitUI.getCurrent().getEtudiant().getCod_etu());
			codetuLabel.setSizeFull();
			codetuLabel.setStyleName(ValoTheme.LABEL_TINY);
			codetuLabel.addStyleName("label-centre");
			nomCodeLayout.addComponent(codetuLabel);	
			nomCodeLayout.setComponentAlignment(codetuLabel, Alignment.MIDDLE_CENTER);

			photoLayout.addComponent(nomCodeLayout);
			photoLayout.setComponentAlignment(nomCodeLayout, Alignment.MIDDLE_CENTER);
			photoLayout.setExpandRatio(nomCodeLayout, 1);

			etuPanel.setContent(photoLayout);

			slimLayout.addComponent(etuPanel);
			slimLayout.setComponentAlignment(etuPanel, Alignment.MIDDLE_CENTER);




			globalLayout.addComponent(slimLayout);








			//Si l'étudiant est inscrit pour l'année en cours
			if(!MdwTouchkitUI.getCurrent().getEtudiant().isInscritPourAnneeEnCours()){
				//Etudiant non inscrit pour l'année en cours
				Panel panelInfos= new Panel(applicationContext.getMessage(NAME+".infos.title", null, getLocale())+" "+Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours(MdwTouchkitUI.getCurrent())));
				panelInfos.setStyleName("centertitle-panel");
				panelInfos.addStyleName("v-medium-panel-caption");

				HorizontalLayout labelNonInscritLayout = new HorizontalLayout();
				labelNonInscritLayout.setMargin(true);
				labelNonInscritLayout.setSizeFull();
				Label labelNonInscrit = new Label(applicationContext.getMessage(NAME + ".inscrit.non", null, getLocale()));
				labelNonInscrit.setStyleName(ValoTheme.LABEL_COLORED);
				labelNonInscrit.addStyleName(ValoTheme.LABEL_BOLD);
				labelNonInscrit.setWidth("100%");
				labelNonInscrit.addStyleName("label-centre");
				labelNonInscritLayout.addComponent(labelNonInscrit);
				panelInfos.setContent(labelNonInscritLayout);
			} 
			for(InfosAnnuelles infos : MdwTouchkitUI.getCurrent().getEtudiant().getInfosAnnuelles()) {
				
				Panel panelInfos= new Panel(applicationContext.getMessage(NAME+".infos.title", null, getLocale())+" "+infos.getLibelle());
				panelInfos.setStyleName("centertitle-panel");
				panelInfos.addStyleName("v-medium-panel-caption");

				FormLayout formInfosLayout = new FormLayout();
				formInfosLayout.setSpacing(true);
				formInfosLayout.setMargin(true);	

				//Si on affiche le certificat de scolarité sur mobile
				if(MdwTouchkitUI.getCurrent().getEtudiant().isInscritPourAnneeEnCours() 
					&& infos.isAnneeEnCours()
					&& configController.isCertificatScolaritePdfMobile()){

					//Si les informations sur les inscriptions n'ont pas déjà été récupérées, on les récupère
					if(MdwTouchkitUI.getCurrent().getEtudiant().getLibEtablissement()==null){
						etudiantController.recupererInscriptions();
					}
					//Si on a au moins une inscription
					if(MdwTouchkitUI.getCurrent().getEtudiant().getLinsciae() !=null &&
						!MdwTouchkitUI.getCurrent().getEtudiant().getLinsciae().isEmpty()){
						// Récupération de la première inscription de la liste
						Inscription inscription = MdwTouchkitUI.getCurrent().getEtudiant().getLinsciae().get(0);

						//Si on peut proposer le certificat de scolarité
						if(etudiantController.proposerCertificat(inscription, MdwTouchkitUI.getCurrent().getEtudiant(), true)){
							//On affiche le bouton pour éditer le certificat de scolarité
							Button bCertificatInscription=new Button();
							//bCertificatInscription.setIcon(FontAwesome.FILE_TEXT);
							bCertificatInscription.setIcon(FontAwesome.FILE_PDF_O);
							bCertificatInscription.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
							bCertificatInscription.addStyleName("red-button-icon");
							bCertificatInscription.setDescription(applicationContext.getMessage(NAME + ".certificatScolarite.link", null, getLocale()));
							if(PropertyUtils.isPushEnabled()){
								MyFileDownloader fd = new MyFileDownloader(inscriptionController.exportPdf(inscription));
								fd.extend(bCertificatInscription);
							}else{
								FileDownloader fd = new FileDownloader(inscriptionController.exportPdf(inscription));
								fd.setOverrideContentType(false);
								fd.extend(bCertificatInscription);
							}
							//ajout du bouton au layout
							HorizontalLayout hlc = new HorizontalLayout();
							hlc.addComponent(bCertificatInscription);
							hlc.setCaption(bCertificatInscription.getDescription());
							formInfosLayout.addComponent(hlc);
						}
					}
				}


				// Infos annuelles
				if(userController.isEtudiant() || 
					(userController.isEnseignant() && configController.isAffInfosAnnuellesEnseignant()) || 
					(userController.isGestionnaire() && configController.isAffInfosAnnuellesGestionnaire())){

					//Numéro Anonymat visible que si l'utilisateur est étudiant
					List<Anonymat> lano = null;
					if(userController.isEtudiant()){
						lano = infos.getNumerosAnonymat();
						if(lano!=null) {
							//Si l'étudiant n'a qu'un seul numéro d'anonymat
							if(lano.size()==1){
								String captionNumAnonymat = applicationContext.getMessage(NAME+".numanonymat.title", null, getLocale());
								TextField fieldNumAnonymat = new TextField(captionNumAnonymat, infos.getNumerosAnonymat().get(0).getCod_etu_ano());
								formatTextField(fieldNumAnonymat);
								formInfosLayout.addComponent(fieldNumAnonymat);
							}
							//Si l'étudiant a plusieurs numéros d'anonymat
							if(lano.size()>1){
								int i=0;
								for(Anonymat ano : lano){
									String captionNumAnonymat = "";
									if(i==0){
										//Pour le premier numéro affiché on affiche le libellé du champ
										captionNumAnonymat = applicationContext.getMessage(NAME+".numanonymats.title", null, getLocale());
									}
									TextField fieldNumAnonymat = new TextField(captionNumAnonymat, ano.getCod_etu_ano()+ " ("+ano.getLib_man()+")");
									formatTextField(fieldNumAnonymat);
									formInfosLayout.addComponent(fieldNumAnonymat);
									i++;
								}
							}
						}
					}

					if(userController.isEtudiant() || 
						(userController.isEnseignant() && configController.isAffBoursierEnseignant()) ||
						(userController.isGestionnaire() && configController.isAffBoursierGestionnaire())) {
						String captionBousier = applicationContext.getMessage(NAME+".boursier.title", null, getLocale());
						TextField fieldNumBoursier = new TextField(captionBousier, infos.isBoursier() ? applicationContext.getMessage(NAME+".boursier.oui", null, getLocale()) : applicationContext.getMessage(NAME+".boursier.non", null, getLocale()));
						formatTextField(fieldNumBoursier);
						formInfosLayout.addComponent(fieldNumBoursier);
					}
					if(userController.isEtudiant() || 
						(userController.isEnseignant() && configController.isAffSalarieEnseignant()) || 
						(userController.isGestionnaire() && configController.isAffSalarieGestionnaire())) {
						String captionSalarie = applicationContext.getMessage(NAME+".salarie.title", null, getLocale());
						TextField fieldSalarie = new TextField(captionSalarie, infos.isTemSalarie() == true ? applicationContext.getMessage(NAME+".salarie.oui", null, getLocale()) : applicationContext.getMessage(NAME+".salarie.non", null, getLocale()));
						formatTextField(fieldSalarie);
						formInfosLayout.addComponent(fieldSalarie);
					}
					if(userController.isEtudiant() || 
						(userController.isEnseignant() && configController.isAffAmenagementEnseignant()) ||
						(userController.isGestionnaire() && configController.isAffAmenagementGestionnaire())) {
						String captionAmenagementEtude = applicationContext.getMessage(NAME+".amenagementetude.title", null, getLocale());
						TextField fieldAmenagementEtude = new TextField(captionAmenagementEtude, infos.isTemAmenagementEtude()==true ? applicationContext.getMessage(NAME+".amenagementetude.oui", null, getLocale()) : applicationContext.getMessage(NAME+".amenagementetude.non", null, getLocale()));
						formatTextField(fieldAmenagementEtude);
						formInfosLayout.addComponent(fieldAmenagementEtude);
					}
				}

				panelInfos.setContent(formInfosLayout);



				//Si étudiant non inscrit ou si user étudiant ou si on a autorisé la visualisation des infos annuelles par l'enseignant
				if(!MdwTouchkitUI.getCurrent().getEtudiant().isInscritPourAnneeEnCours() || userController.isEtudiant() || configController.isAffInfosAnnuellesEnseignant()){
					globalLayout.addComponent(panelInfos);
				}
			}

			addComponent(globalLayout);
			setExpandRatio(globalLayout, 1);
		}
	}

	private void formatTextField(TextField tf){
		tf.setEnabled(false);
		tf.setSizeFull();
		tf.setNullRepresentation("");
		tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
		tf.addStyleName("bold-label");

	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
