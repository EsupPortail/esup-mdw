/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
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
	        s.getApplicationCacheSettings().setOfflineModeEnabled(false);
	        
	    }
	
}
