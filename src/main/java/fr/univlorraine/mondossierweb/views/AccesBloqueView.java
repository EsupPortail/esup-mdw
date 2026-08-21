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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;

/**
 * Page accès bloqué (maintenance)
 */
@SuppressWarnings("serial")
@Route(value = AccesBloqueView.NAME, layout = MainLayout.class)
public class AccesBloqueView extends VerticalLayout implements HasDynamicTitle {

	public static final String NAME = "accesBloqueView";
	public static final String ROUTE = NAME;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		/* Style */
		setPadding(true);
		setSpacing(true);

		/* Titre */
		add(new H1(getTranslation(NAME + ".title")));

		/* Texte */
		add(new Html("<div>" + getTranslation(NAME + ".message") + "</div>"));
	}
}
