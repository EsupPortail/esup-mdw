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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.server.SpringUIProvider;
import com.vaadin.ui.UI;

@Theme("valo-ul")
@StyleSheet("mobileView.css")
public class MdwTouchkitUIProvider extends SpringUIProvider {

	private static final long serialVersionUID = -1535055076149004931L;

	private Logger LOG = LoggerFactory.getLogger(MdwTouchkitUIProvider.class);

	
	public MdwTouchkitUIProvider(WebApplicationContext webApplicationContext) {
		super(webApplicationContext);	
	}
	
	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {

		//Récupération du userAgent
		if(event!=null && event.getRequest()!=null && event.getRequest().getHeader("user-agent")!=null){
			String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
			LOG.debug("UA : "+userAgent);
			
			// on teste que l'utilisateur est sous WP ou accède via un navigateur compatible webkit
			if(userAgent.contains("webkit") || userAgent.contains("windows phone 8")
					|| userAgent.contains("windows phone 9")) {
				//On va vers la version mobile
				LOG.debug("Touckit UI provided ("+userAgent+")");
				return MdwTouchkitUI.class;
			} else{
				LOG.debug("Fallback UI provided ("+userAgent+")");
				//On affiche la page proposant une redirection vers la version Desktop
				return MdwFallbackUI.class;
			}
		}
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
		//Nom de la classe UI à utiliser
		String uiBeanNameObj = "";
		//Récupération du userAgent
		String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
		// on teste que l'utilisateur est sous WP ou accède via un navigateur compatible webkit
		if(userAgent.contains("webkit") || userAgent.contains("windows phone 8")
				|| userAgent.contains("windows phone 9")) {
			//On va vers la version mobile
			LOG.debug("-uiBeanNameObj = mdwTouchkitUI");
			uiBeanNameObj = "mdwTouchkitUI";
		} else {
			//On affiche la page proposant une redirection vers la version Desktop
			LOG.debug("-uiBeanNameObj = mdwFallbackUI");
			uiBeanNameObj = "mdwFallbackUI";
		}

		//Stored in VaadinSession to use it in
		// the ApplicationScope later to initialize vaadin application scope beans
		final Integer uiId = event.getUiId();
		VaadinSession.getCurrent().setAttribute("applicationScope.UiId", uiId);

		//On retourne l'UI décidée plus haut (desktop ou mobile)
		if (uiBeanNameObj instanceof String) {
			String uiBeanName = uiBeanNameObj.toString();
			return (UI) this.getWebApplicationContext().getBean(uiBeanName);
		}
		return super.createInstance(event);
	}
}
