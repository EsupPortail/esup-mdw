package fr.univlorraine.mondossierweb.views.windows;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
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

	/**
	 * Crée une fenêtre
	 */
	public DetailNotesMobileWindow(Etape et) {
		super();
		etape = et;
		init();
	}

	private void init() {

		/* Style */
		setWidth(95, Unit.PERCENTAGE);
		setHeight(95, Unit.PERCENTAGE);
		setModal(true);
		setResizable(false);


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
			notesLayout.setSpacing(true);
			
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
			
			for(ElementPedagogique elp : lelp){
				
				HorizontalLayout libElpLayout = new HorizontalLayout();
				libElpLayout.addStyleName("layout-bottom-line-separator");
				libElpLayout.setSizeFull();
				
				VerticalLayout libVerticalLayout=new VerticalLayout();
				Label libElpLabel = new Label(elp.getLibelle());
				libElpLabel.setStyleName(ValoTheme.LABEL_BOLD);
				libVerticalLayout.addComponent(libElpLabel);
				
				
				Label libLevelLayout = new Label();
				libLevelLayout.setHeight("8px");
				libLevelLayout.setStyleName("layout-level-indicator");
				libVerticalLayout.addComponent(libLevelLayout);
				libElpLayout.addComponent(libVerticalLayout);
				
				HorizontalLayout noteLayout = new HorizontalLayout();
				noteLayout.setSizeFull();
				
				VerticalLayout vlsession1 = new VerticalLayout();
				Label note1 = new Label(elp.getNote1());
				if(!StringUtils.hasText(elp.getNote2())){
					note1.setStyleName(ValoTheme.LABEL_BOLD);
				}
				vlsession1.addComponent(note1);
				if(StringUtils.hasText(elp.getRes1())){
					Label adm1 = new Label(elp.getRes1());
					if(!StringUtils.hasText(elp.getRes2())){
						adm1.setStyleName(ValoTheme.LABEL_BOLD);
					}
					vlsession1.addComponent(adm1);
				}
				noteLayout.addComponent(vlsession1);
				
				VerticalLayout vlsession2 = new VerticalLayout();
				Label note2 = new Label(elp.getNote2());
				if(StringUtils.hasText(elp.getNote2())){
					note2.setStyleName(ValoTheme.LABEL_BOLD);
				}
				vlsession2.addComponent(note2);
				if(StringUtils.hasText(elp.getRes2())){
					Label adm2 = new Label(elp.getRes2());
					if(StringUtils.hasText(elp.getRes2())){
						adm2.setStyleName(ValoTheme.LABEL_BOLD);
					}
					vlsession2.addComponent(adm2);
				}
				noteLayout.addComponent(vlsession2);
				
				libElpLayout.addComponent(noteLayout);
				
				notesLayout.addComponent(libElpLayout);
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
