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

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.FieldGroupFieldFactory;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;



/**
 * Fenêtre d'édition des parametre de l'application
 * 
 */
@Configurable(preConstruction=true)
public class PreferencesApplicationWindow extends Window {
	private static final long serialVersionUID = 6446084804910076622L;

	public static final String NAME = "preferencesApplicationWindow";

	public static final String[] CONF_APP_FIELDS_ORDER = {"prefId", "prefDesc", "valeur"};

	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient ConfigController configController;

	/* Composants */
	private BeanFieldGroup<PreferencesApplication> fieldGroup;
	private Button btnEnregistrer;
	private Button btnAnnuler;

	/**
	 * Crée une fenêtre d'édition des preferences applicative
	 * @param préférence à éditer
	 */
	public PreferencesApplicationWindow(PreferencesApplication prefApp) {
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
		setCaption(applicationContext.getMessage(NAME+".title", null, getLocale()));

		/* Formulaire */
		fieldGroup = new BeanFieldGroup<>(PreferencesApplication.class);
		fieldGroup.setItemDataSource(prefApp);



		fieldGroup.setFieldFactory(new FieldGroupFieldFactory() {
			private static final long serialVersionUID = 1L;

			@Override
			@SuppressWarnings("rawtypes")
			public <T extends Field> T createField(Class<?> dataType, Class<T> fieldType) {
				if (fieldType==NativeSelect.class){
					final NativeSelect field = new NativeSelect();
					field.addItem("true");
					field.addItem("false");
					field.setNullSelectionAllowed(false);
					field.setImmediate(true);
					//field.setValue(centre.getTemSrv());
					field.addValueChangeListener(new ValueChangeListener() {						
						@Override
						public void valueChange(ValueChangeEvent event) {
							field.setValue(event.getProperty().getValue());
						}
					});
					return fieldType.cast(field);
				}else{
					final TextField field = new TextField();
					field.setImmediate(true);
					field.addTextChangeListener(new FieldEvents.TextChangeListener() {
						private static final long serialVersionUID = 1L;

						@Override
						public void textChange(TextChangeEvent event) {
							if(!field.isReadOnly()){
								field.setValue(event.getText());
							}
						}
					});
					return fieldType.cast(field);
				}


			}
		});




		FormLayout formLayout = new FormLayout();
		formLayout.setSpacing(true);
		formLayout.setSizeUndefined();
		formLayout.setWidth("100%");
		for (String fieldName : CONF_APP_FIELDS_ORDER) {
			String caption = applicationContext.getMessage(NAME+".confAppTable." + fieldName, null, getLocale());
			//Si on est sur un parametre booleen
			if(fieldName.equals("valeur") && estUneValeurBooleenne(prefApp.getValeur())){
				//On forme le nativeSelect
				Field<?> field = fieldGroup.buildAndBind(caption, fieldName, NativeSelect.class);
				formLayout.addComponent(field);
			}else{
				Field<?> field = fieldGroup.buildAndBind(caption, fieldName);
				if (field instanceof AbstractTextField) {
					((AbstractTextField) field).setNullRepresentation("");
					field.setWidth("100%");
				}
				formLayout.addComponent(field);
			}
		}

		fieldGroup.getField("prefId").setReadOnly(prefApp.getPrefId() != null);
		fieldGroup.getField("prefDesc").setReadOnly(prefApp.getPrefDesc() != null);

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
				/* Enregistre la conf saisie */
				configController.saveAppParameter(prefApp);
				/* Ferme la fenêtre */
				close();
			} catch (CommitException ce) {
			}
		});
		buttonsLayout.addComponent(btnEnregistrer);
		buttonsLayout.setComponentAlignment(btnEnregistrer, Alignment.MIDDLE_RIGHT);

		/* Centre la fenêtre */
		center();
	}

	/**
	 * 
	 * @param val
	 * @return vrai si la valeur est booleenne
	 */
	boolean estUneValeurBooleenne(String val){
		return (StringUtils.hasText(val)&& (val.equals("true") || val.equals("false")));
	}
}
