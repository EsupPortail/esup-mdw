package fr.univlorraine.mondossierweb.controllers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.query.JRJpaQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRSwapFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.vaadin.server.StreamResource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

import fr.univlorraine.mondossierweb.MainUI;
import fr.univlorraine.mondossierweb.entities.Structure;
import fr.univlorraine.mondossierweb.repositories.StructureRepository;
import fr.univlorraine.mondossierweb.views.windows.ConfirmWindow;
import fr.univlorraine.mondossierweb.views.windows.StructureWindow;

/**
 * Gestion de l'entité Structure
 */
@Component
public class StructureController {
	private Logger logger = LoggerFactory.getLogger(StructureController.class);

	/* Injections */
	@Resource
	private transient Environment environment;
	@Resource
	private transient ApplicationContext applicationContext;
	@Resource
	private transient LockController lockController;
	@Resource
	private transient StructureRepository structureRepository;
	@PersistenceContext (unitName="entityManagerFactory")
	private transient EntityManager entityManager;

	/**
	 * @return liste des structures
	 */
	public List<Structure> getStructures() {
		return structureRepository.findAll();
	}

	/**
	 * Ouvre une fenêtre d'édition d'une nouvelle structure.
	 */
	public void editNewStructure() {
		UI.getCurrent().addWindow(new StructureWindow(new Structure()));
	}

	/**
	 * Ouvre une fenêtre d'édition de structure.
	 * @param structure
	 */
	public void editStructure(Structure structure) {
		if (structure == null) {
			throw new IllegalArgumentException("structure ne peut être null.");
		}

		/* Verrou */
		if (!lockController.getLockOrNotify(structure, null)) {
			return;
		}

		UI.getCurrent().addWindow(new StructureWindow(structure));
	}

	/**
	 * Enregistre une structure
	 * @param structure
	 */
	public void saveStructure(Structure structure) {
		if (structure == null) {
			throw new IllegalArgumentException("structure ne peut être null.");
		}

		/* Verrou */
		if (!lockController.getLockOrNotify(structure, null)) {
			return;
		}

		structureRepository.saveAndFlush(structure);
		lockController.releaseLock(structure);
		MainUI.getCurrent().getGoogleAnalyticsTracker().trackEvent(getClass().getSimpleName(), "Save");
	}

	/**
	 * Supprime une structure
	 * @param structure
	 */
	public void deleteStructure(Structure structure) {
		if (structure == null) {
			throw new IllegalArgumentException("structure ne peut être null.");
		}

		/* Verrou */
		if (!lockController.getLockOrNotify(structure, null)) {
			return;
		}

		ConfirmWindow confirmWindow = new ConfirmWindow(applicationContext.getMessage("structure.window.confirmDelete", new Object[]{structure.getCodStr()}, UI.getCurrent().getLocale()), applicationContext.getMessage("structure.window.confirmDeleteTitle", null, UI.getCurrent().getLocale()));
		confirmWindow.addBtnOuiListener(e -> {
			/* Contrôle que le client courant possède toujours le lock */
			if (lockController.getLockOrNotify(structure, null)) {
				structureRepository.delete(structure);
				MainUI.getCurrent().getGoogleAnalyticsTracker().trackEvent(getClass().getSimpleName(), "Delete");
			}
		});
		confirmWindow.addCloseListener(e -> {
			/* Suppression du lock */
			lockController.releaseLock(structure);
		});
		UI.getCurrent().addWindow(confirmWindow);
	}

	/**
	 * @return export PDF de toutes les structures
	 */
	public com.vaadin.server.Resource getStructuresPdfResource() {
		StreamResource.StreamSource source = new StreamResource.StreamSource() {
			private static final long serialVersionUID = 1618675892058226366L;

			@Override
			public InputStream getStream() {
				/* Chargement de la template Jasper */
				InputStream templateIS = getClass().getResourceAsStream("/jasper/structures_report.jasper");
				if (templateIS == null) {
					logger.error("Impossible de charger la template de l'export.");
					Notification.show(applicationContext.getMessage("jasper.templateError", null, UI.getCurrent().getLocale()), Type.ERROR_MESSAGE);
					return null;
				}

				try {
					Map<String, Object> reportParameters = new HashMap<String, Object>();
					/* Connexion JPA */
					reportParameters.put(JRJpaQueryExecuterFactory.PARAMETER_JPA_ENTITY_MANAGER, entityManager);
					/* Fichier temporaire */
					JRVirtualizer virtualizer = new JRSwapFileVirtualizer(200, new JRSwapFile(System.getProperty("java.io.tmpdir"), 1024, 1024), true);
					reportParameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
					/* Autres paramètres passés au report */
					reportParameters.put("appName", environment.getRequiredProperty("app.name"));
					/* Création de l'export */
					byte[] reportBytes = JasperRunManager.runReportToPdf(templateIS, reportParameters);
					MainUI.getCurrent().getGoogleAnalyticsTracker().trackEvent(getClass().getSimpleName(), "Export");

					return new ByteArrayInputStream(reportBytes);
				} catch (JRException e) {
					logger.error("Erreur lors de la génération de l'export.", e);
					Notification.show(applicationContext.getMessage("jasper.generationError", null, UI.getCurrent().getLocale()), Type.ERROR_MESSAGE);
					return null;
				}
			}
		};

		/* Création de la ressource */
		StreamResource resource = new StreamResource(source, "export-structure.pdf");
		resource.setMIMEType("application/pdf");
		resource.setCacheTime(0);
		return resource;
	}

}
