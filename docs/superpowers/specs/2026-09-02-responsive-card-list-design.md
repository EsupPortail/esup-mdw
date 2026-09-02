# Design — Composant responsive `ResponsiveCardList<T>`

**Date :** 2026-09-02
**Périmètre :** mécanisme réutilisable, pilote sur `FavorisView`
**Stack :** Vaadin 25.1.9 (Flow), Spring Boot 4, Java 21

## Problème

L'application doit être consultable sur PC et sur mobile. Plusieurs vues affichent des `Grid` dont les colonnes sont trop compressées sur petit écran (exemple : `FavorisView.favorisGrid`). Il faut un mécanisme qui remplace le Grid par une liste de cartes sur 2-3 lignes quand la fenêtre est trop étroite.

## Décisions

- **Approche retenue : double rendu + bascule 100 % CSS.** Le composant contient le Grid ET une liste de cartes ; une media query masque l'un des deux selon la largeur du viewport. Aucun JS, aucun aller-retour serveur au resize, bascule instantanée.
- **Comportement de la liste mobile : affichage + actions uniquement** (pas de tri ni de réorganisation de colonnes).
- **Périmètre : mécanisme réutilisable** pour les futures vues (Notes, Inscriptions, Calendrier, fenêtres de détail...).

### Approches écartées

1. *Grid transformé en cartes* (une colonne pleine largeur, autres colonnes masquées en CSS) : écartée car CSS fragile sur les internes shadow-DOM de `vaadin-grid`, cassable à chaque montée de version Vaadin.
2. *Bascule serveur* (`BrowserWindowResizeListener`) : écartée pour l'instant car aller-retours serveur au resize, débouncing et flicker. Reste une option pour les gros volumes (ListeInscrits).

## Architecture

Nouvelle classe `fr.univlorraine.mondossierweb.uicomponents.ResponsiveCardList<T> extends Div` :

```java
public class ResponsiveCardList<T> extends Div {
    private final Grid<T> grid = new Grid<>();
    private final Div cardList = new Div();   // cartes empilées verticalement

    // Colonnes : délègue au Grid (chaînable : .setFlexGrow, .setResizable...)
    public Grid.Column<T> addColumn(ValueProvider<T, ?> provider, String header);
    public Grid.Column<T> addComponentColumn(ValueProvider<T, ? extends Component> provider, String header);

    // Données : alimente le Grid ET reconstruit les cartes (grid et cartes toujours synchronisés)
    public void setItems(List<T> items);
    public void refresh();

    // Rendu carte : optionnel. Sans lui → rendu auto depuis les colonnes
    public void setCardRenderer(Function<T, Component> renderer);
}
```

- Classe CSS `responsive-card-list` posée sur le conteneur.
- **Rendu auto par défaut** : pour chaque colonne, une ligne `En-tête : valeur` (les colonnes de composants rendent leur composant) → branchement d'une nouvelle vue sans effort.
- **Renderer custom** : pour un rendu « carte » soigné, chaque vue fournit sa propre fonction. Pilote : FavorisView.

## Bascule CSS

Ajout dans `src/main/resources/META-INF/resources/css/styles.css` (chargé globalement via `@StyleSheet("css/styles.css")` sur `Application.java`) :

```css
.responsive-card-list .card-list { display: none; }

@media (max-width: 767px) {
  .responsive-card-list vaadin-grid { display: none; }
  .responsive-card-list .card-list {
    display: flex;
    flex-direction: column;
    overflow-y: auto;
  }
}
```

- Seuil **767px** (mobile + tablette portrait), ajustable.
- La liste de cartes possède son propre scroll vertical.

## Intégration dans FavorisView

- Le champ `Grid<Favoris> favorisGrid` devient `ResponsiveCardList<Favoris> favorisList`.
- Les 4 colonnes (type, code, libellé, actions) sont déclarées comme aujourd'hui.
- **Renderer custom** pour une carte lisible sur 2-3 lignes :

```
┌──────────────────────────────────────┐
│ [VET]  ABC123          ← badge + code│
│ L1 Informatique        ← libellé gras│
│ [🗑] [sitemap] [users]  ← actions    │
└──────────────────────────────────────┘
```

- Badge type coloré (pastel bleu/vert comme l'existant), libellé en gras, actions réutilisant la méthode existante `createActionsColumn(fav)`.
- Les handlers de suppression appellent `favorisList.setItems(lfav)` / `refresh()` au lieu de muter le Grid directement.
- État « aucun favori » : inchangé (masquage du composant, affichage du label).

## Cas limites

- **Double payload** (grid + cartes dans le DOM) : acceptable pour les petites listes. ListeInscrits (gros volumes) reste sur un Grid classique pour l'instant ; alternatives ultérieures : VirtualList ou bascule serveur.
- Les composants des cartes existent en plus de ceux du Grid → toute mutation de données doit passer par `setItems`/`refresh` (encapsulé dans le composant).

## Vérification

- `mvn compile` (pas de framework de tests dans le projet).
- Tests manuels : mode responsive des DevTools (mobile/tablette/desktop), rotation d'écran, suppression d'un favori sur carte, état vide, accès arborescence / liste des inscrits depuis les cartes, seuil 767px.
