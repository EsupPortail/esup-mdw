package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.views.windows.DetailInscriptionWindow;
import fr.univlorraine.mondossierweb.views.windows.ModificationAdressesWindow;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(AdressesView.NAME)
@StyleSheet("adressesView.css")
public class AdressesView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "adressesView";

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
		
		removeAllComponents();
		
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

		if(MainUI.getCurrent().getEtudiant().getAdresseAnnuelle()!=null){

			FormLayout formAdresseAnnuelleLayout = new FormLayout();
			formAdresseAnnuelleLayout.setSpacing(true);
			formAdresseAnnuelleLayout.setMargin(true);

			Panel panelAdresseAnnuelle= new Panel(applicationContext.getMessage(NAME+".adresseannuelle.title", null, getLocale())+" "+MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAnnee());

			String captionAdresseAnnuelle = applicationContext.getMessage(NAME+".adresse.title", null, getLocale());
			TextField fieldAdresseAnnuelle = new TextField(captionAdresseAnnuelle, MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresse1());
			formatTextField(fieldAdresseAnnuelle);
			formAdresseAnnuelleLayout.addComponent(fieldAdresseAnnuelle);

			String annuelle2 = valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresse2(),MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresse3());
			if(annuelle2!=null){
				TextField fieldAdresseAnnuelle2 = new TextField("", annuelle2);
				formatTextField(fieldAdresseAnnuelle2);
				formAdresseAnnuelleLayout.addComponent(fieldAdresseAnnuelle2);
			}
			String captionVilleAnnuelle = applicationContext.getMessage(NAME+".ville.title", null, getLocale());
			TextField fieldVilleAnnuelle = new TextField(captionVilleAnnuelle, valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresseetranger(),MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getCodePostal(),MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getVille()));
			formatTextField(fieldVilleAnnuelle);
			formAdresseAnnuelleLayout.addComponent(fieldVilleAnnuelle);

			String captionPaysAnnuelle = applicationContext.getMessage(NAME+".pays.title", null, getLocale());
			TextField fieldPaysAnnuelle = new TextField(captionPaysAnnuelle, MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getPays());
			formatTextField(fieldPaysAnnuelle);
			formAdresseAnnuelleLayout.addComponent(fieldPaysAnnuelle);

			String captionTelephoneAnnuelle = applicationContext.getMessage(NAME+".telephone.title", null, getLocale());
			TextField fieldTelephoneAnnuelle = new TextField(captionTelephoneAnnuelle, MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getNumerotel());
			formatTextField(fieldTelephoneAnnuelle);
			formAdresseAnnuelleLayout.addComponent(fieldTelephoneAnnuelle);

			panelAdresseAnnuelle.setContent(formAdresseAnnuelleLayout);

			globalLayout.addComponent(panelAdresseAnnuelle);
		}

		if(MainUI.getCurrent().getEtudiant().getAdresseFixe()!=null){
			FormLayout formAdresseFixeLayout = new FormLayout();
			formAdresseFixeLayout.setSpacing(true);
			formAdresseFixeLayout.setMargin(true);

			Panel panelAdresseFixe= new Panel(applicationContext.getMessage(NAME+".adressefixe.title", null, getLocale()));

			String captionAdresseFixe = applicationContext.getMessage(NAME+".adresse.title", null, getLocale());
			TextField fieldAdresseFixe = new TextField(captionAdresseFixe, MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresse1());
			formatTextField(fieldAdresseFixe);
			formAdresseFixeLayout.addComponent(fieldAdresseFixe);

			String adfixe2=valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresse2(),MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresse3());
			if(adfixe2!=null){
				TextField fieldAdresseFixe2 = new TextField("", adfixe2);
				formatTextField(fieldAdresseFixe2);
				formAdresseFixeLayout.addComponent(fieldAdresseFixe2);
			}

			String captionVilleFixe = applicationContext.getMessage(NAME+".ville.title", null, getLocale());
			TextField fieldVilleFixe = new TextField(captionVilleFixe, valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresseetranger(),MainUI.getCurrent().getEtudiant().getAdresseFixe().getCodePostal(),MainUI.getCurrent().getEtudiant().getAdresseFixe().getVille()));
			formatTextField(fieldVilleFixe);
			formAdresseFixeLayout.addComponent(fieldVilleFixe);

			String captionPaysFixe = applicationContext.getMessage(NAME+".pays.title", null, getLocale());
			TextField fieldPaysFixe = new TextField(captionPaysFixe, MainUI.getCurrent().getEtudiant().getAdresseFixe().getPays());
			formatTextField(fieldPaysFixe);
			formAdresseFixeLayout.addComponent(fieldPaysFixe);

			String captionTelephoneFixe = applicationContext.getMessage(NAME+".telephone.title", null, getLocale());
			TextField fieldTelephoneFixe = new TextField(captionTelephoneFixe, MainUI.getCurrent().getEtudiant().getAdresseFixe().getNumerotel());
			formatTextField(fieldTelephoneFixe);
			formAdresseFixeLayout.addComponent(fieldTelephoneFixe);

			panelAdresseFixe.setContent(formAdresseFixeLayout);

			globalLayout.addComponent(panelAdresseFixe);

		}

		addComponent(globalLayout);

		if(userController.isEtudiant() && PropertyUtils.isModificationAdressesAutorisee()){
			HorizontalLayout btnLayout = new HorizontalLayout();
			btnLayout.setSizeFull();
			btnLayout.setSpacing(true);
			
			Button btnModifAdresses = new Button (applicationContext.getMessage(NAME+".bouton.modifieradresses", null, getLocale()));
			btnModifAdresses.setStyleName(ValoTheme.BUTTON_PRIMARY);
			btnModifAdresses.setIcon(FontAwesome.EDIT);
			btnModifAdresses.addClickListener(e->{
				ModificationAdressesWindow maw = new ModificationAdressesWindow(MainUI.getCurrent().getEtudiant()); 
				maw.addCloseListener(f->{init();});
				UI.getCurrent().addWindow(maw);
			});
			btnLayout.addComponent(btnModifAdresses);
			btnLayout.setComponentAlignment(btnModifAdresses, Alignment.MIDDLE_LEFT);
			addComponent(btnLayout);
		}
	}

	private String valuateTextFieldFromMultipleValues(String string1, String string2) {
		if(string1 != null && string2 != null){
			return string1 + " "+string2;
		}
		if(string1!=null && string2 ==null){
			return string1;
		}
		if(string1==null && string2 !=null){
			return string2;
		}
		return null;
	}
	private String valuateTextFieldFromMultipleValues(String string1, String string2,String string3) {
		return valuateTextFieldFromMultipleValues(valuateTextFieldFromMultipleValues(string1, string2),string3);
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
