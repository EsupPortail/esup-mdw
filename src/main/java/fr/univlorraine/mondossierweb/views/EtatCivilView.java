package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.BacEtatCivil;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(EtatCivilView.NAME)
@StyleSheet("etatCivilView.css")
public class EtatCivilView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "etatCivilView";

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

		//Layout avec les infos etatcivil et contact
		VerticalLayout idLayout = new VerticalLayout();
		idLayout.setSizeFull();
		idLayout.setSpacing(true);

		/* Generalites */
		FormLayout formGeneralitesLayout = new FormLayout();
		formGeneralitesLayout.setSpacing(true);
		formGeneralitesLayout.setMargin(true);

		Panel panelGeneralites= new Panel(applicationContext.getMessage(NAME+".generalites.title", null, getLocale()));

		String captionNumDossier = applicationContext.getMessage(NAME+".numdossier.title", null, getLocale());
		TextField fieldNumDossier = new TextField(captionNumDossier, MainUI.getCurrent().getEtudiant().getCod_etu());
		formatTextField(fieldNumDossier);
		formGeneralitesLayout.addComponent(fieldNumDossier);

		String captionNNE = applicationContext.getMessage(NAME+".nne.title", null, getLocale());
		TextField fieldNNE = new TextField(captionNNE, MainUI.getCurrent().getEtudiant().getCod_nne());
		formatTextField(fieldNNE);
		formGeneralitesLayout.addComponent(fieldNNE);

		String captionNom = applicationContext.getMessage(NAME+".nom.title", null, getLocale());
		TextField fieldNom = new TextField(captionNom, MainUI.getCurrent().getEtudiant().getNom());
		formatTextField(fieldNom);
		formGeneralitesLayout.addComponent(fieldNom);

		String captionEmail = applicationContext.getMessage(NAME+".mail.title", null, getLocale());
		/*TextField fieldEmail = new TextField(captionEmail, MainUI.getCurrent().getEtudiant().getEmail());
		formatTextField(fieldEmail);
		formGeneralitesLayout.addComponent(fieldEmail);*/
		
		Label mailLabel = new Label();
		mailLabel.setCaption(captionEmail);
		String mail = MainUI.getCurrent().getEtudiant().getEmail();
		if(StringUtils.hasText(mail)){
			mail = "<a href=\"mailto:"+mail+"\">"+mail+"</a>";
			mailLabel.setValue(mail);
			mailLabel.setContentMode(ContentMode.HTML);
		}
		mailLabel.setSizeFull();
		formGeneralitesLayout.addComponent(mailLabel);
		
		
		
		

		String captionNationalite = applicationContext.getMessage(NAME+".nationalite.title", null, getLocale());
		TextField fieldNationalite = new TextField(captionNationalite, MainUI.getCurrent().getEtudiant().getNationalite());
		formatTextField(fieldNationalite);
		formGeneralitesLayout.addComponent(fieldNationalite);

		String captionDateNaissance = applicationContext.getMessage(NAME+".naissance.title", null, getLocale());
		TextField fieldDateNaissance = new TextField(captionDateNaissance, MainUI.getCurrent().getEtudiant().getDatenaissance());
		formatTextField(fieldDateNaissance);
		formGeneralitesLayout.addComponent(fieldDateNaissance);

		String captionLieuNaissance = applicationContext.getMessage(NAME+".lieunaissance.title", null, getLocale());
		TextField fieldLieuNaissance = new TextField(captionLieuNaissance, MainUI.getCurrent().getEtudiant().getLieunaissance());
		formatTextField(fieldLieuNaissance);
		formGeneralitesLayout.addComponent(fieldLieuNaissance);

		String captionDepNaissance = applicationContext.getMessage(NAME+".depnaissance.title", null, getLocale());
		TextField fieldDepNaissance = new TextField(captionDepNaissance, MainUI.getCurrent().getEtudiant().getDepartementnaissance());
		formatTextField(fieldDepNaissance);
		formGeneralitesLayout.addComponent(fieldDepNaissance);

		panelGeneralites.setContent(formGeneralitesLayout);

		idLayout.addComponent(panelGeneralites);




		/* Info de contact */
		FormLayout formContactLayout = new FormLayout();
		formContactLayout.setSpacing(true);
		formContactLayout.setMargin(true);

		Panel panelContact= new Panel(applicationContext.getMessage(NAME+".contact.title", null, getLocale()));

		String captionTelPortable = applicationContext.getMessage(NAME+".portable.title", null, getLocale());
		TextField fieldTelPortable = new TextField(captionTelPortable, MainUI.getCurrent().getEtudiant().getTelPortable());
		formatTextField(fieldTelPortable);
		formContactLayout.addComponent(fieldTelPortable);

		if(userController.isEtudiant()){
			String captionMailPerso = applicationContext.getMessage(NAME+".mailperso.title", null, getLocale());
			TextField fieldMailPerso = new TextField(captionMailPerso, MainUI.getCurrent().getEtudiant().getEmailPerso());
			formatTextField(fieldMailPerso);
			formContactLayout.addComponent(fieldMailPerso);
		}

		panelContact.setContent(formContactLayout);
		idLayout.addComponent(panelContact);


		globalLayout.addComponent(idLayout);




		HorizontalLayout bacLayout = new HorizontalLayout();
		bacLayout.setSizeFull();
		bacLayout.setSpacing(true);

		/* Bac */


		Panel panelBac= new Panel(applicationContext.getMessage(NAME+".bac.title", null, getLocale()));

		//Si plusieurs bac
		if(MainUI.getCurrent().getEtudiant().getListeBac()!=null && MainUI.getCurrent().getEtudiant().getListeBac().size()>1){
			panelBac.setCaption(applicationContext.getMessage(NAME+".bacs.title", null, getLocale()));
			TabSheet bacTabSheet = new TabSheet();
			VerticalLayout vBacLayout = new VerticalLayout();
			vBacLayout.setSizeFull();
			bacTabSheet.setSizeFull();
			bacTabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
			for(BacEtatCivil bec : MainUI.getCurrent().getEtudiant().getListeBac()){

				FormLayout tabBacLayout = new FormLayout();
				tabBacLayout.setSizeFull();
				tabBacLayout.setMargin(false);
				ajouterBacToView(tabBacLayout,bec);
				bacTabSheet.addTab(tabBacLayout, bec.getCod_bac(), FontAwesome.GRADUATION_CAP);

			}
			vBacLayout.addComponent(bacTabSheet);
			panelBac.setContent(vBacLayout);
		}else{
			//Si un seul bac
			FormLayout formBacLayout = new FormLayout();
			formBacLayout.setSizeFull();
			if(MainUI.getCurrent().getEtudiant().getListeBac()!=null && MainUI.getCurrent().getEtudiant().getListeBac().size()==1){
				formBacLayout.setSpacing(true);
				formBacLayout.setMargin(true);
				ajouterBacToView(formBacLayout,MainUI.getCurrent().getEtudiant().getListeBac().get(0));
			}
			panelBac.setContent(formBacLayout);
		}

		bacLayout.addComponent(panelBac);

		globalLayout.addComponent(bacLayout);

		addComponent(globalLayout);





	}

	private void formatTextField(TextField tf){
		tf.setEnabled(false);
		tf.setSizeFull();
		tf.setNullRepresentation("");
		tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
	}

	private void ajouterBacToView(FormLayout formBacLayout,BacEtatCivil bec){
		String captionBac = applicationContext.getMessage(NAME+".codebac.title", null, getLocale());
		TextField fieldBac = new TextField(captionBac, bec.getLib_bac());
		formatTextField(fieldBac);
		formBacLayout.addComponent(fieldBac);

		String captionAnneeBac = applicationContext.getMessage(NAME+".anneebac.title", null, getLocale());
		TextField fieldAnneeBac = new TextField(captionAnneeBac, bec.getDaa_obt_bac_iba());
		formatTextField(fieldAnneeBac);
		formBacLayout.addComponent(fieldAnneeBac);

		String captionMentionBac = applicationContext.getMessage(NAME+".mentionbac.title", null, getLocale());
		TextField fieldMentionBac = new TextField(captionMentionBac, bec.getCod_mnb());
		formatTextField(fieldMentionBac);
		formBacLayout.addComponent(fieldMentionBac);


		String captionTypeEtbBac = applicationContext.getMessage(NAME+".typeetbbac.title", null, getLocale());
		TextField fieldTypeEtbBac = new TextField(captionTypeEtbBac, bec.getCod_tpe());
		formatTextField(fieldTypeEtbBac);
		formBacLayout.addComponent(fieldTypeEtbBac);

		String captionEtbBac = applicationContext.getMessage(NAME+".etbbac.title", null, getLocale());
		TextField fieldEtbBac = new TextField(captionEtbBac, bec.getCod_etb());
		formatTextField(fieldEtbBac);
		formBacLayout.addComponent(fieldEtbBac);

		String captionDepBac = applicationContext.getMessage(NAME+".depbac.title", null, getLocale());
		TextField fieldDepBac = new TextField(captionDepBac, bec.getCod_dep());
		formatTextField(fieldDepBac);
		formBacLayout.addComponent(fieldDepBac);
	}
	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
