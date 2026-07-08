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
package fr.univlorraine.mondossierweb.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

/**
 * Vue affichée lorsque l'accès à une vue est refusé (AccessDeniedException
 * levée par ViewSecurityService).
 */
@SuppressWarnings("serial")
@ParentLayout(MainLayout.class)
public class AccessDeniedErrorView extends VerticalLayout implements HasErrorParameter<AccessDeniedException> {

	public AccessDeniedErrorView() {
		setPadding(true);
		setSpacing(true);
	}

	@Override
	public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter<AccessDeniedException> parameter) {
		removeAll();
		add(new H1(getTranslation(AccesRefuseView.NAME + ".title")));
		add(new Html("<div>" + getTranslation(AccesRefuseView.NAME + ".message") + "</div>"));
		return HttpServletResponse.SC_FORBIDDEN;
	}
}
