package fr.univlorraine.mondossierweb;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;

@Component @Scope("prototype")
@Theme("valo-ul")
@StyleSheet("mobileView.css")
public class MdwFallbackUI extends UI {
	

	private static final long serialVersionUID = 3269956774322268418L;
	
	private Logger LOG = LoggerFactory.getLogger(MdwFallbackUI.class);
	
	// FIXME review message
    private static final String MSG = "<p>Vous tentez d'accéder à MonDossierWeb avec un navigateur non compatible avec la version mobile de l'application</p>"+
    		"<p> Pour utiliser la <a href=\""+PropertyUtils.getAppUrl()+"\">version bureau</a> </p>";

    @Override
    protected void init(VaadinRequest request) {

        Label label = new Label(MSG, ContentMode.HTML);
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.addComponent(label);
        setContent(content);

    }

}
