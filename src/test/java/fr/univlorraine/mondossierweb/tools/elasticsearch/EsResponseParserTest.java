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
