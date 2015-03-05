package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.views.windows.ModificationAdressesWindow;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(AdressesView.NAME)
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
	@Resource
	private transient ConfigController configController;


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


		VerticalLayout globalLayout = new VerticalLayout();
		globalLayout.setSizeFull();
		globalLayout.setSpacing(true);

		//Layout avec les infos etatcivil et contact
		CssLayout idLayout = new CssLayout();
		idLayout.setSizeFull();
		idLayout.setStyleName("flexwrap");

		globalLayout.addComponent(idLayout);
		// Enable Responsive CSS selectors for the layout
		Responsive.makeResponsive(idLayout);

		if(MainUI.getCurrent().getEtudiant().getAdresseAnnuelle()!=null){

			FormLayout formAdresseAnnuelleLayout = new FormLayout();
			formAdresseAnnuelleLayout.setSpacing(true);
			formAdresseAnnuelleLayout.setMargin(true);

			Panel panelAdresseAnnuelle= new Panel(applicationContext.getMessage(NAME+".adresseannuelle.title", null, getLocale())+" "+MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAnnee());

			String captionAdresseAnnuelle = applicationContext.getMessage(NAME+".adresse.title", null, getLocale());
			Label fieldAdresseAnnuelle = new Label();
			formatLabel(fieldAdresseAnnuelle, captionAdresseAnnuelle, MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresse1());
			formAdresseAnnuelleLayout.addComponent(fieldAdresseAnnuelle);

			String annuelle2 = valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresse2(),MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresse3());
			if(annuelle2!=null){
				Label fieldAdresseAnnuelle2 = new Label();
				formatLabel(fieldAdresseAnnuelle2, null, annuelle2);
				formAdresseAnnuelleLayout.addComponent(fieldAdresseAnnuelle2);
			}
			String captionVilleAnnuelle = applicationContext.getMessage(NAME+".ville.title", null, getLocale());
			Label fieldVilleAnnuelle = new Label();
			formatLabel(fieldVilleAnnuelle, captionVilleAnnuelle, valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAdresseetranger(),MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getCodePostal(),MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getVille()));
			formAdresseAnnuelleLayout.addComponent(fieldVilleAnnuelle);

			String captionPaysAnnuelle = applicationContext.getMessage(NAME+".pays.title", null, getLocale());
			Label fieldPaysAnnuelle = new Label();
			formatLabel(fieldPaysAnnuelle, captionPaysAnnuelle, MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getPays());
			formAdresseAnnuelleLayout.addComponent(fieldPaysAnnuelle);

			String captionTelephoneAnnuelle = applicationContext.getMessage(NAME+".telephone.title", null, getLocale());
			Label fieldTelephoneAnnuelle = new Label();
			formatLabel(fieldTelephoneAnnuelle, captionTelephoneAnnuelle, MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getNumerotel());
			formAdresseAnnuelleLayout.addComponent(fieldTelephoneAnnuelle);

			panelAdresseAnnuelle.setContent(formAdresseAnnuelleLayout);


			HorizontalLayout adresseAnnuelleGlobalLayout = new HorizontalLayout();
			adresseAnnuelleGlobalLayout.setSizeUndefined();
			adresseAnnuelleGlobalLayout.setStyleName("firstitembox");
			adresseAnnuelleGlobalLayout.addComponent(panelAdresseAnnuelle);
			adresseAnnuelleGlobalLayout.setExpandRatio(panelAdresseAnnuelle, 1);
			idLayout.addComponent(adresseAnnuelleGlobalLayout);
		}

		if(MainUI.getCurrent().getEtudiant().getAdresseFixe()!=null){
			FormLayout formAdresseFixeLayout = new FormLayout();
			formAdresseFixeLayout.setSpacing(true);
			formAdresseFixeLayout.setMargin(true);

			Panel panelAdresseFixe= new Panel(applicationContext.getMessage(NAME+".adressefixe.title", null, getLocale()));

			String captionAdresseFixe = applicationContext.getMessage(NAME+".adresse.title", null, getLocale());
			Label fieldAdresseFixe = new Label();
			formatLabel(fieldAdresseFixe, captionAdresseFixe, MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresse1());
			formAdresseFixeLayout.addComponent(fieldAdresseFixe);

			String adfixe2=valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresse2(),MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresse3());
			if(adfixe2!=null){
				Label fieldAdresseFixe2 = new Label();
				formatLabel(fieldAdresseFixe2, null, adfixe2);
				formAdresseFixeLayout.addComponent(fieldAdresseFixe2);
			}

			String captionVilleFixe = applicationContext.getMessage(NAME+".ville.title", null, getLocale());
			Label fieldVilleFixe = new Label();
			formatLabel(fieldVilleFixe, captionVilleFixe, valuateTextFieldFromMultipleValues(MainUI.getCurrent().getEtudiant().getAdresseFixe().getAdresseetranger(),MainUI.getCurrent().getEtudiant().getAdresseFixe().getCodePostal(),MainUI.getCurrent().getEtudiant().getAdresseFixe().getVille()));
			formAdresseFixeLayout.addComponent(fieldVilleFixe);

			String captionPaysFixe = applicationContext.getMessage(NAME+".pays.title", null, getLocale());
			Label fieldPaysFixe = new Label();
			formatLabel(fieldPaysFixe, captionPaysFixe, MainUI.getCurrent().getEtudiant().getAdresseFixe().getPays());
			formAdresseFixeLayout.addComponent(fieldPaysFixe);

			String captionTelephoneFixe = applicationContext.getMessage(NAME+".telephone.title", null, getLocale());
			Label fieldTelephoneFixe = new Label();
			formatLabel(fieldTelephoneFixe, captionTelephoneFixe, MainUI.getCurrent().getEtudiant().getAdresseFixe().getNumerotel());
			formAdresseFixeLayout.addComponent(fieldTelephoneFixe);

			panelAdresseFixe.setContent(formAdresseFixeLayout);


			HorizontalLayout adresseFixeGlobalLayout = new HorizontalLayout();
			adresseFixeGlobalLayout.setSizeUndefined();
			adresseFixeGlobalLayout.setStyleName("itembox");
			adresseFixeGlobalLayout.addComponent(panelAdresseFixe);
			adresseFixeGlobalLayout.setExpandRatio(panelAdresseFixe, 1);
			idLayout.addComponent(adresseFixeGlobalLayout);


		}

		addComponent(globalLayout);

		if(userController.isEtudiant() && configController.isModificationAdressesAutorisee()){
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

	private void formatLabel(Label label, String caption, String value){
		if(StringUtils.hasText(caption)){
			label.setCaption(caption);
		}
		if(StringUtils.hasText(value)){
			label.setValue("<b>"+ value +"</b");
			label.setContentMode(ContentMode.HTML);
		}
		label.setSizeFull();
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
