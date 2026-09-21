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

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import fr.univlorraine.mondossierweb.controllers.ConfigController;
import fr.univlorraine.mondossierweb.controllers.FavorisController;
import fr.univlorraine.mondossierweb.controllers.RechercheArborescenteController;
import fr.univlorraine.mondossierweb.controllers.RechercheController;
import fr.univlorraine.mondossierweb.controllers.UserController;
import fr.univlorraine.mondossierweb.entities.mdw.Favoris;
import fr.univlorraine.mondossierweb.entities.mdw.FavorisPK;
import fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList;
import fr.univlorraine.mondossierweb.utils.PropertyUtils;
import fr.univlorraine.mondossierweb.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;

import java.util.LinkedList;
import java.util.List;

/**
 * Favoris
 */
@SuppressWarnings("serial")
@Route(value = FavorisView.NAME, layout = MainLayout.class)
@Secured({Utils.TEACHER_USER, Utils.GEST_USER, Utils.ADMIN_USER})
@Slf4j
public class FavorisView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

	public static final String NAME = "favorisView";
	public static final String ROUTE = NAME;

	/* Injections */
	@Resource
	private transient UserController userController;
	@Resource
	private transient RechercheArborescenteController rechercheArborescenteController;
	@Resource
	private transient FavorisController favorisController;
	@Resource
	private transient RechercheController rechercheController;
	@Resource
	private transient ConfigController configController;

	private List<String> liste_types_inscrits;

	private List<String> liste_type_arbo;

	private ResponsiveCardList<Favoris> favorisList;

	private List<Favoris> lfav;

	private HorizontalLayout labelAucunFavoriLayout;

	@Override
	public String getPageTitle() {
		return getTranslation(NAME + ".title");
	}

	/**
	 * Initialise la vue
	 */
	@PostConstruct
	public void init() {

		//On vérifie le droit d'accéder à la vue
		if (configController.isApplicationActive() && userController.isEnseignant()) {
			removeAll();
			/* Style */
			setPadding(true);
			setSpacing(true);
			setSizeFull();

			liste_types_inscrits = new LinkedList<>();
			liste_types_inscrits.add(Utils.ELP);
			liste_types_inscrits.add(Utils.VET);

			liste_type_arbo = new LinkedList<>();
			liste_type_arbo.add(Utils.CMP);
			liste_type_arbo.add(Utils.VET);

			lfav = favorisController.getFavoris();

			VerticalLayout globalLayout = new VerticalLayout();
			globalLayout.setPadding(false);
			globalLayout.setSizeFull();

			if (lfav != null && !lfav.isEmpty()) {
				favorisList = new ResponsiveCardList<>();
				favorisList.setSizeFull();

				favorisList.addColumn(fav -> rechercheArborescenteController.getTypeObj(fav.getId().getTypfav(), fav.getId().getIdfav()),
					getTranslation(NAME + ".table.id.typfav"))
					.setFlexGrow(1).setResizable(true);
				favorisList.addColumn(fav -> fav.getId().getIdfav(),
					getTranslation(NAME + ".table.id.idfav"))
					.setFlexGrow(1).setResizable(true);
				favorisList.addColumn(fav -> favorisController.getLibObjFavori(fav.getId().getTypfav(), fav.getId().getIdfav()),
					getTranslation(NAME + ".table.id.libelle"))
					.setFlexGrow(3).setResizable(true);
				favorisList.addComponentColumn(this::createActionsColumn,
					getTranslation(NAME + ".table.id.actions"))
					.setFlexGrow(2).setResizable(true);

				/* Cartes affichées sur petit écran */
				favorisList.setCardRenderer(this::createFavoriCard);
				favorisList.setItems(lfav);
				globalLayout.add(favorisList);
				globalLayout.setFlexGrow(1, favorisList);
			}

			labelAucunFavoriLayout = new HorizontalLayout();
			labelAucunFavoriLayout.setPadding(true);
			labelAucunFavoriLayout.setSizeFull();
			Span aucunFavoris = new Span(getTranslation(NAME + ".favoris.aucun"));
			aucunFavoris.addClassName("font-bold");
			labelAucunFavoriLayout.add(aucunFavoris);
			labelAucunFavoriLayout.setVisible(lfav == null || lfav.isEmpty());
			globalLayout.add(labelAucunFavoriLayout);

			add(globalLayout);
		}
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		if (!configController.isApplicationActive() || !configController.isPartieEnseignantActive()) {
			event.forwardTo(AccesBloqueView.class);
		}
	}

	private HorizontalLayout createActionsColumn(final Favoris fav) {
		final String typeObj = fav.getId().getTypfav();
		final String idObj = fav.getId().getIdfav();

		HorizontalLayout boutonActionLayout = new HorizontalLayout();
		boutonActionLayout.setSpacing(false);

		Button btnfav = new Button(VaadinIcon.TRASH.create());
		btnfav.addThemeVariants(ButtonVariant.LUMO_ERROR);
		btnfav.addClassName("deletefavbutton");
		btnfav.setTooltipText(getTranslation(NAME + ".suppressionfavori"));
		//Gestion du clic sur le bouton de suppression du favori
		btnfav.addClickListener(event -> supprimerFavori(fav));
		boutonActionLayout.add(btnfav);

		// Si on peut accéder à l'arborescence depuis le favori
		if (peutAccederArborescence(typeObj)) {
			Button btnArbo = new Button(VaadinIcon.SITEMAP.create());
			btnArbo.setTooltipText(getTranslation(NAME + ".accesarborescence"));
			btnArbo.addClickListener(e -> accederArborescence(idObj, typeObj));
			boutonActionLayout.add(btnArbo);
		}
		// Si on peut accéder à la liste des inscrits depuis le favori
		if (peutAccederListeInscrits(typeObj)) {
			Button btnListeInscrits = new Button(VaadinIcon.USERS.create());
			btnListeInscrits.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			btnListeInscrits.setTooltipText(getTranslation(NAME + ".acceslisteinscrits"));
			btnListeInscrits.addClickListener(e -> accederListeInscrits(idObj, typeObj));
			boutonActionLayout.add(btnListeInscrits);
		}
		return boutonActionLayout;
	}

	/** Supprime le favori de l'utilisateur courant et met à jour l'affichage. */
	private void supprimerFavori(final Favoris fav) {
		final FavorisPK fpk = new FavorisPK();
		fpk.setIdfav(fav.getId().getIdfav());
		fpk.setLogin(userController.getCurrentUserName());
		fpk.setTypfav(fav.getId().getTypfav());
		favorisController.removeFavori(fpk);
		lfav.remove(fav);
		favorisList.setItems(lfav);
		if (lfav.isEmpty()) {
			favorisList.setVisible(false);
			labelAucunFavoriLayout.setVisible(true);
		}
	}

	/** Vrai si le type de favori donne accès à l'arborescence. */
	private boolean peutAccederArborescence(final String typeObj) {
		return typeObj != null && liste_type_arbo != null && liste_type_arbo.contains(typeObj);
	}

	/** Vrai si le type de favori donne accès à la liste des inscrits. */
	private boolean peutAccederListeInscrits(final String typeObj) {
		return typeObj != null && liste_types_inscrits != null && liste_types_inscrits.contains(typeObj);
	}

	/** Ouvre l'arborescence sur l'élément du favori. */
	private void accederArborescence(final String idObj, final String typeObj) {
		rechercheController.accessToRechercheArborescente(idObj, typeObj);
	}

	/** Ouvre la liste des inscrits de l'élément du favori. */
	private void accederListeInscrits(final String idObj, final String typeObj) {
		//Si on doit afficher une fenêtre de loading pendant l'exécution
		if (PropertyUtils.isShowLoadingIndicator()) {
			//affichage de la pop-up de loading
			MainLayout.getCurrent().ifPresent(MainLayout::startBusyIndicator);
			final UI ui = UI.getCurrent();
			//Execution de la méthode en parallèle dans un thread
			new Thread(() -> {
				ui.access(() -> rechercheController.accessToDetail(idObj, typeObj, null));
				ui.access(() -> MainLayout.getCurrent().ifPresent(MainLayout::stopBusyIndicator));
			}).start();
		} else {
			//On ne doit pas afficher de fenêtre de loading, on exécute directement la méthode
			rechercheController.accessToDetail(idObj, typeObj, null);
		}
	}

	/**
	 * Remplit le menu d'actions d'un favori (affiché sur petit écran au clic sur le libellé).
	 */
	private void remplirMenuActions(final ContextMenu menu, final Favoris fav) {
		final String typeObj = fav.getId().getTypfav();
		final String idObj = fav.getId().getIdfav();

		if (peutAccederListeInscrits(typeObj)) {
			menu.addItem(
				actionMenuItem(VaadinIcon.USERS, getTranslation(NAME + ".acceslisteinscrits")),
				e -> accederListeInscrits(idObj, typeObj));
		}
		if (peutAccederArborescence(typeObj)) {
			menu.addItem(
				actionMenuItem(VaadinIcon.SITEMAP, getTranslation(NAME + ".accesarborescence")),
				e -> accederArborescence(idObj, typeObj));
		}
		menu.addItem(
			actionMenuItem(VaadinIcon.TRASH, getTranslation(NAME + ".suppressionfavori")),
			e -> supprimerFavori(fav));
	}

	/** Construit l'icône et le libellé textuel d'une entrée du menu d'actions. */
	private HorizontalLayout actionMenuItem(final VaadinIcon icone, final String libelle) {
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		final Icon icon = icone.create();
		icon.setSize("var(--lumo-icon-size-s)");
		layout.add(icon, new Span(libelle));
		return layout;
	}

	/**
	 * Construit la carte d'un favori, affichée sur petit écran :
	 * badge du type + code, libellé cliquable ouvrant le menu des actions.
	 */
	private Div createFavoriCard(final Favoris fav) {
		final String typeObj = fav.getId().getTypfav();
		final String idObj = fav.getId().getIdfav();

		final Div card = new Div();
		card.addClassName("card");

		/* Ligne 1 : badge du type + code de l'objet */
		final HorizontalLayout ligneEntete = new HorizontalLayout();
		ligneEntete.setSpacing(true);
		final Span badgeType = new Span(rechercheArborescenteController.getTypeObj(typeObj, idObj));
		badgeType.addClassName("card-badge");
		if (Utils.ELP.equals(typeObj)) {
			badgeType.addClassName("card-badge-elp");
		} else {
			badgeType.addClassName("card-badge-arbo");
		}
		ligneEntete.add(badgeType, new Span(idObj));

		/* Ligne 2 : libellé, cliquable pour ouvrir le menu des actions */
		final Span libelle = new Span(favorisController.getLibObjFavori(typeObj, idObj));
		libelle.addClassName("font-bold");
		libelle.addClassName("libelle-actions-menu");

		final ContextMenu menu = new ContextMenu(libelle);
		menu.setOpenOnClick(true);
		//En-tête rappelant l'élément cliqué : libellé puis identifiant technique
		final Span enteteLibelle = new Span(favorisController.getLibObjFavori(typeObj, idObj));
		enteteLibelle.addClassName("menu-actions-entete");
		final Span enteteId = new Span(idObj);
		enteteId.addClassName("menu-actions-entete-id");
		final VerticalLayout entete = new VerticalLayout(enteteLibelle, enteteId);
		entete.setPadding(false);
		entete.setSpacing(false);
		menu.addItem(entete);
		remplirMenuActions(menu, fav);

		card.add(ligneEntete, libelle);
		return card;
	}

}
