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
