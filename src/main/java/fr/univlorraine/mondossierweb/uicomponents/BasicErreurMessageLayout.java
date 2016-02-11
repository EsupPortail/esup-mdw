/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.uicomponents;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;


public class BasicErreurMessageLayout extends VerticalLayout{


	private static final String ERREURVIEW_NAME = "erreurView";
	
	private ApplicationContext applicationContext;
	
	public BasicErreurMessageLayout(ApplicationContext context){
		super();
		applicationContext=context;
		/* Style */
		setMargin(true);
		setSpacing(true);

		/* Titre */
		Label title = new Label(applicationContext.getMessage(ERREURVIEW_NAME + ".title", null, null));
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);

		/* Texte */
		addComponent(new Label(applicationContext.getMessage(ERREURVIEW_NAME + ".text", null, null), ContentMode.HTML));
	}
	
}
