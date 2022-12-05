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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.Diplome;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Resultat;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.SignificationsMobileWindow;

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
			navbar.setHeight("40px");
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
			//globalLayout.setSizeFull();
			globalLayout.setSpacing(true);
			globalLayout.setMargin(true);
			globalLayout.setStyleName("v-scrollableelement");



			List<Diplome> ldiplomes = MdwTouchkitUI.getCurrent().getEtudiant().getDiplomes();
			if(ldiplomes!=null && ldiplomes.size()>0){
				Panel diplomesPanel = new Panel(applicationContext.getMessage(NAME+".table.diplomes", null, getLocale()));
				diplomesPanel.setStyleName("centertitle-panel");
				diplomesPanel.addStyleName("v-colored-panel-caption");
				VerticalLayout diplomesLayout=new VerticalLayout();
				for(Diplome diplome : ldiplomes){
					Panel panelEnCours=null;


					panelEnCours = new Panel(diplome.getAnnee());
					panelEnCours.setStyleName("v-panel-caption-centertitle-panel");

					HorizontalLayout noteLayout = new HorizontalLayout();
					noteLayout.setSizeFull();
					noteLayout.setSpacing(true);


					VerticalLayout libelleLayout = new VerticalLayout();
					libelleLayout.setSizeFull();

					Label libelleButton = new Label(diplome.getLib_web_vdi());
					libelleButton.setHeight("100%");
					libelleButton.setWidth("100%");
					libelleButton.addStyleName("label-centre");

					//Appel de la window contenant le détail des notes
					if(diplome.getResultats()!=null && diplome.getResultats().size()>0){
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

					noteLayout.addComponent(libelleLayout);
					
					globalResultatLayout.addComponent(notesessionLayout);
					//noteLayout.addComponent(notesessionLayout);
					// Si on doit afficher le rang
					if(MdwTouchkitUI.getCurrent().getEtudiant().isAfficherRang() &&
						diplome.isAfficherRang() && StringUtils.hasText(diplome.getRang())){
						globalResultatLayout.addComponent(getRangComponent(diplome.getRang()));
					}
					noteLayout.addComponent(globalResultatLayout);

					panelEnCours.setContent(noteLayout);
					diplomesLayout.addComponent(panelEnCours);
				}
				diplomesPanel.setContent(diplomesLayout);
				globalLayout.addComponent(diplomesPanel);
			}


			List<Etape> letapes=MdwTouchkitUI.getCurrent().getEtudiant().getEtapes();

			if(letapes!=null && letapes.size()>0){
				Panel elpsPanel = new Panel(applicationContext.getMessage(NAME+".table.etapes", null, getLocale()));
				elpsPanel.setStyleName("centertitle-panel");
				elpsPanel.addStyleName("v-colored-panel-caption");
				VerticalLayout elpsLayout=new VerticalLayout();

				for(Etape etape : letapes){
					Panel panelEnCours=null;


					panelEnCours = new Panel(etape.getAnnee());
					panelEnCours.setStyleName("v-panel-caption-centertitle-panel");

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
				elpsPanel.setContent(elpsLayout);
				globalLayout.addComponent(elpsPanel);

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


			/*
			 DetailNotesMobileWindow dnw = new DetailNotesMobileWindow(etape); 
			UI.getCurrent().addWindow(dnw);


			//Recuperer dans la base si l'utilisateur a demandé à ne plus afficher le message
			String val  = userController.getPreference(Utils.SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE);
			boolean afficherMessage = true;
			if(StringUtils.hasText(val)){
				afficherMessage = Boolean.valueOf(val);
			}

			if(afficherMessage){
				String message =applicationContext.getMessage(NAME+".window.message.info", null, getLocale());
				HelpMobileWindow hbw = new HelpMobileWindow(message,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()));
				hbw.addCloseListener(g->{
					boolean choix = hbw.getCheckBox().getValue();
					//Test si l'utilisateur a coché la case pour ne plus afficher le message
					if(choix){
						//mettre a jour dans la base de données
						userController.updatePreference(Utils.SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE, "false");
					}
				});
				UI.getCurrent().addWindow(hbw);
			}
			 */
		});
	}



}
