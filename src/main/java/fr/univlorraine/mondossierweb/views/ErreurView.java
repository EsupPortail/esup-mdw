/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.uicomponents.BasicErreurMessageLayout;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@VaadinView(ErreurView.NAME)
public class ErreurView extends VerticalLayout implements View {
	private static final long serialVersionUID = 5118929963964330113L;

	public static final String NAME = "erreurView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		addComponent(new BasicErreurMessageLayout(applicationContext));

	}

	/**
	 * @see com.vaadin.navigator.View${symbol_pound}enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
