/**
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2015 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.views.windows;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class LoadingIndicatorWindow extends Window {

	private static final long serialVersionUID = 1931039365675290813L;

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
