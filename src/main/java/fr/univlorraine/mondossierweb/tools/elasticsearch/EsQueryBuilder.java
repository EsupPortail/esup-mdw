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
