package fr.univlorraine.mondossierweb.views;

import java.util.List;

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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.BacEtatCivil;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.views.windows.ModificationAdressesWindow;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(EtatCivilView.NAME)
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
	@Resource
	private transient ConfigController configController;


	private TextField fieldTelPortable;
	private TextField fieldMailPerso;
	private Button btnAnnulerModifCoordonneesPerso;
	private Button btnValidModifCoordonneesPerso;
	private Button btnModifCoordonneesPerso;
	private Panel panelContact;

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
		panelContact= new Panel(applicationContext.getMessage(NAME+".contact.title", null, getLocale()));

		renseignerPanelContact();

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

	private void renseignerPanelContact() {

		VerticalLayout contactLayout = new VerticalLayout();
		
		/* Layout pour afficher les erreurs */
		VerticalLayout erreursLayout = new VerticalLayout();
		contactLayout.addComponent(erreursLayout);
		erreursLayout.setVisible(false);
		
		/* Layout avec les champ 'Portable' et 'Email personnel' */
		FormLayout formContactLayout = new FormLayout();
		formContactLayout.setSpacing(true);
		formContactLayout.setMargin(true);

		String captionTelPortable = applicationContext.getMessage(NAME+".portable.title", null, getLocale());
		fieldTelPortable = new TextField(captionTelPortable, MainUI.getCurrent().getEtudiant().getTelPortable());
		formatTextField(fieldTelPortable);
		fieldTelPortable.setMaxLength(15);
		formContactLayout.addComponent(fieldTelPortable);

		if(userController.isEtudiant()){
			String captionMailPerso = applicationContext.getMessage(NAME+".mailperso.title", null, getLocale());
			fieldMailPerso = new TextField(captionMailPerso, MainUI.getCurrent().getEtudiant().getEmailPerso());
			formatTextField(fieldMailPerso);
			fieldMailPerso.setMaxLength(200);
			formContactLayout.addComponent(fieldMailPerso);
		}

		contactLayout.addComponent(formContactLayout);

		/* Si user étudiant et modifi autorisée des coordonnées de contact, on affiche les boutons de modification */
		if(userController.isEtudiant() && configController.isModificationCoordonneesPersoAutorisee()){
			//Layout pour les boutons de modification
			HorizontalLayout btnLayout = new HorizontalLayout();
			btnLayout.setSizeFull();
			btnLayout.setSpacing(true);
			btnLayout.setMargin(true);

			//Bouton pour valider la modification
			btnValidModifCoordonneesPerso = new Button(applicationContext.getMessage(NAME+".bouton.validercoordonnees", null, getLocale()));
			btnValidModifCoordonneesPerso.setStyleName(ValoTheme.BUTTON_FRIENDLY);
			btnValidModifCoordonneesPerso.setIcon(FontAwesome.CHECK);
			btnValidModifCoordonneesPerso.addClickListener(e -> {
				erreursLayout.removeAllComponents();
				List<String> retour = etudiantController.updateContact(fieldTelPortable.getValue(),fieldMailPerso.getValue(),MainUI.getCurrent().getEtudiant().getCod_etu());
				//si modif ok
				if(retour!=null && retour.size()==1 && retour.get(0).equals("OK")){
					etudiantController.recupererEtatCivil();
					renseignerPanelContact();
				}else{
					//affichage erreurs
					if(retour!=null && retour.size()>0){
						String errorMsg="";
						for(String erreur : retour){
							if(!errorMsg.equals(""))
								errorMsg = errorMsg + "<br />";
							errorMsg= errorMsg + erreur;
						}
						Label labelErreur = new Label(errorMsg);
						labelErreur.setContentMode(ContentMode.HTML);
						labelErreur.setStyleName(ValoTheme.LABEL_FAILURE);
						erreursLayout.addComponent(labelErreur);
					}
					erreursLayout.setVisible(true);
				}
			});
			btnValidModifCoordonneesPerso.setVisible(false);
			btnLayout.addComponent(btnValidModifCoordonneesPerso);
			btnLayout.setComponentAlignment(btnValidModifCoordonneesPerso, Alignment.MIDDLE_CENTER);

			//Bouton pour annuler la modification
			btnAnnulerModifCoordonneesPerso = new Button(applicationContext.getMessage(NAME+".bouton.annulercoordonnees", null, getLocale()));
			btnAnnulerModifCoordonneesPerso.setStyleName(ValoTheme.BUTTON_DANGER);
			btnAnnulerModifCoordonneesPerso.setIcon(FontAwesome.TIMES);
			btnAnnulerModifCoordonneesPerso.addClickListener(e -> {
				erreursLayout.removeAllComponents();
				fieldMailPerso.setValue(MainUI.getCurrent().getEtudiant().getEmailPerso());
				fieldMailPerso.setEnabled(false);
				fieldMailPerso.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
				fieldTelPortable.setValue(MainUI.getCurrent().getEtudiant().getTelPortable());
				fieldTelPortable.setEnabled(false);
				fieldTelPortable.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
				btnValidModifCoordonneesPerso.setVisible(false);
				btnAnnulerModifCoordonneesPerso.setVisible(false);
				btnModifCoordonneesPerso.setVisible(true);

			});
			btnAnnulerModifCoordonneesPerso.setVisible(false);
			btnLayout.addComponent(btnAnnulerModifCoordonneesPerso);
			btnLayout.setComponentAlignment(btnAnnulerModifCoordonneesPerso, Alignment.MIDDLE_CENTER);

			//Bouton pour activer la modification des données
			btnModifCoordonneesPerso = new Button (applicationContext.getMessage(NAME+".bouton.modifiercoordonnees", null, getLocale()));
			btnModifCoordonneesPerso.setStyleName(ValoTheme.BUTTON_PRIMARY);
			btnModifCoordonneesPerso.setIcon(FontAwesome.EDIT);
			btnModifCoordonneesPerso.addClickListener(e->{
				fieldMailPerso.setEnabled(true);
				fieldMailPerso.removeStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
				fieldTelPortable.setEnabled(true);
				fieldTelPortable.removeStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
				btnValidModifCoordonneesPerso.setVisible(true);
				btnAnnulerModifCoordonneesPerso.setVisible(true);
				btnModifCoordonneesPerso.setVisible(false);
			});
			btnLayout.addComponent(btnModifCoordonneesPerso);
			btnLayout.setComponentAlignment(btnModifCoordonneesPerso, Alignment.MIDDLE_CENTER);
			contactLayout.addComponent(btnLayout);
		}

		panelContact.setContent(contactLayout);

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
