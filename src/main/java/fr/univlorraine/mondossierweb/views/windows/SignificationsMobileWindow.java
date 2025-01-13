/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb.views.windows;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

/**
 * Fenêtre pour afficher les significations des codes de résultats en version mobile
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SignificationsMobileWindow extends Window {

	public static final String NAME = "significationsMobileWindow";
	
	
	@Resource
	private transient ApplicationContext applicationContext;
	

	/**
	 * Crée une fenêtre
	 */
	public void init(boolean afficherSignificationIndicateurProfondeur){

		setWidth("90%");
		setModal(true);
		setResizable(false);
		setClosable(false);

        VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
        setContent(layout);

		// Titre
		setCaption(applicationContext.getMessage("significationsWindow.title", null, getLocale()));
		// setStyleName("v-popover-blank");

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
					codeLabel.addStyleName("v-small");
					signLayout.addComponent(codeLabel);
					Label valueLabel = new Label(""+MdwTouchkitUI.getCurrent().getEtudiant().getSignificationResultats().get(k));
					valueLabel.addStyleName("v-small");
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

			for (int level = 1; level < 5; level++) {
				significationLayout.addComponent(createInfoLevelLayout(level));
			}

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
        closeButton.addStyleName("v-popover-window-button");
        closeButton.setIcon(FontAwesome.CHECK);
        closeButton.addClickListener(e->{
        	close();
        	
        });
        
        bLayout.addComponent(closeButton);
        bLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_CENTER);
        layout.addComponent(bLayout);

        layout.setExpandRatio(panelLayout, 1);


       
	}

	private com.vaadin.ui.Component createInfoLevelLayout(int level) {
		HorizontalLayout levelLayout = new HorizontalLayout();
		levelLayout.setWidth("100%");
		HorizontalLayout levelMainLayout1 = new HorizontalLayout();
		levelMainLayout1.setWidth("100%");
		levelMainLayout1.setSpacing(true);
		levelMainLayout1.setStyleName("level-indicator-layout");
		int k=0;
		for(int i=0; i<level;i++){
			//Ajout d'un level
			k++;
			Label libLevelLayout = new Label();
			libLevelLayout.setSizeFull();
			libLevelLayout.setHeight("8px");
			libLevelLayout.setStyleName("layout-level-primary-indicator");
			levelMainLayout1.addComponent(libLevelLayout);
		}
		//On pense avoir 7 level maxi
		for(int j=k; j<8;j++){
			Label libLevelSpaceLayout = new Label();
			libLevelSpaceLayout.setSizeFull();
			libLevelSpaceLayout.setHeight("8px");
			levelMainLayout1.addComponent(libLevelSpaceLayout);
		}

		levelLayout.addComponent(levelMainLayout1);
		String rang = level + "er";
		if(level == 2){
			rang = "2nd";
		}
		if(level > 2){
			rang = level + "eme";
		}
		levelLayout.addComponent(new Label(rang + " niveau"));
		levelLayout.addStyleName("v-small");
		return levelLayout;
	}


}
