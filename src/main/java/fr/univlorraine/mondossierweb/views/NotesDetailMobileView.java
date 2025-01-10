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

import com.vaadin.annotations.JavaScript;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.controllers.*;
import fr.univlorraine.mondossierweb.utils.CssUtils;
import fr.univlorraine.mondossierweb.views.windows.SignificationsMobileWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Page du détail des notes sur mobile
 */
@SuppressWarnings("serial")
@Component @Scope("prototype")
@SpringView(name = NotesDetailMobileView.NAME)
@JavaScript("notesDetailMobileView.js")
public class NotesDetailMobileView extends VerticalLayout implements View {

	private Logger LOG = LoggerFactory.getLogger(NotesDetailMobileView.class);

	public static final String NAME = "notesDetailMobileView";



	/* Injections */
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

	@Resource
	private transient ObjectFactory<SignificationsMobileWindow> significationsMobileWindowFactory;

	private Etape etape;

	private String codetu;

	int compteurElp;

	String elpPere;

	private Map<String,LinkedList<HorizontalLayout>> layoutList;


	private Button returnButton;

	private Button significationButton;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

	}

	@SuppressWarnings("deprecation")
	public void refresh(Etape etapeToDisplay, String codetuToDisplay){

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI && MdwTouchkitUI.getCurrent() !=null && MdwTouchkitUI.getCurrent().getEtudiant()!=null && 
			((userController.isEtudiant() && configController.isAffNotesEtudiant() && !MdwTouchkitUI.getCurrent().getEtudiant().isNonAutoriseConsultationNotes()) || 
				(userController.isEnseignant() && configController.isAffNotesEnseignant()) ||
				(userController.isGestionnaire() && configController.isAffNotesGestionnaire())) ){

			//On repassera dans la cration que si on n'a pas dejà créé la vue
			if(codetu==null || !codetuToDisplay.equals(codetu)){
				codetu=null;
			}
			//On repassera dans la cration que si on n'a pas dejà créé la vue
			if(etape == null || !etapeToDisplay.getAnnee().equals(etape.getAnnee()) || !etapeToDisplay.getCode().equals(etape.getCode()) || !etapeToDisplay.getVersion().equals(etape.getVersion())){
				etape=null;
			}
			//On repassera dans la création que si on n'a pas dejà créé la vue
			if(codetu==null || etape == null){
				compteurElp=0;

				removeAllComponents();

				/* Style */
				setMargin(false);
				setSpacing(false);
				setSizeFull();



				//Test si user enseignant
				if(userController.isEnseignant()){
					//On recupere les notes pour un enseignant
					resultatController.renseigneDetailNotesEtResultats(etapeToDisplay, userController.isGestionnaire());
				}else{
					//On récupère les notes pour un étudiant
					resultatController.renseigneDetailNotesEtResultatsEtudiant(etapeToDisplay);
				}

				//NAVBAR
				HorizontalLayout navbar=new HorizontalLayout();
				navbar.setSizeFull();
				navbar.setHeight(CssUtils.NAVBAR_HEIGHT);
				navbar.setStyleName("navigation-bar");

				//Bouton retour
				returnButton = new Button();
				returnButton.setIcon(FontAwesome.ARROW_LEFT);
				//returnButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
				returnButton.setStyleName("v-menu-nav-button");
				returnButton.addClickListener(e->{
					MdwTouchkitUI.getCurrent().navigateToResumeNotes();
				});
				navbar.addComponent(returnButton);
				navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);

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
						w.init(!configController.isIndentNiveauElpMobile());
						UI.getCurrent().addWindow(w);
					});
					navbar.addComponent(significationButton);
					navbar.setComponentAlignment(significationButton, Alignment.MIDDLE_RIGHT);
				}


				addComponent(navbar);


				layoutList = new  HashMap<String,LinkedList<HorizontalLayout>>();

				etape=etapeToDisplay;
				codetu=codetuToDisplay;

				/* Layout */
				VerticalLayout layout = new VerticalLayout();
				layout.setSizeFull();
				layout.setMargin(true);
				layout.setSpacing(true);
				layout.setStyleName("v-scrollableelement");

				/* Titre */
				//setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));


				List<ElementPedagogique> lelp = MdwTouchkitUI.getCurrent().getEtudiant().getElementsPedagogiques();



				if(lelp!=null && lelp.size()>0){
					VerticalLayout notesLayout = new VerticalLayout();

					HorizontalLayout libSessionLayout = new HorizontalLayout();
					libSessionLayout.setSizeFull();
					Label emptyLabel = new Label();
					libSessionLayout.addComponent(emptyLabel);
					libSessionLayout.setExpandRatio(emptyLabel, CssUtils.RATIO_LIB_NOTE);

					HorizontalLayout sessionLayout = new HorizontalLayout();
					sessionLayout.setSizeFull();
					Label session1 = new Label(applicationContext.getMessage(NAME+".table.elp.notesession1", null, getLocale()));
					if(noteController.renommerSession1(lelp) ) {
						session1.setValue(applicationContext.getMessage(NAME+".table.elp.notesession1bis", null, getLocale()));
					}
					session1.setStyleName("label-bold-with-bottom");
					sessionLayout.addComponent(session1);
					
					boolean afficherSession2 = noteController.afficherSession2(lelp);
					if(afficherSession2) {
						Label session2 = new Label(applicationContext.getMessage(NAME+".table.elp.notesession2", null, getLocale()));
						session2.setStyleName("label-bold-with-bottom");
						sessionLayout.addComponent(session2);
					}

					libSessionLayout.addComponent(sessionLayout);
					libSessionLayout.setExpandRatio(sessionLayout, CssUtils.RATIO_NOTES_NOTE);

					notesLayout.addComponent(libSessionLayout);

					boolean blueLevel = false;
					boolean arborescenceValide = true;

					compteurElp = 0;
					elpPere = "";

					HorizontalLayout layoutPere=null;
					int nbFils=0;

					LOG.info("Liste des ELP à afficher : ");
					for (ElementPedagogique elp : lelp) {

						compteurElp++;

						LOG.info(compteurElp+"-(level="+elp.getLevel()+") - "+elp.getCode()+" - "+elp.getLibelle());

						//Si on est sur un element de niveau 1, différent du premier element de la liste (qui est un rappel de l'etape)
						if(elp.getLevel()==1 && compteurElp>1){
							blueLevel = !blueLevel;
						}
						HorizontalLayout libElpLayout = new HorizontalLayout();

						//Le premier élément est ignoré car c'est un rappel de l'étape sélectionée
						if (compteurElp > 1) {
							if (elp.getLevel() == 1) {

								//Si l'ELP de niveau 1 précédent n'avait aucun fils
								if (layoutPere != null && nbFils == 0) {
									//C'est un élément de niveau 1 non cliquable
									//Fond blanc et une ligne en border-bottom
									layoutPere.setStyleName("layout-bottom-line-separator");
								}

								layoutPere=libElpLayout;
								nbFils=0;
								//Sur un elp de niveau 1, il est sur fond sombre
								libElpLayout.addStyleName("main-layout-bottom-line-separator");

								//ajout dans la hashMap
								layoutList.put(elp.getCode(), new LinkedList<HorizontalLayout>());
								elpPere = elp.getCode();

								libElpLayout.setId("layout_pere_"+elp.getCode());

							} else {
								libElpLayout.addStyleName("layout-bottom-line-separator");
								if(!StringUtils.hasText(elpPere)){
									// ANOMALIE , Arborescence non complete
									// On affichera toute la liste des ELP sans les indicateurs de niveau ni JS pour déplier
									libElpLayout.setId(compteurElp+"_"+elp.getCode()+"_layout_fils_");
									arborescenceValide=false;
								} else {
									nbFils++;
									libElpLayout.setId(compteurElp+"_"+elp.getCode()+"_layout_fils_"+elpPere);
									//ajout dans la hashMap
									layoutList.get(elpPere).add(libElpLayout);
								}

							}
						} else {
							//on affiche la racine (qui est un rappel de l'etape) en blanc sur un fond très sombre
							libElpLayout.addStyleName("root-layout-bottom-line-separator");
						}
						libElpLayout.setSizeFull();
						libElpLayout.setHeight("100%");

						VerticalLayout libVerticalLayout=new VerticalLayout();

						Label libElpLabel = new Label(elp.getLibelle());
						if(elp.isEpreuve()){
							libElpLabel.setStyleName("bold-italic-label");
						}else{
							libElpLabel.setStyleName("bold-label");
						}

						//Si on n'est pas sur le premier elp de la liste (rappel de l'étape) on affiche un indicateur de niveau
						if(arborescenceValide && compteurElp>1){
							if(!configController.isIndentNiveauElpMobile()){
								//Ajout des indicateurs dans le layout
								libVerticalLayout.addComponent(getLevelIndicator(elp.getLevel(), blueLevel));
							} else {
								// Ajout identation
								libElpLabel.addStyleName("indent-" + elp.getLevel() + "-em");
							}
						}

						//Ajout du libellé dans le layout
						libVerticalLayout.addComponent(libElpLabel);
						libElpLayout.addComponent(libVerticalLayout);
						libElpLayout.setExpandRatio(libVerticalLayout, CssUtils.RATIO_LIB_NOTE);

						HorizontalLayout noteLayout = new HorizontalLayout();
						noteLayout.setSizeFull();

						VerticalLayout vlsession1 = new VerticalLayout();
						Label note1 = new Label(elp.getNote1());
						if(!StringUtils.hasText(elp.getNote2())){
							if(elp.isEpreuve()){
								note1.setStyleName("bold-italic-label");
							}else{
								note1.setStyleName("bold-label");
							}
						}
						vlsession1.addComponent(note1);
						if(StringUtils.hasText(elp.getRes1())){
							Label adm1 = new Label(elp.getRes1());
							if(!StringUtils.hasText(elp.getRes2())){
								if(elp.isEpreuve()){
									adm1.setStyleName("bold-italic-label");
								}else{
									adm1.setStyleName("bold-label");
								}
							}
							vlsession1.addComponent(adm1);
						}
						noteLayout.addComponent(vlsession1);

						if(afficherSession2) {
							VerticalLayout vlsession2 = new VerticalLayout();
							Label note2 = new Label(elp.getNote2());
							if(StringUtils.hasText(elp.getNote2())){
								if(elp.isEpreuve()){
									note2.setStyleName("bold-italic-label");
								}else{
									note2.setStyleName("bold-label");
								}
							}
							vlsession2.addComponent(note2);
							if(StringUtils.hasText(elp.getRes2())){
								Label adm2 = new Label(elp.getRes2());
								if(StringUtils.hasText(elp.getRes2())){
									if(elp.isEpreuve()){
										adm2.setStyleName("bold-italic-label");
									}else{
										adm2.setStyleName("bold-label");
									}
								}
								vlsession2.addComponent(adm2);
							}
							noteLayout.addComponent(vlsession2);
						}
						
						VerticalLayout globalResultatLayout = new VerticalLayout();
						globalResultatLayout.setSizeFull();
						globalResultatLayout.setSpacing(false);
						globalResultatLayout.addComponent(noteLayout);
						// Si on doit afficher le rang
						if((configController.isAffRangEtudiant() || resultatController.isAfficherRangElpEpr()) && StringUtils.hasText(elp.getRang())){
							globalResultatLayout.addComponent(getRangComponent(elp.getRang()));
						}
						libElpLayout.addComponent(globalResultatLayout);
						libElpLayout.setExpandRatio(globalResultatLayout, CssUtils.RATIO_NOTES_NOTE);
						notesLayout.addComponent(libElpLayout);

						//Au départ, on cache les éléments de niveau supérieur à 1
						if(arborescenceValide && compteurElp>1 && elp.getLevel()>1){
							//libElpLayout.setVisible(false);
							Page.getCurrent().getJavaScript().execute("document.getElementById('"+libElpLayout.getId()+"').style.display=\"none\";");

						}
					}

					//Cas où le dernier élément était un élément le pere qui n'avait aucun fils
					if(layoutPere!=null && nbFils==0){
						layoutPere.setStyleName("layout-bottom-line-separator");
					}

					//Ajout du javascript
					if(arborescenceValide){
						for(Entry<String, LinkedList<HorizontalLayout>> entry : layoutList.entrySet()) {
							String pere = entry.getKey();
							LinkedList<HorizontalLayout> listeLayoutFils = entry.getValue();
							// traitements
							if(listeLayoutFils!=null && listeLayoutFils.size()>0){
								String affichagejavascriptfils = "";
								for(HorizontalLayout hl : listeLayoutFils){
									affichagejavascriptfils += "if(document.getElementById('"+hl.getId()+"').style.display==\"none\"){document.getElementById('"+hl.getId()+"').style.display = \"block\";}else{document.getElementById('"+hl.getId()+"').style.display = \"none\";}";
								}
								//sur le clic du layout pere, on affiche les fils
								Page.getCurrent().getJavaScript().execute("document.getElementById('"+"layout_pere_"+pere+"').onclick=function(){ "+affichagejavascriptfils+"};");
							}
						}
					}

					layout.addComponent(notesLayout);
					layout.setExpandRatio(notesLayout, 1);

				}else{
					setHeight(30, Unit.PERCENTAGE);
					HorizontalLayout messageLayout=new HorizontalLayout();
					messageLayout.setSpacing(true);
					messageLayout.setMargin(true);
					Label labelAucunResultat = new Label(applicationContext.getMessage(NAME+".message.aucuneresultat", null, getLocale()));
					labelAucunResultat.setStyleName(ValoTheme.LABEL_BOLD);
					messageLayout.addComponent(labelAucunResultat);
					layout.addComponent(messageLayout);
				}

				addComponent(layout);
				setExpandRatio(layout, 1);

			}else{
				refreshJavascript();
			}
		}
	}

	private com.vaadin.ui.Component getLevelIndicator(int niveau, boolean isBlue) {
		HorizontalLayout levelMainLayout = new HorizontalLayout();
		levelMainLayout.setSizeFull();
		levelMainLayout.setSpacing(true);
		levelMainLayout.setStyleName("level-indicator-layout");

		int k=0;
		for (int i = 0; i < niveau; i++) {
			//Ajout d'un level
			k++;
			Label libLevelLayout = new Label();
			libLevelLayout.setSizeFull();
			libLevelLayout.setHeight("8px");
			if(isBlue){
				libLevelLayout.setStyleName("layout-level-blue-indicator");
			}else{
				libLevelLayout.setStyleName("layout-level-green-indicator");
			}
			levelMainLayout.addComponent(libLevelLayout);
		}
		//On pense avoir 7 level maxi
		for (int j = k; j < 8; j++) {
			Label libLevelSpaceLayout = new Label();
			libLevelSpaceLayout.setSizeFull();
			libLevelSpaceLayout.setHeight("8px");
			levelMainLayout.addComponent(libLevelSpaceLayout);
		}
		return levelMainLayout;
	}


	private com.vaadin.ui.Component getRangComponent(String r) {
		HorizontalLayout rl = new HorizontalLayout();
		rl.setStyleName("layout-rang-mobile");
		rl.setWidth("100%");
		Label libelleRang = new Label(applicationContext.getMessage(NAME + ".rang", null, getLocale()));
		libelleRang.setStyleName("libelle-rang-mobile-detail");
		libelleRang.setWidth("100%");
		Label rang = new Label(r);
		rang.addStyleName("value-rang-mobile-detail");
		rang.setWidth("100%");
		rl.addComponent(libelleRang);
		rl.addComponent(rang);
		return rl;
	}
	
	@Override
	public void enter(ViewChangeEvent event) {

	}


	public void refreshJavascript() {

		//Ajout du javascript
		for(Entry<String, LinkedList<HorizontalLayout>> entry : layoutList.entrySet()) {
			String pere = entry.getKey();
			LinkedList<HorizontalLayout> listeLayoutFils = entry.getValue();
			// traitements
			if(listeLayoutFils!=null && listeLayoutFils.size()>0){
				String affichagejavascriptfils = "";
				for(HorizontalLayout hl : listeLayoutFils){
					//On masque par défaut tous les fils
					Page.getCurrent().getJavaScript().execute("document.getElementById('"+hl.getId()+"').style.display=\"none\";");
					//Creation du js pour modifier l'affichage au clic sur le pere
					affichagejavascriptfils += "if(document.getElementById('"+hl.getId()+"').style.display==\"none\"){document.getElementById('"+hl.getId()+"').style.display = \"block\";}else{document.getElementById('"+hl.getId()+"').style.display = \"none\";}";
				}
				//sur le clic du layout pere, on affiche les fils
				Page.getCurrent().getJavaScript().execute("document.getElementById('"+"layout_pere_"+pere+"').onclick=function(){ "+affichagejavascriptfils+"};");
			}
		}
	}



}
