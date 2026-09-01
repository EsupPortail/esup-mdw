# Colonne Actions dédiée dans InscriptionsView

Date : 2026-09-01

## Contexte

Dans `InscriptionsView`, la colonne « Filière d'inscription » (`lib_etp`) contient le libellé de
l'étape ainsi que 4 boutons d'actions (détail, certificat de scolarité, attestation d'affiliation,
quittance). On souhaite sortir ces 4 boutons dans une colonne dédiée « Actions ».

## Objectif

- La colonne libellé ne contient plus que `inscription.getLib_etp()`.
- Une nouvelle colonne « Actions » regroupe les 4 boutons.
- Le comportement des flags de configuration est strictement conservé.

## Changements

### 1. Colonne libellé (InscriptionsView.java)

`buildLibelleInscriptionColumn(incription)` est simplifié : elle retourne uniquement le `Span` du
libellé. Suppression de `secondLineLayout`, `twoLinesLayout` et de toute la logique de boutons.

### 2. Nouvelle colonne « Actions »

Nouvelle méthode `buildActionsColumn(Inscription)` ajoutée à la grille via `addComponentColumn`,
insérée juste après la colonne libellé, avec :

- En-tête : traduction `inscriptionsView.table.actions`.
- `setFlexGrow(0)` pour garder la colonne étroite.
- Contenu : `HorizontalLayout` regroupant :
  - **Détail** (`SEARCH`) : icône, si année en cours (inchangé).
  - **Certificat** : selon `isAffBtnCertifNouvelleLigne()` — icône `LUMO_TERTIARY_INLINE` sinon
    bouton `LUMO_SMALL` libellé ; classe `red-button-icon` si `isAffBtnCertificatCouleur()`.
  - **Attestation affiliation** : selon `isAffBtnAttestSsoNouvelleLigne()` ; classe
    `blue-button-icon`.
  - **Quittance** : selon `isAffBtnQuittanceDroitsPayesNouvelleLigne()` ; classe `green-button-icon`
    si `isAffBtnQuittanceCouleur()`.
- Les téléchargements restent encapsulés via `createDownloadAnchor`.
- Cellule vide (layout vide) si aucun bouton applicable.

### 3. Traductions

Ajout dans `messages-default.properties` et `messages.sample.properties` :

```
inscriptionsView.table.actions = Actions
```

## Non changé

- Logique métier des conditions (`proposerCertificat`, `proposerAttestationAffiliationSSO`,
  `proposerQuittanceDroitsPayes`), des contrôleurs, et des fenêtres.
- Vue DAC (« Autres cursus »), qui ne possède pas de boutons.