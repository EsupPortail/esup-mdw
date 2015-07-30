package fr.univlorraine.mondossierweb;

import javax.servlet.http.HttpServletRequest;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;

import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.SpringUIProvider;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

public class MdwUIProvider extends SpringUIProvider  {


	private static final long serialVersionUID = -1535055076149004931L;

	private Logger LOG = LoggerFactory.getLogger(MdwUIProvider.class);
	
	@Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
		
		//Récupération du userAgent
        String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
        LOG.debug("UA : "+userAgent);
        
        /* Device Detection */
		Device currentDevice = DeviceUtils.getCurrentDevice((HttpServletRequest) event.getRequest());
		// on teste que l'utilisateur est sur smartphone et avec un navigateur compatible webkit ou sous WP
		if(currentDevice.isMobile() && (userAgent.contains("webkit")
				|| userAgent.contains("windows phone 8")
	            || userAgent.contains("windows phone 9"))){
			//On affiche la page proposant une redirection vers la version Mobile
			LOG.debug("-FallbackTouchkit UI provided ("+userAgent+")");
            return MdwFallbackTouchkitUI.class;
		}else{
			//On va vers la version desktop
        	LOG.debug("-Fallback UI provided ("+userAgent+")");
            return MainUI.class;
		}
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
			// on teste que l'utilisateur est sur smartphone et avec un navigateur compatible webkit ou sous WP
			if(currentDevice.isMobile() && (userAgent.contains("webkit")
					|| userAgent.contains("windows phone 8")
		            || userAgent.contains("windows phone 9"))){
				//On affiche la page proposant une redirection vers la version Mobile
				LOG.debug("-FallbackTouchkit UI provided ("+userAgent+")");
				uiBeanNameObj = "mdwFallbackTouchkitUI";
			}else{
				//On va vers la version desktop
	        	LOG.debug("-uiBeanNameObj = mainUI");
	        	uiBeanNameObj = "mainUI";
			}

	        //Stored in VaadinSession to use it in
	        // the ApplicationScope later to initialize vaadin application scope beans
	        final Integer uiId = event.getUiId();
	        VaadinSession.getCurrent().setAttribute("applicationScope.UiId", uiId);

	        //On retourne l'UI décidée plus haut (desktop ou mobile)
	        if (uiBeanNameObj instanceof String) {
	            String uiBeanName = uiBeanNameObj.toString();
	            return (UI) SpringApplicationContext.getApplicationContext().getBean(uiBeanName);
	        }
	        return super.createInstance(event);
	    }

}
