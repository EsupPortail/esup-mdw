package fr.univlorraine.mondossierweb.views;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.CalendrierController;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Examen;

/**
 * Page de calendrier sur mobile
 */
@Component @Scope("prototype")
@VaadinView(CalendrierMobileView.NAME)
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
		if((userController.isEnseignant() || userController.isEtudiant()) && MdwTouchkitUI.getCurrent() !=null && MdwTouchkitUI.getCurrent().getEtudiant()!=null){
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
					}

					if(MdwTouchkitUI.getCurrent().getDossierEtuFromView()!=null &&
							MdwTouchkitUI.getCurrent().getDossierEtuFromView().equals(RechercheMobileView.NAME)){
						MdwTouchkitUI.getCurrent().navigateToRecherche(null);
					}
				});
				navbar.addComponent(returnButton);
				navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);
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
					Label dateLabel = new Label(exam.getDatedeb());
					dateLabel.setStyleName(ValoTheme.LABEL_BOLD);
					detailLayout.addComponent(dateLabel);
					dateLabel.addStyleName("v-label-align-right");

					//ajout de l'heure
					Label heureLabel = new Label(exam.getHeure());
					detailLayout.addComponent(heureLabel);
					heureLabel.setStyleName("v-label-align-right");

					//ajout de la salle
					Label salleLabel = new Label(exam.getLibsalle());
					detailLayout.addComponent(salleLabel);
					salleLabel.setStyleName("v-label-align-right");

					//ajout du batiment
					Label batimentLabel = new Label(exam.getBatiment());
					detailLayout.addComponent(batimentLabel);
					batimentLabel.setStyleName("v-label-align-right");


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
	public void enter(ViewChangeEvent event) {
	}





}
