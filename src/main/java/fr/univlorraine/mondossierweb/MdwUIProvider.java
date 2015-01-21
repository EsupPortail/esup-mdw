package fr.univlorraine.mondossierweb;

import org.jfree.util.Log;

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
        System.out.println("UA : "+userAgent);
        
        // on teste webkit ET que la personne est sur un appareil mobile
       //if(userAgent.contains("webkit") && (userAgent.contains("android") || userAgent.contains("windows phone") || userAgent.contains("iphone"))) {
        
        //on ne teste que le webkit pour pouvoir tester sur chrome sur PC desktop
    // if(userAgent.contains("webkit") ) {
        
        //if(userAgent.contains("webkit") && (userAgent.contains("android") || userAgent.contains("windows phone") || userAgent.contains("iphone"))) {
       /* if(userAgent.contains("webkit") ) {
        	Log.debug("-Touckit UI provided ("+userAgent+")");
        	System.out.println("-Touckit UI provided");
            return MdwFallbackTouchkitUI.class;
        } else {*/
        	Log.debug("-Fallback UI provided ("+userAgent+")");
        	System.out.println("-Fallback UI provided");
            return MainUI.class;
        	//return MainUI.class;
       /* }*/
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
	      /*  if(userAgent.contains("webkit") ) {
	        	Log.debug("-uiBeanNameObj = mdwFallbackTouchkitUI");
	        	System.out.println("-uiBeanNameObj = mdwFallbackTouchkitUI");
	        	uiBeanNameObj = "mdwFallbackTouchkitUI";
	        } else {*/
	        	Log.debug("-uiBeanNameObj = mainUI");
	        	System.out.println("-uiBeanNameObj = mainUI");
	        	uiBeanNameObj = "mainUI";
	        /*}*/

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
