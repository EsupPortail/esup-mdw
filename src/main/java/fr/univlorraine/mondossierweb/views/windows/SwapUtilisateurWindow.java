/*
 * ESUP-Portail MONDOSSIERWEB - Copyright (c) 2016 ESUP-Portail consortium
 */
package fr.univlorraine.mondossierweb.views.windows;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;



/**
 * Fenêtre d'édition des parametre de l'application
 * 
 */
@Configurable(preConstruction=true)
public class SwapUtilisateurWindow extends Window {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "swapUtilisateurWindow";

	public static final String[] CONF_APP_FIELDS_ORDER = {"loginSource", "loginCible", "datCre"};

	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient ConfigController configController;

	/* Composants */
	private BeanFieldGroup<UtilisateurSwap> fieldGroup;
	private Button btnEnregistrer;
	private Button btnAnnuler;

	/**
	 * Crée une fenêtre d'édition du swap utilisateur
	 * @param swap utilisateur à éditer
	 */
	public SwapUtilisateurWindow(UtilisateurSwap swap, boolean ajout) {
		/* Style */
		setModal(true);
		setResizable(false);
		setClosable(false);
		setWidth("50%");


		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.setWidth("100%");
		setContent(layout);

		/* Titre */
		if(ajout){
			setCaption(applicationContext.getMessage(NAME+".title.ajout", null, getLocale()));
		}else{
			setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));
		}
		

		/* Formulaire */
		fieldGroup = new BeanFieldGroup<>(UtilisateurSwap.class);
		fieldGroup.setItemDataSource(swap);
		FormLayout formLayout = new FormLayout();
		formLayout.setSpacing(true);
		formLayout.setSizeUndefined();
		formLayout.setWidth("100%");
		for (String fieldName : CONF_APP_FIELDS_ORDER) {
			String caption = applicationContext.getMessage(NAME+".swapUserTable." + fieldName, null, getLocale());
			Field<?> field = fieldGroup.buildAndBind(caption, fieldName);
			if (field instanceof AbstractTextField) {
				((AbstractTextField) field).setNullRepresentation("");
				field.setWidth("100%");
			}
			if (field instanceof DateField) {
				((DateField) field).setResolution(DateField.RESOLUTION_MIN);
			}
			formLayout.addComponent(field);
		}

		//fieldGroup.getField("loginCible").setReadOnly(swap.getLoginCible() != null);
		fieldGroup.getField("loginSource").setReadOnly(swap.getLoginSource() != null);

		layout.addComponent(formLayout);

		/* Ajoute les boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		buttonsLayout.setMargin(true);
		layout.addComponent(buttonsLayout);

		btnAnnuler = new Button(applicationContext.getMessage(NAME+".btnAnnuler", null, getLocale()), FontAwesome.TIMES);
		btnAnnuler.addClickListener(e -> close());
		buttonsLayout.addComponent(btnAnnuler);
		buttonsLayout.setComponentAlignment(btnAnnuler, Alignment.MIDDLE_LEFT);

		btnEnregistrer = new Button(applicationContext.getMessage(NAME+".btnSave", null, getLocale()), FontAwesome.SAVE);
		btnEnregistrer.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnEnregistrer.addClickListener(e -> {
			try {
				/* Valide la saisie */
				fieldGroup.commit();
				boolean commitok=true;
				if(ajout){
					//On regarde si le login source n'est pas déjà dans la table
					String loginSource = (String)fieldGroup.getField("loginSource").getValue();
					if(configController.getSwapUtilisateur(loginSource)!=null){
						commitok=false;
						//afficher message d'erreur
						Notification.show(applicationContext.getMessage(NAME+".error.loginexistant",null, UI.getCurrent().getLocale()), Notification.Type.ERROR_MESSAGE);
					}
				}
				if(commitok){
					/* Enregistre le swap saisie */
					configController.saveSwap(swap);

					/* Ferme la fenêtre */
					close();
				}
			} catch (CommitException ce) {
			}
		});
		buttonsLayout.addComponent(btnEnregistrer);
		buttonsLayout.setComponentAlignment(btnEnregistrer, Alignment.MIDDLE_RIGHT);

		/* Centre la fenêtre */
		center();
	}

}
