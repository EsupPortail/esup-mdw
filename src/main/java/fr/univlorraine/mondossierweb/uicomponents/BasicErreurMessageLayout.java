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
package fr.univlorraine.mondossierweb.uicomponents;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.context.ApplicationContext;


@SuppressWarnings("serial")
public class BasicErreurMessageLayout extends VerticalLayout {


	private static final String ERREURVIEW_NAME = "erreurView";

	private transient ApplicationContext applicationContext;

	public BasicErreurMessageLayout(ApplicationContext context){
		super();
		applicationContext = context;
		/* Style */
		setMargin(true);
		setSpacing(true);

		/* Titre */
		H1 title = new H1(applicationContext.getMessage(ERREURVIEW_NAME + ".title", null, null));
		add(title);

		/* Texte */
		add(new Html("<div>" + applicationContext.getMessage(ERREURVIEW_NAME + ".text", null, null) + "</div>"));
	}

}
