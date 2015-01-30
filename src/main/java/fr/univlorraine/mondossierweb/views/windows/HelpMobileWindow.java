package fr.univlorraine.mondossierweb.views.windows;

import javax.annotation.Resource;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Fenêtre d'aide
 */
@Configurable(preConstruction=true)
public class HelpMobileWindow extends Window {
	private static final long serialVersionUID = -1792808588462463042L;

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;

	/* Composants */
	private Button btnFermer = new Button();
	
	@Getter
	private CheckBox checkBox=new CheckBox();

	

	public void addBtnNonListener(ClickListener clickListener) {
		btnFermer.addClickListener(clickListener);
	}

	public void removeBtnNonListener(ClickListener clickListener) {
		btnFermer.removeClickListener(clickListener);
	}

	/**
	 * Crée une fenêtre de confirmation avec un message et un titre par défaut
	 */
	public HelpMobileWindow() {
		this(null, null);
	}

	/**
	 * Crée une fenêtre de confirmation avec un titre par défaut
	 * @param message
	 */
	public HelpMobileWindow(String message) {
		this(message, null);
	}

	/**
	 * Crée une fenêtre de confirmation
	 * @param message
	 * @param titre
	 */
	public HelpMobileWindow(String message, String titre) {
		/* Style */
		setWidth("90%");
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
			titre = applicationContext.getMessage("helpWindow.defaultTitle", null, getLocale());
		}
		setCaption(titre);

		/* Texte */
		Label textLabel = new Label(message,ContentMode.HTML);
		layout.addComponent(textLabel);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);

		checkBox.setCaption(applicationContext.getMessage("helpWindow.checkBox.mobile.message", null, getLocale()));
		buttonsLayout.addComponent(checkBox);
		buttonsLayout.setComponentAlignment(checkBox, Alignment.MIDDLE_RIGHT);
		
		//btnFermer.setCaption(applicationContext.getMessage("helpWindow.btnFermer", null, getLocale()));
		btnFermer.setIcon(FontAwesome.TIMES);
		btnFermer.setStyleName(ValoTheme.BUTTON_PRIMARY);
		btnFermer.addStyleName("v-popover-button");
		btnFermer.addClickListener(e -> close());
		buttonsLayout.addComponent(btnFermer);
		buttonsLayout.setComponentAlignment(btnFermer, Alignment.MIDDLE_RIGHT);
		buttonsLayout.setExpandRatio(checkBox, 1);


		/* Centre la fenêtre */
		center();
	}

}
