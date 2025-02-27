/**
 *
 *  ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package fr.univlorraine.mondossierweb;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.vaadin.server.Constants;
import com.vaadin.shared.communication.PushMode;
import fr.univlorraine.mondossierweb.config.SpringConfig;
import fr.univlorraine.mondossierweb.utils.MDWTouchkitServlet;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.SessionSupport;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

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
	private static final String WIDGETSET_TO_USE = "com.vaadin.v7.Vaadin7WidgetSet";
	// private static final String WIDGETSET_TO_USE = "fr.univlorraine.mondossierweb.AppWidgetset";

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
	 * Ajoute les paramètres de contexte aux propriétés Logback.
	 */
	private void addContextParametersToLogbackConfig(final ServletContext servletContext) {
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		final JoranConfigurator jc = new JoranConfigurator();
		jc.setContext(loggerContext);
		loggerContext.reset();

		final Enumeration<String> parameterNames = servletContext.getInitParameterNames();
		while (parameterNames.hasMoreElements()) {
			final String parameterName = parameterNames.nextElement();
			loggerContext.putProperty("context." + parameterName, servletContext.getInitParameter(parameterName));
		}

		// loggerContext.putProperty("context.log.level", productionMode ? "info" : "trace");

		try {
			final InputStream logbackConfig = getClass().getResourceAsStream("/logback-mdw.xml");
			jc.doConfigure(logbackConfig);
			logbackConfig.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		addContextParametersToSystemProperties(servletContext);

		addContextParametersToLogbackConfig(servletContext);

		/* Configure les sessions */
		Set<SessionTrackingMode> sessionTrackingModes = new HashSet<SessionTrackingMode>();
		sessionTrackingModes.add(SessionTrackingMode.COOKIE);
		//sessionTrackingModes.add(SessionTrackingMode.URL);
		servletContext.setSessionTrackingModes(sessionTrackingModes);
		servletContext.addListener(new HttpSessionListener() {
			@Override
			public void sessionCreated(HttpSessionEvent httpSessionEvent) {
				// sans nouvelle requête, on garde la session active X minutes
				httpSessionEvent.getSession().setMaxInactiveInterval(Integer.parseInt(System.getProperty("context.param.session.maxinactiveinterval")));
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
		/*FilterRegistration.Dynamic userMdcServletFilter = servletContext.addFilter("userMdcServletFilter", UserMdcServletFilter.class);
		userMdcServletFilter.addMappingForUrlPatterns(null, false, "/*");*/

		/* Filtre Spring Mobile permettant de détecter le device */
		/*FilterRegistration.Dynamic springMobileServletFilter = servletContext.addFilter("deviceResolverRequestFilter", DeviceResolverRequestFilter.class);
		springMobileServletFilter.addMappingForUrlPatterns(null, false, "/*");*/

		/* Filtre qui gère les erreurs de fragment */
		/*FilterRegistration.Dynamic fragmentErrorFilter = servletContext.addFilter("fragmentErrorFilter", FragmentErrorFilter.class);
		fragmentErrorFilter.addMappingForUrlPatterns(null, false, "/*");*/

		/* Servlet Spring-Vaadin */
		//ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", JMeterServlet.class);
		//ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", SpringVaadinServlet.class);
		ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", fr.univlorraine.mondossierweb.utils.MdwSpringVaadinServlet.class);
		springVaadinServlet.setLoadOnStartup(1);
		springVaadinServlet.addMapping("/*");
		/* Défini le bean UI */
		// springVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_UI_PROVIDER, "fr.univlorraine.mondossierweb.MdwUIProvider");
		/* Utilise les messages Spring pour les messages d'erreur Vaadin (cf. http://vaadin.xpoft.ru/#system_messages) */
		//springVaadinServlet.setInitParameter("systemMessagesBeanName", "DEFAULT");
		/* Défini la fréquence du heartbeat en secondes (cf. https://vaadin.com/book/vaadin7/-/page/application.lifecycle.html#application.lifecycle.ui-expiration) */
		springVaadinServlet.setInitParameter(Constants.PARAMETER_WIDGETSET, WIDGETSET_TO_USE);
		springVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_HEARTBEAT_INTERVAL, String.valueOf(30));
		springVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_CLOSE_IDLE_SESSIONS, String.valueOf(true));
		/* Configure le Push */
		springVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_PUSH_MODE, Boolean.valueOf(servletContext.getInitParameter("enablePush")) ? PushMode.AUTOMATIC.name() : PushMode.DISABLED.name());
		/* Active le support des servlet 3 et des requêtes asynchrones (cf. https://vaadin.com/wiki/-/wiki/Main/Working+around+push+issues) */
		springVaadinServlet.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT_SERVLET3, String.valueOf(true));
		/* Active le support des requêtes asynchrones */
		springVaadinServlet.setAsyncSupported(true);



		if(Boolean.valueOf(servletContext.getInitParameter("startServletMobile"))){
			/* Spring-Vaadin Touchkit Servlet  */
			ServletRegistration.Dynamic springTouchkitVaadinServlet = servletContext.addServlet("springTouchkitVaadin", MDWTouchkitServlet.class);
			//springTouchkitVaadinServlet.setLoadOnStartup(1);
			springTouchkitVaadinServlet.addMapping("/m/*");
			/* Utilise les messages Spring pour les messages d'erreur Vaadin (cf. http://vaadin.xpoft.ru/#system_messages) */
			//springTouchkitVaadinServlet.setInitParameter("systemMessagesBeanName", "DEFAULT");
			springTouchkitVaadinServlet.setInitParameter(Constants.PARAMETER_WIDGETSET, WIDGETSET_TO_USE);
			springTouchkitVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_CLOSE_IDLE_SESSIONS, String.valueOf(true));

			/* Configure le Push */
			springTouchkitVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_PUSH_MODE, PushMode.DISABLED.name());
			/* Active le support des servlet 3 et des requêtes asynchrones (cf. https://vaadin.com/wiki/-/wiki/Main/Working+around+push+issues) */
			springTouchkitVaadinServlet.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT_SERVLET3, String.valueOf(true));

			/* Active le support des requêtes asynchrones */
			springTouchkitVaadinServlet.setAsyncSupported(true);
			
		}


		/* Servlet REST */
		ServletRegistration.Dynamic restServlet = servletContext.addServlet("deepLinking", new DispatcherServlet(springContext));
		restServlet.setLoadOnStartup(1);
		restServlet.addMapping("/link", "/link/*", "/adminView");
		
	}



}
