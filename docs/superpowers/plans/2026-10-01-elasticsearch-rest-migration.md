# Migration Elasticsearch TransportClient → REST (ES 2.x + 8/9) — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remplacer l'intégration Elasticsearch `TransportClient` (fournie par `mdw-tools`) par une intégration REST interne capable de fonctionner contre ES 2.x et ES 8/9, sans dépendance `mdw-tools`.

**Architecture:** Le code ES est rapatrié dans `esup-mdw-vaadin25`, dans le paquet `fr.univlorraine.mondossierweb.tools.elasticsearch`. Un client HTTP (`java.net.http.HttpClient` + Jackson) détecte la version du serveur via `GET /`, puis délègue à une stratégie `Es2QueryStrategy` (chemin typé, `_type`) ou `Es8QueryStrategy` (chemin typeless, filtre `terms` sur le champ `type_objet`). La logique métier de `findObj` est conservée à l'identique.

**Tech Stack:** Java 21, Spring Boot 4, Vaadin 25, JDK `HttpClient`, Jackson (BOM Boot), JUnit 5, WireMock standalone.

**Spec:** `docs/superpowers/specs/2026-10-01-elasticsearch-rest-migration-design.md`

## Global Constraints

- Java 21, Spring Boot 4, Vaadin 25 (ne pas changer).
- Paquet Java : `fr.univlorraine.mondossierweb.tools.elasticsearch` (D6 — imports de `SpringConfig` / `RechercheRapideView` inchangés).
- **Retirer** la dépendance `fr.univlorraine:mdw-tools:2.0.5` (pom.xml, bloc « ElasticSearch (recherche rapide) »).
- **Aucune** dépendance runtime ajoutée : `HttpClient` (JDK) + `jackson-databind` (déjà géré par le BOM Boot). Seul ajout : `org.wiremock:wiremock-standalone` en scope `test`.
- Tests : JUnit 5 + WireMock, **sans Docker**.
- `findObj` : comportement métier **identique** (nettoyage de valeur, `match`/`wildcard`, repli par code, filtrage, injection de `esTyp`). `RechercheRapideView` **non modifié**.
- Types ES en majuscules : `CMP`, `VET`, `ELP`, `ETU` (`Utils.CMP` etc.). Champ type côté 8/9 : `type_objet`.
- `esTyp` = clé `Utils.ES_TYPE` (`"TYP_OBJ"`).
- Timeouts : connexion 2 s, requête 5 s.

## Review Focus

Ces cas, implicites dans le spec mais non couverts par la logique nominale, doivent être pinnés par un test dans la tâche propriétaire :

1. `_source` absent ou non-objet → `EsResponseParser.source` renvoie un map vide (pas de NPE). *(Task 3)*
2. Recherche par code avec champ code/version absent du `_source` → pas de NPE, résultat ignoré. *(Task 6)*
3. Réponse ES non-2xx (index absent, requête invalide) → `findObj` renvoie une liste vide, aucune exception remontée. *(Task 6)*
4. Valeur terminée par un espace / contenant des crochets → `lastWordCompletion` et recherche par code préservés ; `[]` ne doit pas lever. *(Task 6)*
5. Version majeure inattendue (5/6/7) → `initConnexion` renvoie `false`, pas d'exception au démarrage. *(Task 6)*

---

## File Structure

**Créés (main)** — tous sous `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/` :
- `ElasticSearchApogeeService.java` — interface (reprise de mdw-tools).
- `SearchHit.java` — modèle normalisé `source` + `type`.
- `EsVersion.java` — `V2` / `V8` + parsing.
- `EsQueryBuilder.java` — fabrique de nœuds JSON (`match`, `wildcard`, `boolMust`, `terms`).
- `EsResponseParser.java` — navigation `hits.hits` + `_source` + normalisation numérique.
- `EsQueryStrategy.java` — interface de stratégie.
- `Es2QueryStrategy.java` — chemin typé + `_type`.
- `Es8QueryStrategy.java` — typeless + filtre `terms` + champ type.
- `EsRestClient.java` — wrapper `HttpClient`.
- `ElasticSearchApogeeServiceImpl.java` — orchestration (réécrit).

**Modifiés :**
- `pom.xml` — retrait mdw-tools, ajout `jackson-databind` + `wiremock-standalone`.
- `src/main/java/.../utils/PropertyUtils.java` — getter `getElasticSearchChampTypeObjet()`.
- `src/main/java/.../config/SpringConfig.java` — appel `setElasticSearchChampTypeObjet(...)`.
- `application.yml`, `application.example.yml` — propriété `param.elasticsearch.index.champtypeobjet`.

**Créés (test)** — sous `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/` :
- `EsVersionTest.java`, `EsQueryBuilderTest.java`, `EsResponseParserTest.java`
- `Es2QueryStrategyTest.java`, `Es8QueryStrategyTest.java`
- `EsRestClientTest.java`, `ElasticSearchApogeeServiceImplTest.java`

---

## Task 1 : Modèle `SearchHit` et énumération `EsVersion`

**Files:**
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/SearchHit.java`
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsVersion.java`
- Test: `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsVersionTest.java`

**Interfaces:**
- Produces: `record SearchHit(Map<String,Object> source, String type)` ; `enum EsVersion { V2, V8 }` ; `EsVersion.fromNumber(String) : EsVersion`.

- [ ] **Step 1 : Écrire le test qui échoue**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EsVersionTest {

	@Test
	void version2DonneV2() {
		assertEquals(EsVersion.V2, EsVersion.fromNumber("2.3.5"));
	}

	@Test
	void version8DonneV8() {
		assertEquals(EsVersion.V8, EsVersion.fromNumber("8.13.0"));
	}

	@Test
	void version9DonneV8() {
		assertEquals(EsVersion.V8, EsVersion.fromNumber("9.0.0"));
	}

	@Test
	void versionNonSupporteeLeve() {
		assertThrows(IllegalArgumentException.class, () -> EsVersion.fromNumber("6.8.0"));
	}
}
```

- [ ] **Step 2 : Lancer le test pour vérifier l'échec**

Run: `mvn -q -Dtest=EsVersionTest test`
Expected: FAIL — `EsVersion` introuvable (compilation).

- [ ] **Step 3 : Implémenter le minimum**

`SearchHit.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.util.Map;

/** Résultat de recherche normalisé, indépendant de la version d'Elasticsearch. */
public record SearchHit(Map<String, Object> source, String type) {
}
```

`EsVersion.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

/** Versions d'Elasticsearch gérées par l'intégration REST. */
public enum EsVersion {

	/** Elasticsearch 2.x : types ES disponibles (`_type`). */
	V2,
	/** Elasticsearch 8.x / 9.x : pas de types, champ de type dans le document. */
	V8;

	/**
	 * Déduit la version à partir du numéro renvoyé par `GET /` (`version.number`).
	 * @throws IllegalArgumentException si la version n'est pas gérée (ni 2.x, ni >= 8).
	 */
	public static EsVersion fromNumber(final String number) {
		if (number == null || !number.matches("\\d+.*")) {
			throw new IllegalArgumentException("Numéro de version Elasticsearch invalide : " + number);
		}
		final int major = Integer.parseInt(number.split("\\.")[0]);
		if (major == 2) {
			return V2;
		}
		if (major >= 8) {
			return V8;
		}
		throw new IllegalArgumentException("Version Elasticsearch non supportée : " + number);
	}
}
```

- [ ] **Step 4 : Lancer le test pour vérifier le succès**

Run: `mvn -q -Dtest=EsVersionTest test`
Expected: PASS.

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/SearchHit.java src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsVersion.java src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsVersionTest.java
git commit -m "feat(es): modèle SearchHit et détection de version"
```

---

## Task 2 : Fabrique de requêtes `EsQueryBuilder`

**Files:**
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsQueryBuilder.java`
- Test: `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsQueryBuilderTest.java`

**Interfaces:**
- Produces: `EsQueryBuilder.match(String, String)`, `.wildcard(String, String)`, `.boolMust(List<ObjectNode>)`, `.terms(String, List<String>)` → `com.fasterxml.jackson.databind.node.ObjectNode`.

- [ ] **Step 1 : Écrire le test qui échoue**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class EsQueryBuilderTest {

	@Test
	void matchProduitLeJsonAttendu() {
		assertEquals("{\"match\":{\"lib_desc_obj\":\"abc\"}}", EsQueryBuilder.match("lib_desc_obj", "abc").toString());
	}

	@Test
	void wildcardProduitLeJsonAttendu() {
		assertEquals("{\"wildcard\":{\"lib_desc_obj\":\"abc*\"}}", EsQueryBuilder.wildcard("lib_desc_obj", "abc*").toString());
	}

	@Test
	void boolMustRegroupeLesClauses() {
		assertEquals(
			"{\"bool\":{\"must\":[{\"match\":{\"f\":\"a\"}},{\"wildcard\":{\"f\":\"b*\"}}]}}",
			EsQueryBuilder.boolMust(List.of(EsQueryBuilder.match("f", "a"), EsQueryBuilder.wildcard("f", "b*"))).toString());
	}

	@Test
	void termsProduitLeJsonAttendu() {
		assertEquals("{\"terms\":{\"type_objet\":[\"CMP\",\"VET\"]}}", EsQueryBuilder.terms("type_objet", List.of("CMP", "VET")).toString());
	}
}
```

- [ ] **Step 2 : Lancer le test pour vérifier l'échec**

Run: `mvn -q -Dtest=EsQueryBuilderTest test`
Expected: FAIL — `EsQueryBuilder` introuvable.

- [ ] **Step 3 : Implémenter le minimum**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Fabrique les nœuds JSON des requêtes Elasticsearch (DSL stable 2.x → 9.x). */
public final class EsQueryBuilder {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private EsQueryBuilder() {
	}

	public static ObjectNode match(final String field, final String value) {
		return single("match", field, value);
	}

	public static ObjectNode wildcard(final String field, final String value) {
		return single("wildcard", field, value);
	}

	public static ObjectNode boolMust(final List<ObjectNode> clauses) {
		final ObjectNode bool = MAPPER.createObjectNode();
		final ArrayNode must = bool.putArray("must");
		clauses.forEach(must::add);
		final ObjectNode query = MAPPER.createObjectNode();
		query.set("bool", bool);
		return query;
	}

	public static ObjectNode terms(final String field, final List<String> values) {
		final ObjectNode terms = MAPPER.createObjectNode();
		final ArrayNode array = terms.putArray(field);
		values.forEach(array::add);
		final ObjectNode query = MAPPER.createObjectNode();
		query.set("terms", terms);
		return query;
	}

	private static ObjectNode single(final String kind, final String field, final String value) {
		final ObjectNode fieldValue = MAPPER.createObjectNode();
		fieldValue.put(field, value);
		final ObjectNode query = MAPPER.createObjectNode();
		query.set(kind, fieldValue);
		return query;
	}
}
```

- [ ] **Step 4 : Lancer le test pour vérifier le succès**

Run: `mvn -q -Dtest=EsQueryBuilderTest test`
Expected: PASS.

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsQueryBuilder.java src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsQueryBuilderTest.java
git commit -m "feat(es): fabrique de requêtes JSON"
```

---

## Task 3 : Parsing des réponses `EsResponseParser`

**Files:**
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsResponseParser.java`
- Test: `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsResponseParserTest.java`

**Interfaces:**
- Consumes: — (autonome).
- Produces: `EsResponseParser.hits(String json) : List<JsonNode>` (nœuds `hits.hits`) ; `EsResponseParser.source(JsonNode hit) : Map<String,Object>` (avec entiers normalisés en `Integer`).

- [ ] **Step 1 : Écrire le test qui échoue**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

class EsResponseParserTest {

	@Test
	void hitsNavigueJusquAuxResultats() throws Exception {
		final String json = "{\"hits\":{\"hits\":[{\"_type\":\"CMP\",\"_source\":{\"cod_obj\":\"A\"}}]}}";
		final List<JsonNode> hits = EsResponseParser.hits(json);
		assertEquals(1, hits.size());
	}

	@Test
	void sourceExtraitEtNormaliseEnInteger() throws Exception {
		final String json = "{\"hits\":{\"hits\":[{\"_source\":{\"cod_obj\":\"A\",\"cod_vrs_obj\":0}}]}}";
		final JsonNode hit = EsResponseParser.hits(json).get(0);
		final Map<String, Object> source = EsResponseParser.source(hit);
		assertEquals("A", source.get("cod_obj"));
		assertEquals(Integer.valueOf(0), source.get("cod_vrs_obj"));
	}

	@Test
	void sourceAbsenteRenvoieMapVide() throws Exception {
		final JsonNode hit = EsResponseParser.hits("{\"hits\":{\"hits\":[{}]}}").get(0);
		assertTrue(EsResponseParser.source(hit).isEmpty());
	}

	@Test
	void reponseSansHitsRenvoieListeVide() throws Exception {
		assertTrue(EsResponseParser.hits("{}").isEmpty());
	}
}
```

- [ ] **Step 2 : Lancer le test pour vérifier l'échec**

Run: `mvn -q -Dtest=EsResponseParserTest test`
Expected: FAIL — `EsResponseParser` introuvable.

- [ ] **Step 3 : Implémenter le minimum**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Navigue dans une réponse Elasticsearch et normalise le `_source`. */
public final class EsResponseParser {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private EsResponseParser() {
	}

	/** Retourne les nœuds `hits.hits` de la réponse. */
	public static List<JsonNode> hits(final String json) throws IOException {
		final List<JsonNode> result = new ArrayList<>();
		final JsonNode hits = MAPPER.readTree(json).path("hits").path("hits");
		if (hits.isArray()) {
			hits.forEach(result::add);
		}
		return result;
	}

	/**
	 * Extrait le `_source` d'un hit sous forme de map.
	 * Les entiers sont normalisés en {@link Integer} lorsqu'ils tiennent sur 32 bits,
	 * afin de préserver le contrat attendu par ResultatDeRecherche.
	 */
	public static Map<String, Object> source(final JsonNode hit) {
		final JsonNode source = hit.path("_source");
		if (!source.isObject()) {
			return new LinkedHashMap<>();
		}
		final Map<String, Object> map = MAPPER.convertValue(source, new TypeReference<Map<String, Object>>() {
		});
		normalizeIntegers(map);
		return map;
	}

	private static void normalizeIntegers(final Map<String, Object> map) {
		for (final Map.Entry<String, Object> entry : map.entrySet()) {
			final Object value = entry.getValue();
			if (value instanceof Long l && l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
				entry.setValue(l.intValue());
			} else if (value instanceof BigInteger bi && bi.bitLength() < 32) {
				entry.setValue(bi.intValue());
			}
		}
	}
}
```

- [ ] **Step 4 : Lancer le test pour vérifier le succès**

Run: `mvn -q -Dtest=EsResponseParserTest test`
Expected: PASS.

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsResponseParser.java src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsResponseParserTest.java
git commit -m "feat(es): parsing des réponses et normalisation numérique"
```

---

## Task 4 : Stratégies de requête `Es2QueryStrategy` / `Es8QueryStrategy`

**Files:**
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsQueryStrategy.java`
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es2QueryStrategy.java`
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es8QueryStrategy.java`
- Test: `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es2QueryStrategyTest.java`
- Test: `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es8QueryStrategyTest.java`

**Interfaces:**
- Consumes: `SearchHit`, `EsQueryBuilder`, `EsResponseParser` (Tasks 1-3).
- Produces: interface `EsQueryStrategy { String searchPath(String index); String buildBody(ObjectNode queryNode); List<SearchHit> parse(String json) throws IOException; }` ; `new Es2QueryStrategy(List<String> types, ObjectMapper mapper)` ; `new Es8QueryStrategy(String typeField, List<String> types, ObjectMapper mapper)`.

- [ ] **Step 1 : Écrire les tests qui échouent**

`Es2QueryStrategyTest.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class Es2QueryStrategyTest {

	private final ObjectMapper mapper = new ObjectMapper();
	private final Es2QueryStrategy strategy = new Es2QueryStrategy(List.of("CMP", "VET"), mapper);

	@Test
	void cheminType() {
		assertEquals("/apogee/CMP,VET/_search", strategy.searchPath("apogee"));
	}

	@Test
	void corpsContientQuerySansFiltre() throws Exception {
		final String body = strategy.buildBody(EsQueryBuilder.match("lib_desc_obj", "abc"));
		final JsonNode node = mapper.readTree(body);
		assertEquals(0, node.get("from").asInt());
		assertEquals(60, node.get("size").asInt());
		assertTrue(node.get("explain").asBoolean());
		assertEquals("abc", node.get("query").get("match").get("lib_desc_obj").asText());
	}

	@Test
	void parseLitLeTypeUnderscore() throws Exception {
		final String json = "{\"hits\":{\"hits\":[{\"_type\":\"CMP\",\"_source\":{\"cod_obj\":\"A\"}}]}}";
		final List<SearchHit> hits = strategy.parse(json);
		assertEquals(1, hits.size());
		assertEquals("CMP", hits.get(0).type());
		assertEquals("A", hits.get(0).source().get("cod_obj"));
	}
}
```

`Es8QueryStrategyTest.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class Es8QueryStrategyTest {

	private final ObjectMapper mapper = new ObjectMapper();
	private final Es8QueryStrategy strategy = new Es8QueryStrategy("type_objet", List.of("CMP", "VET"), mapper);

	@Test
	void cheminTypeless() {
		assertEquals("/apogee/_search", strategy.searchPath("apogee"));
	}

	@Test
	void corpsContientMustEtFiltreTerms() throws Exception {
		final String body = strategy.buildBody(EsQueryBuilder.match("lib_desc_obj", "abc"));
		final JsonNode query = mapper.readTree(body).get("query").get("bool");
		assertEquals("abc", query.get("must").get(0).get("match").get("lib_desc_obj").asText());
		assertEquals("CMP", query.get("filter").get(0).get("terms").get("type_objet").get(0).asText());
		assertEquals("VET", query.get("filter").get(0).get("terms").get("type_objet").get(1).asText());
	}

	@Test
	void parseLitLeChampType() throws Exception {
		final String json = "{\"hits\":{\"hits\":[{\"_source\":{\"cod_obj\":\"A\",\"type_objet\":\"CMP\"}}]}}";
		final List<SearchHit> hits = strategy.parse(json);
		assertEquals(1, hits.size());
		assertEquals("CMP", hits.get(0).type());
	}

	@Test
	void parseSansChampTypeRenvoieNull() throws Exception {
		final String json = "{\"hits\":{\"hits\":[{\"_source\":{\"cod_obj\":\"A\"}}]}}";
		assertNull(strategy.parse(json).get(0).type());
	}
}
```

- [ ] **Step 2 : Lancer les tests pour vérifier l'échec**

Run: `mvn -q -Dtest=Es2QueryStrategyTest,Es8QueryStrategyTest test`
Expected: FAIL — classes introuvables.

- [ ] **Step 3 : Implémenter le minimum**

`EsQueryStrategy.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

/** Construit et interprète une requête de recherche selon la version d'Elasticsearch. */
public interface EsQueryStrategy {

	/** Chemin REST de recherche (ex. `/apogee/_search`). */
	String searchPath(String index);

	/** Corps JSON de la requête, en enveloppant le nœud de requête commun. */
	String buildBody(ObjectNode queryNode);

	/** Interprète la réponse JSON. */
	List<SearchHit> parse(String json) throws IOException;
}
```

`Es2QueryStrategy.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Stratégie Elasticsearch 2.x : recherche sur chemin typé, type lu dans `_type`. */
public class Es2QueryStrategy implements EsQueryStrategy {

	private final List<String> types;
	private final ObjectMapper mapper;

	public Es2QueryStrategy(final List<String> types, final ObjectMapper mapper) {
		this.types = types;
		this.mapper = mapper;
	}

	@Override
	public String searchPath(final String index) {
		return "/" + index + "/" + String.join(",", types) + "/_search";
	}

	@Override
	public String buildBody(final ObjectNode queryNode) {
		final ObjectNode body = mapper.createObjectNode();
		body.put("from", 0);
		body.put("size", 60);
		body.put("explain", true);
		body.set("query", queryNode);
		return body.toString();
	}

	@Override
	public List<SearchHit> parse(final String json) throws IOException {
		final List<SearchHit> hits = new ArrayList<>();
		for (final JsonNode hit : EsResponseParser.hits(json)) {
			final JsonNode type = hit.get("_type");
			hits.add(new SearchHit(EsResponseParser.source(hit), type == null ? null : type.asText()));
		}
		return hits;
	}
}
```

`Es8QueryStrategy.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Stratégie Elasticsearch 8/9 : recherche typeless, type lu dans un champ du document. */
public class Es8QueryStrategy implements EsQueryStrategy {

	private final String typeField;
	private final List<String> types;
	private final ObjectMapper mapper;

	public Es8QueryStrategy(final String typeField, final List<String> types, final ObjectMapper mapper) {
		this.typeField = typeField;
		this.types = types;
		this.mapper = mapper;
	}

	@Override
	public String searchPath(final String index) {
		return "/" + index + "/_search";
	}

	@Override
	public String buildBody(final ObjectNode queryNode) {
		final ObjectNode bool = mapper.createObjectNode();
		bool.putArray("must").add(queryNode);
		bool.putArray("filter").add(EsQueryBuilder.terms(typeField, types));
		final ObjectNode query = mapper.createObjectNode();
		query.set("bool", bool);
		final ObjectNode body = mapper.createObjectNode();
		body.put("from", 0);
		body.put("size", 60);
		body.put("explain", true);
		body.set("query", query);
		return body.toString();
	}

	@Override
	public List<SearchHit> parse(final String json) throws IOException {
		final List<SearchHit> hits = new ArrayList<>();
		for (final JsonNode hit : EsResponseParser.hits(json)) {
			final var source = EsResponseParser.source(hit);
			final Object type = source.get(typeField);
			hits.add(new SearchHit(source, type == null ? null : type.toString()));
		}
		return hits;
	}
}
```

- [ ] **Step 4 : Lancer les tests pour vérifier le succès**

Run: `mvn -q -Dtest=Es2QueryStrategyTest,Es8QueryStrategyTest test`
Expected: PASS.

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsQueryStrategy.java src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es2QueryStrategy.java src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es8QueryStrategy.java src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es2QueryStrategyTest.java src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/Es8QueryStrategyTest.java
git commit -m "feat(es): stratégies de requête ES 2.x et 8/9"
```

---

## Task 5 : Client HTTP `EsRestClient`

**Files:**
- Modify: `pom.xml` (ajouter WireMock en test)
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsRestClient.java`
- Test: `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsRestClientTest.java`

**Interfaces:**
- Consumes: —
- Produces: `new EsRestClient(String url, int port)` ; `getRoot() : String` ; `search(String path, String body) : String` (lève `IOException` si statut >= 300).

- [ ] **Step 1 : Ajouter WireMock au pom**

Dans `pom.xml`, dans le bloc `<dependencies>`, juste après la dépendance `jackson-dataformat-smile` (vers la ligne 336), ajouter :

```xml
		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-databind</artifactId>
		</dependency>
```

Puis dans le bloc `<!-- Tests -->`, après `spring-security-test`, ajouter :

```xml
		<dependency>
			<groupId>org.wiremock</groupId>
			<artifactId>wiremock-standalone</artifactId>
			<version>3.9.1</version>
			<scope>test</scope>
		</dependency>
```

- [ ] **Step 2 : Écrire le test qui échoue**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;

class EsRestClientTest {

	private WireMockServer server;

	@BeforeEach
	void demarrer() {
		server = new WireMockServer(options().dynamicPort());
		server.start();
	}

	@AfterEach
	void arreter() {
		server.stop();
	}

	@Test
	void getRootEtSearch() throws Exception {
		server.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"version\":{\"number\":\"8.13.0\"}}")));
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[]}}")));

		final EsRestClient client = new EsRestClient("localhost", server.port());
		assertTrue(client.getRoot().contains("8.13.0"));
		assertEquals("{\"hits\":{\"hits\":[]}}", client.search("/apogee/_search", "{}"));

		server.verify(postRequestedFor(urlEqualTo("/apogee/_search"))
			.withHeader("Content-Type", equalTo("application/json"))
			.withRequestBody(equalTo("{}")));
	}

	@Test
	void statutNon2xxLeve() {
		server.stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(500)));
		final EsRestClient client = new EsRestClient("localhost", server.port());
		assertThrows(IOException.class, client::getRoot);
	}
}
```

- [ ] **Step 3 : Lancer le test pour vérifier l'échec**

Run: `mvn -q -Dtest=EsRestClientTest test`
Expected: FAIL — `EsRestClient` introuvable.

- [ ] **Step 4 : Implémenter le minimum**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Client HTTP minimal vers Elasticsearch (indépendant de la version du serveur). */
public class EsRestClient {

	private final HttpClient http;
	private final String baseUrl;

	public EsRestClient(final String url, final int port) {
		this.baseUrl = "http://" + url + ":" + port;
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
	}

	/** `GET /` : renvoie la réponse racine (contient `version.number`). */
	public String getRoot() throws IOException, InterruptedException {
		return send(HttpRequest.newBuilder(URI.create(baseUrl + "/"))
			.timeout(Duration.ofSeconds(5))
			.GET()
			.build());
	}

	/** `POST {path}` avec un corps JSON. */
	public String search(final String path, final String body) throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
			.timeout(Duration.ofSeconds(5))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
			.build();
		return send(request);
	}

	private String send(final HttpRequest request) throws IOException, InterruptedException {
		final HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		if (response.statusCode() >= 300) {
			throw new IOException("Elasticsearch HTTP " + response.statusCode() + " sur " + request.uri());
		}
		return response.body();
	}
}
```

- [ ] **Step 5 : Lancer le test pour vérifier le succès**

Run: `mvn -q -Dtest=EsRestClientTest test`
Expected: PASS.

- [ ] **Step 6 : Commit**

```bash
git add pom.xml src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsRestClient.java src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/EsRestClientTest.java
git commit -m "feat(es): client HTTP REST + WireMock de test"
```

---

## Task 6 : Bascule atomique — interface, implémentation REST, retrait de mdw-tools

Cette tâche est **atomique** : le retrait de `mdw-tools` et la création des classes locales `ElasticSearchApogeeService` / `ElasticSearchApogeeServiceImpl` doivent se faire ensemble (mêmes noms de paquet), sinon conflit de classes.

**Files:**
- Modify: `pom.xml` (retirer le bloc mdw-tools)
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/ElasticSearchApogeeService.java`
- Create: `src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/ElasticSearchApogeeServiceImpl.java`
- Test: `src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/ElasticSearchApogeeServiceImplTest.java`

**Interfaces:**
- Consumes: `EsRestClient`, `EsVersion`, `EsQueryStrategy`, `Es2QueryStrategy`, `Es8QueryStrategy`, `EsQueryBuilder`, `SearchHit` (Tasks 1-5).
- Produces: `ElasticSearchApogeeService` (interface identique à mdw-tools) et son implémentation, avec les setters existants + `setElasticSearchChampTypeObjet(String)`.

- [ ] **Step 1 : Retirer la dépendance mdw-tools**

Dans `pom.xml`, supprimer le bloc :

```xml
		<!-- ElasticSearch (recherche rapide) -->
		<dependency>
			<groupId>fr.univlorraine</groupId>
			<artifactId>mdw-tools</artifactId>
			<version>2.0.5</version>
		</dependency>
```

- [ ] **Step 2 : Créer l'interface locale (reprise à l'identique)**

`ElasticSearchApogeeService.java` :
```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.util.List;
import java.util.Map;

/** Service de recherche dans Elasticsearch. */
public interface ElasticSearchApogeeService {

	List<Map<String, Object>> findObj(String value, int maxResult, boolean quickSearck);

	List<Map<String, Object>> findObj(String value, int maxResult, boolean quickSearck, String elasticSearchIndex,
		String elasticSearchChampRecherche, String elasticSearchChampCodeObjet, String elasticSearchChampVersionObjet);

	boolean initConnexion();

	boolean initConnexion(String url, int port, String cluster);
}
```

- [ ] **Step 3 : Écrire le test d'intégration qui échoue**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;

class ElasticSearchApogeeServiceImplTest {

	private WireMockServer server;
	private ElasticSearchApogeeServiceImpl service;

	@BeforeEach
	void demarrer() {
		server = new WireMockServer(options().dynamicPort());
		server.start();
		service = new ElasticSearchApogeeServiceImpl();
		service.setTypCmp("CMP");
		service.setTypVet("VET");
		service.setTypElp("ELP");
		service.setTypEtu("ETU");
		service.setEsTyp("TYP_OBJ");
		service.setElasticSearchUrl("localhost");
		service.setElasticSearchPort(server.port());
		service.setElasticSearchIndex("apogee");
		service.setElasticSearchChampRecherche("lib_desc_obj");
		service.setElasticSearchChampCodeObjet("cod_obj");
		service.setElasticSearchChampVersionObjet("cod_vrs_obj");
		service.setElasticSearchChampTypeObjet("type_objet");
	}

	@AfterEach
	void arreter() {
		server.stop();
	}

	private void stubVersion(final String version) {
		server.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"version\":{\"number\":\"" + version + "\"}}")));
	}

	@Test
	void es2xUtiliseLeCheminTypeEtLUnderscoreType() {
		stubVersion("2.3.5");
		server.stubFor(post(urlEqualTo("/apogee/CMP,VET,ELP,ETU/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[{\"_type\":\"CMP\",\"_source\":{\"cod_obj\":\"A\",\"cod_vrs_obj\":0,\"lib_obj\":\"A\"}}]}}")));

		assertTrue(service.initConnexion());
		final List<Map<String, Object>> results = service.findObj("abc", 10, false);

		assertEquals(1, results.size());
		assertEquals("CMP", results.get(0).get("TYP_OBJ"));
		server.verify(postRequestedFor(urlEqualTo("/apogee/CMP,VET,ELP,ETU/_search")));
	}

	@Test
	void es8xUtiliseLeCheminTypelessEtLeFiltreTerms() {
		stubVersion("8.13.0");
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[{\"_source\":{\"cod_obj\":\"A\",\"cod_vrs_obj\":0,\"lib_obj\":\"A\",\"type_objet\":\"CMP\"}}]}}")));

		assertTrue(service.initConnexion());
		final List<Map<String, Object>> results = service.findObj("abc", 10, false);

		assertEquals(1, results.size());
		assertEquals("CMP", results.get(0).get("TYP_OBJ"));
		server.verify(postRequestedFor(urlEqualTo("/apogee/_search"))
			.withRequestBody(containing("\"terms\":{\"type_objet\":[\"CMP\",\"VET\",\"ELP\",\"ETU\"]}")));
	}

	@Test
	void rechercheParCodeRepli() {
		stubVersion("8.13.0");
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[{\"_source\":{\"cod_obj\":\"ABC\",\"cod_vrs_obj\":0,\"lib_obj\":\"Lib\",\"type_objet\":\"CMP\"}}]}}")));

		assertTrue(service.initConnexion());
		final List<Map<String, Object>> results = service.findObj("abc", 10, false);
		assertEquals(1, results.size());
	}

	@Test
	void quickSearchUtiliseWildcard() {
		stubVersion("8.13.0");
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[]}}")));

		assertTrue(service.initConnexion());
		service.findObj("abc", 10, true);
		server.verify(postRequestedFor(urlEqualTo("/apogee/_search"))
			.withRequestBody(containing("\"wildcard\":{\"lib_desc_obj\":\"abc*\"}")));
	}

	@Test
	void valeurAvecCrochetsNeLevePas() {
		stubVersion("8.13.0");
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[]}}")));

		assertTrue(service.initConnexion());
		assertTrue(service.findObj("[]", 10, true).isEmpty());
	}

	@Test
	void champCodeAbsentNeLevePas() {
		stubVersion("8.13.0");
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[{\"_source\":{\"type_objet\":\"CMP\"}}]}}")));

		assertTrue(service.initConnexion());
		assertTrue(service.findObj("[abc]", 10, false).isEmpty());
	}

	@Test
	void reponseNon2xxRenvoieListeVide() {
		stubVersion("8.13.0");
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse().withStatus(404)));

		assertTrue(service.initConnexion());
		assertTrue(service.findObj("abc", 10, false).isEmpty());
	}

	@Test
	void versionNonSupporteeInitFalse() {
		stubVersion("6.8.0");
		assertFalse(service.initConnexion());
	}

	@Test
	void esInjoignableInitFalse() {
		server.stop();
		assertFalse(service.initConnexion());
	}
}
```

- [ ] **Step 4 : Lancer le test pour vérifier l'échec**

Run: `mvn -q -Dtest=ElasticSearchApogeeServiceImplTest test`
Expected: FAIL — `ElasticSearchApogeeServiceImpl` introuvable.

- [ ] **Step 5 : Implémenter `ElasticSearchApogeeServiceImpl`**

```java
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Recherche dans Elasticsearch via REST, compatible ES 2.x et ES 8/9. */
public class ElasticSearchApogeeServiceImpl implements ElasticSearchApogeeService {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchApogeeServiceImpl.class);

	private final ObjectMapper mapper = new ObjectMapper();

	private String typCmp = "";
	private String typVet = "";
	private String typElp = "";
	private String typEtu = "";
	private String esTyp = "";

	private EsRestClient client;
	private EsQueryStrategy strategy;

	private String elasticSearchCluster;
	private String elasticSearchUrl;
	private int elasticSearchPort;
	private String elasticSearchIndex;
	private String elasticSearchChampRecherche;
	private String elasticSearchChampCodeObjet;
	private String elasticSearchChampVersionObjet;
	private String elasticSearchChampTypeObjet;

	public ElasticSearchApogeeServiceImpl() {
		super();
	}

	@Override
	public boolean initConnexion() {
		return initConnexion(elasticSearchUrl, elasticSearchPort, elasticSearchCluster);
	}

	@Override
	public boolean initConnexion(final String url, final int port, final String cluster) {
		if (client == null) {
			try {
				final EsRestClient newClient = new EsRestClient(url, port);
				final String number = mapper.readTree(newClient.getRoot()).path("version").path("number").asText();
				final EsVersion version = EsVersion.fromNumber(number);
				final List<String> types = List.of(typCmp, typVet, typElp, typEtu);
				strategy = version == EsVersion.V2
					? new Es2QueryStrategy(types, mapper)
					: new Es8QueryStrategy(elasticSearchChampTypeObjet, types, mapper);
				client = newClient;
				LOG.info("Connexion Elasticsearch établie (version {}, stratégie {})", number, version);
			} catch (final Exception ex) {
				LOG.error("problème lors de l'initialisation de la connexion à ElasticSearch", ex);
				return false;
			}
		}
		return true;
	}

	@Override
	public List<Map<String, Object>> findObj(final String value, final int maxResult, final boolean quickSearck) {
		return findObj(value, maxResult, quickSearck, elasticSearchIndex, elasticSearchChampRecherche,
			elasticSearchChampCodeObjet, elasticSearchChampVersionObjet);
	}

	@Override
	public List<Map<String, Object>> findObj(final String value, final int maxResult, final boolean quickSearck,
		final String elasticSearchIndex, final String elasticSearchChampRecherche,
		final String elasticSearchChampCodeObjet, final String elasticSearchChampVersionObjet) {

		initConnexion();

		final List<Map<String, Object>> listeResultats = new LinkedList<>();

		if (StringUtils.hasText(value)) {
			try {
				boolean rechercherParCode = false;
				String lastWordCompletion = "*";

				String v = value.replaceAll("-", " ");
				v = v.replaceAll("\\(", " ");
				v = v.replaceAll("\\)", " ");
				while (v.contains("  ")) {
					v = v.replaceAll("  ", " ");
				}

				if (v.contains("[") && v.contains("]")) {
					v = v.replaceAll("\\[", "");
					v = v.split("\\]")[0];
					rechercherParCode = true;
				} else {
					v = v.replaceAll("\\[", "");
					v = v.replaceAll("\\]", "");
					if (v.substring(v.length() - 1).equals(" ")) {
						lastWordCompletion = "";
					}
					v = v.trim();
					v = v.replaceAll("-", "\\-");
				}
				v = v.toLowerCase();

				List<SearchHit> results = null;

				if (!rechercherParCode) {
					ObjectNode qb = EsQueryBuilder.match(elasticSearchChampRecherche, v);

					if (quickSearck) {
						final String[] mots = v.split(" ");
						if (mots.length == 1) {
							qb = EsQueryBuilder.wildcard(elasticSearchChampRecherche, mots[0] + lastWordCompletion);
						} else {
							final List<ObjectNode> clauses = new ArrayList<>();
							clauses.add(EsQueryBuilder.match(elasticSearchChampRecherche, mots[0]));
							for (int i = 1; i < mots.length; i++) {
								if (i < (mots.length - 1)) {
									clauses.add(EsQueryBuilder.match(elasticSearchChampRecherche, mots[i]));
								} else {
									clauses.add(EsQueryBuilder.wildcard(elasticSearchChampRecherche, mots[i] + lastWordCompletion));
								}
							}
							qb = EsQueryBuilder.boolMust(clauses);
						}
					}

					results = execute(elasticSearchIndex, qb);
				}

				if (rechercherParCode || ((results == null || results.isEmpty()) && !quickSearck)) {
					final ObjectNode qb = EsQueryBuilder.match(elasticSearchChampCodeObjet, v);
					results = execute(elasticSearchIndex, qb);
				}

				for (final SearchHit hit : results) {
					if (listeResultats.size() < (maxResult + 1)) {
						final Map<String, Object> result = hit.source();
						result.put(esTyp, hit.type());

						if (!rechercherParCode) {
							listeResultats.add(result);
						} else {
							final Object codresObj = result.get(elasticSearchChampCodeObjet);
							final Object vrsresObj = result.get(elasticSearchChampVersionObjet);
							if (codresObj == null || vrsresObj == null) {
								continue;
							}
							final String codres = codresObj.toString().toLowerCase();
							final int vrsres = ((Number) vrsresObj).intValue();
							if (!v.contains("/") && codres.equals(v)) {
								listeResultats.add(result);
							}
							if (v.contains("/") && (codres + "/" + vrsres).equals(v)) {
								listeResultats.add(result);
							}
						}
					}
				}
			} catch (final Exception ex) {
				LOG.error("Erreur lors de la recherche ElasticSearch", ex);
				return new LinkedList<>();
			}
		}

		return listeResultats;
	}

	private List<SearchHit> execute(final String index, final ObjectNode queryNode) throws Exception {
		final String body = strategy.buildBody(queryNode);
		return strategy.parse(client.search(strategy.searchPath(index), body));
	}

	// --- getters / setters ---

	public String getTypCmp() {
		return typCmp;
	}

	public void setTypCmp(final String typCmp) {
		this.typCmp = typCmp;
	}

	public String getTypVet() {
		return typVet;
	}

	public void setTypVet(final String typVet) {
		this.typVet = typVet;
	}

	public String getTypElp() {
		return typElp;
	}

	public void setTypElp(final String typElp) {
		this.typElp = typElp;
	}

	public String getTypEtu() {
		return typEtu;
	}

	public void setTypEtu(final String typEtu) {
		this.typEtu = typEtu;
	}

	public String getEsTyp() {
		return esTyp;
	}

	public void setEsTyp(final String esTyp) {
		this.esTyp = esTyp;
	}

	public String getElasticSearchCluster() {
		return elasticSearchCluster;
	}

	public void setElasticSearchCluster(final String elasticSearchCluster) {
		this.elasticSearchCluster = elasticSearchCluster;
	}

	public String getElasticSearchUrl() {
		return elasticSearchUrl;
	}

	public void setElasticSearchUrl(final String elasticSearchUrl) {
		this.elasticSearchUrl = elasticSearchUrl;
	}

	public int getElasticSearchPort() {
		return elasticSearchPort;
	}

	public void setElasticSearchPort(final int elasticSearchPort) {
		this.elasticSearchPort = elasticSearchPort;
	}

	public String getElasticSearchIndex() {
		return elasticSearchIndex;
	}

	public void setElasticSearchIndex(final String elasticSearchIndex) {
		this.elasticSearchIndex = elasticSearchIndex;
	}

	public String getElasticSearchChampRecherche() {
		return elasticSearchChampRecherche;
	}

	public void setElasticSearchChampRecherche(final String elasticSearchChampRecherche) {
		this.elasticSearchChampRecherche = elasticSearchChampRecherche;
	}

	public String getElasticSearchChampCodeObjet() {
		return elasticSearchChampCodeObjet;
	}

	public void setElasticSearchChampCodeObjet(final String elasticSearchChampCodeObjet) {
		this.elasticSearchChampCodeObjet = elasticSearchChampCodeObjet;
	}

	public String getElasticSearchChampVersionObjet() {
		return elasticSearchChampVersionObjet;
	}

	public void setElasticSearchChampVersionObjet(final String elasticSearchChampVersionObjet) {
		this.elasticSearchChampVersionObjet = elasticSearchChampVersionObjet;
	}

	public String getElasticSearchChampTypeObjet() {
		return elasticSearchChampTypeObjet;
	}

	public void setElasticSearchChampTypeObjet(final String elasticSearchChampTypeObjet) {
		this.elasticSearchChampTypeObjet = elasticSearchChampTypeObjet;
	}
}
```

- [ ] **Step 6 : Lancer le test pour vérifier le succès**

Run: `mvn -q -Dtest=ElasticSearchApogeeServiceImplTest test`
Expected: PASS (les 9 tests).

- [ ] **Step 7 : Compiler tout le projet**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS (les imports `SpringConfig` / `RechercheRapideView` pointent désormais vers les classes locales).

- [ ] **Step 8 : Commit**

```bash
git add pom.xml src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/ElasticSearchApogeeService.java src/main/java/fr/univlorraine/mondossierweb/tools/elasticsearch/ElasticSearchApogeeServiceImpl.java src/test/java/fr/univlorraine/mondossierweb/tools/elasticsearch/ElasticSearchApogeeServiceImplTest.java
git commit -m "feat(es): implémentation REST locale et retrait de mdw-tools"
```

---

## Task 7 : Câblage de configuration

**Files:**
- Modify: `src/main/java/fr/univlorraine/mondossierweb/utils/PropertyUtils.java` (après la ligne 111)
- Modify: `src/main/java/fr/univlorraine/mondossierweb/config/SpringConfig.java` (après la ligne 132)
- Modify: `application.yml` (après la ligne 112)
- Modify: `application.example.yml` (après la ligne 112)

**Interfaces:**
- Consumes: `ElasticSearchApogeeServiceImpl.setElasticSearchChampTypeObjet(String)` (Task 6).
- Produces: `PropertyUtils.getElasticSearchChampTypeObjet() : String`.

- [ ] **Step 1 : Ajouter le getter dans `PropertyUtils`**

Dans `PropertyUtils.java`, après la méthode `getElasticSearchChampLibelleObjet()` (ligne 111), ajouter :

```java
	/** Retourne le champ de l'index elasticSearch correspondant au type de l'objet (ES 8/9) */
	public static String getElasticSearchChampTypeObjet() {
		return getRequired("param.elasticsearch.index.champtypeobjet");
	}
```

- [ ] **Step 2 : Câbler dans `SpringConfig`**

Dans `SpringConfig.java`, après la ligne `esas.setElasticSearchChampVersionObjet(...)` (ligne 132), ajouter :

```java
		esas.setElasticSearchChampTypeObjet(PropertyUtils.getElasticSearchChampTypeObjet());
```

- [ ] **Step 3 : Ajouter la propriété dans `application.yml`**

Après la ligne `param.elasticsearch.index.champlibelleobjet: lib_obj` (ligne 112), ajouter :

```yaml
# Champ portant le type d'objet (CMP/VET/ELP/ETU) — utilisé uniquement avec ES 8/9
param.elasticsearch.index.champtypeobjet: type_objet
```

- [ ] **Step 4 : Ajouter la propriété dans `application.example.yml`**

Après la ligne `param.elasticsearch.index.champlibelleobjet: lib_obj` (ligne 112), ajouter la même ligne que ci-dessus.

- [ ] **Step 5 : Compiler**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 6 : Commit**

```bash
git add src/main/java/fr/univlorraine/mondossierweb/utils/PropertyUtils.java src/main/java/fr/univlorraine/mondossierweb/config/SpringConfig.java application.yml application.example.yml
git commit -m "feat(es): configuration du champ de type d'objet"
```

---

## Task 8 : Vérification finale

**Files:** aucun (vérification).

- [ ] **Step 1 : Lancer toute la suite de tests**

Run: `mvn -q test`
Expected: BUILD SUCCESS. Si des tests Spring context existants échouent pour une raison sans lien, le noter explicitement (ne pas masquer).

- [ ] **Step 2 : Vérifier l'absence de mdw-tools et du client ES 2.x**

Run: `mvn -q dependency:tree -Dincludes=fr.univlorraine:mdw-tools,org.elasticsearch:elasticsearch`
Expected: aucune ligne (dépendance absente).

- [ ] **Step 3 : Packager**

Run: `mvn -q -DskipTests package`
Expected: BUILD SUCCESS.

- [ ] **Step 4 : Smoke test manuel (hors CI)**

Checklist à exécuter sur un environnement réel :
1. ES 2.x : renseigner `param.elasticsearch.port` avec le **port HTTP** (pas 9302 transport) ; lancer l'app ; vérifier que la recherche rapide propose des résultats et que le type (`CMP`/`VET`/…) est correct.
2. ES 8/9 : pointer vers le nouvel index (contenant le champ `type_objet`) ; vérifier les mêmes résultats.
3. Cas d'erreur : ES arrêté → le champ de recherche ne s'affiche pas (initConnexion false), pas d'erreur bloquante.

- [ ] **Step 5 : Commit (si ajustements)**

```bash
git add -A
git commit -m "chore(es): vérification finale de la migration REST"
```

---

## Self-Review (couverture du spec)

| Exigence du spec | Tâche |
|---|---|
| Interface conservée (D6) | Task 6 |
| Retrait mdw-tools (D5) | Task 6 |
| JDK HttpClient + Jackson (D4) | Tasks 5, 6 |
| Détection version, V2/V8 | Tasks 1, 6 |
| Chemin typé 2.x + `_type` (D1, D2) | Tasks 4, 6 |
| Typeless 8/9 + `terms` sur `type_objet` (D2, D8) | Tasks 4, 6 |
| Normalisation numérique Integer | Task 3 |
| Config `champtypeobjet`, port HTTP, cluster obsolète | Tasks 6, 7 |
| Erreurs → false / liste vide | Task 6 (Review Focus 3, 5) |
| Tests WireMock sans Docker (D7) | Tasks 5, 6 |
| `RechercheRapideView` inchangé | Global Constraints (aucune tâche ne le modifie) |
| Cible sans sécurité (D3) | `EsRestClient` en HTTP simple |
