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


import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
@Theme("valo-ul")
@StyleSheet("mobileView.css")
public class MdwFallbackUI extends UI {
	

	private static final long serialVersionUID = 3269956774322268418L;
	
	
	@Resource
	private transient ApplicationContext applicationContext;
	
    @Override
    protected void init(VaadinRequest request) {

    	Label label = new Label(applicationContext.getMessage("fallbackToDesktop.message", new Object[] {PropertyUtils.getAppUrl()}, getLocale()), ContentMode.HTML);
        VerticalLayout content = new VerticalLayout();
        content.setMargin(true);
        content.addComponent(label);
        setContent(content);

    }

}
