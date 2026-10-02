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
