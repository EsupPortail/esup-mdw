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
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
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
	 * Crée une fenêtre d'édition de structure
	 * @param structure la structure à éditer
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
		FormLayout formLayout = new FormLayout();
		formLayout.setSpacing(true);
		formLayout.setSizeUndefined();
		formLayout.setWidth("100%");
		for (String fieldName : CONF_APP_FIELDS_ORDER) {
			String caption = applicationContext.getMessage(NAME+".confAppTable." + fieldName, null, getLocale());
			Field<?> field = fieldGroup.buildAndBind(caption, fieldName);
			if (field instanceof AbstractTextField) {
				((AbstractTextField) field).setNullRepresentation("");
				field.setWidth("100%");
			}
			formLayout.addComponent(field);
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
				/* Enregistre la structure saisie */
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

}
