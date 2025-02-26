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
import fr.univlorraine.mondossierweb.utils.DeviceUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@Theme("valo-ul")
@StyleSheet("mainView.css")
@Slf4j
public class MdwUIProvider extends SpringUIProvider {

	private final boolean startServletMobile;

	public MdwUIProvider(VaadinSession vaadinSession) {
		super(vaadinSession);
		startServletMobile = Boolean.parseBoolean(getWebApplicationContext().getEnvironment().getProperty("startServletMobile"));
	}
	
	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {

		//Récupération du userAgent
		if(startServletMobile && event!=null && event.getRequest()!=null){
			// on teste que l'utilisateur est sur mobile
			if( DeviceUtils.isMobileDevice((HttpServletRequest) event.getRequest())){
				//On affiche la page proposant une redirection vers la version Mobile
				return MdwFallbackTouchkitUI.class;
			}else{
				//On va vers la version desktop
				return MainUI.class;
			}
		}
		//On va vers la version desktop
		return MainUI.class;
	}

	@Override
	public boolean isPreservedOnRefresh(UICreateEvent event)
	{
		return false;
	}

	@Override
	public UI createInstance(UICreateEvent event) {
		//Nom de la classe UI à utiliser
		log.debug("-uiBeanNameObj = mainUI");

		final Class<UIID> key = UIID.class;
		final UIID identifier = new UIID(event);
		CurrentInstance.set(key, identifier);


		boolean mobile = false;

		if (DeviceUtils.isMobileDevice((HttpServletRequest) event.getRequest())) {
			//On affiche la page proposant une redirection vers la version Mobile
			log.debug("-FallbackTouchkit UI ");
			mobile = true;
		}

		//Stored in VaadinSession to use it in
		// the ApplicationScope later to initialize vaadin application scope beans
		/*final Integer uiId = event.getUiId();
		LOG.info("uiId : "+uiId+ " VaadinSessionScope:"+VaadinSession.getCurrent().getAttribute("applicationScope.UiId"));
		VaadinSession.getCurrent().setAttribute("applicationScope.UiId", uiId);*/

		try {
			logger.debug(
					"Creating a new UI bean of class [{}] with identifier [{}]",
					mobile ? MdwFallbackTouchkitUI.class.getCanonicalName() : MainUI.class.getCanonicalName(), identifier);
			UI ui = null;
			if(mobile) {
				ui = getWebApplicationContext().getBean(MdwFallbackTouchkitUI.class);
			} else {
				ui = getWebApplicationContext().getBean(MainUI.class);
			}
			configureNavigator(ui);
			//On retourne l'UI décidée plus haut (desktop ou mobile)
			return ui;
		} finally {
			CurrentInstance.set(key, null);
		}
    }

}
