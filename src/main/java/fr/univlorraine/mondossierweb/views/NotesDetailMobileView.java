package fr.univlorraine.mondossierweb.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.zookeeper.data.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.NoteController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.views.windows.SignificationsMobileWindow;

/**
 * Page du détail des notes sur mobile
 */
@Component @Scope("prototype")
@VaadinView(NotesDetailMobileView.NAME)
public class NotesDetailMobileView extends VerticalLayout implements View {

	private static final long serialVersionUID = 2295120253787356472L;

	private Logger LOG = LoggerFactory.getLogger(NotesDetailMobileView.class);

	public static final String NAME = "notesDetailMobileView";



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient NoteController noteController;
	@Resource
	private transient ConfigController configController;

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
		if((userController.isEnseignant() || userController.isEtudiant()) && MdwTouchkitUI.getCurrent() !=null && MdwTouchkitUI.getCurrent().getEtudiant()!=null){
			if(codetu==null || !codetuToDisplay.equals(codetu)){
				codetu=null;
			}
			//On va chercher les infos dans Apogée si ce n'est pas déjà fait
			if(etape == null || !etapeToDisplay.getAnnee().equals(etape.getAnnee()) || !etapeToDisplay.getCode().equals(etape.getCode()) || !etapeToDisplay.getVersion().equals(etape.getVersion())){
				etape=null;
			}
			if(codetu==null || etape == null){
				compteurElp=0;

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
				returnButton = new Button();
				returnButton.setIcon(FontAwesome.ARROW_LEFT);
				//returnButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
				returnButton.setStyleName("v-nav-button");
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
					significationButton.setStyleName("v-nav-button");
					significationButton.addClickListener(e->{
						//afficher les significations
						SignificationsMobileWindow w = new SignificationsMobileWindow(true);
						UI.getCurrent().addWindow(w);
					});
					navbar.addComponent(significationButton);
					navbar.setComponentAlignment(significationButton, Alignment.MIDDLE_RIGHT);
				}


				addComponent(navbar);


				layoutList = new  HashMap<String,LinkedList<HorizontalLayout>>();

				//Test si user enseignant
				if(userController.isEnseignant()){
					//On recupere les notes pour un enseignant
					etudiantController.renseigneDetailNotesEtResultatsEnseignant(etapeToDisplay);
				}else{
					//On récupère les notes pour un étudiant
					etudiantController.renseigneDetailNotesEtResultats(etapeToDisplay);
				}

				etape=etapeToDisplay;
				codetu=codetuToDisplay;

				/* Layout */
				VerticalLayout layout = new VerticalLayout();
				layout.setSizeFull();
				layout.setMargin(true);
				layout.setSpacing(true);
				layout.setStyleName("v-scrollableelement");

				/* Titre */
				setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));


				List<ElementPedagogique> lelp = MdwTouchkitUI.getCurrent().getEtudiant().getElementsPedagogiques();



				if(lelp!=null && lelp.size()>0){
					//Panel notesPanel = new Panel();
					//notesPanel.setSizeFull();

					VerticalLayout notesLayout = new VerticalLayout();
					//notesLayout.setSpacing(true);

					HorizontalLayout libSessionLayout = new HorizontalLayout();
					libSessionLayout.setSizeFull();
					libSessionLayout.addComponent(new Label());

					HorizontalLayout sessionLayout = new HorizontalLayout();
					sessionLayout.setSizeFull();
					Label session1 = new Label("Session1");
					session1.setStyleName("label-bold-with-bottom");
					sessionLayout.addComponent(session1);
					Label session2 = new Label("Session2");
					session2.setStyleName("label-bold-with-bottom");
					sessionLayout.addComponent(session2);

					libSessionLayout.addComponent(sessionLayout);

					notesLayout.addComponent(libSessionLayout);

					boolean blueLevel = false;

					compteurElp = 0;
					elpPere = "";

					HorizontalLayout layoutPere=null;
					int nbFils=0;
					
					for(ElementPedagogique elp : lelp){

						compteurElp++;

						//Si on est sur un element de niveau 1, différent du premier element de la liste (qui est un rappel de l'etape)
						if(elp.getLevel()==1 && compteurElp>1){
							blueLevel = !blueLevel;
						}
						HorizontalLayout libElpLayout = new HorizontalLayout();

						if(compteurElp>1){
							if(elp.getLevel()==1){
								
								//Si le pere précédent n'avait aucun fils
								if(layoutPere!=null && nbFils==0){
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

								/*libElpLayout.addListener(new LayoutClickListener() {
									public void layoutClick(LayoutClickEvent event) {
										if(layoutList.get(elp.getCode())==null || layoutList.get(elp.getCode()).size()==0){
											Notification.show(applicationContext.getMessage(NAME+".message.aucunsouselement", null, getLocale()));
										}else{
											//On parcourt les layout des éléments fils de l'élément cliqué
											for(HorizontalLayout hl : layoutList.get(elp.getCode())){
												//Si le layout es visible
												if(hl.isVisible()){
													//On masque le layout
													hl.setVisible(false);
												}else{
													//On affiche le layout
													hl.setVisible(true);
												}
											}
										}
									}
								});*/


								//Page.getCurrent().getJavaScript().execute("document.getElementById('"+"layout_pere_"+elp.getCode()+"').onclick=function(){ alert('hello');};");

							}else{
								nbFils++;
								libElpLayout.addStyleName("layout-bottom-line-separator");
								libElpLayout.setId(compteurElp+"_"+elp.getCode()+"_layout_fils_"+elpPere);
								//ajout dans la hashMap
								layoutList.get(elpPere).add(libElpLayout);

							}
						}else{
							//on affiche la racine (qui est un rappel de l'etape) en blanc sur un fond très sombre
							libElpLayout.addStyleName("root-layout-bottom-line-separator");
						}
						libElpLayout.setSizeFull();

						VerticalLayout libVerticalLayout=new VerticalLayout();
						
						Label libElpLabel = new Label(elp.getLibelle());
						libElpLabel.setStyleName("bold-label");
						libVerticalLayout.addComponent(libElpLabel);

						//Si on n'est pas sur le premier elp de la liste (rappel de l'étape) on affiche un indicateur de niveau
						if(compteurElp>1){
							HorizontalLayout levelMainLayout = new HorizontalLayout();
							levelMainLayout.setSizeFull();
							levelMainLayout.setSpacing(true);
							levelMainLayout.setStyleName("level-indicator-layout");

							int k=0;
							for(int i=0; i<elp.getLevel();i++){
								//Ajout d'un level
								k++;
								Label libLevelLayout = new Label();
								libLevelLayout.setSizeFull();
								libLevelLayout.setHeight("8px");
								if(blueLevel){
									libLevelLayout.setStyleName("layout-level-blue-indicator");
								}else{
									libLevelLayout.setStyleName("layout-level-green-indicator");
								}
								levelMainLayout.addComponent(libLevelLayout);
							}
							//On pense avoir 7 level maxi 
							for(int j=k; j<8;j++){
								Label libLevelSpaceLayout = new Label();
								libLevelSpaceLayout.setSizeFull();
								libLevelSpaceLayout.setHeight("8px");
								levelMainLayout.addComponent(libLevelSpaceLayout);
							}

							libVerticalLayout.addComponent(levelMainLayout);
						}
						libElpLayout.addComponent(libVerticalLayout);

						HorizontalLayout noteLayout = new HorizontalLayout();
						noteLayout.setSizeFull();

						VerticalLayout vlsession1 = new VerticalLayout();
						Label note1 = new Label(elp.getNote1());
						if(!StringUtils.hasText(elp.getNote2())){
							note1.setStyleName("bold-label");
						}
						vlsession1.addComponent(note1);
						if(StringUtils.hasText(elp.getRes1())){
							Label adm1 = new Label(elp.getRes1());
							if(!StringUtils.hasText(elp.getRes2())){
								adm1.setStyleName("bold-label");
							}
							vlsession1.addComponent(adm1);
						}
						noteLayout.addComponent(vlsession1);

						VerticalLayout vlsession2 = new VerticalLayout();
						Label note2 = new Label(elp.getNote2());
						if(StringUtils.hasText(elp.getNote2())){
							note2.setStyleName("bold-label");
						}
						vlsession2.addComponent(note2);
						if(StringUtils.hasText(elp.getRes2())){
							Label adm2 = new Label(elp.getRes2());
							if(StringUtils.hasText(elp.getRes2())){
								adm2.setStyleName("bold-label");
							}
							vlsession2.addComponent(adm2);
						}
						noteLayout.addComponent(vlsession2);

						libElpLayout.addComponent(noteLayout);

						notesLayout.addComponent(libElpLayout);

						//Au départ, on cache les éléments de niveau supérieur à 1
						if(compteurElp>1 && elp.getLevel()>1){
							//libElpLayout.setVisible(false);
							Page.getCurrent().getJavaScript().execute("document.getElementById('"+libElpLayout.getId()+"').style.display=\"none\";");
							
						}
					}

					//Cas où le dernier élément était un élément le pere qui n'avait aucun fils
					if(layoutPere!=null && nbFils==0){
						layoutPere.setStyleName("layout-bottom-line-separator");
					}

					//Ajout du javascript
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

			}
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}



}
