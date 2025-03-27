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
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.fieldgroup.BeanFieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroupFieldFactory;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.event.FieldEvents;
import com.vaadin.v7.ui.AbstractTextField;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.NativeSelect;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplication;
import fr.univlorraine.mondossierweb.entities.mdw.PreferencesApplicationValeurs;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.Resource;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Locale;



/**
 * Fenêtre d'édition des parametre de l'application
 * 
 */
@SuppressWarnings("serial")
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PreferencesApplicationWindow extends Window {

	public static final String NAME = "preferencesApplicationWindow";
	public static final String[] CONF_APP_FIELDS_ORDER = {"prefId", "prefDesc", "valeur"};
	public static final String[]  PAV_FIELD_ORDER = {"valeur"}; 

	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient ConfigController configController;
	private BeanItemContainer<PreferencesApplicationValeurs> pavContainer;
	
	/* Composants */
	private BeanFieldGroup<PreferencesApplication> fieldGroup;
	private Button btnEnregistrer;
	private Button btnAnnuler;

	/**
	 * Crée une fenêtre d'édition des preferences applicative
	 * @param prefApp à éditer
	 */
	public void init(PreferencesApplication prefApp) {
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

			@Override
			@SuppressWarnings("rawtypes")
			public <T extends Field> T createField(Class<?> dataType, Class<T> fieldType) {
				if (fieldType == NativeSelect.class){
					final NativeSelect field = new NativeSelect();
					field.addItem("true");
					field.addItem("false");
					field.setNullSelectionAllowed(false);
					field.setImmediate(true);
					//field.setValue(centre.getTemSrv());
					field.addValueChangeListener(new Property.ValueChangeListener() {
						@Override
						public void valueChange(Property.ValueChangeEvent event) {
							field.setValue(event.getProperty().getValue());
						}
					});
					return fieldType.cast(field);
				}else{
					final TextField field = new TextField();
					field.setImmediate(true);
					field.addTextChangeListener(new FieldEvents.TextChangeListener() {

						@Override
						public void textChange(FieldEvents.TextChangeEvent event) {
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
			if(fieldName.equals("valeur") && prefApp.getType().equals(Utils.PARAM_TYP_BOOLEAN)){
				//On forme le nativeSelect
				Field<?> field = fieldGroup.buildAndBind(caption, fieldName, NativeSelect.class);
				formLayout.addComponent(field);
			}else{
				//Si on est sur un parametre List
				if(fieldName.equals("valeur") && prefApp.getType().equals(Utils.PARAM_TYP_LIST)){

					//On forme le modificateur de liste
					VerticalLayout listlayout= new VerticalLayout();
					listlayout.setWidth("100%");
					HorizontalLayout nvl= new HorizontalLayout();
					nvl.setWidth("100%");
					//champ de saisie
					final TextField listInputField = new TextField();
					listInputField.setImmediate(true);
					listInputField.setNullRepresentation("");
					listInputField.setWidth("100%");
					nvl.addComponent(listInputField);
					//Bouton add
					Button btnAddInput = new Button(applicationContext.getMessage(NAME+".list.addbtn", null, Locale.getDefault()));
					btnAddInput.setIcon(FontAwesome.PLUS);
					btnAddInput.addClickListener(e->addListValueToPrefApp( prefApp,listInputField));
					nvl.addComponent(btnAddInput);
					listlayout.addComponent(nvl);
					nvl.setExpandRatio(listInputField, 1);
					//bouton supprimer
					Button boutonSupprimerPav = new Button(applicationContext.getMessage(NAME+".btnPavDelete", null, Locale.getDefault()));
					boutonSupprimerPav.setEnabled(false);
					boutonSupprimerPav.setIcon(FontAwesome.MINUS);
					//affichage des pav
					pavContainer = new BeanItemContainer<PreferencesApplicationValeurs>(PreferencesApplicationValeurs.class);
					refreshPavContainer(prefApp);
					Table pavTable = new Table(null, pavContainer);
					pavTable.setWidth("100%");
					pavTable.setHeight("200px");
					pavTable.setVisibleColumns((Object[]) PAV_FIELD_ORDER);
					pavTable.setColumnHeader("valeur", "Valeur");
					pavTable.setSelectable(true);
					pavTable.setImmediate(true);
					pavTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {
						@Override
						public void containerItemSetChange(Container.ItemSetChangeEvent event) {
							pavTable.sanitizeSelection();
						}
					});
					pavTable.addValueChangeListener(new Property.ValueChangeListener() {
						@Override
						public void valueChange(Property.ValueChangeEvent event) {
							boutonSupprimerPav.setEnabled(true);
						}

					});
					boutonSupprimerPav.addClickListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent event) {
							if(pavTable.getValue()!=null &&  pavTable.getValue() instanceof PreferencesApplicationValeurs){
								PreferencesApplicationValeurs pavChoisie =  ((PreferencesApplicationValeurs)pavTable.getValue());
								supprimerPav(prefApp, pavChoisie);
							}
						}

					});
					listlayout.addComponent(pavTable);
					listlayout.setComponentAlignment(pavTable, Alignment.MIDDLE_LEFT);
					listlayout.addComponent(boutonSupprimerPav);

					formLayout.addComponent(listlayout);


				}else{
					Field<?> field = fieldGroup.buildAndBind(caption, fieldName);
					if (field instanceof AbstractTextField) {
						((AbstractTextField) field).setNullRepresentation("");
						field.setWidth("100%");
					}
					formLayout.addComponent(field);
				}
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
		btnAnnuler.addStyleName("admin-button");
		buttonsLayout.setComponentAlignment(btnAnnuler, Alignment.MIDDLE_LEFT);

		btnEnregistrer = new Button(applicationContext.getMessage(NAME+".btnSave", null, getLocale()), FontAwesome.SAVE);
		btnEnregistrer.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnEnregistrer.addStyleName("admin-button");
		btnEnregistrer.addClickListener(e -> {
			try {
				/* Valide la saisie */
				fieldGroup.commit();
				/* Enregistre la conf saisie */
				configController.saveAppParameter(prefApp);
				/* Ferme la fenêtre */
				close();
			} catch (FieldGroup.CommitException ce) {
			}
		});
		buttonsLayout.addComponent(btnEnregistrer);
		buttonsLayout.setComponentAlignment(btnEnregistrer, Alignment.MIDDLE_RIGHT);

		/* Centre la fenêtre */
		center();
	}

	private void refreshPavContainer(PreferencesApplication prefApp) {
		pavContainer.removeAllItems();
		if(prefApp.getPreferencesApplicationValeurs()!=null && !prefApp.getPreferencesApplicationValeurs().isEmpty()){
			for(PreferencesApplicationValeurs pav : prefApp.getPreferencesApplicationValeurs()){
				pavContainer.addBean(pav);
			}
		}
		
	}

	/* vajout d'un element */
	private void addListValueToPrefApp(PreferencesApplication prefApp,TextField tf){
		boolean ajouter = true;
		String value = tf.getValue();
		if(prefApp.getPreferencesApplicationValeurs()==null){
			prefApp.setPreferencesApplicationValeurs(new LinkedList<PreferencesApplicationValeurs> ());
		}else{
			for(PreferencesApplicationValeurs p : prefApp.getPreferencesApplicationValeurs()){
				if(p.getValeur()!=null && p.getValeur().equals(value)){
					ajouter = false;
				}
			}
		}
		tf.setValue(null);
		if(ajouter){
			PreferencesApplicationValeurs pav = new PreferencesApplicationValeurs();
			pav.setValeur(value);
			pav.setPreferencesApplication(prefApp);
			prefApp.getPreferencesApplicationValeurs().add(pav);
			refreshPavContainer(prefApp);
		}else{
			Notification.show(applicationContext.getMessage(NAME+".notification.valeur.presente",null, getLocale()));
		}
	}


	private void supprimerPav( PreferencesApplication prefApp, PreferencesApplicationValeurs pavChoisie) {
		if(prefApp!=null && prefApp.getPreferencesApplicationValeurs()!=null && pavChoisie!=null && StringUtils.hasText(pavChoisie.getValeur())){
			if(prefApp.getPreferencesApplicationValeurs().size()==1){
				//On vérifie que c'est bien l'élément à supprimer
				PreferencesApplicationValeurs p = prefApp.getPreferencesApplicationValeurs().get(0);
				if(p.getValeur()!=null && p.getValeur().equals(pavChoisie.getValeur())){
					prefApp.setPreferencesApplicationValeurs(null);
				}
			}else{
				int i=0;
				int rangToDelete = 0;
				boolean pavToDeleted = false;
				for(PreferencesApplicationValeurs p : prefApp.getPreferencesApplicationValeurs()){
					//On test sur la valur car il se peut que l'objet n'ai pas encore d'id
					if(!pavToDeleted && p.getValeur()!=null && p.getValeur().equals(pavChoisie.getValeur())){
						rangToDelete = i;
						pavToDeleted = true;
					}
					i++;
				}
				if(pavToDeleted){
					prefApp.getPreferencesApplicationValeurs().remove(rangToDelete);
				}
			}
		}
		refreshPavContainer(prefApp);
	}
}
