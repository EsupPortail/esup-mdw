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
package fr.univlorraine.mondossierweb;


import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.internal.UIID;
import com.vaadin.spring.server.SpringUIProvider;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;

@SuppressWarnings("serial")
@Theme("valo-ul")
@StyleSheet("mobileView.css")
@Slf4j
public class MdwTouchkitUIProvider extends SpringUIProvider {

	
	public MdwTouchkitUIProvider(VaadinSession vaadinSession) {
		super(vaadinSession);
	}
	
	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
		//On va vers la version mobile
		return MdwTouchkitUI.class;
	}

	@Override
	public boolean isPreservedOnRefresh(UICreateEvent event)
	{
		return false;
	}

	@Override
	public UI createInstance(UICreateEvent event) {
		final Class<UIID> key = UIID.class;
		final UIID identifier = new UIID(event);
		CurrentInstance.set(key, identifier);
		try {
			log.debug(
					"Creating a new UI bean of class [{}] with identifier [{}]",
					event.getUIClass().getCanonicalName(), identifier);
			UI ui = getWebApplicationContext().getBean(event.getUIClass());
			configureNavigator(ui);
			return ui;
		}catch(BeanCreationException bce) {
			log.error("Erreur lors de la création de l'UI. key : {} - id : {} - CurrenInstance.getUUID : {}", key, identifier, CurrentInstance.get(UIID.class), bce);
			// 2nd essai
			CurrentInstance.set(key, identifier);
			UI ui = getWebApplicationContext().getBean(event.getUIClass());
			configureNavigator(ui);
			return ui;
		} finally {
			CurrentInstance.set(key, null);
		}
	}

	/*
	@Override
	public UI createInstance(UICreateEvent event) {
		//Nom de la classe UI à utiliser
		log.debug("-uiBeanNameObj = mdwTouchkitUI");

		final Class<UIID> key = UIID.class;
		final UIID identifier = new UIID(event);
		CurrentInstance.set(key, identifier);

		try {
			UI ui  = getWebApplicationContext().getBean(MdwTouchkitUI.class);
			configureNavigator(ui);
			return ui;
		} finally {
			CurrentInstance.set(key, null);
		}
	}
	*/

}
