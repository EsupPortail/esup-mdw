package fr.univlorraine.mondossierweb.views;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.annotations.StyleSheet;
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
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Diplome;
import fr.univlorraine.mondossierweb.beans.Etape;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.beans.Resultat;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.Favoris;
import fr.univlorraine.mondossierweb.entities.FavorisPK;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.views.RechercheArborescenteView.MyColumnGenerator;
import fr.univlorraine.mondossierweb.views.windows.DetailNotesWindow;

/**
 * Page de gestion des structures
 */
@Component @Scope("prototype")
@VaadinView(NotesView.NAME)
@StyleSheet("notesView.css")
public class NotesView extends VerticalLayout implements View {
	private static final long serialVersionUID = -6491779626961549383L;

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


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		removeAllComponents();
		/* Style */
		setMargin(true);
		setSpacing(true);


		/* Titre */
		Label title = new Label(applicationContext.getMessage(NAME + ".title", null, getLocale()));
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);


		//Test si user enseignant et en vue Enseignant
		if(userController.isEnseignant() && MainUI.getCurrent().isVueEnseignantNotesEtResultats()){
			//On recupere les notes pour un enseignant
			etudiantController.renseigneNotesEtResultatsVueEnseignant(MainUI.getCurrent().getEtudiant());
		}else{
			//On récupère les notes pour un étudiant
			etudiantController.renseigneNotesEtResultats(MainUI.getCurrent().getEtudiant());
		}

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
		if(PropertyUtils.isAffMentionEtudiant()){
			notesDiplomesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.diplomes.rang", null, getLocale()), new RangColumnGenerator());
		}

		notesDiplomesTable.setColumnCollapsingAllowed(true);
		notesDiplomesTable.setColumnReorderingAllowed(true);
		notesDiplomesTable.setSelectable(true);
		notesDiplomesTable.setImmediate(true);
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
		if(PropertyUtils.isAffMentionEtudiant()){
			notesEtapesTable.addGeneratedColumn(applicationContext.getMessage(NAME+".table.etapes.rang", null, getLocale()), new RangColumnGenerator());
		}

		notesEtapesTable.setColumnCollapsingAllowed(true);
		notesEtapesTable.setColumnReorderingAllowed(true);
		notesEtapesTable.setSelectable(true);
		notesEtapesTable.setImmediate(true);
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
