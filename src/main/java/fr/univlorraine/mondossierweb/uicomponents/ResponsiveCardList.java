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
