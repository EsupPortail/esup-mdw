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
package fr.univlorraine.mondossierweb.tools.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Stratégie Elasticsearch 8/9 : recherche typeless, type lu dans un champ du document. */
public class Es8QueryStrategy implements EsQueryStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(Es8QueryStrategy.class);

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
			if (type == null) {
				LOG.warn("Champ de type '{}' absent du _source du document Elasticsearch", typeField);
			}
			hits.add(new SearchHit(source, type == null ? null : type.toString()));
		}
		return hits;
	}
}
