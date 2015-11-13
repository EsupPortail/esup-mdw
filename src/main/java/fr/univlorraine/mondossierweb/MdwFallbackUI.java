/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb;


import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;

@Scope("prototype")
@Component
@Theme("valo-ul")
@StyleSheet("mobileView.css")
public class MdwFallbackUI extends UI {
	

	private static final long serialVersionUID = 3269956774322268418L;
	
	
	@Resource
	private transient ApplicationContext applicationContext;
	
    @Override
    protected void init(VaadinRequest request) {

    	Label label = new Label(applicationContext.getMessage("fallbackToDesktop.message", new Object[] {PropertyUtils.getAppUrl()}, getLocale()), ContentMode.HTML);
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.addComponent(label);
        setContent(content);

    }

}
