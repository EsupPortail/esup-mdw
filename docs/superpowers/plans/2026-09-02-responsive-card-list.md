# ResponsiveCardList Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remplacer les Grid illisibles sur mobile par un mécanisme réutilisable qui bascule automatiquement entre un Grid (grand écran) et une liste de cartes 2-3 lignes (petit écran), pilote sur FavorisView.

**Architecture:** Composant `ResponsiveCardList<T> extends Div` contenant le Grid ET une liste de cartes ; la bascule est une media query CSS (`max-width: 767px`), sans JS ni aller-retour serveur. Rendu des cartes automatique (une ligne « En-tête : valeur » par colonne) ou custom par vue (`setCardRenderer`). Les données sont synchronisées grid/cartes via `setItems`/`refresh`.

**Tech Stack:** Vaadin 25.1.9 (Flow), Spring Boot 4, Java 21, CSS global existant.

## Global Constraints

- Vaadin 25.1.9 / Flow, Spring Boot 4.0.5, Java 21
- Pas de framework de tests dans le projet → vérification = `mvn compile` + tests manuels DevTools
- Indentation par tabulations, en-tête de licence ESUP copié des fichiers existants, commentaires en français
- CSS global : `src/main/resources/META-INF/resources/css/styles.css` (chargé via `@StyleSheet("css/styles.css")` sur `Application.java`)
- Messages i18n : `src/main/resources/i18n/messages-default.properties` (clés `favorisView.*` existantes, aucune clé nouvelle nécessaire)
- Seuil de bascule : `max-width: 767px`
- Style des commits du repo : messages courts, minuscules, en français (ex. « maj affichage des grids »)

---

### Task 1: Composant ResponsiveCardList + CSS responsive

**Files:**
- Create: `src/main/java/fr/univlorraine/mondossierweb/uicomponents/ResponsiveCardList.java`
- Modify: `src/main/resources/META-INF/resources/css/styles.css` (ajout en fin de fichier)

**Interfaces:**
- Consumes: rien (composant autonome, API Grid de Vaadin Flow)
- Produces: `fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList<T>` avec :
  - `public Grid.Column<T> addColumn(ValueProvider<T, ?> provider, String header)`
  - `public Grid.Column<T> addComponentColumn(ValueProvider<T, ? extends Component> provider, String header)`
  - `public void setItems(List<T> newItems)`
  - `public void refresh()`
  - `public void setCardRenderer(Function<T, Component> cardRenderer)`
  - Classe CSS posée sur le conteneur : `responsive-card-list`

- [ ] **Step 1: Créer la classe ResponsiveCardList**

Créer `src/main/java/fr/univlorraine/mondossierweb/uicomponents/ResponsiveCardList.java` :

```java
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
package fr.univlorraine.mondossierweb.uicomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.function.ValueProvider;

/**
 * Conteneur réutilisable affichant des données sous deux formes : un Grid
 * classique (grand écran) ou une liste de cartes lisibles sur 2-3 lignes
 * (petit écran). La bascule est assurée par une media query CSS.
 * Par défaut, chaque carte reprend une ligne « En-tête : valeur » par
 * colonne ; une vue peut fournir un renderer custom via
 * {@link #setCardRenderer(Function)}.
 *
 * @param <T> type des enregistrements affichés
 */
@SuppressWarnings("serial")
public class ResponsiveCardList<T> extends Div {

	private final Grid<T> grid = new Grid<>();
	private final Div cardList = new Div();

	private final List<ColumnInfo<T>> columnInfos = new ArrayList<>();
	private List<T> items = List.of();
	private Function<T, Component> cardRenderer;

	private record ColumnInfo<T>(String header, ValueProvider<T, ?> provider) {
	}

	public ResponsiveCardList() {
		addClassName("responsive-card-list");
		grid.addClassName("responsive-grid");
		cardList.addClassName("card-list");
		add(grid, cardList);
	}

	/**
	 * Ajoute une colonne texte au Grid (grand écran) et au rendu auto des cartes.
	 */
	public Grid.Column<T> addColumn(final ValueProvider<T, ?> provider, final String header) {
		columnInfos.add(new ColumnInfo<>(header, provider));
		return grid.addColumn(provider).setHeader(header);
	}

	/**
	 * Ajoute une colonne de composants au Grid (grand écran) et au rendu auto des cartes.
	 */
	public Grid.Column<T> addComponentColumn(final ValueProvider<T, ? extends Component> provider, final String header) {
		columnInfos.add(new ColumnInfo<>(header, provider));
		return grid.addComponentColumn(provider).setHeader(header);
	}

	/**
	 * Définit les données affichées : alimente le Grid et reconstruit les cartes.
	 */
	public void setItems(final List<T> newItems) {
		this.items = newItems != null ? newItems : List.of();
		grid.setItems(this.items);
		rebuildCards();
	}

	/**
	 * Rafraîchit le Grid et reconstruit les cartes après une mutation des données.
	 */
	public void refresh() {
		grid.getDataProvider().refreshAll();
		rebuildCards();
	}

	/**
	 * Fournit un renderer custom pour les cartes. Sans renderer, une ligne
	 * « En-tête : valeur » est générée pour chaque colonne.
	 */
	public void setCardRenderer(final Function<T, Component> cardRenderer) {
		this.cardRenderer = cardRenderer;
		rebuildCards();
	}

	private void rebuildCards() {
		cardList.removeAll();
		for (final T item : items) {
			cardList.add(buildCard(item));
		}
	}

	private Component buildCard(final T item) {
		if (cardRenderer != null) {
			return cardRenderer.apply(item);
		}
		/* Rendu auto : une ligne « En-tête : valeur » par colonne */
		final Div card = new Div();
		card.addClassName("card");
		for (final ColumnInfo<T> col : columnInfos) {
			final Object value = col.provider().apply(item);
			if (value instanceof Component componentValue) {
				componentValue.getElement().getClassList().add("card-line");
				card.add(componentValue);
			} else if (value != null) {
				final HorizontalLayout line = new HorizontalLayout();
				line.setSpacing(false);
				line.addClassName("card-line");
				final Span headerSpan = new Span(col.header() + " : ");
				headerSpan.addClassName("font-bold");
				line.add(headerSpan, new Span(String.valueOf(value)));
				card.add(line);
			}
		}
		return card;
	}
}
```

- [ ] **Step 2: Ajouter le CSS responsive en fin de styles.css**

À la fin de `src/main/resources/META-INF/resources/css/styles.css`, après le bloc `.dossier-anim-out { ... }`, ajouter :

```css

/* ResponsiveCardList : Grid sur grand écran, liste de cartes sur petit écran */
.responsive-card-list {
	display: flex;
	flex-direction: column;
	min-height: 0;
}

.responsive-card-list > vaadin-grid {
	flex: 1 1 auto;
}

.responsive-card-list .card-list {
	display: none;
}

.responsive-card-list .card {
	border-bottom: 1px solid var(--lumo-contrast-20pct);
	padding: var(--lumo-space-s) var(--lumo-space-xs);
}

.responsive-card-list .card-line {
	align-items: baseline;
	margin-bottom: var(--lumo-space-xs);
}

.responsive-card-list .card-badge {
	background: var(--lumo-primary-color-10pct);
	color: var(--lumo-primary-text-color);
	border-radius: 999px;
	padding: 0 var(--lumo-space-s);
	font-size: var(--lumo-font-size-s);
}

.responsive-card-list .card-badge-elp {
	background: #0da50d73;
}

.responsive-card-list .card-badge-arbo {
	background: #3057ed8a;
}

@media (max-width: 767px) {
	.responsive-card-list > vaadin-grid {
		display: none;
	}

	.responsive-card-list .card-list {
		display: flex;
		flex-direction: column;
		flex: 1 1 auto;
		overflow-y: auto;
	}
}
```

- [ ] **Step 3: Compiler**

Run: `mvn compile -q`
Expected: `BUILD SUCCESS`, aucune erreur

- [ ] **Step 4: Commit**

```bash
git add src/main/java/fr/univlorraine/mondossierweb/uicomponents/ResponsiveCardList.java src/main/resources/META-INF/resources/css/styles.css
git commit -m "composant ResponsiveCardList : grid grand écran / cartes petit écran"
```

---

### Task 2: Intégration dans FavorisView

**Files:**
- Modify: `src/main/java/fr/univlorraine/mondossierweb/views/FavorisView.java`

**Interfaces:**
- Consumes: `ResponsiveCardList<Favoris>` (API du Task 1 : `addColumn`, `addComponentColumn`, `setCardRenderer`, `setItems`)
- Produces: vue Favoris consultable sur mobile ; méthode privée `createFavoriCard(Favoris fav)` retournant `Div` (renderer custom)

- [ ] **Step 1: Remplacer le champ Grid par ResponsiveCardList**

Dans `FavorisView.java`, remplacer la ligne 24 :

```java
import com.vaadin.flow.component.grid.Grid;
```

par :

```java
import com.vaadin.flow.component.html.Div;
import fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList;
```

(`Div` s'insère à sa place alphabétique dans les imports `com.vaadin.flow.component.html.*`, entre `com.vaadin.flow.component.grid.Grid` et `com.vaadin.flow.component.html.Span` ; l'import `ResponsiveCardList` rejoint le groupe `fr.univlorraine.mondossierweb.*`.)

Remplacer le champ (ligne 78) :

```java
	private Grid<Favoris> favorisGrid;
```

par :

```java
	private ResponsiveCardList<Favoris> favorisList;
```

- [ ] **Step 2: Adapter la construction dans init()**

Dans `init()`, remplacer le bloc lignes 117-138 :

```java
			if (lfav != null && !lfav.isEmpty()) {
				favorisGrid = new Grid<>();
				favorisGrid.setSizeFull();
				favorisGrid.addClassName("scrollabletable");

				favorisGrid.addColumn(fav -> rechercheArborescenteController.getTypeObj(fav.getId().getTypfav(), fav.getId().getIdfav()))
					.setHeader(getTranslation(NAME + ".table.id.typfav"))
					.setFlexGrow(1).setResizable(true);
				favorisGrid.addColumn(fav -> fav.getId().getIdfav())
					.setHeader(getTranslation(NAME + ".table.id.idfav"))
					.setFlexGrow(1).setResizable(true);
				favorisGrid.addColumn(fav -> favorisController.getLibObjFavori(fav.getId().getTypfav(), fav.getId().getIdfav()))
					.setHeader(getTranslation(NAME + ".table.id.libelle"))
					.setFlexGrow(3).setResizable(true);
				favorisGrid.addComponentColumn(this::createActionsColumn)
					.setHeader(getTranslation(NAME + ".table.id.actions"))
					.setFlexGrow(2).setResizable(true);

				favorisGrid.setItems(lfav);
				globalLayout.add(favorisGrid);
				globalLayout.setFlexGrow(1, favorisGrid);
			}
```

par :

```java
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
```

- [ ] **Step 3: Adapter le handler de suppression**

Dans `createActionsColumn`, remplacer le listener du bouton corbeille (lignes 172-184) :

```java
		btnfav.addClickListener(event -> {
			FavorisPK fpk = new FavorisPK();
			fpk.setIdfav(idObj);
			fpk.setLogin(userController.getCurrentUserName());
			fpk.setTypfav(typeObj);
			favorisController.removeFavori(fpk);
			lfav.remove(fav);
			favorisList.setItems(lfav);
			if (lfav.isEmpty()) {
				favorisList.setVisible(false);
				labelAucunFavoriLayout.setVisible(true);
			}
		});
```

- [ ] **Step 4: Ajouter le renderer de carte**

Ajouter à la fin de la classe `FavorisView` (avant l'accolade fermante finale, après `createActionsColumn`) :

```java
	/**
	 * Construit la carte d'un favori, affichée sur petit écran :
	 * badge du type + code, libellé, boutons d'action.
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

		/* Ligne 2 : libellé */
		final Span libelle = new Span(favorisController.getLibObjFavori(typeObj, idObj));
		libelle.addClassName("font-bold");

		/* Ligne 3 : actions (méthode partagée avec le Grid) */
		final HorizontalLayout actions = createActionsColumn(fav);

		card.add(ligneEntete, libelle, actions);
		return card;
	}
```

- [ ] **Step 5: Compiler**

Run: `mvn compile -q`
Expected: `BUILD SUCCESS`, aucune erreur (aucune référence résiduelle à `favorisGrid` — vérifier avec `rg "favorisGrid" src/main/java` qui ne doit plus rien retourner)

- [ ] **Step 6: Vérification manuelle**

Lancer l'application et vérifier dans le navigateur (mode responsive des DevTools) :

1. Largeur ≥ 768px : le Grid s'affiche comme avant (4 colonnes, actions)
2. Largeur < 768px : le Grid disparaît, les cartes s'affichent (badge type + code / libellé gras / actions), scroll vertical fonctionnel
3. Redimensionnement en traversant 767px dans les deux sens : bascule instantanée, sans rechargement
4. Suppression d'un favori depuis une carte : la carte disparaît, liste à jour ; dernier favori supprimé → message « Vous n'avez défini aucun favori »
5. Boutons carte : accès arborescence (sitemap, favoris CMP/VET) et liste des inscrits (users, favoris ELP/VET) fonctionnels

- [ ] **Step 7: Commit**

```bash
git add src/main/java/fr/univlorraine/mondossierweb/views/FavorisView.java
git commit -m "favorisView : bascule grid/cartes sur mobile via ResponsiveCardList"
```
