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
	
	private static final long serialVersionUID = 1L;

	private Logger LOG = LoggerFactory.getLogger(MDWTouchkitServlet.class);
	
	 private MdwTouchkitUIProvider uiProvider = new MdwTouchkitUIProvider();

	    @Override
	    protected void servletInitialized() throws ServletException {
	        super.servletInitialized();
		    
	        getService().addSessionInitListener(new SessionInitListener() {

				private static final long serialVersionUID = 3292761415754953448L;

				@Override
	            public void sessionInit(SessionInitEvent event) throws ServiceException {
	            	LOG.debug("UI Provider : "+event.getSession().getUIProviders().size()+"  -  "+event.getSession().getUIProviders());
	                event.getSession().addUIProvider(uiProvider);
	            }
	        });
	        

	        TouchKitSettings s = getTouchKitSettings();
	        s.getWebAppSettings().setWebAppCapable(true);
	        s.getWebAppSettings().setStatusBarStyle("black");

	        s.getApplicationCacheSettings().setCacheManifestEnabled(true);
	        

	    }
	
}
