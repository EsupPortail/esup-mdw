package fr.univlorraine.mondossierweb;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.SessionSupport;
import org.springframework.mobile.device.DeviceResolverRequestFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.vaadin.server.Constants;
import com.vaadin.shared.communication.PushMode;

import fr.univlorraine.mondossierweb.config.SpringConfig;
import fr.univlorraine.mondossierweb.utils.JMeterServlet;
import fr.univlorraine.tools.atmosphere.RecoverSecurityContextAtmosphereInterceptor;
import fr.univlorraine.tools.logback.UserMdcServletFilter;
import fr.univlorraine.tools.vaadin.FrenchUnsupportedBrowserHandlerSpringVaadinServlet;

/**
 * Initialisation de l'application web
 * 
 * @author Adrien Colson
 */
public class Initializer implements WebApplicationInitializer {

	/**
	 * Profil Spring de debug
	 */
	public final static String DEBUG_PROFILE = "debug";

	/**
	 * Ajoute les paramètres de contexte aux propriétés systèmes, de manière à les rendre accessibles dans logback.xml
	 * @param servletContext
	 */
	private void addContextParametersToSystemProperties(ServletContext servletContext) {
		Enumeration<String> e = servletContext.getInitParameterNames();
		while (e.hasMoreElements()) {
			String parameterName = e.nextElement();
			System.setProperty("context." + parameterName, servletContext.getInitParameter(parameterName));
		}
	}

	/**
	 * @see org.springframework.web.WebApplicationInitializer#onStartup(javax.servlet.ServletContext)
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		addContextParametersToSystemProperties(servletContext);

		/* Configure les sessions */
		Set<SessionTrackingMode> sessionTrackingModes = new HashSet<SessionTrackingMode>();
		sessionTrackingModes.add(SessionTrackingMode.COOKIE);
		servletContext.setSessionTrackingModes(sessionTrackingModes);
		servletContext.addListener(new HttpSessionListener() {
			@Override
			public void sessionCreated(HttpSessionEvent httpSessionEvent) {
				httpSessionEvent.getSession().setMaxInactiveInterval(60);
			}

			@Override
			public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
			}
		});
		/* Gestion des sessions dans Atmosphere (Push Vaadin) */
		servletContext.addListener(SessionSupport.class);

		/* Configure Spring */
		AnnotationConfigWebApplicationContext springContext = new AnnotationConfigWebApplicationContext();
		if (!Boolean.valueOf(servletContext.getInitParameter(Constants.SERVLET_PARAMETER_PRODUCTION_MODE))) {
			springContext.getEnvironment().setActiveProfiles(DEBUG_PROFILE);
		}
		springContext.register(SpringConfig.class);
		servletContext.addListener(new ContextLoaderListener(springContext));
		servletContext.addListener(new RequestContextListener());

		/* Filtre Spring Security */
		FilterRegistration.Dynamic springSecurityFilterChain = servletContext.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
		springSecurityFilterChain.addMappingForUrlPatterns(null, false, "/*");

		/* Filtre passant l'utilisateur courant à Logback */
		FilterRegistration.Dynamic userMdcServletFilter = servletContext.addFilter("userMdcServletFilter", UserMdcServletFilter.class);
		userMdcServletFilter.addMappingForUrlPatterns(null, false, "/*");
		
		/* Filtre Spring Mobile permettant de détecter le device */
		FilterRegistration.Dynamic springMobileServletFilter = servletContext.addFilter("deviceResolverRequestFilter", DeviceResolverRequestFilter.class);
		springMobileServletFilter.addMappingForUrlPatterns(null, false, "/*");

		/* Servlet REST */
		ServletRegistration.Dynamic restServlet = servletContext.addServlet("rest", new DispatcherServlet(springContext));
		restServlet.setLoadOnStartup(1);
		restServlet.addMapping("/rest", "/rest/*");

		/* Servlet Spring-Vaadin */
		//ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", JMeterServlet.class);
		ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", FrenchUnsupportedBrowserHandlerSpringVaadinServlet.class);
		springVaadinServlet.setLoadOnStartup(1);
		springVaadinServlet.addMapping("/*");
		/* Défini le bean UI */
		//springVaadinServlet.setInitParameter("beanName", "mainUI");
		springVaadinServlet.setInitParameter("UIProvider", "fr.univlorraine.mondossierweb.MdwUIProvider");
		/* Utilise les messages Spring pour les messages d'erreur Vaadin (cf. http://vaadin.xpoft.ru/#system_messages) */
		springVaadinServlet.setInitParameter("systemMessagesBeanName", "DEFAULT");
		/* Défini la fréquence du heartbeat en secondes (cf. https://vaadin.com/book/vaadin7/-/page/application.lifecycle.html#application.lifecycle.ui-expiration) */
		springVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, String.valueOf(15));
		//springVaadinServlet.setInitParameter(Constants.PARAMETER_WIDGETSET, "com.vaadin.DefaultWidgetSet");
		springVaadinServlet.setInitParameter(Constants.PARAMETER_WIDGETSET, "fr.univlorraine.mondossierweb.AppWidgetsetDesktop");
		//springVaadinServlet.setInitParameter(Constants.DEFAULT_WIDGETSET, "fr.univlorraine.mondossierweb.AppWidgetset");
		//springVaadinServlet.setInitParameter(Constants.WIDGETSET_DIR_PATH, "VAADIN/widgetsets");
		
		/* Configure le Push */
		springVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_PUSH_MODE, Boolean.valueOf(servletContext.getInitParameter("enablePush")) ? PushMode.AUTOMATIC.name() : PushMode.DISABLED.name());
		/* Active le support des servlet 3 et des requêtes asynchrones (cf. https://vaadin.com/wiki/-/wiki/Main/Working+around+push+issues) */
		springVaadinServlet.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT_SERVLET3, String.valueOf(true));
		/* Active le support des requêtes asynchrones */
		springVaadinServlet.setAsyncSupported(true);
		/* Ajoute l'interceptor Atmosphere permettant de restaurer le SecurityContext dans le SecurityContextHolder (cf. https://groups.google.com/forum/#!msg/atmosphere-framework/8yyOQALZEP8/ZCf4BHRgh_EJ) */
		springVaadinServlet.setInitParameter(ApplicationConfig.ATMOSPHERE_INTERCEPTORS, RecoverSecurityContextAtmosphereInterceptor.class.getName());
	
	
		/* Spring-Vaadin Touchkit Servlet  */
		ServletRegistration.Dynamic springTouchkitVaadinServlet = servletContext.addServlet("springTouchkitVaadin", MDWTouchkitServlet.class);
		//springTouchkitVaadinServlet.setLoadOnStartup(1);
		springTouchkitVaadinServlet.addMapping("/m/*");
		/* Défini le bean UI */
		//springTouchkitVaadinServlet.setInitParameter("beanName", "mdwFallbackTouchkitUI");
		springVaadinServlet.setInitParameter("UIProvider", "fr.univlorraine.mondossierweb.MdwTouchkitUIProvider");
		/* Utilise les messages Spring pour les messages d'erreur Vaadin (cf. http://vaadin.xpoft.ru/#system_messages) */
		springTouchkitVaadinServlet.setInitParameter("systemMessagesBeanName", "DEFAULT");
		/* Défini la fréquence du heartbeat en secondes (cf. https://vaadin.com/book/vaadin7/-/page/application.lifecycle.html#application.lifecycle.ui-expiration) */
		springTouchkitVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, String.valueOf(15));
		//springVaadinServlet.setInitParameter(Constants.PARAMETER_WIDGETSET, "com.vaadin.DefaultWidgetSet");
		springTouchkitVaadinServlet.setInitParameter(Constants.PARAMETER_WIDGETSET, "fr.univlorraine.mondossierweb.AppWidgetset");
		//springTouchkitVaadinServlet.setInitParameter(Constants.DEFAULT_WIDGETSET, "fr.univlorraine.mondossierweb.AppWidgetset");
		//springTouchkitVaadinServlet.setInitParameter(Constants.WIDGETSET_DIR_PATH, "VAADIN/widgetsets");
		
		/* Configure le Push */
	//	springTouchkitVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_PUSH_MODE, Boolean.valueOf(servletContext.getInitParameter("enablePush")) ? PushMode.AUTOMATIC.name() : PushMode.DISABLED.name());
		/* Active le support des servlet 3 et des requêtes asynchrones (cf. https://vaadin.com/wiki/-/wiki/Main/Working+around+push+issues) */
		springTouchkitVaadinServlet.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT_SERVLET3, String.valueOf(true));
		/* Active le support des requêtes asynchrones */
		springTouchkitVaadinServlet.setAsyncSupported(true);
		/* Ajoute l'interceptor Atmosphere permettant de restaurer le SecurityContext dans le SecurityContextHolder (cf. https://groups.google.com/forum/#!msg/atmosphere-framework/8yyOQALZEP8/ZCf4BHRgh_EJ) */
		springTouchkitVaadinServlet.setInitParameter(ApplicationConfig.ATMOSPHERE_INTERCEPTORS, RecoverSecurityContextAtmosphereInterceptor.class.getName());
	
	
	}
	


}
