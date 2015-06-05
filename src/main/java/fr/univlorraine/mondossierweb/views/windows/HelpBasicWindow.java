package fr.univlorraine.mondossierweb.views.windows;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.views.AssistanceView;

/**
 * Fenêtre d'aide basique
 */
@Configurable(preConstruction=true)
public class HelpBasicWindow extends Window {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2974754443259576179L;
	
	public static final String NAME = "helpBasicWindow";

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient ConfigController configController;

	/* Composants */
	private Button btnFermer = new Button();



	public void addBtnNonListener(ClickListener clickListener) {
		btnFermer.addClickListener(clickListener);
	}

	public void removeBtnNonListener(ClickListener clickListener) {
		btnFermer.removeClickListener(clickListener);
	}

	/**
	 * Crée une fenêtre de confirmation avec un message et un titre par défaut
	 */
	public HelpBasicWindow() {
		this(null, null);
	}

	/**
	 * Crée une fenêtre de confirmation avec un titre par défaut
	 * @param message
	 */
	public HelpBasicWindow(String message) {
		this(message, null);
	}

	public HelpBasicWindow(String message, String titre) {
		this(message, titre, false);
	}
	/**
	 * Crée une fenêtre de confirmation
	 * @param message
	 * @param titre
	 */
	public HelpBasicWindow(String message, String titre,boolean displayLienContact) {
		/* Style */
		setWidth(900, Unit.PIXELS);
		setModal(true);
		setResizable(false);
		setClosable(false);

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(false);
		setContent(layout);

		/* Titre */
		setCaption(titre);

		// Lien de contact
		if(displayLienContact){
			String mailContact = configController.getAssistanceContactMail();
			if(StringUtils.hasText(mailContact)){
				Button contactBtn = new Button(applicationContext.getMessage(NAME + ".btnContact", null, getLocale()), FontAwesome.ENVELOPE);
				contactBtn.addStyleName(ValoTheme.BUTTON_LINK);
				BrowserWindowOpener contactBwo = new BrowserWindowOpener("mailto:" + mailContact);
				contactBwo.extend(contactBtn);
				layout.addComponent(contactBtn);
				layout.setComponentAlignment(contactBtn, Alignment.TOP_RIGHT);
			}
		}

		/* Texte */
		Label textLabel = new Label(message,ContentMode.HTML);
		layout.addComponent(textLabel);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);

		btnFermer.setCaption(applicationContext.getMessage("helpWindow.btnFermer", null, getLocale()));
		btnFermer.setIcon(FontAwesome.TIMES);
		btnFermer.addClickListener(e -> close());
		buttonsLayout.addComponent(btnFermer);
		buttonsLayout.setComponentAlignment(btnFermer, Alignment.MIDDLE_RIGHT);


		/* Centre la fenêtre */
		center();
	}

}
