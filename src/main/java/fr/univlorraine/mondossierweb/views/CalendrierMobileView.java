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
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.CalendrierController;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;
import fr.univlorraine.mondossierweb.utils.CssUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Page de calendrier sur mobile
 */
@Component @Scope("prototype")
@SpringView(name = CalendrierMobileView.NAME)
public class CalendrierMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(CalendrierMobileView.class);

	public static final String NAME = "calendrierMobileView";



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

	private Button returnButton;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

	}
	public void refresh(){
		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI &&  MdwTouchkitUI.getCurrent() !=null && MdwTouchkitUI.getCurrent().getEtudiant()!=null &&
			((userController.isEtudiant() && configController.isAffCalendrierEpreuvesEtudiant()) || 
				(userController.isEnseignant() && configController.isAffCalendrierEpreuvesEnseignant()) ||
				(userController.isGestionnaire() && configController.isAffCalendrierEpreuvesGestionnaire()))){
			
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
				returnButton.setStyleName("v-menu-nav-button");
				returnButton.addClickListener(e->{
					if(MdwTouchkitUI.getCurrent().getDossierEtuFromView()!=null &&
							MdwTouchkitUI.getCurrent().getDossierEtuFromView().equals(ListeInscritsMobileView.NAME)){
						MdwTouchkitUI.getCurrent().navigateToListeInscrits();
					}

					if(MdwTouchkitUI.getCurrent().getDossierEtuFromView()!=null &&
							MdwTouchkitUI.getCurrent().getDossierEtuFromView().equals(RechercheMobileView.NAME)){
						MdwTouchkitUI.getCurrent().navigateToRecherche(null);
					}
				});
				navbar.addComponent(returnButton);
				navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);
			} else {
				Utils.ajoutLogoBandeau(configController.getLogoUniversiteMobile(), navbar);
			}

			//Title
			Label labelNavBar = new Label(MdwTouchkitUI.getCurrent().getEtudiant().getNom());
			labelNavBar.setStyleName("v-label-navbar");
			navbar.addComponent(labelNavBar);
			navbar.setComponentAlignment(labelNavBar, Alignment.MIDDLE_CENTER);

			navbar.setExpandRatio(labelNavBar, 1);
			addComponent(navbar);

			VerticalLayout globalLayout = new VerticalLayout();
			//globalLayout.setSizeFull();
			globalLayout.setSpacing(true);
			globalLayout.setMargin(true);
			globalLayout.setStyleName("v-scrollableelement");



			if(MdwTouchkitUI.getCurrent().getEtudiant()!=null && MdwTouchkitUI.getCurrent().getEtudiant().getCalendrier()!=null && MdwTouchkitUI.getCurrent().getEtudiant().getCalendrier().size()>0){

				List<Examen> listeExam = MdwTouchkitUI.getCurrent().getEtudiant().getCalendrier();

				for(Examen exam : listeExam){
					Panel panelCalendrier= new Panel();
					panelCalendrier.setSizeFull();
					HorizontalLayout labelExamenLayout = new HorizontalLayout();
					labelExamenLayout.setMargin(true);
					labelExamenLayout.setSpacing(true);
					labelExamenLayout.setSizeFull();

					//Détail de l'épreuve
					VerticalLayout detailLayout = new VerticalLayout();
					detailLayout.setSizeFull();
					//ajout de la date
					Label dateLabel = new Label(Utils.formatDateToString(exam.getDatedeb()));
					dateLabel.setStyleName(ValoTheme.LABEL_BOLD);
					detailLayout.addComponent(dateLabel);
					dateLabel.addStyleName("v-label-align-right");

					//ajout de l'heure
					Label heureLabel = new Label(exam.getHeure());
					detailLayout.addComponent(heureLabel);
					heureLabel.setStyleName("v-label-align-right");

					//ajout du batiment
					Label batimentLabel = new Label(exam.getBatiment());
					detailLayout.addComponent(batimentLabel);
					batimentLabel.setStyleName("v-label-align-right");
					
					//ajout de la salle
					Label salleLabel = new Label(exam.getSalle());
					if(StringUtils.hasText(exam.getLibsalle())) {
						salleLabel.setValue(exam.getSalle() + " ("+exam.getLibsalle()+")");
					}
					detailLayout.addComponent(salleLabel);
					salleLabel.setStyleName("v-label-align-right");
					salleLabel.addStyleName("v-small");
					
					//ajout de la place
					if(configController.isAffNumPlaceExamen() && StringUtils.hasText(exam.getPlace())){
						Label placeLabel = new Label(applicationContext.getMessage(NAME+".place", null, getLocale())+ " " +exam.getPlace());
						detailLayout.addComponent(placeLabel);
						placeLabel.setStyleName("v-label-align-right");
						placeLabel.addStyleName("v-small");
					}

					//Libelle de l'épreuve
					VerticalLayout libelleLayout = new VerticalLayout();
					libelleLayout.setSizeFull();
					Label libLabel = new Label(exam.getEpreuve());
					libLabel.setSizeFull();
					libLabel.setStyleName(ValoTheme.LABEL_BOLD);
					libelleLayout.addComponent(new Label(""));
					libelleLayout.addComponent(libLabel);


					//Ajout des 2 layouts dans le layout principal 
					labelExamenLayout.addComponent(detailLayout);
					labelExamenLayout.addComponent(libelleLayout);

					//Ajout du layout principal dans le panel
					panelCalendrier.setContent(labelExamenLayout);

					//Ajout du panel à la vue
					globalLayout.addComponent(panelCalendrier);


				}


			}else{
				Panel panelCalendrier= new Panel();
				panelCalendrier.setSizeFull();
				HorizontalLayout labelExamenLayout = new HorizontalLayout();
				labelExamenLayout.setMargin(true);
				labelExamenLayout.setSizeFull();
				Label aucunExamen = new Label(applicationContext.getMessage(NAME + ".examen.aucun", null, getLocale()));
				aucunExamen.setStyleName(ValoTheme.LABEL_COLORED);
				aucunExamen.addStyleName(ValoTheme.LABEL_BOLD);
				aucunExamen.setWidth("100%");
				aucunExamen.addStyleName("label-centre");
				labelExamenLayout.addComponent(aucunExamen);
				panelCalendrier.setContent(labelExamenLayout);
				globalLayout.addComponent(panelCalendrier);
			}



			addComponent(globalLayout);
			setExpandRatio(globalLayout, 1);
		}
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
	}





}
