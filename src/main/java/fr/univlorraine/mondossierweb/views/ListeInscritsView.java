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

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit.Vet;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.utils.MyFileDownloader;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.DetailGroupesWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpBasicWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Recherche arborescente
 */
@Component @Scope("prototype")
@SpringView(name = ListeInscritsView.NAME)
public class ListeInscritsView extends VerticalLayout implements View {

	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(ListeInscritsView.class);

	public static final String NAME = "listeInscritsView";

	public static final String[] INS_FIELDS_ELP = {"nom","prenom","date_nai_ind"};

	public static final String[] INS_FIELDS_VET = {"nom","prenom","date_nai_ind","iae", "ipe"};

	public static final String[] INS_FIELDS_TO_DISPLAY_ELP = {"cod_etu","nom","prenom","date_nai_ind","email","etape","notes1","notes2","groupe"};

	public static final String[] INS_FIELDS_TO_DISPLAY_VET = {"cod_etu","nom","prenom","date_nai_ind","email","iae","ipe","notes1","notes2","groupe"};

	public static final String TOUTES_LES_ETAPES_LABEL = "toutes";

	public static final String TOUS_LES_GROUPES_LABEL = "tous";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;


	@Resource
	private transient ListeInscritsController listeInscritsController;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient FavorisController favorisController;

	@Resource
	private transient ObjectFactory<HelpBasicWindow> helpBasicWindowFactory;

	@Resource
	private transient ObjectFactory<DetailGroupesWindow> detailGroupesWindowFactory;

	/** Thread pool  */
	ExecutorService executorService = Executors.newSingleThreadExecutor();

	//la liste des inscrits
	private List<Inscrit> linscrits;

	private Button btnTrombi;

	private Button btnRetourListe;

	private Table inscritstable;

	private GridLayout trombiLayout;

	private Button btnMasquerFiltre;

	private Button btnAjoutFavori;

	private VerticalLayout favoriLayout;

	private VerticalLayout verticalLayoutForTrombi;

	private String code;

	private String typeFavori;

	private VerticalLayout dataLayout;

	private boolean afficherTrombinoscope;

	private ComboBox listeAnnees;

	private ComboBox listeEtapes;

	private ComboBox listeGroupes;

	//liste contenant tous les codind à afficher (apres application du filtre)
	private List<String> listecodind;

	private VerticalLayout infoLayout;

	private Label infoNbInscrit;

	private String libelleObj;

	private Panel panelFormInscrits;

	private HorizontalLayout leftResumeLayout;

	private HorizontalLayout middleResumeLayout;

	private Button btnDisplayFiltres;

	private Button btnExportTrombi;

	private Button btnExportExcel;

	private CheckBox collapseEtp;

	private CheckBox collapseResultatsS1;

	private CheckBox collapseResultatsS2;
	
	private CheckBox collapseGrp;



	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {


	}
	public void refresh() {

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && userController.isEnseignant()){
			//Actualiser de l'affiche du bouton de mise en favori
			if(btnAjoutFavori !=null && favoriLayout!=null && StringUtils.hasText(code) && StringUtils.hasText(typeFavori)){

				List<Favoris> lfav = favorisController.getFavoris();
				FavorisPK favpk = new FavorisPK();
				favpk.setLogin(userController.getCurrentUserName());
				favpk.setIdfav(code);
				favpk.setTypfav(typeFavori);
				Favoris favori  = new Favoris();
				favori.setId(favpk);
				if(lfav!=null && lfav.contains(favori)){
					btnAjoutFavori.setVisible(false);
				}else{
					btnAjoutFavori.setVisible(true);
				}
			}
		}
	}

	public void initListe() {
		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MainUI && userController.isEnseignant()){
			// initialisation de la vue
			removeAllComponents();
			listeEtapes = null;
			listeGroupes = null;

			// Style 
			setMargin(true);
			setSpacing(true);
			setSizeFull();

			// Récupération de l'objet de la SE dont on doit afficher les inscrits
			code = MainUI.getCurrent().getCodeObjListInscrits();
			typeFavori = MainUI.getCurrent().getTypeObjListInscrits();
			libelleObj = "";
			if(typeIsVet() && MainUI.getCurrent().getEtapeListeInscrits()!=null){
				libelleObj= MainUI.getCurrent().getEtapeListeInscrits().getLibelle();
			}
			if(typeIsElp() && MainUI.getCurrent().getElpListeInscrits()!=null){
				libelleObj = MainUI.getCurrent().getElpListeInscrits().getLibelle();
			}

			// Si l'objet est renseigné
			if(code!=null && typeFavori!=null){

				//Panel contenant les filtres d'affichage et le bouton de mise en favori
				HorizontalLayout panelLayout = new HorizontalLayout();
				panelLayout.setSizeFull();
				panelLayout.addStyleName("small-font-element");

				// Layout contenant les filtres
				FormLayout formInscritLayout = new FormLayout();
				formInscritLayout.setStyleName(ValoTheme.FORMLAYOUT_LIGHT);
				formInscritLayout.setSpacing(true);
				formInscritLayout.setMargin(true);

				panelFormInscrits= new Panel(code+" "+libelleObj);

				//Affichage d'une liste déroulante contenant la liste des années
				List<String> lannees = MainUI.getCurrent().getListeAnneeInscrits();
				if(lannees != null && lannees.size()>0){
					listeAnnees = new ComboBox(applicationContext.getMessage(NAME+".annee", null, getLocale()));
					listeAnnees.setPageLength(5);
					listeAnnees.setTextInputAllowed(false);
					listeAnnees.setNullSelectionAllowed(false);
					listeAnnees.setWidth("150px");
					for(String annee : lannees){
						listeAnnees.addItem(annee);
						int anneenplusun = Integer.parseInt(annee) + 1;
						listeAnnees.setItemCaption(annee,annee+"/"+anneenplusun);
					}
					listeAnnees.setValue( MainUI.getCurrent().getAnneeInscrits());

					//Gestion de l'événement sur le changement d'année
					listeAnnees.addValueChangeListener(new ValueChangeListener() {
						@Override
						public void valueChange(ValueChangeEvent event) {
							String selectedValue = (String) event.getProperty().getValue();

							//faire le changement
							Map<String, String> parameterMap = new HashMap<>();
							parameterMap.put("code",code);
							parameterMap.put("type",typeFavori);

							//récupération de la nouvelle liste
							if(typeIsVet()){
								listeInscritsController.recupererLaListeDesInscrits(parameterMap, selectedValue,MainUI.getCurrent());
							}
							if(typeIsElp()){
								listeInscritsController.recupererLaListeDesInscritsELP(parameterMap, selectedValue,MainUI.getCurrent());
							}

							//update de l'affichage
							initListe();
						}
					});
					formInscritLayout.addComponent(listeAnnees);

				}

				//Si on affiche la liste des inscrits à un ELP
				//on doit affiche l'étape d'appartenance et éventuellement les groupes
				//Affichage d'une liste déroulante contenant la liste des années
				if(typeIsElp()){
					List<VersionEtape> letapes = MainUI.getCurrent().getListeEtapesInscrits();
					if(letapes != null && letapes.size()>0){
						listeEtapes = new ComboBox(applicationContext.getMessage(NAME+".etapes", null, getLocale()));
						listeEtapes.setPageLength(5);
						listeEtapes.setNullSelectionAllowed(false);
						listeEtapes.setTextInputAllowed(false);
						listeEtapes.setRequired(false);
						listeEtapes.setWidth("400px");
						listeEtapes.addItem(TOUTES_LES_ETAPES_LABEL);
						listeEtapes.setItemCaption(TOUTES_LES_ETAPES_LABEL,TOUTES_LES_ETAPES_LABEL);
						for(VersionEtape etape : letapes){
							String idEtape  = etape.getId().getCod_etp()+"/"+etape.getId().getCod_vrs_vet();
							listeEtapes.addItem(idEtape);
							listeEtapes.setItemCaption(idEtape,"["+idEtape+"] "+etape.getLib_web_vet());
						}

						if(MainUI.getCurrent().getEtapeInscrits()!=null){
							listeEtapes.setValue( MainUI.getCurrent().getEtapeInscrits());
						}else{

							listeEtapes.setValue( TOUTES_LES_ETAPES_LABEL);
						}

						//Gestion de l'événement sur le changement d'étape
						listeEtapes.addValueChangeListener(new ValueChangeListener() {
							@Override
							public void valueChange(ValueChangeEvent event) {
								String vetSelectionnee = (String) event.getProperty().getValue();
								if(vetSelectionnee.equals(TOUTES_LES_ETAPES_LABEL)){
									vetSelectionnee = null;
								}
								MainUI.getCurrent().setEtapeInscrits(vetSelectionnee);

								//faire le changement
								filtrerInscrits(vetSelectionnee, getGroupeSelectionne());

							}
						});
						formInscritLayout.addComponent(listeEtapes);

					}

					List<ElpDeCollection> lgroupes = MainUI.getCurrent().getListeGroupesInscrits();
					if(lgroupes != null && lgroupes.size()>0){
						listeGroupes = new ComboBox();
						listeGroupes.setPageLength(5);
						listeGroupes.setNullSelectionAllowed(false);
						listeGroupes.setTextInputAllowed(false);
						listeGroupes.setRequired(false);
						listeGroupes.setStyleName(ValoTheme.COMBOBOX_BORDERLESS);
						listeGroupes.setWidth("348px");
						listeGroupes.addItem(TOUS_LES_GROUPES_LABEL);
						listeGroupes.setItemCaption(TOUS_LES_GROUPES_LABEL,TOUS_LES_GROUPES_LABEL);
						for(ElpDeCollection edc : lgroupes){
							for(CollectionDeGroupes cdg : edc.getListeCollection()){
								for(Groupe groupe : cdg.getListeGroupes()){
									listeGroupes.addItem(groupe.getCleGroupe());
									listeGroupes.setItemCaption(groupe.getCleGroupe(),groupe.getLibGroupe());

								}
							}
						}
						if(MainUI.getCurrent().getGroupeInscrits()!=null){
							listeGroupes.setValue( MainUI.getCurrent().getGroupeInscrits());
						}else{
							listeGroupes.setValue(TOUS_LES_GROUPES_LABEL);
						}


						//Gestion de l'événement sur le changement de groupe
						listeGroupes.addValueChangeListener(new ValueChangeListener() {
							@Override
							public void valueChange(ValueChangeEvent event) {
								String grpSelectionnee = (String) event.getProperty().getValue();
								if(grpSelectionnee.equals(TOUS_LES_GROUPES_LABEL)){
									grpSelectionnee = null;
								}
								MainUI.getCurrent().setGroupeInscrits(grpSelectionnee);

								//faire le changement
								filtrerInscrits(getEtapeSelectionnee(),grpSelectionnee);

							}
						});

						HorizontalLayout gpLayout = new HorizontalLayout();
						gpLayout.setCaption(applicationContext.getMessage(NAME+".groupes", null, getLocale()));
						gpLayout.setMargin(false);
						gpLayout.setSpacing(false);
						gpLayout.addComponent(listeGroupes);
						Button btnDetailGpe=new Button();
						btnDetailGpe.setWidth("52px");
						btnDetailGpe.setHeight("32px");
						btnDetailGpe.setStyleName(ValoTheme.BUTTON_PRIMARY);
						btnDetailGpe.setIcon(FontAwesome.SEARCH);
						btnDetailGpe.setDescription(applicationContext.getMessage(NAME+".detail.groupes", null, getLocale()));
						btnDetailGpe.addClickListener(e->{
							String vet =null;
							if(listeEtapes!=null && listeEtapes.getValue()!=null && !listeEtapes.getValue().equals(TOUTES_LES_ETAPES_LABEL)){
								vet = listeEtapes.getItemCaption(listeEtapes.getValue());
							}
							DetailGroupesWindow dgw = detailGroupesWindowFactory.getObject();
							dgw.init(lgroupes, panelFormInscrits.getCaption(), vet, (String)listeAnnees.getValue()); 
							UI.getCurrent().addWindow(dgw);
						});
						gpLayout.addComponent(btnDetailGpe);

						formInscritLayout.addComponent(gpLayout);

					}
				}
				panelLayout.addComponent(formInscritLayout);
				panelLayout.setComponentAlignment(formInscritLayout, Alignment.MIDDLE_LEFT);


				//Création du favori pour l'objet concerné pas la liste des inscrits
				List<Favoris> lfav = favorisController.getFavoris();
				FavorisPK favpk = new FavorisPK();
				favpk.setLogin(userController.getCurrentUserName());
				favpk.setIdfav(code);
				favpk.setTypfav(typeFavori);
				Favoris favori  = new Favoris();
				favori.setId(favpk);
				//Création du bouton pour ajouter l'objet aux favoris
				favoriLayout = new VerticalLayout();
				favoriLayout.setSizeFull();
				favoriLayout.setMargin(true);
				favoriLayout.setSpacing(true);
				btnAjoutFavori = new Button(applicationContext.getMessage(NAME+".btn.ajoutFavori", null, getLocale()));
				btnAjoutFavori.setIcon(FontAwesome.STAR_O);
				btnAjoutFavori.addStyleName(ValoTheme.BUTTON_PRIMARY);
				btnAjoutFavori.setDescription(applicationContext.getMessage(NAME+".btn.ajoutFavori", null, getLocale()));
				btnAjoutFavori.addClickListener(e->{

					//creation du favori en base sur le clic du bouton
					favorisController.saveFavori(favori);

					//On cache le bouton de mise en favori
					btnAjoutFavori.setVisible(false);

					//Affichage d'un message de confirmation
					Notification.show(applicationContext.getMessage(NAME+".message.favoriAjoute", null, getLocale()), Type.TRAY_NOTIFICATION );
				});

				//Ajout du bouton à l'interface
				favoriLayout.addComponent(btnAjoutFavori);
				favoriLayout.setComponentAlignment(btnAjoutFavori, Alignment.TOP_RIGHT);
				if(typeIsElp()){
					btnMasquerFiltre = new Button(applicationContext.getMessage(NAME+".btn.btnMasquerFiltre", null, getLocale()));
					btnMasquerFiltre.setIcon(FontAwesome.CHEVRON_CIRCLE_UP);
					btnMasquerFiltre.addStyleName(ValoTheme.BUTTON_FRIENDLY);
					btnMasquerFiltre.setDescription(applicationContext.getMessage(NAME+".btn.btnMasquerFiltre", null, getLocale()));
					btnMasquerFiltre.addClickListener(e->{
						panelFormInscrits.setContent(null);
						if(btnDisplayFiltres!=null){
							btnDisplayFiltres.setVisible(true);
						}
					});
					favoriLayout.addComponent(btnMasquerFiltre);
					favoriLayout.setComponentAlignment(btnMasquerFiltre, Alignment.BOTTOM_RIGHT);
				}
				panelLayout.addComponent(favoriLayout);
				panelLayout.setComponentAlignment(favoriLayout, Alignment.TOP_RIGHT);

				//Si l'objet est déjà en favori
				if(lfav!=null && lfav.contains(favori)){

					//On affiche pas le bouton de mise en favori
					btnAjoutFavori.setVisible(false);
				}

				panelFormInscrits.setContent(panelLayout);
				addComponent(panelFormInscrits);

				//Récupération de la liste des inscrits
				linscrits = MainUI.getCurrent().getListeInscrits();

				refreshListeCodind(new BeanItemContainer<>(Inscrit.class, linscrits));

				//Test si la liste contient des étudiants
				if(linscrits!=null && linscrits.size()>0 && listecodind!=null && listecodind.size()>0){
					infoLayout= new VerticalLayout();
					infoLayout.setSizeFull();

					//Layout avec le nb d'inscrit, le bouton trombinoscope et le bouton d'export
					HorizontalLayout resumeLayout=new HorizontalLayout();
					resumeLayout.setWidth("100%");
					resumeLayout.setHeight("50px");

					//Label affichant le nb d'inscrits
					infoNbInscrit = new Label(applicationContext.getMessage(NAME+".message.nbinscrit", null, getLocale())+ " : "+linscrits.size());

					leftResumeLayout= new HorizontalLayout();
					leftResumeLayout.addComponent(infoNbInscrit);
					leftResumeLayout.setComponentAlignment(infoNbInscrit, Alignment.MIDDLE_LEFT);

					Button infoDescriptionButton = new Button();
					infoDescriptionButton.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
					infoDescriptionButton.setIcon(FontAwesome.INFO_CIRCLE);
					infoDescriptionButton.setDescription(applicationContext.getMessage(NAME+".message.info.predescription", null, getLocale()));
					infoDescriptionButton.addClickListener(e->{
						String message ="";
						if(typeIsVet()){
							message=applicationContext.getMessage(NAME+".message.info.vetdescription", null, getLocale());
						}
						if(typeIsElp()){
							message=applicationContext.getMessage(NAME+".message.info.elpdescription", null, getLocale());
						}

						HelpBasicWindow hbw = helpBasicWindowFactory.getObject();
						hbw.init(message,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()));
						UI.getCurrent().addWindow(hbw);
					});
					leftResumeLayout.addComponent(infoDescriptionButton);
					leftResumeLayout.setComponentAlignment(infoDescriptionButton, Alignment.MIDDLE_LEFT);




					//Bouton export trombinoscope
					btnExportTrombi=new Button();
					btnExportTrombi.setIcon(FontAwesome.FILE_PDF_O);
					btnExportTrombi.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
					btnExportTrombi.addStyleName("button-icon");
					btnExportTrombi.addStyleName("red-button-icon");
					btnExportTrombi.setDescription(applicationContext.getMessage(NAME + ".pdf.trombinoscope.link", null, getLocale()));

					//methode qui permet de generer l'export à la demande
					//Création du nom du fichier
					String nomFichier = applicationContext.getMessage("pdf.trombinoscope.title", null, Locale.getDefault())+"_" + panelFormInscrits.getCaption() +  ".pdf";
					nomFichier = nomFichier.replaceAll(" ","_");
					StreamResource resource = new StreamResource(new StreamResource.StreamSource() {
						@Override
						public InputStream getStream() {

							//recuperation de l'année sélectionnée et du libellé de l'ELP
							String annee=(String)listeAnnees.getValue();
							String libObj=panelFormInscrits.getCaption();

							//création du trombi en pdf
							return listeInscritsController.getPdfStream(linscrits, listecodind,libObj,  annee);
						}
					}, nomFichier);
					resource.setMIMEType("application/force-download;charset=UTF-8");
					resource.setCacheTime(0);

					//On ajoute le FD sur le bouton d'export
					if(PropertyUtils.isPushEnabled()){
						new MyFileDownloader(resource).extend(btnExportTrombi);
					}else{
						FileDownloader fdpdf = new FileDownloader(resource);
						fdpdf.setOverrideContentType(false);
						fdpdf.extend(btnExportTrombi);
					}

					leftResumeLayout.addComponent(btnExportTrombi);
					leftResumeLayout.setComponentAlignment(btnExportTrombi, Alignment.MIDDLE_LEFT);
					//if(!afficherTrombinoscope){

					//On cache le bouton d'export pdf
					btnExportTrombi.setVisible(false);
					//}


					//Bouton export liste excel
					btnExportExcel=new Button();
					btnExportExcel.setIcon(FontAwesome.FILE_EXCEL_O);
					btnExportExcel.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
					btnExportExcel.addStyleName("button-icon");
					btnExportExcel.setDescription(applicationContext.getMessage(NAME + ".excel.link", null, getLocale()));
					String nomFichierExcel = applicationContext.getMessage("excel.listeinscrits.title", null, Locale.getDefault())+"_" + panelFormInscrits.getCaption() +  ".xlsx";
					nomFichierExcel = nomFichierExcel.replaceAll(" ","_");
					StreamResource resourceXls = new StreamResource(new StreamResource.StreamSource() {
						@Override
						public InputStream getStream() {
							//recuperation de l'année sélectionnée et du libellé de l'ELP
							String annee=(String)listeAnnees.getValue();
							String libObj=panelFormInscrits.getCaption();
							// Récupération des paramètres d'affichage du tableau
							boolean etp = collapseEtp != null && collapseEtp.getValue();
							boolean s1 = collapseResultatsS1 != null && collapseResultatsS1.getValue();
							boolean s2 = collapseResultatsS2 !=null && collapseResultatsS2.getValue();
							boolean grp = collapseGrp != null && collapseGrp.getValue();
							//création du trombi en pdf
							return listeInscritsController.getXlsStream(linscrits, listecodind, listeGroupes, libObj, annee, typeFavori, etp, s1, s2, grp);
						}
					}, nomFichierExcel);
					resourceXls.setMIMEType("application/force-download;charset=UTF-8");
					resourceXls.setCacheTime(0);
					//On ajoute le FD sur le bouton d'export
					if(PropertyUtils.isPushEnabled()){
						new MyFileDownloader(resourceXls).extend(btnExportExcel);
					}else{
						FileDownloader fd = new FileDownloader(resourceXls);
						fd.setOverrideContentType(false);
						fd.extend(btnExportExcel);
					}

					//if(!afficherTrombinoscope){
					//On échange le bouton d'export pdf par le bouton export excel
					leftResumeLayout.replaceComponent(btnExportTrombi, btnExportExcel);
					//}

					resumeLayout.addComponent(leftResumeLayout);

					//Middle layout avec les bouton de collapse des colonnes
					middleResumeLayout= new HorizontalLayout();
					middleResumeLayout.setSizeFull();
					middleResumeLayout.addStyleName("small-font-element");
					middleResumeLayout.setSpacing(true);

					if(!typeIsVet()){
						collapseEtp = new CheckBox(applicationContext.getMessage(NAME+".collapseEtp.title", null, getLocale()));
						collapseEtp.setValue(true);
						collapseEtp.addValueChangeListener(e->{
							inscritstable.setColumnCollapsed("etape", !collapseEtp.getValue());
						});
						collapseEtp.setDescription(applicationContext.getMessage(NAME+".collapseEtp.description", null, getLocale()));
						middleResumeLayout.addComponent(collapseEtp);
						middleResumeLayout.setComponentAlignment(collapseEtp, Alignment.MIDDLE_CENTER);
					}
					collapseResultatsS1  = new CheckBox(applicationContext.getMessage(NAME+".collapseResultatsS1.title", null, getLocale()));
					collapseResultatsS1.setValue(false);
					collapseResultatsS1.addValueChangeListener(e->{
						inscritstable.setColumnCollapsed("notes1", !collapseResultatsS1.getValue());
					});
					collapseResultatsS1.setDescription(applicationContext.getMessage(NAME+".collapseResultatsS1.description", null, getLocale()));
					middleResumeLayout.addComponent(collapseResultatsS1);
					middleResumeLayout.setComponentAlignment(collapseResultatsS1, Alignment.MIDDLE_CENTER);

					collapseResultatsS2  = new CheckBox(applicationContext.getMessage(NAME+".collapseResultatsS2.title", null, getLocale()));
					collapseResultatsS2.setValue(false);
					collapseResultatsS2.addValueChangeListener(e->{
						inscritstable.setColumnCollapsed("notes2", !collapseResultatsS2.getValue());
					});
					collapseResultatsS2.setDescription(applicationContext.getMessage(NAME+".collapseResultatsS2.description", null, getLocale()));
					middleResumeLayout.addComponent(collapseResultatsS2);
					middleResumeLayout.setComponentAlignment(collapseResultatsS2, Alignment.MIDDLE_CENTER);
					
					if(listeGroupes != null) {
						collapseGrp = new CheckBox(applicationContext.getMessage(NAME+".collapseGrp.title", null, getLocale()));
						collapseGrp.setValue(false);
						collapseGrp.addValueChangeListener(e->{
							inscritstable.setColumnCollapsed("groupe", !collapseGrp.getValue());
						});
						collapseGrp.setDescription(applicationContext.getMessage(NAME+".collapseGrp.description", null, getLocale()));
						middleResumeLayout.addComponent(collapseGrp);
						middleResumeLayout.setComponentAlignment(collapseGrp, Alignment.MIDDLE_CENTER);
					}


					resumeLayout.addComponent(middleResumeLayout);


					HorizontalLayout buttonResumeLayout = new HorizontalLayout();
					buttonResumeLayout.setSizeFull();
					buttonResumeLayout.setSpacing(true);
					//Bouton pour afficher les filtres
					btnDisplayFiltres=new Button();
					btnDisplayFiltres.setWidth("52px");
					btnDisplayFiltres.setHeight("32px");
					btnDisplayFiltres.setStyleName(ValoTheme.BUTTON_FRIENDLY);
					btnDisplayFiltres.setIcon(FontAwesome.FILTER);
					btnDisplayFiltres.setDescription(applicationContext.getMessage(NAME+".btn.displayFilters", null, getLocale()));
					btnDisplayFiltres.addClickListener(e->{
						panelFormInscrits.setContent(panelLayout);
						btnDisplayFiltres.setVisible(false);
					});
					buttonResumeLayout.addComponent(btnDisplayFiltres);
					buttonResumeLayout.setComponentAlignment(btnDisplayFiltres, Alignment.MIDDLE_RIGHT);
					buttonResumeLayout.setExpandRatio(btnDisplayFiltres, 1);
					btnDisplayFiltres.setVisible(false);

					//Bouton trombinoscope
					btnTrombi = new Button(applicationContext.getMessage(NAME+".message.trombinoscope", null, getLocale()));
					if(listeInscritsController.isPhotoProviderOperationnel()){
						btnTrombi.setIcon(FontAwesome.GROUP);
						buttonResumeLayout.addComponent(btnTrombi);


						//Gestion du clic sur le bouton trombinoscope
						btnTrombi.addClickListener(e->{

							//Si on doit afficher une fenêtre de loading pendant l'exécution
							if(PropertyUtils.isPushEnabled() &&  PropertyUtils.isShowLoadingIndicator()){
								//affichage de la pop-up de loading
								MainUI.getCurrent().startBusyIndicator();

								//Execution de la méthode en parallèle dans un thread
								executorService.execute(new Runnable() {
									public void run() {
										MainUI.getCurrent().access(new Runnable() {
											@Override
											public void run() {
												executeDisplayTrombinoscope();
												//close de la pop-up de loading
												MainUI.getCurrent().stopBusyIndicator();
											}
										} );
									}
								});

							}else{
								//On ne doit pas afficher de fenêtre de loading, on exécute directement la méthode
								executeDisplayTrombinoscope();
							}

						});
						buttonResumeLayout.setComponentAlignment(btnTrombi, Alignment.MIDDLE_RIGHT);
					}


					//Bouton de retour à l'affichage de la liste
					btnRetourListe= new Button(applicationContext.getMessage(NAME+".message.retourliste", null, getLocale()));
					btnRetourListe.setIcon(FontAwesome.BARS);
					buttonResumeLayout.addComponent(btnRetourListe);
					//if(!afficherTrombinoscope){
					btnRetourListe.setVisible(false);
					//}

					//Gestion du clic sur le bouton de  retour à l'affichage de la liste
					btnRetourListe.addClickListener(e->{
						afficherTrombinoscope = false;
						btnExportTrombi.setVisible(false);
						leftResumeLayout.replaceComponent(btnExportTrombi,btnExportExcel);
						btnTrombi.setVisible(true);
						btnRetourListe.setVisible(false);
						dataLayout.removeAllComponents();
						dataLayout.addComponent(inscritstable);
						middleResumeLayout.setVisible(true);

					});
					buttonResumeLayout.setComponentAlignment(btnRetourListe, Alignment.MIDDLE_RIGHT);

					resumeLayout.addComponent(buttonResumeLayout);

					infoLayout.addComponent(resumeLayout);

					//Layout qui contient la liste des inscrits et le trombinoscope
					dataLayout = new VerticalLayout();
					dataLayout.setSizeFull();

					//Table contenant la liste des inscrits
					inscritstable = new Table(null, new BeanItemContainer<>(Inscrit.class, linscrits));

					inscritstable.addStyleName("table-without-column-selector");
					inscritstable.setSizeFull();
					inscritstable.setVisibleColumns(new String[0]);

					String[] fields = INS_FIELDS_ELP;
					if(typeIsVet()){
						fields = INS_FIELDS_VET;
					}
					for (String fieldName : fields) {
						inscritstable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
					}


					inscritstable.addGeneratedColumn("cod_etu", new CodEtuColumnGenerator());
					inscritstable.setColumnHeader("cod_etu", applicationContext.getMessage(NAME+".table.cod_etu", null, getLocale()));
					inscritstable.addGeneratedColumn("email", new MailColumnGenerator());
					inscritstable.setColumnHeader("email", applicationContext.getMessage(NAME+".table.email", null, getLocale()));
					inscritstable.addGeneratedColumn("notes1", new Session1ColumnGenerator());
					inscritstable.setColumnHeader("notes1", applicationContext.getMessage(NAME+".table.notes1", null, getLocale()));
					inscritstable.addGeneratedColumn("notes2", new Session2ColumnGenerator());
					inscritstable.setColumnHeader("notes2", applicationContext.getMessage(NAME+".table.notes2", null, getLocale()));
					
					inscritstable.addGeneratedColumn("groupe", new GroupeColumnGenerator());
					inscritstable.setColumnHeader("groupe", applicationContext.getMessage(NAME+".table.groupe", null, getLocale()));

					//Si on est sur un ELP
					if(typeIsElp()){
						//on affiche l'étape de rattachement
						inscritstable.addGeneratedColumn("etape", new EtapeColumnGenerator());
						inscritstable.setColumnHeader("etape", applicationContext.getMessage(NAME+".table.etape", null, getLocale()));
					}

					String[] fields_to_display = INS_FIELDS_TO_DISPLAY_ELP;
					if(typeIsVet()){
						fields_to_display = INS_FIELDS_TO_DISPLAY_VET;
					}

					inscritstable.setVisibleColumns((Object[]) fields_to_display);

					inscritstable.setColumnCollapsingAllowed(true);
					inscritstable.setColumnReorderingAllowed(false);

					//On masque les colonnes de notes et groupe par défaut
					inscritstable.setColumnCollapsed("notes1", true);
					inscritstable.setColumnCollapsed("notes2", true);
					inscritstable.setColumnCollapsed("groupe", true);



					inscritstable.setSelectable(false);
					inscritstable.setImmediate(true);
					inscritstable.addStyleName("scrollabletable");
					//Si on n'a pas déjà demandé à afficher le trombinoscope
					//if(!afficherTrombinoscope){
					//la layout contient la table
					dataLayout.addComponent(inscritstable);
					//}

					//Layout contenant le gridLayout correspondant au trombinoscope
					verticalLayoutForTrombi = new VerticalLayout();
					verticalLayoutForTrombi.setSizeFull();
					verticalLayoutForTrombi.addStyleName("v-scrollablepanel");

					//Création du trombinoscope
					displayTrombinoscope();

					verticalLayoutForTrombi.addComponent(trombiLayout);
					verticalLayoutForTrombi.setSizeFull();
					verticalLayoutForTrombi.setHeight(null);

					//Si on a demandé à afficher le trombinoscope
					/*if(afficherTrombinoscope){
						//Le layout contient le trombi à afficher
						dataLayout.addComponent(verticalLayoutForTrombi);
					}*/
					infoLayout.addComponent(dataLayout);
					infoLayout.setExpandRatio(dataLayout, 1);
					addComponent(infoLayout);
					setExpandRatio(infoLayout, 1);

					//refresh de la liste à afficher au départ
					resfreshListeToDisplay();

					//Si on a demandé à afficher le trombinoscope
					if(afficherTrombinoscope){
						//On execute la procédure d'affichage du trombinoscope
						executeDisplayTrombinoscope();
					}
				}else{
					Label infoAucuninscrit = new Label(applicationContext.getMessage(NAME+".message.aucuninscrit", null, getLocale()));
					addComponent(infoAucuninscrit);
					setComponentAlignment(infoAucuninscrit, Alignment.TOP_CENTER);
					setExpandRatio(infoAucuninscrit, 1);
				}

			}
		}
	}

	//met à jour la liste des inscrit sans avoir à lever un événement
	private void resfreshListeToDisplay() {
		filtrerInscrits(getEtapeSelectionnee(),getGroupeSelectionne());
	}

	//récupère l'étape sélectionnée
	private String getEtapeSelectionnee() {
		String etapeSelectionnee = ((listeEtapes!=null && listeEtapes.getValue()!=null)?(String)listeEtapes.getValue():null);
		if(etapeSelectionnee!=null && etapeSelectionnee.equals(TOUTES_LES_ETAPES_LABEL)){
			etapeSelectionnee=null;
		}
		return etapeSelectionnee;
	}

	//récupère le groupe sélectionné
	private String getGroupeSelectionne() {
		String grpSelectionne = ((listeGroupes!=null && listeGroupes.getValue()!=null)?(String)listeGroupes.getValue():null);
		if(grpSelectionne!=null && grpSelectionne.equals(TOUS_LES_GROUPES_LABEL)){
			grpSelectionne=null;
		}
		return grpSelectionne;
	}
	private void executeDisplayTrombinoscope() {

		//On refresh le ticket de photo et on met a jour le trombinoscope avec les urls photo misent à jour
		listeInscritsController.setUrlPhotos(MainUI.getCurrent().getListeInscrits());
		displayTrombinoscope();

		afficherTrombinoscope=true;
		btnTrombi.setVisible(false);
		btnExportTrombi.setVisible(true);
		leftResumeLayout.replaceComponent(btnExportExcel, btnExportTrombi);

		//Bouton retour a la liste devient visible
		btnRetourListe.setVisible(true);
		dataLayout.removeAllComponents();
		dataLayout.addComponent(verticalLayoutForTrombi);
		dataLayout.setHeight("100%");
		verticalLayoutForTrombi.setHeight("100%");
		middleResumeLayout.setVisible(false);

	}
	/*private void majCheckbox() {
		if(typeIsElp()){
			collapseEtp.setValue(!inscritstable.isColumnCollapsed("etape"));
		}
		collapseResultatsS1.setValue(!inscritstable.isColumnCollapsed("notes1"));
		collapseResultatsS2.setValue(!inscritstable.isColumnCollapsed("notes2"));

	}*/
	private void displayTrombinoscope() {
		List<Inscrit> linscrits = MainUI.getCurrent().getListeInscrits();

		if(trombiLayout!=null){
			trombiLayout.removeAllComponents();
		}else{
			trombiLayout = new GridLayout();
			trombiLayout.setColumns(5);
			trombiLayout.setWidth("100%");
			trombiLayout.setHeight(null);
			trombiLayout.setSpacing(true);
		}


		for(Inscrit inscrit : linscrits){
			if(listecodind.contains(inscrit.getCod_ind())){
				VerticalLayout photoLayout = new VerticalLayout();
				photoLayout.setId(inscrit.getCod_ind());
				photoLayout.setHeight("100%");
				if(inscrit.getUrlphoto()!=null){
					//Button fotoEtu=new Button();
					Image fotoEtudiant = new Image(null, new ExternalResource(inscrit.getUrlphoto()));
					fotoEtudiant.setWidth("120px");
					fotoEtudiant.setStyleName(ValoTheme.BUTTON_LINK);
					fotoEtudiant.addStyleName("photo-mdw");
					fotoEtudiant.addClickListener(e->{
						rechercheController.accessToDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU,null);
					});

					photoLayout.addComponent(fotoEtudiant);
					//photoLayout.addComponent(fotoEtu);
					photoLayout.setComponentAlignment(fotoEtudiant, Alignment.MIDDLE_CENTER);
					photoLayout.setExpandRatio(fotoEtudiant, 1);

				}
				VerticalLayout nomCodeLayout = new VerticalLayout();
				nomCodeLayout.setSizeFull();
				nomCodeLayout.setSpacing(false);

				Button btnNomEtudiant = new Button(inscrit.getPrenom()+" "+inscrit.getNom());
				btnNomEtudiant.setSizeFull();
				btnNomEtudiant.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				btnNomEtudiant.addStyleName("link"); 
				btnNomEtudiant.addStyleName("v-link");
				nomCodeLayout.addComponent(btnNomEtudiant);
				btnNomEtudiant.addClickListener(e->{
					rechercheController.accessToDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU, null);
				});
				nomCodeLayout.setComponentAlignment(btnNomEtudiant, Alignment.MIDDLE_CENTER);
				//nomCodeLayout.setExpandRatio(btnNomEtudiant, 1);

				Label codetuLabel = new Label(inscrit.getCod_etu());
				codetuLabel.setSizeFull();
				codetuLabel.setStyleName(ValoTheme.LABEL_TINY);
				codetuLabel.addStyleName("label-centre");
				nomCodeLayout.addComponent(codetuLabel);	
				nomCodeLayout.setComponentAlignment(codetuLabel, Alignment.TOP_CENTER);

				photoLayout.addComponent(nomCodeLayout);

				trombiLayout.addComponent(photoLayout);
				trombiLayout.setComponentAlignment(photoLayout, Alignment.MIDDLE_CENTER);
			}
		}





	}
	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//LOG.debug("enter listeInscritsView");
	}



	/** Formats the position in a column containing Date objects. */
	@SuppressWarnings("serial")
	class Session1ColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
			Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Inscrit> bins = (BeanItem<Inscrit>) item;
			if(bins!=null){
				Inscrit i = (Inscrit) bins.getBean();
				HorizontalLayout notelayout = new HorizontalLayout();
				notelayout.setSpacing(true);
				if(StringUtils.hasText(i.getNotej())){
					Label note = new Label(i.getNotej());
					notelayout.addComponent(note);
				}
				if(StringUtils.hasText(i.getResj())){
					Label res = new Label(i.getResj());
					notelayout.addComponent(res);
				}
				return notelayout;
			}
			return null;
		}
	}

	/** Formats the position in a column containing Date objects. */
	@SuppressWarnings("serial")
	class Session2ColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
			Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Inscrit> bins = (BeanItem<Inscrit>) item;
			if(bins!=null){
				Inscrit i = (Inscrit) bins.getBean();
				HorizontalLayout notelayout = new HorizontalLayout();
				notelayout.setSpacing(true);
				if(StringUtils.hasText(i.getNotes())){
					Label note = new Label(i.getNotes());
					notelayout.addComponent(note);
				}

				if(StringUtils.hasText(i.getRess())){
					Label res = new Label(i.getRess());
					notelayout.addComponent(res);
				}
				return notelayout;
			}
			return null;
		}
	}
	
	
	/** Formats the position in a column containing Date objects. */
	@SuppressWarnings("serial")
	class GroupeColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
			Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Inscrit> bins = (BeanItem<Inscrit>) item;
			if(bins!=null){
				Inscrit i = (Inscrit) bins.getBean();
				HorizontalLayout groupelayout = new HorizontalLayout();
				groupelayout.setSpacing(true);
				if(StringUtils.hasText(i.getCodes_groupes())){
					Label groupes = new Label(Utils.getLibelleFromComboBox(i.getCodes_groupes(), listeGroupes));
					groupelayout.addComponent(groupes);
				}
				return groupelayout;
			}
			return null;
		}
	}

	/** Formats the position in a column containing Date objects. */
	@SuppressWarnings("serial")
	class MailColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
			Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Inscrit> bins = (BeanItem<Inscrit>) item;
			if(bins!=null){
				Inscrit i = (Inscrit) bins.getBean();
				HorizontalLayout maillayout = new HorizontalLayout();
				if(StringUtils.hasText(i.getEmail())){
					Label mailLabel = new Label();
					if(StringUtils.hasText(i.getEmail())){
						String mail = "<a href=\"mailto:"+i.getEmail()+"\">"+i.getEmail()+"</a>";
						mailLabel.setValue(mail);
						mailLabel.setContentMode(ContentMode.HTML);
					}

					maillayout.addComponent(mailLabel);
				}

				return maillayout;
			}
			return null;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class CodEtuColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
			Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Inscrit> bins = (BeanItem<Inscrit>) item;
			if(bins!=null){
				Inscrit i = (Inscrit) bins.getBean();
				Button btnCodEtu = new Button(i.getCod_etu());
				btnCodEtu.setStyleName("link"); 
				btnCodEtu.addStyleName("v-link");
				btnCodEtu.addClickListener(e->{
					rechercheController.accessToDetail(i.getCod_etu().toString(),Utils.TYPE_ETU, null);
				});

				return btnCodEtu;
			}
			return null;
		}
	}


	/** Formats the position in a column containing Date objects. */
	class EtapeColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
			Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Inscrit> bins = (BeanItem<Inscrit>) item;
			if(bins!=null){
				Inscrit i = (Inscrit) bins.getBean();
					VerticalLayout vets = new VerticalLayout();
					//On parcourt la liste des vets
					for(Vet ve : i.getListe_vet()) {
						// Pour chaque vet, on créé un label
						Label etapeLabel = new Label();
						if(StringUtils.hasText(ve.getLib_etp())){
							etapeLabel.setValue(ve.getLib_etp());
							if(vets.getComponentCount()>0) {
								// Ajout d'un espacement entre les vets
								etapeLabel.addStyleName("additional-vet-label");
							}
						}
						//Ajout du label au vertical layout
						vets.addComponent(etapeLabel);						
					}
					return vets;
			}
			return null;
		}
	}

	private void filtrerInscrits(String idEtape, String idgroupe) {

		LOG.debug("filtre les inscrits pour idetape:"+idEtape +" et idgroupe :"+idgroupe);
		if(inscritstable!=null){

			BeanItemContainer<Inscrit> ic = (BeanItemContainer<Inscrit>) inscritstable.getContainerDataSource();
			if(ic!=null){
				ic.removeAllContainerFilters();
				try{
					if(StringUtils.hasText(idEtape)){
						Filter filterStringToSearch =  new SimpleStringFilter("id_etp",idEtape, true, false);
						ic.addContainerFilter(filterStringToSearch);
					}

					if(StringUtils.hasText(idgroupe)){
						Filter filterStringToSearch =  new SimpleStringFilter("codes_groupes",idgroupe, true, false);
						ic.addContainerFilter(filterStringToSearch);
					}

				}catch(NullPointerException npe){
					LOG.info("NullPointerException lors du addContainerFilter sur ListeInscritView pour code : "+code+" type : "+typeFavori+ "et idEtape : "+idEtape + " et idgroupe : "+idgroupe);
				}
			}

			//Maj de la liste contenant tous les codind à afficher apres application du filtre
			refreshListeCodind(ic);

			//maj trombinoscope
			displayTrombinoscope();

			//maj de l'affichage du nombre d'étudiant
			infoNbInscrit.setValue(applicationContext.getMessage(NAME+".message.nbinscrit", null, getLocale())+ " : "+listecodind.size());
		}
	}


	private boolean typeIsVet(){
		return (typeFavori!=null && typeFavori.equals(Utils.VET));
	}

	private boolean typeIsElp(){
		return (typeFavori!=null && typeFavori.equals(Utils.ELP));
	}


	private void refreshListeCodind(BeanItemContainer<Inscrit> ic) {
		if(listecodind!=null){
			listecodind.clear();
		}else{
			listecodind = new LinkedList<String>();
		}
		for(Inscrit inscrit : ic.getItemIds()){
			listecodind.add(inscrit.getCod_ind());
		}
	}




}
