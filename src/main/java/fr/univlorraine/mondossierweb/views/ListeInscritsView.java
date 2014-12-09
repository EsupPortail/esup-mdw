package fr.univlorraine.mondossierweb.views;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.data.Item;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.And;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.Favoris;
import fr.univlorraine.mondossierweb.entities.FavorisPK;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.DetailInscriptionWindow;
import fr.univlorraine.mondossierweb.views.windows.DetailGroupesWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpBasicWindow;

/**
 * Recherche arborescente
 */
@Component @Scope("prototype")
@VaadinView(ListeInscritsView.NAME)
public class ListeInscritsView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "listeInscritsView";

	public static final String[] INS_FIELDS_ELP = {"nom","prenom","date_nai_ind"};

	public static final String[] INS_FIELDS_VET = {"nom","prenom","date_nai_ind","iae"};

	public static final String[] INS_FIELDS_TO_DISPLAY_ELP = {"cod_etu","prenom","nom","date_nai_ind","email","etape","notes1","notes2"};

	public static final String[] INS_FIELDS_TO_DISPLAY_VET = {"cod_etu","prenom","nom","date_nai_ind","email","iae","notes1","notes2"};


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

	private Button btnTrombi;

	private Button btnRetourListe;

	private Table inscritstable;

	private GridLayout trombiLayout;

	private Button btnAjoutFavori;

	private VerticalLayout favoriLayout;

	private VerticalLayout verticalLayoutForTrombi;

	private String code;

	private String typeFavori;

	private VerticalLayout dataLayout;

	private boolean afficherTrombinoscope;

	private NativeSelect listeAnnees;

	private NativeSelect listeEtapes;

	private NativeSelect listeGroupes;

	//liste contenant tous les codind à afficher (apres application du filtre)
	private List<String> listecodind;

	private VerticalLayout infoLayout;

	private Label infoNbInscrit;

	private String libelleObj;

	private Panel panelFormInscrits;

	private HorizontalLayout leftResumeLayout;

	private Button btnExportTrombi;

	private Button btnExportExcel;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {


	}
	public void refresh() {
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

	public void initListe() {
		removeAllComponents();

		listeEtapes = null;
		listeGroupes = null;

		/* Style */
		setMargin(true);
		setSpacing(true);
		setSizeFull();

		code = MainUI.getCurrent().getCodeObjListInscrits();
		typeFavori = MainUI.getCurrent().getTypeObjListInscrits();
		System.out.println("typeFavori : "+typeFavori);
		libelleObj = "";
		if(typeIsVet() && MainUI.getCurrent().getEtapeListeInscrits()!=null){
			libelleObj= MainUI.getCurrent().getEtapeListeInscrits().getLibelle();
		}
		if(typeIsElp() && MainUI.getCurrent().getElpListeInscrits()!=null){
			libelleObj = MainUI.getCurrent().getElpListeInscrits().getLibelle();
		}


		if(code!=null && typeFavori!=null){

			//Panel contenant les filtres d'affichage et le bouton de mise en favori
			HorizontalLayout panelLayout = new HorizontalLayout();
			panelLayout.setSizeFull();

			FormLayout formInscritLayout = new FormLayout();
			formInscritLayout.setSpacing(true);
			formInscritLayout.setMargin(true);

			panelFormInscrits= new Panel(code+" "+libelleObj);

			//Affichage d'une liste déroulante contenant la liste des années
			List<String> lannees = MainUI.getCurrent().getListeAnneeInscrits();
			if(lannees != null && lannees.size()>0){
				listeAnnees = new NativeSelect();
				listeAnnees.setCaption(applicationContext.getMessage(NAME+".annee", null, getLocale()));
				listeAnnees.setNullSelectionAllowed(false);
				listeAnnees.setRequired(false);
				listeAnnees.setWidth("150px");
				for(String annee : lannees){
					listeAnnees.addItem(annee);
					int anneenplusun = Integer.parseInt(annee) + 1;
					listeAnnees.setItemCaption(annee,annee+"/"+anneenplusun);
				}
				System.out.println("MainUI.getCurrent().getAnneeInscrits() : "+MainUI.getCurrent().getAnneeInscrits());
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
							listeInscritsController.recupererLaListeDesInscrits(parameterMap, selectedValue);
						}
						if(typeIsElp()){
							listeInscritsController.recupererLaListeDesInscritsELP(parameterMap, selectedValue);
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
					listeEtapes = new NativeSelect();
					listeEtapes.setCaption(applicationContext.getMessage(NAME+".etapes", null, getLocale()));
					listeEtapes.setNullSelectionAllowed(false);
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
							System.out.println("vet selectionnee : "+vetSelectionnee);
							if(vetSelectionnee.equals(TOUTES_LES_ETAPES_LABEL)){
								vetSelectionnee = null;
							}
							MainUI.getCurrent().setEtapeInscrits(vetSelectionnee);
							//faire le changement
							//Map<String, String> parameterMap = new HashMap<>();
							//parameterMap.put("code",code);
							//parameterMap.put("type",typeFavori);
							//récupération de la nouvelle liste
							//filtrer la liste par etape
							//listeInscritsController.recupererLaListeDesInscritsELP(parameterMap, ((listeAnnees!=null && listeAnnees.getValue()!=null)? (String) listeAnnees.getValue():null) ,selectedVet, ((listeGroupes!=null && listeGroupes.getValue()!=null) ? (String) listeGroupes.getValue():null));
							String groupeSelectionne = ((listeGroupes!=null && listeGroupes.getValue()!=null)?(String)listeGroupes.getValue():null);
							if(groupeSelectionne!=null && groupeSelectionne.equals(TOUS_LES_GROUPES_LABEL)){
								groupeSelectionne=null;
							}
							filtrerInscrits(vetSelectionnee, groupeSelectionne);
							//update de l'affichage
							//initListe();

						}
					});
					formInscritLayout.addComponent(listeEtapes);

				}

				List<ElpDeCollection> lgroupes = MainUI.getCurrent().getListeGroupesInscrits();
				if(lgroupes != null && lgroupes.size()>0){
					listeGroupes = new NativeSelect();
					//listeGroupes.setCaption(applicationContext.getMessage(NAME+".groupes", null, getLocale()));
					listeGroupes.setNullSelectionAllowed(false);
					listeGroupes.setRequired(false);
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
							//Map<String, String> parameterMap = new HashMap<>();
							//parameterMap.put("code",code);
							//parameterMap.put("type",typeFavori);
							//récupération de la nouvelle liste
							//filtrer la liste par etape
							//listeInscritsController.recupererLaListeDesInscritsELP(parameterMap, ((listeAnnees!=null && listeAnnees.getValue()!=null)? (String) listeAnnees.getValue():null) ,selectedVet, ((listeGroupes!=null && listeGroupes.getValue()!=null) ? (String) listeGroupes.getValue():null));
							String etapeSelectionnee = ((listeEtapes!=null && listeEtapes.getValue()!=null)?(String)listeEtapes.getValue():null);
							if(etapeSelectionnee!=null && etapeSelectionnee.equals(TOUTES_LES_ETAPES_LABEL)){
								etapeSelectionnee=null;
							}
							filtrerInscrits(etapeSelectionnee,grpSelectionnee);
							//update de l'affichage
							//initListe();
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
						DetailGroupesWindow dgw = new DetailGroupesWindow(lgroupes, panelFormInscrits.getCaption(), vet, (String)listeAnnees.getValue()); 
						UI.getCurrent().addWindow(dgw);
					});
					gpLayout.addComponent(btnDetailGpe);

					formInscritLayout.addComponent(gpLayout);

					//formInscritLayout.addComponent(listeGroupes);
				}
			}
			panelLayout.addComponent(formInscritLayout);


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
			List<Inscrit> linscrits = MainUI.getCurrent().getListeInscrits();

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
					
					HelpBasicWindow hbw = new HelpBasicWindow(message,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()));
					UI.getCurrent().addWindow(hbw);
				});
				leftResumeLayout.addComponent(infoDescriptionButton);
				leftResumeLayout.setComponentAlignment(infoDescriptionButton, Alignment.MIDDLE_LEFT);


				//Bouton export trombinoscope
				btnExportTrombi=new Button();
				btnExportTrombi.setIcon(FontAwesome.FILE_PDF_O);
				btnExportTrombi.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				btnExportTrombi.setDescription(applicationContext.getMessage(NAME + ".pdf.trombinoscope.link", null, getLocale()));

				/*btnExportTrombi.addClickListener(e->{
					fd.setFileDownloadResource(listeInscritsController.exportPdf(linscrits, listecodind,panelFormInscrits.getCaption(),  (String)listeAnnees.getValue()));

				});*/

				//methode qui permet de generer l'export à la demande
				//Création du nom du fichier
				String nomFichier = applicationContext.getMessage("pdf.trombinoscope.title", null, Locale.getDefault())+"_" + panelFormInscrits.getCaption() +  ".pdf";
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
				resource.setMIMEType("application/pdf");
				resource.setCacheTime(0);
				//On ajoute le FD sur le bouton d'export
				new FileDownloader(resource).extend(btnExportTrombi);

				leftResumeLayout.addComponent(btnExportTrombi);
				leftResumeLayout.setComponentAlignment(btnExportTrombi, Alignment.MIDDLE_LEFT);
				if(!afficherTrombinoscope){
					//On cache le bouton d'export pdf
					btnExportTrombi.setVisible(false);
				}


				//Bouton export liste excel
				btnExportExcel=new Button();
				btnExportExcel.setIcon(FontAwesome.FILE_EXCEL_O);
				btnExportExcel.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				btnExportExcel.setDescription(applicationContext.getMessage(NAME + ".excel.link", null, getLocale()));
				String nomFichierXls = applicationContext.getMessage("excel.listeinscrits.title", null, Locale.getDefault())+"_" + panelFormInscrits.getCaption() +  ".xls";
				StreamResource resourceXls = new StreamResource(new StreamResource.StreamSource() {
					@Override
					public InputStream getStream() {
						//recuperation de l'année sélectionnée et du libellé de l'ELP
						String annee=(String)listeAnnees.getValue();
						String libObj=panelFormInscrits.getCaption();
						//création du trombi en pdf
						return listeInscritsController.getXlsStream(linscrits, listecodind,libObj,  annee, typeFavori);
					}
				}, nomFichierXls);
				resourceXls.setMIMEType("application/xls");
				resourceXls.setCacheTime(0);
				//On ajoute le FD sur le bouton d'export
				new FileDownloader(resourceXls).extend(btnExportExcel);
				if(!afficherTrombinoscope){
					//On échange le bouton d'export pdf par le bouton export excel
					leftResumeLayout.replaceComponent(btnExportTrombi, btnExportExcel);
				}


				resumeLayout.addComponent(leftResumeLayout);



				//Bouton trombinoscope
				btnTrombi = new Button(applicationContext.getMessage(NAME+".message.trombinoscope", null, getLocale()));
				btnTrombi.setIcon(FontAwesome.GROUP);
				resumeLayout.addComponent(btnTrombi);

				//Test si trombinoscope est affiché
				if(afficherTrombinoscope){
					//On masque le bouton trombinoscope
					btnTrombi.setVisible(false);
				}
				//Gestion du clic sur le bouton trombinoscope
				btnTrombi.addClickListener(e->{
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
				});
				resumeLayout.setComponentAlignment(btnTrombi, Alignment.MIDDLE_RIGHT);

				//Bouton de retour à l'affichage de la liste
				btnRetourListe= new Button(applicationContext.getMessage(NAME+".message.retourliste", null, getLocale()));
				btnRetourListe.setIcon(FontAwesome.BARS);
				resumeLayout.addComponent(btnRetourListe);
				if(!afficherTrombinoscope){
					btnRetourListe.setVisible(false);
				}
				//Gestion du clic sur le bouton de  retour à l'affichage de la liste
				btnRetourListe.addClickListener(e->{
					afficherTrombinoscope = false;
					btnExportTrombi.setVisible(false);
					leftResumeLayout.replaceComponent(btnExportTrombi,btnExportExcel);
					btnTrombi.setVisible(true);
					btnRetourListe.setVisible(false);
					dataLayout.removeAllComponents();
					dataLayout.addComponent(inscritstable);

				});
				resumeLayout.setComponentAlignment(btnRetourListe, Alignment.MIDDLE_RIGHT);

				infoLayout.addComponent(resumeLayout);

				//Layout qui contient la liste des inscrits et le trombinoscope
				dataLayout = new VerticalLayout();
				dataLayout.setSizeFull();

				//Table contenant la liste des inscrits
				inscritstable = new Table(null, new BeanItemContainer<>(Inscrit.class, linscrits));

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
				inscritstable.setSelectable(false);
				inscritstable.setImmediate(true);
				inscritstable.addStyleName("scrollabletable");
				//Si on n'a pas déjà demandé à afficher le trombinoscope
				if(!afficherTrombinoscope){
					//la layout contient la table
					dataLayout.addComponent(inscritstable);
				}

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
				if(afficherTrombinoscope){
					//Le layout contient le trombi à afficher
					dataLayout.addComponent(verticalLayoutForTrombi);
				}
				infoLayout.addComponent(dataLayout);
				infoLayout.setExpandRatio(dataLayout, 1);
				addComponent(infoLayout);
				setExpandRatio(infoLayout, 1);
			}else{
				Label infoAucuninscrit = new Label(applicationContext.getMessage(NAME+".message.aucuninscrit", null, getLocale()));
				addComponent(infoAucuninscrit);
				setComponentAlignment(infoAucuninscrit, Alignment.TOP_CENTER);
				setExpandRatio(infoAucuninscrit, 1);
			}

		}

	}

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
					fotoEtudiant.setHeight("153px");
					fotoEtudiant.setStyleName(ValoTheme.BUTTON_LINK);
					fotoEtudiant.addClickListener(e->{
						rechercheController.accessToDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU);
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
					rechercheController.accessToDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU);
				});
				nomCodeLayout.setComponentAlignment(btnNomEtudiant, Alignment.MIDDLE_CENTER);
				nomCodeLayout.setExpandRatio(btnNomEtudiant, 1);

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
		//System.out.println("enter");
	}

	private void formatTextField(TextField tf){
		tf.setEnabled(false);
		tf.setSizeFull();
		tf.setNullRepresentation("");
		tf.addStyleName(ValoTheme.TEXTFIELD_BORDERLESS);
	}


	/** Formats the position in a column containing Date objects. */
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
	}

	/** Formats the position in a column containing Date objects. */
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
	}

	/** Formats the position in a column containing Date objects. */
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
			Inscrit i = (Inscrit) bins.getBean();
			Button btnCodEtu = new Button(i.getCod_etu());
			btnCodEtu.setStyleName("link"); 
			btnCodEtu.addStyleName("v-link");
			btnCodEtu.addClickListener(e->{
				rechercheController.accessToDetail(i.getCod_etu().toString(),Utils.TYPE_ETU);
			});

			return btnCodEtu;
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
			Inscrit i = (Inscrit) bins.getBean();
			Label etapeLabel = new Label();
			if(StringUtils.hasText(i.getId_etp())){
				etapeLabel.setValue(i.getId_etp());
			}
			return etapeLabel;
		}
	}

	private void filtrerInscrits(String idEtape, String idgroupe) {
		System.out.println(idEtape+" "+idgroupe);
		if(inscritstable!=null){

			BeanItemContainer<Inscrit> ic = (BeanItemContainer<Inscrit>) inscritstable.getContainerDataSource();
			if(ic!=null){
				ic.removeAllContainerFilters();

				if(StringUtils.hasText(idEtape)){
					Filter filterStringToSearch =  new SimpleStringFilter("id_etp",idEtape, true, false);
					//Test du filtre : 
					//Filter filterStringToSearch =  new SimpleStringFilter("nom","AN", true, false);
					ic.addContainerFilter(filterStringToSearch);
				}

				if(StringUtils.hasText(idgroupe)){
					Filter filterStringToSearch =  new SimpleStringFilter("codes_groupes",idgroupe, true, false);
					ic.addContainerFilter(filterStringToSearch);
				}

			}

			System.out.println("nb  item valides : "+ic.getItemIds().size());

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
