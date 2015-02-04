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
public class MdwFallbackTouchkitUI extends UI {
	
	private static final long serialVersionUID = 1L;


	private Logger LOG = LoggerFactory.getLogger(MdwFallbackTouchkitUI.class);
	
	// FIXME review message
    private static final String MSG = "<h1>Oups...</h1> <p>Vous accédez à MonDossierWeb depuis un mobile et avec un navigateur qui supporte WebKit</p>"+
    		"<p> Pour utiliser la <a href=\""+PropertyUtils.getAppUrl()+"/m\">version mobile</a> </p>";


    @Override
    protected void init(VaadinRequest request) {

        Label label = new Label(MSG, ContentMode.HTML);
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.addComponent(label);
        setContent(content);

        
    }

}
