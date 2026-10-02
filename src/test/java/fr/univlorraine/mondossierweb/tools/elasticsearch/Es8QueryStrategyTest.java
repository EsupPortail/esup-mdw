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
