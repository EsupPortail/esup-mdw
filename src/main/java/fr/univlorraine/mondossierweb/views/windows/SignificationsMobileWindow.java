/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.views.windows;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.GridLayout;
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
	public SignificationsMobileWindow(boolean afficherSignificationIndicateurProfondeur){
		
		setWidth("95%");
		setHeight("95%");


		setCaption(applicationContext.getMessage("significationsWindow.title", null, getLocale()));
        setModal(true);
		setResizable(false);
		setClosable(false);
        setStyleName("v-popover-blank");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setContent(layout);
        

        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setWidth("100%");
        panelLayout.setStyleName("v-scrollableelement");
    	panelLayout.setSpacing(true);
    	panelLayout.setMargin(true);
       
        if(MdwTouchkitUI.getCurrent().getEtudiant().isSignificationResultatsUtilisee()){
        	
			Panel panelSignificationResultats= new Panel();
			panelSignificationResultats.setCaption(applicationContext.getMessage(NAME+".info.significations.resultats", null, getLocale()));
			panelSignificationResultats.addStyleName("significationpanel");
			panelSignificationResultats.setWidth("100%");
			
			VerticalLayout significationLayout = new VerticalLayout();
			significationLayout.setWidth("100%");
			significationLayout.setMargin(true);
			significationLayout.setSpacing(true);

			Set<String> ss = MdwTouchkitUI.getCurrent().getEtudiant().getSignificationResultats().keySet();
			for(String k : ss){
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
			panelLayout.addComponent(panelSignificationResultats);
		
		}
        
        if(afficherSignificationIndicateurProfondeur){
        	
        	Panel panelSignificationIndicateurs= new Panel();
        	panelSignificationIndicateurs.setCaption(applicationContext.getMessage(NAME+".info.significations.indicateurs", null, getLocale()));
        	panelSignificationIndicateurs.addStyleName("significationpanel");
        	panelSignificationIndicateurs.setWidth("100%");

			VerticalLayout significationLayout = new VerticalLayout();
			significationLayout.setMargin(true);
			significationLayout.setSpacing(true);
			significationLayout.setWidth("100%");

			//1er NIVEAU
			HorizontalLayout levelLayout1 = new HorizontalLayout();
			levelLayout1.setWidth("100%");
			HorizontalLayout levelMainLayout1 = new HorizontalLayout();
			levelMainLayout1.setWidth("100%");
			levelMainLayout1.setSpacing(true);
			levelMainLayout1.setStyleName("level-indicator-layout");
			int k=0;
			for(int i=0; i<1;i++){
				//Ajout d'un level
				k++;
				Label libLevelLayout = new Label();
				libLevelLayout.setSizeFull();
				libLevelLayout.setHeight("8px");
				libLevelLayout.setStyleName("layout-level-green-indicator");
				levelMainLayout1.addComponent(libLevelLayout);
			}
			//On pense avoir 7 level maxi 
			for(int j=k; j<8;j++){
				Label libLevelSpaceLayout = new Label();
				libLevelSpaceLayout.setSizeFull();
				libLevelSpaceLayout.setHeight("8px");
				levelMainLayout1.addComponent(libLevelSpaceLayout);
			}

			levelLayout1.addComponent(levelMainLayout1);
			levelLayout1.addComponent(new Label("1er niveau"));
			significationLayout.addComponent(levelLayout1);
			
			//2em NIVEAU
			HorizontalLayout levelLayout2 = new HorizontalLayout();
			levelLayout2.setSizeFull();
			HorizontalLayout levelMainLayout2 = new HorizontalLayout();
			levelMainLayout2.setSizeFull();
			levelMainLayout2.setSpacing(true);
			levelMainLayout2.setStyleName("level-indicator-layout");
			k=0;
			for(int i=0; i<2;i++){
				//Ajout d'un level
				k++;
				Label libLevelLayout = new Label();
				libLevelLayout.setSizeFull();
				libLevelLayout.setHeight("8px");
				libLevelLayout.setStyleName("layout-level-green-indicator");
				levelMainLayout2.addComponent(libLevelLayout);
			}
			//On pense avoir 7 level maxi 
			for(int j=k; j<8;j++){
				Label libLevelSpaceLayout = new Label();
				libLevelSpaceLayout.setSizeFull();
				libLevelSpaceLayout.setHeight("8px");
				levelMainLayout2.addComponent(libLevelSpaceLayout);
			}

			levelLayout2.addComponent(levelMainLayout2);
			levelLayout2.addComponent(new Label("2em niveau"));
			significationLayout.addComponent(levelLayout2);
			
			
			//3em NIVEAU
			HorizontalLayout levelLayout3 = new HorizontalLayout();
			levelLayout3.setSizeFull();
			HorizontalLayout levelMainLayout3 = new HorizontalLayout();
			levelMainLayout3.setSizeFull();
			levelMainLayout3.setSpacing(true);
			levelMainLayout3.setStyleName("level-indicator-layout");
			k=0;
			for(int i=0; i<3;i++){
				//Ajout d'un level
				k++;
				Label libLevelLayout = new Label();
				libLevelLayout.setSizeFull();
				libLevelLayout.setHeight("8px");
				libLevelLayout.setStyleName("layout-level-green-indicator");
				levelMainLayout3.addComponent(libLevelLayout);
			}
			//On pense avoir 7 level maxi 
			for(int j=k; j<8;j++){
				Label libLevelSpaceLayout = new Label();
				libLevelSpaceLayout.setSizeFull();
				libLevelSpaceLayout.setHeight("8px");
				levelMainLayout3.addComponent(libLevelSpaceLayout);
			}

			levelLayout3.addComponent(levelMainLayout3);
			levelLayout3.addComponent(new Label("3em niveau"));
			significationLayout.addComponent(levelLayout3);
			
			
			
			//4em NIVEAU
			HorizontalLayout levelLayout4 = new HorizontalLayout();
			levelLayout4.setSizeFull();
			HorizontalLayout levelMainLayout4 = new HorizontalLayout();
			levelMainLayout4.setSizeFull();
			levelMainLayout4.setSpacing(true);
			levelMainLayout4.setStyleName("level-indicator-layout");
			k=0;
			for(int i=0; i<4;i++){
				//Ajout d'un level
				k++;
				Label libLevelLayout = new Label();
				libLevelLayout.setSizeFull();
				libLevelLayout.setHeight("8px");
				libLevelLayout.setStyleName("layout-level-green-indicator");
				levelMainLayout4.addComponent(libLevelLayout);
			}
			//On pense avoir 7 level maxi 
			for(int j=k; j<8;j++){
				Label libLevelSpaceLayout = new Label();
				libLevelSpaceLayout.setSizeFull();
				libLevelSpaceLayout.setHeight("8px");
				levelMainLayout4.addComponent(libLevelSpaceLayout);
			}

			levelLayout4.addComponent(levelMainLayout4);
			levelLayout4.addComponent(new Label("4em niveau"));
			significationLayout.addComponent(levelLayout4);
			
			//ETC
			HorizontalLayout levelLayoutEtc = new HorizontalLayout();
			levelLayoutEtc.setSizeFull();

			levelLayoutEtc.addComponent(new Label("..."));
			levelLayoutEtc.addComponent(new Label(""));
			significationLayout.addComponent(levelLayoutEtc);
			
	
			
			panelSignificationIndicateurs.setContent(significationLayout);
			panelLayout.addComponent(panelSignificationIndicateurs);

        }

    	layout.addComponent(panelLayout);
		    

        // close button
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.setSizeFull();
        bLayout.setHeight("50px");
  
        Button closeButton = new Button();
        //closeButton.setCaption(applicationContext.getMessage("significationsWindow.btnFermer", null, getLocale()));
        closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        closeButton.addStyleName("v-popover-button");
        closeButton.setIcon(FontAwesome.CHECK);
        closeButton.addClickListener(e->{
        	close();
        	
        });
        
        bLayout.addComponent(closeButton);
        bLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_CENTER);
        layout.addComponent(bLayout);

        layout.setExpandRatio(panelLayout, 1);


       
	}




}
