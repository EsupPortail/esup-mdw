package fr.univlorraine.mondossierweb.tools.elasticsearch;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;

class EsRestClientTest {

	private WireMockServer server;

	@BeforeEach
	void demarrer() {
		server = new WireMockServer(options().dynamicPort());
		server.start();
	}

	@AfterEach
	void arreter() {
		server.stop();
	}

	@Test
	void getRootEtSearch() throws Exception {
		server.stubFor(get(urlEqualTo("/")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"version\":{\"number\":\"8.13.0\"}}")));
		server.stubFor(post(urlEqualTo("/apogee/_search")).willReturn(aResponse()
			.withHeader("Content-Type", "application/json")
			.withBody("{\"hits\":{\"hits\":[]}}")));

		final EsRestClient client = new EsRestClient("localhost", server.port());
		assertTrue(client.getRoot().contains("8.13.0"));
		assertEquals("{\"hits\":{\"hits\":[]}}", client.search("/apogee/_search", "{}"));

		server.verify(postRequestedFor(urlEqualTo("/apogee/_search"))
			.withHeader("Content-Type", equalTo("application/json"))
			.withRequestBody(equalTo("{}")));
	}

	@Test
	void statutNon2xxLeve() {
		server.stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(500)));
		final EsRestClient client = new EsRestClient("localhost", server.port());
		assertThrows(IOException.class, client::getRoot);
	}
}
