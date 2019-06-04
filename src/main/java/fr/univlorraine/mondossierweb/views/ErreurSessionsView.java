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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.uicomponents.BasicErreurMessageLayout;

/**
 * Page d'accueil
 */
@Component @Scope("prototype")
@SpringView(name = ErreurSessionsView.NAME)
public class ErreurSessionsView extends VerticalLayout implements View {
	private static final long serialVersionUID = 5118929963964330113L;

	public static final String NAME = "erreurSessionsView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		/* Style */
		setMargin(true);
		setSpacing(true);

		/* Titre */
		Label title = new Label(applicationContext.getMessage(NAME + ".title", null, null));
		title.addStyleName(ValoTheme.LABEL_H1);
		addComponent(title);

		/* Texte */
		addComponent(new Label(applicationContext.getMessage(NAME + ".text", null, null), ContentMode.HTML));

	}

	/**
	 * @see com.vaadin.navigator.View${symbol_pound}enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

}
