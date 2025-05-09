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
package fr.univlorraine.mondossierweb.utils;


import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.DefaultUIProvider;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.UIProvider;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.server.SpringUIProvider;
import com.vaadin.spring.server.SpringVaadinServlet;
import fr.univlorraine.mondossierweb.MdwUIProvider;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("serial")
@Slf4j
public class MdwSpringVaadinServlet extends SpringVaadinServlet {

	@Override
    protected void servletInitialized() throws ServletException {
		super.servletInitialized();

		getService().setSystemMessagesProvider(smi -> {
			WebApplicationContext webApplicationContext = WebApplicationContextUtils
                    .getWebApplicationContext(getServletContext());
			final Locale locale = smi.getLocale();
			final CustomizedSystemMessages messages = new CustomizedSystemMessages();
			messages.setSessionExpiredCaption(webApplicationContext.getMessage("vaadin.sessionExpired.caption", null, locale));
			messages.setSessionExpiredMessage(webApplicationContext.getMessage("vaadin.sessionExpired.message", null, locale));
			messages.setCommunicationErrorCaption(webApplicationContext.getMessage("vaadin.communicationError.caption", null, locale));
			messages.setCommunicationErrorMessage(webApplicationContext.getMessage("vaadin.communicationError.message", null, locale));
			messages.setAuthenticationErrorCaption(webApplicationContext.getMessage("vaadin.authenticationError.caption", null, locale));
			messages.setAuthenticationErrorMessage(webApplicationContext.getMessage("vaadin.authenticationError.message", null, locale));
			messages.setInternalErrorCaption(webApplicationContext.getMessage("vaadin.internalError.caption", null, locale));
			messages.setInternalErrorMessage(webApplicationContext.getMessage("vaadin.internalError.message", null, locale));
			messages.setCookiesDisabledCaption(webApplicationContext.getMessage("vaadin.cookiesDisabled.caption", null, locale));
			messages.setCookiesDisabledMessage(webApplicationContext.getMessage("vaadin.cookiesDisabled.message", null, locale));
			/* Don't show any SessionExpired messages, redirect immediately to the session expired URL */
			messages.setSessionExpiredNotificationEnabled(true);
			/* Don't show any CommunicationError message, reload the page instead */
			messages.setCommunicationErrorNotificationEnabled(true);
			return messages;
		});

		
		
        getService().addSessionInitListener(new SessionInitListener() {

            @Override
            public void sessionInit(SessionInitEvent event) throws ServiceException {
                // remove DefaultUIProvider instances to avoid mapping
                // extraneous UIs if e.g. a servlet is declared as a nested
                // class in a UI class
               	VaadinSession session = event.getSession();
               List<UIProvider> uiProviders = new ArrayList<UIProvider>(session.getUIProviders());
                for (UIProvider provider : uiProviders) {
                    // use canonical names as these may have been loaded with
                    // different classloaders
                   if (DefaultUIProvider.class.getCanonicalName().equals(provider.getClass().getCanonicalName())
				   || SpringUIProvider.class.getCanonicalName().equals(provider.getClass().getCanonicalName())) {
                        session.removeUIProvider(provider);
                    }

                }
                // add Spring UI provider
				MdwUIProvider uiProvider = new MdwUIProvider(session);
                session.addUIProvider(uiProvider);

				log.info("####UI Provider : "+event.getSession().getUIProviders().size()+"  -  "+event.getSession().getUIProviders() + " ####");
            }
        });

		log.info("################## MdwSpringVaadinServlet servletInitialized DONE ##################");
    }
	
}
