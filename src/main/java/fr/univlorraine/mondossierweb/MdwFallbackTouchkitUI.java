package fr.univlorraine.mondossierweb;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import fr.univlorraine.mondossierweb.utils.PropertyUtils;

@Component @Scope("prototype")
@Theme("univ-lorraine")
public class MdwFallbackTouchkitUI extends UI {
	
	private static final long serialVersionUID = 1L;


	/*@Getter
	private Navigator navigator;*/
	
	// FIXME review message
    private static final String MSG = "<h1>Ooops...</h1> <p>You accessed MonDossierWeb with a browser that is support WebKit</p>"+
    		"<p> go to the <a href=\""+PropertyUtils.getAppUrl()+"m\">mobile version</a> </p>";


    @Override
    protected void init(VaadinRequest request) {

        Label label = new Label(MSG, ContentMode.HTML);
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.addComponent(label);
        setContent(content);

        
    }

}
