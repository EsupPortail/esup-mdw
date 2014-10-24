package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(InformationsAnnuellesView.NAME)
public class InformationsAnnuellesView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "informationsAnnuellesView";

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


		HorizontalLayout globalLayout = new HorizontalLayout();
		globalLayout.setSizeFull();
		globalLayout.setSpacing(true);

		Panel panelInfos= new Panel(applicationContext.getMessage(NAME+".infos.title", null, getLocale())+" "+Utils.getAnneeUniversitaireEnCours(etudiantController.getAnneeUnivEnCours()));

		FormLayout formInfosLayout = new FormLayout();
		formInfosLayout.setSpacing(true);
		formInfosLayout.setMargin(true);

		//Numéro Anonymat visible que si l'utilisateur est étudiant
		if(userController.isEtudiant()){
			String captionNumAnonymat = applicationContext.getMessage(NAME+".numanonymat.title", null, getLocale());
			TextField fieldNumAnonymat = new TextField(captionNumAnonymat, MainUI.getCurrent().getEtudiant().getNumAnonymat());
			formatTextField(fieldNumAnonymat);
			formInfosLayout.addComponent(fieldNumAnonymat);
		}

		String captionBousier = applicationContext.getMessage(NAME+".boursier.title", null, getLocale());
		TextField fieldNumBoursier = new TextField(captionBousier, MainUI.getCurrent().getEtudiant().getNumBoursier() == null ? "N" : "O");
		formatTextField(fieldNumBoursier);
		formInfosLayout.addComponent(fieldNumBoursier);

		String captionSalarie = applicationContext.getMessage(NAME+".salarie.title", null, getLocale());
		TextField fieldSalarie = new TextField(captionSalarie, MainUI.getCurrent().getEtudiant().isTemSalarie() == true ? "O" : "N");
		formatTextField(fieldSalarie);
		formInfosLayout.addComponent(fieldSalarie);

		String captionAmenagementEtude = applicationContext.getMessage(NAME+".amenagementetude.title", null, getLocale());
		TextField fieldAmenagementEtude = new TextField(captionAmenagementEtude, MainUI.getCurrent().getEtudiant().isTemAmenagementEtude()==true ? "O" : "N");
		formatTextField(fieldAmenagementEtude);
		formInfosLayout.addComponent(fieldAmenagementEtude);



		panelInfos.setContent(formInfosLayout);
		globalLayout.addComponent(panelInfos);
		globalLayout.addComponent(new VerticalLayout());
		addComponent(globalLayout);


	}

	private void formatTextField(TextField tf){
		tf.setEnabled(false);
		tf.setSizeFull();
		tf.setNullRepresentation("");
		tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
