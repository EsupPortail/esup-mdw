package fr.univlorraine.mondossierweb;

import org.jfree.util.Log;

import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.SpringUIProvider;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

public class MdwTouchkitUIProvider extends SpringUIProvider {


	private static final long serialVersionUID = -1535055076149004931L;

	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {

		String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
		Log.debug("UA : "+userAgent);

		// on ne teste que le webkit pour pouvoir tester sur chrome sur PC desktop
		if(userAgent.contains("webkit") || userAgent.contains("windows phone 8")
	            || userAgent.contains("windows phone 9")) {
		//if(userAgent.contains("webkit") && (userAgent.contains("android") || userAgent.contains("windows phone") || userAgent.contains("iphone"))) {
			Log.debug("Touckit UI provided ("+userAgent+")");
			return MdwTouchkitUI.class;
		} else {
			Log.debug("Fallback UI provided ("+userAgent+")");
			return MdwFallbackUI.class;
			//return MainUI.class;
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
		if(userAgent.contains("webkit") || userAgent.contains("windows phone 8")
	            || userAgent.contains("windows phone 9")) {
			Log.debug("-uiBeanNameObj = mdwTouchkitUI");
			uiBeanNameObj = "mdwTouchkitUI";
		} else {
			Log.debug("-uiBeanNameObj = mdwFallbackUI");
			uiBeanNameObj = "mdwFallbackUI";
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
