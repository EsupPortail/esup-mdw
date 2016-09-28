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
package fr.univlorraine.mondossierweb.views.windows;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Adresse;
import fr.univlorraine.mondossierweb.beans.Etudiant;
import fr.univlorraine.mondossierweb.controllers.AdresseController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import gouv.education.apogee.commun.transverse.dto.etudiant.TypeHebergementDTO;
import gouv.education.apogee.commun.transverse.dto.geographie.PaysDTO;
import gouv.education.apogee.commun.transverse.dto.geographie.CommuneDTO;

/**
 * Fenêtre de modification des adresses
 */
@Configurable(preConstruction=true)
public class ModificationAdressesWindow extends Window {
	private static final long serialVersionUID = -1792808588462463042L;

	public static final String NAME = "modificationAdressesWindow";

	private static final String COD_HEBERG_DOMICILE_PARENTAL = "4";
	private static final String COD_PAY_FRANCE = "100";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient AdresseController adresseController;
	@Resource
	private transient EtudiantController etudiantController;

	/* Composants */
	private Button btnAnnuler = new Button();

	private Button btnValider = new Button();


	private NativeSelect lhebergement;
	private TextField fieldAnnu1;
	private TextField fieldAnnu2;
	private TextField fieldAnnu3;
	private NativeSelect lpays1;
	private TextField fieldTelephone1;
	private TextField fieldCodePostal1;
	private String codePostalVillesAnnu;
	private NativeSelect lville1;
	private TextField fieldVilleEtranger1;
	private Label labelChoixHebergement;


	private TextField fieldFixe1;
	private TextField fieldFixe2;
	private TextField fieldFixe3;
	private NativeSelect lpays2;
	private TextField fieldTelephone2;
	private TextField fieldCodePostal2;
	private String codePostalVillesFixe;
	private NativeSelect lville2;
	private TextField fieldVilleEtranger2;


	/**
	 * Crée une fenêtre de confirmation
	 * @param message
	 * @param titre
	 */
	public ModificationAdressesWindow(Etudiant etudiant) {
		/* Style */
		//setWidth(900, Unit.PIXELS);
		setModal(true);
		setResizable(false);
		setClosable(false);

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);


		/* Titre */
		setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));
		
		/* Layout pour afficher les erreurs */
		VerticalLayout erreursLayout = new VerticalLayout();
		layout.addComponent(erreursLayout);
		erreursLayout.setVisible(false);
		
		HorizontalLayout panelslayout = new HorizontalLayout();
		panelslayout.setMargin(true);
		panelslayout.setSpacing(true);
		layout.addComponent(panelslayout);

		/* Panel adresse annuelle */
		Panel adressesAnnuellePanel = new Panel(applicationContext.getMessage(NAME+".panel.adresseannuelle.title", null, getLocale())+" "+MainUI.getCurrent().getEtudiant().getAdresseAnnuelle().getAnnee());

		FormLayout formAdresseAnnuelleLayout = new FormLayout();
		formAdresseAnnuelleLayout.setSpacing(true);
		formAdresseAnnuelleLayout.setMargin(true);

		//TypeHebergement
		String captionHebergement = applicationContext.getMessage(NAME+".typehebergement", null, getLocale());
		TypeHebergementDTO[] hebergements = adresseController.getTypesHebergement();
		lhebergement = new NativeSelect();
		lhebergement.setCaption(captionHebergement);
		lhebergement.setNullSelectionAllowed(false);
		lhebergement.setRequired(true);
		lhebergement.setWidth("326px");
		for(TypeHebergementDTO h : hebergements){
			lhebergement.addItem(h.getCodTypeHebergement());
			lhebergement.setItemCaption(h.getCodTypeHebergement(), h.getLibWebTypeHebergement());
		}
		lhebergement.setValue(etudiant.getAdresseAnnuelle().getType());
		lhebergement.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String selectedValue = (String) event.getProperty().getValue();

				//Si un hébergement autre que la Domicile parental a été choisi
				if(!selectedValue.equals(COD_HEBERG_DOMICILE_PARENTAL)){
					activeFormulaireAdresseAnnuelle();

				}else{
					desactiveFormulaireAdresseAnnuelle();
				}
			}
		});

		formAdresseAnnuelleLayout.addComponent(lhebergement);

		//labelChoixHebergement
		labelChoixHebergement = new Label(applicationContext.getMessage(NAME+".labelhebergement", null, getLocale()));
		formAdresseAnnuelleLayout.addComponent(labelChoixHebergement);

		//AdresseAnnuelle1
		fieldAnnu1 = new TextField(applicationContext.getMessage(NAME+".annu1", null, getLocale()));
		fieldAnnu1.setValue(etudiant.getAdresseAnnuelle().getAdresse1());
		fieldAnnu1.setNullRepresentation("");
		fieldAnnu1.setWidth("326px");
		fieldAnnu1.setMaxLength(32);
		fieldAnnu1.setRequired(true);
		formAdresseAnnuelleLayout.addComponent(fieldAnnu1);

		//AdresseAnnuelle2
		fieldAnnu2 = new TextField(applicationContext.getMessage(NAME+".annu2", null, getLocale()));
		fieldAnnu2.setValue(etudiant.getAdresseAnnuelle().getAdresse2());
		fieldAnnu2.setNullRepresentation("");
		fieldAnnu2.setWidth("326px");
		fieldAnnu2.setMaxLength(32);
		formAdresseAnnuelleLayout.addComponent(fieldAnnu2);

		//AdresseAnnuelle3
		fieldAnnu3 = new TextField(applicationContext.getMessage(NAME+".annu3", null, getLocale()));
		fieldAnnu3.setValue(etudiant.getAdresseAnnuelle().getAdresse3());
		fieldAnnu3.setNullRepresentation("");
		fieldAnnu3.setWidth("326px");
		fieldAnnu3.setMaxLength(32);
		formAdresseAnnuelleLayout.addComponent(fieldAnnu3);



		//Liste des Pays
		String captionPays= applicationContext.getMessage(NAME+".pays1", null, getLocale());
		PaysDTO[] pays = adresseController.getPays();
		lpays1 = new NativeSelect();
		lpays1.setCaption(captionPays);
		lpays1.setNullSelectionAllowed(false);
		lpays1.setRequired(true);
		lpays1.setWidth("326px");
		for(PaysDTO p : pays){
			lpays1.addItem(p.getCodePay());
			lpays1.setItemCaption(p.getCodePay(),p.getLibPay());
		}
		lpays1.setValue(etudiant.getAdresseAnnuelle().getCodPays());
		lpays1.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String selectedValue = (String) event.getProperty().getValue();

				//Si un pays autre que France a été choisi
				if(!selectedValue.equals(COD_PAY_FRANCE)){
					activerChampPourAdresseAnnuelleEtranger();
				}else{
					activerChampPourAdresseAnnuelleEnFrance();
				}
			}
		});
		formAdresseAnnuelleLayout.addComponent(lpays1);


		//Ville pour adresse à l'étranger
		fieldVilleEtranger1 = new TextField(applicationContext.getMessage(NAME+".villeetranger1", null, getLocale()));
		fieldVilleEtranger1.setValue(etudiant.getAdresseAnnuelle().getAdresseetranger());
		fieldVilleEtranger1.setNullRepresentation("");
		fieldVilleEtranger1.setWidth("326px");
		fieldVilleEtranger1.setMaxLength(5);
		fieldVilleEtranger1.setRequired(true);
		formAdresseAnnuelleLayout.addComponent(fieldVilleEtranger1);

		//codePostal1 pour adresses en france
		fieldCodePostal1 = new TextField(applicationContext.getMessage(NAME+".codepostal1", null, getLocale()));
		fieldCodePostal1.setValue(etudiant.getAdresseAnnuelle().getCodePostal());
		fieldCodePostal1.setNullRepresentation("");
		fieldCodePostal1.setWidth("326px");
		fieldCodePostal1.setMaxLength(5);
		fieldCodePostal1.setRequired(true);
		//fieldCodePostal1.setTextChangeEventMode(TextChangeEventMode.EAGER);
		fieldCodePostal1.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				updateListeVillesAnnuelle(event.getText());
			}
		});

		formAdresseAnnuelleLayout.addComponent(fieldCodePostal1);

		//Ville pour adresse en france
		List<CommuneDTO> villes1 = adresseController.getVilles(etudiant.getAdresseAnnuelle().getCodePostal());
		lville1 = new NativeSelect();
		lville1.setCaption(applicationContext.getMessage(NAME+".ville1", null, getLocale()));
		lville1.setNullSelectionAllowed(false);
		lville1.setRequired(true);
		lville1.setWidth("326px");
		for(CommuneDTO v : villes1){
			lville1.addItem(v.getLibCommune());
			lville1.setItemCaption(v.getLibCommune(),v.getLibCommune());
		}
		codePostalVillesAnnu = etudiant.getAdresseAnnuelle().getCodePostal();
		lville1.setValue(etudiant.getAdresseAnnuelle().getVille());
		lville1.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateCodePostalVilleAnnuelle();
			}
		});
		formAdresseAnnuelleLayout.addComponent(lville1);


		//activation des champs utiles en fonction de l'adresse de l'étudiant avant la modification
		if(etudiant.getAdresseAnnuelle().getCodPays().equals(COD_PAY_FRANCE)){
			activerChampPourAdresseAnnuelleEnFrance();
		}else{
			activerChampPourAdresseAnnuelleEtranger();
		}


		//Téléphone1
		fieldTelephone1 = new TextField(applicationContext.getMessage(NAME+".tel1", null, getLocale()));
		fieldTelephone1.setValue(etudiant.getAdresseAnnuelle().getNumerotel());
		fieldTelephone1.setNullRepresentation("");
		fieldTelephone1.setWidth("326px");
		fieldTelephone1.setMaxLength(15);
		fieldTelephone1.setRequired(false);
		formAdresseAnnuelleLayout.addComponent(fieldTelephone1);

		//ajout du panel adresse Annuelle
		adressesAnnuellePanel.setContent(formAdresseAnnuelleLayout);
		panelslayout.addComponent(adressesAnnuellePanel);


		//Si un hébergement autre que la Domicile parental a été choisi
		if(!etudiant.getAdresseAnnuelle().getType().equals(COD_HEBERG_DOMICILE_PARENTAL)){
			activeFormulaireAdresseAnnuelle();
		}else{
			desactiveFormulaireAdresseAnnuelle();
		}



		/* Panel adresse fixe */
		Panel adressesFixePanel = new Panel(applicationContext.getMessage(NAME+".panel.adressefixe.title", null, getLocale()));

		FormLayout formAdresseFixeLayout = new FormLayout();
		formAdresseFixeLayout.setSpacing(true);
		formAdresseFixeLayout.setMargin(true);


		//AdresseFixe1
		fieldFixe1 = new TextField(applicationContext.getMessage(NAME+".fixe1", null, getLocale()));
		fieldFixe1.setValue(etudiant.getAdresseFixe().getAdresse1());
		fieldFixe1.setNullRepresentation("");
		fieldFixe1.setWidth("326px");
		fieldFixe1.setMaxLength(32);
		fieldFixe1.setRequired(true);
		formAdresseFixeLayout.addComponent(fieldFixe1);

		//AdresseFixe2
		fieldFixe2 = new TextField(applicationContext.getMessage(NAME+".fixe2", null, getLocale()));
		fieldFixe2.setValue(etudiant.getAdresseFixe().getAdresse2());
		fieldFixe2.setNullRepresentation("");
		fieldFixe2.setWidth("326px");
		fieldFixe2.setMaxLength(32);
		formAdresseFixeLayout.addComponent(fieldFixe2);

		//AdresseFixe3
		fieldFixe3 = new TextField(applicationContext.getMessage(NAME+".fixe3", null, getLocale()));
		fieldFixe3.setValue(etudiant.getAdresseFixe().getAdresse3());
		fieldFixe3.setNullRepresentation("");
		fieldFixe3.setWidth("326px");
		fieldFixe3.setMaxLength(32);
		formAdresseFixeLayout.addComponent(fieldFixe3);



		//Liste des Pays
		lpays2 = new NativeSelect();
		lpays2.setCaption(captionPays);
		lpays2.setNullSelectionAllowed(false);
		lpays2.setRequired(true);
		lpays2.setWidth("326px");
		for(PaysDTO p : pays){
			lpays2.addItem(p.getCodePay());
			lpays2.setItemCaption(p.getCodePay(),p.getLibPay());
		}
		lpays2.setValue(etudiant.getAdresseFixe().getCodPays());
		lpays2.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				String selectedValue = (String) event.getProperty().getValue();

				//Si un pays autre que France a été choisi
				if(!selectedValue.equals(COD_PAY_FRANCE)){
					activerChampPourAdresseFixeEtranger();
				}else{
					activerChampPourAdresseFixeEnFrance();
				}
			}
		});
		formAdresseFixeLayout.addComponent(lpays2);


		//Ville pour adresse à l'étranger
		fieldVilleEtranger2 = new TextField(applicationContext.getMessage(NAME+".villeetranger2", null, getLocale()));
		fieldVilleEtranger2.setValue(etudiant.getAdresseFixe().getAdresseetranger());
		fieldVilleEtranger2.setNullRepresentation("");
		fieldVilleEtranger2.setWidth("326px");
		fieldVilleEtranger2.setMaxLength(5);
		fieldVilleEtranger2.setRequired(true);
		formAdresseFixeLayout.addComponent(fieldVilleEtranger2);

		//codePostal2 pour adresses en france
		fieldCodePostal2 = new TextField(applicationContext.getMessage(NAME+".codepostal2", null, getLocale()));
		fieldCodePostal2.setValue(etudiant.getAdresseFixe().getCodePostal());
		fieldCodePostal2.setNullRepresentation("");
		fieldCodePostal2.setWidth("326px");
		fieldCodePostal2.setMaxLength(5);
		fieldCodePostal2.setRequired(true);
		//fieldCodePostal1.setTextChangeEventMode(TextChangeEventMode.EAGER);
		fieldCodePostal2.addTextChangeListener(new TextChangeListener() {
			@Override
			public void textChange(TextChangeEvent event) {
				updateListeVillesFixe(event.getText());
			}
		});

		formAdresseFixeLayout.addComponent(fieldCodePostal2);

		//Ville pour adresse en france
		List<CommuneDTO> villes2 = adresseController.getVilles(etudiant.getAdresseFixe().getCodePostal());
		lville2 = new NativeSelect();
		lville2.setCaption(applicationContext.getMessage(NAME+".ville2", null, getLocale()));
		lville2.setNullSelectionAllowed(false);
		lville2.setRequired(true);
		lville2.setWidth("326px");
		for(CommuneDTO v : villes2){
			lville2.addItem(v.getLibCommune());
			lville2.setItemCaption(v.getLibCommune(),v.getLibCommune());
		}
		codePostalVillesFixe = etudiant.getAdresseFixe().getCodePostal();
		lville2.setValue(etudiant.getAdresseFixe().getVille());
		lville2.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				updateCodePostalVilleFixe();
			}
		});
		formAdresseFixeLayout.addComponent(lville2);


		//activation des champs utiles en fonction de l'adresse de l'étudiant avant la modification
		if(etudiant.getAdresseFixe().getCodPays().equals(COD_PAY_FRANCE)){
			activerChampPourAdresseFixeEnFrance();
		}else{
			activerChampPourAdresseFixeEtranger();
		}


		//Téléphone2
		fieldTelephone2 = new TextField(applicationContext.getMessage(NAME+".tel2", null, getLocale()));
		fieldTelephone2.setValue(etudiant.getAdresseFixe().getNumerotel());
		fieldTelephone2.setNullRepresentation("");
		fieldTelephone2.setWidth("326px");
		fieldTelephone2.setMaxLength(15);
		fieldTelephone2.setRequired(false);
		formAdresseFixeLayout.addComponent(fieldTelephone2);

		//ajout du panel adresse fixe
		adressesFixePanel.setContent(formAdresseFixeLayout);
		panelslayout.addComponent(adressesFixePanel);






		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);

		btnValider.setCaption(applicationContext.getMessage(NAME+".btnValider", null, getLocale()));
		btnValider.setIcon(FontAwesome.CHECK);
		btnValider.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnValider.addClickListener(e -> {

			Adresse adresseAnnuelle = new Adresse();
			adresseAnnuelle.setType(lhebergement.getValue().toString());
			adresseAnnuelle.setAdresse1(fieldAnnu1.getValue());
			adresseAnnuelle.setAdresse2(fieldAnnu2.getValue());
			adresseAnnuelle.setAdresse3(fieldAnnu3.getValue());
			adresseAnnuelle.setCodPays(lpays1.getValue().toString());
			adresseAnnuelle.setCodePostal(fieldCodePostal1.getValue());
			adresseAnnuelle.setVille((lville1.getValue()==null)? null : lville1.getValue().toString());
			adresseAnnuelle.setAdresseetranger(fieldVilleEtranger1.getValue());
			adresseAnnuelle.setNumerotel(fieldTelephone1.getValue());


			Adresse adresseFixe = new Adresse();
			adresseFixe.setAdresse1(fieldFixe1.getValue());
			adresseFixe.setAdresse2(fieldFixe2.getValue());
			adresseFixe.setAdresse3(fieldFixe3.getValue());
			adresseFixe.setCodPays(lpays2.getValue().toString());
			adresseFixe.setCodePostal(fieldCodePostal2.getValue());
			adresseFixe.setVille((lville2.getValue()==null)? null : lville2.getValue().toString());
			adresseFixe.setAdresseetranger(fieldVilleEtranger2.getValue());
			adresseFixe.setNumerotel(fieldTelephone2.getValue());

			erreursLayout.removeAllComponents();
			List<String> retour = adresseController.majAdresses(adresseAnnuelle, adresseFixe);
			if(retour!=null && retour.size()==1 && retour.get(0).equals("OK")){
				//ajout maj vue adresse
				etudiantController.recupererAdresses();
				close();
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
		buttonsLayout.addComponent(btnValider);
		buttonsLayout.setComponentAlignment(btnValider, Alignment.MIDDLE_CENTER);

		btnAnnuler.setCaption(applicationContext.getMessage("modificationAdressesWindow.btnAnnuler", null, getLocale()));
		btnAnnuler.setIcon(FontAwesome.TIMES);
		btnAnnuler.addStyleName(ValoTheme.BUTTON_DANGER);
		btnAnnuler.addClickListener(e -> close());
		buttonsLayout.addComponent(btnAnnuler);
		buttonsLayout.setComponentAlignment(btnAnnuler, Alignment.MIDDLE_CENTER);

		layout.addComponent(buttonsLayout);

		/* Centre la fenêtre */
		center();
	}


	private void activerChampPourAdresseAnnuelleEnFrance() {
		fieldVilleEtranger1.setVisible(false);
		fieldCodePostal1.setVisible(true);
		lville1.setVisible(true);

	}

	private void activerChampPourAdresseAnnuelleEtranger() {
		fieldVilleEtranger1.setVisible(true);
		fieldCodePostal1.setVisible(false);
		lville1.setVisible(false);

	}


	private void updateCodePostalVilleAnnuelle() {
		fieldCodePostal1.setValue(codePostalVillesAnnu);
	}

	private void updateListeVillesAnnuelle(String text) {
		List<CommuneDTO> villes1 = adresseController.getVilles(text);
		if(villes1!=null && villes1.size()>0){
			codePostalVillesAnnu = text;
			if(lville1!=null){
				lville1.removeAllItems();
				for(CommuneDTO v : villes1){
					lville1.addItem(v.getLibCommune());
					lville1.setItemCaption(v.getLibCommune(),v.getLibCommune());
				}
				lville1.setValue(villes1.get(0).getLibCommune());
			}
		}

	}







	private void activerChampPourAdresseFixeEnFrance() {
		fieldVilleEtranger2.setVisible(false);
		fieldCodePostal2.setVisible(true);
		lville2.setVisible(true);

	}

	private void activerChampPourAdresseFixeEtranger() {
		fieldVilleEtranger2.setVisible(true);
		fieldCodePostal2.setVisible(false);
		lville2.setVisible(false);

	}


	private void updateCodePostalVilleFixe() {
		fieldCodePostal2.setValue(codePostalVillesFixe);
	}

	private void updateListeVillesFixe(String text) {
		List<CommuneDTO> villes2 = adresseController.getVilles(text);
		if(villes2!=null && villes2.size()>0){
			codePostalVillesFixe = text;
			if(lville2!=null){
				lville2.removeAllItems();
				for(CommuneDTO v : villes2){
					lville2.addItem(v.getLibCommune());
					lville2.setItemCaption(v.getLibCommune(),v.getLibCommune());
				}
				lville2.setValue(villes2.get(0).getLibCommune());
			}
		}

	}


	private void activeFormulaireAdresseAnnuelle(){
		//active les champs adresse annuelle
		fieldAnnu1.setVisible(true);
		fieldAnnu2.setVisible(true);
		fieldAnnu3.setVisible(true);
		lpays1.setVisible(true);
		fieldTelephone1.setVisible(true);
		fieldCodePostal1.setVisible(lpays1.getValue().equals(COD_PAY_FRANCE));
		lville1.setVisible(lpays1.getValue().equals(COD_PAY_FRANCE));
		fieldVilleEtranger1.setVisible(!lpays1.getValue().equals(COD_PAY_FRANCE));
		labelChoixHebergement.setVisible(false);
		

	}

	private void desactiveFormulaireAdresseAnnuelle(){
		//desactive les champs adresse annuelle
		fieldAnnu1.setVisible(false);
		fieldAnnu2.setVisible(false);
		fieldAnnu3.setVisible(false);
		lpays1.setVisible(false);
		fieldTelephone1.setVisible(false);
		fieldCodePostal1.setVisible(false);
		lville1.setVisible(false);
		fieldVilleEtranger1.setVisible(false);
		labelChoixHebergement.setVisible(true);
	}
}
