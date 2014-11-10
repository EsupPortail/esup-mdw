package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Page de gestion des structures
 */
@Component @Scope("prototype")
@VaadinView(NotesView.NAME)
public class NotesView extends VerticalLayout implements View {
	private static final long serialVersionUID = -6491779626961549383L;

	public static final String NAME = "notesView";


	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		/* Style */
		setMargin(true);
		setSpacing(true);


		/* Titre */
		Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);

		//Si on n'a pas déjà essayer de récupérer les notes
		if(!MainUI.getCurrent().getEtudiant().isNotesRecuperees()){
			//Test si user enseignant
			if(userController.isEnseignant()){
				//On recupere les notes pour un enseignant
				etudiantController.renseigneNotesEtResultatsVueEnseignant(MainUI.getCurrent().getEtudiant());
			}else{
				//On récupère les notes pour un étudiant
				etudiantController.renseigneNotesEtResultats(MainUI.getCurrent().getEtudiant());
			}
		}

	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}





}
