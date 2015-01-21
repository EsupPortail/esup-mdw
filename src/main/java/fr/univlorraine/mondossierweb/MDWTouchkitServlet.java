package fr.univlorraine.mondossierweb;

import javax.servlet.ServletException;

import com.vaadin.addon.touchkit.server.TouchKitServlet;
import com.vaadin.addon.touchkit.settings.TouchKitSettings;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;

public class MDWTouchkitServlet extends TouchKitServlet {
	
	 private MdwTouchkitUIProvider uiProvider = new MdwTouchkitUIProvider();

	    @Override
	    protected void servletInitialized() throws ServletException {
	        super.servletInitialized();
		    
	        getService().addSessionInitListener(new SessionInitListener() {
	            @Override
	            public void sessionInit(SessionInitEvent event) throws ServiceException {
	            	System.out.println("ui provider : "+event.getSession().getUIProviders().size()+"  -  "+event.getSession().getUIProviders());
	                event.getSession().addUIProvider(uiProvider);
	            }
	        });
	        

	        TouchKitSettings s = getTouchKitSettings();
	        s.getWebAppSettings().setWebAppCapable(true);
	        s.getWebAppSettings().setStatusBarStyle("black");
	        String contextPath = getServletConfig().getServletContext().getContextPath();

	        s.getApplicationCacheSettings().setCacheManifestEnabled(true);
	        

	    }
	
}
