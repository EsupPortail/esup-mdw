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

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import jakarta.annotation.Resource;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Fenêtre de saisie
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InputWindow extends Window {

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	/* Composants */
	private TextField inputTextField = new TextField();
	private Button btnOk = new Button();
	private Button btnCancel = new Button();

	/** Listeners */
	@Getter
	private Set<BtnOkListener> btnOkListeners = new LinkedHashSet<>();

	public void addBtnOkListener(BtnOkListener btnOkListener) {
		btnOkListeners.add(btnOkListener);
	}

	public void removeBtnOkListener(BtnOkListener btnOkListener) {
		btnOkListeners.remove(btnOkListener);
	}

	public void addBtnCancelListener(Button.ClickListener clickListener) {
		btnCancel.addClickListener(clickListener);
	}

	public void removeBtnCancelListener(Button.ClickListener clickListener) {
		btnCancel.removeClickListener(clickListener);
	}

	/**
	 * Crée une fenêtre de saisie avec un message et un titre par défaut
	 */
	public void init() {
		init(null, null);
	}

	/**
	 * Crée une fenêtre de saisie avec un titre par défaut
	 * @param message
	 */
	public void init(String message) {
		init(message, null);
	}

	/**
	 * Crée une fenêtre de saisie
	 * @param message
	 * @param titre
	 */
	public void init(String message, String titre) {
		/* Style */
		setWidth(400, Unit.PIXELS);
		setModal(true);
		setResizable(false);
		setClosable(false);

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);

		/* Titre */
		if (titre == null) {
			titre = applicationContext.getMessage("inputWindow.defaultTitle", null, getLocale());
		}
		setCaption(titre);

		/* Texte */
		if (message == null) {
			message = applicationContext.getMessage("inputWindow.defaultMessage", null, getLocale());
		}
		Label textLabel = new Label(message);
		layout.addComponent(textLabel);

		/* Champ de saisie */
		inputTextField.setWidth(100, Unit.PERCENTAGE);
		inputTextField.addShortcutListener(new ShortcutListener(null, ShortcutAction.KeyCode.ENTER, null) {

			@Override
			public void handleAction(Object sender, Object target) {
				btnOk.click();
			}
		});
		layout.addComponent(inputTextField);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);

		btnCancel.setCaption(applicationContext.getMessage("inputWindow.btnCancel", null, getLocale()));
		btnCancel.addClickListener(e -> close());
		buttonsLayout.addComponent(btnCancel);
		buttonsLayout.setComponentAlignment(btnCancel, Alignment.MIDDLE_LEFT);

		btnOk.setCaption(applicationContext.getMessage("inputWindow.btnOk", null, getLocale()));
		btnOk.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnOk.addClickListener(e -> {
			btnOkListeners.forEach(l -> l.btnOkClick(inputTextField.getValue()));
			close();
		});
		buttonsLayout.addComponent(btnOk);
		buttonsLayout.setComponentAlignment(btnOk, Alignment.MIDDLE_RIGHT);

		/* Centre la fenêtre */
		center();
		/* Place le focus sur le champ de saisie */
		inputTextField.focus();
	}

	/**
	 * Interface pour les listeners du bouton ok.
	 */
	public interface BtnOkListener extends Serializable {
		/**
		 * Appelé lorsque Ok est cliqué.
		 */
		public void btnOkClick(String text);
	}
}
