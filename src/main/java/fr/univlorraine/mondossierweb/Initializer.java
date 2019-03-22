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
import fr.univlorraine.mondossierweb.utils.MDWTouchkitServlet;
import fr.univlorraine.tools.logback.UserMdcServletFilter;

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
		//sessionTrackingModes.add(SessionTrackingMode.URL);
		servletContext.setSessionTrackingModes(sessionTrackingModes);
		servletContext.addListener(new HttpSessionListener() {
			@Override
			public void sessionCreated(HttpSessionEvent httpSessionEvent) {
				// sans nouvelle requête, on garde la session active 4 minutes
				httpSessionEvent.getSession().setMaxInactiveInterval(240);
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



		/* Servlet Spring-Vaadin */
		//ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", JMeterServlet.class);
		//ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", SpringVaadinServlet.class);
		ServletRegistration.Dynamic springVaadinServlet = servletContext.addServlet("springVaadin", fr.univlorraine.mondossierweb.utils.MdwSpringVaadinServlet.class);
		springVaadinServlet.setLoadOnStartup(1);
		springVaadinServlet.addMapping("/*");
		/* Défini le bean UI */
		//springVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_UI_PROVIDER, "fr.univlorraine.mondossierweb.MdwUIProvider");
		/* Utilise les messages Spring pour les messages d'erreur Vaadin (cf. http://vaadin.xpoft.ru/#system_messages) */
		springVaadinServlet.setInitParameter("systemMessagesBeanName", "DEFAULT");
		/* Défini la fréquence du heartbeat en secondes (cf. https://vaadin.com/book/vaadin7/-/page/application.lifecycle.html#application.lifecycle.ui-expiration) */
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
			springTouchkitVaadinServlet.setInitParameter("systemMessagesBeanName", "DEFAULT");
			springTouchkitVaadinServlet.setInitParameter(Constants.PARAMETER_WIDGETSET, "fr.univlorraine.mondossierweb.AppWidgetset");
			springTouchkitVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_CLOSE_IDLE_SESSIONS, String.valueOf(true));

			/* Configure le Push */
			springTouchkitVaadinServlet.setInitParameter(Constants.SERVLET_PARAMETER_PUSH_MODE, PushMode.DISABLED.name());
			/* Active le support des servlet 3 et des requêtes asynchrones (cf. https://vaadin.com/wiki/-/wiki/Main/Working+around+push+issues) */
			springTouchkitVaadinServlet.setInitParameter(ApplicationConfig.WEBSOCKET_SUPPORT_SERVLET3, String.valueOf(true));

			/* Active le support des requêtes asynchrones */
			springTouchkitVaadinServlet.setAsyncSupported(true);
			
		}


		/* Servlet REST */
		ServletRegistration.Dynamic restServlet = servletContext.addServlet("link", new DispatcherServlet(springContext));
		restServlet.setLoadOnStartup(1);
		restServlet.addMapping("/link", "/link/*");
	}



}
