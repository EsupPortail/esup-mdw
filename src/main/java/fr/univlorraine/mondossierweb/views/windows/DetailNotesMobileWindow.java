package fr.univlorraine.mondossierweb.views.windows;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import aj.org.objectweb.asm.Type;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.NoteController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Fenêtre du détail des notes sur mobile
 */
@Configurable(preConstruction=true)
public class DetailNotesMobileWindow extends Window {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "notesMobileWindow";



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

	int compteurElp;

	String elpPere;

	private Map<String,LinkedList<HorizontalLayout>> layoutList;

	/**
	 * Crée une fenêtre
	 */
	public DetailNotesMobileWindow(Etape et) {
		super();
		etape = et;
		compteurElp=0;
		init();
	}

	private void init() {

		/* Style */
		setWidth(95, Unit.PERCENTAGE);
		setHeight(95, Unit.PERCENTAGE);
		setModal(true);
		setResizable(false);

		layoutList = new  HashMap<String,LinkedList<HorizontalLayout>>();

		//Test si user enseignant
		if(userController.isEnseignant()){
			//On recupere les notes pour un enseignant
			etudiantController.renseigneDetailNotesEtResultatsEnseignant(etape);
		}else{
			//On récupère les notes pour un étudiant
			etudiantController.renseigneDetailNotesEtResultats(etape);
		}

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setMargin(true);
		layout.setSpacing(true);

		/* Titre */
		setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));


		List<ElementPedagogique> lelp = MdwTouchkitUI.getCurrent().getEtudiant().getElementsPedagogiques();



		if(lelp!=null && lelp.size()>0){
			Panel notesPanel = new Panel();
			notesPanel.setSizeFull();

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

			for(ElementPedagogique elp : lelp){

				compteurElp++;

				//Si on est sur un element de niveau 1, différent du premier element de la liste (qui est un rappel de l'etape)
				if(elp.getLevel()==1 && compteurElp>1){
					blueLevel = !blueLevel;
				}
				HorizontalLayout libElpLayout = new HorizontalLayout();
				

				if(compteurElp>1){
					if(elp.getLevel()==1){
						//Sur un elp de niveau 1, il est sur fond sombre
						libElpLayout.addStyleName("main-layout-bottom-line-separator");

						//ajout dans la hashMap
						layoutList.put(elp.getCode(), new LinkedList<HorizontalLayout>());
						elpPere = elp.getCode();
						
						libElpLayout.addListener(new LayoutClickListener() {
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
						});

					}else{
						libElpLayout.addStyleName("layout-bottom-line-separator");
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
					//On pense avoir 7 level maxi (donc 14 espaces de level necessaire au maxi).
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
					libElpLayout.setVisible(false);
				}
			}

			notesPanel.setContent(notesLayout);
			layout.addComponent(notesPanel);
			layout.setExpandRatio(notesPanel, 1);

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



		setContent(layout);


		/* Centre la fenêtre */
		center();



	}




}
