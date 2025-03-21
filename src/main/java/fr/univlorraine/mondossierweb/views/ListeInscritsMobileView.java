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
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.utils.CssUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.FiltreInscritsMobileWindow;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;


/**
 * ListeInscrits sur mobile
 */
@Component @Scope("prototype")
@SpringView(name = ListeInscritsMobileView.NAME)
//@PreAuthorize("@userController.hasRoleInProperty('teacher')")
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
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient ListeInscritsController listeInscritsController;
	@Resource
	private transient ObjectFactory<FiltreInscritsMobileWindow> filtreInscritsMobileWindowFactory;

	private String typeFavori;

	private String libelleObj;

	private Label infoLibelleObj;

	private VerticalLayout infoLayout;

	private VerticalLayout dataLayout;

	private VerticalLayout verticalLayoutForTrombi;

	private VerticalLayout trombiLayout;

	private Button returnButton;

	private Button filterButton;


	private String vetSelectionnee;


	private String groupeSelectionne;

	private int pageEnCours;

	private int pageMax;

	private int nbEtuParPage;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI && userController.isEnseignant()){
			// On réinitialise la vue
			removeAllComponents();

			// Style
			setSizeFull();
			addStyleName("v-noscrollableelement");

			// On récupère le favori à afficher
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

			//Récupération du nombre d'inscrit par page
			nbEtuParPage=configController.getTrombiMobileNbEtuParPage();

			//NAVBAR
			HorizontalLayout navbar=new HorizontalLayout();
			navbar.setSizeFull();
			navbar.setHeight(CssUtils.NAVBAR_HEIGHT);
			navbar.setStyleName("navigation-bar");

			//Bouton retour
			returnButton = new Button();
			returnButton.setIcon(FontAwesome.ARROW_LEFT);
			returnButton.setStyleName("v-menu-nav-button");
			returnButton.addClickListener(e->{
				if(MdwTouchkitUI.getCurrent().getTrombinoscopeFromView()!=null &&
						MdwTouchkitUI.getCurrent().getTrombinoscopeFromView().equals(FavorisMobileView.NAME)){
					MdwTouchkitUI.getCurrent().navigateTofavoris();
				}
				if(MdwTouchkitUI.getCurrent().getTrombinoscopeFromView()!=null &&
						MdwTouchkitUI.getCurrent().getTrombinoscopeFromView().equals(RechercheMobileView.NAME)){
					MdwTouchkitUI.getCurrent().navigateToRecherche(null);
				}
			});
			navbar.addComponent(returnButton);
			navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);

			//Title
			Label labelTrombi = new Label(applicationContext.getMessage(NAME + ".title.label", null, getLocale()));
			labelTrombi.setStyleName("v-label-navbar");
			navbar.addComponent(labelTrombi);
			navbar.setComponentAlignment(labelTrombi, Alignment.MIDDLE_CENTER);

			// bouton pour ajouter en favori si ce n'est pas déjà le cas
			List<Favoris> lfav = favorisController.getFavoris();
			FavorisPK favpk = new FavorisPK();
			favpk.setLogin(userController.getCurrentUserName());
			favpk.setIdfav( MdwTouchkitUI.getCurrent().getCodeObjListInscrits());
			favpk.setTypfav(typeFavori);
			Favoris favori  = new Favoris();
			favori.setId(favpk);

			//Si l'objet n'est pas déjà en favori
			if(lfav!=null && !lfav.contains(favori)){
				//Création du bouton pour ajouter l'objet aux favoris
				Button btnAjoutFavori = new Button();
				btnAjoutFavori.setIcon(FontAwesome.STAR_O);
				btnAjoutFavori.setStyleName("v-menu-nav-button");
				btnAjoutFavori.setHeight("100%");
				btnAjoutFavori.addClickListener(e->{
					//creation du favori en base sur le clic du bouton
					favorisController.saveFavori(favori);

					//On cache le bouton de mise en favori
					btnAjoutFavori.setVisible(false);

					//Affichage d'un message de confirmation
					Notification.show(applicationContext.getMessage(NAME+".message.favoriAjoute", null, getLocale()), Notification.Type.TRAY_NOTIFICATION );
				});

				//Ajout du bouton à l'interface
				navbar.addComponent(btnAjoutFavori);
			}

			//Bouton Filtre
			//On a la possibilité de filtrer le trombinoscope que si on est positionné sur un ELP
			if(typeIsElp()){
				FiltreInscritsMobileWindow w = filtreInscritsMobileWindowFactory.getObject();
				w.init();
				//Si on a des éléments à afficher dans la popup filtre
				if((w.getListeEtapes()!=null && !w.getListeEtapes().isEmpty()) || (w.getListeGroupes()!=null && !w.getListeGroupes().isEmpty())){
					filterButton = new Button();
					filterButton.setIcon(FontAwesome.FILTER);
					filterButton.setStyleName("v-menu-nav-button");

					filterButton.addClickListener(e->{
						w.addCloseListener(f->{
							//Si la personne a fermé la popup en appuyant sur le bouton FILTRER
							if(w.isDemandeFiltrage()){
								vetSelectionnee = w.getVetSelectionnee();
								groupeSelectionne = w.getGroupeSelectionne();
								displayTrombinoscope(false);
							}
						});
						UI.getCurrent().addWindow(w);
					});
					navbar.addComponent(filterButton);
					navbar.setComponentAlignment(filterButton, Alignment.MIDDLE_RIGHT);
				}
			}

			navbar.setExpandRatio(labelTrombi, 1);
			addComponent(navbar);

			//Test si la liste contient des étudiants
			if(linscrits!=null && !linscrits.isEmpty()){
				pageEnCours=1;
				//Calcul du nombre maxi de page
				if(nbEtuParPage>0 && linscrits.size()>nbEtuParPage){
					pageMax=(linscrits.size()/nbEtuParPage) + 1 ;
				}else{
					//On affiche tous les inscrits sur une seule page
					pageMax = 1;
				}

				infoLayout= new VerticalLayout();
				infoLayout.setSizeFull();
				infoLayout.setMargin(true);
				infoLayout.setSpacing(true);
				infoLayout.addStyleName("v-scrollableelement");

				//Layout avec le Libelle
				HorizontalLayout resumeLayout=new HorizontalLayout();
				resumeLayout.setWidth("100%");
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
				displayTrombinoscope(false);

				verticalLayoutForTrombi.addComponent(trombiLayout);
				verticalLayoutForTrombi.setSizeFull();
				verticalLayoutForTrombi.setHeight(null);

				if(pageMax > 1){
					HorizontalLayout layoutPagination = new HorizontalLayout();
					layoutPagination.setWidth("100%");
					layoutPagination.setMargin(true);
					if(pageEnCours<pageMax){
						Button btnNext= new Button(applicationContext.getMessage(NAME+".btn.affichersuite", null, getLocale()));
						btnNext.setStyleName(ValoTheme.BUTTON_PRIMARY);
						btnNext.addStyleName("v-popover-button");
						btnNext.addClickListener(e->{
							pageEnCours++;
							displayTrombinoscope(true);
							if(pageEnCours>=pageMax){
								btnNext.setVisible(false);
							}
						});
						layoutPagination.addComponent(btnNext);
						layoutPagination.setComponentAlignment(btnNext, Alignment.MIDDLE_CENTER);
						layoutPagination.setExpandRatio(btnNext, 1);
					}
					verticalLayoutForTrombi.addComponent(layoutPagination);
				}

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

	}


	/**
	 * Affichage du trombinoscope
	 */
	private void displayTrombinoscope(boolean completion) {
		// Récupération de la liste des inscrits
		List<Inscrit> linscrits = MdwTouchkitUI.getCurrent().getListeInscrits();

		// On réinitialise le layout contenant le trombinoscope
		if(trombiLayout!=null){
			//On n'a pas fait 'afficher suivant'
			if(!completion){
				trombiLayout.removeAllComponents();
			}
		}else{
			trombiLayout = new VerticalLayout();
			trombiLayout.setSizeFull();
			trombiLayout.setSpacing(true);
		}

		int compteurEtu=0;
		//Pour chaque inscrit
		for(Inscrit inscrit : linscrits){

			compteurEtu++;
			//Si on affiche tout sur une page ou si l'étudiant doit être affiché sur cette page
			if(pageMax==1 || ((((pageEnCours-1) * nbEtuParPage) < compteurEtu) && ( compteurEtu <= ((pageEnCours) * nbEtuParPage)) )){

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
						// On met à jour l'url de la photo, des fois que le ticket ait expiré entre temps
						inscrit.setUrlphoto(listeInscritsController.getUrlPhoto(inscrit));

						// Image contenant la photo de l'étudiant
						Image fotoEtudiant = new Image(null, new ExternalResource(inscrit.getUrlphoto()));
						fotoEtudiant.setWidth("120px");
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
					//nomCodeLayout.setSizeFull();
					nomCodeLayout.setSpacing(false);

					// Bouton contenant le nom/prénom
					Button btnNomEtudiant = new Button(inscrit.getPrenom()+" "+inscrit.getNom());
					Utils.setButtonStyle(btnNomEtudiant);
					btnNomEtudiant.addStyleName("text-size-medium");

					// Ajout du bouton au layout
					nomCodeLayout.addComponent(btnNomEtudiant);
					//Gestion du clic sur le bouton
					btnNomEtudiant.addClickListener(e->{
						// Au clic sur le bouton on redirige vers le contenu du dossier de l'étudiant dont le nom a été cliqué
						rechercheController.accessToMobileDetail(inscrit.getCod_etu().toString(),Utils.TYPE_ETU,false);
					});
					nomCodeLayout.setComponentAlignment(btnNomEtudiant, Alignment.MIDDLE_CENTER);
					//nomCodeLayout.setExpandRatio(btnNomEtudiant, 1);

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
					photoLayout.setExpandRatio(nomCodeLayout, 1);

					// Ajout du layout de la photo comme contenu du panel
					etuPanel.setContent(photoLayout);
					trombiLayout.addComponent(etuPanel);
					trombiLayout.setComponentAlignment(etuPanel, Alignment.MIDDLE_CENTER);
				}
			}
		}

	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
		//LOG.debug("enter listeInscritsMobileView");
	}

	private boolean typeIsVet(){
		return (typeFavori!=null && typeFavori.equals(Utils.VET));
	}

	private boolean typeIsElp(){
		return (typeFavori!=null && typeFavori.equals(Utils.ELP));
	}



}
