/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.views;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Anonymat;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Page d'accueil mobile de l'étudiant
 */
@Component @Scope("prototype")
@SpringView(name = InformationsAnnuellesMobileView.NAME)
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
			navbar.setHeight("40px");
			navbar.setStyleName("navigation-bar");

			//Bouton retour
			if(userController.isEnseignant()){
				returnButton = new Button();
				returnButton.setIcon(FontAwesome.ARROW_LEFT);
				//returnButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
				returnButton.setStyleName("v-nav-button");
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
					searchButton.setStyleName("v-nav-button");
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
				mailLayout.setHeight("25px");
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
				fotoEtudiant.setHeight("153px");
				fotoEtudiant.setStyleName(ValoTheme.BUTTON_LINK);
				photoLayout.addComponent(fotoEtudiant);


			}
			VerticalLayout nomCodeLayout = new VerticalLayout();
			nomCodeLayout.setSizeFull();
			nomCodeLayout.setSpacing(false);

			Label labelNomEtudiant = new Label(MdwTouchkitUI.getCurrent().getEtudiant().getNom());
			labelNomEtudiant.setSizeFull();
			labelNomEtudiant.setStyleName(ValoTheme.LABEL_BOLD);
			labelNomEtudiant.addStyleName("v-button-multiline");
			labelNomEtudiant.addStyleName("label-centre");
			nomCodeLayout.addComponent(labelNomEtudiant);
			nomCodeLayout.setComponentAlignment(labelNomEtudiant, Alignment.MIDDLE_CENTER);
			nomCodeLayout.setExpandRatio(labelNomEtudiant, 1);

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



			Panel panelInfos= new Panel(applicationContext.getMessage(NAME+".infos.title", null, getLocale())+" "+Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours(MdwTouchkitUI.getCurrent())));
			panelInfos.setStyleName("centertitle-panel");
			panelInfos.addStyleName("v-colored-panel-caption");

			//Si l'étudiant est inscrit pour l'année en cours
			if(MdwTouchkitUI.getCurrent().getEtudiant().isInscritPourAnneeEnCours()){

				FormLayout formInfosLayout = new FormLayout();
				formInfosLayout.setSpacing(true);
				formInfosLayout.setMargin(true);

				//Numéro Anonymat visible que si l'utilisateur est étudiant
				List<Anonymat> lano = null;
				if(!userController.isEnseignant() && userController.isEtudiant()){
					lano = MdwTouchkitUI.getCurrent().getEtudiant().getNumerosAnonymat();
					if(lano!=null) {
						//Si l'étudiant n'a qu'un seul numéro d'anonymat
						if(lano.size()==1){
							String captionNumAnonymat = applicationContext.getMessage(NAME+".numanonymat.title", null, getLocale());
							TextField fieldNumAnonymat = new TextField(captionNumAnonymat, MdwTouchkitUI.getCurrent().getEtudiant().getNumerosAnonymat().get(0).getCod_etu_ano());
							formatTextField(fieldNumAnonymat);
							//fieldNumAnonymat.setIcon(FontAwesome.INFO_CIRCLE);
							//fieldNumAnonymat.setDescription(applicationContext.getMessage(NAME+".numanonymat.description", null, getLocale()));
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
								if(i==0){
									//Pour le premier numéro affiché on affiche l'info bulle
									//fieldNumAnonymat.setIcon(FontAwesome.INFO_CIRCLE);
									//fieldNumAnonymat.setDescription(applicationContext.getMessage(NAME+".numanonymat.description", null, getLocale()));
								}
								formInfosLayout.addComponent(fieldNumAnonymat);
								i++;
							}
						}
					}
				}

				String captionBousier = applicationContext.getMessage(NAME+".boursier.title", null, getLocale());
				TextField fieldNumBoursier = new TextField(captionBousier, MdwTouchkitUI.getCurrent().getEtudiant().getNumBoursier() == null ? applicationContext.getMessage(NAME+".boursier.non", null, getLocale()) : applicationContext.getMessage(NAME+".boursier.oui", null, getLocale()));
				formatTextField(fieldNumBoursier);
				formInfosLayout.addComponent(fieldNumBoursier);

				String captionSalarie = applicationContext.getMessage(NAME+".salarie.title", null, getLocale());
				TextField fieldSalarie = new TextField(captionSalarie, MdwTouchkitUI.getCurrent().getEtudiant().isTemSalarie() == true ? applicationContext.getMessage(NAME+".salarie.oui", null, getLocale()) : applicationContext.getMessage(NAME+".salarie.non", null, getLocale()));
				formatTextField(fieldSalarie);
				formInfosLayout.addComponent(fieldSalarie);

				String captionAmenagementEtude = applicationContext.getMessage(NAME+".amenagementetude.title", null, getLocale());
				TextField fieldAmenagementEtude = new TextField(captionAmenagementEtude, MdwTouchkitUI.getCurrent().getEtudiant().isTemAmenagementEtude()==true ? applicationContext.getMessage(NAME+".amenagementetude.oui", null, getLocale()) : applicationContext.getMessage(NAME+".amenagementetude.non", null, getLocale()));
				formatTextField(fieldAmenagementEtude);
				formInfosLayout.addComponent(fieldAmenagementEtude);



				panelInfos.setContent(formInfosLayout);
			}else{

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
			globalLayout.addComponent(panelInfos);


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
