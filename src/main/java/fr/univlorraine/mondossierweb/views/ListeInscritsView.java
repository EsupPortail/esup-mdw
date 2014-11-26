package fr.univlorraine.mondossierweb.views;

import java.util.HashMap;
import java.util.List;
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
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.ElementPedagogique;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.Favoris;
import fr.univlorraine.mondossierweb.entities.FavorisPK;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.utils.Utils;

/**
 * Recherche arborescente
 */
@Component @Scope("prototype")
@VaadinView(ListeInscritsView.NAME)
@StyleSheet("listeInscritsView.css")
public class ListeInscritsView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "listeInscritsView";

	public static final String[] INS_FIELDS = {"nom","prenom","date_nai_ind","iae"};
	
	public static final String[] INS_FIELDS_TO_DISPLAY = {"cod_etu", "nom","prenom","date_nai_ind","email","iae","notes1","notes2"};

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

	private String code;

	private String typeFavori;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {


	}
	public void refresh() {
		//Actualiser de l'affiche du bouton de mise en favori
		if(btnTrombi !=null && favoriLayout!=null && StringUtils.hasText(code) && StringUtils.hasText(typeFavori)){

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
		/* Style */
		setMargin(true);
		setSpacing(true);

		/* Titre */
		/*Label title = new Label("Liste inscrits");
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);*/



		code = MainUI.getCurrent().getCodeObjListInscrits();
		String type = MainUI.getCurrent().getTypeObjListInscrits();
		typeFavori = "";
		String libelle = "";
		String caption = "";
		if(type.equals(Utils.TYPE_VET)){
			caption = applicationContext.getMessage(NAME+".vet.libelle", null, getLocale());
			libelle = MainUI.getCurrent().getEtapeListeInscrits().getLibelle();
			typeFavori = Utils.VET;
		}



		if(code!=null && type!=null){

			HorizontalLayout panelLayout = new HorizontalLayout();
			panelLayout.setSizeFull();

			FormLayout formInscritLayout = new FormLayout();
			formInscritLayout.setSpacing(true);
			formInscritLayout.setMargin(true);

			Panel panelFormInscrits= new Panel(code+" "+libelle);


			/*TextField elementRecherche = new TextField();
			elementRecherche.setCaption(caption);
			elementRecherche.setValue(code+" "+libelle);
			formatTextField(elementRecherche);
			formInscritLayout.addComponent(elementRecherche);*/

			List<String> lannees = MainUI.getCurrent().getListeAnneeInscrits();
			if(lannees != null && lannees.size()>0){
				NativeSelect listeAnnees = new NativeSelect();
				listeAnnees.setCaption(applicationContext.getMessage(NAME+".annee", null, getLocale()));
				listeAnnees.setNullSelectionAllowed(false);
				listeAnnees.setRequired(false);
				listeAnnees.setWidth("150px");
				for(String annee : lannees){
					listeAnnees.addItem(annee);
					int anneenplusun = Integer.parseInt(annee) + 1;
					listeAnnees.setItemCaption(annee,annee+"/"+anneenplusun);
				}
				listeAnnees.setValue( MainUI.getCurrent().getAnneeInscrits());
				listeAnnees.addValueChangeListener(new ValueChangeListener() {
					@Override
					public void valueChange(ValueChangeEvent event) {
						String selectedValue = (String) event.getProperty().getValue();

						//faire le changement
						Map<String, String> parameterMap = new HashMap<>();
						parameterMap.put("code",code);
						parameterMap.put("type",type);
						listeInscritsController.recupererLaListeDesInscrits(parameterMap, selectedValue);
						initListe();
					}
				});
				formInscritLayout.addComponent(listeAnnees);
				panelLayout.addComponent(formInscritLayout);

			}

			//Si l'objet concerné n'est pas dans les favoris
			List<Favoris> lfav = favorisController.getFavoris();
			FavorisPK favpk = new FavorisPK();
			favpk.setLogin(userController.getCurrentUserName());
			favpk.setIdfav(code);
			favpk.setTypfav(typeFavori);
			Favoris favori  = new Favoris();
			favori.setId(favpk);

			//ajout du bouton pour ajouter l'objet aux favoris
			favoriLayout = new VerticalLayout();
			favoriLayout.setSizeFull();
			favoriLayout.setMargin(true);
			favoriLayout.setSpacing(true);
			btnAjoutFavori = new Button(applicationContext.getMessage(NAME+".btn.ajoutFavori", null, getLocale()));
			btnAjoutFavori.setIcon(FontAwesome.BOOKMARK_O);
			btnAjoutFavori.addStyleName(ValoTheme.BUTTON_PRIMARY);
			btnAjoutFavori.setDescription(applicationContext.getMessage(NAME+".btn.ajoutFavori", null, getLocale()));
			btnAjoutFavori.addClickListener(e->{
				//creation du favori en base
				favorisController.saveFavori(favori);
				btnAjoutFavori.setVisible(false);
				Notification.show(applicationContext.getMessage(NAME+".message.favoriAjoute", null, getLocale()), Type.TRAY_NOTIFICATION );
			});
			favoriLayout.addComponent(btnAjoutFavori);
			favoriLayout.setComponentAlignment(btnAjoutFavori, Alignment.TOP_RIGHT);
			panelLayout.addComponent(favoriLayout);
			panelLayout.setComponentAlignment(favoriLayout, Alignment.TOP_RIGHT);

			if(lfav!=null && lfav.contains(favori)){
				btnAjoutFavori.setVisible(false);
			}

			panelFormInscrits.setContent(panelLayout);
			addComponent(panelFormInscrits);


			List<Inscrit> linscrits = MainUI.getCurrent().getListeInscrits();

			if(linscrits!=null && linscrits.size()>0){
				HorizontalLayout resumeLayout=new HorizontalLayout();
				resumeLayout.setSizeFull();
				Label infoNbInscrit = new Label(applicationContext.getMessage(NAME+".message.nbinscrit", null, getLocale())+ " : "+linscrits.size());
				resumeLayout.addComponent(infoNbInscrit);
				btnTrombi = new Button(applicationContext.getMessage(NAME+".message.trombinoscope", null, getLocale()));
				btnTrombi.setIcon(FontAwesome.GROUP);
				resumeLayout.addComponent(btnTrombi);
				btnTrombi.addClickListener(e->{
					btnTrombi.setVisible(false);
					btnRetourListe.setVisible(true);
					inscritstable.setVisible(false);
					trombiLayout.setVisible(true);
				});
				resumeLayout.setComponentAlignment(btnTrombi, Alignment.MIDDLE_RIGHT);

				btnRetourListe= new Button(applicationContext.getMessage(NAME+".message.retourliste", null, getLocale()));
				btnRetourListe.setIcon(FontAwesome.BARS);
				resumeLayout.addComponent(btnRetourListe);
				btnRetourListe.setVisible(false);
				btnRetourListe.addClickListener(e->{
					btnTrombi.setVisible(true);
					btnRetourListe.setVisible(false);
					inscritstable.setVisible(true);
					trombiLayout.setVisible(false);
				});
				resumeLayout.setComponentAlignment(btnRetourListe, Alignment.MIDDLE_RIGHT);

				addComponent(resumeLayout);



				inscritstable = new Table(null, new BeanItemContainer<>(Inscrit.class, linscrits));
				inscritstable.setWidth("100%");

				inscritstable.setVisibleColumns(new String[0]);
				for (String fieldName : INS_FIELDS) {
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
				
				inscritstable.setVisibleColumns((Object[]) INS_FIELDS_TO_DISPLAY);
				
				
				inscritstable.setColumnCollapsingAllowed(true);
				inscritstable.setColumnReorderingAllowed(false);
				inscritstable.setSelectable(false);
				inscritstable.setImmediate(true);
				addComponent(inscritstable);


				trombiLayout = new GridLayout();
				trombiLayout.setColumns(5);
				trombiLayout.setWidth("100%");
				trombiLayout.setSpacing(true);
				for(Inscrit inscrit : linscrits){
					VerticalLayout photoLayout = new VerticalLayout();
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

				trombiLayout.setVisible(false);
				addComponent(trombiLayout);
			}else{
				Label infoAucuninscrit = new Label(applicationContext.getMessage(NAME+".message.aucuninscrit", null, getLocale()));
				addComponent(infoAucuninscrit);
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
}
