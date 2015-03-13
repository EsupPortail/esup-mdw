package fr.univlorraine.mondossierweb.views;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Diplome;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Resultat;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.NoteController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.utils.MyFileDownloader;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.windows.DetailNotesWindow;
import fr.univlorraine.mondossierweb.views.windows.HelpWindow;

/**
 * Page de gestion des structures
 */
@Component @Scope("prototype")
@VaadinView(NotesView.NAME)
@PreAuthorize("hasRole('consultation_dossier')")
public class NotesView extends VerticalLayout implements View {

	private static final long serialVersionUID = -6491779626961549383L;

	private Logger LOG = LoggerFactory.getLogger(NotesView.class);

	public static final String NAME = "notesView";

	public static final String[] DIPLOMES_FIELDS_ORDER = {"annee", "cod_dip","lib_web_vdi"};

	public static final String[] ETAPES_FIELDS_ORDER = {"annee"};

	private boolean vueEnseignant;

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient NoteController noteController;
	@Resource
	private transient ConfigController configController;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if((userController.isEnseignant() || userController.isEtudiant()) && MainUI.getCurrent()!=null && MainUI.getCurrent().getEtudiant()!=null){

			LOG.debug(userController.getCurrentUserName()+" NotesView");

			removeAllComponents();
			/* Style */
			setMargin(true);
			setSpacing(true);

			//Test si user enseignant et en vue Enseignant
			if(userController.isEnseignant() && MainUI.getCurrent().isVueEnseignantNotesEtResultats()){
				//On recupere les notes pour un enseignant
				etudiantController.renseigneNotesEtResultatsVueEnseignant(MainUI.getCurrent().getEtudiant());
			}else{
				//On récupère les notes pour un étudiant
				etudiantController.renseigneNotesEtResultats(MainUI.getCurrent().getEtudiant());
			}


			/* Titre */
			HorizontalLayout titleLayout = new HorizontalLayout();
			titleLayout.setWidth("100%");
			Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
			title.addStyleName(ValoTheme.LABEL_H1);
			titleLayout.addComponent(title);
			titleLayout.setComponentAlignment(title,Alignment.MIDDLE_LEFT);
			//Test si on a des diplomes ou des etapes
			if((MainUI.getCurrent().getEtudiant().getDiplomes()!=null && MainUI.getCurrent().getEtudiant().getDiplomes().size()>0)||
					(MainUI.getCurrent().getEtudiant().getEtapes()!=null && MainUI.getCurrent().getEtudiant().getEtapes().size()>0)){
				Button pdfButton = new Button();
				pdfButton.setStyleName(ValoTheme.BUTTON_BORDERLESS_COLORED);
				pdfButton.addStyleName("button-big-icon");
				pdfButton.setIcon(FontAwesome.FILE_PDF_O);
				pdfButton.setDescription(applicationContext.getMessage(NAME + ".btn.pdf.description", null, getLocale()));
				MyFileDownloader fd = new MyFileDownloader(noteController.exportPdfResume());
				fd.extend(pdfButton);
				titleLayout.addComponent(pdfButton);
				titleLayout.setComponentAlignment(pdfButton, Alignment.MIDDLE_RIGHT);
			}
			addComponent(titleLayout);




			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setSizeFull();
			globalLayout.setSpacing(true);


			//Test si user enseignant
			if(userController.isEnseignant()){
				Panel panelVue= new Panel();

				HorizontalLayout vueLayout = new HorizontalLayout();
				vueLayout.setMargin(true);
				vueLayout.setSpacing(true);
				vueLayout.setSizeFull();

				Button changerVueButton = new Button(applicationContext.getMessage(NAME+".button.vueEnseignant", null, getLocale()));
				changerVueButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
				if(MainUI.getCurrent().isVueEnseignantNotesEtResultats()){
					changerVueButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
					changerVueButton.setCaption(applicationContext.getMessage(NAME+".button.vueEtudiant", null, getLocale()));
				}
				//On change la variable vueEnseignantNotesEtResultats et on recréé la vue en cours
				changerVueButton.addClickListener(e -> {etudiantController.changerVueNotesEtResultats();init();});

				Label vueLabel=new Label(applicationContext.getMessage(NAME+".label.vueEtudiant", null, getLocale()));
				if(MainUI.getCurrent().isVueEnseignantNotesEtResultats()){
					vueLabel.setValue(applicationContext.getMessage(NAME+".label.vueEnseignant", null, getLocale()));
				}
				vueLabel.setContentMode(ContentMode.HTML); 
				vueLabel.setStyleName(ValoTheme.LABEL_SMALL);

				vueLayout.addComponent(changerVueButton);
				vueLayout.setComponentAlignment(changerVueButton, Alignment.MIDDLE_CENTER);
				vueLayout.addComponent(vueLabel);
				vueLayout.setExpandRatio(vueLabel, 1);

				panelVue.setContent(vueLayout);
				globalLayout.addComponent(panelVue);
			}



			Panel panelNotesDiplomes= new Panel(applicationContext.getMessage(NAME+".table.diplomes", null, getLocale()));


			Table notesDiplomesTable = new Table(null, new BeanItemContainer<>(Diplome.class, MainUI.getCurrent().getEtudiant().getDiplomes()));
			notesDiplomesTable.setWidth("100%");
			notesDiplomesTable.setVisibleColumns((Object[]) DIPLOMES_FIELDS_ORDER);
			for (String fieldName : DIPLOMES_FIELDS_ORDER) {
				notesDiplomesTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table.diplomes." + fieldName, null, getLocale()));
			}
			notesDiplomesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.diplomes.session", null, getLocale()), new SessionColumnGenerator());
			notesDiplomesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.diplomes.note", null, getLocale()), new NoteColumnGenerator());
			notesDiplomesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.diplomes.resultat", null, getLocale()), new ResultatColumnGenerator());

			if(MainUI.getCurrent().getEtudiant().isAfficherRang()){
				notesDiplomesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.diplomes.mention", null, getLocale()), new MentionColumnGenerator());
			}
			if(configController.isAffMentionEtudiant()){
				notesDiplomesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.diplomes.rang", null, getLocale()), new RangColumnGenerator());
			}

			notesDiplomesTable.setColumnCollapsingAllowed(true);
			notesDiplomesTable.setColumnReorderingAllowed(false);
			notesDiplomesTable.setSelectable(false);
			notesDiplomesTable.setImmediate(true);
			notesDiplomesTable.setStyleName("noscrollabletable");
			notesDiplomesTable.setPageLength(notesDiplomesTable.getItemIds().size() );
			panelNotesDiplomes.setContent(notesDiplomesTable);
			globalLayout.addComponent(panelNotesDiplomes);



			Panel panelNotesEtapes= new Panel(applicationContext.getMessage(NAME+".table.etapes", null, getLocale()));


			Table notesEtapesTable = new Table(null, new BeanItemContainer<>(Etape.class, MainUI.getCurrent().getEtudiant().getEtapes()));
			notesEtapesTable.setWidth("100%");
			notesEtapesTable.setVisibleColumns((Object[]) ETAPES_FIELDS_ORDER);
			for (String fieldName : ETAPES_FIELDS_ORDER) {
				notesEtapesTable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table.etapes." + fieldName, null, getLocale()));
			}
			notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.codevers", null, getLocale()), new CodeEtapeColumnGenerator());
			notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.libelle", null, getLocale()), new LibelleEtapeColumnGenerator());
			notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.session", null, getLocale()), new SessionColumnGenerator());
			notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.note", null, getLocale()), new NoteColumnGenerator());
			notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.resultat", null, getLocale()), new ResultatColumnGenerator());

			if(MainUI.getCurrent().getEtudiant().isAfficherRang()){
				notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.mention", null, getLocale()), new MentionColumnGenerator());
			}
			if(configController.isAffMentionEtudiant()){
				notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.rang", null, getLocale()), new RangColumnGenerator());
			}

			notesEtapesTable.setColumnCollapsingAllowed(true);
			notesEtapesTable.setColumnReorderingAllowed(false);
			notesEtapesTable.setSelectable(false);
			notesEtapesTable.setImmediate(true);
			notesEtapesTable.setStyleName("noscrollabletable");
			notesEtapesTable.setPageLength(notesEtapesTable.getItemIds().size() );
			panelNotesEtapes.setContent(notesEtapesTable);
			globalLayout.addComponent(panelNotesEtapes);



			if(MainUI.getCurrent().getEtudiant().isSignificationResultatsUtilisee()){
				Panel panelSignificationResultats= new Panel(applicationContext.getMessage(NAME+".info.significations.resultats", null, getLocale()));

				panelSignificationResultats.addStyleName("significationpanel");
				panelSignificationResultats.setIcon(FontAwesome.INFO_CIRCLE);

				VerticalLayout significationLayout = new VerticalLayout();
				significationLayout.setMargin(true);
				significationLayout.setSpacing(true);

				String grilleSignficationResultats = "";
				//grilleSignficationResultats = significationResultats.toString().substring(1,significationResultats.toString().length()-1);
				Set<String> ss = MainUI.getCurrent().getEtudiant().getSignificationResultats().keySet();
				for(String k : ss){
					if(k != null && !k.equals("") && !k.equals(" ")){
						grilleSignficationResultats = grilleSignficationResultats + "<b>"+k+"</b>&#160;:&#160;"+ MainUI.getCurrent().getEtudiant().getSignificationResultats().get(k);
						grilleSignficationResultats = grilleSignficationResultats + "&#160;&#160;&#160;";
					}
				}
				Label mapSignificationLabel=new Label(grilleSignficationResultats);
				mapSignificationLabel.setContentMode(ContentMode.HTML);
				mapSignificationLabel.setStyleName(ValoTheme.LABEL_SMALL);
				significationLayout.addComponent(mapSignificationLabel);

				panelSignificationResultats.setContent(significationLayout);
				globalLayout.addComponent(panelSignificationResultats);
			}


			addComponent(globalLayout);
		}
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}


	/** Formats the position in a column containing Date objects. */
	class SessionColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			List<Resultat> resultats = (List<Resultat>)item.getItemProperty("resultats").getValue();
			VerticalLayout sessionLayout = new VerticalLayout();
			for( Resultat r : resultats)
				sessionLayout.addComponent(new Label(r.getSession()));

			return sessionLayout;
		}
	}


	/** Formats the position in a column containing Date objects. */
	class NoteColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			List<Resultat> resultats = (List<Resultat>)item.getItemProperty("resultats").getValue();
			VerticalLayout sessionLayout = new VerticalLayout();
			for( Resultat r : resultats){
				String res = r.getNote();
				//ajout du bareme si différent de 0 et de 20
				if(r.getBareme()!=0 && r.getBareme()!=20){
					res = res + "/"+r.getBareme();
				}
				sessionLayout.addComponent(new Label(res));
			}

			return sessionLayout;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class ResultatColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			List<Resultat> resultats = (List<Resultat>)item.getItemProperty("resultats").getValue();
			VerticalLayout sessionLayout = new VerticalLayout();
			for( Resultat r : resultats){
				sessionLayout.addComponent(new Label(r.getAdmission()));
			}

			return sessionLayout;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class MentionColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			List<Resultat> resultats = (List<Resultat>)item.getItemProperty("resultats").getValue();
			VerticalLayout sessionLayout = new VerticalLayout();
			for( Resultat r : resultats){
				sessionLayout.addComponent(new Label(r.getCodMention()));
			}

			return sessionLayout;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class RangColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			String rang = (String)item.getItemProperty("rang").getValue();
			Boolean afficherRang = (boolean)item.getItemProperty("afficherRang").getValue();
			VerticalLayout sessionLayout = new VerticalLayout();
			if(afficherRang){
				sessionLayout.addComponent(new Label(rang));
			}
			return sessionLayout;
		}
	}

	/** Formats the position in a column containing Date objects. */
	class CodeEtapeColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Etape> bid = (BeanItem<Etape>) item;
			Etape etape = (Etape) bid.getBean();
			VerticalLayout sessionLayout = new VerticalLayout();

			if(etape.getRang()==null || !etape.getRang().equals("NC")){
				Button b = new Button(etape.getCode()+"/"+etape.getVersion());
				b.setStyleName("link"); 
				b.addStyleName("v-link");

				//Appel de la window contenant le détail des notes
				prepareBoutonAppelDetailDesNotes( b, etape);

				sessionLayout.addComponent(b);
			}else{
				sessionLayout.addComponent(new Label(etape.getCode()+"/"+etape.getVersion()));
			}
			return sessionLayout;
		}
	}

	private void prepareBoutonAppelDetailDesNotes(Button b, Etape etape){
		//Appel de la window contenant le détail des notes
		b.addClickListener(e->{
			DetailNotesWindow dnw = new DetailNotesWindow(etape); 
			vueEnseignant = MainUI.getCurrent().isVueEnseignantNotesEtResultats();
			dnw.addCloseListener(f->{
				//Si la vue a changer, on repasse par l'init
				if(vueEnseignant!=MainUI.getCurrent().isVueEnseignantNotesEtResultats()){
					init();
				}
			});
			UI.getCurrent().addWindow(dnw);


			//Recuperer dans la base si l'utilisateur a demandé à ne plus afficher le message
			boolean afficherMessage = true;
			if(!userController.isEtudiant()){
				String val  = userController.getPreference(Utils.SHOW_MESSAGE_NOTES_PREFERENCE);
				if(StringUtils.hasText(val)){
					afficherMessage = Boolean.valueOf(val);
				}
			}

			if(afficherMessage){
				String message =applicationContext.getMessage(NAME+".window.message.info", null, getLocale());
				HelpWindow hbw = new HelpWindow(message,applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale()),!userController.isEtudiant());
				hbw.addCloseListener(g->{
					if(!userController.isEtudiant()){
						boolean choix = hbw.getCheckBox().getValue();
						//Test si l'utilisateur a coché la case pour ne plus afficher le message
						if(choix){
							//mettre a jour dans la base de données
							userController.updatePreference(Utils.SHOW_MESSAGE_NOTES_PREFERENCE, "false");
						}
					}
				});
				UI.getCurrent().addWindow(hbw);
			}
		});
	}

	/** Formats the position in a column containing Date objects. */
	class LibelleEtapeColumnGenerator implements Table.ColumnGenerator {
		/**
		 * Generates the cell containing the value. The column is
		 * irrelevant in this use case.
		 */
		public Object generateCell(Table source, Object itemId,
				Object columnId) {

			Item item = source.getItem(itemId);

			// RECUPERATION DE LA VALEUR 
			BeanItem<Etape> bid = (BeanItem<Etape>) item;
			Etape etape = (Etape) bid.getBean();
			VerticalLayout sessionLayout = new VerticalLayout();

			if(etape.getRang()==null || !etape.getRang().equals("NC")){
				Button b = new Button(etape.getLibelle());
				b.setStyleName("link"); 
				b.addStyleName("v-link");

				//Appel de la window contenant le détail des notes
				prepareBoutonAppelDetailDesNotes( b, etape);

				sessionLayout.addComponent(b);
			}else{
				sessionLayout.addComponent(new Label(etape.getLibelle()));
			}

			return sessionLayout;
		}
	}

}
