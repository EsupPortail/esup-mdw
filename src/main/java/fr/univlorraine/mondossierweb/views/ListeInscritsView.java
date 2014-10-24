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


import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.UserController;

/**
 * Recherche arborescente
 */
@Component @Scope("prototype")
@VaadinView(ListeInscritsView.NAME)
public class ListeInscritsView extends VerticalLayout implements View {
	private static final long serialVersionUID = -2056224835347802529L;

	public static final String NAME = "listeInscritsView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient UserController userController;


	@Resource
	private transient ListeInscritsController listeInscritsController;


	/**
	 * reinitialise la vue pour pointer sur les données en paramètres
	 * @param parameterMap
	 */
	public void initFromParameters(Map<String, String> parameterMap){
		removeAllComponents();
		listeInscritsController.setCode(parameterMap.get("code"));
		listeInscritsController.setType(parameterMap.get("type"));
		init();
	}
	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		/* Style */
		setMargin(true);
		setSpacing(true);

		/* Titre */
		Label title = new Label("Liste inscrits");
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);

		if(listeInscritsController.getCode()!=null && listeInscritsController.getType()!=null){
			Label elementRecherche = new Label(listeInscritsController.getCode() +" "+listeInscritsController.getType());
			elementRecherche.addStyleName(ValoTheme.LABEL_H1);
			addComponent(elementRecherche);
		}


	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
		//System.out.println("enter");
	}

}
