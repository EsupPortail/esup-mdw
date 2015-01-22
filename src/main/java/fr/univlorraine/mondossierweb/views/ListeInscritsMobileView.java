package fr.univlorraine.mondossierweb.views;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.jfree.util.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;


import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.utils.Utils;


/**
 * ListeInscrits sur mobile
 */
@Component @Scope("prototype")
@VaadinView(ListeInscritsMobileView.NAME)
public class ListeInscritsMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "listeInscritsMobileView";




	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheController rechercheController;


	private String code;

	private String typeFavori;
	
	private String libelleObj;
	
	private Label infoLibelleObj;
	
	//liste contenant tous les codind à afficher (apres application du filtre)
	private List<String> listecodind;
	
	private VerticalLayout infoLayout;
	
	private HorizontalLayout leftResumeLayout;
	
	private VerticalLayout dataLayout;
	
	private VerticalLayout verticalLayoutForTrombi;
	
	private VerticalLayout trombiLayout;
	
	private Button returnButton;
	
	private Button filterButton;
	

	
	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		removeAllComponents();
		


		
		
		
	//	((MdwTouchkitUI)MdwTouchkitUI.getCurrent()).checkMenuIsNotDisplayed();

		/* Style */
		//setMargin(true);
		//setSpacing(true);
		setSizeFull();
		addStyleName("v-noscrollableelement");

		code = MdwTouchkitUI.getCurrent().getCodeObjListInscrits();
		typeFavori = MdwTouchkitUI.getCurrent().getTypeObjListInscrits();
		libelleObj = "";
		if(typeIsVet() && MdwTouchkitUI.getCurrent().getEtapeListeInscrits()!=null){
			libelleObj= MdwTouchkitUI.getCurrent().getEtapeListeInscrits().getLibelle();
		}
		if(typeIsElp() && MdwTouchkitUI.getCurrent().getElpListeInscrits()!=null){
			libelleObj = MdwTouchkitUI.getCurrent().getElpListeInscrits().getLibelle();
		}
		
		
		//Récupération de la liste des inscrits
		List<Inscrit> linscrits = MdwTouchkitUI.getCurrent().getListeInscrits();

		refreshListeCodind(new BeanItemContainer<>(Inscrit.class, linscrits));
		
		//NAVBAR
		HorizontalLayout navbar=new HorizontalLayout();
		navbar.setSizeFull();
		navbar.setHeight("40px");
		navbar.setStyleName("navigation-bar");
		
		//Bouton retour
		returnButton = new Button();
		returnButton.setIcon(FontAwesome.ARROW_LEFT);
		//returnButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		returnButton.setStyleName("v-nav-button");
		navbar.addComponent(returnButton);
		navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);
		
		//Title
		Label labelTrombi = new Label(applicationContext.getMessage(NAME + ".title.label", null, getLocale()));
		labelTrombi.setStyleName("v-label-navbar");
		navbar.addComponent(labelTrombi);
		navbar.setComponentAlignment(labelTrombi, Alignment.MIDDLE_CENTER);
		
		//Bouton Filtre
		filterButton = new Button();
		filterButton.setIcon(FontAwesome.ELLIPSIS_V);
		//filterButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
		filterButton.setStyleName("v-nav-button");
		navbar.addComponent(filterButton);
		navbar.setComponentAlignment(filterButton, Alignment.MIDDLE_RIGHT);
		
		navbar.setExpandRatio(labelTrombi, 1);
		addComponent(navbar);
		
		//Test si la liste contient des étudiants
		if(linscrits!=null && linscrits.size()>0 && listecodind!=null && listecodind.size()>0){
			infoLayout= new VerticalLayout();
			infoLayout.setSizeFull();
			infoLayout.setMargin(true);
			infoLayout.setSpacing(true);
			infoLayout.addStyleName("v-scrollableelement");
			
			//Layout avec le Libelle
			HorizontalLayout resumeLayout=new HorizontalLayout();
			resumeLayout.setWidth("100%");
			resumeLayout.setHeight("20px");
			//Label affichant le nb d'inscrits
			infoLibelleObj = new Label(libelleObj);
			infoLibelleObj.setStyleName(ValoTheme.LABEL_SMALL);
			infoLibelleObj.setSizeFull();
			resumeLayout.addComponent(infoLibelleObj);
			resumeLayout.setComponentAlignment(infoLibelleObj, Alignment.TOP_CENTER);
			infoLayout.addComponent(resumeLayout);
			
			
			
			//Layout qui contient la liste des inscrits et le trombinoscope
			dataLayout = new VerticalLayout();
			dataLayout.setSizeFull();


			//Layout contenant le gridLayout correspondant au trombinoscope
			verticalLayoutForTrombi = new VerticalLayout();
			verticalLayoutForTrombi.setSizeFull();
			verticalLayoutForTrombi.addStyleName("v-scrollablepanel");

			//Création du trombinoscope
			displayTrombinoscope();

			verticalLayoutForTrombi.addComponent(trombiLayout);
			verticalLayoutForTrombi.setSizeFull();
			verticalLayoutForTrombi.setHeight(null);


			//Le layout contient le trombi à afficher
			dataLayout.addComponent(verticalLayoutForTrombi);
			
			infoLayout.addComponent(dataLayout);
			infoLayout.setExpandRatio(dataLayout, 1);
			
			addComponent(infoLayout);

			
			setExpandRatio(infoLayout, 1);
		}else{
			Label infoAucuninscrit = new Label(applicationContext.getMessage(NAME+".message.aucuninscrit", null, getLocale()));
			infoAucuninscrit.setSizeFull();
			
			addComponent(infoAucuninscrit);

			
			//setComponentAlignment(infoAucuninscrit, Alignment.TOP_CENTER);
			setExpandRatio(infoAucuninscrit, 1);
			System.out.println("Aucun inscrit");
		}
		

		
	}


	private void displayTrombinoscope() {
		List<Inscrit> linscrits = MdwTouchkitUI.getCurrent().getListeInscrits();

		if(trombiLayout!=null){
			trombiLayout.removeAllComponents();
		}else{
			trombiLayout = new VerticalLayout();
			trombiLayout.setSizeFull();
			trombiLayout.setSpacing(true);
		}


		for(Inscrit inscrit : linscrits){
			if(listecodind.contains(inscrit.getCod_ind())){
				Panel etuPanel = new Panel();
				HorizontalLayout photoLayout = new HorizontalLayout();
				photoLayout.setId(inscrit.getCod_ind());
				photoLayout.setSizeFull();
				if(inscrit.getUrlphoto()!=null){
					//Button fotoEtu=new Button();
					Image fotoEtudiant = new Image(null, new ExternalResource(inscrit.getUrlphoto()));
					fotoEtudiant.setWidth("120px");
					fotoEtudiant.setHeight("153px");
					fotoEtudiant.setStyleName(ValoTheme.BUTTON_LINK);
					fotoEtudiant.addClickListener(e->{
						rechercheController.accessToMobileDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU);
					});

					photoLayout.addComponent(fotoEtudiant);
					//photoLayout.addComponent(fotoEtu);
					//photoLayout.setComponentAlignment(fotoEtudiant, Alignment.MIDDLE_RIGHT);
					//photoLayout.setExpandRatio(fotoEtudiant, 1);

				}
				VerticalLayout nomCodeLayout = new VerticalLayout();
				nomCodeLayout.setSizeFull();
				nomCodeLayout.setSpacing(false);

				Button btnNomEtudiant = new Button(inscrit.getPrenom()+" "+inscrit.getNom());
				btnNomEtudiant.setSizeFull();
				btnNomEtudiant.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				btnNomEtudiant.addStyleName("link"); 
				btnNomEtudiant.addStyleName("v-link");
				btnNomEtudiant.addStyleName("v-button-multiline");
				nomCodeLayout.addComponent(btnNomEtudiant);
				btnNomEtudiant.addClickListener(e->{
					rechercheController.accessToMobileDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU);
				});
				nomCodeLayout.setComponentAlignment(btnNomEtudiant, Alignment.MIDDLE_CENTER);
				nomCodeLayout.setExpandRatio(btnNomEtudiant, 1);

				Label codetuLabel = new Label(inscrit.getCod_etu());
				codetuLabel.setSizeFull();
				codetuLabel.setStyleName(ValoTheme.LABEL_TINY);
				codetuLabel.addStyleName("label-centre");
				nomCodeLayout.addComponent(codetuLabel);	
				nomCodeLayout.setComponentAlignment(codetuLabel, Alignment.MIDDLE_CENTER);

				photoLayout.addComponent(nomCodeLayout);
				photoLayout.setComponentAlignment(nomCodeLayout, Alignment.MIDDLE_CENTER);

				etuPanel.setContent(photoLayout);
				trombiLayout.addComponent(etuPanel);
				trombiLayout.setComponentAlignment(etuPanel, Alignment.MIDDLE_CENTER);
			}
		}

	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//LOG.debug("enter listeInscritsMobileView");
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
