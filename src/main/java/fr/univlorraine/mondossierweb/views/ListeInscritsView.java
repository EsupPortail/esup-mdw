package fr.univlorraine.mondossierweb.views;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import lombok.Getter;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.DiscoveryNavigator;
import ru.xpoft.vaadin.VaadinView;







import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.beans.Inscription;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Inscrit;
import fr.univlorraine.mondossierweb.utils.Utils;
import fr.univlorraine.mondossierweb.views.InscriptionsView.LibelleInscriptionColumnGenerator;

/**
 * Recherche arborescente
 */
@Component @Scope("prototype")
@VaadinView(ListeInscritsView.NAME)
public class ListeInscritsView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "listeInscritsView";
	
	public static final String[] INS_FIELDS_ORDER = {"cod_etu", "nom","prenom","email"};

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;


	@Resource
	private transient ListeInscritsController listeInscritsController;



	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {


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

		
		
		String code = MainUI.getCurrent().getCodeObjListInscrits();
		String type = MainUI.getCurrent().getTypeObjListInscrits();
		String libelle = "";
		if(type.equals(Utils.TYPE_VET)){
			libelle = MainUI.getCurrent().getEtapeListeInscrits().getLibelle();
		}
		
		if(code!=null && type!=null){
			Label elementRecherche = new Label(code+" "+libelle);
			elementRecherche.addStyleName(ValoTheme.LABEL_COLORED);
			addComponent(elementRecherche);
		}
		
		
		
		Table inscritstable = new Table(null, new BeanItemContainer<>(Inscrit.class, MainUI.getCurrent().getListeInscrits()));
		inscritstable.setWidth("100%");
		String[] colonnes = INS_FIELDS_ORDER;

		inscritstable.setVisibleColumns((Object[]) colonnes);
		for (String fieldName : colonnes) {
			inscritstable.setColumnHeader(fieldName, applicationContext.getMessage(NAME+".table." + fieldName, null, getLocale()));
		}
		//inscriptionsTable.setSortContainerPropertyId("cod_anu");
		inscritstable.setColumnCollapsingAllowed(true);
		inscritstable.setColumnReorderingAllowed(false);
		inscritstable.setSelectable(false);
		inscritstable.setImmediate(true);
		//inscritstable.setPageLength(inscritstable.getItemIds().size() );
		addComponent(inscritstable);

		

	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//System.out.println("enter");
	}

}
