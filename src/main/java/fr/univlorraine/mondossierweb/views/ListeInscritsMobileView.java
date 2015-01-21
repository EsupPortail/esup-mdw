package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;

import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * ListeInscrits sur mobile
 */
@Component @Scope("prototype")
@VaadinView(ListeInscritsMobileView.NAME)
public class ListeInscritsMobileView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "ListeInscritsMobileView";




	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheController rechercheController;


	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		
	}
	
	/**
	 * Initialise la vue
	 */
	public void initListe() {
		removeAllComponents();
		/* Style */
		setMargin(true);
		setSpacing(true);
		setSizeFull();

		
		
	}


	
	

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//LOG.debug("ENTER FAVORIS VIEW");
	}

	


}
