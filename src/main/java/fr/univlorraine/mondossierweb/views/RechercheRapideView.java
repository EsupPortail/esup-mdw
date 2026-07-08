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
package fr.univlorraine.mondossierweb.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import fr.univlorraine.mondossierweb.beans.ResultatDeRecherche;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.tools.elasticsearch.ElasticSearchApogeeService;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Recherche rapide (ElasticSearch)
 */
@SuppressWarnings("serial")
@Route(value = RechercheRapideView.NAME, layout = MainLayout.class)
@Secured({Utils.TEACHER_USER, Utils.GEST_USER, Utils.ADMIN_USER})
public class RechercheRapideView extends VerticalLayout {

	public static final String NAME = "rechercheRapideView";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient RechercheArborescenteController rechercheArborescenteController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient ConfigController configController;

	@Resource
	private transient ElasticSearchApogeeService ElasticSearchService;

	private ComboBox<ResultatDeRecherche> champRecherche;
	private Grid<ResultatDeRecherche> tableResultats;
	private Checkbox casesAcocherComposantes;
	private Checkbox casesAcocherVet;
	private Checkbox casesAcocherElp;
	private Checkbox casesAcocherEtudiant;
	private final List<ResultatDeRecherche> resultats = new ArrayList<>();
	private String derniereSaisie = "";

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		//On vérifie le droit d'accéder à la vue
		if (configController.isApplicationActive() && userController.isEnseignant()) {
			/* Style */
			setPadding(true);
			setSpacing(true);
			setSizeFull();

			//Init connexion à ES, pour gain perf aux premières lettres tapées
			if (ElasticSearchService.initConnexion()) {

				//CHAMP DE RECHERCHE : suggestions ElasticSearch au fil de la saisie
				champRecherche = new ComboBox<>();
				champRecherche.setWidth("700px");
				champRecherche.setMaxWidth("100%");
				champRecherche.setAllowCustomValue(true);
				champRecherche.setPlaceholder(getTranslation(NAME + ".champrecherche.placeholder"));
				champRecherche.setItemLabelGenerator(r -> r.getLib());
				//Affichage des suggestions : libellé + type lisible (comme l'ancien AutoComplete)
				champRecherche.setRenderer(new ComponentRenderer<>(r -> {
					final HorizontalLayout suggestion = new HorizontalLayout();
					suggestion.setWidthFull();
					suggestion.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
					suggestion.setAlignItems(FlexComponent.Alignment.BASELINE);
					final Span typeSpan = new Span(rechercheArborescenteController.getTypeObj(r.type, r.getCode()));
					typeSpan.addClassName("text-secondary");
					suggestion.add(new Span(r.getLib()), typeSpan);
					return suggestion;
				}));
				champRecherche.setItems(query -> {
					derniereSaisie = query.getFilter().orElse("");
					return quickSearch(derniereSaisie).stream()
						.skip(query.getOffset())
						.limit(query.getLimit());
				});
				//Sélection d'une suggestion : accès direct au détail
				champRecherche.addValueChangeListener(e -> {
					if (e.isFromClient() && e.getValue() != null) {
						rechercheController.accessToDetail(e.getValue().getCode(), e.getValue().type, null);
					}
				});
				//Saisie libre : recherche complète via le bouton ou Entrée
				champRecherche.addCustomValueSetListener(e -> {
					derniereSaisie = e.getDetail();
					search(e.getDetail());
				});
				champRecherche.focus();

				//BOUTON DE RECHERCHE
				final Button btnRecherche = new Button(getTranslation("buttonChercher.label"), VaadinIcon.SEARCH.create());
				btnRecherche.addClickShortcut(Key.ENTER);
				btnRecherche.addClickListener(e -> {
					final ResultatDeRecherche selection = champRecherche.getValue();
					if (selection != null) {
						rechercheController.accessToDetail(selection.getCode(), selection.type, null);
					} else {
						search(derniereSaisie);
					}
				});

				//BOUTON RESET
				final Button resetButton = new Button(VaadinIcon.CLOSE.create());
				resetButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
				resetButton.addClassName("btn-reset");
				resetButton.addClickListener(e -> {
					champRecherche.clear();
					champRecherche.focus();
				});

				final HorizontalLayout champRechercheLayout = new HorizontalLayout();
				champRechercheLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
				champRechercheLayout.add(champRecherche, resetButton, btnRecherche);
				add(champRechercheLayout);

				//CASES A COCHER : types d'objets recherchés
				casesAcocherComposantes = new Checkbox(getTranslation(NAME + ".filtre.composantes"));
				casesAcocherComposantes.setValue(true);
				casesAcocherComposantes.addValueChangeListener(e -> tuneSearch());
				casesAcocherVet = new Checkbox(getTranslation(NAME + ".filtre.etapes"));
				casesAcocherVet.setValue(true);
				casesAcocherVet.addValueChangeListener(e -> tuneSearch());
				casesAcocherElp = new Checkbox(getTranslation(NAME + ".filtre.elp"));
				casesAcocherElp.setValue(true);
				casesAcocherElp.addValueChangeListener(e -> tuneSearch());
				casesAcocherEtudiant = new Checkbox(getTranslation(NAME + ".filtre.etudiants"));
				casesAcocherEtudiant.setValue(true);
				casesAcocherEtudiant.addValueChangeListener(e -> tuneSearch());

				final HorizontalLayout checkBoxLayout = new HorizontalLayout();
				checkBoxLayout.setSpacing(true);
				checkBoxLayout.add(casesAcocherComposantes, casesAcocherVet, casesAcocherElp, casesAcocherEtudiant);
				add(checkBoxLayout);

				//TABLE DE RESULTATS
				tableResultats = new Grid<>();
				tableResultats.setSizeFull();
				tableResultats.addComponentColumn(this::createLibelleColumn)
					.setHeader(getTranslation("result.table.lib"))
					.setFlexGrow(3);
				tableResultats.addColumn(r -> rechercheArborescenteController.getTypeObj(r.type, r.getCode()))
					.setHeader(getTranslation("result.table.type"))
					.setFlexGrow(1);
				tableResultats.setVisible(false);
				add(tableResultats);
				setFlexGrow(1, tableResultats);
			} else {
				//Message fonctionnalité indisponible
				add(new Html("<div>" + getTranslation(NAME + ".indisponible.message") + "</div>"));
			}
		}
	}

	private com.vaadin.flow.component.Component createLibelleColumn(final ResultatDeRecherche r) {
		// Libellé cliquable, accès au détail de l'objet
		final Button b = new Button(r.getLib());
		b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		b.addClassName("v-link");
		b.addClickListener(e -> rechercheController.accessToDetail(r.getCode(), r.type, null));

		// Pour un étudiant : affichage de la formation en cours
		if (r.type != null && (r.type.equals(Utils.TYPE_ETU) || r.type.equals(Utils.ETU))) {
			final String formationEnCours = etudiantController.getFormationEnCours(r.getCode());
			if (StringUtils.hasText(formationEnCours)) {
				final HorizontalLayout libhl = new HorizontalLayout();
				libhl.setWidthFull();
				libhl.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
				libhl.setAlignItems(FlexComponent.Alignment.CENTER);
				final Span formation = new Span(formationEnCours);
				formation.addClassName("text-secondary");
				libhl.add(b, formation);
				return libhl;
			}
		}
		return b;
	}

	private List<ResultatDeRecherche> quickSearch(final String valueString) {

		final List<ResultatDeRecherche> listeReponses = new LinkedList<>();

		if (StringUtils.hasText(valueString) && valueString.length() > 2) {

			//appel elasticSearch
			final List<Map<String, Object>> lobjresult = ElasticSearchService.findObj(valueString, Utils.NB_MAX_RESULT_QUICK_SEARCH * 5, true);

			//Liste des types autorisés
			final List<String> listeTypeAutorise = getListeTypesAutorises();

			// recuperation des obj ElasticSearch
			if (lobjresult != null && !listeTypeAutorise.isEmpty()) {
				for (final Map<String, Object> obj : lobjresult) {
					if (listeReponses.size() < Utils.NB_MAX_RESULT_QUICK_SEARCH && obj != null
						&& listeTypeAutorise.contains((String) obj.get(Utils.ES_TYPE))) {
						final ResultatDeRecherche candidat = new ResultatDeRecherche(obj);
						//On évite les doublons
						final boolean doublon = listeReponses.stream()
							.anyMatch(r -> r.lib.toUpperCase().equals(candidat.lib.toUpperCase()));
						if (!doublon) {
							listeReponses.add(candidat);
						}
					}
				}
			}
		}
		return listeReponses;
	}

	private List<String> getListeTypesAutorises() {
		final List<String> listeTypeAutorise = new LinkedList<>();
		if (Boolean.TRUE.equals(casesAcocherComposantes.getValue())) {
			listeTypeAutorise.add(Utils.CMP);
		}
		if (Boolean.TRUE.equals(casesAcocherVet.getValue())) {
			listeTypeAutorise.add(Utils.VET);
		}
		if (PropertyUtils.isEnableRechercheAutoElp() && Boolean.TRUE.equals(casesAcocherElp.getValue())) {
			listeTypeAutorise.add(Utils.ELP);
		}
		if (Boolean.TRUE.equals(casesAcocherEtudiant.getValue())) {
			listeTypeAutorise.add(Utils.ETU);
		}
		return listeTypeAutorise;
	}

	private void search(final String value) {

		if (StringUtils.hasText(value) && value.length() > 1) {

			//appel elasticSearch
			final List<Map<String, Object>> lobjresult = ElasticSearchService.findObj(value, Utils.NB_MAX_RESULT_SEARCH, false);

			// recuperation des objets
			if (lobjresult != null) {
				resultats.clear();
				for (final Map<String, Object> obj : lobjresult) {
					if (obj != null) {
						resultats.add(new ResultatDeRecherche(obj));
					}
				}
				tableResultats.setVisible(true);
				tuneSearch();

				//la recherche n'a qu'un seul résultat : on accède directement au détail
				if (resultats.size() == 1) {
					final ResultatDeRecherche rr = resultats.get(0);
					rechercheController.accessToDetail(rr.getCode(), rr.type, null);
				}
			}
		} else if (StringUtils.hasText(value)) {
			//afficher message erreur
			final Notification notification = Notification.show(getTranslation(NAME + ".erreur.minimum2lettres"));
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		}
	}

	/**
	 * Filtre les résultats affichés selon les types cochés.
	 */
	private void tuneSearch() {
		if (tableResultats != null) {
			final List<String> typesAutorises = new LinkedList<>();
			if (Boolean.TRUE.equals(casesAcocherComposantes.getValue())) {
				typesAutorises.add(Utils.CMP);
			}
			if (Boolean.TRUE.equals(casesAcocherVet.getValue())) {
				typesAutorises.add(Utils.VET);
			}
			if (Boolean.TRUE.equals(casesAcocherElp.getValue())) {
				typesAutorises.add(Utils.ELP);
			}
			if (Boolean.TRUE.equals(casesAcocherEtudiant.getValue())) {
				typesAutorises.add(Utils.ETU);
			}
			tableResultats.setItems(resultats.stream()
				.filter(r -> r.type != null && typesAutorises.stream().anyMatch(t -> r.type.toUpperCase().contains(t.toUpperCase())))
				.toList());
		}
	}
}
