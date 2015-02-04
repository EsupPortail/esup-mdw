package fr.univlorraine.mondossierweb;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.addon.touchkit.server.TouchKitServlet;
import com.vaadin.addon.touchkit.settings.TouchKitSettings;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;

public class MDWTouchkitServlet extends TouchKitServlet {
	
	private Logger LOG = LoggerFactory.getLogger(MDWTouchkitServlet.class);
	
	 private MdwTouchkitUIProvider uiProvider = new MdwTouchkitUIProvider();

	    @Override
	    protected void servletInitialized() throws ServletException {
	        super.servletInitialized();
		    
	        getService().addSessionInitListener(new SessionInitListener() {
	            @Override
	            public void sessionInit(SessionInitEvent event) throws ServiceException {
	            	LOG.debug("UI Provider : "+event.getSession().getUIProviders().size()+"  -  "+event.getSession().getUIProviders());
	                event.getSession().addUIProvider(uiProvider);
	            }
	        });
	        

	        TouchKitSettings s = getTouchKitSettings();
	        s.getWebAppSettings().setWebAppCapable(true);
	        s.getWebAppSettings().setStatusBarStyle("black");
	       // String contextPath = getServletConfig().getServletContext().getContextPath();

	        s.getApplicationCacheSettings().setCacheManifestEnabled(true);
	        

	    }
	
}
