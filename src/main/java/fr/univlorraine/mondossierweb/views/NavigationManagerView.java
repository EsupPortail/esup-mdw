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

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.MdwTouchkitUI;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Page des notes sur mobile
 */
@SuppressWarnings("serial")
@Component @Scope("prototype")
@SpringView(name = NavigationManagerView.NAME)
public class NavigationManagerView extends VerticalLayout implements View {
	public static final String NAME = "NavigationManagerView";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	private VerticalLayout currentView;
	private VerticalLayout firstView;
	private VerticalLayout nextView;
	
	
	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

	}
	
	public Object getCurrentView() {
		return currentView;
	}
	
	public void refresh(){
		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI){
			removeAllComponents();
			/* Style */
			setMargin(false);
			setSpacing(false);
			setSizeFull();
			currentView = firstView;
			addComponent(firstView);
		}
	}
	
	public void navigateToNextView() {
		//On vérifie le droit d'accéder à la vue
		if(UI.getCurrent() instanceof MdwTouchkitUI){
			removeAllComponents();
			/* Style */
			setMargin(false);
			setSpacing(false);
			setSizeFull();
			currentView = nextView;
			addComponent(nextView);
		}
		
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {
	}
	
	public void setFirstComponent(VerticalLayout currentView) {
		this.firstView = currentView;
		refresh();
		
	}
	public void setNextComponent(VerticalLayout nextView) {
		this.nextView = nextView;
		
	}
	public Object getFirstComponent() {
		return this.firstView;
	}
	public void navigateBack() {
		refresh();
	}

}
