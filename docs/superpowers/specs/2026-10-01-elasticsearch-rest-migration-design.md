# Migration de l'intégration Elasticsearch : TransportClient → REST (compatibilité ES 2.x et 8/9)

- **Date** : 2026-10-01
- **Projet** : `esup-mdw-vaadin25`
- **Statut** : spec validé (en attente de relecture avant plan d'implémentation)
- **Auteur** : conception assistée

## 1. Contexte

L'application `esup-mdw-vaadin25` (Spring Boot 4 / Java 21 / Vaadin 25) propose une
« recherche rapide » (`RechercheRapideView`) adossée à Elasticsearch. L'intégration
provient de la dépendance externe `fr.univlorraine:mdw-tools:2.0.5`, qui embarque
`org.elasticsearch:elasticsearch:2.3.5` et se connecte via le **`TransportClient`**
(protocole binaire, port 9300, notion de `cluster.name`).

Le `TransportClient` impose une correspondance stricte de version majeure avec le
serveur et a été **supprimé en Elasticsearch 8.0**. Il est donc impossible de faire
pointer l'application vers un Elasticsearch récent sans changer de client.

## 2. Objectif

Remplacer l'intégration transport par une intégration **REST (HTTP/JSON)** capable de
fonctionner indifféremment contre :

- un **Elasticsearch 2.x** (index existant, **non modifié**) ;
- un **Elasticsearch 8.x ou 9.x** (nouvel index, sans sécurité/auth/TLS).

Et **supprimer la dépendance `mdw-tools`** : le code est intégré directement dans le
projet `esup-mdw-vaadin25`.

## 3. Contraintes et décisions

| # | Décision | Justification |
|---|---|---|
| D1 | L'index ES 2.x existant **reste tel quel** (types conservés) | pas de réindexation possible |
| D2 | Le **branchement par version** est retenu | `_type` existe en 2.x, disparaît en 8+ |
| D3 | Cible ES 8/9 **sans sécurité** (HTTP simple) | simplification de la config client |
| D4 | Client = **JDK `java.net.http.HttpClient` + Jackson** | zéro dépendance HTTP externe, Java 21 disponible |
| D5 | Le code est **intégré dans `esup-mdw-vaadin25`**, dépendance `mdw-tools` supprimée | plus de repo/publication séparés |
| D6 | Le **paquet Java est conservé** (`fr.univlorraine.mondossierweb.tools.elasticsearch`) | les imports de `SpringConfig` / `RechercheRapideView` ne changent pas |
| D7 | Tests = **WireMock seul**, sans Docker | choix utilisateur, CI légère |
| D8 | Le type d'objet côté 8/9 est porté par un **champ document** `type_objet` | `_type` n'existe plus |

## 4. Architecture

### 4.1 Vue d'ensemble

```
RechercheRapideView (@Resource ElasticSearchApogeeService)
        │
        ▼
ElasticSearchApogeeService (interface, inchangée)
        │
        ▼
ElasticSearchApogeeServiceImpl  ── utilise ──►  EsRestClient (HttpClient + Jackson)
        │                                              │  GET /            → détection version
        │  choisit une stratégie                      │  POST …/_search   → exécution
        ▼                                              ▼
EsQueryStrategy ──► Es2QueryStrategy / Es8QueryStrategy
        │
        ▼
SearchHit { Map<String,Object> source; String type; }
```

### 4.2 Composants (paquet `fr.univlorraine.mondossierweb.tools.elasticsearch`)

| Composant | Rôle |
|---|---|
| `ElasticSearchApogeeService` | Interface publique, **reprise telle quelle** de mdw-tools (`findObj` ×2, `initConnexion` ×2). |
| `ElasticSearchApogeeServiceImpl` | Orchestration : nettoie la valeur, exécute la recherche principale puis le repli « par code », filtre, injecte `esTyp`. **Signature et logique métier inchangées.** |
| `EsRestClient` | Wrapper `java.net.http.HttpClient` : base URL, timeouts, `getRoot()` (`GET /`), `search(path, body)`. |
| `EsVersion` | Énumération `V2` / `V8`, déduite du numéro majeur. |
| `EsQueryStrategy` | Contrat : `searchPath(index, types)`, `buildBody(...)`, `parse(json)`. |
| `Es2QueryStrategy` | Chemin typé + lecture de `_type`. |
| `Es8QueryStrategy` | Chemin typeless + filtre `terms` sur `type_objet` + lecture du champ. |
| `SearchHit` | Modèle normalisé : `source` + `type`. |

`ElasticSearchClientInitializer` (mdw-tools) **n'est pas repris** ; son rôle est absorbé
par `EsRestClient`.

### 4.3 Dépendances

- **Retrait** : `fr.univlorraine:mdw-tools:2.0.5` (retire aussi les transitifs
  `spring-context-support:4.1.0` et `elasticsearch:2.3.5`).
- **Aucune** dépendance runtime ajoutée : `HttpClient` (JDK) + `jackson-databind`
  (déjà géré par le BOM Spring Boot).
- **Ajout test uniquement** : WireMock.

## 5. Détection de version et connexion

`initConnexion(url, port, cluster)` :

1. crée un `EsRestClient` sur `http://{url}:{port}` ;
2. appelle `GET /` et lit `version.number` ;
3. déduit le majeur :
   - `2` → `EsVersion.V2` ;
   - `>= 8` → `EsVersion.V8` ;
   - autre → **échec** (`false` + log d'erreur) ;
4. mémorise (cache) le client et la stratégie ;
5. retourne `true`.

- Le paramètre `cluster` n'a plus de sens en REST : **conservé dans la signature**
  (rétro-compatibilité) mais **ignoré**, avec un log d'avertissement.
- Timeouts : connexion 2 s, requête 5 s (constantes).
- Une seconde initialisation ne recrée pas le client (idempotence comme aujourd'hui).

## 6. Requêtes (cœur de la double compatibilité)

La logique de `findObj` **ne change pas** : nettoyage de la valeur (tirets,
parenthèses, espaces multiples, crochets pour la recherche par code, minuscules),
`matchQuery`, `wildcard` en recherche rapide, repli sur le champ code, filtrage final
et injection de `esTyp`. Seul le **transport** diffère.

Le DSL `match` / `wildcard` / `bool.must` est **compatible** entre 2.x et 8/9.

### 6.1 ES 2.x (`Es2QueryStrategy`)

- Chemin : `POST /{index}/{type1,type2,…}/_search` (chemin typé → renvoie `_type`).
  Exemple : `POST /apogee/cmp,vet,elp,etu/_search`.
- Corps :

```json
{
  "from": 0,
  "size": 60,
  "explain": true,
  "query": { /* match | wildcard | bool.must, identique à l'existant */ }
}
```

- Pas de `search_type` (l'ancien `QUERY_AND_FETCH` est supprimé en 6.0 ; le défaut
  `query_then_fetch` convient).
- Lecture : `hits.hits[]._source` + `hits.hits[]._type`.

### 6.2 ES 8/9 (`Es8QueryStrategy`)

- Chemin : `POST /{index}/_search` (typeless).
- Corps :

```json
{
  "from": 0,
  "size": 60,
  "explain": true,
  "query": {
    "bool": {
      "must": [ { /* match | wildcard | bool.must */ } ],
      "filter": [
        { "terms": { "type_objet": ["cmp", "vet", "elp", "etu"] } }
      ]
    }
  }
}
```

- `type_objet` provient de `param.elasticsearch.index.champtypeobjet`.
- Lecture : `hits.hits[]._source` ; le type est lu dans
  `_source[champtypeobjet]`. Si absent → `type = null` + log d'avertissement.

## 7. Modèle et parsing

- Jackson `ObjectMapper.readTree(response)` puis navigation `hits.hits`.
- **Normalisation numérique** : `ResultatDeRecherche` caste `(Integer) o.get(champVersion)`.
  Jackson pouvant produire `Integer` ou `Long`, le parsing **convertit les valeurs
  entières du `_source` en `Integer`** lorsqu'elles tiennent sur 32 bits, afin de
  préserver le comportement du `TransportClient` et d'éviter toute `ClassCastException`.
- `esTyp` (nom du champ configuré, `Utils.ES_TYPE`) est réinjecté dans le `Map` résultat
  exactement comme aujourd'hui.

## 8. Configuration

| Propriété | Changement |
|---|---|
| `param.elasticsearch.url` | inchangé (hôte, sans schéma) |
| `param.elasticsearch.port` | **change de sens : devient le port HTTP** (ex. 9200/9202) |
| `param.elasticsearch.cluster` | **obsolète** : conservé mais ignoré |
| `param.elasticsearch.index` | inchangé |
| `param.elasticsearch.index.champrecherche` | inchangé |
| `param.elasticsearch.index.champcodeobjet` | inchangé |
| `param.elasticsearch.index.champversionobjet` | inchangé |
| `param.elasticsearch.index.champlibelleobjet` | inchangé |
| `param.elasticsearch.index.champtypeobjet` | **nouveau** (ex. `type_objet`), utilisé en 8/9 |

Fichiers à adapter : `application.yml`, `application.example.yml`, `PropertyUtils`
(nouveau getter `getElasticSearchChampTypeObjet()`), `SpringConfig` (ajout de
`esas.setElasticSearchChampTypeObjet(...)`).

`RechercheRapideView` : **aucune modification**.

## 9. Gestion d'erreurs

- Version non supportée ou ES injoignable → `initConnexion` retourne `false` + log
  (comportement actuel : la vue teste ce retour et n'affiche pas le champ).
- Erreur HTTP ou JSON pendant `findObj` → log d'erreur et retour d'une **liste vide**
  (pas de rupture de l'UI).
- Timeouts explicites.

## 10. Tests (WireMock, sans Docker)

Nouveaux tests dans `esup-mdw-vaadin25/src/test/java/...`, JUnit 5 + WireMock.

1. Détection 2.x (`GET /` → `2.3.5`) → chemin **typé** utilisé ; corps vérifié.
2. Détection 8.x (`GET /` → `8.13.0`) → chemin **typeless** + filtre `terms` vérifié.
3. Parsing réponse 2.x (`_type`) → `esTyp` correct.
4. Parsing réponse 8.x (champ `type_objet`) → `esTyp` correct.
5. Repli « recherche par code » dans les deux modes.
6. Recherche rapide : `wildcard` (`mot*`) et gestion de l'espace final
   (`lastWordCompletion`).
7. Version non supportée (ex. 6.8) → `initConnexion` = `false`.
8. ES injoignable → `initConnexion` = `false`.
9. Normalisation numérique : le champ version ressort en `Integer`.

## 11. Impacts et mise en œuvre

- Un seul dépôt, un seul ensemble de commits.
- `esup-mdw` (legacy Vaadin 7/8) **non touché** : il continue d'utiliser `mdw-tools:2.0.5`.
- Aucun bump/publication de `mdw-tools`.
- Prérequis **données** (hors code) : l'index 8/9 doit porter le champ `type_objet`.
- Déploiement : publier l'app, ajuster `param.elasticsearch.port` vers le port HTTP,
  renseigner `champtypeobjet`.

## 12. Hors périmètre

- Authentification / TLS (cible actuellement sans sécurité).
- Réindexation et mapping du nouvel index 8/9.
- Suppression définitive de `param.elasticsearch.cluster`.
- Pagination au-delà de 60 résultats.
- Migration de l'application legacy `esup-mdw`.

## 13. Critères de succès

1. La recherche rapide fonctionne contre **ES 2.x** (port HTTP) avec `esTyp` correct.
2. La recherche rapide fonctionne contre **ES 8/9** (champ `type_objet`) avec `esTyp` correct.
3. Plus aucune dépendance `mdw-tools` ni `org.elasticsearch` au classpath de l'application.
4. Les tests WireMock passent.
5. `RechercheRapideView` inchangé.

## 14. Risques

| Risque | Mitigation |
|---|---|
| Le `port` change de sens (transport 9302 → HTTP) : erreur de config au déploiement | documenter explicitement dans `application.example.yml` |
| L'index 8/9 n'a pas le champ `type_objet` | prérequis explicité ; log d'avertissement si absent |
| Divergences de DSL entre versions | le DSL utilisé (match/wildcard/bool) est stable ; couvert par les tests WireMock |
| ES 2.x n'expose pas HTTP / accès réseau fermé | à valider en environnement ; hors périmètre code |
