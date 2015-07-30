package fr.univlorraine.mondossierweb;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.xpoft.vaadin.SpringApplicationContext;
import ru.xpoft.vaadin.SpringUIProvider;

import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UICreateEvent;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

public class MdwTouchkitUIProvider extends SpringUIProvider {


	private static final long serialVersionUID = -1535055076149004931L;
	
	private Logger LOG = LoggerFactory.getLogger(MdwTouchkitUIProvider.class);

	@Override
	public Class<? extends UI> getUIClass(UIClassSelectionEvent event) {

		//Récupération du userAgent
		String userAgent = event.getRequest().getHeader("user-agent").toLowerCase();
		LOG.debug("UA : "+userAgent);

		// on teste que l'utilisateur est sous WP ou accède via un navigateur compatible webkit
		if(userAgent.contains("webkit") || userAgent.contains("windows phone 8")
	            || userAgent.contains("windows phone 9")) {
			//On va vers la version mobile
			LOG.debug("Touckit UI provided ("+userAgent+")");
			return MdwTouchkitUI.class;
		} else {
			//On affiche la page proposant une redirection vers la version Desktop
			LOG.debug("Fallback UI provided ("+userAgent+")");
			return MdwFallbackUI.class;
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
			return (UI) SpringApplicationContext.getApplicationContext().getBean(uiBeanName);
		}
		return super.createInstance(event);
	}
}
