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
	void sourceConserveLongAuDelaDeIntegerMax() throws Exception {
		final String json = "{\"hits\":{\"hits\":[{\"_source\":{\"cod_vrs_obj\":3000000000}}]}}";
		final JsonNode hit = EsResponseParser.hits(json).get(0);
		final Map<String, Object> source = EsResponseParser.source(hit);
		assertEquals(Long.valueOf(3000000000L), source.get("cod_vrs_obj"));
	}

	@Test
	void reponseSansHitsRenvoieListeVide() throws Exception {
		assertTrue(EsResponseParser.hits("{}").isEmpty());
	}
}
