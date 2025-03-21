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

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.DateField;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.entities.mdw.UtilisateurSwap;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;



/**
 * Fenêtre d'édition des parametre de l'application
 * 
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SwapUtilisateurWindow extends Window {

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
	public void init(UtilisateurSwap swap, boolean ajout) {
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
		btnAnnuler.addStyleName("admin-button");
		buttonsLayout.addComponent(btnAnnuler);
		buttonsLayout.setComponentAlignment(btnAnnuler, Alignment.MIDDLE_LEFT);

		btnEnregistrer = new Button(applicationContext.getMessage(NAME+".btnSave", null, getLocale()), FontAwesome.SAVE);
		btnEnregistrer.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnEnregistrer.addStyleName("admin-button");
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
						//Afficher message d'erreur
						Notification.show(applicationContext.getMessage(NAME+".error.loginexistant",null, UI.getCurrent().getLocale()), Notification.Type.ERROR_MESSAGE);
					}
				}
				if(commitok){
					/* Enregistre le swap saisie */
					configController.saveSwap(swap);

					/* Ferme la fenêtre */
					close();
				}
			} catch (FieldGroup.CommitException ce) {
			}
		});
		buttonsLayout.addComponent(btnEnregistrer);
		buttonsLayout.setComponentAlignment(btnEnregistrer, Alignment.MIDDLE_RIGHT);

		/* Centre la fenêtre */
		center();
	}
}
