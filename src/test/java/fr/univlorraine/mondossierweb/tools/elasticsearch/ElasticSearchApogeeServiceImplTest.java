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
