package fr.univlorraine.mondossierweb.views;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import ru.xpoft.vaadin.VaadinView;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import fr.univlorraine.mondossierweb.controllers.StructureController;
import fr.univlorraine.mondossierweb.entities.Structure;
import fr.univlorraine.mondossierweb.entities.Structure_;
import fr.univlorraine.tools.vaadin.EntityPushListener;
import fr.univlorraine.tools.vaadin.EntityPusher;

/**
 * Page de gestion des structures
 */
@Component @Scope("prototype")
@VaadinView(StructuresView.NAME)
public class StructuresView extends VerticalLayout implements View, EntityPushListener<Structure> {
	private static final long serialVersionUID = -6491779626961549383L;

	public static final String NAME = "structuresView";

	public static final String[] STRUCTURE_FIELDS_ORDER = {Structure_.codStr.getName(), Structure_.codApo.getName(), Structure_.codStrMer.getName(), Structure_.libCrt.getName(), Structure_.libJur.getName(), Structure_.libLng.getName(), Structure_.listeNomDns.getName(), Structure_.nomSymbFiler.getName(), Structure_.ptiNom.getName(), Structure_.ptiNomValide.getName(), Structure_.typStr.getName(), Structure_.uai.getName()};

	/* Injections */
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient StructureController structureController;
	@Resource
	private transient EntityPusher<Structure> structureEntityPusher;

	/* Composants */
	private Button btnExport;
	private Button btnNouveau;
	private Button btnEdit;
	private Button btnSupprimer;
	private Table structureTable;

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		/* Style */
		setSizeFull();
		setMargin(true);
		setSpacing(true);

		/* Boutons */
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setWidth(100, Unit.PERCENTAGE);
		buttonsLayout.setSpacing(true);
		addComponent(buttonsLayout);

		HorizontalLayout leftButtonsLayout = new HorizontalLayout();
		leftButtonsLayout.setSpacing(true);
		buttonsLayout.addComponent(leftButtonsLayout);
		buttonsLayout.setComponentAlignment(leftButtonsLayout, Alignment.MIDDLE_LEFT);

		btnExport = new Button(applicationContext.getMessage("structure.btnExport", null, getLocale()), FontAwesome.FILE_PDF_O);
		btnExport.addStyleName(ValoTheme.BUTTON_PRIMARY);
		FileDownloader fd = new FileDownloader(structureController.getStructuresPdfResource());
		fd.extend(btnExport);
		leftButtonsLayout.addComponent(btnExport);

		btnNouveau = new Button(applicationContext.getMessage("structure.btnNouveau", null, getLocale()), FontAwesome.PLUS);
		btnNouveau.addClickListener(e -> structureController.editNewStructure());
		leftButtonsLayout.addComponent(btnNouveau);

		btnEdit = new Button(applicationContext.getMessage("structure.btnEdit", null, getLocale()), FontAwesome.PENCIL);
		btnEdit.setEnabled(false);
		btnEdit.addClickListener(e -> {
			if (structureTable.getValue() instanceof Structure) {
				structureController.editStructure((Structure) structureTable.getValue());
			}
		});
		buttonsLayout.addComponent(btnEdit);
		buttonsLayout.setComponentAlignment(btnEdit, Alignment.MIDDLE_CENTER);

		btnSupprimer = new Button(applicationContext.getMessage("structure.btnDelete", null, getLocale()), FontAwesome.TRASH_O);
		btnSupprimer.setEnabled(false);
		btnSupprimer.addClickListener(e -> {
			if (structureTable.getValue() instanceof Structure) {
				structureController.deleteStructure((Structure) structureTable.getValue());
			}
		});
		buttonsLayout.addComponent(btnSupprimer);
		buttonsLayout.setComponentAlignment(btnSupprimer, Alignment.MIDDLE_RIGHT);

		/* Table des structures */
		structureTable = new Table(null, new BeanItemContainer<>(Structure.class, structureController.getStructures()));
		structureTable.setSizeFull();
		structureTable.setVisibleColumns((Object[]) STRUCTURE_FIELDS_ORDER);
		for (String fieldName : STRUCTURE_FIELDS_ORDER) {
			structureTable.setColumnHeader(fieldName, applicationContext.getMessage("structure.table." + fieldName, null, getLocale()));
		}
		structureTable.setSortContainerPropertyId(Structure_.codStr.getName());
		structureTable.setColumnCollapsingAllowed(true);
		structureTable.setColumnReorderingAllowed(true);
		structureTable.setSelectable(true);
		structureTable.setImmediate(true);
		structureTable.addItemSetChangeListener(e -> structureTable.sanitizeSelection());
		structureTable.addValueChangeListener(e -> {
			/* Les boutons d'édition et de suppression de structure sont actifs seulement si une structure est sélectionnée. */
			boolean structureIsSelected = structureTable.getValue() instanceof Structure;
			btnEdit.setEnabled(structureIsSelected);
			btnSupprimer.setEnabled(structureIsSelected);
		});
		structureTable.addItemClickListener(e -> {
			if (e.isDoubleClick()) {
				structureTable.select(e.getItemId());
				btnEdit.click();
			}
		});
		addComponent(structureTable);
		setExpandRatio(structureTable, 1);

		/* Inscrit la vue aux mises à jour de structures */
		structureEntityPusher.registerEntityPushListener(this);
	}

	/**
	 * @see com.vaadin.navigator.View#enter(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
	 */
	@Override
	public void enter(ViewChangeEvent event) {
	}

	/**
	 * @see com.vaadin.ui.AbstractComponent#detach()
	 */
	@Override
	public void detach() {
		/* Désinscrit la vue des mises à jour de structures */
		structureEntityPusher.unregisterEntityPushListener(this);

		super.detach();
	}

	/**
	 * @see fr.univlorraine.tools.vaadin.EntityPushListener#entityPersisted(java.lang.Object)
	 */
	@Override
	public void entityPersisted(Structure entity) {
		structureTable.removeItem(entity);
		structureTable.addItem(entity);
		structureTable.sort();
	}

	/**
	 * @see fr.univlorraine.tools.vaadin.EntityPushListener#entityUpdated(java.lang.Object)
	 */
	@Override
	public void entityUpdated(Structure entity) {
		structureTable.removeItem(entity);
		structureTable.addItem(entity);
		structureTable.sort();
	}

	/**
	 * @see fr.univlorraine.tools.vaadin.EntityPushListener#entityDeleted(java.lang.Object)
	 */
	@Override
	public void entityDeleted(Structure entity) {
		structureTable.removeItem(entity);
	}

}
