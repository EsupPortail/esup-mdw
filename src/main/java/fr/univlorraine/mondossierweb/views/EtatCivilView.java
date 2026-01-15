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


import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.BacEtatCivil;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.uicomponents.BasicErreurMessageLayout;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@SpringView(name = EtatCivilView.NAME)
@Slf4j
@PreAuthorize("@userController.hasRoleInProperty('consultation_dossier')")
public class EtatCivilView extends VerticalLayout implements View {
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

		log.debug(userController.getCurrentUserName()+" EtatCivilView");

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && (userController.isEnseignant() || userController.isEtudiant())){
			if( MainUI.getCurrent()!=null && MainUI.getCurrent().getEtudiant()!=null){
					log.debug(userController.getCurrentUserName()+" init EtatCivilView "+SecurityContextHolder.getContext().getAuthentication().getName());

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


					/* Generalites */
					FormLayout formGeneralitesLayout = new FormLayout();
					formGeneralitesLayout.setSpacing(true);
					formGeneralitesLayout.setMargin(true);

					Panel panelGeneralites= new Panel(applicationContext.getMessage(NAME+".generalites.title", null, getLocale()));

					String captionNumDossier = applicationContext.getMessage(NAME+".numdossier.title", null, getLocale());
					Label fieldNumDossier = new Label();
					formatLabel(fieldNumDossier, captionNumDossier, MainUI.getCurrent().getEtudiant().getCod_etu());
					formGeneralitesLayout.addComponent(fieldNumDossier);

					String captionNNE = applicationContext.getMessage(NAME+".nne.title", null, getLocale());
					Label fieldNNE = new Label();
					formatLabel(fieldNNE, captionNNE, MainUI.getCurrent().getEtudiant().getCod_nne());
					formGeneralitesLayout.addComponent(fieldNNE);

					String captionNom = applicationContext.getMessage(NAME+".nom.title", null, getLocale());
					Label fieldNom = new Label();
					formatLabel(fieldNom, captionNom, MainUI.getCurrent().getEtudiant().getNom());
					formGeneralitesLayout.addComponent(fieldNom);

					String captionMail = applicationContext.getMessage(NAME+".mail.title", null, getLocale());
					Label mailLabel = new Label();
					mailLabel.setCaption(captionMail);
					String mail = MainUI.getCurrent().getEtudiant().getEmail();
					if(StringUtils.hasText(mail)){
						mail = "<a href=\"mailto:"+mail+"\">"+mail+"</a>";
						mailLabel.setValue(mail);
						mailLabel.setContentMode(ContentMode.HTML);
					}
					mailLabel.setSizeFull();
					formGeneralitesLayout.addComponent(mailLabel);

					// Si on doit afficher les infos de naissance
					if(userController.isEtudiant()
							|| (userController.isEnseignant() && configController.isAffInfoNaissanceEnseignant())
							|| (userController.isGestionnaire() && configController.isAffInfoNaissanceGestionnaire())) {
						String captionNationalite = applicationContext.getMessage(NAME + ".nationalite.title", null, getLocale());
						Label fieldNationalite = new Label();
						formatLabel(fieldNationalite, captionNationalite, MainUI.getCurrent().getEtudiant().getNationalite());
						formGeneralitesLayout.addComponent(fieldNationalite);

						String captionDateNaissance = applicationContext.getMessage(NAME + ".naissance.title", null, getLocale());
						Label fieldDateNaissance = new Label();
						formatLabel(fieldDateNaissance, captionDateNaissance, MainUI.getCurrent().getEtudiant().getDatenaissance());
						formGeneralitesLayout.addComponent(fieldDateNaissance);

						String captionLieuNaissance = applicationContext.getMessage(NAME + ".lieunaissance.title", null, getLocale());
						Label fieldLieuNaissance = new Label();
						formatLabel(fieldLieuNaissance, captionLieuNaissance, MainUI.getCurrent().getEtudiant().getLieunaissance());
						formGeneralitesLayout.addComponent(fieldLieuNaissance);

						String captionDepNaissance = applicationContext.getMessage(NAME + ".depnaissance.title", null, getLocale());
						Label fieldDepNaissance = new Label();
						formatLabel(fieldDepNaissance, captionDepNaissance, MainUI.getCurrent().getEtudiant().getDepartementnaissance());
						formGeneralitesLayout.addComponent(fieldDepNaissance);
					}

					panelGeneralites.setContent(formGeneralitesLayout);


					HorizontalLayout generalitesGlobalLayout = new HorizontalLayout();
					generalitesGlobalLayout.setSizeUndefined();
					generalitesGlobalLayout.setStyleName("firstitembox");
					generalitesGlobalLayout.addComponent(panelGeneralites);
					generalitesGlobalLayout.setExpandRatio(panelGeneralites, 1);
					idLayout.addComponent(generalitesGlobalLayout);


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
						//Un seul bac
						FormLayout formBacLayout = new FormLayout();
						formBacLayout.setSizeFull();
						if(MainUI.getCurrent().getEtudiant().getListeBac()!=null && MainUI.getCurrent().getEtudiant().getListeBac().size()==1){
							formBacLayout.setSpacing(true);
							formBacLayout.setMargin(true);
							ajouterBacToView(formBacLayout,MainUI.getCurrent().getEtudiant().getListeBac().get(0));
						}
						panelBac.setContent(formBacLayout);
					}


					HorizontalLayout bacGlobalLayout = new HorizontalLayout();
					bacGlobalLayout.setSizeUndefined();
					bacGlobalLayout.setStyleName("itembox");
					bacGlobalLayout.addComponent(panelBac);
					bacGlobalLayout.setExpandRatio(panelBac, 1);
					idLayout.addComponent(bacGlobalLayout);


					/* Infos de contact */
					if(userController.isEtudiant() || 
						(userController.isEnseignant() && (configController.isAffContactTelEnseignant() || configController.isAffContactMailEnseignant())) ||
						(userController.isGestionnaire() && (configController.isAffContactTelGestionnaire() || configController.isAffContactMailGestionnaire()))){
						panelContact= new Panel(applicationContext.getMessage(NAME+".contact.title", null, getLocale()));
						renseignerPanelContact();
						globalLayout.addComponent(panelContact);
					}
					addComponent(globalLayout);

			}else{
				/* Erreur */
				addComponent(new BasicErreurMessageLayout(applicationContext));
			}
		}
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

		if(userController.isEtudiant()
				|| (userController.isGestionnaire() && configController.isAffContactTelGestionnaire())
				|| (userController.isEnseignant() && configController.isAffContactTelEnseignant())) {
			String captionTelPortable = applicationContext.getMessage(NAME + ".portable.title", null, getLocale());
			fieldTelPortable = new TextField(captionTelPortable, MainUI.getCurrent().getEtudiant().getTelPortable());
			formatTextField(fieldTelPortable);
			fieldTelPortable.setMaxLength(15);
			formContactLayout.addComponent(fieldTelPortable);
		}

		if(userController.isEtudiant()
				|| (userController.isGestionnaire() && configController.isAffContactMailGestionnaire())
				|| (userController.isEnseignant() && configController.isAffContactMailEnseignant())) {
			String captionMailPerso = applicationContext.getMessage(NAME+".mailperso.title", null, getLocale());
			fieldMailPerso = new TextField(captionMailPerso, MainUI.getCurrent().getEtudiant().getEmailPerso());
			formatTextField(fieldMailPerso);
			fieldMailPerso.setMaxLength(200);
			formContactLayout.addComponent(fieldMailPerso);
		}

		contactLayout.addComponent(formContactLayout);

		/* Si user étudiant , modifications autorisée des coordonnées de contact
		 * et si l'étudiant possède une addresse annuelle, on affiche les boutons de modification */
		if(userController.isEtudiant() && configController.isModificationCoordonneesPersoAutorisee()
				&& MainUI.getCurrent().getEtudiant().getAdresseAnnuelle()!=null){
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
				// si modif ok
				if(retour!=null && retour.size()==1 && retour.get(0).equals("OK")){
					etudiantController.recupererEtatCivil();
					renseignerPanelContact();
				}else{
					//affichage erreurs
					if (retour != null && !retour.isEmpty()) {
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

	private void ajouterBacToView(FormLayout formBacLayout,BacEtatCivil bec){
		String captionBac = applicationContext.getMessage(NAME+".codebac.title", null, getLocale());
		Label fieldBac = new Label();
		formatLabel(fieldBac, captionBac, bec.getLib_bac());
		formBacLayout.addComponent(fieldBac);
		
		if(bec.getLicSpeBacPre()!=null) {
			String captionSpePreBac = applicationContext.getMessage(NAME+".speprebac.title", null, getLocale());
			Label fieldSpePreBac = new Label();
			formatLabel(fieldSpePreBac, captionSpePreBac, bec.getLicSpeBacPre());
			formBacLayout.addComponent(fieldSpePreBac);
		}
		if(bec.getLicSpe1Bac()!=null) {
			String captionSpe1Bac = applicationContext.getMessage(NAME+".spe1bac.title", null, getLocale());
			Label fieldSpe1Bac = new Label();
			formatLabel(fieldSpe1Bac, captionSpe1Bac, bec.getLicSpe1Bac());
			formBacLayout.addComponent(fieldSpe1Bac);
		}
		if(bec.getLicSpe2Bac()!=null) {
			String captionSpe2Bac = applicationContext.getMessage(NAME+".spe2bac.title", null, getLocale());
			Label fieldSpe2Bac = new Label();
			formatLabel(fieldSpe2Bac, captionSpe2Bac, bec.getLicSpe2Bac());
			formBacLayout.addComponent(fieldSpe2Bac);
		}
		if(bec.getLicOpt1Bac()!=null) {
			String captionOpt1Bac = applicationContext.getMessage(NAME+".opt1bac.title", null, getLocale());
			Label fieldOpt1Bac = new Label();
			formatLabel(fieldOpt1Bac, captionOpt1Bac, bec.getLicOpt1Bac());
			formBacLayout.addComponent(fieldOpt1Bac);
		}
		if(bec.getLicOpt2Bac()!=null) {
			String captionOpt2Bac = applicationContext.getMessage(NAME+".opt2bac.title", null, getLocale());
			Label fieldOpt2Bac = new Label();
			formatLabel(fieldOpt2Bac, captionOpt2Bac, bec.getLicOpt2Bac());
			formBacLayout.addComponent(fieldOpt2Bac);
		}
		if(bec.getLicOpt3Bac()!=null) {
			String captionOpt3Bac = applicationContext.getMessage(NAME+".opt3bac.title", null, getLocale());
			Label fieldOpt3Bac = new Label();
			formatLabel(fieldOpt3Bac, captionOpt3Bac, bec.getLicOpt3Bac());
			formBacLayout.addComponent(fieldOpt3Bac);
		}
		if(bec.getLicOpt4Bac()!=null) {
			String captionOpt4Bac = applicationContext.getMessage(NAME+".opt4bac.title", null, getLocale());
			Label fieldOpt4Bac = new Label();
			formatLabel(fieldOpt4Bac, captionOpt4Bac, bec.getLicOpt4Bac());
			formBacLayout.addComponent(fieldOpt4Bac);
		}

		String captionAnneeBac = applicationContext.getMessage(NAME+".anneebac.title", null, getLocale());
		Label fieldAnneeBac = new Label();
		formatLabel(fieldAnneeBac, captionAnneeBac, bec.getDaa_obt_bac_iba());
		formBacLayout.addComponent(fieldAnneeBac);

		String captionMentionBac = applicationContext.getMessage(NAME+".mentionbac.title", null, getLocale());
		Label fieldMentionBac = new Label();
		formatLabel(fieldMentionBac, captionMentionBac, bec.getCod_mnb());
		formBacLayout.addComponent(fieldMentionBac);


		String captionTypeEtbBac = applicationContext.getMessage(NAME+".typeetbbac.title", null, getLocale());
		Label fieldTypeEtbBac = new Label();
		formatLabel(fieldTypeEtbBac, captionTypeEtbBac, bec.getCod_tpe());
		formBacLayout.addComponent(fieldTypeEtbBac);
		

		String captionEtbBac = applicationContext.getMessage(NAME+".etbbac.title", null, getLocale());
		Label fieldEtbBac = new Label();
		formatLabel(fieldEtbBac, captionEtbBac, bec.getCod_etb());
		formBacLayout.addComponent(fieldEtbBac);

		String captionDepBac = applicationContext.getMessage(NAME+".depbac.title", null, getLocale());
		Label fieldDepBac = new Label();
		formatLabel(fieldDepBac, captionDepBac, bec.getCod_dep());
		formBacLayout.addComponent(fieldDepBac);
	}
	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
	}

}
