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
