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

import fr.univlorraine.mondossierweb.controllers.StructureController;
import fr.univlorraine.mondossierweb.entities.Structure;
import fr.univlorraine.mondossierweb.entities.Structure_;

/**
 * Fenêtre d'édition de structure
 */
@Configurable(preConstruction=true)
public class StructureWindow extends Window {
	private static final long serialVersionUID = 6446084804910076622L;

	public static final String[] STRUCTURE_FIELDS_ORDER = {Structure_.codStr.getName(), Structure_.codApo.getName(), Structure_.codStrMer.getName(), Structure_.libCrt.getName(), Structure_.libJur.getName(), Structure_.libLng.getName(), Structure_.listeNomDns.getName(), Structure_.nomSymbFiler.getName(), Structure_.ptiNom.getName(), Structure_.ptiNomValide.getName(), Structure_.typStr.getName(), Structure_.uai.getName()};

	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient StructureController structureController;

	/* Composants */
	private BeanFieldGroup<Structure> fieldGroup;
	private Button btnEnregistrer;
	private Button btnAnnuler;

	/**
	 * Crée une fenêtre d'édition de structure
	 * @param structureItem l'item de structure à éditer
	 */
	public StructureWindow(Structure structure) {
		/* Style */
		setModal(true);
		setResizable(false);
		setClosable(false);

		/* Layout */
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);

		/* Titre */
		setCaption(applicationContext.getMessage("structure.window", null, getLocale()));

		/* Formulaire */
		fieldGroup = new BeanFieldGroup<>(Structure.class);
		fieldGroup.setItemDataSource(structure);
		FormLayout formLayout = new FormLayout();
		formLayout.setSpacing(true);
		formLayout.setSizeUndefined();
		for (String fieldName : STRUCTURE_FIELDS_ORDER) {
			String caption = applicationContext.getMessage("structure.table." + fieldName, null, getLocale());
			Field<?> field = fieldGroup.buildAndBind(caption, fieldName);
			if (field instanceof AbstractTextField) {
				((AbstractTextField) field).setNullRepresentation("");
			}
			formLayout.addComponent(field);
		}

		fieldGroup.getField(Structure_.codStr.getName()).setReadOnly(structure.getCodStr() != null);

		layout.addComponent(formLayout);

		/* Ajoute les boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		layout.addComponent(buttonsLayout);

		btnAnnuler = new Button(applicationContext.getMessage("structure.window.btnAnnuler", null, getLocale()), FontAwesome.TIMES);
		btnAnnuler.addClickListener(e -> close());
		buttonsLayout.addComponent(btnAnnuler);
		buttonsLayout.setComponentAlignment(btnAnnuler, Alignment.MIDDLE_LEFT);

		btnEnregistrer = new Button(applicationContext.getMessage("structure.window.btnSave", null, getLocale()), FontAwesome.SAVE);
		btnEnregistrer.addStyleName(ValoTheme.BUTTON_PRIMARY);
		btnEnregistrer.addClickListener(e -> {
			try {
				/* Valide la saisie */
				fieldGroup.commit();
				/* Enregistre la structure saisie */
				structureController.saveStructure(structure);
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
