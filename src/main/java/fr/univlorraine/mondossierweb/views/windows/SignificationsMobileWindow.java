package fr.univlorraine.mondossierweb.views.windows;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.MdwTouchkitUI;

/**
 * Fenêtre pour afficher les significations des codes de résultats en version mobile
 */
@Configurable(preConstruction=true)
public class SignificationsMobileWindow extends Window {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "significationsMobileWindow";
	
	
	@Resource
	private transient ApplicationContext applicationContext;
	

	/**
	 * Crée une fenêtre
	 */
	public SignificationsMobileWindow(){
		setWidth("90%");
		int height = 110;
		
        setModal(true);
		setResizable(false);

		setCaption(applicationContext.getMessage(NAME+".info.significations.resultats", null, getLocale()));
        setStyleName("v-popover-blank");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);
        
       
        if(MdwTouchkitUI.getCurrent().getEtudiant().isSignificationResultatsUtilisee()){
			Panel panelSignificationResultats= new Panel();

			panelSignificationResultats.addStyleName("significationpanel");
			

			VerticalLayout significationLayout = new VerticalLayout();
			significationLayout.setMargin(true);
			significationLayout.setSpacing(true);

			Set<String> ss = MdwTouchkitUI.getCurrent().getEtudiant().getSignificationResultats().keySet();
			for(String k : ss){
				height += 67;
				if(k != null && !k.equals("") && !k.equals(" ")){
					HorizontalLayout signLayout = new HorizontalLayout();
					signLayout.setSizeFull();
					signLayout.setMargin(true);
					signLayout.setSpacing(true);
					Label codeLabel = new Label(k);
					codeLabel.setStyleName(ValoTheme.LABEL_BOLD);
					codeLabel.addStyleName("v-label-align-right");
					signLayout.addComponent(codeLabel);
					Label valueLabel = new Label(""+MdwTouchkitUI.getCurrent().getEtudiant().getSignificationResultats().get(k));
					signLayout.addComponent(valueLabel);
					significationLayout.addComponent(signLayout);
				}
			}
			
			panelSignificationResultats.setContent(significationLayout);
			layout.addComponent(panelSignificationResultats);
		}

        setHeight(height, Unit.PIXELS);
        this.setResizable(false);
        

        // Have a close button
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.setSizeFull();
  
        Button closeButton = new Button();
        closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        closeButton.addStyleName("v-popover-button");
        closeButton.setIcon(FontAwesome.TIMES);
        closeButton.addClickListener(e->{
        	close();
        	
        });
        
        bLayout.addComponent(closeButton);
        bLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_CENTER);
        layout.addComponent(bLayout);


        setContent(layout);
       
	}




}
