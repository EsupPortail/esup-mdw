package fr.univlorraine.mondossierweb.views;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import lombok.Getter;

import org.jfree.util.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.FiltreInscritsMobileWindow;
import groovy.util.slurpersupport.GPathResult;


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

	private VerticalLayout infoLayout;

	private HorizontalLayout leftResumeLayout;

	private VerticalLayout dataLayout;

	private VerticalLayout verticalLayoutForTrombi;

	private VerticalLayout trombiLayout;

	private Button returnButton;

	private Button filterButton;


	private String vetSelectionnee;


	private String groupeSelectionne;



	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		// On réinitialise la vue
		removeAllComponents();
		
		// Style
		setSizeFull();
		addStyleName("v-noscrollableelement");

		// On récupère le favori à afficher
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



		//NAVBAR
		HorizontalLayout navbar=new HorizontalLayout();
		navbar.setSizeFull();
		navbar.setHeight("40px");
		navbar.setStyleName("navigation-bar");

		//Bouton retour
		returnButton = new Button();
		returnButton.setIcon(FontAwesome.ARROW_LEFT);
		returnButton.setStyleName("v-nav-button");
		returnButton.addClickListener(e->{
			if(MdwTouchkitUI.getCurrent().getTrombinoscopeFromView()!=null &&
					MdwTouchkitUI.getCurrent().getTrombinoscopeFromView().equals(FavorisMobileView.NAME)){
				MdwTouchkitUI.getCurrent().navigateTofavoris();
			}
			if(MdwTouchkitUI.getCurrent().getTrombinoscopeFromView()!=null &&
					MdwTouchkitUI.getCurrent().getTrombinoscopeFromView().equals(RechercheMobileView.NAME)){
				MdwTouchkitUI.getCurrent().navigateToRecherche();
			}
		});
		navbar.addComponent(returnButton);
		navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);

		//Title
		Label labelTrombi = new Label(applicationContext.getMessage(NAME + ".title.label", null, getLocale()));
		labelTrombi.setStyleName("v-label-navbar");
		navbar.addComponent(labelTrombi);
		navbar.setComponentAlignment(labelTrombi, Alignment.MIDDLE_CENTER);

		//Bouton Filtre
		//On a la possibilité de filtrer le trombinoscope que si on est positionné sur un ELP
		if(typeIsElp()){
			filterButton = new Button();
			filterButton.setIcon(FontAwesome.ELLIPSIS_V);
			filterButton.setStyleName("v-nav-button");
			filterButton.addClickListener(e->{
				FiltreInscritsMobileWindow w = new FiltreInscritsMobileWindow();
				w.addCloseListener(f->{
					//Si la personne a fermé la popup en appuyant sur le bouton FILTRER
					if(w.isDemandeFiltrage()){
						vetSelectionnee = w.getVetSelectionnee();
						groupeSelectionne = w.getGroupeSelectionne();
						displayTrombinoscope();
					}
				});
				UI.getCurrent().addWindow(w);

			});
			navbar.addComponent(filterButton);
			navbar.setComponentAlignment(filterButton, Alignment.MIDDLE_RIGHT);
		}

		navbar.setExpandRatio(labelTrombi, 1);
		addComponent(navbar);

		//Test si la liste contient des étudiants
		if(linscrits!=null && linscrits.size()>0){
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
			// Layout contenant le label du message indiquant aucun inscrit
			infoLayout= new VerticalLayout();
			infoLayout.setMargin(true);
			infoLayout.setSpacing(true);

			// Label du message indiquant aucun inscrit
			Label infoAucuninscrit = new Label(applicationContext.getMessage(NAME+".message.aucuninscrit", null, getLocale()));
			infoAucuninscrit.setSizeFull();

			// Ajout du label au layout
			infoLayout.addComponent(infoAucuninscrit);
			addComponent(infoLayout);
			setExpandRatio(infoLayout, 1);
		}



	}

	
	/**
	 * Affichage du trombinoscope
	 */
	private void displayTrombinoscope() {
		// Récupération de la liste des inscrits
		List<Inscrit> linscrits = MdwTouchkitUI.getCurrent().getListeInscrits();

		// On réinitialise le layout contenant le trombinoscope
		if(trombiLayout!=null){
			trombiLayout.removeAllComponents();
		}else{
			trombiLayout = new VerticalLayout();
			trombiLayout.setSizeFull();
			trombiLayout.setSpacing(true);
		}

		//Pour chaque inscrit
		for(Inscrit inscrit : linscrits){

			
			boolean afficherEtudiant= true;

			//Si l'étudiant n'est pas dans la VET sélectionnée, on ne l'affiche pas
			if(StringUtils.hasText(vetSelectionnee) && (inscrit.getId_etp()==null || !inscrit.getId_etp().contains(vetSelectionnee))){
				afficherEtudiant=false;

			}
			
			// Si l'étudiant n'est pas dans le groupe sélectionné, on ne l'affiche pas
			if(StringUtils.hasText(groupeSelectionne) && (inscrit.getCodes_groupes()==null ||!inscrit.getCodes_groupes().contains(groupeSelectionne))){
				afficherEtudiant=false;
			}

			
			// Si l'étudiant doit être affiché
			if(afficherEtudiant){
				
				// Panel contenant l'étudiant
				Panel etuPanel = new Panel();
				
				// Layout du Panel contenant l'étudiant
				HorizontalLayout photoLayout = new HorizontalLayout();
				// Ajout d'un id sur le layout
				photoLayout.setId(inscrit.getCod_ind());
				photoLayout.setSizeFull();
				
				// Si on a une url renseignée vers la photo de l'étudiant
				if(inscrit.getUrlphoto()!=null){

					// Image contenant la photo de l'étudiant
					Image fotoEtudiant = new Image(null, new ExternalResource(inscrit.getUrlphoto()));
					fotoEtudiant.setWidth("120px");
					fotoEtudiant.setHeight("153px");
					fotoEtudiant.setStyleName(ValoTheme.BUTTON_LINK);
					// Gestion du clic sur la photo
					fotoEtudiant.addClickListener(e->{
						// Au clic sur la photo on redirige vers le contenu du dossier de l'étudiant dont la photo a été cliquée
						rechercheController.accessToMobileDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU,false);
					});
					// Ajout de la photo au layout
					photoLayout.addComponent(fotoEtudiant);

				}
				
				// Layout contenant le nom, prénom et le codetu
				VerticalLayout nomCodeLayout = new VerticalLayout();
				nomCodeLayout.setSizeFull();
				nomCodeLayout.setSpacing(false);

				// Bouton contenant le nom/prénom
				Button btnNomEtudiant = new Button(inscrit.getPrenom()+" "+inscrit.getNom());
				btnNomEtudiant.setSizeFull();
				btnNomEtudiant.setStyleName(ValoTheme.BUTTON_BORDERLESS);
				btnNomEtudiant.addStyleName("link"); 
				btnNomEtudiant.addStyleName("v-link");
				btnNomEtudiant.addStyleName("v-button-multiline");
				// Ajout du bouton au layout
				nomCodeLayout.addComponent(btnNomEtudiant);
				//Gestion du clic sur le bouton
				btnNomEtudiant.addClickListener(e->{
					// Au clic sur le bouton on redirige vers le contenu du dossier de l'étudiant dont le nom a été cliqué
					rechercheController.accessToMobileDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU,false);
				});
				nomCodeLayout.setComponentAlignment(btnNomEtudiant, Alignment.MIDDLE_CENTER);
				nomCodeLayout.setExpandRatio(btnNomEtudiant, 1);

				// Label contenant le codetu
				Label codetuLabel = new Label(inscrit.getCod_etu());
				codetuLabel.setSizeFull();
				codetuLabel.setStyleName(ValoTheme.LABEL_TINY);
				codetuLabel.addStyleName("label-centre");
				// Ajout du label au layout
				nomCodeLayout.addComponent(codetuLabel);	
				nomCodeLayout.setComponentAlignment(codetuLabel, Alignment.MIDDLE_CENTER);

				// Ajout du layout contenant nom, prénom et codetu au layout de la photo
				photoLayout.addComponent(nomCodeLayout);
				photoLayout.setComponentAlignment(nomCodeLayout, Alignment.MIDDLE_CENTER);

				// Ajout du layout de la photo comme contenu du panel
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



}
