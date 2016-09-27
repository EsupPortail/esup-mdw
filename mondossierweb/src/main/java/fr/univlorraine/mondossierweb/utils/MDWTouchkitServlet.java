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
package fr.univlorraine.mondossierweb.utils;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vaadin.addon.touchkit.server.TouchKitServlet;
import com.vaadin.addon.touchkit.settings.TouchKitSettings;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;

import fr.univlorraine.mondossierweb.MdwTouchkitUIProvider;

public class MDWTouchkitServlet extends TouchKitServlet {
	
	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(MDWTouchkitServlet.class);
	
	
	    @Override
	    protected void servletInitialized() throws ServletException {
	        super.servletInitialized();
		    
	        getService().addSessionInitListener(new SessionInitListener() {

				private static final long serialVersionUID = 3292761415754953448L;

				@Override
	            public void sessionInit(SessionInitEvent event) throws ServiceException {
					event.getSession().addUIProvider(new MdwTouchkitUIProvider(WebApplicationContextUtils.getWebApplicationContext(getServletContext())));
	            	LOG.debug("UI Provider : "+event.getSession().getUIProviders().size()+"  -  "+event.getSession().getUIProviders());
	            }
	        });
	        

	        TouchKitSettings s = getTouchKitSettings();
	        s.getWebAppSettings().setWebAppCapable(true);
	        s.getApplicationCacheSettings().setCacheManifestEnabled(true);
	        
	    }
	
}
