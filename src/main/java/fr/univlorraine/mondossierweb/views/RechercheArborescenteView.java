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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.shared.Registration;
import fr.univlorraine.mondossierweb.beans.CollectionDeGroupes;
import fr.univlorraine.mondossierweb.beans.ElpDeCollection;
import fr.univlorraine.mondossierweb.beans.Groupe;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.EtudiantController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.ListeInscritsController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.apogee.Composante;
import fr.univlorraine.mondossierweb.entities.apogee.ElementPedagogique;
import fr.univlorraine.mondossierweb.entities.apogee.VersionDiplome;
import fr.univlorraine.mondossierweb.entities.apogee.VersionEtape;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.entities.vaadin.ObjetBase;
import fr.univlorraine.mondossierweb.services.apogee.ComposanteService;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Recherche arborescente
 */
@SuppressWarnings("serial")
@Route(value = RechercheArborescenteView.NAME, layout = MainLayout.class)
@Secured({Utils.TEACHER_USER, Utils.GEST_USER, Utils.ADMIN_USER})
@Component
@UIScope
public class RechercheArborescenteView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

	public static final String NAME = "rechercheArborescenteView";
	public static final String ROUTE = NAME;

	/** id des noeuds "placeholder" affichant la flèche de dépliage avant chargement des fils */
	private static final String PLACEHOLDER_SUFFIX = "_placeholder__";

	/** Seuil (exclu) sous lequel les éléments secondaires (colonnes code/type, rappel de sélection) sont masqués, aligné sur le seuil CSS 767px */
	private static final int SEUIL_MOBILE_PX = 768;

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheArborescenteController rechercheArborescenteController;
	@Resource
	private transient EtudiantController etudiantController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient ComposanteService composanteService;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient ListeInscritsController listeInscritsController;
	@Resource
	private transient ConfigController configController;

	private TreeGrid<ObjetBase> table;
	private TreeData<ObjetBase> treeData;
	private TreeDataProvider<ObjetBase> treeDataProvider;
	/** Colonnes masquées sur petit écran pour laisser la place à l'arborescence */
	private Grid.Column<ObjetBase> colCode;
	private Grid.Column<ObjetBase> colType;
	/** Enregistrement du listener de redimensionnement de la fenêtre */
	private Registration resizeRegistration;

	/** Vrai quand l'écran est sous le seuil mobile (masque les éléments secondaires) */
	private boolean affichageMobile;
	private final Map<String, ObjetBase> itemsById = new HashMap<>();
	private List<String> markedRows;
	private List<String> liste_types_favoris;
	private List<String> liste_types_inscrits;
	private List<String> liste_types_deplier;
	/** Rappel de la ligne sélectionnée, masqué sur petit écran */
	private VerticalLayout ligneLayout;
	private Span ligneSelectionneeLabel;
	private Span vetElpSelectionneLabel;
	private Span labelLigneSelectionneeLabel;
	private String annee;
	private ComboBox<String> comboBoxAnneeUniv;
	private String code;
	private String type;
	private Button reinitButton;

	/** liste des composantes actives */
	private List<Composante> lcomp;

	private boolean initEffectue;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		if (!configController.isApplicationActive() || !configController.isPartieEnseignantActive()) {
			event.forwardTo(AccesBloqueView.class);
			return;
		}
		//Paramètres de navigation (accès depuis un favori ou la recherche rapide)
		final Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
		final String newCode = params.containsKey("code") ? params.get("code").get(0) : null;
		final String newType = params.containsKey("type") ? params.get("type").get(0) : null;
		if (StringUtils.hasText(newCode) && StringUtils.hasText(newType)
			&& (!newCode.equals(code) || !newType.equals(type))) {
			code = newCode;
			type = newType;
			if (initEffectue) {
				initComposantes();
				reinitButton.setVisible(true);
			}
		}
	}

	/**
	 * Sur chaque (ré-)attachement de la vue (navigation depuis une autre vue),
	 * ré-enregistre le listener de scroll côté client (l'élément DOM est recréé)
	 * et restaure la position de scroll sauvegardée dans {@code window.__mdw_arbo_scroll}.
	 * <p>La variable {@code window} persiste tant que l'onglet reste ouvert :
	 * même portée qu'{@code @UIScope} (une UI = un onglet).
	 */
	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		if (table != null) {
			table.getElement().executeJs(
				"requestAnimationFrame(() => {"
					+ "  var t = this.shadowRoot && this.shadowRoot.querySelector('table');"
					+ "  if (t) {"
					+ "    t.addEventListener('scroll', function(e) { window.__mdw_arbo_scroll = e.target.scrollTop; });"
					+ "    if (window.__mdw_arbo_scroll !== undefined) t.scrollTop = window.__mdw_arbo_scroll;"
					+ "  }"
					+ "});");
		}
		//Éléments secondaires masqués sur petit écran : applique la largeur connue puis suit les redimensionnements
		final UI ui = attachEvent.getUI();
		ui.getPage().retrieveExtendedClientDetails(details -> majAffichageMobile(details.getWindowInnerWidth()));
		resizeRegistration = ui.getPage().addBrowserWindowResizeListener(e -> majAffichageMobile(e.getWidth()));
	}

	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		if (resizeRegistration != null) {
			resizeRegistration.remove();
			resizeRegistration = null;
		}
		super.onDetach(detachEvent);
	}

	/**
	 * Sur petit écran, masque les colonnes code et type ainsi que le rappel de
	 * sélection pour laisser la place à l'arborescence. Un aller-retour serveur
	 * n'a lieu que lorsque le seuil est franchi.
	 */
	private void majAffichageMobile(final int largeur) {
		final boolean mobile = largeur < SEUIL_MOBILE_PX;
		affichageMobile = mobile;
		if (ligneLayout != null && mobile == ligneLayout.isVisible()) {
			ligneLayout.setVisible(!mobile);
		}
		//Ce label est ré-affiché par selectionnerLigne : on le masque sans le ré-afficher au retour desktop
		if (labelLigneSelectionneeLabel != null && mobile) {
			labelLigneSelectionneeLabel.setVisible(false);
		}
		if (colCode != null && colType != null && mobile == colCode.isVisible()) {
			colCode.setVisible(!mobile);
			colType.setVisible(!mobile);
		}
	}

	/**
	 * reinitialise la vue sur la liste des composantes
	 */
	public void initFromScratch() {
		annee = null;
		code = null;
		type = null;
		reinitButton.setVisible(false);
		comboBoxAnneeUniv.setValue(etudiantController.getAnneeUnivEnCours());
		annee = comboBoxAnneeUniv.getValue();
		initComposantes();
	}

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {
		//On vérifie le droit d'accéder à la vue
		if (configController.isApplicationActive() && userController.isEnseignant()) {
			/* Style */
			setPadding(false);
			setSpacing(false);
			setSizeFull();

			liste_types_favoris = List.of(Utils.CMP, Utils.ELP, Utils.VET);
			liste_types_inscrits = List.of(Utils.ELP, Utils.VET);
			liste_types_deplier = List.of(Utils.ELP, Utils.VET);

			recuperationDesfavoris();

			comboBoxAnneeUniv = new ComboBox<>(getTranslation(NAME + ".anneeuniv"));
			comboBoxAnneeUniv.setAllowCustomValue(false);
			//Initialisation de la liste des années
			final List<String> lanneeUniv = rechercheArborescenteController.recupererLesDernieresAnneeUniversitaire();
			if (lanneeUniv != null && !lanneeUniv.isEmpty()) {
				comboBoxAnneeUniv.setItems(lanneeUniv);
				comboBoxAnneeUniv.setItemLabelGenerator(anneeUniv -> anneeUniv + "/" + (Integer.parseInt(anneeUniv) + 1));
				if (annee == null) {
					annee = etudiantController.getAnneeUnivEnCours();
				}
			}
			comboBoxAnneeUniv.setValue(annee);
			comboBoxAnneeUniv.addValueChangeListener(e -> {
				if (e.isFromClient()) {
					changerAnnee(e.getValue());
				}
			});

			reinitButton = new Button(VaadinIcon.CLOSE.create());
			reinitButton.setTooltipText(getTranslation(NAME + ".reinitbutton.description"));
			reinitButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
			reinitButton.addClickListener(e -> initFromScratch());
			reinitButton.setVisible(StringUtils.hasText(code));

			labelLigneSelectionneeLabel = new Span(getTranslation(NAME + ".ligneselectionnee"));
			labelLigneSelectionneeLabel.addClassName("text-secondary");
			labelLigneSelectionneeLabel.setVisible(false);

			ligneSelectionneeLabel = new Span();
			ligneSelectionneeLabel.setVisible(false);
			vetElpSelectionneLabel = new Span();
			vetElpSelectionneLabel.setVisible(false);

			ligneLayout = new VerticalLayout();
			ligneLayout.setPadding(false);
			ligneLayout.setSpacing(false);
			ligneLayout.add(ligneSelectionneeLabel, vetElpSelectionneLabel);

			final HorizontalLayout btnLayout = new HorizontalLayout();
			btnLayout.setWidthFull();
			btnLayout.setPadding(true);
			btnLayout.setAlignItems(FlexComponent.Alignment.CENTER);
			btnLayout.add(comboBoxAnneeUniv, labelLigneSelectionneeLabel, ligneLayout, reinitButton);
			btnLayout.expand(ligneLayout);
			add(btnLayout);

			table = new TreeGrid<>();
			table.setSizeFull();
			table.addClassName("scrollabletable");
			table.setSelectionMode(com.vaadin.flow.component.grid.Grid.SelectionMode.SINGLE);

			table.addHierarchyColumn(obj -> obj.getLibelle() == null ? "" : obj.getLibelle())
				.setHeader(getTranslation(NAME + ".table.libelle"))
				.setFlexGrow(4).setResizable(true);
			colCode = table.addColumn(ObjetBase::getTrueObjectId)
				.setHeader(getTranslation(NAME + ".table.trueObjectId"))
				.setFlexGrow(1).setResizable(true);
			colType = table.addColumn(obj -> isPlaceholder(obj) ? "" : rechercheArborescenteController.getTypeObj(obj.getType(), obj.getTrueObjectId()))
				.setHeader(getTranslation(NAME + ".table.type"))
				.setFlexGrow(1).setResizable(true);
			table.addComponentColumn(this::createActionsColumn)
				.setHeader(getTranslation(NAME + ".table.actions"))
				.setFlexGrow(2).setResizable(true);

			//gestion du style pour les lignes en favori
			table.setPartNameGenerator(obj -> {
				if (obj.getType() != null && markedRows.contains(obj.getType() + ":" + obj.getTrueObjectId())) {
					return "marked";
				}
				return null;
			});

			table.addItemClickListener(event -> {
				if (event.getItem() != null && !isPlaceholder(event.getItem())) {
					selectionnerLigne(event.getItem());
				}
			});

			//gestion du clic sur la fleche pour déplier une entrée
			table.addExpandListener(event -> {
				if (event.isFromClient()) {
					event.getItems().stream().findFirst().ifPresent(obj -> {
						selectionnerLigne(obj);
						deplierNoeud(obj, true);
					});
				}
			});

			initComposantes();

			add(table);
			setFlexGrow(1, table);
			initEffectue = true;
		}
	}

	private boolean isPlaceholder(final ObjetBase obj) {
		return obj.getId() != null && obj.getId().endsWith(PLACEHOLDER_SUFFIX);
	}

	private void recuperationDesfavoris() {
		//Recuperation des favoris
		final List<Favoris> lfav = favorisController.getFavorisFromLogin(userController.getCurrentUserName());
		markedRows = new LinkedList<>();
		for (final Favoris fav : lfav) {
			markedRows.add(fav.getId().getTypfav() + ":" + fav.getId().getIdfav());
		}
	}

	private void initComposantes() {
		treeData = new TreeData<>();
		itemsById.clear();

		//Si aucun code ni type spécifié on récupère toutes les composantes
		if (!StringUtils.hasText(code) || !StringUtils.hasText(type)) {
			if (lcomp == null || lcomp.isEmpty()) {
				lcomp = composanteService.findComposantesEnService();
			}
			for (final Composante comp : lcomp) {
				final ObjetBase obj = new ObjetBase();
				rechercheArborescenteController.renseigneObjFromCmp(obj, comp);
				ajouterItem(obj, null);
			}
			treeDataProvider = new TreeDataProvider<>(treeData);
			table.setDataProvider(treeDataProvider);
		} else {
			//On ajoute l'objet racine et on déplie le premier niveau
			final ObjetBase obj = rechercheArborescenteController.getObj(code, type);
			ajouterItem(obj, null);
			treeDataProvider = new TreeDataProvider<>(treeData);
			table.setDataProvider(treeDataProvider);
			selectionnerLigne(obj);
			deplierNoeud(obj, true);
			table.expand(obj);
		}
	}

	/**
	 * Ajoute un item à l'arbre, avec un fils placeholder si l'élément est potentiellement dépliable.
	 */
	private void ajouterItem(final ObjetBase obj, final ObjetBase parent) {
		if (itemsById.containsKey(obj.getId())) {
			return;
		}
		treeData.addItem(parent, obj);
		itemsById.put(obj.getId(), obj);
		if ("false".equals(obj.getDeplie())) {
			final ObjetBase placeholder = new ObjetBase();
			placeholder.setId(obj.getId() + PLACEHOLDER_SUFFIX);
			placeholder.setLibelle("");
			placeholder.setTrueObjectId("");
			placeholder.setType("");
			placeholder.setDeplie("true");
			treeData.addItem(obj, placeholder);
		}
	}

	private void retirerPlaceholder(final ObjetBase obj) {
		treeData.getChildren(obj).stream()
			.filter(this::isPlaceholder)
			.toList()
			.forEach(treeData::removeItem);
	}

	private void changerAnnee(final String value) {
		annee = value;
		initComposantes();
	}

	private com.vaadin.flow.component.Component createActionsColumn(final ObjetBase item) {
		final HorizontalLayout boutonActionLayout = new HorizontalLayout();
		boutonActionLayout.setSpacing(false);
		if (isPlaceholder(item)) {
			return boutonActionLayout;
		}
		final String typeObj = item.getType();
		final String idObj = item.getTrueObjectId();
		final String idFav = typeObj + ":" + idObj;

		//Si c'est un objet qui peut être mis en favori
		if (typeObj != null && liste_types_favoris.contains(typeObj)) {
			final Button btnFav = new Button();
			majBoutonFavori(btnFav, idFav);
			//Gestion du clic sur le bouton favori
			btnFav.addClickListener(event -> {
				final FavorisPK fpk = new FavorisPK();
				fpk.setLogin(userController.getCurrentUserName());
				fpk.setTypfav(typeObj);
				fpk.setIdfav(idObj);
				if (markedRows.contains(idFav)) {
					//suppression du favori en base
					favorisController.removeFavori(fpk);
					markedRows.remove(idFav);
				} else {
					final Favoris favori = new Favoris();
					favori.setId(fpk);
					//ajout du favori en base
					favorisController.saveFavori(favori);
					markedRows.add(idFav);
				}
				majBoutonFavori(btnFav, idFav);
				treeDataProvider.refreshAll();
			});
			boutonActionLayout.add(btnFav);
		}

		if (typeObj != null && liste_types_deplier.contains(typeObj)) {
			final Button btnDeplier = new Button(VaadinIcon.SITEMAP.create());
			btnDeplier.setTooltipText(getTranslation(NAME + ".deplierarbo"));
			btnDeplier.addClickListener(e -> {
				if (PropertyUtils.isShowLoadingIndicator()) {
					//affichage de la pop-up de loading
					MainLayout.getCurrent().ifPresent(MainLayout::startBusyIndicator);
					final UI ui = UI.getCurrent();
					//Execution de la méthode en parallèle dans un thread
					new Thread(() -> {
						ui.access(() -> deplierTout(item));
						ui.access(() -> MainLayout.getCurrent().ifPresent(MainLayout::stopBusyIndicator));
					}).start();
				} else {
					deplierTout(item);
				}
			});
			boutonActionLayout.add(btnDeplier);
		}

		if (typeObj != null && liste_types_inscrits.contains(typeObj)) {
			final Button btnListeInscrits = new Button(VaadinIcon.USERS.create());
			btnListeInscrits.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			btnListeInscrits.setTooltipText(getTranslation(NAME + ".acceslisteinscrits"));
			btnListeInscrits.addClickListener(e -> rechercheController.accessToDetail(idObj, typeObj, comboBoxAnneeUniv.getValue()));
			boutonActionLayout.add(btnListeInscrits);
		}

		return boutonActionLayout;
	}

	private void majBoutonFavori(final Button btnFav, final String idFav) {
		btnFav.removeThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
		if (markedRows.contains(idFav)) {
			btnFav.setIcon(VaadinIcon.TRASH.create());
			btnFav.addThemeVariants(ButtonVariant.LUMO_ERROR);
			btnFav.setTooltipText(getTranslation(NAME + ".supprimerfavori"));
		} else {
			btnFav.setIcon(VaadinIcon.STAR_O.create());
			btnFav.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			btnFav.setTooltipText(getTranslation(NAME + ".ajouterfavori"));
		}
	}

	private void selectionnerLigne(final ObjetBase item) {
		table.select(item);
		final String typeItemSelected = item.getType();
		ligneSelectionneeLabel.setText(item.getLibelle() + " (" + typeItemSelected + ")");
		//Si c'est un ELP, une collection ou un groupe qui est sélectionné
		if (typeItemSelected.equals(Utils.ELP) || typeItemSelected.equals(Utils.COL) || typeItemSelected.equals(Utils.GRP)) {
			//On va chercher le premier élément père non ELP pour l'afficher en rappel
			final String idItemSelected = item.getId();
			//On récupère l'id complet de l'ELP et on le split pour en parcourir l'arborescence
			final String[] elps = idItemSelected.split("_");
			boolean vetTrouvee = false;
			String idsElp = "";
			String idVet = "";
			for (int i = 0; i < elps.length; i++) {
				if (!vetTrouvee) {
					//Si le type est différent de ELP, GRP et COL
					final String typeItem = elps[i].substring(0, Math.min(3, elps[i].length()));
					if (!typeItem.equals(Utils.ELP) && !typeItem.equals(Utils.COL) && !typeItem.equals(Utils.GRP)) {
						vetTrouvee = true;
						idVet = idItemSelected.replaceAll(idsElp + "_", "");
					} else {
						//tant qu'on n'a pas trouvé la VET on reconstitue la partie gauche de l'id
						if (StringUtils.hasText(idsElp)) {
							idsElp = idsElp + "_";
						}
						idsElp = idsElp + elps[i];
					}
				}
			}
			final ObjetBase vet = itemsById.get(idVet);
			if (vetTrouvee && StringUtils.hasText(idVet) && vet != null) {
				//On passe l'ELP dans le label du bas
				vetElpSelectionneLabel.setText("→ " + ligneSelectionneeLabel.getText());
				vetElpSelectionneLabel.setVisible(true);
				//La VET est affichée dans le label du haut
				ligneSelectionneeLabel.setText(vet.getLibelle() + " (" + vet.getType() + ")");
			}
		} else {
			vetElpSelectionneLabel.setVisible(false);
		}
		ligneSelectionneeLabel.setVisible(true);
		//Sur petit écran, le libellé "Ligne sélectionnée" reste masqué
		labelLigneSelectionneeLabel.setVisible(!affichageMobile);
	}

	public void deplierTout(final ObjetBase item) {
		deplierNoeudComplet(item);
		selectionnerLigne(item);
		table.scrollToItem(item);
	}

	private void deplierNoeudComplet(final ObjetBase item) {
		deplierNoeud(item, false);
		table.expand(item);
		//parcourir les fils, pour chaque fils faire deplierNoeudComplet
		final List<ObjetBase> lfils = List.copyOf(treeData.getChildren(item));
		for (final ObjetBase fils : lfils) {
			if (!isPlaceholder(fils)) {
				deplierNoeudComplet(fils);
			}
		}
	}

	private void deplierNoeud(final ObjetBase item, final boolean afficherMessage) {
		final String typeItem = item.getType();
		final String trueObjectId = item.getTrueObjectId();
		final String objectId = item.getId();

		//Si on n'a pas déjà déplié ou tenté de déplier cet élément
		if (!"false".equals(item.getDeplie())) {
			return;
		}

		if (typeItem.equals(Utils.CMP)) {
			//recuperation des vdi
			final List<VersionDiplome> lvdi = composanteService.findVdiFromComposante(annee, trueObjectId);
			retirerPlaceholder(item);
			if (lvdi != null && !lvdi.isEmpty()) {
				for (final VersionDiplome vdi : lvdi) {
					final ObjetBase obj = new ObjetBase();
					obj.setType(Utils.VDI);
					obj.setId(Utils.VDI + ":" + vdi.getId().getCod_dip() + "/" + vdi.getId().getCod_vrs_vdi() + "_" + objectId);
					obj.setTrueObjectId(vdi.getId().getCod_dip() + "/" + vdi.getId().getCod_vrs_vdi());
					obj.setLibelle(vdi.getLib_web_vdi());
					obj.setDeplie("false");
					ajouterItem(obj, item);
				}
			}
			//maj du Deplie pour la composante
			item.setDeplie("true");
			treeDataProvider.refreshAll();

		} else if (typeItem.equals(Utils.VDI)) {
			//recuperation des vet
			final String[] tabs = trueObjectId.split("/");
			final String codDip = tabs[0];
			final String vrsDip = tabs[1];

			final String[] tabs2 = objectId.split("CMP:");
			final String codCmp = tabs2[1];

			final List<VersionEtape> lvet = composanteService.findVetFromVdiAndCmp(annee, codDip, vrsDip, codCmp);
			retirerPlaceholder(item);
			if (lvet != null && !lvet.isEmpty()) {
				for (final VersionEtape vet : lvet) {
					final ObjetBase obj = new ObjetBase();
					rechercheArborescenteController.renseigneObjFromVet(obj, vet, objectId);
					ajouterItem(obj, item);
				}
			}
			//maj du Deplie pour la VDI
			item.setDeplie("true");
			treeDataProvider.refreshAll();

		} else if (typeItem.equals(Utils.VET) || typeItem.equals(Utils.ELP)) {

			List<ElementPedagogique> lelp = new LinkedList<>();
			if (typeItem.equals(Utils.VET)) {
				//recuperation des elp
				final String[] tabs = trueObjectId.split("/");
				lelp = composanteService.findElpFromVet(tabs[0], tabs[1]);
			}
			retirerPlaceholder(item);
			//true si on est sur un ELP pour lequel on récupère des groupes, false sinon
			boolean elpAvecGroupe = false;
			if (typeItem.equals(Utils.ELP)) {
				//recuperation des elp
				lelp = composanteService.findElpFromElp(trueObjectId);

				//Si ELP n'a pas de fils, on cherche les groupes
				if (lelp == null || lelp.isEmpty()) {
					//On tente de récupérer les groupes de l'ELP
					final List<ElpDeCollection> lgroupes = listeInscritsController.recupererGroupes(annee, trueObjectId);
					//Si on a récupéré des groupes
					if (lgroupes != null && !lgroupes.isEmpty()) {
						//Vrai si on a plusieurs collections (dans ce cas on affiche les collections dans l'arbo)
						boolean plsrsCollection = false;
						//On parcourt l'ELP (un seul ELP dans la liste en vérité)
						for (final ElpDeCollection edc : lgroupes) {
							if (edc.getListeCollection().size() > 1) {
								plsrsCollection = true;
							}
							for (final CollectionDeGroupes cdg : edc.getListeCollection()) {
								ObjetBase parentPourGroupe = item;
								//Si plusieurs collections on ajoute la collection dans la table
								if (plsrsCollection) {
									final ObjetBase obj = new ObjetBase();
									rechercheArborescenteController.renseigneObjFromCollection(obj, cdg, objectId);
									obj.setDeplie("true");
									ajouterItem(obj, item);
									table.expand(obj);
									//On remet le focus sur l'ELP
									selectionnerLigne(item);
									//Le père des groupes sera la collection et non l'elp
									parentPourGroupe = obj;
								}
								//Pour chaque groupe
								for (final Groupe groupe : cdg.getListeGroupes()) {
									elpAvecGroupe = true;
									final ObjetBase obj = new ObjetBase();
									rechercheArborescenteController.renseigneObjFromGroupe(obj, groupe, parentPourGroupe.getId());
									obj.setDeplie("true");
									ajouterItem(obj, parentPourGroupe);
								}
							}
						}
					}
				}
			}

			//Si on a récupéré des ELP
			if (lelp != null && !lelp.isEmpty()) {
				//On parcourt les ELP
				for (final ElementPedagogique elp : lelp) {
					final ObjetBase obj = new ObjetBase();
					rechercheArborescenteController.renseigneObjFromElp(obj, elp, objectId);
					ajouterItem(obj, item);
				}
			} else if (!elpAvecGroupe && afficherMessage) {
				//On affiche le message de notification
				Notification.show(getTranslation(NAME + ".aucunsouselement"));
			}

			//maj du Deplie pour l'element ou la vet
			item.setDeplie("true");
			treeDataProvider.refreshAll();
		}
	}
}
