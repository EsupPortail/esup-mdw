package fr.univlorraine.mondossierweb;

import javax.servlet.http.HttpServletRequest;

import org.jfree.util.Log;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;

import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.SpringUIProvider;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

public class MdwUIProvider extends SpringUIProvider  {


	private static final long serialVersionUID = -1535055076149004931L;

	@Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {
		//super.getUIClass(event);
		
        String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
        Log.debug("UA : "+userAgent);
        
        /* Device Detection */
		Device currentDevice = DeviceUtils.getCurrentDevice((HttpServletRequest) event.getRequest());
		if(currentDevice.isMobile() && (userAgent.contains("webkit")
				|| userAgent.contains("windows phone 8")
	            || userAgent.contains("windows phone 9"))){
			Log.debug("-FallbackTouchkit UI provided ("+userAgent+")");
            return MdwFallbackTouchkitUI.class;
		}else{
        	Log.debug("-Fallback UI provided ("+userAgent+")");
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
	        String uiBeanNameObj = "";
	        String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
	        
	        /* Device Detection */
			Device currentDevice = DeviceUtils.getCurrentDevice((HttpServletRequest) event.getRequest());
			if(currentDevice.isMobile() && (userAgent.contains("webkit")
					|| userAgent.contains("windows phone 8")
		            || userAgent.contains("windows phone 9"))){
				Log.debug("-FallbackTouchkit UI provided ("+userAgent+")");
				uiBeanNameObj = "mdwFallbackTouchkitUI";
			}else{
	        	Log.debug("-uiBeanNameObj = mainUI");
	        	uiBeanNameObj = "mainUI";
			}

	        //Stored in VaadinSession to use it in
	        // the ApplicationScope later to initialize vaadin application scope beans
	        final Integer uiId = event.getUiId();
	        VaadinSession.getCurrent().setAttribute("applicationScope.UiId", uiId);

	        if (uiBeanNameObj instanceof String) {
	            String uiBeanName = uiBeanNameObj.toString();
	            return (UI) SpringApplicationContext.getApplicationContext().getBean(uiBeanName);
	        }
	        return super.createInstance(event);
	    }

}
