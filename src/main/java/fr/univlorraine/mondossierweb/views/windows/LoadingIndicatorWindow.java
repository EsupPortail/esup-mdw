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
package fr.univlorraine.mondossierweb.views.windows;


import com.vaadin.ui.Alignment;
import com.vaadin.ui.Window;
import com.vaadin.v7.ui.ProgressBar;
import com.vaadin.v7.ui.VerticalLayout;

@SuppressWarnings("serial")
public class LoadingIndicatorWindow extends Window {
	public LoadingIndicatorWindow() {
		super();
		setModal(true);
		setDraggable(false);
		setResizable(false);
		setClosable(false);
		addStyleName("loadingIndicatorWindow");

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);

		ProgressBar busyIndicator = new ProgressBar();
		busyIndicator.setIndeterminate(true);
		layout.addComponent(busyIndicator);
		layout.setComponentAlignment(busyIndicator, Alignment.MIDDLE_CENTER);

		center();
	}
}
