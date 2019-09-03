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

import javax.servlet.http.HttpServletRequest;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.server.SpringUIProvider;
import com.vaadin.ui.UI;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("serial")
@Theme("valo-ul")
@StyleSheet("mainView.css")
@Slf4j
public class MdwUIProvider extends SpringUIProvider  {
	
	private boolean startServletMobile;

	
	public MdwUIProvider(VaadinSession vaadinSession) {
		super(vaadinSession);
		startServletMobile = Boolean.valueOf(getWebApplicationContext().getEnvironment().getProperty("startServletMobile"));
	}
	
	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {

		//Récupération du userAgent
		if(startServletMobile && event!=null && event.getRequest()!=null && event.getRequest().getHeader("user-agent")!=null){
			//String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();

			/* Device Detection */
			Device currentDevice = DeviceUtils.getCurrentDevice((HttpServletRequest) event.getRequest());
			boolean isMobile =currentDevice.isMobile();
			
			// on teste que l'utilisateur est sur smartphone et avec un navigateur compatible webkit ou sous WP
			/*if(isMobile && (userAgent.contains("webkit")
					|| userAgent.contains("windows phone 8")
					|| userAgent.contains("windows phone 9"))){*/
			// on teste que l'utilisateur est sur mobile
			if(isMobile){
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
		String uiBeanNameObj = "";
		//Récupération du userAgent
		String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();

		/* Device Detection */
		Device currentDevice = DeviceUtils.getCurrentDevice((HttpServletRequest) event.getRequest());
		boolean isMobile =currentDevice.isMobile();
		
		// on teste que l'utilisateur est sur smartphone et avec un navigateur compatible webkit ou sous WP
		/*if(startServletMobile && isMobile && (userAgent.contains("webkit")
				|| userAgent.contains("windows phone 8")
				|| userAgent.contains("windows phone 9"))){*/
		if(isMobile) {
			//On affiche la page proposant une redirection vers la version Mobile
			log.debug("-FallbackTouchkit UI provided ("+userAgent+")");
			uiBeanNameObj = "mdwFallbackTouchkitUI";
		}else{
			//On va vers la version desktop
			log.debug("-uiBeanNameObj = mainUI");
			uiBeanNameObj = "mainUI";
		}

		//Stored in VaadinSession to use it in
		// the ApplicationScope later to initialize vaadin application scope beans
		final Integer uiId = event.getUiId();
		log.debug("uiId : "+uiId+ " VaadinSessionScope:"+VaadinSession.getCurrent().getAttribute("applicationScope.UiId"));
		VaadinSession.getCurrent().setAttribute("applicationScope.UiId", uiId);

		//On retourne l'UI décidée plus haut (desktop ou mobile)
		if (uiBeanNameObj instanceof String) {
			String uiBeanName = uiBeanNameObj.toString();
			return (UI) this.getWebApplicationContext().getBean(uiBeanName);
		}
		return super.createInstance(event);
	}

}
