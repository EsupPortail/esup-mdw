package fr.univlorraine.mondossierweb.views.windows;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Fenêtre pour filtrer les inscrits sur mobile
 */
@Configurable(preConstruction=true)
public class FiltreInscritsMobileWindow extends Window {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "filtreInscritsMobileWindow";


	
	@Resource
	private transient ApplicationContext applicationContext;
	

	/**
	 * Crée une fenêtre
	 */
	public FiltreInscritsMobileWindow(){
		setWidth("90%");
        setHeight("260px");
        setModal(true);
		setResizable(false);

        
        setCaption("Filtre");
        
  
        setStyleName("v-popover-blank");

       
        
        // Have some details to display
        VerticalLayout layout = new VerticalLayout();
       // layout.setStyleName(ValoTheme.LAYOUT_WELL);
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);

        Label etapeLabel = new Label("Etape d'appartenance");

        
        NativeSelect etapeSelect = new NativeSelect();

        
        HorizontalLayout gLayout = new HorizontalLayout();
        gLayout.setSizeFull();

        Label groupeLabel = new Label("Groupe");
        gLayout.addComponent(groupeLabel);
        gLayout.setComponentAlignment(groupeLabel, Alignment.MIDDLE_LEFT);
        
        NativeSelect groupeSelect = new NativeSelect();
 
        
        
        layout.addComponent(etapeLabel);
        layout.addComponent(etapeSelect);
        layout.addComponent(gLayout);
        layout.addComponent(groupeSelect);
        	
       

        this.setResizable(false);
        

        // Have a close button
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.setSizeFull();
  
        Button closeButton = new Button("Filtrer");
        closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        closeButton.addStyleName("v-popover-button");
        //closeButton.setHeight("40px");
        closeButton.addClickListener(e->{
        	close();
        });
        bLayout.addComponent(closeButton);
        bLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_CENTER);
        layout.addComponent(bLayout);


        setContent(layout);
       
	}










}
