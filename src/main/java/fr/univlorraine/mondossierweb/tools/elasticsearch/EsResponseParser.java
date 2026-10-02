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
