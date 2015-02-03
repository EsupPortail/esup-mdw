package fr.univlorraine.mondossierweb.views;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Resultat;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.views.windows.SignificationsMobileWindow;

/**
 * Page des notes sur mobile
 */
@Component @Scope("prototype")
@VaadinView(NotesMobileView.NAME)
public class NotesMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	private Logger LOG = LoggerFactory.getLogger(NotesMobileView.class);

	public static final String NAME = "notesMobileView";



	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient ConfigController configController;
	@Resource
	private transient RechercheController rechercheController;

	private Button returnButton;

	private Button significationButton;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

	}
	public void refresh(){
		removeAllComponents();

		/* Style */
		setMargin(false);
		setSpacing(false);
		setSizeFull();



		//NAVBAR
		HorizontalLayout navbar=new HorizontalLayout();
		navbar.setSizeFull();
		navbar.setHeight("40px");
		navbar.setStyleName("navigation-bar");

		//Bouton retour
		if(userController.isEnseignant()){
			returnButton = new Button();
			returnButton.setIcon(FontAwesome.ARROW_LEFT);
			//returnButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			returnButton.setStyleName("v-nav-button");
			returnButton.addClickListener(e->{
				MdwTouchkitUI.getCurrent().navigateToListeInscrits();
			});
			navbar.addComponent(returnButton);
			navbar.setComponentAlignment(returnButton, Alignment.MIDDLE_LEFT);
		}

		//Titre
		Label labelNavBar = new Label(MdwTouchkitUI.getCurrent().getEtudiant().getNom());
		labelNavBar.setStyleName("v-label-navbar");
		navbar.addComponent(labelNavBar);
		navbar.setComponentAlignment(labelNavBar, Alignment.MIDDLE_CENTER);

		navbar.setExpandRatio(labelNavBar, 1);

		//Significations
		if(MdwTouchkitUI.getCurrent().getEtudiant().isSignificationResultatsUtilisee()){
			significationButton = new Button();
			significationButton.setIcon(FontAwesome.INFO_CIRCLE);
			significationButton.setStyleName("v-nav-button");
			significationButton.addClickListener(e->{
				//afficher les significations
				SignificationsMobileWindow w = new SignificationsMobileWindow();
				UI.getCurrent().addWindow(w);
			});
			navbar.addComponent(significationButton);
			navbar.setComponentAlignment(significationButton, Alignment.MIDDLE_RIGHT);
		}


		addComponent(navbar);



		VerticalLayout globalLayout = new VerticalLayout();
		//globalLayout.setSizeFull();
		globalLayout.setSpacing(true);
		globalLayout.setMargin(true);
		globalLayout.setStyleName("v-scrollableelement");







		List<Etape> letapes=MdwTouchkitUI.getCurrent().getEtudiant().getEtapes();

		if(letapes!=null && letapes.size()>0){


			for(Etape etape : letapes){
				Panel panelEnCours=null;


				panelEnCours = new Panel(etape.getAnnee());
				panelEnCours.setStyleName("v-panel-caption-centertitle-panel");

				HorizontalLayout noteLayout = new HorizontalLayout();
				noteLayout.setSizeFull();
				noteLayout.setSpacing(true);
				//noteLayout.setMargin(true);
				

				VerticalLayout libelleLayout = new VerticalLayout();
				libelleLayout.setSizeFull();
				
				Button libelleButton = new Button(etape.getLibelle());
				libelleButton.setStyleName("v-button-multiline");
				libelleButton.addStyleName("link"); 
				libelleButton.addStyleName("v-link");
				libelleButton.setHeight("100%");
				libelleButton.setWidth("100%");
				
				//Appel de la window contenant le détail des notes
				prepareBoutonAppelDetailDesNotes( libelleButton, etape);
				if(etape.getResultats()!=null && etape.getResultats().size()>0){
					libelleLayout.addComponent(new Label(""));
				}
				libelleLayout.addComponent(libelleButton);
				
				
				

				HorizontalLayout notesessionLayout = new HorizontalLayout();
				notesessionLayout.setSizeFull();
				notesessionLayout.setSpacing(true);

				if(etape.getResultats()!=null && etape.getResultats().size()>0){
					int i=0;
					for(Resultat r : etape.getResultats()){
						i++;
						VerticalLayout resultatLayout = new VerticalLayout();
						resultatLayout.setSizeFull();
						Label session = new Label(r.getSession());
						session.setStyleName("label-bold-with-bottom");
						resultatLayout.addComponent(session);
						Label note = new Label(r.getNote());
						resultatLayout.addComponent(note);
						Label resultat = new Label(r.getAdmission());
						resultatLayout.addComponent(resultat);
						//Si c'est la dernière session
						if(i==etape.getResultats().size()){
							//On affiche les infos en gras
							note.setStyleName(ValoTheme.LABEL_BOLD);
							resultat.setStyleName(ValoTheme.LABEL_BOLD);
						}
						notesessionLayout.addComponent(resultatLayout);
					}
				}else{
					Label resultat = new Label("Aucun résultat");
					resultat.setStyleName(ValoTheme.LABEL_SMALL);
					notesessionLayout.addComponent(resultat);
				}

				noteLayout.addComponent(libelleLayout);


				noteLayout.addComponent(notesessionLayout);

				panelEnCours.setContent(noteLayout);
				globalLayout.addComponent(panelEnCours);
			}


		}












		addComponent(globalLayout);
		setExpandRatio(globalLayout, 1);

	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

	private void prepareBoutonAppelDetailDesNotes(Button b, Etape etape){
		//Appel de la window contenant le détail des notes
		b.addClickListener(e->{
			
			
			rechercheController.accessToMobileNotesDetail(etape);
			
			
			/*
			 DetailNotesMobileWindow dnw = new DetailNotesMobileWindow(etape); 
			UI.getCurrent().addWindow(dnw);
			
			
			//Recuperer dans la base si l'utilisateur a demandé à ne plus afficher le message
			String val  = userController.getPreference(Utils.SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE);
			boolean afficherMessage = true;
			if(StringUtils.hasText(val)){
				afficherMessage = Boolean.valueOf(val);
			}

			if(afficherMessage){
				String message =applicationContext.getMessage(NAME+".window.message.info", null, getLocale());
				HelpMobileWindow hbw = new HelpMobileWindow(message,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()));
				hbw.addCloseListener(g->{
					boolean choix = hbw.getCheckBox().getValue();
					//Test si l'utilisateur a coché la case pour ne plus afficher le message
					if(choix){
						//mettre a jour dans la base de données
						userController.updatePreference(Utils.SHOW_MESSAGE_NOTES_MOBILE_PREFERENCE, "false");
					}
				});
				UI.getCurrent().addWindow(hbw);
			}
			*/
		});
	}



}
