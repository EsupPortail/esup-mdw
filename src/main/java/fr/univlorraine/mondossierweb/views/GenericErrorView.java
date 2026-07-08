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
import lombok.extern.slf4j.Slf4j;

/**
 * Vue d'erreur générique de navigation (remplace l'ancien AppExceptionHandler).
 */
@SuppressWarnings("serial")
@Slf4j
@ParentLayout(MainLayout.class)
public class GenericErrorView extends VerticalLayout implements HasErrorParameter<Exception> {

	public GenericErrorView() {
		setPadding(true);
		setSpacing(true);
	}

	@Override
	public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter<Exception> parameter) {
		log.error("Erreur lors de la navigation vers {}", event.getLocation().getPath(), parameter.getException());
		removeAll();
		add(new H1(getTranslation(ErreurView.NAME + ".title")));
		add(new Html("<div>" + getTranslation(ErreurView.NAME + ".text") + "</div>"));
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
}
