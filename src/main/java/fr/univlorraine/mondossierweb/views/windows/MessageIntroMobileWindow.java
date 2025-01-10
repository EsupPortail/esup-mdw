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
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Fenêtre pour afficher le message de bienvenue/info sur mobile
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MessageIntroMobileWindow extends Window {

	public static final String NAME = "messageIntroMobileWindow";
	
	
	@Resource
	private transient ApplicationContext applicationContext;

	@Getter
	private CheckBox checkBox=new CheckBox();
	

	/**
	 * Crée une fenêtre
	 */
	public void init(){
		
		setWidth("100%");
		setHeight("24em");

		setCaption(applicationContext.getMessage("messageIntroMobileWindow.title", null, getLocale()));
        setModal(true);
		setResizable(false);
		setClosable(false);
		setStyleName("info-mobile-window");

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        setContent(layout);

        VerticalLayout panelLayout = new VerticalLayout();
        panelLayout.setWidth("100%");
        panelLayout.setStyleName("v-scrollableelement");
    	panelLayout.setSpacing(true);
    	panelLayout.setMargin(true);

		// Le message de bienvenue
		String message = applicationContext.getMessage("helpWindowMobile.text.enseignant", null, getLocale());
		Label label = new Label(message, ContentMode.HTML);
		panelLayout.addComponent(label);

    	layout.addComponent(panelLayout);

        // close button
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.setSizeFull();
        bLayout.setHeight("50px");
		bLayout.setSpacing(true);

		// Ajout case à cocher
		checkBox.setCaption("Ne plus afficher");
		bLayout.addComponent(checkBox);
		bLayout.setComponentAlignment(checkBox, Alignment.MIDDLE_RIGHT);
  
        Button closeButton = new Button();
		closeButton.setCaption(applicationContext.getMessage("helpWindow.btnFermer", null, getLocale()));
        closeButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        closeButton.addStyleName("v-popover-window-button");
        closeButton.setIcon(FontAwesome.TIMES);
        closeButton.addClickListener(e->{
        	close();
        });
        
        bLayout.addComponent(closeButton);
        bLayout.setComponentAlignment(closeButton, Alignment.MIDDLE_CENTER);
		bLayout.setExpandRatio(checkBox, 1);
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
