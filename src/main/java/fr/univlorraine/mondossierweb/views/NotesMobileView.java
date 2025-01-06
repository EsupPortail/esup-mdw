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

import com.vaadin.event.LayoutEvents;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.Diplome;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Resultat;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.CssUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.SignificationsMobileWindow;
import org.flywaydb.core.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Page des notes sur mobile
 */
@Component @Scope("prototype")
@SpringView(name = NotesMobileView.NAME)
public class NotesMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(NotesMobileView.class);

	public static final String NAME = "notesMobileView";



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
	private transient RechercheController rechercheController;
	@Resource
	private transient ObjectFactory<SignificationsMobileWindow> significationsMobileWindowFactory;

	private Button returnButton;

	private Button significationButton;

	private VerticalLayout diplomesLayout;
	private VerticalLayout elpsLayout;

	private HorizontalLayout showDiplomesLayout;

	private HorizontalLayout showEtapesLayout;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

	}
	public void refresh(){

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI && MdwTouchkitUI.getCurrent() !=null && MdwTouchkitUI.getCurrent().getEtudiant()!=null && 
				((userController.isEtudiant() && configController.isAffNotesEtudiant() && !MdwTouchkitUI.getCurrent().getEtudiant().isNonAutoriseConsultationNotes()) || 
					(userController.isEnseignant() && configController.isAffNotesEnseignant()) ||
					(userController.isGestionnaire() && configController.isAffNotesGestionnaire())) ){

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
					}

					if(MdwTouchkitUI.getCurrent().getDossierEtuFromView()!=null &&
							MdwTouchkitUI.getCurrent().getDossierEtuFromView().equals(RechercheMobileView.NAME)){
						MdwTouchkitUI.getCurrent().navigateToRecherche(null);
					}
				});
				navbar.addComponent(returnButton);
				navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);
			}

			//Titre
			Label labelNavBar = new Label(MdwTouchkitUI.getCurrent().getEtudiant().getNom());
			labelNavBar.setStyleName("v-label-navbar");
			navbar.addComponent(labelNavBar);
			navbar.setComponentAlignment(labelNavBar, Alignment.MIDDLE_CENTER);

			navbar.setExpandRatio(labelNavBar, 1);

			//Significations
			if(MdwTouchkitUI.getCurrent().getEtudiant().isSignificationResultatsUtilisee()){
				significationButton = new Button();
				significationButton.setIcon(FontAwesome.INFO_CIRCLE);
				significationButton.setStyleName("v-menu-nav-button");
				significationButton.addClickListener(e->{
					//afficher les significations
					SignificationsMobileWindow w = significationsMobileWindowFactory.getObject();
					w.init(false);
					UI.getCurrent().addWindow(w);
				});
				navbar.addComponent(significationButton);
				navbar.setComponentAlignment(significationButton, Alignment.MIDDLE_RIGHT);
			}


			addComponent(navbar);



			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setSpacing(true);
			globalLayout.setMargin(true);
			globalLayout.setStyleName("v-scrollableelement");

			diplomesLayout = new VerticalLayout();
			diplomesLayout.setSpacing(true);
			elpsLayout = new VerticalLayout();
			elpsLayout.setSpacing(true);
			showDiplomesLayout = createSelectButton(applicationContext.getMessage(NAME+".table.diplomes", null, getLocale()), true);
			showEtapesLayout = createSelectButton(applicationContext.getMessage(NAME+".table.etapes", null, getLocale()), false);
			HorizontalLayout selectLayout = new HorizontalLayout();
			selectLayout.setWidth("100%");
			selectLayout.setHeight("2em");
			selectLayout.addComponents(showDiplomesLayout, showEtapesLayout);
			globalLayout.addComponent(selectLayout);
			showDiplomesLayout.addListener(new LayoutEvents.LayoutClickListener() {
				public void layoutClick(LayoutEvents.LayoutClickEvent event) {
					showDiplomes();
				}
			});
			showEtapesLayout.addListener(new LayoutEvents.LayoutClickListener() {
				public void layoutClick(LayoutEvents.LayoutClickEvent event) {
					showElps();
				}
			});
			showDiplomes();

			List<Diplome> ldiplomes = MdwTouchkitUI.getCurrent().getEtudiant().getDiplomes();
			if(ldiplomes!=null && ldiplomes.size()>0){
				String anneeEnCours = null;
				Panel panelEnCours = null;
				VerticalLayout notesLayout = null;
				for(Diplome diplome : ldiplomes){

					if(panelEnCours == null || anneeEnCours == null || !anneeEnCours.equals(diplome.getAnnee())) {
						panelEnCours = new Panel();
						notesLayout = new VerticalLayout();
						diplomesLayout.addComponent(panelEnCours);
						panelEnCours.setCaption(applicationContext.getMessage(NAME+".annee", null, getLocale())+" " + diplome.getAnnee());
						anneeEnCours = diplome.getAnnee();
						panelEnCours.setStyleName("lefttitle-panel");
						panelEnCours.addStyleName("v-medium-panel-caption");
						panelEnCours.setContent(notesLayout);
					}

					HorizontalLayout diplomeLayout = new HorizontalLayout();
					diplomeLayout.setSizeFull();
					diplomeLayout.setSpacing(true);

					VerticalLayout libelleLayout = new VerticalLayout();
					libelleLayout.setSizeFull();

					Label libelleDiplome = new Label(diplome.getLib_web_vdi());
					libelleDiplome.setHeight("100%");
					libelleDiplome.setWidth("100%");
					libelleDiplome.setStyleName(ValoTheme.LABEL_SMALL);

					// Si le diplôme a des résultats
					if(diplome.getResultats() != null && diplome.getResultats().size() > 0){
						// on saute une ligne au niveau du libellé
						libelleLayout.addComponent(new Label(""));
					}
					libelleLayout.addComponent(libelleDiplome);

					VerticalLayout globalResultatLayout = new VerticalLayout();
					globalResultatLayout.setSizeFull();
					globalResultatLayout.setSpacing(false);

					HorizontalLayout notesessionLayout = new HorizontalLayout();
					notesessionLayout.setSizeFull();
					notesessionLayout.setSpacing(true);

					if(diplome.getResultats()!=null && diplome.getResultats().size()>0){
						int i=0;
						for(Resultat r : diplome.getResultats()){
							i++;
							VerticalLayout resultatLayout = new VerticalLayout();
							resultatLayout.setSizeFull();
							Label session = new Label(r.getSession());
							session.setStyleName("label-bold-with-bottom");
							resultatLayout.addComponent(session);
							Label note = new Label(r.getNote());
							resultatLayout.addComponent(note);
							Label resultat = new Label(r.getAdmission());
							resultatLayout.addComponent(resultat);
							//Si c'est la dernière session
							if(i==diplome.getResultats().size()){
								//On affiche les infos en gras
								note.setStyleName(ValoTheme.LABEL_BOLD);
								resultat.setStyleName(ValoTheme.LABEL_BOLD);
							}
							notesessionLayout.addComponent(resultatLayout);
						}
					}else{
						Label resultat = new Label("Aucun résultat");
						resultat.setStyleName(ValoTheme.LABEL_SMALL);
						notesessionLayout.addComponent(resultat);
					}

					diplomeLayout.addComponent(libelleLayout);
					
					globalResultatLayout.addComponent(notesessionLayout);
					//noteLayout.addComponent(notesessionLayout);
					// Si on doit afficher le rang
					if(MdwTouchkitUI.getCurrent().getEtudiant().isAfficherRang() &&
						diplome.isAfficherRang() && StringUtils.hasText(diplome.getRang())){
						globalResultatLayout.addComponent(getRangComponent(diplome.getRang()));
					}
					diplomeLayout.addComponent(globalResultatLayout);
					notesLayout.addComponent(diplomeLayout);
				}
				globalLayout.addComponent(this.diplomesLayout);
			}


			List<Etape> letapes=MdwTouchkitUI.getCurrent().getEtudiant().getEtapes();

			if(letapes!=null && letapes.size()>0){
				
				String anneeEnCours = null;
				for(Etape etape : letapes){
					Panel panelEnCours = new Panel();
					if(anneeEnCours == null || !anneeEnCours.equals(etape.getAnnee())) {
						panelEnCours.setCaption(applicationContext.getMessage(NAME+".annee", null, getLocale())+" " + etape.getAnnee());
						anneeEnCours = etape.getAnnee();
					}
					// panelEnCours.setStyleName("v-panel-caption-lefttitle-panel");
					panelEnCours.setStyleName("lefttitle-panel");
					panelEnCours.addStyleName("v-medium-panel-caption");

					HorizontalLayout noteLayout = new HorizontalLayout();
					noteLayout.setSizeFull();
					noteLayout.setSpacing(true);


					VerticalLayout libelleLayout = new VerticalLayout();
					libelleLayout.setSizeFull();

					Button libelleButton = new Button(etape.getLibelle());
					Utils.setButtonStyle(libelleButton);
					libelleButton.setHeight("100%");
					libelleButton.setWidth("100%");

					//Appel de la window contenant le détail des notes
					prepareBoutonAppelDetailDesNotes( libelleButton, etape);
					if(etape.getResultats()!=null && etape.getResultats().size()>0){
						libelleLayout.addComponent(new Label(""));
					}
					libelleLayout.addComponent(libelleButton);
					libelleLayout.setComponentAlignment(libelleButton, Alignment.MIDDLE_CENTER);

					VerticalLayout globalResultatLayout = new VerticalLayout();
					globalResultatLayout.setSizeFull();
					globalResultatLayout.setSpacing(false);

					HorizontalLayout notesessionLayout = new HorizontalLayout();
					notesessionLayout.setSizeFull();
					notesessionLayout.setSpacing(true);

					if(etape.getResultats()!=null && etape.getResultats().size()>0){
						int i=0;
						for(Resultat r : etape.getResultats()){
							i++;
							VerticalLayout resultatLayout = new VerticalLayout();
							resultatLayout.setSizeFull();
							Label session = new Label(r.getSession());
							session.setStyleName("label-bold-with-bottom");
							resultatLayout.addComponent(session);
							Label note = new Label(r.getNote());
							resultatLayout.addComponent(note);
							Label resultat = new Label(r.getAdmission());
							resultatLayout.addComponent(resultat);
							//Si c'est la dernière session
							if(i==etape.getResultats().size()){
								//On affiche les infos en gras
								note.setStyleName(ValoTheme.LABEL_BOLD);
								resultat.setStyleName(ValoTheme.LABEL_BOLD);
							}
							notesessionLayout.addComponent(resultatLayout);
						}
					}else{
						Label resultat = new Label("Aucun résultat");
						resultat.setStyleName(ValoTheme.LABEL_SMALL);
						notesessionLayout.addComponent(resultat);
					}

					noteLayout.addComponent(libelleLayout);
					
					globalResultatLayout.addComponent(notesessionLayout);
					//noteLayout.addComponent(notesessionLayout);
					// Si on doit afficher le rang
					if(MdwTouchkitUI.getCurrent().getEtudiant().isAfficherRang() &&
						etape.isAfficherRang() && StringUtils.hasText(etape.getRang())){
						globalResultatLayout.addComponent(getRangComponent(etape.getRang()));
					}
					noteLayout.addComponent(globalResultatLayout);

					panelEnCours.setContent(noteLayout);
					elpsLayout.addComponent(panelEnCours);
				}
				globalLayout.addComponent(this.elpsLayout);

			}



			addComponent(globalLayout);
			setExpandRatio(globalLayout, 1);
		}else{
			if(UI.getCurrent() instanceof MdwTouchkitUI 
					&& MdwTouchkitUI.getCurrent() !=null && MdwTouchkitUI.getCurrent().getEtudiant()!=null
					&& userController.isEtudiant() && MdwTouchkitUI.getCurrent().getEtudiant().isNonAutoriseConsultationNotes()){
				//message non autorisé.
				HorizontalLayout refusLayout = new HorizontalLayout();
				refusLayout.setWidth("100%");
				Label title = new Label(applicationContext.getMessage(NAME + ".blocage.msg", null, getLocale()));
				refusLayout.addComponent(title);
				refusLayout.setComponentAlignment(title,Alignment.MIDDLE_LEFT);
				addComponent(refusLayout);
				setExpandRatio(refusLayout, 1);
			}
			
		}
	}

	private HorizontalLayout createSelectButton(String message, boolean first) {
		HorizontalLayout l = new HorizontalLayout();
		l.setWidth("100%");
		Label label=new Label(message);
		label.setHeight("1.8em");
		l.addComponent(label);
		return l;
	}

	private void showElps() {
		diplomesLayout.setVisible(false);
		elpsLayout.setVisible(true);
		showEtapesLayout.setStyleName("layout-checked");
		showDiplomesLayout.setStyleName("layout-unchecked");
	}

	private void showDiplomes() {
		diplomesLayout.setVisible(true);
		elpsLayout.setVisible(false);
		showDiplomesLayout.setStyleName("layout-checked");
		showEtapesLayout.setStyleName("layout-unchecked");
	}

	private com.vaadin.ui.Component getRangComponent(String r) {
		HorizontalLayout rl = new HorizontalLayout();
		rl.setStyleName("layout-rang-mobile");
		rl.setWidth("100%");
		Label libelleRang = new Label(applicationContext.getMessage(NAME + ".rang", null, getLocale()));
		libelleRang.setStyleName("libelle-rang-mobile");
		libelleRang.setWidth("100%");
		Label rang = new Label(r);
		rang.addStyleName("value-rang-mobile");
		rang.setWidth("100%");
		rl.addComponent(libelleRang);
		rl.addComponent(rang);
		return rl;
	}
	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

	private void prepareBoutonAppelDetailDesNotes(Button b, Etape etape){
		//Appel de la window contenant le détail des notes
		b.addClickListener(e->{
			rechercheController.accessToMobileNotesDetail(etape);
		});
	}



}
